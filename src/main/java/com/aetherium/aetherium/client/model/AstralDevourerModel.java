package com.aetherium.aetherium.client.model;

import com.aetherium.aetherium.content.boss.AstralDevourerEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/** Floating cosmic horror with a faceted core, orbiting crown and six animated tendrils. */
public class AstralDevourerModel extends EntityModel<AstralDevourerEntity> {
    private final ModelRenderer core, crown, eyeLeft, eyeRight, innerCore, halo;
    private final ModelRenderer[] tendrils = new ModelRenderer[6];
    private final ModelRenderer[] shards = new ModelRenderer[4];

    public AstralDevourerModel() {
        this.texWidth = 64;
        this.texHeight = 64;
        core = new ModelRenderer(this, 0, 0);
        core.setPos(0.0F, 19.0F, 0.0F);
        core.addBox(-12.0F, -11.0F, -8.0F, 24.0F, 22.0F, 16.0F);

        crown = new ModelRenderer(this, 0, 40);
        crown.setPos(0.0F, -8.0F, 0.0F);
        crown.addBox(-8.0F, -7.0F, -6.0F, 16.0F, 7.0F, 12.0F);
        core.addChild(crown);

        eyeLeft = new ModelRenderer(this, 32, 0);
        eyeLeft.setPos(-4.0F, -1.5F, -8.2F);
        eyeLeft.addBox(-2.0F, -1.5F, -0.5F, 4.0F, 3.0F, 1.0F);
        core.addChild(eyeLeft);
        eyeRight = new ModelRenderer(this, 32, 5);
        eyeRight.setPos(4.0F, -1.5F, -8.2F);
        eyeRight.addBox(-2.0F, -1.5F, -0.5F, 4.0F, 3.0F, 1.0F);
        core.addChild(eyeRight);

        innerCore = new ModelRenderer(this, 44, 0);
        innerCore.setPos(0.0F, 1.0F, -8.4F);
        innerCore.addBox(-3.0F, -3.0F, -0.6F, 6.0F, 6.0F, 1.2F);
        core.addChild(innerCore);

        halo = new ModelRenderer(this, 36, 14);
        halo.setPos(0.0F, -3.0F, 0.0F);
        halo.addBox(-14.0F, -0.5F, -0.5F, 28.0F, 1.0F, 1.0F);
        core.addChild(halo);

        for (int i = 0; i < tendrils.length; i++) {
            float x = (i % 2 == 0 ? -1.0F : 1.0F) * (i < 2 ? 13.0F : 10.0F);
            float z = i < 2 ? -3.0F : (i < 4 ? 4.0F : 0.0F);
            tendrils[i] = new ModelRenderer(this, 0, 56);
            tendrils[i].setPos(x, 8.0F + (i % 3) * 2.0F, z);
            tendrils[i].addBox(-2.5F, 0.0F, -2.5F, 5.0F, 14.0F + (i % 2) * 3.0F, 5.0F);
        }

        for (int i = 0; i < shards.length; i++) {
            shards[i] = new ModelRenderer(this, 40, 24);
            shards[i].setPos((i - 1.5F) * 7.0F, 16.0F + (i % 2) * 5.0F, 0.0F);
            shards[i].addBox(-1.5F, -3.5F, -1.5F, 3.0F, 7.0F, 3.0F);
        }
    }

    @Override
    public void setupAnim(AstralDevourerEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float idle = MathHelper.sin(ageInTicks * 0.075F);
        float attack = entity.getAttackAnimationTicks() / 16.0F;
        core.y = 19.0F + idle * 1.15F;
        core.yRot = netHeadYaw * ((float)Math.PI / 180.0F) * 0.14F
                + MathHelper.sin(ageInTicks * 0.035F) * 0.08F;
        core.xRot = headPitch * ((float)Math.PI / 180.0F) * 0.10F;
        crown.yRot = MathHelper.sin(ageInTicks * 0.045F) * 0.22F;
        crown.xRot = MathHelper.cos(ageInTicks * 0.06F) * 0.08F;
        innerCore.y = 1.0F + MathHelper.sin(ageInTicks * 0.20F) * 0.55F;
        halo.yRot = ageInTicks * 0.025F;
        halo.xRot = 0.35F + MathHelper.sin(ageInTicks * 0.035F) * 0.10F;
        for (int i = 0; i < tendrils.length; i++) {
            float phase = ageInTicks * 0.07F + i * 1.05F;
            tendrils[i].xRot = 0.38F + MathHelper.sin(phase) * 0.24F - attack * 0.55F;
            tendrils[i].zRot = MathHelper.cos(phase * 0.8F) * 0.20F
                    + (i % 2 == 0 ? -1.0F : 1.0F) * attack * 0.22F;
        }
        for (int i = 0; i < shards.length; i++) {
            shards[i].yRot = ageInTicks * (0.035F + i * 0.004F) + i * 1.57F;
            shards[i].xRot = 0.3F + MathHelper.sin(ageInTicks * 0.08F + i) * 0.20F;
        }
        if (entity.getPhase() == 3) {
            crown.xRot += MathHelper.sin(ageInTicks * 0.12F) * 0.13F;
            for (ModelRenderer tendril : tendrils) tendril.xRot -= 0.12F;
        }
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.matrix.MatrixStack stack,
                               com.mojang.blaze3d.vertex.IVertexBuilder buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        core.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        for (ModelRenderer tendril : tendrils) {
            tendril.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
        for (ModelRenderer shard : shards) {
            shard.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
}
