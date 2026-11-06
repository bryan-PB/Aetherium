package com.aetherium.aetherium.world;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.config.AetheriumConfig;
import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.TopSolidRangeConfig;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/** Low-frequency, data-light ore generation. No per-tick or retroactive chunk scans. */
public final class AetheriumWorldGen {
    private static ConfiguredFeature<?, ?> configuredAetheriumOre;
    private static ConfiguredFeature<?, ?> configuredBrumaceroOre;
    private static ConfiguredFeature<?, ?> configuredUmbracitaOre;
    private static ConfiguredFeature<?, ?> configuredPrismalitaOre;

    private AetheriumWorldGen() {}

    /** Register once in common setup, matching the existing 1.16.5 placement pipeline. */
    public static void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            configuredAetheriumOre = registerOre("aetherium_ore",
                    RegistryHandler.AETHERIUM_ORE.get(),
                    AetheriumConfig.ORE_VEIN_SIZE.get(),
                    AetheriumConfig.ORE_VEINS_PER_CHUNK.get(),
                    AetheriumConfig.ORE_MIN_Y.get(), AetheriumConfig.ORE_MAX_Y.get());

            configuredBrumaceroOre = registerOre("brumacero_ore",
                    RegistryHandler.BRUMACERO_ORE.get(),
                    AetheriumConfig.BRUMACERO_VEIN_SIZE.get(),
                    AetheriumConfig.BRUMACERO_VEINS_PER_CHUNK.get(),
                    AetheriumConfig.BRUMACERO_MIN_Y.get(),
                    AetheriumConfig.BRUMACERO_MAX_Y.get());

            configuredUmbracitaOre = registerOre("umbracita_ore",
                    RegistryHandler.UMBRACITA_ORE.get(),
                    AetheriumConfig.UMBRACITA_VEIN_SIZE.get(),
                    AetheriumConfig.UMBRACITA_VEINS_PER_CHUNK.get(),
                    AetheriumConfig.UMBRACITA_MIN_Y.get(),
                    AetheriumConfig.UMBRACITA_MAX_Y.get());

            configuredPrismalitaOre = registerOre("prismalita_ore",
                    RegistryHandler.PRISMALITA_ORE.get(),
                    AetheriumConfig.PRISMALITA_VEIN_SIZE.get(),
                    AetheriumConfig.PRISMALITA_VEINS_PER_CHUNK.get(),
                    AetheriumConfig.PRISMALITA_MIN_Y.get(),
                    AetheriumConfig.PRISMALITA_MAX_Y.get());
        });
    }

    private static ConfiguredFeature<?, ?> registerOre(String id, Block ore, int veinSize,
                                                        int attempts, int configuredMinY,
                                                        int configuredMaxY) {
        int minY = Math.min(254, Math.max(0, configuredMinY));
        int maxY = Math.max(minY + 1, Math.min(255, configuredMaxY));
        ConfiguredFeature<?, ?> feature = Feature.ORE.configured(
                new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE,
                        ore.defaultBlockState(), veinSize))
                .decorated(Placement.RANGE.configured(
                        new TopSolidRangeConfig(minY, 0, maxY)))
                .squared()
                .count(attempts);

        return Registry.register(WorldGenRegistries.CONFIGURED_FEATURE,
                new ResourceLocation(AetheriumMod.MOD_ID, id), feature);
    }

    public static void onBiomeLoading(final BiomeLoadingEvent event) {
        // Same Overworld category filter as the original Kryzorae deposit.
        // This leaves Nether and End untouched and avoids all chunk-tick hooks.
        Biome.Category category = event.getCategory();
        if (category == Biome.Category.NETHER || category == Biome.Category.THEEND) return;

        if (configuredAetheriumOre != null) addOre(event, configuredAetheriumOre);
        if (configuredBrumaceroOre != null) addOre(event, configuredBrumaceroOre);
        if (configuredUmbracitaOre != null) addOre(event, configuredUmbracitaOre);
        if (configuredPrismalitaOre != null) addOre(event, configuredPrismalitaOre);
    }

    private static void addOre(BiomeLoadingEvent event, ConfiguredFeature<?, ?> ore) {
        event.getGeneration().addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, ore);
    }
}
