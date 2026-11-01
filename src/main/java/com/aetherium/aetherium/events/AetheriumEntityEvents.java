package com.aetherium.aetherium.events;

import com.aetherium.aetherium.config.AetheriumConfig;
import com.aetherium.aetherium.content.entity.LumiveloEntity;
import com.aetherium.aetherium.content.entity.RiftStalkerEntity;
import com.aetherium.aetherium.content.boss.RiftGuardianEntity;
import com.aetherium.aetherium.content.boss.AstralDevourerEntity;
import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/** Attribute, placement and biome-spawn registration for Aetherium creatures. */
public final class AetheriumEntityEvents {
    private AetheriumEntityEvents() {
    }

    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(RegistryHandler.RIFT_STALKER.get(), RiftStalkerEntity.createAttributes().build());
        event.put(RegistryHandler.LUMIVELO.get(), LumiveloEntity.createAttributes().build());
        event.put(RegistryHandler.RIFT_GUARDIAN.get(), RiftGuardianEntity.createAttributes().build());
        event.put(RegistryHandler.ASTRAL_DEVOURER.get(), AstralDevourerEntity.createAttributes().build());
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            EntitySpawnPlacementRegistry.register(RegistryHandler.RIFT_STALKER.get(),
                    EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,
                    Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MonsterEntity::checkMonsterSpawnRules);
            EntitySpawnPlacementRegistry.register(RegistryHandler.LUMIVELO.get(),
                    EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,
                    Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::checkAnimalSpawnRules);
        });
    }

    public static void onBiomeLoading(BiomeLoadingEvent event) {
        Biome.Category category = event.getCategory();
        if (category == Biome.Category.NETHER || category == Biome.Category.THEEND
                || category == Biome.Category.OCEAN || category == Biome.Category.RIVER) {
            return;
        }

        int stalkerWeight = AetheriumConfig.RIFT_STALKER_WEIGHT.get();
        if (stalkerWeight > 0) {
            event.getSpawns().getSpawner(EntityClassification.MONSTER).add(
                    new MobSpawnInfo.Spawners(RegistryHandler.RIFT_STALKER.get(), stalkerWeight, 1, 2));
        }

        int lumiveloWeight = AetheriumConfig.LUMIVELO_WEIGHT.get();
        if (lumiveloWeight > 0 && (category == Biome.Category.FOREST || category == Biome.Category.PLAINS
                || category == Biome.Category.TAIGA || category == Biome.Category.JUNGLE
                || category == Biome.Category.SAVANNA)) {
            event.getSpawns().getSpawner(EntityClassification.CREATURE).add(
                    new MobSpawnInfo.Spawners(RegistryHandler.LUMIVELO.get(), lumiveloWeight, 1, 2));
        }
    }
}
