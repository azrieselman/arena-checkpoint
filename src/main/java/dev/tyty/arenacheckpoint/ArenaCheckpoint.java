package dev.tyty.arenacheckpoint;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ArenaCheckpoint.MOD_ID)
public class ArenaCheckpoint {
    public static final String MOD_ID = "arenacheckpoint";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ArenaCheckpoint(IEventBus modEventBus, ModContainer modContainer) {
        ArenaAttachments.ATTACHMENT_TYPES.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, ArenaCheckpointConfig.SPEC);
        NeoForge.EVENT_BUS.register(BossFightTracker.class);
    }
}
