package com.aetherium.aetherium.client.model;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Two genuinely DIFFERENT fitted 3-D silhouettes in Minecraft 1.16.5:
 *
 * Brumacero: closed ivory-steel knight helm, raised GOLD ridges, red brow crest,
 * overlapping shoulder shields, a gold-plated breast and heraldic tabard.
 *
 * Umbracita: tall, slightly asymmetrical wide-brimmed adventurer HAT,
 * violet ribbon and amber clasp, visible player eyes, lower-face scarf,
 * fabric coat, mantle, strapped details and light bracers.
 *
 * Every mesh is attached to its original biped pivot. No client tick, emissive
 * rendering, animated cape, shader or extra entity; only normal armor cuboids.
 * Brumacero encloses the head, while Umbracita intentionally leaves the
 * upper face exposed beneath the hat brim. No eyes are painted over the skin.
 *
 * Texture layout: 256x128, 48x48 patches at x=0/48/96/144/192,
 * row 0 / row 64. All boxes sample coherent material/trim/cloth patches.
 */
@OnlyIn(Dist.CLIENT)
public final class IntermediateArmorModel extends BipedModel<LivingEntity> {
    private static final int PLATE=0, SHADOW=48, GOLD=96, ACCENT=144, VOID=192;
    private static final int LIGHT=0, FABRIC=48, DETAIL=96, FACE=144, GLOW=192;
    private static final int ROW=64;

    private static IntermediateArmorModel steelOuter, steelLegs, shadeOuter, shadeLegs;

    private IntermediateArmorModel(boolean shade, boolean leggings) {
        super(0.0F);
        this.texWidth = 256;
        this.texHeight = 128;

        // Discard every vanilla textured armor cube: the old model only
        // recolored them, which caused the flat blocky appearance.
        this.head = part(0, 0, 0);
        this.hat = part(0, 0, 0);
        this.body = part(0, 0, 0);
        this.rightArm = part(-5, 2, 0);
        this.leftArm = part(5, 2, 0);
        this.rightLeg = part(-1.9F, 12, 0);
        this.leftLeg = part(1.9F, 12, 0);

        if (shade) {
            sorcererHat();
            shadowRobe();
        } else {
            knightHelm();
            knightChest();
        }
        arm(rightArm, false, shade);
        arm(leftArm, true, shade);
        leg(rightLeg, shade, leggings, false);
        leg(leftLeg, shade, leggings, true);
    }

    private ModelRenderer part(float x, float y, float z) {
        ModelRenderer m = new ModelRenderer(this);
        m.setPos(x,y,z);
        return m;
    }

    private void cube(ModelRenderer pivot, int u, int v,
                      float x, float y, float z, float w, float h, float d) {
        ModelRenderer item = new ModelRenderer(this, u, v);
        item.addBox(x,y,z,w,h,d);
        pivot.addChild(item);
    }

