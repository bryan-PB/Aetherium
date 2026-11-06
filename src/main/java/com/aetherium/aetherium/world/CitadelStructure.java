package com.aetherium.aetherium.world;

import com.aetherium.aetherium.AetheriumMod;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;

/**
 * Guaranteed connected, seven-room Citadel.
 *
 * The previous jigsaw version could produce an empty or incomplete start and
 * rejected low plains (Y<65). All seven original room NBTs now have explicit
 * chunk-safe locations, using Minecraft's normal template-structure pipeline.
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
        return canGenerateMegaSite(generator, x, z);
    }

    /**
     * Sample five main-axis points using WORLD_SURFACE_WG without generating
     * neighbor chunks. A 170-block building should not start across a sheer
     * cliff or an open water channel; foundations bridge at most 20 blocks.
     * This check runs only for candidate structure starts, never on ticks.
     */
    public static boolean canGenerateMegaSite(ChunkGenerator generator, int x, int z) {
        if (!canGenerateOnColumn(generator, x, z)) return false;
        int reference = generator.getFirstOccupiedHeight(x, z, Heightmap.Type.WORLD_SURFACE_WG);
        for (int dz : new int[] {32, 72, 112, 152}) {
            int height = generator.getFirstOccupiedHeight(x, z + dz, Heightmap.Type.WORLD_SURFACE_WG);
            if (height < 60 || height > 126 || Math.abs(height - reference) > 20) {
                return false;
            }
        }
        return true;
    }

    /**
     * Identical, side-effect-free terrain test for both native structure starts
     * and /kryzorae citadel probe. No chunks are generated and no world blocks
     * are changed when this method is called.
     */
    public static boolean canGenerateOnColumn(ChunkGenerator generator, int x, int z) {
        int height = generator.getFirstOccupiedHeight(x, z, Heightmap.Type.WORLD_SURFACE_WG);

        // Plains and many vanilla forests are at Y=62..64; the old Y>=65
        // condition excluded most ordinary regions and broke /locate results.
        if (height < 60 || height > 126) return false;
        IBlockReader column = generator.getBaseColumn(x, z);
        BlockState surface = column.getBlockState(new BlockPos(x, height - 1, z));
        return surface.getFluidState().isEmpty()
                && !surface.isAir();
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
            int y = generator.getFirstOccupiedHeight(
                    chunkX * 16 + 8, chunkZ * 16 + 8, Heightmap.Type.WORLD_SURFACE_WG);
            if (y < 60 || y > 126) return;

            // The central gateway determines the shared ground-floor Y.
            // Undercrypts are separate, strictly nonoverlapping Y ranges.
            if (!canGenerateMegaSite(generator, chunkX * 16 + 8, chunkZ * 16 + 8)) return;
            BlockPos origin = CitadelLayout.originForChunk(chunkX, chunkZ, y);
            try {
                for (CitadelLayout.RoomSpec room : CitadelLayout.rooms()) {
                    CitadelPieces.Room candidate = new CitadelPieces.Room(templates, room, origin);
                    for (net.minecraft.world.gen.feature.structure.StructurePiece existing : this.pieces) {
                        if (existing.getBoundingBox().intersects(candidate.getBoundingBox())) {
                            throw new IllegalStateException(
                                    "Overlapping Citadel rooms: " + room.template);
                        }
                    }
                    this.pieces.add(candidate);
                }
            } catch (IllegalStateException invalidLayout) {
                this.pieces.clear();
                AetheriumMod.LOGGER.error("Citadel templates/layout invalid; cannot generate",
                        invalidLayout);
                return;
            }
            this.calculateBoundingBox();
        }
    }
}
