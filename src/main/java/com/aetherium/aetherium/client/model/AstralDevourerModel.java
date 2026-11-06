package com.aetherium.aetherium.client.model;

import com.aetherium.aetherium.content.boss.AstralDevourerEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/**
 * Winged Executioner / Verdugo Alado.
 *
 * Built from scratch around a full-height masked torso, heavy gauntlets, articulated
 * four-part wings and a functional chest eye. No orbital rings or giant floating head.
 *
 * Texture: textures/entity/boss/winged_executioner.png (128x128).
 * All parts share a torso pivot so the attack poses cannot leave features behind.
 * Model coordinate Y is measured from the vanilla 24px standing origin.
 */
public class AstralDevourerModel extends EntityModel<AstralDevourerEntity> {
    private final ModelRenderer torso, head, maskChin;
    private final ModelRenderer leftChestDoor, rightChestDoor, eyeIrisGold, eyeIrisRed;
    private final ModelRenderer eyePupil, eyeHighlight, upperLid, lowerLid;
    private final ModelRenderer leftWing, rightWing, leftOuterWing, rightOuterWing;
    private final ModelRenderer leftWingScallop, rightWingScallop;
    private final ModelRenderer leftUpperArm, rightUpperArm, leftForearm, rightForearm;
    private final ModelRenderer leftClaw, rightClaw, leftLeg, rightLeg;
    private final ModelRenderer leftMantle, rightMantle;
    private final ModelRenderer leftRib, rightRib, leftWingBlade, rightWingBlade;
    private final ModelRenderer leftCrack, rightCrack;
    private final ModelRenderer[] castFeathers = new ModelRenderer[3];

