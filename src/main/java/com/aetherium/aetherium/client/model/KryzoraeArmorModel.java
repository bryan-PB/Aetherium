package com.aetherium.aetherium.client.model;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Two complete armor silhouettes for Kryzorae.
 * Violet = the original aetherium equipment IDs, Crimson = astral IDs.
 *
 * Unlike the previous version this replaces ALL seven vanilla limb meshes with
 * purpose-built armor geometry. Every extra feature is parented to a vanilla
 * biped pivot; horns and shoulders follow animations instead of floating.
 *
 * The two custom 256x128 atlases contain dedicated panels for visor, metal,
 * joint fabric, chest and gemstone, all in pixel art. IDs are unchanged.
 */
@OnlyIn(Dist.CLIENT)
public final class KryzoraeArmorModel extends BipedModel<LivingEntity> {
    private static final int HELMET = 0, CHEST = 64, LIMB = 128, BOOT = 192;
    private static final int EDGE = 0, RECESS = 64, ACCENT = 128, HIGHLIGHT = 192;
    private static KryzoraeArmorModel violetOuter, violetLegs, crimsonOuter, crimsonLegs;

    private KryzoraeArmorModel(boolean crimson, boolean leggings) {
        super(0.0F);
        this.texWidth = 256;
        this.texHeight = 128;
        // Throw away the legacy vanilla armor cuboids. Their old 64x32 UVs
        // caused broken patches when superimposed on 3D decorations.
        this.head = pivot(0, 0, 0);
        this.hat = pivot(0, 0, 0);
        this.body = pivot(0, 0, 0);
        this.rightArm = pivot(-5, 2, 0);
        this.leftArm = pivot(5, 2, 0);
        this.rightLeg = pivot(-1.9F, 12, 0);
        this.leftLeg = pivot(1.9F, 12, 0);

        buildHelmet(crimson);
        buildChest(crimson);
        buildArm(rightArm, false, crimson);
        buildArm(leftArm, true, crimson);
        buildLeg(rightLeg, crimson, leggings);
        buildLeg(leftLeg, crimson, leggings);
    }

    private ModelRenderer pivot(float x, float y, float z) {
        ModelRenderer part = new ModelRenderer(this);
        part.setPos(x, y, z);
        return part;
    }

    private ModelRenderer box(ModelRenderer parent, int u, int v,
                              float x, float y, float z,
                              float w, float h, float d) {
        ModelRenderer part = new ModelRenderer(this, u, v);
        part.addBox(x, y, z, w, h, d);
        parent.addChild(part);
        return part;
    }

    private void buildHelmet(boolean crimson) {
        // Completely enclosed helmet; no exposed player skin in the crown.
        box(head, HELMET, 0, -4.6F, -8.6F, -4.5F, 9.2F, 9.0F, 9.0F);
        box(head, EDGE, 64, -4.9F, -7.0F, -4.95F, 9.8F, 1.0F, 1.5F);
        box(head, RECESS, 64, -3.7F, -5.65F, -5.08F, 7.4F, 3.05F, 0.65F);
        box(head, ACCENT, 64, -4.8F, -6.35F, -5.25F, 1.6F, 5.0F, 1.2F);
        box(head, ACCENT, 64, 3.2F, -6.35F, -5.25F, 1.6F, 5.0F, 1.2F);
        box(head, EDGE, 64, -3.4F, -2.25F, -5.0F, 6.8F, 1.8F, 1.0F);
        box(head, ACCENT, 64, -1.0F, -9.9F, -4.65F, 2.0F, 3.8F, 1.0F);
        // Glowing eyes are deliberately subtle; never a floating sheet.
        if (crimson) {
            box(head, HIGHLIGHT, 64, -3.05F, -5.2F, -5.48F, 2.1F, 0.85F, 0.4F);
            box(head, HIGHLIGHT, 64, 0.95F, -5.2F, -5.48F, 2.1F, 0.85F, 0.4F);
            // Jaw/fang guard is short and fitted to the face.
            for (int i = 0; i < 4; i++) {
                box(head, EDGE, 64, -2.8F + i * 1.55F,
                        -2.1F, -5.25F, 0.85F, 1.65F, 0.85F);
            }
            // Each horn is three joined cubes: broad root, tapered elbow,
            // narrow tip. They attach to the head pivot and do not float.
            buildHorn(-1);
            buildHorn(1);
        } else {
            box(head, ACCENT, 64, -3.7F, -8.6F, -4.75F, 7.4F, 0.8F, 0.7F);
            // A crest and two compact swept-back violet projections.
            box(head, HIGHLIGHT, 64, -1.0F, -11.2F, -2.1F, 2.0F, 3.2F, 3.8F);
            box(head, ACCENT, 64, -4.8F, -9.5F, -1.5F, 2.5F, 2.1F, 3.2F);
            box(head, ACCENT, 64, 2.3F, -9.5F, -1.5F, 2.5F, 2.1F, 3.2F);
            box(head, HIGHLIGHT, 64, -3.7F, -5.9F, -5.35F, 7.4F, 0.85F, 0.65F);
        }
        // Solid back panel and neck guard instead of exposing a broken gap.
        box(head, EDGE, 64, -3.3F, -3.3F, 4.3F, 6.6F, 3.2F, 1.0F);
    }

