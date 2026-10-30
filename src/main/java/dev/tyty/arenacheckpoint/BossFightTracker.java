package dev.tyty.arenacheckpoint;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Server-side record of where a player crossed into a boss's radius, and the
 * death relocation that uses it.
 *
 * <p>{@code ServerPlayer.die} fires {@code LivingDeathEvent} before it drops loot
 * or records the death location. Corpse (henkelmax) snapshots the player from
 * that event at {@link EventPriority#LOWEST} via corelib, then spawns its corpse
 * from the snapshot. Moving the player at {@link EventPriority#LOW} puts both
 * the corpse and vanilla drops on the entry point. This class does not reference
 * Corpse, so the game still loads when Corpse is absent.
 */
public final class BossFightTracker {
    /** Recent combat still counts as the current target. Ten seconds. */
    private static final int COMBAT_WINDOW_TICKS = 200;

    private static final Map<UUID, PendingMove> PENDING = new HashMap<>();

    private BossFightTracker() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player instanceof FakePlayer) {
            return;
        }
        if (player.isSpectator() || !player.isAlive()) {
            return;
        }

        long gameTime = player.level().getGameTime();
        PendingMove pending = PENDING.get(player.getUUID());
        if (pending != null && pending.gameTime != gameTime) {
            PENDING.remove(player.getUUID());
        }

        double radius = ArenaCheckpointConfig.entryRadius();
        boolean nearBoss = isNearLivingBoss(player, radius);
        BossEntry current = readEntry(player);

        if (nearBoss) {
            if (!current.inside()) {
                BossEntry updated = current.entered(
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        player.level().dimension().location().toString());
                player.setData(ArenaAttachments.BOSS_ENTRY.get(), updated);
                ArenaCheckpoint.LOGGER.info(
                        "Saved boss-fight entry for {} at {}, {}, {} in {}",
                        player.getGameProfile().getName(),
                        updated.x(),
                        updated.y(),
                        updated.z(),
                        updated.dimension());
            }
        } else if (current.inside()) {
            player.setData(ArenaAttachments.BOSS_ENTRY.get(), current.left());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        // Before Corpse's LOWEST snapshot, and before ServerPlayer.die drops loot.
        relocate(player, event.getSource(), true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Vec3 destination = relocate(player, event.getSource(), false);
        if (destination == null) {
            return;
        }
        for (ItemEntity drop : event.getDrops()) {
            drop.setPos(destination.x, destination.y, destination.z);
        }
    }

    private static Vec3 relocate(ServerPlayer player, DamageSource source, boolean log) {
        if (player instanceof FakePlayer || player.isSpectator()) {
            return null;
        }
        Vec3 destination = destination(player, source);
        if (destination == null) {
            return null;
        }
        moveToEntry(player, destination);
        if (log) {
            ArenaCheckpoint.LOGGER.info(
                    "Moved {}'s boss death to their fight entry at {}, {}, {}",
                    player.getGameProfile().getName(),
                    destination.x,
                    destination.y,
                    destination.z);
        }
        return destination;
    }

    private static Vec3 destination(ServerPlayer player, DamageSource source) {
        long gameTime = player.level().getGameTime();
        PendingMove pending = PENDING.get(player.getUUID());
        if (pending != null && pending.gameTime == gameTime) {
            return pending.position;
        }
        if (!shouldRelocate(player, source)) {
            return null;
        }
        BossEntry entry = readEntry(player);
        if (!entry.present() || entry.dimension().isEmpty()) {
            return null;
        }
        if (!entry.dimension().equals(player.level().dimension().location().toString())) {
            return null;
        }
        Vec3 position = new Vec3(entry.x(), entry.y(), entry.z());
        PENDING.put(player.getUUID(), new PendingMove(position, gameTime));
        return position;
    }

    private static boolean shouldRelocate(ServerPlayer player, DamageSource source) {
        if (isBossSource(source.getEntity()) || isBossSource(source.getDirectEntity())) {
            return true;
        }
        if (isRecentBoss(player, player.getLastHurtByMob(), player.getLastHurtByMobTimestamp())) {
            return true;
        }
        if (isRecentBoss(player, player.getLastHurtMob(), player.getLastHurtMobTimestamp())) {
            return true;
        }
        return isNearLivingBoss(player, ArenaCheckpointConfig.entryRadius());
    }

    private static boolean isBossSource(Entity entity) {
        if (BossTags.isBossType(entity)) {
            return true;
        }
        return entity instanceof TraceableEntity traceable && BossTags.isBossType(traceable.getOwner());
    }

    private static boolean isRecentBoss(ServerPlayer player, LivingEntity target, int timestamp) {
        if (!BossTags.isBossType(target) || !target.isAlive()) {
            return false;
        }
        return player.tickCount - timestamp <= COMBAT_WINDOW_TICKS;
    }

    private static boolean isNearLivingBoss(ServerPlayer player, double radius) {
        double radiusSqr = radius * radius;
        AABB area = player.getBoundingBox().inflate(radius);
        for (LivingEntity entity : player.serverLevel().getEntitiesOfClass(LivingEntity.class, area, candidate ->
                candidate != player && candidate.isAlive() && BossTags.isBossType(candidate))) {
            if (player.distanceToSqr(entity) <= radiusSqr) {
                return true;
            }
        }
        return false;
    }

    private static void moveToEntry(ServerPlayer player, Vec3 destination) {
        if (player.isPassenger()) {
            player.stopRiding();
        }
        player.fallDistance = 0.0F;
        player.setDeltaMovement(Vec3.ZERO);
        player.teleportTo(destination.x, destination.y, destination.z);
        player.setPos(destination.x, destination.y, destination.z);
        player.setLastDeathLocation(Optional.of(GlobalPos.of(
                player.level().dimension(),
                BlockPos.containing(destination.x, destination.y, destination.z))));
    }

    private static BossEntry readEntry(ServerPlayer player) {
        if (!player.hasData(ArenaAttachments.BOSS_ENTRY)) {
            return BossEntry.NONE;
        }
        return player.getData(ArenaAttachments.BOSS_ENTRY);
    }

    private record PendingMove(Vec3 position, long gameTime) {
    }
}
