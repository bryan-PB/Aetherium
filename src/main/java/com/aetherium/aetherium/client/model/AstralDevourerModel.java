package com.aetherium.aetherium.client.model;

import com.aetherium.aetherium.content.boss.AstralDevourerEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/** Floating void warlord redesigned with a skull, torso, articulated arms and spectral lower body. */
public class AstralDevourerModel extends EntityModel<AstralDevourerEntity> {
    private final ModelRenderer head, neck, core, mantle, chestPlate, innerCore, jaw, faceMask;
    private final ModelRenderer eyeLeft, eyeRight, crown, crownLeft, crownRight, halo;
    private final ModelRenderer leftArm, rightArm, leftForearm, rightForearm, leftHand, rightHand;
    private final ModelRenderer leftShoulder, rightShoulder, leftLeg, rightLeg, leftShin, rightShin;
    private final ModelRenderer leftFoot, rightFoot;
    private final ModelRenderer[] tendrils = new ModelRenderer[6];
    private final ModelRenderer[] shards = new ModelRenderer[4];
    private final ModelRenderer[] ribs = new ModelRenderer[4];

    public AstralDevourerModel() {
        this.texWidth = 64;
        this.texHeight = 64;

        head = new ModelRenderer(this, 0, 0);
        head.setPos(0.0F, -13.0F, 0.0F);
        head.addBox(-5.5F, -6.0F, -4.5F, 11.0F, 12.0F, 9.0F);

        jaw = new ModelRenderer(this, 32, 0);
        jaw.setPos(0.0F, 3.4F, -4.1F);
        jaw.addBox(-4.0F, -0.2F, -0.8F, 8.0F, 2.4F, 1.2F);
        head.addChild(jaw);

        faceMask = new ModelRenderer(this, 0, 22);
        faceMask.setPos(0.0F, -1.4F, -4.62F);
        faceMask.addBox(-5.0F, -3.0F, -0.35F, 10.0F, 6.0F, 0.7F);
        head.addChild(faceMask);

        eyeLeft = new ModelRenderer(this, 28, 22);
        eyeLeft.setPos(-2.2F, -1.4F, -5.05F);
        eyeLeft.addBox(-1.1F, -0.55F, -0.2F, 2.2F, 1.1F, 0.4F);
        head.addChild(eyeLeft);
        eyeRight = new ModelRenderer(this, 34, 22);
        eyeRight.setPos(2.2F, -1.4F, -5.05F);
        eyeRight.addBox(-1.1F, -0.55F, -0.2F, 2.2F, 1.1F, 0.4F);
        head.addChild(eyeRight);

        crown = new ModelRenderer(this, 28, 32);
        crown.setPos(0.0F, -5.5F, 0.0F);
        crown.addBox(-4.8F, -2.0F, -4.0F, 9.6F, 3.0F, 8.0F);
        head.addChild(crown);
        crownLeft = new ModelRenderer(this, 32, 28);
        crownLeft.setPos(-3.6F, -1.2F, 0.0F);
        crownLeft.addBox(-1.1F, -7.0F, -1.0F, 2.2F, 7.0F, 2.0F);
        crownLeft.zRot = -0.32F;
        crown.addChild(crownLeft);
        crownRight = new ModelRenderer(this, 42, 28);
        crownRight.setPos(3.6F, -1.2F, 0.0F);
        crownRight.addBox(-1.1F, -7.0F, -1.0F, 2.2F, 7.0F, 2.0F);
        crownRight.zRot = 0.32F;
        crown.addChild(crownRight);

        halo = new ModelRenderer(this, 0, 40);
        halo.setPos(0.0F, -1.5F, 4.5F);
        halo.addBox(-11.0F, -9.0F, -0.5F, 22.0F, 1.0F, 1.0F);
        halo.addBox(-11.0F, 8.0F, -0.5F, 22.0F, 1.0F, 1.0F);
        halo.addBox(-11.0F, -9.0F, -0.5F, 1.0F, 18.0F, 1.0F);
        halo.addBox(10.0F, -9.0F, -0.5F, 1.0F, 18.0F, 1.0F);
        head.addChild(halo);

        neck = new ModelRenderer(this, 0, 0);
        neck.setPos(0.0F, -9.0F, 0.0F);
        neck.addBox(-3.0F, -1.0F, -3.0F, 6.0F, 5.0F, 6.0F);

        core = new ModelRenderer(this, 20, 28);
        core.setPos(0.0F, -1.0F, 0.0F);
        core.addBox(-7.0F, -9.0F, -4.0F, 14.0F, 18.0F, 8.0F);

        mantle = new ModelRenderer(this, 0, 0);
        mantle.setPos(0.0F, -7.0F, 0.0F);
        mantle.addBox(-9.0F, -1.5F, -4.5F, 18.0F, 5.0F, 9.0F);
        core.addChild(mantle);

        chestPlate = new ModelRenderer(this, 38, 40);
        chestPlate.setPos(0.0F, -4.0F, -4.15F);
        chestPlate.addBox(-5.5F, -2.0F, -0.45F, 11.0F, 7.0F, 0.9F);
        core.addChild(chestPlate);

        innerCore = new ModelRenderer(this, 50, 40);
        innerCore.setPos(0.0F, -1.2F, -4.8F);
        innerCore.addBox(-2.5F, -2.5F, -0.45F, 5.0F, 5.0F, 0.9F);
        core.addChild(innerCore);

        for (int i = 0; i < ribs.length; i++) {
            ribs[i] = new ModelRenderer(this, 44, 0);
            float side = i % 2 == 0 ? -1.0F : 1.0F;
            float y = -2.0F + (i / 2) * 4.0F;
            ribs[i].setPos(side * 5.2F, y, -3.4F);
            ribs[i].addBox(-0.7F, -1.8F, -0.6F, 1.4F, 3.6F, 1.2F);
            core.addChild(ribs[i]);
        }

        leftShoulder = new ModelRenderer(this, 40, 0);
        leftShoulder.setPos(8.0F, -7.0F, 0.0F);
        leftShoulder.addBox(-1.5F, -2.4F, -3.2F, 5.5F, 4.8F, 6.4F);
        rightShoulder = new ModelRenderer(this, 40, 12);
        rightShoulder.setPos(-8.0F, -7.0F, 0.0F);
        rightShoulder.addBox(-4.0F, -2.4F, -3.2F, 5.5F, 4.8F, 6.4F);

        leftArm = new ModelRenderer(this, 44, 0);
        leftArm.setPos(8.0F, -7.0F, 0.0F);
        leftArm.addBox(-2.2F, -0.5F, -2.2F, 4.4F, 8.5F, 4.4F);
        rightArm = new ModelRenderer(this, 44, 16);
        rightArm.setPos(-8.0F, -7.0F, 0.0F);
        rightArm.addBox(-2.2F, -0.5F, -2.2F, 4.4F, 8.5F, 4.4F);

        leftForearm = new ModelRenderer(this, 44, 32);
        leftForearm.setPos(0.0F, 7.0F, 0.0F);
        leftForearm.addBox(-2.0F, -0.4F, -2.0F, 4.0F, 8.0F, 4.0F);
        leftArm.addChild(leftForearm);
        rightForearm = new ModelRenderer(this, 44, 44);
        rightForearm.setPos(0.0F, 7.0F, 0.0F);
        rightForearm.addBox(-2.0F, -0.4F, -2.0F, 4.0F, 8.0F, 4.0F);
        rightArm.addChild(rightForearm);

        leftHand = new ModelRenderer(this, 48, 16);
        leftHand.setPos(0.0F, 7.0F, -0.2F);
        leftHand.addBox(-2.0F, -0.5F, -2.0F, 4.0F, 4.0F, 4.0F);
        leftForearm.addChild(leftHand);
        rightHand = new ModelRenderer(this, 48, 28);
        rightHand.setPos(0.0F, 7.0F, -0.2F);
        rightHand.addBox(-2.0F, -0.5F, -2.0F, 4.0F, 4.0F, 4.0F);
        rightForearm.addChild(rightHand);

        leftLeg = new ModelRenderer(this, 40, 40);
        leftLeg.setPos(3.8F, 8.0F, 0.0F);
        leftLeg.addBox(-2.5F, 0.0F, -2.5F, 5.0F, 7.0F, 5.0F);
        rightLeg = new ModelRenderer(this, 40, 48);
        rightLeg.setPos(-3.8F, 8.0F, 0.0F);
        rightLeg.addBox(-2.5F, 0.0F, -2.5F, 5.0F, 7.0F, 5.0F);

        leftShin = new ModelRenderer(this, 40, 40);
        leftShin.setPos(0.0F, 6.0F, 0.0F);
        leftShin.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F);
        leftLeg.addChild(leftShin);
        rightShin = new ModelRenderer(this, 40, 48);
        rightShin.setPos(0.0F, 6.0F, 0.0F);
        rightShin.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F);
        rightLeg.addChild(rightShin);

        leftFoot = new ModelRenderer(this, 44, 40);
        leftFoot.setPos(0.0F, 7.0F, -0.5F);
        leftFoot.addBox(-2.4F, 0.0F, -3.0F, 4.8F, 2.0F, 5.0F);
        leftShin.addChild(leftFoot);
        rightFoot = new ModelRenderer(this, 44, 48);
        rightFoot.setPos(0.0F, 7.0F, -0.5F);
        rightFoot.addBox(-2.4F, 0.0F, -3.0F, 4.8F, 2.0F, 5.0F);
        rightShin.addChild(rightFoot);

        for (int i = 0; i < tendrils.length; i++) {
            float side = i % 2 == 0 ? -1.0F : 1.0F;
            float x = side * (i < 2 ? 4.5F : 6.0F);
            float z = i < 2 ? 2.0F : (i < 4 ? 0.0F : 3.0F);
            tendrils[i] = new ModelRenderer(this, 52, 0);
            tendrils[i].setPos(x, 8.0F + (i % 3) * 1.3F, z);
            tendrils[i].addBox(-1.5F, 0.0F, -1.5F, 3.0F, 11.0F + (i % 2) * 2.0F, 3.0F);
        }

        for (int i = 0; i < shards.length; i++) {
            float side = i % 2 == 0 ? -1.0F : 1.0F;
            shards[i] = new ModelRenderer(this, 52, 16);
            shards[i].setPos(side * (10.0F + (i / 2) * 1.2F), -5.0F + (i % 2) * 7.0F, 0.0F);
            shards[i].addBox(-1.5F, -3.5F, -1.5F, 3.0F, 7.0F, 3.0F);
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

        core.y = -1.0F + idle * 0.65F - attack * 0.55F;
        head.yRot = netHeadYaw * ((float)Math.PI / 180.0F) * 0.72F
                + MathHelper.sin(ageInTicks * 0.035F) * (0.05F + ascended * 0.08F);
        head.xRot = headPitch * ((float)Math.PI / 180.0F) * 0.35F - attack * 0.18F;
        crown.yRot = MathHelper.sin(ageInTicks * (0.045F + awakened * 0.025F)) * (0.16F + ascended * 0.16F);
        crown.xRot = MathHelper.cos(ageInTicks * 0.06F) * 0.06F - attack * 0.28F;
        crownLeft.xRot = -0.12F - awakened * 0.24F - ascended * 0.18F;
        crownRight.xRot = crownLeft.xRot;
        halo.yRot = ageInTicks * (0.025F + awakened * 0.045F + ascended * 0.07F);
        halo.xRot = 0.28F + MathHelper.sin(ageInTicks * 0.035F) * 0.09F + ascended * 0.28F;
        jaw.xRot = attack * 0.4F + ascended * MathHelper.sin(ageInTicks * 0.14F) * 0.10F;

        innerCore.y = -1.2F + MathHelper.sin(ageInTicks * (0.20F + awakened * 0.08F)) * (0.45F + ascended * 0.4F);
        innerCore.zRot = ageInTicks * (0.035F + awakened * 0.035F + ascended * 0.055F);
        mantle.xRot = 0.04F + awakened * 0.12F + ascended * 0.18F;
        chestPlate.xRot = attack * 0.08F;
        for (int i = 0; i < ribs.length; i++) {
            ribs[i].zRot = MathHelper.sin(ageInTicks * 0.045F + i) * 0.035F + ascended * 0.05F;
        }

        leftArm.xRot = -0.18F + MathHelper.sin(ageInTicks * 0.08F) * 0.08F
                - attack * (0.65F + ascended * 0.28F);
        rightArm.xRot = -0.18F - MathHelper.sin(ageInTicks * 0.08F) * 0.08F
                - attack * (0.65F + ascended * 0.28F);
        leftArm.zRot = -0.10F - awakened * 0.12F;
        rightArm.zRot = 0.10F + awakened * 0.12F;
        leftForearm.xRot = 0.18F + attack * 0.45F + ascended * 0.14F;
        rightForearm.xRot = 0.18F + attack * 0.45F + ascended * 0.14F;
        leftHand.xRot = -0.12F - attack * 0.2F;
        rightHand.xRot = -0.12F - attack * 0.2F;
        leftShoulder.xRot = leftArm.xRot * 0.22F;
        rightShoulder.xRot = rightArm.xRot * 0.22F;

        leftLeg.xRot = MathHelper.sin(ageInTicks * 0.08F) * 0.08F - attack * 0.12F;
        rightLeg.xRot = -leftLeg.xRot;
        leftShin.xRot = 0.08F + awakened * 0.06F;
        rightShin.xRot = leftShin.xRot;
        leftFoot.xRot = -leftShin.xRot * 0.45F;
        rightFoot.xRot = leftFoot.xRot;

        for (int i = 0; i < tendrils.length; i++) {
            float phaseOffset = ageInTicks * (0.07F + awakened * 0.025F + ascended * 0.035F) + i * 1.05F;
            tendrils[i].xRot = 0.38F + MathHelper.sin(phaseOffset) * (0.22F + ascended * 0.18F)
                    - attack * (0.45F + ascended * 0.38F);
            tendrils[i].zRot = MathHelper.cos(phaseOffset * 0.8F) * (0.18F + awakened * 0.12F)
                    + (i % 2 == 0 ? -1.0F : 1.0F) * (attack * 0.20F + ascended * 0.12F);
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
        head.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        neck.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        core.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftShoulder.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightShoulder.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftArm.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightArm.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftLeg.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightLeg.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        for (ModelRenderer tendril : tendrils) {
            tendril.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
        for (ModelRenderer shard : shards) {
            shard.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
}
