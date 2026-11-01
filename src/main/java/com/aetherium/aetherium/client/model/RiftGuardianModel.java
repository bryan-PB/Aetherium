package com.aetherium.aetherium.client.model;

import com.aetherium.aetherium.content.boss.RiftGuardianEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/** Broad, angular guardian silhouette built from a small number of cuboids. */
public class RiftGuardianModel extends EntityModel<RiftGuardianEntity> {
    private final ModelRenderer head;
    private final ModelRenderer body;
    private final ModelRenderer leftArm;
    private final ModelRenderer rightArm;
    private final ModelRenderer leftLeg;
    private final ModelRenderer rightLeg;
    private final ModelRenderer leftHorn;
    private final ModelRenderer rightHorn;

    public RiftGuardianModel() {
        this.texWidth = 64;
        this.texHeight = 64;
        this.head = new ModelRenderer(this, 0, 0);
        this.head.setPos(0.0F, 2.0F, 0.0F);
        this.head.addBox(-4.5F, -4.0F, -4.0F, 9.0F, 8.0F, 8.0F);

        this.leftHorn = new ModelRenderer(this, 0, 18);
        this.leftHorn.setPos(-3.0F, -2.0F, 0.0F);
        this.leftHorn.addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F);
        this.head.addChild(this.leftHorn);
        this.rightHorn = new ModelRenderer(this, 8, 18);
        this.rightHorn.setPos(3.0F, -2.0F, 0.0F);
        this.rightHorn.addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F);
        this.head.addChild(this.rightHorn);

        this.body = new ModelRenderer(this, 0, 28);
        this.body.setPos(0.0F, 11.0F, 0.0F);
        this.body.addBox(-6.0F, -7.0F, -3.5F, 12.0F, 14.0F, 7.0F);

        this.leftArm = new ModelRenderer(this, 38, 0);
        this.leftArm.setPos(7.0F, 7.0F, 0.0F);
        this.leftArm.addBox(-2.0F, -1.0F, -2.0F, 4.0F, 13.0F, 4.0F);
        this.rightArm = new ModelRenderer(this, 38, 18);
        this.rightArm.setPos(-7.0F, 7.0F, 0.0F);
        this.rightArm.addBox(-2.0F, -1.0F, -2.0F, 4.0F, 13.0F, 4.0F);

        this.leftLeg = new ModelRenderer(this, 0, 48);
        this.leftLeg.setPos(3.0F, 18.0F, 0.0F);
        this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F);
        this.rightLeg = new ModelRenderer(this, 16, 48);
        this.rightLeg.setPos(-3.0F, 18.0F, 0.0F);
        this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F);
    }

    @Override
    public void setupAnim(RiftGuardianEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);
        float swing = MathHelper.cos(limbSwing * 0.55F) * 0.75F * limbSwingAmount;
        this.leftLeg.xRot = swing;
        this.rightLeg.xRot = -swing;
        this.leftArm.xRot = -swing * 0.45F;
        this.rightArm.xRot = swing * 0.45F;
        this.leftHorn.zRot = 0.04F + MathHelper.sin(ageInTicks * 0.035F) * 0.035F;
        this.rightHorn.zRot = -this.leftHorn.zRot;
        if (entity.getPhase() == 2) {
            this.leftArm.xRot -= 0.12F;
            this.rightArm.xRot -= 0.12F;
        }
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
