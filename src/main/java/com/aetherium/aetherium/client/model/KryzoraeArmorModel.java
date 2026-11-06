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

    /**
     * Complete articulated arm assembly. There was previously a small box on
     * top of the player's upper arm and almost no side panel; at a normal
     * camera distance the exposed player skin looked like missing armor.
     *
     * Each part is rooted in the proper biped ARM (never in the torso), so
     * shoulders do not detach from the arms while swinging or swimming.
     * The cap is almost ten model pixels wide: it remains visible in front,
     * side and rear views rather than disappearing into the breastplate.
     */
    private void buildArm(ModelRenderer arm, boolean left, boolean crimson) {
        // Root cause of the exposed turquoise player sleeves: Minecraft 1.16.5
        // biped arms do NOT have their cube centered on the arm pivot.
        // Right arm is x[-3,+1] at pivot -5; left x[-1,+3] at pivot +5.
        // The previous custom meshes used centered [-2.55,+2.55] cubes
        // and left the outer edge of either original arm uncovered.
        // Shift every decoration AND the solid under-sleeve together;
        // they now move in one rigid assembly with the correct arm pivot.
        final float vanillaOffsetX = left ? 1.0F : -1.0F;
        ModelRenderer sleeve = pivot(vanillaOffsetX, 0.0F, 0.0F);
        arm.addChild(sleeve);

        // Opaque continuous under-sleeve covers the entire vanilla 4x12x4
        // arm (including player skin/sleeve layer with 0.25 px inflation):
        // right actual x[-3.9,+1.9], left x[-1.9,+3.9].
        // The dark seam is visible only at joints, not as a bare skin strip.
        box(sleeve, RECESS, 64, -3.00F, -3.05F, -3.00F,
                6.00F, 15.20F, 6.00F);
        box(sleeve, LIMB, 0, -2.88F, -2.85F, -2.88F,
                5.76F, 14.80F, 5.76F);

        // Full upper-arm plate on all three visible sides, with inset front.
        box(sleeve, LIMB + 25, 22, -2.85F, 2.00F, -3.05F, 5.70F, 3.55F, 6.10F);
        box(sleeve, ACCENT, 64, -2.45F, 2.35F, -3.40F, 4.90F, 2.65F, 0.90F);
        float outside = left ? 2.65F : -3.55F;
        box(sleeve, EDGE, 64, outside, 2.4F, -2.55F, 0.90F, 3.25F, 5.10F);

        // Elbow: a recessed joint between two shells, then the front kneecap.
        box(sleeve, RECESS, 64, -2.70F, 5.30F, -2.78F, 5.40F, 1.65F, 5.56F);
        box(sleeve, EDGE, 64, -2.80F, 5.40F, -3.35F, 5.60F, 1.35F, 1.10F);
        box(sleeve, HIGHLIGHT, 64, -1.25F, 5.55F, -3.58F, 2.50F, 0.68F, 0.45F);

        // Broad lower bracer wraps the arm on all four sides, not just front.
        box(sleeve, BOOT, 0, -2.98F, 7.05F, -3.12F, 5.96F, 4.45F, 6.24F);
        box(sleeve, ACCENT, 64, -3.05F, 7.15F, -3.44F, 6.10F, 1.05F, 6.88F);
        box(sleeve, EDGE, 64, -3.12F, 10.58F, -3.50F, 6.24F, 1.00F, 7.00F);
        box(sleeve, HIGHLIGHT, 64, -1.00F, 8.20F, -3.65F, 2.00F, 2.15F, 0.46F);

        if (crimson) {
            // Carmesí: black iron foundation, layered red steel pauldrons.
            box(sleeve, EDGE, 64, -4.50F, -2.65F, -3.72F, 9.00F, 5.00F, 7.44F);
            box(sleeve, ACCENT, 64, -4.23F, -3.36F, -3.85F, 8.46F, 3.55F, 7.70F);
            box(sleeve, ACCENT, 64, -3.95F, 0.20F, -4.10F, 7.90F, 1.95F, 1.28F);
            box(sleeve, EDGE, 64, -3.60F, -3.70F, -3.25F, 7.20F, 0.72F, 6.50F);
            // Distinct silhouette: hooked armor spike on the OUTER side.
            float spike = left ? 3.70F : -6.25F;
            box(sleeve, EDGE, 64, spike, -3.30F, -2.35F, 2.55F, 3.70F, 4.70F);
            spike = left ? 4.75F : -6.75F;
            box(sleeve, EDGE, 64, spike, -5.10F, -1.60F, 2.00F, 2.30F, 3.20F);
            spike = left ? 5.40F : -6.60F;
            box(sleeve, EDGE, 64, spike, -6.60F, -0.85F, 1.20F, 2.15F, 1.70F);
            // A second lacquered plate with black surround, above the elbow.
            box(sleeve, ACCENT, 64, -2.15F, 3.00F, -3.62F, 4.30F, 1.42F, 0.75F);
            // Heavy layered knuckles and red reinforcement on the forearm.
            box(sleeve, ACCENT, 64, -2.75F, 8.25F, -3.92F, 5.50F, 2.30F, 0.65F);
            box(sleeve, HIGHLIGHT, 64, -0.75F, 8.64F, -4.14F, 1.50F, 1.35F, 0.46F);
            box(sleeve, EDGE, 64, -2.90F, 10.70F, -3.95F, 5.80F, 0.75F, 1.00F);
        } else {
            // Violeta: broad two-step ceremonial shoulder / pauldrons.
            box(sleeve, EDGE, 64, -4.30F, -2.55F, -3.50F, 8.60F, 5.10F, 7.00F);
            box(sleeve, ACCENT, 64, -4.02F, -3.27F, -3.62F, 8.04F, 2.75F, 7.24F);
            box(sleeve, HIGHLIGHT, 64, -3.83F, -3.64F, -3.25F, 7.66F, 0.72F, 6.50F);
            // Swept side fin attached to the cap; thick, not an invisible plane.
            float fin = left ? 3.75F : -6.05F;
            box(sleeve, ACCENT, 64, fin, -1.70F, -3.05F, 2.30F, 3.60F, 6.10F);
            fin = left ? 4.12F : -5.62F;
            box(sleeve, HIGHLIGHT, 64, fin, -2.23F, -2.65F, 1.50F, 0.75F, 5.30F);
            // Magenta front plate and raised seam on the bicep.
            box(sleeve, ACCENT, 64, -2.50F, 2.60F, -3.64F, 5.00F, 1.22F, 0.77F);
            box(sleeve, HIGHLIGHT, 64, -2.50F, 3.84F, -3.58F, 5.00F, 0.66F, 0.56F);
            // Crystal inset and bright cuff band tie the bracer to the torso.
            box(sleeve, EDGE, 64, -1.60F, 7.90F, -3.94F, 3.20F, 2.97F, 0.46F);
            box(sleeve, HIGHLIGHT, 64, -0.80F, 8.30F, -4.22F, 1.60F, 2.00F, 0.52F);
            box(sleeve, HIGHLIGHT, 64, -2.80F, 10.42F, -3.89F, 5.60F, 0.78F, 0.55F);
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