    private void buildHorn(int sign) {
        float x = sign < 0 ? -5.0F : 3.2F;
        box(head, EDGE, 64, x, -9.9F, -0.8F, 1.8F, 4.3F, 2.0F);
        x = sign < 0 ? -6.1F : 4.1F;
        box(head, EDGE, 64, x, -12.3F, -0.6F, 2.0F, 4.4F, 1.8F);
        x = sign < 0 ? -6.7F : 5.3F;
        box(head, EDGE, 64, x, -14.0F, -0.25F, 1.4F, 3.1F, 1.4F);
    }

    private void buildChest(boolean crimson) {
        // All chest pieces are one torso mesh; overlapping cuts are intentional
        // plate seams and do not pass through other animated body parts.
        box(body, CHEST, 0, -4.7F, -0.5F, -2.85F, 9.4F, 12.2F, 5.7F);
        box(body, EDGE, 64, -4.8F, 0.3F, -3.4F, 9.6F, 2.8F, 1.05F);
        box(body, ACCENT, 64, -4.4F, 2.8F, -3.5F, 4.05F, 4.6F, 0.95F);
        box(body, ACCENT, 64, 0.35F, 2.8F, -3.5F, 4.05F, 4.6F, 0.95F);
        box(body, EDGE, 64, -4.15F, 8.2F, -3.25F, 8.3F, 1.5F, 0.9F);
        box(body, RECESS, 64, -3.0F, 9.8F, -3.15F, 6.0F, 2.0F, 0.75F);
        box(body, ACCENT, 64, -3.9F, 0.5F, 2.75F, 7.8F, 5.0F, 1.0F);
        box(body, EDGE, 64, -3.4F, 7.2F, 2.8F, 6.8F, 4.0F, 0.65F);

        if (crimson) {
            // Broad overlapping breastplate ridge with a crimson chevron.
            box(body, ACCENT, 64, -1.05F, 1.1F, -4.0F, 2.1F, 7.6F, 1.35F);
            box(body, ACCENT, 64, -3.5F, 6.4F, -3.75F, 7.0F, 2.0F, 1.05F);
            box(body, HIGHLIGHT, 64, -0.8F, 8.2F, -3.75F, 1.6F, 2.8F, 0.9F);
        } else {
            // Dedicated chest jewel: recessed frame -> faceted crystal -> glint.
            box(body, EDGE, 64, -2.3F, 2.2F, -4.0F, 4.6F, 5.2F, 1.05F);
            box(body, HIGHLIGHT, 64, -1.55F, 2.9F, -4.6F, 3.1F, 3.75F, 1.0F);
            box(body, RECESS, 64, -3.6F, 7.3F, -3.85F, 7.2F, 1.0F, 1.1F);
            // Short hanging frontal and rear tabards stay WITH the chest slot,
            // leaving a deliberate gap so the thighs can bend when walking.
            box(body, ACCENT, 64, -1.7F, 11.8F, -2.85F, 3.4F, 2.2F, 0.85F);
            box(body, ACCENT, 64, -1.7F, 11.8F, 2.25F, 3.4F, 2.2F, 0.85F);
        }
    }

