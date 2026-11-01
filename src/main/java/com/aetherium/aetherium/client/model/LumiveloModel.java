package com.aetherium.aetherium.client.model;

import com.aetherium.aetherium.content.entity.LumiveloEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/** A small crystal-backed grazer with expressive ears, layered mane and a swaying tail. */
public class LumiveloModel extends EntityModel<LumiveloEntity> {
    private final ModelRenderer body, head, muzzle, leftEar, rightEar, tail;
    private final ModelRenderer frontLeft, frontRight, backLeft, backRight;
    private final ModelRenderer crystalA, crystalB, crystalC, cheekLeft, cheekRight;

    public LumiveloModel() {
        this.texWidth = 64;
        this.texHeight = 64;

        body = new ModelRenderer(this, 0, 0);
        body.setPos(0.0F, 13.0F, 0.0F);
        body.addBox(-4.5F, -3.5F, -6.5F, 9.0F, 7.0F, 13.0F);
        crystalA = new ModelRenderer(this, 0, 22);
        crystalA.setPos(0.0F, -2.7F, 0.0F);
        crystalA.addBox(-1.6F, -5.0F, -1.2F, 3.2F, 5.5F, 2.4F);
        body.addChild(crystalA);
        crystalB = new ModelRenderer(this, 12, 22);
        crystalB.setPos(-2.2F, -2.0F, 1.0F);
        crystalB.addBox(-1.0F, -3.6F, -0.8F, 2.0F, 4.0F, 1.6F);
        body.addChild(crystalB);
        crystalC = new ModelRenderer(this, 20, 22);
        crystalC.setPos(2.2F, -2.0F, 1.0F);
        crystalC.addBox(-1.0F, -3.6F, -0.8F, 2.0F, 4.0F, 1.6F);
        body.addChild(crystalC);

        head = new ModelRenderer(this, 0, 34);
        head.setPos(0.0F, 10.8F, -6.0F);
        head.addBox(-3.2F, -3.0F, -4.2F, 6.4F, 6.0F, 6.8F);
        muzzle = new ModelRenderer(this, 28, 34);
        muzzle.setPos(0.0F, 1.0F, -3.0F);
        muzzle.addBox(-2.0F, -1.0F, -2.2F, 4.0F, 2.4F, 2.5F);
        head.addChild(muzzle);

        leftEar = new ModelRenderer(this, 42, 0);
        leftEar.setPos(2.2F, -2.1F, -0.8F);
        leftEar.addBox(-0.8F, -5.8F, -0.8F, 1.6F, 6.0F, 1.6F);
        head.addChild(leftEar);
        rightEar = new ModelRenderer(this, 48, 0);
        rightEar.setPos(-2.2F, -2.1F, -0.8F);
        rightEar.addBox(-0.8F, -5.8F, -0.8F, 1.6F, 6.0F, 1.6F);
        head.addChild(rightEar);
        cheekLeft = new ModelRenderer(this, 36, 34);
        cheekLeft.setPos(2.7F, 0.0F, -1.8F);
        cheekLeft.addBox(-0.5F, -1.6F, -1.2F, 1.0F, 3.0F, 2.4F);
        head.addChild(cheekLeft);
        cheekRight = new ModelRenderer(this, 36, 34);
        cheekRight.setPos(-2.7F, 0.0F, -1.8F);
        cheekRight.addBox(-0.5F, -1.6F, -1.2F, 1.0F, 3.0F, 2.4F);
        head.addChild(cheekRight);

        frontLeft = leg(0, 46, 2.7F, 15.0F, -4.0F);
        frontRight = leg(10, 46, -2.7F, 15.0F, -4.0F);
        backLeft = leg(20, 46, 2.7F, 15.0F, 4.0F);
        backRight = leg(30, 46, -2.7F, 15.0F, 4.0F);

        tail = new ModelRenderer(this, 42, 18);
        tail.setPos(0.0F, 12.0F, 5.8F);
        tail.addBox(-1.2F, -1.0F, 0.0F, 2.4F, 2.4F, 6.5F);
    }

    private ModelRenderer leg(int u, int v, float x, float y, float z) {
        ModelRenderer leg = new ModelRenderer(this, u, v);
        leg.setPos(x, y, z);
        leg.addBox(-1.1F, 0.0F, -1.1F, 2.2F, 5.5F, 2.2F);
        return leg;
    }

    @Override
    public void setupAnim(LumiveloEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float gait = limbSwing * 0.72F;
        float walk = Math.min(1.0F, limbSwingAmount);
        float idle = MathHelper.sin(ageInTicks * 0.11F);
        head.yRot = netHeadYaw * ((float)Math.PI / 180.0F);
        head.xRot = headPitch * ((float)Math.PI / 180.0F) + idle * 0.025F;
        body.y = 13.0F + idle * 0.22F;
        body.xRot = MathHelper.cos(gait * 2.0F) * 0.04F * walk;
        frontLeft.xRot = MathHelper.cos(gait) * 1.12F * walk;
        frontRight.xRot = MathHelper.cos(gait + (float)Math.PI) * 1.12F * walk;
        backLeft.xRot = MathHelper.cos(gait + (float)Math.PI) * 1.12F * walk;
        backRight.xRot = MathHelper.cos(gait) * 1.12F * walk;
        tail.yRot = MathHelper.sin(ageInTicks * 0.12F) * 0.34F;
        tail.xRot = 0.12F + MathHelper.cos(ageInTicks * 0.08F) * 0.08F;
        leftEar.zRot = 0.08F + MathHelper.sin(ageInTicks * 0.18F) * 0.13F;
        rightEar.zRot = -0.08F - MathHelper.sin(ageInTicks * 0.18F + 0.8F) * 0.13F;
        crystalA.zRot = MathHelper.sin(ageInTicks * 0.10F) * 0.035F;
        crystalB.zRot = 0.10F + MathHelper.cos(ageInTicks * 0.12F) * 0.06F;
        crystalC.zRot = -crystalB.zRot;
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.matrix.MatrixStack stack,
                               com.mojang.blaze3d.vertex.IVertexBuilder buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        body.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        frontLeft.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        frontRight.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        backLeft.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        backRight.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        tail.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