    private void knightHelm() {
        // Enclosed helmet: broad ivory metal crown, inset black T visor, warm-gold
        // nasal and brow. Unlike Violeta, the shape is compact and not horned.
        cube(head, PLATE,0, -4.5F,-8.3F,-4.45F, 9,8.7F,8.9F);
        cube(head, LIGHT,ROW, -4.7F,-8.65F,-4.65F, 9.4F,2.3F,9.3F);
        cube(head, SHADOW,0, -4.75F,-5.9F,-4.75F, 9.5F,5.8F,9.5F);
        cube(head, PLATE,0, -4.6F,-5.7F,-4.8F, 9.2F,5.4F,9.1F);
        cube(head, FACE,ROW, -3.45F,-5.65F,-5.16F, 6.9F,3.1F,0.65F);
        cube(head, GOLD,0, -4.7F,-6.45F,-5.28F, 9.4F,0.95F,1.05F);
        cube(head, GOLD,0, -0.85F,-7.9F,-5.47F, 1.7F,5.65F,0.83F);
        cube(head, ACCENT,0, -0.8F,-10.6F,-1.55F, 1.6F,3.15F,3.0F);
        // Crest runs over the crown; separate material so it reads as red.
        cube(head, GOLD,0, -1.35F,-8.85F,-4.9F, 2.7F,0.7F,9.3F);
        cube(head, ACCENT,0, -0.75F,-8.89F,-4.96F, 1.5F,0.8F,8.0F);
        cube(head, GOLD,0, -4.9F,-3.35F,-5.02F, 9.8F,0.85F,1.05F);
        cube(head, PLATE,0, -3.8F,-2.5F,-5.04F, 7.6F,2.65F,1.1F);
        cube(head, GOLD,0, -4.9F,-5.1F,-3.85F, 1.2F,3.4F,7.7F);
        cube(head, GOLD,0, 3.7F,-5.1F,-3.85F, 1.2F,3.4F,7.7F);
        // Raised back nape covers skin while sprinting and leaning.
        cube(head, SHADOW,0, -4.2F,-2.2F,4.25F, 8.4F,2.55F,0.75F);
    }

    /**
     * Umbracita is a real BROAD-BRIMMED HAT, not a hard-shell helmet or hood.
     * The face is deliberately open from Y=-6.5 to -3.1, so the wearer's
     * skin and eyes can show under the shadow of the brim. A cloth mask wraps
     * only the mouth/neck. The wide rim and tall crown follow the head pivot.
     */
    private void sorcererHat() {
        // Dark felt under-brim: very wide and thin, with a softly stepped
        // outline. The large horizontal silhouette is visible from the back,
        // front and sides (unlike the old cubic hood).
        cube(head, VOID, 0, -7.70F, -7.88F, -6.65F, 15.40F, 0.45F, 13.30F);
        cube(head, FABRIC, ROW, -7.22F, -8.10F, -6.30F, 14.44F, 0.43F, 12.60F);
        cube(head, SHADOW, 0, -7.10F, -7.78F, -7.12F, 14.20F, 0.55F, 0.90F);
        cube(head, LIGHT, ROW, -6.98F, -7.73F, -7.20F, 13.96F, 0.24F, 0.42F);
        cube(head, LIGHT, ROW, -7.08F, -7.75F, 6.18F, 14.16F, 0.24F, 0.48F);

        // Off-center brim ends make the hat feel worn instead of a perfect
        // flat square; these are still simple, cheap model cuboids.
        cube(head, FABRIC, ROW, -8.08F, -7.79F, -4.10F, 1.30F, 0.36F, 8.20F);
        cube(head, FABRIC, ROW, 6.88F, -7.86F, -3.65F, 1.20F, 0.36F, 7.30F);

        // Clear compact silhouette: the highest crown point now reaches y=-12.17,
        // down from -15.65 in the original hat (3.48 model pixels shorter).
        // The brim, face opening and scarf stay at their original locations.
        // Band, eye opening, scarf and other armor geometry stay unchanged.
        cube(head, VOID, 0, -3.95F, -9.18F, -3.85F, 7.90F, 1.50F, 7.70F);
        cube(head, FABRIC, ROW, -3.65F, -11.00F, -3.55F, 7.30F, 2.50F, 7.10F);
        cube(head, SHADOW, 0, -3.25F, -11.60F, -3.30F, 6.20F, 1.25F, 6.60F);
        cube(head, FABRIC, ROW, -2.80F, -12.00F, -3.05F, 5.50F, 0.70F, 6.05F);
        // Purple crown crease and folded-back top, rather than a sharp horn.
        cube(head, LIGHT, ROW, -2.10F, -12.17F, -2.55F, 4.35F, 0.35F, 5.10F);
        cube(head, SHADOW, 0, -3.05F, -11.15F, 2.95F, 6.10F, 1.45F, 0.70F);

        // Ribbon immediately above the brim and its single orange medallion.
        cube(head, SHADOW, 0, -3.92F, -9.74F, -3.92F, 7.84F, 1.26F, 7.84F);
        cube(head, LIGHT, ROW, -3.82F, -9.47F, -4.05F, 7.64F, 0.73F, 0.54F);
        cube(head, LIGHT, ROW, -3.82F, -9.47F, 3.51F, 7.64F, 0.73F, 0.54F);
        cube(head, ACCENT, 0, -0.75F, -9.80F, -4.34F, 1.50F, 1.32F, 0.60F);
        cube(head, GOLD, 0, -0.37F, -9.58F, -4.65F, 0.74F, 0.85F, 0.42F);

        // Do NOT add a solid cube in front of the eyes. Only a fitted lower
        // scarf/mask and compact fabric near the ears; hair and eyes stay free.
        cube(head, FACE, ROW, -4.14F, -2.90F, -4.61F, 8.28F, 2.92F, 0.49F);
        cube(head, FABRIC, ROW, -4.30F, -2.18F, -4.79F, 8.60F, 1.38F, 0.59F);
        cube(head, SHADOW, 0, -4.50F, -2.20F, -3.70F, 0.52F, 2.20F, 7.40F);
        cube(head, SHADOW, 0, 3.98F, -2.20F, -3.70F, 0.52F, 2.20F, 7.40F);
        cube(head, ACCENT, 0, -4.46F, -1.73F, -4.88F, 0.85F, 0.95F, 0.40F);
        cube(head, ACCENT, 0, 3.61F, -1.73F, -4.88F, 0.85F, 0.95F, 0.40F);
        // Compact rear scarf fold, not a second hood.
        cube(head, FABRIC, ROW, -3.65F, -1.86F, 4.13F, 7.30F, 1.80F, 0.75F);
    }

