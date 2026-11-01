package com.aetherium.aetherium.client.model;

import com.aetherium.aetherium.content.boss.AstralDevourerEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/** Floating void creature: one core and four articulated-looking low-poly tendrils. */
public class AstralDevourerModel extends EntityModel<AstralDevourerEntity> {
    private final ModelRenderer core;
    private final ModelRenderer crown;
    private final ModelRenderer leftEye;
    private final ModelRenderer rightEye;
    private final ModelRenderer[] tendrils = new ModelRenderer[4];

    public AstralDevourerModel() {
        this.texWidth = 64;
        this.texHeight = 64;
        this.core = new ModelRenderer(this, 0, 0);
        this.core.setPos(0.0F, 10.0F, 0.0F);
        this.core.addBox(-7.0F, -8.0F, -5.0F, 14.0F, 16.0F, 10.0F);

        this.crown = new ModelRenderer(this, 0, 28);
        this.crown.setPos(0.0F, -6.0F, 0.0F);
        this.crown.addBox(-5.0F, -5.0F, -4.0F, 10.0F, 5.0F, 8.0F);
        this.core.addChild(this.crown);

        this.leftEye = new ModelRenderer(this, 36, 0);
        this.leftEye.setPos(-3.0F, -1.0F, -5.1F);
        this.leftEye.addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F);
        this.core.addChild(this.leftEye);
        this.rightEye = new ModelRenderer(this, 36, 4);
        this.rightEye.setPos(3.0F, -1.0F, -5.1F);
        this.rightEye.addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F);
        this.core.addChild(this.rightEye);

        for (int i = 0; i < 4; i++) {
            this.tendrils[i] = new ModelRenderer(this, 40, 12 + i * 10);
            this.tendrils[i].setPos(i < 2 ? -5.0F : 5.0F, 11.0F, i % 2 == 0 ? -2.5F : 2.5F);
            this.tendrils[i].addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F);
        }
    }

    @Override
    public void setupAnim(AstralDevourerEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.core.y = 10.0F + MathHelper.sin(ageInTicks * 0.08F) * 0.65F;
        this.core.yRot = netHeadYaw * ((float) Math.PI / 180.0F) * 0.18F;
        this.core.xRot = headPitch * ((float) Math.PI / 180.0F) * 0.12F;
        for (int i = 0; i < this.tendrils.length; i++) {
            this.tendrils[i].xRot = 0.25F + MathHelper.sin(ageInTicks * 0.065F + i * 1.6F) * 0.22F;
            this.tendrils[i].zRot = MathHelper.cos(ageInTicks * 0.055F + i * 1.3F) * 0.18F;
        }
        if (entity.getPhase() == 3) {
            this.crown.xRot = MathHelper.sin(ageInTicks * 0.12F) * 0.10F;
        } else {
            this.crown.xRot = 0.0F;
        }
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.matrix.MatrixStack stack,
                               com.mojang.blaze3d.vertex.IVertexBuilder buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.core.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        for (ModelRenderer tendril : this.tendrils) {
            tendril.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
}
