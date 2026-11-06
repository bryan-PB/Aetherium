package com.aetherium.aetherium.world;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.config.AetheriumConfig;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

/**
 * Forge 1.16.5 structure pipeline: an actual /locate-compatible structure,
 * jigsaw NBT room pool, and region-based spacing. No chunk/player tick hooks.
 *
 * Based on the public Forge 1.16.3 structure tutorial by TelepathicGrunt;
 * no structure code or assets are bundled from other mods.
 */
public final class CitadelWorldGen {
    public static final DeferredRegister<Structure<?>> STRUCTURES =
            DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, AetheriumMod.MOD_ID);

    public static final RegistryObject<Structure<NoFeatureConfig>> CITADEL =
            STRUCTURES.register("citadel", () -> new CitadelStructure(NoFeatureConfig.CODEC));

    private static StructureFeature<?, ?> configuredCitadel;
    private static StructureSeparationSettings separation;

    private CitadelWorldGen() {}

    public static void register(IEventBus modBus) {
        STRUCTURES.register(modBus);
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Register the template room piece decoder before worlds can load.
            CitadelPieces.register();
            int spacing = AetheriumConfig.CITADEL_SPACING.get();
            int minDistance = Math.min(spacing - 1, AetheriumConfig.CITADEL_SEPARATION.get());
            separation = new StructureSeparationSettings(spacing, minDistance, 1583096713);

            Structure<?> structure = CITADEL.get();
            // Both maps are required by Forge 1.16.5 for /locate and world load.
            Structure.STRUCTURES_REGISTRY.put(structure.getRegistryName().toString(), structure);
            DimensionStructuresSettings.DEFAULTS =
                    ImmutableMap.<Structure<?>, StructureSeparationSettings>builder()
                            .putAll(DimensionStructuresSettings.DEFAULTS)
                            .put(structure, separation)
                            .build();

            // Also register the spacing for data-driven chunk generators that
            // consult noise-generator settings before WorldEvent.Load fires.
            WorldGenRegistries.NOISE_GENERATOR_SETTINGS.entrySet().forEach(entry -> {
                Map<Structure<?>, StructureSeparationSettings> map =
                        new HashMap<>(entry.getValue().structureSettings().structureConfig());
                map.putIfAbsent(structure, separation);
                entry.getValue().structureSettings().structureConfig = map;
            });

            configuredCitadel = CITADEL.get().configured(IFeatureConfig.NONE);
            Registry.register(WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE,
                    new ResourceLocation(AetheriumMod.MOD_ID, "configured_citadel"),
                    configuredCitadel);
            FlatGenerationSettings.STRUCTURE_FEATURES.put(structure, configuredCitadel);
        });
    }

    /**
     * Land biomes only. The old 4-category whitelist excluded entire common
     * spawn biomes such as savannas and deserts, giving /locate no candidates.
     * Dimension spacing is deliberately registered in the Overworld only.
     */
    public static void onBiomeLoading(BiomeLoadingEvent event) {
        if (!AetheriumConfig.CITADEL_ENABLED.get() || configuredCitadel == null) return;
        Biome.Category category = event.getCategory();
        if (category == Biome.Category.OCEAN
                || category == Biome.Category.RIVER
                || category == Biome.Category.BEACH
                || category == Biome.Category.SWAMP
                || category == Biome.Category.NETHER
                || category == Biome.Category.THEEND
                || category == Biome.Category.NONE) return;
        event.getGeneration().getStructures().add(() -> configuredCitadel);
    }

    /**
     * Without this step, a successfully registered 1.16.5 structure never
     * generates. Do it once at world load, not when chunks are ticking.
     */
    public static void onWorldLoad(WorldEvent.Load event) {
        if (!(event.getWorld() instanceof ServerWorld)
                || !AetheriumConfig.CITADEL_ENABLED.get() || separation == null) return;

        ServerWorld world = (ServerWorld) event.getWorld();
        if (!World.OVERWORLD.equals(world.dimension())
                || world.getChunkSource().getGenerator() instanceof FlatChunkGenerator) return;

        Map<Structure<?>, StructureSeparationSettings> map =
                new HashMap<>(world.getChunkSource().generator.getSettings().structureConfig());
        map.putIfAbsent(CITADEL.get(), separation);
        world.getChunkSource().generator.getSettings().structureConfig = map;
    }
}
