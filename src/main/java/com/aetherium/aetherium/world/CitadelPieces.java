package com.aetherium.aetherium.world;

import com.aetherium.aetherium.AetheriumMod;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.TemplateStructurePiece;
import net.minecraft.world.gen.feature.template.JigsawReplacementStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.Random;

/**
 * Forge 1.16.5 vanilla template pieces. Each NBT is placed only in the current
 * chunk's supplied bounding box. Rotation/mirror are serialized so structures
 * can be saved, unloaded and resumed without silently changing orientation.
 */
public final class CitadelPieces {
    private static IStructurePieceType pieceType;
    private static final int SUPPORT_DEPTH = 20;

    private CitadelPieces() {}

    public static void register() {
        if (pieceType == null) {
            pieceType = IStructurePieceType.setPieceId(Room::new, "aetherium:citadel_room");
        }
    }

    public static final class Room extends TemplateStructurePiece {
        private ResourceLocation templateId;
        private Rotation rotation;
        private Mirror mirror;

        private static PlacementSettings placement(Rotation rotation, Mirror mirror) {
            return new PlacementSettings()
                    .setRotation(rotation)
                    .setMirror(mirror)
                    .addProcessor(JigsawReplacementStructureProcessor.INSTANCE)
                    .setIgnoreEntities(true);
        }

        public Room(TemplateManager manager, CitadelLayout.RoomSpec room, BlockPos origin) {
            super(pieceType, 0);
            this.templateId = new ResourceLocation(AetheriumMod.MOD_ID, "citadel/" + room.template);
            this.rotation = room.rotation;
            this.mirror = room.mirror;
            Template template = manager.get(this.templateId);
            if (template == null) {
                throw new IllegalStateException("Missing Kryzorae Citadel template: " + templateId);
            }
            this.setup(template, room.position(origin), placement(rotation, mirror));
        }

        /**
         * Read prior 7-room saves too: old pieces had no rotation/mirror tags,
         * so vanilla's original orientation (NONE) remains the default.
         */
        public Room(TemplateManager manager, CompoundNBT nbt) {
            super(pieceType, nbt);
            this.templateId = new ResourceLocation(nbt.getString("CitadelTemplate"));
            this.rotation = nbt.contains("CitadelRotation", 8)
                    ? Rotation.valueOf(nbt.getString("CitadelRotation")) : Rotation.NONE;
            this.mirror = nbt.contains("CitadelMirror", 8)
                    ? Mirror.valueOf(nbt.getString("CitadelMirror")) : Mirror.NONE;
            this.setup(manager.getOrCreate(this.templateId), new BlockPos(
                    nbt.getInt("CitadelX"), nbt.getInt("CitadelY"), nbt.getInt("CitadelZ")),
                    placement(rotation, mirror));
        }

        @Override
        protected void addAdditionalSaveData(CompoundNBT nbt) {
            super.addAdditionalSaveData(nbt);
            nbt.putString("CitadelTemplate", this.templateId.toString());
            nbt.putString("CitadelRotation", rotation.name());
            nbt.putString("CitadelMirror", mirror.name());
            nbt.putInt("CitadelX", this.templatePosition.getX());
            nbt.putInt("CitadelY", this.templatePosition.getY());
            nbt.putInt("CitadelZ", this.templatePosition.getZ());
        }

        /**
         * After the normal chunk-clipped template placement, build four short
         * foundations beneath each above-ground module. The depth is strictly
         * capped; this never queries or writes outside the current chunk box,
         * never invokes neighbor chunk generation, and never touches the two
         * underground rooms. This prevents floating corners in shallow relief.
         */
        @Override
        public boolean postProcess(ISeedReader world, StructureManager structures,
                                   ChunkGenerator generator, Random random,
                                   MutableBoundingBox chunkBox, ChunkPos chunk,
                                   BlockPos pivot) {
            if (!super.postProcess(world, structures, generator, random, chunkBox, chunk, pivot)) {
                return false;
            }
            String id = templateId.getPath();
            if (id.endsWith("/catacombs") || id.endsWith("/archive_vault")) {
                return true;
            }
            MutableBoundingBox room = this.getBoundingBox();
            int[] xPoints = {room.x0 + 2, room.x1 - 2};
            int[] zPoints = {room.z0 + 2, room.z1 - 2};
            for (int x : xPoints) {
                for (int z : zPoints) {
                    for (int y = room.y0 - 1; y >= Math.max(1, room.y0 - SUPPORT_DEPTH); y--) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (!chunkBox.isInside(pos)) break;
                        BlockState existing = world.getBlockState(pos);
                        if (!existing.isAir() && existing.getFluidState().isEmpty()) break;
                        world.setBlock(pos, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                    }
                }
            }
            return true;
        }

        @Override
        protected void handleDataMarker(String marker, BlockPos pos, IServerWorld world,
                                        Random random, MutableBoundingBox boundingBox) {
            // Loot, vanilla mob spawners and hazards live directly inside NBT.
        }
    }
}
