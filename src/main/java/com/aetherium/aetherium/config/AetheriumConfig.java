package com.aetherium.aetherium.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Server-friendly common settings for Aetherium world generation. */
public final class AetheriumConfig {
    public static final ForgeConfigSpec COMMON_SPEC;

    public static final ForgeConfigSpec.IntValue ORE_VEIN_SIZE;
    public static final ForgeConfigSpec.IntValue ORE_VEINS_PER_CHUNK;
    public static final ForgeConfigSpec.IntValue ORE_MIN_Y;
    public static final ForgeConfigSpec.IntValue ORE_MAX_Y;
    public static final ForgeConfigSpec.IntValue RIFT_STALKER_WEIGHT;
    public static final ForgeConfigSpec.IntValue LUMIVELO_WEIGHT;
    public static final ForgeConfigSpec.IntValue MAGNET_RADIUS;
    public static final ForgeConfigSpec.IntValue MAGNET_MAX_ITEMS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Aetherium world generation settings.").push("worldgen");

        ORE_VEIN_SIZE = builder
                .comment("Maximum blocks per Aetherium ore vein. Small values reduce world-generation work.")
                .defineInRange("oreVeinSize", 4, 1, 8);
        ORE_VEINS_PER_CHUNK = builder
                .comment("Maximum vein placement attempts per chunk.")
                .defineInRange("oreVeinsPerChunk", 2, 0, 4);
        ORE_MIN_Y = builder
                .comment("Lowest Y level used by the Aetherium ore placement range.")
                .defineInRange("oreMinY", 4, 0, 16);
        ORE_MAX_Y = builder
                .comment("Highest Y level used by the Aetherium ore placement range.")
                .defineInRange("oreMaxY", 16, 4, 32);

        builder.pop();

        builder.comment("Creature spawn settings. Set a weight to 0 to disable natural spawning.").push("spawning");
        RIFT_STALKER_WEIGHT = builder
                .comment("Natural spawn weight for the hostile Rift Stalker. Lower values mean rarer spawns.")
                .defineInRange("riftStalkerWeight", 8, 0, 40);
        LUMIVELO_WEIGHT = builder
                .comment("Natural spawn weight for the peaceful Lumivelo in selected biomes.")
                .defineInRange("lumiveloWeight", 4, 0, 30);
        builder.pop();

        builder.comment("Optional utility settings. Magnet work is only performed while an active magnet is carried.").push("utility");
        MAGNET_RADIUS = builder
                .comment("Pickup magnet radius in blocks.")
                .defineInRange("magnetRadius", 4, 2, 6);
        MAGNET_MAX_ITEMS = builder
                .comment("Maximum item entities pulled per magnet pulse (one pulse every 10 ticks).")
                .defineInRange("magnetMaxItems", 8, 1, 16);
        builder.pop();

        COMMON_SPEC = builder.build();
    }

    private AetheriumConfig() {
    }
}
