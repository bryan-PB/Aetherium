package com.aetherium.aetherium.client.model;

import com.aetherium.aetherium.content.entity.RiftStalkerEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/** Jagged rift hunter with plated shoulders, hooked claws and a segmented crown. */
public class RiftStalkerModel extends EntityModel<RiftStalkerEntity> {
    private final ModelRenderer head, jaw, body, core, leftHorn, rightHorn;
    private final ModelRenderer leftShoulder, rightShoulder, leftArm, rightArm;
    private final ModelRenderer leftClaw, rightClaw, leftLeg, rightLeg;
    private final ModelRenderer spineA, spineB, spineC, leftHip, rightHip;

    public RiftStalkerModel() {
        this.texWidth = 64;
        this.texHeight = 64;

        head = new ModelRenderer(this, 0, 0);
        head.setPos(0.0F, 5.0F, 0.0F);
        head.addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
        jaw = new ModelRenderer(this, 32, 0);
        jaw.setPos(0.0F, 2.0F, -2.0F);
        jaw.addBox(-2.7F, -0.2F, -3.0F, 5.4F, 2.0F, 3.0F);
        head.addChild(jaw);

        leftHorn = new ModelRenderer(this, 0, 18);
        leftHorn.setPos(-2.8F, -3.2F, 0.0F);
        leftHorn.addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F);
        head.addChild(leftHorn);
        rightHorn = new ModelRenderer(this, 8, 18);
        rightHorn.setPos(2.8F, -3.2F, 0.0F);
        rightHorn.addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F);
        head.addChild(rightHorn);

        body = new ModelRenderer(this, 0, 32);
        body.setPos(0.0F, 12.0F, 0.0F);
        body.addBox(-4.0F, -6.0F, -2.5F, 8.0F, 13.0F, 5.0F);
        core = new ModelRenderer(this, 40, 0);
        core.setPos(0.0F, -2.0F, -2.7F);
        core.addBox(-2.0F, -2.0F, -0.8F, 4.0F, 5.0F, 1.0F);
        body.addChild(core);

        leftShoulder = new ModelRenderer(this, 24, 18);
        leftShoulder.setPos(4.7F, -4.5F, 0.0F);
        leftShoulder.addBox(-0.6F, -1.8F, -2.2F, 4.0F, 4.0F, 4.4F);
        rightShoulder = new ModelRenderer(this, 24, 18);
        rightShoulder.setPos(-4.7F, -4.5F, 0.0F);
        rightShoulder.addBox(-3.4F, -1.8F, -2.2F, 4.0F, 4.0F, 4.4F);

        leftArm = new ModelRenderer(this, 36, 20);
        leftArm.setPos(5.0F, 7.0F, 0.0F);
        leftArm.addBox(-1.5F, -0.5F, -1.6F, 3.0F, 10.0F, 3.2F);
        rightArm = new ModelRenderer(this, 36, 20);
        rightArm.setPos(-5.0F, 7.0F, 0.0F);
        rightArm.addBox(-1.5F, -0.5F, -1.6F, 3.0F, 10.0F, 3.2F);

        leftClaw = new ModelRenderer(this, 48, 20);
        leftClaw.setPos(0.0F, 9.0F, -0.2F);
        leftClaw.addBox(-1.2F, 0.0F, -1.5F, 2.4F, 3.0F, 2.8F);
        leftArm.addChild(leftClaw);
        rightClaw = new ModelRenderer(this, 48, 20);
        rightClaw.setPos(0.0F, 9.0F, -0.2F);
        rightClaw.addBox(-1.2F, 0.0F, -1.5F, 2.4F, 3.0F, 2.8F);
        rightArm.addChild(rightClaw);

        leftLeg = new ModelRenderer(this, 0, 48);
        leftLeg.setPos(2.0F, 18.0F, 0.0F);
        leftLeg.addBox(-1.5F, 0.0F, -1.6F, 3.0F, 8.0F, 3.2F);
        rightLeg = new ModelRenderer(this, 12, 48);
        rightLeg.setPos(-2.0F, 18.0F, 0.0F);
        rightLeg.addBox(-1.5F, 0.0F, -1.6F, 3.0F, 8.0F, 3.2F);

        spineA = new ModelRenderer(this, 24, 42);
        spineA.setPos(0.0F, 7.0F, 2.2F);
        spineA.addBox(-1.5F, -4.0F, 0.0F, 3.0F, 5.0F, 2.0F);
        spineB = new ModelRenderer(this, 34, 42);
        spineB.setPos(0.0F, 12.0F, 2.2F);
        spineB.addBox(-1.2F, -2.5F, 0.0F, 2.4F, 4.0F, 2.0F);
        spineC = new ModelRenderer(this, 44, 42);
        spineC.setPos(0.0F, 17.0F, 2.0F);
        spineC.addBox(-1.0F, -1.8F, 0.0F, 2.0F, 3.0F, 1.8F);

        leftHip = new ModelRenderer(this, 24, 52);
        leftHip.setPos(2.8F, 17.0F, 0.0F);
        leftHip.addBox(-0.5F, -1.0F, -2.0F, 2.2F, 4.0F, 4.0F);
        rightHip = new ModelRenderer(this, 24, 52);
        rightHip.setPos(-2.8F, 17.0F, 0.0F);
        rightHip.addBox(-1.7F, -1.0F, -2.0F, 2.2F, 4.0F, 4.0F);
    }

    @Override
    public void setupAnim(RiftStalkerEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float walk = Math.min(1.0F, limbSwingAmount);
        float gait = limbSwing * 0.6662F;
        float idle = MathHelper.sin(ageInTicks * 0.09F);
        float attack = MathHelper.sin(entity.getAttackAnim(0.0F) * (float)Math.PI);
        float pounce = entity.getPounceAnimationTicks() / 12.0F;

        head.yRot = netHeadYaw * ((float)Math.PI / 180.0F);
        head.xRot = headPitch * ((float)Math.PI / 180.0F) - pounce * 0.32F;
        jaw.xRot = 0.04F + attack * 0.32F;
        body.xRot = -pounce * 0.58F + idle * 0.025F;
        body.y = 12.0F + Math.abs(idle) * 0.16F;
        leftLeg.xRot = MathHelper.cos(gait) * 1.1F * walk - pounce * 0.5F;
        rightLeg.xRot = MathHelper.cos(gait + (float)Math.PI) * 1.1F * walk - pounce * 0.5F;
        leftArm.xRot = MathHelper.cos(gait + (float)Math.PI) * 0.65F * walk - attack * 1.15F - pounce * 0.8F;
        rightArm.xRot = MathHelper.cos(gait) * 0.65F * walk - attack * 1.15F - pounce * 0.8F;
        leftShoulder.xRot = leftArm.xRot * 0.35F;
        rightShoulder.xRot = rightArm.xRot * 0.35F;
        leftHorn.zRot = 0.08F + idle * 0.025F;
        rightHorn.zRot = -leftHorn.zRot;
        spineA.xRot = -0.12F + idle * 0.04F;
        spineB.xRot = 0.08F - idle * 0.035F;
        spineC.xRot = -0.10F + idle * 0.03F;
        leftHip.zRot = 0.04F;
        rightHip.zRot = -0.04F;
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
        spineA.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        spineB.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        spineC.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftHip.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightHip.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
