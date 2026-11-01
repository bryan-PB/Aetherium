package com.aetherium.aetherium.client.model;

import com.aetherium.aetherium.content.boss.RiftGuardianEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/** Heavy, plated colossus. Layered armor and a visible core give the Guardian a true boss silhouette. */
public class RiftGuardianModel extends EntityModel<RiftGuardianEntity> {
    private final ModelRenderer head, body, chestPlate, core, jaw;
    private final ModelRenderer leftArm, rightArm, leftShoulder, rightShoulder;
    private final ModelRenderer leftGauntlet, rightGauntlet, leftLeg, rightLeg;
    private final ModelRenderer leftGreave, rightGreave, leftHorn, rightHorn;
    private final ModelRenderer backSpikes;

    public RiftGuardianModel() {
        this.texWidth = 64;
        this.texHeight = 64;

        head = new ModelRenderer(this, 0, 0);
        head.setPos(0.0F, 4.0F, 0.0F);
        head.addBox(-6.0F, -7.0F, -5.5F, 12.0F, 12.0F, 11.0F);
        jaw = new ModelRenderer(this, 32, 0);
        jaw.setPos(0.0F, 2.0F, -3.8F);
        jaw.addBox(-3.5F, -0.3F, -2.0F, 7.0F, 2.5F, 2.5F);
        head.addChild(jaw);

        leftHorn = new ModelRenderer(this, 0, 18);
        leftHorn.setPos(-4.2F, -4.5F, 0.0F);
        leftHorn.addBox(-1.5F, -7.0F, -1.5F, 3.0F, 7.0F, 3.0F);
        head.addChild(leftHorn);
        rightHorn = new ModelRenderer(this, 12, 18);
        rightHorn.setPos(4.2F, -4.5F, 0.0F);
        rightHorn.addBox(-1.5F, -7.0F, -1.5F, 3.0F, 7.0F, 3.0F);
        head.addChild(rightHorn);

        body = new ModelRenderer(this, 0, 28);
        body.setPos(0.0F, 16.0F, 0.0F);
        body.addBox(-8.0F, -7.0F, -5.0F, 16.0F, 18.0F, 10.0F);
        chestPlate = new ModelRenderer(this, 32, 28);
        chestPlate.setPos(0.0F, -3.0F, -5.1F);
        chestPlate.addBox(-5.5F, -2.0F, -0.7F, 11.0F, 9.0F, 1.2F);
        body.addChild(chestPlate);
        core = new ModelRenderer(this, 48, 28);
        core.setPos(0.0F, 1.5F, -6.0F);
        core.addBox(-2.0F, -2.0F, -0.4F, 4.0F, 4.0F, 1.0F);
        body.addChild(core);

        leftShoulder = new ModelRenderer(this, 0, 48);
        leftShoulder.setPos(8.0F, 10.0F, 0.0F);
        leftShoulder.addBox(-1.0F, -3.0F, -3.0F, 5.5F, 6.0F, 6.0F);
        rightShoulder = new ModelRenderer(this, 24, 48);
        rightShoulder.setPos(-8.0F, 10.0F, 0.0F);
        rightShoulder.addBox(-4.5F, -3.0F, -3.0F, 5.5F, 6.0F, 6.0F);

        leftArm = new ModelRenderer(this, 0, 60);
        leftArm.setPos(9.0F, 11.0F, 0.0F);
        leftArm.addBox(-2.0F, -1.0F, -2.5F, 4.5F, 15.0F, 5.0F);
        rightArm = new ModelRenderer(this, 20, 60);
        rightArm.setPos(-9.0F, 11.0F, 0.0F);
        rightArm.addBox(-2.5F, -1.0F, -2.5F, 4.5F, 15.0F, 5.0F);
        leftGauntlet = new ModelRenderer(this, 38, 48);
        leftGauntlet.setPos(0.0F, 11.0F, -0.2F);
        leftGauntlet.addBox(-2.2F, 0.0F, -2.8F, 4.8F, 4.0F, 5.6F);
        leftArm.addChild(leftGauntlet);
        rightGauntlet = new ModelRenderer(this, 38, 48);
        rightGauntlet.setPos(0.0F, 11.0F, -0.2F);
        rightGauntlet.addBox(-2.6F, 0.0F, -2.8F, 4.8F, 4.0F, 5.6F);
        rightArm.addChild(rightGauntlet);

        leftLeg = new ModelRenderer(this, 0, 0);
        leftLeg.setPos(4.2F, 27.0F, 0.0F);
        leftLeg.addBox(-2.8F, 0.0F, -2.8F, 5.6F, 13.0F, 5.6F);
        rightLeg = new ModelRenderer(this, 24, 0);
        rightLeg.setPos(-4.2F, 27.0F, 0.0F);
        rightLeg.addBox(-2.8F, 0.0F, -2.8F, 5.6F, 13.0F, 5.6F);
        leftGreave = new ModelRenderer(this, 0, 18);
        leftGreave.setPos(0.0F, 5.0F, -2.9F);
        leftGreave.addBox(-2.4F, -1.0F, -0.5F, 4.8F, 7.0F, 1.0F);
        leftLeg.addChild(leftGreave);
        rightGreave = new ModelRenderer(this, 16, 18);
        rightGreave.setPos(0.0F, 5.0F, -2.9F);
        rightGreave.addBox(-2.4F, -1.0F, -0.5F, 4.8F, 7.0F, 1.0F);
        rightLeg.addChild(rightGreave);

        backSpikes = new ModelRenderer(this, 40, 0);
        backSpikes.setPos(0.0F, -3.5F, 4.7F);
        backSpikes.addBox(-6.0F, -3.0F, 0.0F, 12.0F, 7.0F, 2.0F);
        body.addChild(backSpikes);
    }

    @Override
    public void setupAnim(RiftGuardianEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float walk = Math.min(1.0F, limbSwingAmount);
        float gait = limbSwing * 0.55F;
        float idle = MathHelper.sin(ageInTicks * 0.07F);
        float slam = entity.getSlamAnimationTicks() / 14.0F;
        float attack = MathHelper.sin(entity.getAttackAnim(0.0F) * (float)Math.PI);
        head.yRot = netHeadYaw * ((float)Math.PI / 180.0F);
        head.xRot = headPitch * ((float)Math.PI / 180.0F) - slam * 0.18F;
        body.y = 16.0F + idle * 0.12F;
        body.xRot = slam * 0.24F + idle * 0.018F;
        leftLeg.xRot = MathHelper.cos(gait) * 0.62F * walk - slam * 0.18F;
        rightLeg.xRot = MathHelper.cos(gait + (float)Math.PI) * 0.62F * walk - slam * 0.18F;
        leftArm.xRot = -MathHelper.cos(gait) * 0.38F * walk - attack * 0.45F - slam * 1.05F;
        rightArm.xRot = MathHelper.cos(gait) * 0.38F * walk - attack * 0.45F - slam * 1.05F;
        leftShoulder.xRot = leftArm.xRot * 0.3F;
        rightShoulder.xRot = rightArm.xRot * 0.3F;
        leftHorn.zRot = 0.035F + idle * 0.02F;
        rightHorn.zRot = -leftHorn.zRot;
        core.y = 1.5F + MathHelper.sin(ageInTicks * 0.18F) * 0.35F;
        if (entity.getPhase() == 2) {
            leftArm.xRot -= 0.12F;
            rightArm.xRot -= 0.12F;
        }
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
