package com.aetherium.aetherium.world;

import com.aetherium.aetherium.AetheriumMod;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.TemplateStructurePiece;
import net.minecraft.world.gen.feature.template.JigsawReplacementStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.Random;

/**
 * Direct NBT room pieces, like vanilla igloos, shipwrecks and ruined portals.
 * Unlike the old Citadel jigsaw pipeline, all seven rooms get an explicit,
 * deterministic location and are clipped per chunk by TemplateStructurePiece.
 * Nothing runs each tick: the templates are placed once during worldgen.
 */
public final class CitadelPieces {
    private static IStructurePieceType pieceType;

    private CitadelPieces() {}

    public static void register() {
        if (pieceType == null) {
            pieceType = IStructurePieceType.setPieceId(Room::new, "aetherium:citadel_room");
        }
    }

    public static final class Room extends TemplateStructurePiece {
        private ResourceLocation templateId;

        private static PlacementSettings placement() {
            return new PlacementSettings()
                    .setRotation(Rotation.NONE)
                    .setMirror(Mirror.NONE)
                    .addProcessor(JigsawReplacementStructureProcessor.INSTANCE)
                    .setIgnoreEntities(true);
        }

        public Room(TemplateManager manager, String room, BlockPos position) {
            super(pieceType, 0);
            this.templateId = new ResourceLocation(AetheriumMod.MOD_ID, "citadel/" + room);
            Template template = manager.get(this.templateId);
            if (template == null) {
                throw new IllegalStateException("Missing Kryzorae Citadel template: " + templateId);
            }
            this.setup(template, position, placement());
        }

        public Room(TemplateManager manager, CompoundNBT nbt) {
            super(pieceType, nbt);
            this.templateId = new ResourceLocation(nbt.getString("CitadelTemplate"));
            this.setup(manager.getOrCreate(this.templateId), new BlockPos(
                    nbt.getInt("CitadelX"), nbt.getInt("CitadelY"), nbt.getInt("CitadelZ")),
                    placement());
        }

        @Override
        protected void addAdditionalSaveData(CompoundNBT nbt) {
            super.addAdditionalSaveData(nbt);
            nbt.putString("CitadelTemplate", this.templateId.toString());
            nbt.putInt("CitadelX", this.templatePosition.getX());
            nbt.putInt("CitadelY", this.templatePosition.getY());
            nbt.putInt("CitadelZ", this.templatePosition.getZ());
        }

        @Override
        protected void handleDataMarker(String function, BlockPos pos, IServerWorld level,
                                        Random random, MutableBoundingBox boundingBox) {
            // Room loot comes from real chest NBT loot tables, not marker scans.
        }
    }
}