    private void knightChest() {
        // Continuous steel cuirass with layered breast plates and red center
        // gemstone; no floating solid-colored player torso or gap at back.
        cube(body, SHADOW,0, -4.5F,-0.25F,-2.65F, 9,12.1F,5.3F);
        cube(body, PLATE,0, -4.62F,0.0F,-2.95F, 9.24F,10.8F,5.9F);
        cube(body, GOLD,0, -4.75F,0.55F,-3.5F, 9.5F,1.1F,0.75F);
        cube(body, GOLD,0, -4.3F,5.75F,-3.47F, 8.6F,0.85F,0.8F);
        cube(body, LIGHT,ROW, -4.2F,2.0F,-3.53F, 3.7F,4.4F,0.9F);
        cube(body, LIGHT,ROW, 0.5F,2.0F,-3.53F, 3.7F,4.4F,0.9F);
        cube(body, GOLD,0, -1.0F,0.6F,-3.75F, 2.0F,6.6F,0.9F);
        cube(body, GOLD,0, -3.9F,7.1F,-3.47F, 7.8F,1.1F,0.9F);
        cube(body, ACCENT,0, -1.1F,3.35F,-4.16F, 2.2F,3.1F,0.85F);
        cube(body, GOLD,0, -1.55F,3.0F,-4.0F, 3.1F,0.5F,0.8F);
        cube(body, GOLD,0, -1.55F,6.5F,-4.0F, 3.1F,0.5F,0.8F);
        cube(body, SHADOW,0, -4.35F,8.55F,-3.2F, 8.7F,2.8F,6.35F);
        cube(body, GOLD,0, -4.45F,8.8F,-3.45F, 8.9F,1.1F,6.9F);
        cube(body, GOLD,0, -4.35F,10.3F,-3.32F, 8.7F,0.75F,6.64F);

        // Short ceremonial front-and-rear tabard is attached to torso rather
        // than legs, plus clean ivory panel / gold hem.
        cube(body, FABRIC,ROW, -1.75F,11.3F,-3.0F, 3.5F,4.1F,0.64F);
        cube(body, GOLD,0, -2.0F,11.65F,-3.21F, 0.6F,3.7F,0.77F);
        cube(body, GOLD,0, 1.4F,11.65F,-3.21F, 0.6F,3.7F,0.77F);
        cube(body, GOLD,0, -1.9F,14.8F,-3.22F, 3.8F,0.7F,0.78F);
        cube(body, FABRIC,ROW, -1.7F,11.35F,2.45F, 3.4F,3.5F,0.62F);
        cube(body, GOLD,0, -1.75F,14.5F,2.6F, 3.5F,0.65F,0.65F);
        // Back plates stay fitted and have a separate waist band.
        cube(body, LIGHT,ROW, -3.7F,1.45F,2.93F, 7.4F,4.8F,0.85F);
        cube(body, GOLD,0, -4.1F,7.35F,2.9F, 8.2F,0.95F,0.9F);
    }

