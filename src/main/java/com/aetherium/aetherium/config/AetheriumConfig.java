package com.aetherium.aetherium.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Server-friendly common settings for Aetherium world generation. */
public final class AetheriumConfig {
    public static final ForgeConfigSpec COMMON_SPEC;

    public static final ForgeConfigSpec.IntValue ORE_VEIN_SIZE;
    public static final ForgeConfigSpec.IntValue ORE_VEINS_PER_CHUNK;
    public static final ForgeConfigSpec.IntValue ORE_MIN_Y;
    public static final ForgeConfigSpec.IntValue ORE_MAX_Y;
    /**
     * New setting intentionally applied on top of the legacy per-ore counts:
     * existing 1.16.5 installations keep their preferences yet automatically
     * receive the more useful ore distribution without deleting config files.
     */
    public static final ForgeConfigSpec.IntValue NEW_ORE_DENSITY_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue BRUMACERO_VEIN_SIZE;
    public static final ForgeConfigSpec.IntValue BRUMACERO_VEINS_PER_CHUNK;
    public static final ForgeConfigSpec.IntValue BRUMACERO_MIN_Y;
    public static final ForgeConfigSpec.IntValue BRUMACERO_MAX_Y;
    public static final ForgeConfigSpec.IntValue UMBRACITA_VEIN_SIZE;
    public static final ForgeConfigSpec.IntValue UMBRACITA_VEINS_PER_CHUNK;
    public static final ForgeConfigSpec.IntValue UMBRACITA_MIN_Y;
    public static final ForgeConfigSpec.IntValue UMBRACITA_MAX_Y;
    public static final ForgeConfigSpec.IntValue PRISMALITA_VEIN_SIZE;
    public static final ForgeConfigSpec.IntValue PRISMALITA_VEINS_PER_CHUNK;
    public static final ForgeConfigSpec.IntValue PRISMALITA_MIN_Y;
    public static final ForgeConfigSpec.IntValue PRISMALITA_MAX_Y;
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


        NEW_ORE_DENSITY_MULTIPLIER = builder
                .comment("Global multiplier for Brumacero, Umbracita and Prismalita "
                        + "placement attempts in NEW chunks only. "
                        + "Default 6 raises legacy counts 2/1/1 to 12/6/6. "
                        + "Aetherium is unaffected. Set to 1 for old abundance. "
                        + "Each ore can still be disabled with its own veinsPerChunk=0; "
                        + "effective attempts are capped at 24 for performance.")
                .defineInRange("newOreDensityMultiplier", 6, 1, 8);

        BRUMACERO_VEIN_SIZE = builder
                .comment("BRUMACERO vein size in blocks; processed only during terrain generation.")
                .defineInRange("brumaceroVeinSize", 5, 1, 8);
        BRUMACERO_VEINS_PER_CHUNK = builder
                .comment("BRUMACERO placement attempts per chunk; set to zero to disable.")
                .defineInRange("brumaceroVeinsPerChunk", 2, 0, 6);
        BRUMACERO_MIN_Y = builder
                .comment("BRUMACERO minimum spawn height (1.16.5 Overworld).")
                .defineInRange("brumaceroMinY", 20, 0, 255);
        BRUMACERO_MAX_Y = builder
                .comment("BRUMACERO maximum spawn height (1.16.5 Overworld).")
                .defineInRange("brumaceroMaxY", 64, 1, 255);

        UMBRACITA_VEIN_SIZE = builder
                .comment("UMBRACITA vein size in blocks; processed only during terrain generation.")
                .defineInRange("umbracitaVeinSize", 4, 1, 8);
        UMBRACITA_VEINS_PER_CHUNK = builder
                .comment("UMBRACITA placement attempts per chunk; set to zero to disable.")
                .defineInRange("umbracitaVeinsPerChunk", 1, 0, 6);
        UMBRACITA_MIN_Y = builder
                .comment("UMBRACITA minimum spawn height (1.16.5 Overworld).")
                .defineInRange("umbracitaMinY", 8, 0, 255);
        UMBRACITA_MAX_Y = builder
                .comment("UMBRACITA maximum spawn height (1.16.5 Overworld).")
                .defineInRange("umbracitaMaxY", 36, 1, 255);

        PRISMALITA_VEIN_SIZE = builder
                .comment("PRISMALITA vein size in blocks; processed only during terrain generation.")
                .defineInRange("prismalitaVeinSize", 3, 1, 8);
        PRISMALITA_VEINS_PER_CHUNK = builder
                .comment("PRISMALITA placement attempts per chunk; set to zero to disable.")
                .defineInRange("prismalitaVeinsPerChunk", 1, 0, 6);
        PRISMALITA_MIN_Y = builder
                .comment("PRISMALITA minimum spawn height (1.16.5 Overworld).")
                .defineInRange("prismalitaMinY", 12, 0, 255);
        PRISMALITA_MAX_Y = builder
                .comment("PRISMALITA maximum spawn height (1.16.5 Overworld).")
                .defineInRange("prismalitaMaxY", 42, 1, 255);

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
