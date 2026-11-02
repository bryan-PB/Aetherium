package com.aetherium.aetherium.client.model;

import com.aetherium.aetherium.content.boss.RiftGuardianEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/** Armoured rift juggernaut with a crowned helm, layered plates and an exposed reactor. */
public class RiftGuardianModel extends EntityModel<RiftGuardianEntity> {
    private final ModelRenderer head, body, chestPlate, core, jaw, faceMask, leftEye, rightEye, crown;
    private final ModelRenderer leftArm, rightArm, leftShoulder, rightShoulder;
    private final ModelRenderer leftGauntlet, rightGauntlet, leftLeg, rightLeg;
    private final ModelRenderer leftGreave, rightGreave, leftHorn, rightHorn;
    private final ModelRenderer leftShoulderSpike, rightShoulderSpike, leftBlade, rightBlade;
    private final ModelRenderer leftTasset, rightTasset, backSpikes, reactorFrame;

    public RiftGuardianModel() {
        this.texWidth = 64;
        this.texHeight = 64;

        head = new ModelRenderer(this, 0, 0);
        head.setPos(0.0F, 4.0F, 0.0F);
        head.addBox(-6.0F, -7.0F, -5.5F, 12.0F, 12.0F, 11.0F);
        jaw = new ModelRenderer(this, 32, 0);
        jaw.setPos(0.0F, 2.0F, -3.8F);
        jaw.addBox(-3.8F, -0.2F, -2.0F, 7.6F, 2.7F, 2.5F);
        head.addChild(jaw);
        faceMask = new ModelRenderer(this, 0, 18);
        faceMask.setPos(0.0F, -1.1F, -5.55F);
        faceMask.addBox(-4.7F, -2.8F, -0.55F, 9.4F, 5.6F, 1.0F);
        head.addChild(faceMask);
        leftEye = new ModelRenderer(this, 40, 0);
        leftEye.setPos(-2.5F, -1.2F, -6.18F);
        leftEye.addBox(-1.15F, -0.45F, -0.25F, 2.3F, 0.9F, 0.5F);
        head.addChild(leftEye);
        rightEye = new ModelRenderer(this, 40, 3);
        rightEye.setPos(2.5F, -1.2F, -6.18F);
        rightEye.addBox(-1.15F, -0.45F, -0.25F, 2.3F, 0.9F, 0.5F);
        head.addChild(rightEye);

        leftHorn = new ModelRenderer(this, 0, 26);
        leftHorn.setPos(-4.2F, -4.5F, 0.0F);
        leftHorn.addBox(-1.6F, -7.0F, -1.5F, 3.2F, 7.0F, 3.0F);
        head.addChild(leftHorn);
        rightHorn = new ModelRenderer(this, 12, 26);
        rightHorn.setPos(4.2F, -4.5F, 0.0F);
        rightHorn.addBox(-1.6F, -7.0F, -1.5F, 3.2F, 7.0F, 3.0F);
        head.addChild(rightHorn);
        crown = new ModelRenderer(this, 24, 18);
        crown.setPos(0.0F, -5.0F, 0.0F);
        crown.addBox(-1.7F, -6.5F, -1.5F, 3.4F, 7.0F, 3.0F);
        head.addChild(crown);

        body = new ModelRenderer(this, 0, 34);
        body.setPos(0.0F, 16.0F, 0.0F);
        body.addBox(-8.0F, -7.0F, -5.0F, 16.0F, 18.0F, 10.0F);
        chestPlate = new ModelRenderer(this, 32, 28);
        chestPlate.setPos(0.0F, -3.0F, -5.1F);
        chestPlate.addBox(-6.0F, -2.0F, -0.7F, 12.0F, 10.0F, 1.2F);
        body.addChild(chestPlate);
        core = new ModelRenderer(this, 48, 28);
        core.setPos(0.0F, 1.2F, -6.0F);
        core.addBox(-2.2F, -2.2F, -0.55F, 4.4F, 4.4F, 1.2F);
        body.addChild(core);
        reactorFrame = new ModelRenderer(this, 40, 40);
        reactorFrame.setPos(0.0F, 1.2F, -5.8F);
        reactorFrame.addBox(-3.2F, -3.2F, -0.25F, 6.4F, 6.4F, 0.7F);
        body.addChild(reactorFrame);

        leftTasset = new ModelRenderer(this, 0, 52);
        leftTasset.setPos(5.4F, 7.0F, -0.1F);
        leftTasset.addBox(-0.7F, -1.0F, -3.7F, 4.0F, 8.0F, 7.4F);
        body.addChild(leftTasset);
        rightTasset = new ModelRenderer(this, 16, 52);
        rightTasset.setPos(-5.4F, 7.0F, -0.1F);
        rightTasset.addBox(-3.3F, -1.0F, -3.7F, 4.0F, 8.0F, 7.4F);
        body.addChild(rightTasset);

        leftShoulder = new ModelRenderer(this, 0, 48);
        leftShoulder.setPos(8.0F, 10.0F, 0.0F);
        leftShoulder.addBox(-1.0F, -3.6F, -3.6F, 6.5F, 7.2F, 7.2F);
        rightShoulder = new ModelRenderer(this, 24, 48);
        rightShoulder.setPos(-8.0F, 10.0F, 0.0F);
        rightShoulder.addBox(-5.5F, -3.6F, -3.6F, 6.5F, 7.2F, 7.2F);
        leftShoulderSpike = new ModelRenderer(this, 40, 48);
        leftShoulderSpike.setPos(2.6F, -2.0F, 0.0F);
        leftShoulderSpike.addBox(-1.0F, -5.8F, -1.0F, 2.0F, 6.0F, 2.0F);
        leftShoulder.addChild(leftShoulderSpike);
        rightShoulderSpike = new ModelRenderer(this, 48, 48);
        rightShoulderSpike.setPos(-2.6F, -2.0F, 0.0F);
        rightShoulderSpike.addBox(-1.0F, -5.8F, -1.0F, 2.0F, 6.0F, 2.0F);
        rightShoulder.addChild(rightShoulderSpike);

        leftArm = new ModelRenderer(this, 0, 60);
        leftArm.setPos(9.0F, 11.0F, 0.0F);
        leftArm.addBox(-2.0F, -1.0F, -2.8F, 4.5F, 15.0F, 5.6F);
        rightArm = new ModelRenderer(this, 20, 60);
        rightArm.setPos(-9.0F, 11.0F, 0.0F);
        rightArm.addBox(-2.5F, -1.0F, -2.8F, 4.5F, 15.0F, 5.6F);
        leftGauntlet = new ModelRenderer(this, 38, 48);
        leftGauntlet.setPos(0.0F, 10.0F, -0.2F);
        leftGauntlet.addBox(-2.4F, 0.0F, -3.0F, 4.8F, 4.5F, 6.0F);
        leftArm.addChild(leftGauntlet);
        rightGauntlet = new ModelRenderer(this, 38, 48);
        rightGauntlet.setPos(0.0F, 10.0F, -0.2F);
        rightGauntlet.addBox(-2.4F, 0.0F, -3.0F, 4.8F, 4.5F, 6.0F);
        rightArm.addChild(rightGauntlet);
        leftBlade = new ModelRenderer(this, 52, 40);
        leftBlade.setPos(0.0F, 11.0F, 0.0F);
        leftBlade.addBox(-0.7F, 0.0F, -3.8F, 1.4F, 7.0F, 1.0F);
        leftArm.addChild(leftBlade);
        rightBlade = new ModelRenderer(this, 56, 40);
        rightBlade.setPos(0.0F, 11.0F, 0.0F);
        rightBlade.addBox(-0.7F, 0.0F, -3.8F, 1.4F, 7.0F, 1.0F);
        rightArm.addChild(rightBlade);

        leftLeg = new ModelRenderer(this, 0, 0);
        leftLeg.setPos(4.2F, 27.0F, 0.0F);
        leftLeg.addBox(-2.8F, 0.0F, -2.8F, 5.6F, 13.0F, 5.6F);
        rightLeg = new ModelRenderer(this, 24, 0);
        rightLeg.setPos(-4.2F, 27.0F, 0.0F);
        rightLeg.addBox(-2.8F, 0.0F, -2.8F, 5.6F, 13.0F, 5.6F);
        leftGreave = new ModelRenderer(this, 0, 18);
        leftGreave.setPos(0.0F, 5.0F, -2.9F);
        leftGreave.addBox(-2.4F, -1.0F, -0.6F, 4.8F, 7.0F, 1.2F);
        leftLeg.addChild(leftGreave);
        rightGreave = new ModelRenderer(this, 16, 18);
        rightGreave.setPos(0.0F, 5.0F, -2.9F);
        rightGreave.addBox(-2.4F, -1.0F, -0.6F, 4.8F, 7.0F, 1.2F);
        rightLeg.addChild(rightGreave);

        backSpikes = new ModelRenderer(this, 40, 0);
        backSpikes.setPos(0.0F, -3.5F, 4.7F);
        backSpikes.addBox(-7.0F, -3.0F, 0.0F, 14.0F, 8.0F, 2.4F);
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
        leftHorn.zRot = 0.05F + idle * 0.025F - rage * 0.16F;
        rightHorn.zRot = -leftHorn.zRot;
        jaw.xRot = 0.02F + attack * 0.16F + rage * 0.12F;

        body.y = 16.0F + idle * 0.12F;
        body.xRot = slam * 0.30F + idle * 0.018F;
        core.y = 1.2F + MathHelper.sin(ageInTicks * (rage > 0.0F ? 0.28F : 0.18F)) * (0.35F + rage * 0.25F);
        core.zRot = MathHelper.sin(ageInTicks * 0.12F) * 0.08F;
        reactorFrame.zRot = -core.zRot;
        leftTasset.xRot = MathHelper.sin(gait) * 0.12F * walk;
        rightTasset.xRot = -leftTasset.xRot;

        leftLeg.xRot = MathHelper.cos(gait) * 0.62F * walk - slam * 0.18F;
        rightLeg.xRot = MathHelper.cos(gait + (float)Math.PI) * 0.62F * walk - slam * 0.18F;
        leftArm.xRot = -MathHelper.cos(gait) * 0.38F * walk - attack * 0.45F - slam * 1.05F - rage * 0.22F;
        rightArm.xRot = MathHelper.cos(gait) * 0.38F * walk - attack * 0.45F - slam * 1.05F - rage * 0.22F;
        leftShoulder.xRot = leftArm.xRot * 0.3F - rage * 0.18F;
        rightShoulder.xRot = rightArm.xRot * 0.3F - rage * 0.18F;
        leftShoulder.zRot = -rage * 0.18F;
        rightShoulder.zRot = rage * 0.18F;
        leftShoulderSpike.xRot = -0.18F - rage * 0.48F;
        rightShoulderSpike.xRot = -0.18F - rage * 0.48F;
        leftBlade.xRot = -slam * 0.7F - rage * 0.25F;
        rightBlade.xRot = -slam * 0.7F - rage * 0.25F;
        backSpikes.xRot = 0.12F + rage * 0.30F;
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.matrix.MatrixStack stack,
                               com.mojang.blaze3d.vertex.IVertexBuilder buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        head.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftShoulder.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightShoulder.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftArm.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightArm.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftLeg.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightLeg.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
