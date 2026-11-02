package com.aetherium.aetherium.client.model;

import com.aetherium.aetherium.content.boss.AstralDevourerEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/** Astral Devourer: a winged void tyrant with an animated halo, claws and six living tendrils. */
public class AstralDevourerModel extends EntityModel<AstralDevourerEntity> {
    private final ModelRenderer head, jaw, mask, eye, eyeLeft, eyeRight, hornLeft, hornRight, halo;
    private final ModelRenderer neck, body, mantle, chest, core, leftShoulder, rightShoulder;
    private final ModelRenderer leftArm, rightArm, leftForearm, rightForearm, leftHand, rightHand;
    private final ModelRenderer leftWing, rightWing, leftWingTip, rightWingTip;
    private final ModelRenderer leftLeg, rightLeg, leftShin, rightShin, leftClaw, rightClaw;
    private final ModelRenderer[] tendrils = new ModelRenderer[6];
    private final ModelRenderer[] shards = new ModelRenderer[4];
    private final ModelRenderer[] ribs = new ModelRenderer[4];

    public AstralDevourerModel() {
        texWidth = 64;
        texHeight = 64;

        head = new ModelRenderer(this, 0, 0);
        head.setPos(0.0F, -15.0F, 0.0F);
        head.addBox(-5.5F, -5.5F, -4.5F, 11.0F, 11.0F, 9.0F);

        jaw = new ModelRenderer(this, 36, 0);
        jaw.setPos(0.0F, 3.0F, -4.15F);
        jaw.addBox(-3.8F, 0.0F, -0.7F, 7.6F, 2.4F, 1.1F);
        head.addChild(jaw);

        mask = new ModelRenderer(this, 0, 20);
        mask.setPos(0.0F, -1.0F, -4.62F);
        mask.addBox(-4.7F, -2.5F, -0.35F, 9.4F, 5.0F, 0.7F);
        head.addChild(mask);

        eye = new ModelRenderer(this, 32, 20);
        eye.setPos(0.0F, -1.1F, -5.1F);
        eye.addBox(-2.3F, -2.3F, -0.3F, 4.6F, 4.6F, 0.6F);
        head.addChild(eye);
        eyeLeft = new ModelRenderer(this, 44, 20);
        eyeLeft.setPos(-3.1F, -1.2F, -4.95F);
        eyeLeft.addBox(-0.7F, -0.4F, -0.25F, 1.4F, 0.8F, 0.5F);
        head.addChild(eyeLeft);
        eyeRight = new ModelRenderer(this, 48, 20);
        eyeRight.setPos(3.1F, -1.2F, -4.95F);
        eyeRight.addBox(-0.7F, -0.4F, -0.25F, 1.4F, 0.8F, 0.5F);
        head.addChild(eyeRight);

        hornLeft = new ModelRenderer(this, 0, 28);
        hornLeft.setPos(-4.0F, -3.5F, 0.0F);
        hornLeft.addBox(-1.1F, -7.0F, -1.0F, 2.2F, 7.2F, 2.0F);
        hornLeft.zRot = -0.28F;
        head.addChild(hornLeft);
        hornRight = new ModelRenderer(this, 12, 28);
        hornRight.setPos(4.0F, -3.5F, 0.0F);
        hornRight.addBox(-1.1F, -7.0F, -1.0F, 2.2F, 7.2F, 2.0F);
        hornRight.zRot = 0.28F;
        head.addChild(hornRight);

        halo = new ModelRenderer(this, 0, 40);
        halo.setPos(0.0F, -1.0F, 4.8F);
        halo.addBox(-11.0F, -9.0F, -0.4F, 22.0F, 1.0F, 0.8F);
        halo.addBox(-11.0F, 8.0F, -0.4F, 22.0F, 1.0F, 0.8F);
        halo.addBox(-11.0F, -9.0F, -0.4F, 1.0F, 18.0F, 0.8F);
        halo.addBox(10.0F, -9.0F, -0.4F, 1.0F, 18.0F, 0.8F);
        head.addChild(halo);

        neck = new ModelRenderer(this, 0, 0);
        neck.setPos(0.0F, -10.0F, 0.0F);
        neck.addBox(-2.8F, -0.4F, -2.8F, 5.6F, 4.5F, 5.6F);

        body = new ModelRenderer(this, 20, 28);
        body.setPos(0.0F, -1.0F, 0.0F);
        body.addBox(-7.0F, -8.0F, -4.0F, 14.0F, 16.0F, 8.0F);

        mantle = new ModelRenderer(this, 0, 0);
        mantle.setPos(0.0F, -6.0F, 0.0F);
        mantle.addBox(-8.5F, -1.0F, -4.5F, 17.0F, 4.0F, 9.0F);
        body.addChild(mantle);

        chest = new ModelRenderer(this, 36, 32);
        chest.setPos(0.0F, -3.8F, -4.15F);
        chest.addBox(-5.3F, -2.0F, -0.45F, 10.6F, 6.5F, 0.9F);
        body.addChild(chest);

        core = new ModelRenderer(this, 48, 40);
        core.setPos(0.0F, -1.0F, -4.7F);
        core.addBox(-2.5F, -2.5F, -0.5F, 5.0F, 5.0F, 1.0F);
        body.addChild(core);

        for (int i = 0; i < ribs.length; i++) {
            ribs[i] = new ModelRenderer(this, 36, 20);
            float side = i % 2 == 0 ? -1.0F : 1.0F;
            ribs[i].setPos(side * 5.0F, -1.8F + (i / 2) * 4.0F, -3.8F);
            ribs[i].addBox(-0.65F, -1.7F, -0.45F, 1.3F, 3.4F, 0.9F);
            body.addChild(ribs[i]);
        }

        leftShoulder = new ModelRenderer(this, 0, 20);
        leftShoulder.setPos(7.0F, -6.0F, 0.0F);
        leftShoulder.addBox(-0.8F, -2.8F, -3.0F, 5.0F, 5.6F, 6.0F);
        rightShoulder = new ModelRenderer(this, 20, 20);
        rightShoulder.setPos(-7.0F, -6.0F, 0.0F);
        rightShoulder.addBox(-4.2F, -2.8F, -3.0F, 5.0F, 5.6F, 6.0F);

        leftArm = new ModelRenderer(this, 0, 0);
        leftArm.setPos(7.5F, -6.0F, 0.0F);
        leftArm.addBox(-1.8F, -0.5F, -2.1F, 3.8F, 7.0F, 4.2F);
        rightArm = new ModelRenderer(this, 20, 0);
        rightArm.setPos(-7.5F, -6.0F, 0.0F);
        rightArm.addBox(-2.0F, -0.5F, -2.1F, 3.8F, 7.0F, 4.2F);
        leftForearm = new ModelRenderer(this, 0, 14);
        leftForearm.setPos(0.0F, 6.0F, 0.0F);
        leftForearm.addBox(-1.8F, 0.0F, -2.3F, 3.8F, 7.0F, 4.6F);
        leftArm.addChild(leftForearm);
        rightForearm = new ModelRenderer(this, 20, 14);
        rightForearm.setPos(0.0F, 6.0F, 0.0F);
        rightForearm.addBox(-2.0F, 0.0F, -2.3F, 3.8F, 7.0F, 4.6F);
        rightArm.addChild(rightForearm);

        leftHand = new ModelRenderer(this, 40, 0);
        leftHand.setPos(0.0F, 6.2F, -0.2F);
        leftHand.addBox(-1.8F, 0.0F, -2.0F, 3.6F, 3.0F, 4.0F);
        leftForearm.addChild(leftHand);
        rightHand = new ModelRenderer(this, 40, 8);
        rightHand.setPos(0.0F, 6.2F, -0.2F);
        rightHand.addBox(-1.8F, 0.0F, -2.0F, 3.6F, 3.0F, 4.0F);
        rightForearm.addChild(rightHand);

        leftWing = new ModelRenderer(this, 48, 0);
        leftWing.setPos(6.0F, -8.0F, 3.5F);
        leftWing.addBox(0.0F, -1.0F, -1.0F, 8.0F, 12.0F, 2.0F);
        body.addChild(leftWing);
        rightWing = new ModelRenderer(this, 48, 14);
        rightWing.setPos(-6.0F, -8.0F, 3.5F);
        rightWing.addBox(-8.0F, -1.0F, -1.0F, 8.0F, 12.0F, 2.0F);
        body.addChild(rightWing);

        leftWingTip = new ModelRenderer(this, 48, 28);
        leftWingTip.setPos(7.0F, 9.0F, 0.0F);
        leftWingTip.addBox(0.0F, -1.0F, -0.6F, 6.0F, 9.0F, 1.2F);
        leftWing.addChild(leftWingTip);
        rightWingTip = new ModelRenderer(this, 48, 38);
        rightWingTip.setPos(-7.0F, 9.0F, 0.0F);
        rightWingTip.addBox(-6.0F, -1.0F, -0.6F, 6.0F, 9.0F, 1.2F);
        rightWing.addChild(rightWingTip);

        leftLeg = new ModelRenderer(this, 0, 0);
        leftLeg.setPos(3.6F, 7.0F, 0.0F);
        leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F);
        rightLeg = new ModelRenderer(this, 18, 0);
        rightLeg.setPos(-3.6F, 7.0F, 0.0F);
        rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F);
        leftShin = new ModelRenderer(this, 0, 12);
        leftShin.setPos(0.0F, 5.5F, 0.0F);
        leftShin.addBox(-1.6F, 0.0F, -1.7F, 3.2F, 7.0F, 3.4F);
        leftLeg.addChild(leftShin);
        rightShin = new ModelRenderer(this, 16, 12);
        rightShin.setPos(0.0F, 5.5F, 0.0F);
        rightShin.addBox(-1.6F, 0.0F, -1.7F, 3.2F, 7.0F, 3.4F);
        rightLeg.addChild(rightShin);
        leftClaw = new ModelRenderer(this, 36, 44);
        leftClaw.setPos(0.0F, 6.4F, -0.4F);
        leftClaw.addBox(-1.8F, 0.0F, -2.8F, 3.6F, 2.0F, 4.0F);
        leftShin.addChild(leftClaw);
        rightClaw = new ModelRenderer(this, 48, 44);
        rightClaw.setPos(0.0F, 6.4F, -0.4F);
        rightClaw.addBox(-1.8F, 0.0F, -2.8F, 3.6F, 2.0F, 4.0F);
        rightShin.addChild(rightClaw);

        for (int i = 0; i < tendrils.length; i++) {
            float side = i % 2 == 0 ? -1.0F : 1.0F;
            tendrils[i] = new ModelRenderer(this, 0, 48);
            tendrils[i].setPos(side * (4.2F + (i / 2) * 0.8F), 6.0F + (i % 3) * 1.2F, 3.0F);
            tendrils[i].addBox(-1.2F, 0.0F, -1.2F, 2.4F, 10.0F + (i % 2) * 3.0F, 2.4F);
        }

        for (int i = 0; i < shards.length; i++) {
            shards[i] = new ModelRenderer(this, 36, 48);
            shards[i].setPos((i % 2 == 0 ? -1.0F : 1.0F) * 10.0F, -2.0F + (i / 2) * 6.0F, 0.0F);
            shards[i].addBox(-1.3F, -3.5F, -1.3F, 2.6F, 7.0F, 2.6F);
        }
    }

    @Override
    public void setupAnim(AstralDevourerEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        int phase = entity.getPhase();
        int state = entity.getAttackState();
        float timer = entity.getAttackAnimationTicks();
        float awakened = phase >= 2 ? 1.0F : 0.0F;
        float ascended = phase >= 3 ? 1.0F : 0.0F;
        float idle = MathHelper.sin(ageInTicks * 0.075F);
        float charge = state == 2 ? MathHelper.clamp(timer / 24.0F, 0.0F, 1.0F) : 0.0F;
        float beam = state == 3 ? 1.0F : 0.0F;
        float nova = state == 5 ? 1.0F : 0.0F;
        float cast = state == 1 ? 1.0F : 0.0F;
        float pull = state == 4 ? 1.0F : 0.0F;

        body.y = -1.0F + idle * 0.65F - charge * 0.35F + nova * 0.5F;
        body.xRot = -charge * 0.18F + beam * 0.15F - nova * 0.24F;
        head.yRot = netHeadYaw * ((float)Math.PI / 180.0F) * 0.72F
                + MathHelper.sin(ageInTicks * 0.04F) * (0.06F + ascended * 0.08F);
        head.xRot = headPitch * ((float)Math.PI / 180.0F) * 0.38F - charge * 0.2F + beam * 0.12F;
        jaw.xRot = MathHelper.sin(ageInTicks * 0.12F) * 0.04F + nova * 0.5F + beam * 0.3F;
        eye.y = -1.1F + MathHelper.sin(ageInTicks * 0.2F) * 0.18F - charge * 0.45F;
        eye.zRot = ageInTicks * (0.025F + ascended * 0.04F);
        hornLeft.zRot = -0.28F - ascended * 0.12F;
        hornRight.zRot = -hornLeft.zRot;

        halo.yRot = ageInTicks * (0.025F + awakened * 0.05F + ascended * 0.095F);
        halo.xRot = 0.25F + MathHelper.sin(ageInTicks * 0.04F) * 0.08F + ascended * 0.3F;
        halo.zRot = MathHelper.sin(ageInTicks * 0.05F) * 0.08F + beam * 0.22F;

        core.y = -1.0F + MathHelper.sin(ageInTicks * (0.18F + awakened * 0.08F)) * (0.45F + ascended * 0.35F);
        core.zRot = ageInTicks * (0.035F + ascended * 0.08F);
        chest.xRot = charge * 0.1F - nova * 0.12F;
        mantle.xRot = 0.04F + awakened * 0.12F + ascended * 0.2F;
        for (int i = 0; i < ribs.length; i++) {
            ribs[i].zRot = MathHelper.sin(ageInTicks * 0.06F + i) * 0.04F + ascended * 0.05F;
        }

        leftArm.xRot = -0.18F + MathHelper.sin(ageInTicks * 0.08F) * 0.08F
                - cast * 0.95F - charge * 1.55F + beam * 0.25F - nova * 1.1F + pull * 0.45F;
        rightArm.xRot = -0.18F - MathHelper.sin(ageInTicks * 0.08F) * 0.08F
                - cast * 0.95F - charge * 1.55F + beam * 0.25F - nova * 1.1F + pull * 0.45F;
        leftArm.zRot = -0.12F - awakened * 0.15F - beam * 0.35F - nova * 0.55F;
        rightArm.zRot = 0.12F + awakened * 0.15F + beam * 0.35F + nova * 0.55F;
        leftForearm.xRot = 0.18F + cast * 0.75F + charge * 0.95F + beam * 0.7F + nova * 1.1F;
        rightForearm.xRot = 0.18F + cast * 0.75F + charge * 0.95F + beam * 0.7F + nova * 1.1F;
        leftHand.xRot = -0.15F - nova * 0.25F;
        rightHand.xRot = leftHand.xRot;
        leftShoulder.xRot = leftArm.xRot * 0.22F;
        rightShoulder.xRot = rightArm.xRot * 0.22F;

        leftWing.zRot = -0.35F - awakened * 0.45F - ascended * 0.35F
                + MathHelper.sin(ageInTicks * (0.12F + ascended * 0.08F)) * (0.12F + awakened * 0.12F);
        rightWing.zRot = -leftWing.zRot;
        leftWing.xRot = 0.12F + ascended * 0.25F + beam * 0.18F;
        rightWing.xRot = -leftWing.xRot;
        leftWingTip.zRot = -0.35F - awakened * 0.25F - ascended * 0.3F;
        rightWingTip.zRot = -leftWingTip.zRot;

        leftLeg.xRot = MathHelper.sin(ageInTicks * 0.08F) * 0.07F - nova * 0.25F;
        rightLeg.xRot = -leftLeg.xRot;
        leftShin.xRot = 0.1F + awakened * 0.08F;
        rightShin.xRot = leftShin.xRot;
        leftClaw.xRot = 0.12F + ascended * 0.22F;
        rightClaw.xRot = leftClaw.xRot;

        for (int i = 0; i < tendrils.length; i++) {
            float wave = ageInTicks * (0.075F + awakened * 0.03F + ascended * 0.045F) + i * 1.05F;
            tendrils[i].xRot = 0.45F + MathHelper.sin(wave) * (0.23F + ascended * 0.16F)
                    - charge * 0.35F - beam * 0.5F + nova * 0.65F;
            tendrils[i].zRot = MathHelper.cos(wave * 0.8F) * (0.18F + awakened * 0.15F + ascended * 0.1F)
                    + (i % 2 == 0 ? -1.0F : 1.0F) * (nova * 0.28F + beam * 0.12F);
        }
        for (int i = 0; i < shards.length; i++) {
            float angle = ageInTicks * (0.04F + awakened * 0.04F + ascended * 0.075F) + i * 1.57F;
            shards[i].x = MathHelper.cos(angle) * (10.0F + ascended * 2.0F);
            shards[i].y = -2.0F + (i / 2) * 6.0F + MathHelper.sin(angle) * 2.0F;
            shards[i].z = MathHelper.sin(angle) * 2.8F;
            shards[i].yRot = -angle;
            shards[i].zRot = MathHelper.sin(angle * 2.0F) * 0.24F + ascended * 0.2F;
        }
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.matrix.MatrixStack stack,
                               com.mojang.blaze3d.vertex.IVertexBuilder buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        head.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        neck.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftShoulder.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightShoulder.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftArm.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightArm.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftLeg.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightLeg.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        for (ModelRenderer tendril : tendrils) tendril.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        for (ModelRenderer shard : shards) shard.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