    /**
     * Umbracita is a tailored coat, not a torso made of stacked armor blocks.
     * Dark shirt/vest, open purple lapels, layered cloth tails and a diagonally
     * strapped utility belt preserve the sorcerer-adventurer silhouette.
     * Stiff coat panels are short: no animated cloth or extra render passes.
     */
    private void shadowRobe() {
        // Subtle fitted silhouette; unbroken cloth around the back.
        cube(body, SHADOW, 0, -4.18F, 0.0F, -2.42F, 8.36F, 11.7F, 4.84F);
        cube(body, FABRIC, ROW, -4.36F, 0.15F, -2.68F, 8.72F, 11.15F, 5.36F);
        // High scarf collar remains beneath the compact hat; no hard cuirass.
        cube(body, SHADOW, 0, -4.35F, -0.45F, -3.02F, 8.70F, 1.58F, 6.04F);
        cube(body, LIGHT, ROW, -4.25F, 0.30F, -3.12F, 8.50F, 0.50F, 0.40F);
        // Front opens into a nearly-black vest, flanked by lapels and thin
        // lavender vertical piping. The undershirt breaks the old single cube.
        cube(body, SHADOW, 0, -2.27F, 1.15F, -3.06F, 4.54F, 8.55F, 0.54F);
        cube(body, PLATE, 0, -4.23F, 1.40F, -3.18F, 2.02F, 8.65F, 0.57F);
        cube(body, PLATE, 0, 2.21F, 1.40F, -3.18F, 2.02F, 8.65F, 0.57F);
        cube(body, LIGHT, ROW, -2.37F, 1.62F, -3.46F, 0.38F, 8.35F, 0.40F);
        cube(body, LIGHT, ROW, 1.99F, 1.62F, -3.46F, 0.38F, 8.35F, 0.40F);
        cube(body, FABRIC, ROW, -1.60F, 1.18F, -3.24F, 3.20F, 0.88F, 0.45F);
        // Small angular breast emblem and amber fastener.
        cube(body, ACCENT, 0, 2.72F, 2.95F, -3.77F, 0.80F, 1.77F, 0.52F);
        cube(body, GOLD, 0, 2.46F, 3.18F, -3.92F, 1.29F, 0.40F, 0.55F);

        // Diagonal brown leather band from the right shoulder to opposite hip.
        // The strap is a child of BODY, not a separate update/animation entity.
        ModelRenderer strap = part(2.83F, 1.05F, -3.64F);
        strap.zRot = 0.43F;
        body.addChild(strap);
        cube(strap, DETAIL, ROW, -0.61F, 0.0F, -0.18F, 1.22F, 9.32F, 0.56F);
        cube(strap, GOLD, 0, -0.68F, 3.30F, -0.34F, 1.36F, 0.52F, 0.74F);
        cube(strap, GOLD, 0, -0.68F, 6.25F, -0.34F, 1.36F, 0.52F, 0.74F);
        cube(strap, ACCENT, 0, -0.49F, 1.25F, -0.35F, 0.98F, 1.10F, 0.70F);

        // Tailored leather waist belt with polished buckle.
        cube(body, DETAIL, ROW, -4.34F, 9.50F, -3.12F, 8.68F, 0.86F, 6.24F);
        cube(body, GOLD, 0, -0.88F, 9.31F, -3.43F, 1.76F, 1.16F, 0.51F);
        cube(body, VOID, 0, -0.44F, 9.57F, -3.56F, 0.88F, 0.55F, 0.20F);
        // Adventurer's utility clasp and two hanging knife sheaths. Tiny
        // stylized parts stay attached to the torso, with no item interactions.
        cube(body, DETAIL, ROW, 2.87F, 9.80F, -3.60F, 0.73F, 2.90F, 0.75F);
        cube(body, LIGHT, ROW, 2.96F, 10.62F, -3.83F, 0.55F, 1.65F, 0.35F);
        cube(body, GOLD, 0, 2.78F, 10.40F, -3.89F, 0.96F, 0.42F, 0.42F);

        // Coattails are visibly open at the center and have staggered violet
        // hems, unlike the prior short solid box. End above the boot height.
        cube(body, FABRIC, ROW, -4.27F, 10.51F, -2.60F, 8.54F, 3.30F, 5.18F);
        cube(body, PLATE, 0, -4.68F, 11.05F, -3.17F, 2.58F, 6.98F, 0.66F);
        cube(body, PLATE, 0, 2.10F, 11.05F, -3.17F, 2.58F, 6.98F, 0.66F);
        cube(body, SHADOW, 0, -2.08F, 11.02F, -2.86F, 4.16F, 3.20F, 0.30F);
        cube(body, LIGHT, ROW, -4.81F, 11.15F, -3.47F, 0.40F, 6.76F, 0.37F);
        cube(body, LIGHT, ROW, 4.41F, 11.15F, -3.47F, 0.40F, 6.76F, 0.37F);
        cube(body, LIGHT, ROW, -4.81F, 17.64F, -3.47F, 2.73F, 0.37F, 0.39F);
        cube(body, LIGHT, ROW, 2.08F, 17.64F, -3.47F, 2.73F, 0.37F, 0.39F);

        // Split back of the long coat, so the legs remain separately readable.
        cube(body, FABRIC, ROW, -3.95F, 10.82F, 2.65F, 7.90F, 7.35F, 0.72F);
        cube(body, SHADOW, 0, -0.38F, 13.05F, 3.37F, 0.76F, 4.95F, 0.28F);
        cube(body, LIGHT, ROW, -3.92F, 17.71F, 3.37F, 7.84F, 0.39F, 0.35F);
        cube(body, LIGHT, ROW, -3.98F, 2.06F, 2.74F, 7.96F, 0.37F, 0.41F);
    }

