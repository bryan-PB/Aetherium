package com.aetherium.aetherium.client.model;

import com.aetherium.aetherium.content.entity.LumiveloEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/** Compact quadruped with oversized crystal ears, deliberately simple for integrated GPUs. */
public class LumiveloModel extends EntityModel<LumiveloEntity> {
    private final ModelRenderer body;
    private final ModelRenderer head;
    private final ModelRenderer leftEar;
    private final ModelRenderer rightEar;
    private final ModelRenderer frontLeft;
    private final ModelRenderer frontRight;
    private final ModelRenderer backLeft;
    private final ModelRenderer backRight;
    private final ModelRenderer tail;

    public LumiveloModel() {
        this.texWidth = 64;
        this.texHeight = 64;
        this.body = new ModelRenderer(this, 0, 0);
        this.body.setPos(0.0F, 13.0F, 0.0F);
        this.body.addBox(-4.0F, -3.5F, -6.0F, 8.0F, 7.0F, 12.0F);

        this.head = new ModelRenderer(this, 0, 22);
        this.head.setPos(0.0F, 11.0F, -6.0F);
        this.head.addBox(-3.0F, -3.0F, -4.0F, 6.0F, 6.0F, 6.0F);
        this.leftEar = new ModelRenderer(this, 24, 22);
        this.leftEar.setPos(2.0F, -2.0F, -1.0F);
        this.leftEar.addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F);
        this.head.addChild(this.leftEar);
        this.rightEar = new ModelRenderer(this, 32, 22);
        this.rightEar.setPos(-2.0F, -2.0F, -1.0F);
        this.rightEar.addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F);
        this.head.addChild(this.rightEar);

        this.frontLeft = leg(0, 34, 2.5F, 15.0F, -4.0F);
        this.frontRight = leg(10, 34, -2.5F, 15.0F, -4.0F);
        this.backLeft = leg(20, 34, 2.5F, 15.0F, 4.0F);
        this.backRight = leg(30, 34, -2.5F, 15.0F, 4.0F);
        this.tail = new ModelRenderer(this, 42, 0);
        this.tail.setPos(0.0F, 12.0F, 5.0F);
        this.tail.addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 5.0F);
    }

    private ModelRenderer leg(int texU, int texV, float x, float y, float z) {
        ModelRenderer leg = new ModelRenderer(this, texU, texV);
        leg.setPos(x, y, z);
        leg.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F);
        return leg;
    }

    @Override
    public void setupAnim(LumiveloEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);
        this.frontLeft.xRot = MathHelper.cos(limbSwing * 0.6662F) * 1.15F * limbSwingAmount;
        this.frontRight.xRot = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.15F * limbSwingAmount;
        this.backLeft.xRot = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.15F * limbSwingAmount;
        this.backRight.xRot = MathHelper.cos(limbSwing * 0.6662F) * 1.15F * limbSwingAmount;
        this.tail.xRot = 0.12F + MathHelper.cos(ageInTicks * 0.08F) * 0.08F;
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.matrix.MatrixStack stack,
                               com.mojang.blaze3d.vertex.IVertexBuilder buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.body.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.head.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.frontLeft.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.frontRight.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.backLeft.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.backRight.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.tail.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