    public AstralDevourerModel() {
        texWidth = 128;
        texHeight = 128;

        // A humanoid silhouette nearly three blocks high, rather than the old
        // one-block-tall hovering sphere inside a 2.8-block hitbox.
        torso = part(null, 0, 0, 0, 6.5F, 0, -6.5F, -15.5F, -3.8F, 13, 19.5F, 8);
        part(torso, 64, 96, 0, 1, -0.1F, -4.3F, 0, -3.3F, 8.6F, 3.4F, 6.6F);
        part(torso, 0, 0, 0, -14.6F, 0, -7.2F, -1.2F, -4.3F, 14.4F, 4.2F, 8.7F);
        part(torso, 0, 96, 0, -11.7F, 4.2F, -5.0F, -1, 0, 10, 1.7F, 2);
        part(torso, 0, 96, 0, -3.1F, 4.2F, -4.4F, 0, 0, 8.8F, 1.1F, 1.9F);

        // An executioner's blank ceramic face: the chest eye is the true gaze.
        head = part(torso, 0, 0, 0, -18, -0.4F, -4.7F, -7.1F, -4.0F, 9.4F, 8.7F, 8);
        part(head, 64, 0, 0, -0.2F, -4.15F, -3.75F, -5.8F, -0.8F, 7.5F, 7.1F, 1.2F);
        part(head, 0, 96, 0, -2.0F, -5.1F, -3.45F, -0.4F, -0.55F, 6.9F, 1.0F, 1.0F);
        maskChin = part(head, 64, 0, 0, 3.6F, -4.5F, -2.6F, 0, -0.5F, 5.2F, 3.5F, 1.2F);
        part(head, 0, 0, 0, -7.3F, -1.0F, -2.9F, -2.0F, -3.7F, 5.8F, 3.0F, 7.4F);

        // The eye is built into the breastplate, layered from socket to iris.
        // Its world height (~1.6 blocks) matches the server's laser origin.
        part(torso, 0, 64, 0, -8.5F, -4.6F, -5.0F, -4.8F, -0.7F, 10, 9.6F, 1.3F);
        leftChestDoor = part(torso, 0, 0, -4.65F, -8.5F, -5.25F,
                -0.15F, -4.0F, -0.45F, 2.6F, 8.0F, 0.9F);
        rightChestDoor = part(torso, 0, 0, 4.65F, -8.5F, -5.25F,
                -2.45F, -4.0F, -0.45F, 2.6F, 8.0F, 0.9F);
        eyeIrisGold = part(torso, 64, 64, 0, -8.5F, -5.45F,
                -3.55F, -3.3F, -0.5F, 7.1F, 6.6F, 0.8F);
        eyeIrisRed = part(torso, 96, 64, 0, -8.5F, -5.45F,
                -3.55F, -3.3F, -0.5F, 7.1F, 6.6F, 0.8F);
        eyePupil = part(torso, 0, 0, 0, -8.5F, -6.05F,
                -0.95F, -2.1F, -0.5F, 1.9F, 4.2F, 0.8F);
        eyeHighlight = part(torso, 64, 0, -0.8F, -10.0F, -6.58F,
                -0.35F, -0.45F, -0.3F, 0.7F, 0.9F, 0.6F);
        upperLid = part(torso, 64, 0, 0, -11.8F, -5.35F,
                -4.0F, -0.6F, -0.75F, 8.0F, 2.0F, 1.5F);
        lowerLid = part(torso, 64, 0, 0, -5.2F, -5.35F,
                -4.0F, -1.25F, -0.75F, 8.0F, 1.8F, 1.5F);
        // Split breastplate layers open in phases II and III.
        leftRib = part(torso, 0, 96, -5.2F, -9.5F, -4.5F,
                -1.3F, -3.0F, -0.65F, 2.3F, 7.1F, 1.3F);
        rightRib = part(torso, 0, 96, 5.2F, -9.5F, -4.5F,
                -1.0F, -3.0F, -0.65F, 2.3F, 7.1F, 1.3F);
        leftCrack = part(torso, 96, 64, -3.2F, -1.5F, -4.2F,
                -1.2F, -0.4F, -0.3F, 2.4F, 0.9F, 0.6F);
        rightCrack = part(torso, 96, 64, 3.2F, -1.5F, -4.2F,
                -1.2F, -0.4F, -0.3F, 2.4F, 0.9F, 0.6F);

        // Oversized executioner gauntlets flank the chest eye, but never cover it.
        part(torso, 0, 0, -7.0F, -13.1F, 0, -4.1F, -2.0F, -4.1F, 4.8F, 5.2F, 8.2F);
        part(torso, 0, 0, 7.0F, -13.1F, 0, -0.7F, -2.0F, -4.1F, 4.8F, 5.2F, 8.2F);
        leftUpperArm = part(torso, 0, 0, -8.7F, -10.3F, 0,
                -2.0F, 0, -2.1F, 4.0F, 9.0F, 4.2F);
        rightUpperArm = part(torso, 0, 0, 8.7F, -10.3F, 0,
                -2.0F, 0, -2.1F, 4.0F, 9.0F, 4.2F);
        leftForearm = part(leftUpperArm, 0, 96, 0, 8, -0.5F,
                -2.7F, -0.4F, -2.1F, 5.4F, 7.6F, 4.2F);
        rightForearm = part(rightUpperArm, 0, 96, 0, 8, -0.5F,
                -2.7F, -0.4F, -2.1F, 5.4F, 7.6F, 4.2F);
        leftClaw = part(leftForearm, 64, 0, -1.3F, 7.1F, -1.1F,
                -0.85F, 0, -0.8F, 1.7F, 4.0F, 1.6F);
        rightClaw = part(rightForearm, 64, 0, 1.3F, 7.1F, -1.1F,
                -0.85F, 0, -0.8F, 1.7F, 4.0F, 1.6F);
        part(leftForearm, 64, 0, 1.0F, 7.0F, -1.1F,
                -0.7F, 0, -0.8F, 1.4F, 3.4F, 1.6F);
        part(rightForearm, 64, 0, -1.0F, 7.0F, -1.1F,
                -0.7F, 0, -0.8F, 1.4F, 3.4F, 1.6F);

        // Split armor skirt and dangling talons keep the bottom silhouette active.
        leftMantle = part(torso, 64, 96, -3.0F, 2.7F, 0.0F,
                -3.2F, 0, -3.6F, 5.8F, 13.0F, 6.8F);
        rightMantle = part(torso, 64, 96, 3.0F, 2.7F, 0.0F,
                -2.6F, 0, -3.6F, 5.8F, 11.0F, 6.8F);
        leftLeg = part(torso, 0, 0, -3.0F, 2.7F, 1.6F,
                -1.6F, 0, -2, 3.2F, 10.8F, 4.0F);
        rightLeg = part(torso, 0, 0, 3.0F, 2.7F, 1.6F,
                -1.6F, 0, -2, 3.2F, 10.8F, 4.0F);
        part(leftLeg, 64, 0, 0, 10.0F, -1.5F, -1.7F, 0, -1.8F, 3.4F, 3.0F, 3.0F);
        part(rightLeg, 64, 0, 0, 10.0F, -1.5F, -1.7F, 0, -1.8F, 3.4F, 3.0F, 3.0F);

        // Four large, articulated surfaces: a pair of root wings and outer
        // folding sections. Thick leading edges + dark, uneven membranes.
        leftWing = part(torso, 0, 96, -6.5F, -13.0F, 2.7F,
                -16, -1.4F, -1.0F, 16.5F, 2.8F, 2.0F);
        rightWing = part(torso, 0, 96, 6.5F, -13.0F, 2.7F,
                -0.5F, -1.4F, -1.0F, 16.5F, 2.8F, 2.0F);
        part(leftWing, 0, 32, -1, 0.5F, 0,
                -13.5F, 0, -0.75F, 13.2F, 10.5F, 1.5F);
        part(rightWing, 0, 32, 1, 0.5F, 0,
                0.3F, 0, -0.75F, 13.2F, 10.5F, 1.5F);
        // A descending scallop prevents the membrane reading as a rectangle.
        leftWingScallop = part(leftWing, 64, 32, -9.3F, 5.5F, 0,
                -4.0F, 0, -0.6F, 6.8F, 6.5F, 1.2F);
        rightWingScallop = part(rightWing, 64, 32, 9.3F, 5.5F, 0,
                -2.8F, 0, -0.6F, 6.8F, 6.5F, 1.2F);
        leftOuterWing = part(leftWing, 0, 96, -15.7F, -0.8F, 0,
                -13.5F, -1.0F, -0.75F, 14.0F, 2.0F, 1.5F);
        rightOuterWing = part(rightWing, 0, 96, 15.7F, -0.8F, 0,
                -0.5F, -1.0F, -0.75F, 14.0F, 2.0F, 1.5F);
        part(leftOuterWing, 64, 32, -1.0F, 0.4F, 0,
                -11.7F, 0, -0.65F, 12.0F, 10.0F, 1.3F);
        part(rightOuterWing, 64, 32, 1.0F, 0.4F, 0,
                -0.3F, 0, -0.65F, 12.0F, 10.0F, 1.3F);
        part(leftOuterWing, 0, 32, -8.5F, 4.5F, 0,
                -2.2F, 0, -0.5F, 6.0F, 8.0F, 1.0F);
        part(rightOuterWing, 0, 32, 8.5F, 4.5F, 0,
                -3.8F, 0, -0.5F, 6.0F, 8.0F, 1.0F);
        leftWingBlade = part(leftOuterWing, 64, 0, -12.5F, -0.4F, 0,
                -0.8F, -5.0F, -0.8F, 1.6F, 6.2F, 1.6F);
        rightWingBlade = part(rightOuterWing, 64, 0, 12.5F, -0.4F, 0,
                -0.8F, -5.0F, -0.8F, 1.6F, 6.2F, 1.6F);
        // Three sheathed black iron feathers materialize during the volley
        // warning. Their jagged vanes and narrow quills read as BLADES,
        // never as hovering prisms/crystals.
        for (int i = 0; i < castFeathers.length; i++) {
            float x = (i - 1) * 5.0F;
            castFeathers[i] = part(torso, 0, 96, x, -8.5F, -7.0F,
                    -0.55F, -3.9F, -0.42F, 1.1F, 7.0F, 0.84F);
            part(castFeathers[i], 64, 96, -0.7F, -2.2F, 0.0F,
                    -0.75F, -1.4F, -0.5F, 1.5F, 3.6F, 1.0F);
            part(castFeathers[i], 0, 96, 0.7F, -1.0F, 0.0F,
                    -0.75F, -1.2F, -0.5F, 1.5F, 2.5F, 1.0F);
        }
    }