    private void buildArm(ModelRenderer arm, boolean left, boolean crimson) {
        box(arm, LIMB, 0, -2.3F, -1.0F, -2.35F, 4.6F, 12.2F, 4.7F);
        // Shoulder caps are attached to their own arm, not a floating torso.
        box(arm, ACCENT, 64, -3.25F, -1.7F, -3.1F, 6.5F, 4.3F, 6.2F);
        box(arm, EDGE, 64, -3.1F, 0.45F, -3.3F, 6.2F, 1.0F, 1.2F);
        box(arm, EDGE, 64, -2.55F, 5.3F, -2.75F, 5.1F, 1.2F, 5.5F);
        box(arm, ACCENT, 64, -2.75F, 7.1F, -3.0F, 5.5F, 4.4F, 6.0F);
        box(arm, HIGHLIGHT, 64, -1.05F, 8.25F, -3.4F, 2.1F, 2.0F, 0.85F);
        if (crimson) {
            float x = left ? 1.3F : -3.0F;
            box(arm, EDGE, 64, x, -4.1F, -0.6F, 1.7F, 3.2F, 1.8F);
            box(arm, ACCENT, 64, -2.35F, 1.7F, -3.45F, 4.7F, 1.3F, 1.0F);
        } else {
            // Violet pauldrons sweep outward via two stepped connected plates.
            float x = left ? 2.1F : -3.8F;
            box(arm, ACCENT, 64, x, -1.0F, -2.7F, 1.7F, 3.25F, 5.4F);
            box(arm, HIGHLIGHT, 64, -2.7F, 1.0F, -3.45F, 5.4F, 0.75F, 0.75F);
        }
    }

    private void buildLeg(ModelRenderer leg, boolean crimson, boolean leggings) {
        // The leggings contain only hips/thighs/knees. Boots contain only feet:
        // rendering the same full leg geometry in both slots caused z-fighting
        // and a "broken armor" appearance in the previous model.
        if (leggings) {
            box(leg, LIMB, 0, -2.45F, -0.05F, -2.4F, 4.9F, 8.45F, 4.8F);
            box(leg, ACCENT, 64, -2.8F, 0.15F, -2.9F, 5.6F, 3.3F, 1.25F);
            box(leg, EDGE, 64, -2.6F, 5.8F, -3.05F, 5.2F, 2.4F, 1.2F);
            if (crimson) {
                box(leg, ACCENT, 64, -1.2F, 0.4F, -3.15F, 2.4F, 5.7F, 0.85F);
            } else {
                box(leg, HIGHLIGHT, 64, -2.5F, 2.6F, -3.15F, 5.0F, 0.75F, 0.7F);
            }
        } else {
            box(leg, BOOT, 0, -2.55F, 8.0F, -2.6F, 5.1F, 4.0F, 5.3F);
            box(leg, ACCENT, 64, -2.7F, 8.1F, -2.85F, 5.4F, 1.2F, 5.8F);
            box(leg, EDGE, 64, -2.65F, 10.65F, -3.35F, 5.3F, 1.4F, 1.5F);
            if (crimson) {
                box(leg, EDGE, 64, -1.0F, 8.0F, -3.25F, 2.0F, 3.0F, 0.8F);
            } else {
                box(leg, HIGHLIGHT, 64, -2.3F, 9.0F, -3.15F, 4.6F, 0.75F, 0.65F);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <A extends BipedModel<?>> A forSlot(boolean crimson, EquipmentSlotType slot, A original) {
        KryzoraeArmorModel model;
        if (crimson) {
            if (slot == EquipmentSlotType.LEGS) {
                if (crimsonLegs == null) crimsonLegs = new KryzoraeArmorModel(true, true);
                model = crimsonLegs;
            } else {
                if (crimsonOuter == null) crimsonOuter = new KryzoraeArmorModel(true, false);
                model = crimsonOuter;
            }
        } else {
            if (slot == EquipmentSlotType.LEGS) {
                if (violetLegs == null) violetLegs = new KryzoraeArmorModel(false, true);
                model = violetLegs;
            } else {
                if (violetOuter == null) violetOuter = new KryzoraeArmorModel(false, false);
                model = violetOuter;
            }
        }
        // BipedModel's original crouch, swimming, age and swing state are
        // transferred before client ArmorLayer calls setupAnim on the result.
        ((BipedModel)original).copyPropertiesTo(model);
        model.setAllVisible(false);
        switch (slot) {
            case HEAD:
                model.head.visible = true;
                break;
            case CHEST:
                model.body.visible = true;
                model.rightArm.visible = true;
                model.leftArm.visible = true;
                break;
            case LEGS:
            case FEET:
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
                break;
            default:
                break;
        }
        return (A)model;
    }
}
