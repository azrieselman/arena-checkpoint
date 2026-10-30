package dev.tyty.arenacheckpoint;

import java.util.function.Supplier;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ArenaAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ArenaCheckpoint.MOD_ID);

    public static final Supplier<AttachmentType<BossEntry>> BOSS_ENTRY = ATTACHMENT_TYPES.register(
            "boss_entry",
            () -> AttachmentType.builder(() -> BossEntry.NONE).serialize(BossEntry.CODEC).build());

    private ArenaAttachments() {
    }
}
