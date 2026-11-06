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
         * Chunk-local, one-time foundation. Four corner posts were not enough
         * for a 27x27 courtyard or the large sanctum: the walls and entire
         * floor often appeared to float above hillsides. We now continue the
         * OUTER WALL down to dry ground, and place sparse interior supports.
         *
         * Existing solid ground ends each column immediately. The maximum
         * depth is 20 and all writes stay inside Minecraft's supplied chunk
         * bounding box, so nothing generates adjoining chunks.
         */
        @Override
        public boolean postProcess(ISeedReader world, StructureManager structures,
                                   ChunkGenerator generator, Random random,
                                   MutableBoundingBox chunkBox, ChunkPos chunk,
                                   BlockPos pivot) {
            if (!super.postProcess(world, structures, generator, random, chunkBox, chunk, pivot)) {
                return false;
            }
            placeFoundations(world, chunkBox);
            return true;
        }

        /**
         * Creative preview normally bypasses StructurePiece.postProcess.
         * Explicitly create the same foundations after the NBT is placed, so
         * the preview does not falsely depict unsupported floating rooms.
         */
        public void placePreviewFoundations(ISeedReader world) {
            MutableBoundingBox bounds = this.getBoundingBox();
            MutableBoundingBox allowed = new MutableBoundingBox(
                    bounds.x0, 0, bounds.z0, bounds.x1, 255, bounds.z1);
            placeFoundations(world, allowed);
        }

        private void placeFoundations(ISeedReader world, MutableBoundingBox chunkBox) {
            // Upper crypt/archive have a SAME-FOOTPRINT undercroft. Supports
            // here would invade and block the underground ladders and halls.
            String id = templateId.getPath();
            if (id.endsWith("/catacombs") || id.endsWith("/archive_vault")
                    || id.endsWith("/crypt") || id.endsWith("/archive")) {
                return;
            }

            MutableBoundingBox bounds = this.getBoundingBox();
            int y = bounds.y0;
            for (int x = bounds.x0; x <= bounds.x1; x++) {
                supportColumn(world, chunkBox, x, bounds.z0, y);
                supportColumn(world, chunkBox, x, bounds.z1, y);
            }
            for (int z = bounds.z0 + 1; z < bounds.z1; z++) {
                supportColumn(world, chunkBox, bounds.x0, z, y);
                supportColumn(world, chunkBox, bounds.x1, z, y);
            }

            // Sparse interior piers on broad open plazas avoid a solid 27x27
            // foundation that would cause excessive worldgen block updates.
            if (bounds.x1 - bounds.x0 >= 22 || bounds.z1 - bounds.z0 >= 22) {
                for (int x = bounds.x0 + 5; x <= bounds.x1 - 5; x += 7) {
                    for (int z = bounds.z0 + 5; z <= bounds.z1 - 5; z += 7) {
                        supportColumn(world, chunkBox, x, z, y);
                    }
                }
            }
        }

        private static void supportColumn(ISeedReader world, MutableBoundingBox chunkBox,
                                          int x, int z, int floorY) {
            for (int y = floorY - 1; y >= Math.max(1, floorY - SUPPORT_DEPTH); y--) {
                BlockPos below = new BlockPos(x, y, z);
                if (!chunkBox.isInside(below)) return;
                BlockState existing = world.getBlockState(below);
                if (!existing.isAir() && existing.getFluidState().isEmpty()) return;
                world.setBlock(below, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
            }
        }

        @Override
        protected void handleDataMarker(String marker, BlockPos pos, IServerWorld world,
                                        Random random, MutableBoundingBox boundingBox) {
            // Loot, vanilla mob spawners and hazards live directly inside NBT.
        }
    }
}
