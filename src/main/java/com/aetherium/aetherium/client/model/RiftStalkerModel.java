package com.aetherium.aetherium.client.model;

import com.aetherium.aetherium.content.entity.RiftStalkerEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/** Low-poly humanoid silhouette with split crown horns and long forearms. */
public class RiftStalkerModel extends EntityModel<RiftStalkerEntity> {
    private final ModelRenderer head;
    private final ModelRenderer body;
    private final ModelRenderer leftHorn;
    private final ModelRenderer rightHorn;
    private final ModelRenderer leftArm;
    private final ModelRenderer rightArm;
    private final ModelRenderer leftLeg;
    private final ModelRenderer rightLeg;

    public RiftStalkerModel() {
        this.texWidth = 64;
        this.texHeight = 64;
        this.head = new ModelRenderer(this, 0, 0);
        this.head.setPos(0.0F, 5.0F, 0.0F);
        this.head.addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);

        this.leftHorn = new ModelRenderer(this, 0, 18);
        this.leftHorn.setPos(-2.8F, -3.2F, 0.0F);
        this.leftHorn.addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F);
        this.head.addChild(this.leftHorn);
        this.rightHorn = new ModelRenderer(this, 8, 18);
        this.rightHorn.setPos(2.8F, -3.2F, 0.0F);
        this.rightHorn.addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F);
        this.head.addChild(this.rightHorn);

        this.body = new ModelRenderer(this, 0, 32);
        this.body.setPos(0.0F, 12.0F, 0.0F);
        this.body.addBox(-4.0F, -6.0F, -2.0F, 8.0F, 12.0F, 4.0F);

        this.leftArm = new ModelRenderer(this, 24, 0);
        this.leftArm.setPos(5.0F, 7.0F, 0.0F);
        this.leftArm.addBox(-1.5F, 0.0F, -1.5F, 3.0F, 11.0F, 3.0F);
        this.rightArm = new ModelRenderer(this, 36, 0);
        this.rightArm.setPos(-5.0F, 7.0F, 0.0F);
        this.rightArm.addBox(-1.5F, 0.0F, -1.5F, 3.0F, 11.0F, 3.0F);

        this.leftLeg = new ModelRenderer(this, 24, 24);
        this.leftLeg.setPos(2.0F, 18.0F, 0.0F);
        this.leftLeg.addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F);
        this.rightLeg = new ModelRenderer(this, 36, 24);
        this.rightLeg.setPos(-2.0F, 18.0F, 0.0F);
        this.rightLeg.addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F);
    }

    @Override
    public void setupAnim(RiftStalkerEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);
        this.leftLeg.xRot = MathHelper.cos(limbSwing * 0.6662F) * 1.25F * limbSwingAmount;
        this.rightLeg.xRot = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.25F * limbSwingAmount;
        this.leftArm.xRot = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 0.75F * limbSwingAmount;
        this.rightArm.xRot = MathHelper.cos(limbSwing * 0.6662F) * 0.75F * limbSwingAmount;
        this.leftHorn.zRot = 0.06F;
        this.rightHorn.zRot = -0.06F;
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.matrix.MatrixStack stack,
                               com.mojang.blaze3d.vertex.IVertexBuilder buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.head.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.body.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.leftArm.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.rightArm.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.leftLeg.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.rightLeg.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
