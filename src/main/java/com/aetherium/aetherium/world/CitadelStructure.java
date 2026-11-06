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
            int cx = chunkX * 16 + 8;
            int cz = chunkZ * 16 + 8;
            int y = generator.getFirstOccupiedHeight(cx, cz, Heightmap.Type.WORLD_SURFACE_WG);
            if (y < 60 || y > 126) return;

            // No random attachment/retry: the boxes only meet at their door
            // faces. North/south doors are 3 wide and 4 tall, and all rooms
            // share a common floor Y. Side wings do not intersect the spine.
            int x = cx - 9;
            int z = cz - 7;
            try {
                this.pieces.add(new CitadelPieces.Room(templates, "gateway",
                        new BlockPos(x, y, z)));
                this.pieces.add(new CitadelPieces.Room(templates, "corridor",
                        new BlockPos(x + 4, y, z + 15)));
                this.pieces.add(new CitadelPieces.Room(templates, "guardhouse",
                        new BlockPos(x + 1, y, z + 32)));
                this.pieces.add(new CitadelPieces.Room(templates, "archive",
                        new BlockPos(x + 1, y, z + 49)));
                this.pieces.add(new CitadelPieces.Room(templates, "arena_shell",
                        new BlockPos(x - 4, y, z + 66)));
                this.pieces.add(new CitadelPieces.Room(templates, "crypt",
                        new BlockPos(x + 15, y, z + 15)));
                this.pieces.add(new CitadelPieces.Room(templates, "forge",
                        new BlockPos(x - 16, y, z + 32)));
            } catch (IllegalStateException missingTemplate) {
                this.pieces.clear();
                AetheriumMod.LOGGER.error("Citadel templates missing; cannot generate", missingTemplate);
                return;
            }
            this.calculateBoundingBox();
        }
    }
}
