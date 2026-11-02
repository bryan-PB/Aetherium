package com.aetherium.aetherium.client.model;

import com.aetherium.aetherium.content.boss.RiftGuardianEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/** Armoured rift juggernaut with a clearer humanoid frame and brutal, segmented anatomy. */
public class RiftGuardianModel extends EntityModel<RiftGuardianEntity> {
    private final ModelRenderer head, body, neck, chestPlate, core, jaw, faceMask, leftEye, rightEye, crown;
    private final ModelRenderer leftArm, rightArm, leftForearm, rightForearm, leftHand, rightHand;
    private final ModelRenderer leftShoulder, rightShoulder, leftGauntlet, rightGauntlet;
    private final ModelRenderer leftLeg, rightLeg, leftGreave, rightGreave, leftKnee, rightKnee;
    private final ModelRenderer leftHorn, rightHorn, leftShoulderSpike, rightShoulderSpike;
    private final ModelRenderer leftTasset, rightTasset, backSpikes, reactorFrame;
    private final ModelRenderer leftPectoral, rightPectoral;
    private final ModelRenderer[] abdomen = new ModelRenderer[3];
    private final ModelRenderer[] leftClaws = new ModelRenderer[3];
    private final ModelRenderer[] rightClaws = new ModelRenderer[3];