    private void arm(ModelRenderer root, boolean left, boolean shade) {
        // Vanilla arm axes are offset by one additional pixel relative to
        // their pivot. Cover ALL 4x12x4 vanilla arm geometry plus the sleeve.
        ModelRenderer arm = part(left ? 1F : -1F, 0, 0);
        root.addChild(arm);
        cube(arm, SHADOW,0, -2.95F,-2.75F,-2.95F, 5.9F,14.9F,5.9F);
        cube(arm, shade ? FABRIC : PLATE,shade ? ROW : 0,
                -2.78F,-2.65F,-2.76F, 5.56F,14.55F,5.52F);

        if (shade) {
            // Fitted sleeves and light cloth shoulders, not giant pauldrons.
            cube(arm, SHADOW, 0, -3.32F, -3.20F, -3.10F, 6.64F, 3.75F, 6.20F);
            cube(arm, PLATE, 0, -3.48F, -3.27F, -3.20F, 6.96F, 1.70F, 6.40F);
            cube(arm, LIGHT, ROW, -3.44F, -1.86F, -3.44F, 6.88F, 0.45F, 0.45F);
            cube(arm, FABRIC, ROW, -2.78F, 0.20F, -2.91F, 5.56F, 5.60F, 5.82F);
            cube(arm, LIGHT, ROW, -2.80F, 5.30F, -3.10F, 5.60F, 0.43F, 6.20F);
            cube(arm, SHADOW, 0, -2.76F, 6.03F, -2.94F, 5.52F, 1.72F, 5.88F);
            cube(arm, FABRIC, ROW, -2.67F, 7.76F, -2.90F, 5.34F, 3.37F, 5.80F);
            // Subtle brown strap, small amber wrist cuff, no armor gauntlet.
            cube(arm, DETAIL, ROW, -2.89F, 8.42F, -3.16F, 5.78F, 0.62F, 6.32F);
            cube(arm, LIGHT, ROW, -2.88F, 10.42F, -3.13F, 5.76F, 0.48F, 6.26F);
            cube(arm, GOLD, 0, -0.62F, 8.31F, -3.35F, 1.24F, 0.84F, 0.38F);
        } else {
            // Articulated knight shoulders: TWO overlapping bronze-gold plates,
            // a raised outer flap and clean elbow / gauntlet separation.
            cube(arm, SHADOW,0, -4.4F,-3.7F,-3.65F, 8.8F,5.4F,7.3F);
            cube(arm, PLATE,0, -4.2F,-3.85F,-3.75F, 8.4F,3.3F,7.5F);
            cube(arm, GOLD,0, -4.48F,-0.65F,-3.78F, 8.96F,0.9F,7.56F);
            cube(arm, GOLD,0, -4.25F,-3.92F,-3.85F, 8.5F,0.65F,7.7F);
            cube(arm, PLATE,0, -3.25F,1.65F,-3.21F, 6.5F,3.65F,6.42F);
            cube(arm, SHADOW,0, -2.85F,5.15F,-3.12F, 5.7F,1.7F,6.24F);
            cube(arm, GOLD,0, -3.0F,5.3F,-3.43F, 6.0F,0.85F,6.86F);
            cube(arm, PLATE,0, -3.22F,7.0F,-3.26F, 6.44F,4.6F,6.52F);
            cube(arm, GOLD,0, -3.32F,7.15F,-3.42F, 6.64F,0.75F,6.84F);
            cube(arm, GOLD,0, -3.35F,10.8F,-3.48F, 6.7F,0.85F,6.96F);
            cube(arm, LIGHT,ROW, -1.5F,8.35F,-3.59F, 3.0F,1.55F,0.48F);
        }
    }