    private ModelRenderer part(ModelRenderer parent, int u, int v,
                               float px, float py, float pz,
                               float x, float y, float z, float w, float h, float d) {
        ModelRenderer result = new ModelRenderer(this, u, v);
        result.setPos(px, py, pz);
        result.addBox(x, y, z, w, h, d);
        if (parent != null) parent.addChild(result);
        return result;
    }

    @Override
    public void setupAnim(AstralDevourerEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        int phase = entity.getPhase();
        int state = entity.getAttackState();
        float time = entity.getAttackAnimationTicks();
        float idle = MathHelper.sin(ageInTicks * 0.09F);
        // During a dash all idle flapping is suspended: wings fold into an
        // aerodynamic silhouette instead of paddling at full speed.
        float flap = MathHelper.sin(ageInTicks * (phase >= 3 ? 0.25F : 0.18F))
                * ((state == 6 || state == 7) ? 0.08F : 1.0F);
        float casting = state == 1 ? 1.0F : 0.0F;
        // Keep the extended 32-tick pincer telegraph visibly charged from the outset.
        float castingCharge = casting * MathHelper.clamp((32.0F - time) / 16.0F, 0, 1);
        float laserCharge = state == 2 ? MathHelper.clamp(1.0F - time / 26.0F, 0, 1) : 0;
        float laserFire = state == 3 ? 1.0F : 0.0F;
        float dashReady = state == 6 ? 1.0F : 0.0F;
        float dash = state == 7 ? 1.0F : 0.0F;
        float brace = dashReady * MathHelper.sin(ageInTicks * 1.4F) * 0.040F;
        float well = state == 4 ? 1.0F : 0.0F;
        float nova = state == 5 ? 1.0F : 0.0F;
        float stagger = state == 8 ? 1.0F : 0.0F;
        float transforming = state == 9
                ? MathHelper.sin((1.0F - time / 54.0F) * (float)Math.PI) : 0.0F;
        float spread = (phase >= 2 ? 0.20F : 0) + (phase >= 3 ? 0.15F : 0);
        float open = phase == 1 ? 0.2F : (phase == 2 ? 0.6F : 1.0F);
        open += castingCharge * 0.4F + laserCharge * 0.35F
                + laserFire * 0.5F + transforming * 0.5F - dash * 0.22F;

        // Three clearly different poses: upright hover, drawn-back windup,
        // then the 55-degree committed spear-like lunge.
        torso.y = 6.5F + idle * 0.45F - transforming * 0.65F
                - dashReady * 0.8F + dash * 0.45F + brace * 2.0F
                + stagger * 0.25F;
        torso.xRot = headPitch * 0.004F - dashReady * 0.31F
                + dash * 0.96F - nova * 0.24F - laserFire * 0.07F
                + brace + entity.deathTime * 0.035F;
        torso.yRot = netHeadYaw * 0.003F;
        torso.zRot = MathHelper.sin(ageInTicks * 0.06F) * 0.025F
                * (1.0F - dashReady - dash);
        head.xRot = -0.05F + MathHelper.sin(ageInTicks * 0.07F) * 0.04F
                + casting * 0.20F + laserCharge * 0.15F
                + dashReady * 0.12F - dash * 0.23F + transforming * 0.16F;
        head.yRot = netHeadYaw * ((float)Math.PI / 180F) * 0.25F;
        maskChin.xRot = nova * 0.16F + transforming * 0.18F;

        eyeIrisGold.visible = phase < 3;
        eyeIrisRed.visible = phase >= 3;
        eyePupil.y = -8.5F + MathHelper.sin(ageInTicks * 0.09F) * 0.15F;
        eyePupil.z = -6.05F - Math.min(0.16F, (laserCharge + castingCharge) * 0.12F);
        eyeHighlight.y = -10.0F + MathHelper.sin(ageInTicks * 0.15F) * 0.15F;
        upperLid.y = -11.8F - 1.35F * open;
        lowerLid.y = -5.2F + 0.9F * open;
        leftChestDoor.yRot = -0.1F - open * 0.65F;
        rightChestDoor.yRot = 0.1F + open * 0.65F;
        leftRib.visible = phase >= 2;
        rightRib.visible = phase >= 2;
        leftRib.yRot = 0.15F + open * 0.18F;
        rightRib.yRot = -leftRib.yRot;
        leftCrack.visible = phase >= 3;
        rightCrack.visible = phase >= 3;

        // The two hinges per wing fold BACK around the torso for a dash.
        // During feather volleys the wings extend and hold still.
        float lift = 0.24F + spread + flap * (0.12F + (phase >= 3 ? 0.08F : 0))
                + casting * 0.28F + laserCharge * 0.18F + laserFire * 0.22F
                + transforming * 0.4F - dashReady * 0.38F - dash * 0.64F
                - stagger * 0.28F;
        leftWing.zRot = lift;
        rightWing.zRot = -lift;
        leftWing.xRot = 0.09F + dashReady * 0.31F + dash * 0.88F + well * 0.12F;
        rightWing.xRot = leftWing.xRot;
        leftWing.yRot = 0.10F + dashReady * 0.70F + dash * 1.25F;
        rightWing.yRot = -leftWing.yRot;
        leftOuterWing.zRot = 0.14F + spread * 0.6F
                + MathHelper.sin(ageInTicks * 0.18F + 0.9F) * 0.12F
                        * (1.0F - dashReady - dash)
                + casting * 0.22F + laserCharge * 0.16F
                - dashReady * 0.28F - dash * 0.40F;
        rightOuterWing.zRot = -leftOuterWing.zRot;
        leftOuterWing.yRot = 0.12F + dashReady * 0.43F + dash * 0.80F;
        rightOuterWing.yRot = -leftOuterWing.yRot;
        leftWingScallop.xRot = MathHelper.sin(ageInTicks * 0.15F) * 0.07F;
        rightWingScallop.xRot = leftWingScallop.xRot;
        leftWingBlade.visible = phase >= 3;
        rightWingBlade.visible = phase >= 3;
        for (int i = 0; i < castFeathers.length; i++) {
            castFeathers[i].visible = state == 1 && time >= 4;
            castFeathers[i].y = -8.5F + MathHelper.sin(ageInTicks * 0.21F + i) * 0.35F;
            castFeathers[i].zRot = (i - 1) * 0.36F
                    + MathHelper.sin(ageInTicks * 0.12F + i) * 0.08F;
        }

        // Hands frame the chest while casting, then tuck against the torso during a rush.
        leftUpperArm.xRot = 0.05F - casting * 0.60F - laserCharge * 0.37F
                + dashReady * 0.27F - dash * 1.05F
                + nova * 0.25F + well * 0.15F;
        rightUpperArm.xRot = leftUpperArm.xRot;
        leftUpperArm.zRot = 0.12F + casting * 0.35F + laserCharge * 0.22F
                - dashReady * 0.13F - dash * 0.18F
                - transforming * 0.25F;
        rightUpperArm.zRot = -leftUpperArm.zRot;
        leftForearm.xRot = -0.08F - casting * 0.3F - laserFire * 0.25F
                - dash * 0.38F;
        rightForearm.xRot = leftForearm.xRot;
        leftClaw.xRot = MathHelper.sin(ageInTicks * 0.12F) * 0.08F + castingCharge * 0.4F;
        rightClaw.xRot = leftClaw.xRot;
        leftMantle.xRot = -0.15F + MathHelper.sin(ageInTicks * 0.08F) * 0.07F + dash * 0.8F;
        rightMantle.xRot = -0.15F + MathHelper.cos(ageInTicks * 0.08F) * 0.07F + dash * 0.8F;
        leftLeg.xRot = MathHelper.sin(ageInTicks * 0.11F) * 0.13F + dash * 0.52F;
        rightLeg.xRot = -leftLeg.xRot;
    }

    @Override
    public void renderToBuffer(MatrixStack stack, IVertexBuilder buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        torso.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