    public RiftGuardianModel() {
        this.texWidth = 64;
        this.texHeight = 64;

        head = new ModelRenderer(this, 0, 0);
        head.setPos(0.0F, -12.0F, 0.0F);
        head.addBox(-5.5F, -6.0F, -5.0F, 11.0F, 12.0F, 10.0F);

        jaw = new ModelRenderer(this, 32, 0);
        jaw.setPos(0.0F, 3.0F, -4.6F);
        jaw.addBox(-3.8F, -0.2F, -1.0F, 7.6F, 2.5F, 1.5F);
        head.addChild(jaw);

        faceMask = new ModelRenderer(this, 0, 24);
        faceMask.setPos(0.0F, -1.0F, -5.08F);
        faceMask.addBox(-4.6F, -2.8F, -0.45F, 9.2F, 5.6F, 0.9F);
        head.addChild(faceMask);

        leftEye = new ModelRenderer(this, 40, 0);
        leftEye.setPos(-2.35F, -1.3F, -5.65F);
        leftEye.addBox(-1.0F, -0.45F, -0.2F, 2.0F, 0.9F, 0.4F);
        head.addChild(leftEye);
        rightEye = new ModelRenderer(this, 40, 4);
        rightEye.setPos(2.35F, -1.3F, -5.65F);
        rightEye.addBox(-1.0F, -0.45F, -0.2F, 2.0F, 0.9F, 0.4F);
        head.addChild(rightEye);

        leftHorn = new ModelRenderer(this, 40, 18);
        leftHorn.setPos(-3.8F, -4.0F, 0.0F);
        leftHorn.addBox(-1.0F, -7.0F, -1.1F, 2.0F, 7.0F, 2.2F);
        leftHorn.zRot = -0.22F;
        head.addChild(leftHorn);
        rightHorn = new ModelRenderer(this, 48, 18);
        rightHorn.setPos(3.8F, -4.0F, 0.0F);
        rightHorn.addBox(-1.0F, -7.0F, -1.1F, 2.0F, 7.0F, 2.2F);
        rightHorn.zRot = 0.22F;
        head.addChild(rightHorn);

        crown = new ModelRenderer(this, 32, 24);
        crown.setPos(0.0F, -5.2F, 0.0F);
        crown.addBox(-1.8F, -5.5F, -1.8F, 3.6F, 6.0F, 3.6F);
        head.addChild(crown);

        neck = new ModelRenderer(this, 0, 0);
        neck.setPos(0.0F, -8.2F, 0.0F);
        neck.addBox(-3.0F, -0.8F, -3.0F, 6.0F, 5.0F, 6.0F);

        body = new ModelRenderer(this, 0, 36);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.addBox(-7.0F, -8.0F, -4.5F, 14.0F, 18.0F, 9.0F);

        chestPlate = new ModelRenderer(this, 40, 36);
        chestPlate.setPos(0.0F, -3.0F, -4.65F);
        chestPlate.addBox(-5.0F, -2.5F, -0.45F, 10.0F, 8.0F, 0.9F);
        body.addChild(chestPlate);

        leftPectoral = new ModelRenderer(this, 40, 20);
        leftPectoral.setPos(-2.6F, -4.0F, -5.0F);
        leftPectoral.addBox(-2.7F, -1.8F, -0.55F, 5.4F, 4.8F, 1.1F);
        body.addChild(leftPectoral);
        rightPectoral = new ModelRenderer(this, 40, 20);
        rightPectoral.setPos(2.6F, -4.0F, -5.0F);
        rightPectoral.addBox(-2.7F, -1.8F, -0.55F, 5.4F, 4.8F, 1.1F);
        body.addChild(rightPectoral);

        core = new ModelRenderer(this, 50, 20);
        core.setPos(0.0F, 0.6F, -5.0F);
        core.addBox(-2.0F, -2.0F, -0.5F, 4.0F, 4.0F, 1.0F);
        body.addChild(core);
        reactorFrame = new ModelRenderer(this, 36, 44);
        reactorFrame.setPos(0.0F, 0.6F, -4.9F);
        reactorFrame.addBox(-3.0F, -3.0F, -0.25F, 6.0F, 6.0F, 0.5F);
        body.addChild(reactorFrame);

        for (int i = 0; i < abdomen.length; i++) {
            abdomen[i] = new ModelRenderer(this, 40, 20);
            abdomen[i].setPos(0.0F, 2.8F + i * 2.15F, -4.72F);
            float halfWidth = 4.2F - i * 0.45F;
            abdomen[i].addBox(-halfWidth, -0.8F, -0.35F, halfWidth * 2.0F, 1.6F, 0.7F);
            body.addChild(abdomen[i]);
        }

        leftTasset = new ModelRenderer(this, 0, 0);
        leftTasset.setPos(4.8F, 7.0F, -0.1F);
        leftTasset.addBox(-0.5F, -1.0F, -3.2F, 3.6F, 7.0F, 6.4F);
        body.addChild(leftTasset);
        rightTasset = new ModelRenderer(this, 20, 0);
        rightTasset.setPos(-4.8F, 7.0F, -0.1F);
        rightTasset.addBox(-3.1F, -1.0F, -3.2F, 3.6F, 7.0F, 6.4F);
        body.addChild(rightTasset);

        leftShoulder = new ModelRenderer(this, 0, 24);
        leftShoulder.setPos(7.0F, -6.0F, 0.0F);
        leftShoulder.addBox(-0.8F, -3.2F, -3.2F, 6.0F, 6.4F, 6.4F);
        rightShoulder = new ModelRenderer(this, 24, 24);
        rightShoulder.setPos(-7.0F, -6.0F, 0.0F);
        rightShoulder.addBox(-5.2F, -3.2F, -3.2F, 6.0F, 6.4F, 6.4F);

        leftShoulderSpike = new ModelRenderer(this, 36, 0);
        leftShoulderSpike.setPos(2.8F, -1.8F, 0.0F);
        leftShoulderSpike.addBox(-1.0F, -5.5F, -1.0F, 2.0F, 5.8F, 2.0F);
        leftShoulder.addChild(leftShoulderSpike);
        rightShoulderSpike = new ModelRenderer(this, 44, 0);
        rightShoulderSpike.setPos(-2.8F, -1.8F, 0.0F);
        rightShoulderSpike.addBox(-1.0F, -5.5F, -1.0F, 2.0F, 5.8F, 2.0F);
        rightShoulder.addChild(rightShoulderSpike);

        leftArm = new ModelRenderer(this, 0, 0);
        leftArm.setPos(8.0F, -5.0F, 0.0F);
        leftArm.addBox(-2.0F, -1.0F, -2.4F, 4.0F, 8.0F, 4.8F);
        rightArm = new ModelRenderer(this, 20, 0);
        rightArm.setPos(-8.0F, -5.0F, 0.0F);
        rightArm.addBox(-2.0F, -1.0F, -2.4F, 4.0F, 8.0F, 4.8F);

        leftForearm = new ModelRenderer(this, 0, 16);
        leftForearm.setPos(0.0F, 6.0F, 0.0F);
        leftForearm.addBox(-2.1F, -0.5F, -2.8F, 4.2F, 7.0F, 5.6F);
        leftArm.addChild(leftForearm);
        rightForearm = new ModelRenderer(this, 20, 16);
        rightForearm.setPos(0.0F, 6.0F, 0.0F);
        rightForearm.addBox(-2.1F, -0.5F, -2.8F, 4.2F, 7.0F, 5.6F);
        rightArm.addChild(rightForearm);

        leftGauntlet = new ModelRenderer(this, 40, 0);
        leftGauntlet.setPos(0.0F, 5.4F, -0.1F);
        leftGauntlet.addBox(-2.25F, -0.4F, -2.9F, 4.5F, 3.5F, 5.8F);
        leftForearm.addChild(leftGauntlet);
        rightGauntlet = new ModelRenderer(this, 40, 12);
        rightGauntlet.setPos(0.0F, 5.4F, -0.1F);
        rightGauntlet.addBox(-2.25F, -0.4F, -2.9F, 4.5F, 3.5F, 5.8F);
        rightForearm.addChild(rightGauntlet);

        leftHand = new ModelRenderer(this, 44, 12);
        leftHand.setPos(0.0F, 2.6F, -0.2F);
        leftHand.addBox(-1.8F, 0.0F, -2.4F, 3.6F, 3.0F, 4.8F);
        leftForearm.addChild(leftHand);
        rightHand = new ModelRenderer(this, 44, 24);
        rightHand.setPos(0.0F, 2.6F, -0.2F);
        rightHand.addBox(-1.8F, 0.0F, -2.4F, 3.6F, 3.0F, 4.8F);
        rightForearm.addChild(rightHand);

        for (int i = 0; i < 3; i++) {
            leftClaws[i] = new ModelRenderer(this, 52, 12);
            leftClaws[i].setPos(-1.2F + i * 1.2F, 2.4F, -2.0F);
            leftClaws[i].addBox(-0.35F, 0.0F, -0.35F, 0.7F, 3.0F, 0.9F);
            leftHand.addChild(leftClaws[i]);

            rightClaws[i] = new ModelRenderer(this, 52, 16);
            rightClaws[i].setPos(-1.2F + i * 1.2F, 2.4F, -2.0F);
            rightClaws[i].addBox(-0.35F, 0.0F, -0.35F, 0.7F, 3.0F, 0.9F);
            rightHand.addChild(rightClaws[i]);
        }

        leftLeg = new ModelRenderer(this, 0, 0);
        leftLeg.setPos(3.8F, 10.0F, 0.0F);
        leftLeg.addBox(-2.5F, 0.0F, -2.6F, 5.0F, 7.0F, 5.2F);
        rightLeg = new ModelRenderer(this, 22, 0);
        rightLeg.setPos(-3.8F, 10.0F, 0.0F);
        rightLeg.addBox(-2.5F, 0.0F, -2.6F, 5.0F, 7.0F, 5.2F);

        leftGreave = new ModelRenderer(this, 0, 36);
        leftGreave.setPos(0.0F, 5.0F, -2.7F);
        leftGreave.addBox(-2.15F, -1.0F, -0.55F, 4.3F, 4.5F, 1.1F);
        leftLeg.addChild(leftGreave);
        rightGreave = new ModelRenderer(this, 16, 36);
        rightGreave.setPos(0.0F, 5.0F, -2.7F);
        rightGreave.addBox(-2.15F, -1.0F, -0.55F, 4.3F, 4.5F, 1.1F);
        rightLeg.addChild(rightGreave);

        leftKnee = new ModelRenderer(this, 40, 24);
        leftKnee.setPos(0.0F, 4.5F, -2.7F);
        leftKnee.addBox(-2.2F, -1.3F, -0.7F, 4.4F, 3.0F, 1.4F);
        leftLeg.addChild(leftKnee);
        rightKnee = new ModelRenderer(this, 40, 28);
        rightKnee.setPos(0.0F, 4.5F, -2.7F);
        rightKnee.addBox(-2.2F, -1.3F, -0.7F, 4.4F, 3.0F, 1.4F);
        rightLeg.addChild(rightKnee);

        backSpikes = new ModelRenderer(this, 30, 36);
        backSpikes.setPos(0.0F, -3.5F, 4.2F);
        backSpikes.addBox(-6.0F, -3.0F, 0.0F, 12.0F, 7.0F, 1.5F);
        body.addChild(backSpikes);
    }