    private void leg(ModelRenderer root, boolean shade, boolean leggings,
                     boolean left) {
        // Boot and trouser segments occupy separate equipment slots. Their
        // parent pivots follow ordinary Minecraft walking and crouching.
        if (leggings) {
            if (shade) {
                // Close-fitting fabric rather than the older bulky armor thighs.
                cube(root, SHADOW, 0, -2.26F, -0.08F, -2.26F, 4.52F, 8.20F, 4.52F);
                cube(root, FABRIC, ROW, -2.35F, 0.08F, -2.31F, 4.70F, 7.88F, 4.62F);
                cube(root, PLATE, 0, -2.27F, 0.35F, -2.48F, 1.12F, 5.95F, 0.32F);
                cube(root, LIGHT, ROW, 1.19F, 0.48F, -2.48F, 0.36F, 6.50F, 0.32F);
                cube(root, SHADOW, 0, -2.35F, 6.30F, -2.48F, 4.70F, 1.05F, 4.96F);
                cube(root, LIGHT, ROW, -2.35F, 7.28F, -2.49F, 4.70F, 0.39F, 4.98F);
                // The asymmetrical thigh strap is only on one leg.
                if (left) {
                    cube(root, DETAIL, ROW, -2.44F, 3.10F, -2.51F, 4.88F, 0.64F, 5.02F);
                    cube(root, GOLD, 0, -0.92F, 3.00F, -2.88F, 1.54F, 0.86F, 0.48F);
                    cube(root, VOID, 0, -0.55F, 3.21F, -3.05F, 0.82F, 0.43F, 0.24F);
                }
            } else {
                cube(root, SHADOW, 0, -2.4F, -0.15F, -2.37F, 4.8F, 8.8F, 4.74F);
                cube(root, PLATE, 0, -2.53F, 0F, -2.52F, 5.06F, 7.85F, 5.04F);
                cube(root, GOLD, 0, -2.62F, 0.25F, -2.8F, 5.24F, 1.2F, 5.6F);
                cube(root, PLATE, 0, -2.63F, 3.0F, -2.77F, 5.26F, 3.4F, 5.54F);
                cube(root, GOLD, 0, -2.67F, 6.8F, -2.96F, 5.34F, 0.95F, 5.92F);
            }
        } else {
            if (shade) {
                // Adventurer mid-calf boots; cuff and heel, narrow toe plate.
                cube(root, SHADOW, 0, -2.23F, 8.00F, -2.28F, 4.46F, 3.99F, 4.56F);
                cube(root, FABRIC, ROW, -2.41F, 8.17F, -2.44F, 4.82F, 3.62F, 4.88F);
                cube(root, PLATE, 0, -2.50F, 8.16F, -2.53F, 5.00F, 1.08F, 5.06F);
                cube(root, LIGHT, ROW, -2.46F, 8.17F, -2.61F, 4.92F, 0.38F, 5.22F);
                cube(root, DETAIL, ROW, -2.44F, 9.82F, -2.57F, 4.88F, 0.58F, 5.14F);
                cube(root, GOLD, 0, -0.32F, 9.73F, -2.91F, 0.68F, 0.75F, 0.43F);
                cube(root, PLATE, 0, -2.49F, 10.78F, -3.02F, 4.98F, 1.20F, 5.57F);
                cube(root, LIGHT, ROW, -1.36F, 11.05F, -3.27F, 2.72F, 0.52F, 0.36F);
                cube(root, SHADOW, 0, -2.54F, 11.74F, -3.18F, 5.08F, 0.28F, 5.75F);
            } else {
                cube(root, SHADOW, 0, -2.48F, 7.9F, -2.5F, 4.96F, 4.15F, 5F);
                cube(root, PLATE, 0, -2.75F, 8.05F, -2.62F, 5.5F, 3.95F, 5.24F);
                cube(root, GOLD, 0, -2.85F, 8.12F, -2.8F, 5.7F, 0.9F, 5.6F);
                cube(root, PLATE, 0, -2.68F, 9.45F, -3.18F, 5.36F, 2.55F, 5.95F);
                cube(root, GOLD, 0, -2.83F, 11.13F, -3.25F, 5.66F, 0.85F, 6.4F);
            }
        }
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    public static <A extends BipedModel<?>> A forSlot(
            boolean shade, EquipmentSlotType slot, A original) {
        IntermediateArmorModel model;
        if (shade) {
            if (slot == EquipmentSlotType.LEGS) {
                if (shadeLegs == null) shadeLegs = new IntermediateArmorModel(true,true);
                model = shadeLegs;
            } else {
                if (shadeOuter == null) shadeOuter = new IntermediateArmorModel(true,false);
                model = shadeOuter;
            }
        } else {
            if (slot == EquipmentSlotType.LEGS) {
                if (steelLegs == null) steelLegs = new IntermediateArmorModel(false,true);
                model = steelLegs;
            } else {
                if (steelOuter == null) steelOuter = new IntermediateArmorModel(false,false);
                model = steelOuter;
            }
        }
        // Same animation transfer, biped pivots and equipment-slot partition
        // as the previously approved Violet / Crimson custom models.
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
