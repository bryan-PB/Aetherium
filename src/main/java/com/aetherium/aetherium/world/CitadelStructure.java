package com.aetherium.aetherium.world;

import com.aetherium.aetherium.AetheriumMod;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.structure.VillageConfig;
import net.minecraft.world.gen.feature.template.TemplateManager;

/**
 * Five connected modular rooms on the surface of the Overworld. Each room is
 * a cached structure template processed by vanilla's chunk-safe jigsaw system.
 */
public final class CitadelStructure extends Structure<NoFeatureConfig> {
    public CitadelStructure(Codec<NoFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public IStartFactory<NoFeatureConfig> getStartFactory() {
        return Start::new;
    }

    @Override
    public GenerationStage.Decoration step() {
        return GenerationStage.Decoration.SURFACE_STRUCTURES;
    }

    @Override
    protected boolean isFeatureChunk(ChunkGenerator generator, BiomeProvider biomes,
                                     long seed, SharedSeedRandom random, int chunkX, int chunkZ,
                                     Biome biome, ChunkPos chunkPos, NoFeatureConfig config) {
        int x = chunkX * 16 + 8;
        int z = chunkZ * 16 + 8;
        int y = generator.getFirstOccupiedHeight(x, z, Heightmap.Type.WORLD_SURFACE_WG);
        if (y < 65 || y > 115) return false; // Never attempt ocean/sky fortresses.
        IBlockReader column = generator.getBaseColumn(x, z);
        BlockState ground = column.getBlockState(new BlockPos(x, y - 1, z));
        return ground.getFluidState().isEmpty();
    }

    public static final class Start extends StructureStart<NoFeatureConfig> {
        public Start(Structure<NoFeatureConfig> structure, int chunkX, int chunkZ,
                     MutableBoundingBox box, int references, long seed) {
            super(structure, chunkX, chunkZ, box, references, seed);
        }

        @Override
        public void generatePieces(DynamicRegistries registries, ChunkGenerator generator,
                                   TemplateManager templates, int chunkX, int chunkZ,
                                   Biome biome, NoFeatureConfig config) {
            BlockPos origin = new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8);
            JigsawManager.addPieces(
                    registries,
                    new VillageConfig(() -> registries.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY)
                            .get(new ResourceLocation(AetheriumMod.MOD_ID, "citadel/start")), 6),
                    AbstractVillagePiece::new, generator, templates, origin,
                    this.pieces, this.random, false, true);
            if (this.pieces.isEmpty()) return;
            this.pieces.forEach(piece -> piece.move(0, 1, 0));

            // The start pool anchors a corner by default; center on the safe
            // heightmap location used in isFeatureChunk.
            Vector3i center = this.pieces.get(0).getBoundingBox().getCenter();
            int dx = origin.getX() - center.getX();
            int dz = origin.getZ() - center.getZ();
            for (StructurePiece piece : this.pieces) piece.move(dx, 0, dz);
            this.calculateBoundingBox();
        }
    }
}