    @Override
    public void setupAnim(RiftGuardianEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float walk = Math.min(1.0F, limbSwingAmount);
        float gait = limbSwing * 0.55F;
        float idle = MathHelper.sin(ageInTicks * 0.07F);
        float slam = entity.getSlamAnimationTicks() / 16.0F;
        float attack = MathHelper.sin(entity.getAttackAnim(0.0F) * (float)Math.PI);
        float rage = entity.getPhase() == 2 ? 1.0F : 0.0F;

        head.yRot = netHeadYaw * ((float)Math.PI / 180.0F);
        head.xRot = headPitch * ((float)Math.PI / 180.0F) - slam * 0.22F;
        crown.zRot = MathHelper.sin(ageInTicks * 0.09F) * 0.035F + rage * 0.12F;
        leftHorn.zRot = -0.22F + idle * 0.025F - rage * 0.16F;
        rightHorn.zRot = -leftHorn.zRot;
        jaw.xRot = 0.02F + attack * 0.16F + rage * 0.12F;

        body.y = idle * 0.12F;
        body.xRot = slam * 0.30F + idle * 0.018F;
        neck.yRot = head.yRot * 0.30F;
        core.y = 0.6F + MathHelper.sin(ageInTicks * (rage > 0.0F ? 0.28F : 0.18F)) * (0.35F + rage * 0.25F);
        core.zRot = MathHelper.sin(ageInTicks * 0.12F) * 0.08F;
        reactorFrame.zRot = -core.zRot;
        leftPectoral.xRot = -attack * 0.07F - rage * 0.025F;
        rightPectoral.xRot = leftPectoral.xRot;
        for (int i = 0; i < abdomen.length; i++) {
            abdomen[i].xRot = slam * 0.05F + MathHelper.sin(ageInTicks * 0.06F + i) * 0.015F;
        }
        leftTasset.xRot = MathHelper.sin(gait) * 0.12F * walk;
        rightTasset.xRot = -leftTasset.xRot;

        leftLeg.xRot = MathHelper.cos(gait) * 0.52F * walk - slam * 0.18F;
        rightLeg.xRot = MathHelper.cos(gait + (float)Math.PI) * 0.52F * walk - slam * 0.18F;
        leftArm.xRot = -MathHelper.cos(gait) * 0.30F * walk - attack * 0.38F - slam * 0.85F - rage * 0.20F;
        rightArm.xRot = MathHelper.cos(gait) * 0.30F * walk - attack * 0.38F - slam * 0.85F - rage * 0.20F;
        leftForearm.xRot = 0.12F + attack * 0.32F + slam * 0.35F;
        rightForearm.xRot = 0.12F + attack * 0.32F + slam * 0.35F;
        leftHand.xRot = -0.12F - attack * 0.2F;
        rightHand.xRot = -0.12F - attack * 0.2F;
        leftShoulder.xRot = leftArm.xRot * 0.3F - rage * 0.18F;
        rightShoulder.xRot = rightArm.xRot * 0.3F - rage * 0.18F;
        leftShoulder.zRot = -rage * 0.18F;
        rightShoulder.zRot = rage * 0.18F;
        leftShoulderSpike.xRot = -0.18F - rage * 0.48F;
        rightShoulderSpike.xRot = -0.18F - rage * 0.48F;
        leftKnee.xRot = -0.12F + walk * 0.15F;
        rightKnee.xRot = -0.12F + walk * 0.15F;
        backSpikes.xRot = 0.12F + rage * 0.30F;
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.matrix.MatrixStack stack,
                               com.mojang.blaze3d.vertex.IVertexBuilder buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        head.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        neck.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftShoulder.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightShoulder.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftArm.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightArm.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftLeg.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightLeg.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
