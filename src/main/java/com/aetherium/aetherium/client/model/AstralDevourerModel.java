package com.aetherium.aetherium.client.model;

import com.aetherium.aetherium.content.boss.AstralDevourerEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/** The Void Sovereign: floating mask, broken halo, orbiting shards and animated tendrils. */
public class AstralDevourerModel extends EntityModel<AstralDevourerEntity> {
    private final ModelRenderer core, crown, eyeLeft, eyeRight, innerCore, halo, jaw, faceMask;
    private final ModelRenderer crownLeft, crownRight, mantle, cheekLeft, cheekRight;
    private final ModelRenderer[] tendrils = new ModelRenderer[6];
    private final ModelRenderer[] shards = new ModelRenderer[4];

    public AstralDevourerModel() {
        this.texWidth = 64;
        this.texHeight = 64;
        core = new ModelRenderer(this, 0, 0);
        core.setPos(0.0F, 19.0F, 0.0F);
        core.addBox(-12.0F, -11.0F, -8.0F, 24.0F, 22.0F, 16.0F);

        mantle = new ModelRenderer(this, 0, 40);
        mantle.setPos(0.0F, 4.0F, 0.0F);
        mantle.addBox(-14.0F, -2.0F, -6.0F, 28.0F, 5.0F, 12.0F);
        core.addChild(mantle);

        crown = new ModelRenderer(this, 0, 40);
        crown.setPos(0.0F, -8.0F, 0.0F);
        crown.addBox(-8.0F, -7.0F, -6.0F, 16.0F, 7.0F, 12.0F);
        core.addChild(crown);
        crownLeft = new ModelRenderer(this, 32, 40);
        crownLeft.setPos(-6.0F, -6.0F, -1.0F);
        crownLeft.addBox(-1.5F, -7.0F, -1.5F, 3.0F, 7.5F, 3.0F);
        crownLeft.zRot = -0.42F;
        crown.addChild(crownLeft);
        crownRight = new ModelRenderer(this, 44, 40);
        crownRight.setPos(6.0F, -6.0F, -1.0F);
        crownRight.addBox(-1.5F, -7.0F, -1.5F, 3.0F, 7.5F, 3.0F);
        crownRight.zRot = 0.42F;
        crown.addChild(crownRight);

        faceMask = new ModelRenderer(this, 0, 52);
        faceMask.setPos(0.0F, -1.0F, -8.1F);
        faceMask.addBox(-6.5F, -4.0F, -0.6F, 13.0F, 8.0F, 1.2F);
        core.addChild(faceMask);
        eyeLeft = new ModelRenderer(this, 32, 0);
        eyeLeft.setPos(-4.0F, -1.5F, -8.85F);
        eyeLeft.addBox(-2.0F, -1.5F, -0.45F, 4.0F, 3.0F, 0.9F);
        core.addChild(eyeLeft);
        eyeRight = new ModelRenderer(this, 32, 5);
        eyeRight.setPos(4.0F, -1.5F, -8.85F);
        eyeRight.addBox(-2.0F, -1.5F, -0.45F, 4.0F, 3.0F, 0.9F);
        core.addChild(eyeRight);
        cheekLeft = new ModelRenderer(this, 40, 10);
        cheekLeft.setPos(-7.0F, 1.0F, -7.7F);
        cheekLeft.addBox(-1.0F, -2.0F, -0.3F, 2.0F, 5.0F, 1.0F);
        core.addChild(cheekLeft);
        cheekRight = new ModelRenderer(this, 46, 10);
        cheekRight.setPos(7.0F, 1.0F, -7.7F);
        cheekRight.addBox(-1.0F, -2.0F, -0.3F, 2.0F, 5.0F, 1.0F);
        core.addChild(cheekRight);
        jaw = new ModelRenderer(this, 32, 16);
        jaw.setPos(0.0F, 5.0F, -7.9F);
        jaw.addBox(-4.5F, -0.5F, -0.8F, 9.0F, 2.5F, 1.2F);
        core.addChild(jaw);

        innerCore = new ModelRenderer(this, 44, 0);
        innerCore.setPos(0.0F, 1.0F, -8.45F);
        innerCore.addBox(-3.2F, -3.2F, -0.6F, 6.4F, 6.4F, 1.2F);
        core.addChild(innerCore);

        // Four disconnected segments form a readable broken halo rather than a single floating bar.
        halo = new ModelRenderer(this, 36, 24);
        halo.setPos(0.0F, -3.0F, 0.0F);
        halo.addBox(-14.0F, -10.0F, -0.5F, 28.0F, 1.0F, 1.0F);
        halo.addBox(-14.0F, 9.0F, -0.5F, 28.0F, 1.0F, 1.0F);
        halo.addBox(-14.0F, -10.0F, -0.5F, 1.0F, 20.0F, 1.0F);
        halo.addBox(13.0F, -10.0F, -0.5F, 1.0F, 20.0F, 1.0F);
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
            shards[i].addBox(-1.8F, -4.5F, -1.8F, 3.6F, 9.0F, 3.6F);
        }
    }

    @Override
    public void setupAnim(AstralDevourerEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float idle = MathHelper.sin(ageInTicks * 0.075F);
        float attack = entity.getAttackAnimationTicks() / 16.0F;
        int phase = entity.getPhase();
        float awakened = phase >= 2 ? 1.0F : 0.0F;
        float ascended = phase >= 3 ? 1.0F : 0.0F;

        core.y = 19.0F + idle * 1.15F - attack * 0.55F;
        core.yRot = netHeadYaw * ((float)Math.PI / 180.0F) * 0.14F
                + MathHelper.sin(ageInTicks * 0.035F) * (0.08F + ascended * 0.12F);
        core.xRot = headPitch * ((float)Math.PI / 180.0F) * 0.10F;
        crown.yRot = MathHelper.sin(ageInTicks * (0.045F + awakened * 0.025F)) * (0.22F + ascended * 0.18F);
        crown.xRot = MathHelper.cos(ageInTicks * 0.06F) * 0.08F - attack * 0.3F;
        crownLeft.xRot = -0.1F - awakened * 0.28F - ascended * 0.20F;
        crownRight.xRot = crownLeft.xRot;
        innerCore.y = 1.0F + MathHelper.sin(ageInTicks * (0.20F + awakened * 0.08F)) * (0.55F + ascended * 0.45F);
        innerCore.zRot = ageInTicks * (0.035F + awakened * 0.035F + ascended * 0.055F);
        halo.yRot = ageInTicks * (0.025F + awakened * 0.045F + ascended * 0.07F);
        halo.xRot = 0.35F + MathHelper.sin(ageInTicks * 0.035F) * 0.10F + ascended * 0.35F;
        halo.zRot = MathHelper.sin(ageInTicks * 0.045F) * 0.08F + awakened * 0.18F;

        mantle.xRot = 0.05F + awakened * 0.18F + ascended * 0.20F;
        jaw.xRot = attack * 0.45F + ascended * MathHelper.sin(ageInTicks * 0.14F) * 0.12F;
        for (int i = 0; i < tendrils.length; i++) {
            float phaseOffset = ageInTicks * (0.07F + awakened * 0.025F + ascended * 0.035F) + i * 1.05F;
            tendrils[i].xRot = 0.38F + MathHelper.sin(phaseOffset) * (0.24F + ascended * 0.18F)
                    - attack * (0.55F + ascended * 0.45F);
            tendrils[i].zRot = MathHelper.cos(phaseOffset * 0.8F) * (0.20F + awakened * 0.12F)
                    + (i % 2 == 0 ? -1.0F : 1.0F) * (attack * 0.22F + ascended * 0.12F);
        }
        for (int i = 0; i < shards.length; i++) {
            shards[i].yRot = ageInTicks * (0.035F + i * 0.004F + awakened * 0.035F + ascended * 0.055F)
                    + i * 1.57F;
            shards[i].xRot = 0.3F + MathHelper.sin(ageInTicks * (0.08F + ascended * 0.04F) + i) * 0.20F;
            shards[i].zRot = (i % 2 == 0 ? 1.0F : -1.0F) * (awakened * 0.25F + ascended * 0.48F);
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
