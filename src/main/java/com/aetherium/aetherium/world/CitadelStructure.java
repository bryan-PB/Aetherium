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

    // A huge linked fortress cannot follow steep terrain independently at each
    // room: the 3x4 doorways would be at different heights. Instead choose a
    // dry, relatively level SITE and use the median ground height for every
    // above-ground module. The underground pieces retain negative offsets.
    private static final int MAX_RELIEF = 5;

    /**
     * Samples the REAL footprints of all fifteen above-ground modules, not
     * just the original 5 central-axis points. Returns -1 for unsuitable
     * terrain. Only chunk-generator base columns are queried: no neighboring
     * chunk is loaded, and no world blocks are read or changed.
     */
    public static int findPlacementY(ChunkGenerator generator, int x, int z) {
        java.util.List<Integer> centralHeights = new java.util.ArrayList<>();
        java.util.List<Integer> sampledHeights = new java.util.ArrayList<>();
        int baseX = x - 9;
        int baseZ = z - 7;

        for (CitadelLayout.RoomSpec room : CitadelLayout.rooms()) {
            if (room.offsetY < 0) continue;
            boolean quarterTurn = room.rotation == net.minecraft.util.Rotation.CLOCKWISE_90
                    || room.rotation == net.minecraft.util.Rotation.COUNTERCLOCKWISE_90;
            int width = quarterTurn ? room.depth : room.width;
            int depth = quarterTurn ? room.width : room.depth;
            int minX = baseX + room.offsetX;
            int minZ = baseZ + room.offsetZ;
            int midX = minX + width / 2;
            int midZ = minZ + depth / 2;
            int[][] samples = {
                    {midX, midZ},
                    {minX + 2, minZ + 2},
                    {minX + width - 3, minZ + depth - 3}
            };

            for (int i = 0; i < samples.length; i++) {
                int sx = samples[i][0];
                int sz = samples[i][1];
                int height = generator.getFirstOccupiedHeight(
                        sx, sz, Heightmap.Type.WORLD_SURFACE_WG);
                if (height < 60 || height > 126 || !isDryGround(generator, sx, sz, height)) {
                    return -1;
                }
                sampledHeights.add(height);
                if (i == 0) centralHeights.add(height);
            }

            // The largest plazas also require both opposite corners to be
            // tested; the 17-room citadel must never bridge entire ravines.
            if (width >= 23 || depth >= 23) {
                int[][] corners = {
                        {minX + width - 3, minZ + 2},
                        {minX + 2, minZ + depth - 3}
                };
                for (int[] pos : corners) {
                    int height = generator.getFirstOccupiedHeight(
                            pos[0], pos[1], Heightmap.Type.WORLD_SURFACE_WG);
                    if (height < 60 || height > 126
                            || !isDryGround(generator, pos[0], pos[1], height)) {
                        return -1;
                    }
                    sampledHeights.add(height);
                }
            }
        }
        if (centralHeights.isEmpty()) return -1;
        java.util.Collections.sort(centralHeights);
        int chosenY = centralHeights.get(centralHeights.size() / 2);
        for (int height : sampledHeights) {
            if (Math.abs(height - chosenY) > MAX_RELIEF) return -1;
        }
        return chosenY;
    }

    public static boolean canGenerateMegaSite(ChunkGenerator generator, int x, int z) {
        return findPlacementY(generator, x, z) >= 0;
    }

    /**
     * Light-weight local test used by /kryzorae citadel probe. Unlike the
     * full-footprint test, this checks only one column under the operator.
     */
    public static boolean canGenerateOnColumn(ChunkGenerator generator, int x, int z) {
        int height = generator.getFirstOccupiedHeight(x, z, Heightmap.Type.WORLD_SURFACE_WG);
        return height >= 60 && height <= 126 && isDryGround(generator, x, z, height);
    }

    private static boolean isDryGround(ChunkGenerator generator, int x, int z, int height) {
        IBlockReader column = generator.getBaseColumn(x, z);
        BlockState surface = column.getBlockState(new BlockPos(x, height - 1, z));
        return surface.getFluidState().isEmpty() && !surface.isAir();
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
            // This is the exact same site selector as isFeatureChunk().
            // Choosing a shared median prevents gates from jumping in height.
            int y = findPlacementY(
                    generator, chunkX * 16 + 8, chunkZ * 16 + 8);
            if (y < 0) return;
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
