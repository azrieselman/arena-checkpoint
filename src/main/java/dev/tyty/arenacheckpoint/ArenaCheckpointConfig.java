package dev.tyty.arenacheckpoint;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ArenaCheckpointConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue ENTRY_RADIUS = BUILDER
            .comment(
                    "Distance, in blocks, at which walking up to a living boss saves your position as the fight entry.",
                    "The saved spot is where you are at the moment you cross this radius. Leave and come back, and it is updated.",
                    "On a boss death, dropped items and an optional Corpse are placed at that saved spot.",
                    "Server config: saves/<world>/serverconfig/arenacheckpoint-server.toml (or <world>/serverconfig on a dedicated server).")
            .defineInRange("entryRadius", 48.0D, 1.0D, 512.0D);

    static final ModConfigSpec SPEC = BUILDER.build();

    private ArenaCheckpointConfig() {
    }

    public static double entryRadius() {
        return ENTRY_RADIUS.get();
    }
}
