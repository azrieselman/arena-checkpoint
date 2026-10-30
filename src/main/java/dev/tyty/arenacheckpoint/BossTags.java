package dev.tyty.arenacheckpoint;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.Tags;

public final class BossTags {
    /**
     * Shipped tag. Includes the ender dragon and wither, and optionally {@code #c:bosses}.
     * File: data/arenacheckpoint/tags/entity_type/bosses.json
     */
    public static final TagKey<EntityType<?>> BOSSES = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ArenaCheckpoint.MOD_ID, "bosses"));

    private BossTags() {
    }

    public static boolean isBossType(Entity entity) {
        if (entity == null) {
            return false;
        }
        EntityType<?> type = entity.getType();
        return type.is(BOSSES) || type.is(Tags.EntityTypes.BOSSES);
    }
}
