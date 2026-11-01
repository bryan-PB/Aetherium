package com.aetherium.aetherium.world;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.config.AetheriumConfig;
import com.aetherium.aetherium.registry.RegistryHandler;
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

/** Registers one small, low-frequency ore feature and adds it to Overworld-like biomes. */
public final class AetheriumWorldGen {
    private static ConfiguredFeature<?, ?> configuredAetheriumOre;

    private AetheriumWorldGen() {
    }

    public static void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            int minY = AetheriumConfig.ORE_MIN_Y.get();
            int maxY = Math.max(minY, AetheriumConfig.ORE_MAX_Y.get());
            int veinSize = AetheriumConfig.ORE_VEIN_SIZE.get();
            int attempts = AetheriumConfig.ORE_VEINS_PER_CHUNK.get();

            ConfiguredFeature<?, ?> feature = Feature.ORE.configured(
                    new OreFeatureConfig(
                            OreFeatureConfig.FillerBlockType.NATURAL_STONE,
                            RegistryHandler.AETHERIUM_ORE.get().defaultBlockState(),
                            veinSize))
                    .decorated(Placement.RANGE.configured(
                            new TopSolidRangeConfig(minY, 0, maxY)))
                    .squared()
                    .count(attempts);

            configuredAetheriumOre = Registry.register(
                    WorldGenRegistries.CONFIGURED_FEATURE,
                    new ResourceLocation(AetheriumMod.MOD_ID, "aetherium_ore"),
                    feature);
        });
    }

    public static void onBiomeLoading(final BiomeLoadingEvent event) {
        if (configuredAetheriumOre == null) {
            return;
        }

        // BiomeLoadingEvent is dimension-agnostic in 1.16.5. Exclude the two vanilla
        // non-Overworld categories; this also supports most modded Overworld biomes.
        Biome.Category category = event.getCategory();
        if (category == Biome.Category.NETHER || category == Biome.Category.THEEND) {
            return;
        }

        event.getGeneration().addFeature(
                GenerationStage.Decoration.UNDERGROUND_ORES,
                configuredAetheriumOre);
    }
}
