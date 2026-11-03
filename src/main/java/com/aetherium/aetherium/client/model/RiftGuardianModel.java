package com.aetherium.aetherium.client.model;

import com.aetherium.aetherium.content.boss.RiftGuardianEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/** Iron Warden: a heavy knight silhouette built around an oversized cleaving greatsword. */
public class RiftGuardianModel extends EntityModel<RiftGuardianEntity> {
    private final ModelRenderer head, jaw, visor, eyeLeft, eyeRight, hornLeft, hornRight;
    private final ModelRenderer neck, body, chestPlate, core, backPlate, leftShoulder, rightShoulder;
    private final ModelRenderer leftSpike, rightSpike, leftArm, rightArm, leftForearm, rightForearm;
    private final ModelRenderer leftHand, rightHand, leftLeg, rightLeg, leftShin, rightShin;
    private final ModelRenderer leftBoot, rightBoot, greatsword, swordGuard, swordGrip, swordPommel;
    private final ModelRenderer[] chestRibs = new ModelRenderer[3];
    private float phaseTintRed = 1.0F;
    private float phaseTintGreen = 1.0F;

    public RiftGuardianModel() {
        texWidth = 64;
        texHeight = 64;

        head = new ModelRenderer(this, 0, 0);
        head.setPos(0.0F, -14.0F, 0.0F);
        head.addBox(-4.8F, -5.0F, -4.2F, 9.6F, 10.0F, 8.4F);
        jaw = new ModelRenderer(this, 38, 0);
        jaw.setPos(0.0F, 2.6F, -3.9F);
        jaw.addBox(-3.4F, 0.0F, -0.7F, 6.8F, 1.8F, 1.2F);
        head.addChild(jaw);
        visor = new ModelRenderer(this, 0, 19);
        visor.setPos(0.0F, -0.8F, -4.35F);
        visor.addBox(-4.0F, -1.2F, -0.35F, 8.0F, 2.4F, 0.7F);
        head.addChild(visor);
        eyeLeft = new ModelRenderer(this, 44, 0);
        eyeLeft.setPos(-2.0F, -1.1F, -4.82F);
        eyeLeft.addBox(-0.55F, -0.35F, -0.18F, 1.1F, 0.7F, 0.36F);
        head.addChild(eyeLeft);
        eyeRight = new ModelRenderer(this, 48, 0);
        eyeRight.setPos(2.0F, -1.1F, -4.82F);
        eyeRight.addBox(-0.55F, -0.35F, -0.18F, 1.1F, 0.7F, 0.36F);
        head.addChild(eyeRight);

        hornLeft = new ModelRenderer(this, 40, 8);
        hornLeft.setPos(-3.4F, -3.4F, 0.0F);
        hornLeft.addBox(-0.9F, -5.6F, -0.9F, 1.8F, 6.0F, 1.8F);
        hornLeft.zRot = -0.2F;
        head.addChild(hornLeft);
        hornRight = new ModelRenderer(this, 48, 8);
        hornRight.setPos(3.4F, -3.4F, 0.0F);
        hornRight.addBox(-0.9F, -5.6F, -0.9F, 1.8F, 6.0F, 1.8F);
        hornRight.zRot = 0.2F;
        head.addChild(hornRight);

        neck = new ModelRenderer(this, 0, 0);
        neck.setPos(0.0F, -8.2F, 0.0F);
        neck.addBox(-2.8F, -0.4F, -2.8F, 5.6F, 4.0F, 5.6F);

        body = new ModelRenderer(this, 0, 32);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.addBox(-7.0F, -8.0F, -4.4F, 14.0F, 17.0F, 8.8F);
        chestPlate = new ModelRenderer(this, 32, 32);
        chestPlate.setPos(0.0F, -4.1F, -4.55F);
        chestPlate.addBox(-5.4F, -2.0F, -0.35F, 10.8F, 6.8F, 0.7F);
        body.addChild(chestPlate);
        core = new ModelRenderer(this, 48, 32);
        core.setPos(0.0F, -0.2F, -4.95F);
        core.addBox(-1.8F, -1.8F, -0.3F, 3.6F, 3.6F, 0.6F);
        body.addChild(core);
        backPlate = new ModelRenderer(this, 0, 48);
        backPlate.setPos(0.0F, -4.5F, 4.2F);
        backPlate.addBox(-5.0F, -3.0F, 0.0F, 10.0F, 6.0F, 1.3F);
        body.addChild(backPlate);
        for (int i = 0; i < chestRibs.length; i++) {
            chestRibs[i] = new ModelRenderer(this, 32, 20);
            chestRibs[i].setPos(0.0F, 2.0F + i * 2.0F, -4.62F);
            chestRibs[i].addBox(-3.8F + i * 0.25F, -0.45F, -0.25F,
                    7.6F - i * 0.5F, 0.9F, 0.5F);
            body.addChild(chestRibs[i]);
        }

        leftShoulder = new ModelRenderer(this, 0, 20);
        leftShoulder.setPos(7.0F, -6.2F, 0.0F);
        leftShoulder.addBox(-0.6F, -3.6F, -3.5F, 5.8F, 6.8F, 7.0F);
        rightShoulder = new ModelRenderer(this, 20, 20);
        rightShoulder.setPos(-7.0F, -6.2F, 0.0F);
        rightShoulder.addBox(-5.2F, -3.6F, -3.5F, 5.8F, 6.8F, 7.0F);
        leftSpike = new ModelRenderer(this, 40, 16);
        leftSpike.setPos(2.8F, -2.4F, 0.0F);
        leftSpike.addBox(-0.7F, -4.2F, -0.7F, 1.4F, 4.5F, 1.4F);
        leftShoulder.addChild(leftSpike);
        rightSpike = new ModelRenderer(this, 44, 16);
        rightSpike.setPos(-2.8F, -2.4F, 0.0F);
        rightSpike.addBox(-0.7F, -4.2F, -0.7F, 1.4F, 4.5F, 1.4F);
        rightShoulder.addChild(rightSpike);

        leftArm = new ModelRenderer(this, 0, 0);
        leftArm.setPos(8.0F, -4.8F, 0.0F);
        leftArm.addBox(-1.9F, -0.6F, -2.2F, 3.8F, 7.0F, 4.4F);
        rightArm = new ModelRenderer(this, 16, 0);
        rightArm.setPos(-8.0F, -4.8F, 0.0F);
        rightArm.addBox(-1.9F, -0.6F, -2.2F, 3.8F, 7.0F, 4.4F);
        leftForearm = new ModelRenderer(this, 0, 12);
        leftForearm.setPos(0.0F, 5.8F, 0.0F);
        leftForearm.addBox(-2.0F, 0.0F, -2.7F, 4.0F, 7.0F, 5.4F);
        leftArm.addChild(leftForearm);
        rightForearm = new ModelRenderer(this, 16, 12);
        rightForearm.setPos(0.0F, 5.8F, 0.0F);
        rightForearm.addBox(-2.0F, 0.0F, -2.7F, 4.0F, 7.0F, 5.4F);
        rightArm.addChild(rightForearm);
        leftHand = new ModelRenderer(this, 40, 22);
        leftHand.setPos(0.0F, 6.4F, -0.1F);
        leftHand.addBox(-1.7F, 0.0F, -2.1F, 3.4F, 3.0F, 4.2F);
        leftForearm.addChild(leftHand);
        rightHand = new ModelRenderer(this, 40, 28);
        rightHand.setPos(0.0F, 6.4F, -0.1F);
        rightHand.addBox(-1.7F, 0.0F, -2.1F, 3.4F, 3.0F, 4.2F);
        rightForearm.addChild(rightHand);

        // Oversized weapon is a separate articulated part held by the right hand.
        greatsword = new ModelRenderer(this, 0, 48);
        greatsword.setPos(0.0F, 1.4F, -1.7F);
        greatsword.addBox(-1.6F, -15.5F, -0.55F, 3.2F, 17.0F, 1.1F);
        swordGuard = new ModelRenderer(this, 32, 48);
        swordGuard.setPos(0.0F, -1.4F, 0.0F);
        swordGuard.addBox(-4.3F, -0.6F, -0.75F, 8.6F, 1.2F, 1.5F);
        greatsword.addChild(swordGuard);
        swordGrip = new ModelRenderer(this, 48, 48);
        swordGrip.setPos(0.0F, 1.0F, 0.0F);
        swordGrip.addBox(-0.65F, 0.0F, -0.5F, 1.3F, 4.5F, 1.0F);
        greatsword.addChild(swordGrip);
        swordPommel = new ModelRenderer(this, 56, 48);
        swordPommel.setPos(0.0F, 5.0F, 0.0F);
        swordPommel.addBox(-1.1F, -0.5F, -0.8F, 2.2F, 1.8F, 1.6F);
        greatsword.addChild(swordPommel);
        rightHand.addChild(greatsword);

        leftLeg = new ModelRenderer(this, 0, 0);
        leftLeg.setPos(3.6F, 8.0F, 0.0F);
        leftLeg.addBox(-2.4F, 0.0F, -2.5F, 4.8F, 8.0F, 5.0F);
        rightLeg = new ModelRenderer(this, 20, 0);
        rightLeg.setPos(-3.6F, 8.0F, 0.0F);
        rightLeg.addBox(-2.4F, 0.0F, -2.5F, 4.8F, 8.0F, 5.0F);
        leftShin = new ModelRenderer(this, 0, 18);
        leftShin.setPos(0.0F, 7.0F, 0.0F);
        leftShin.addBox(-1.9F, -0.2F, -2.0F, 3.8F, 7.5F, 4.0F);
        leftLeg.addChild(leftShin);
        rightShin = new ModelRenderer(this, 20, 18);
        rightShin.setPos(0.0F, 7.0F, 0.0F);
        rightShin.addBox(-1.9F, -0.2F, -2.0F, 3.8F, 7.5F, 4.0F);
        rightLeg.addChild(rightShin);
        leftBoot = new ModelRenderer(this, 40, 36);
        leftBoot.setPos(0.0F, 6.8F, -0.4F);
        leftBoot.addBox(-2.1F, 0.0F, -2.7F, 4.2F, 2.0F, 4.8F);
        leftShin.addChild(leftBoot);
        rightBoot = new ModelRenderer(this, 48, 36);
        rightBoot.setPos(0.0F, 6.8F, -0.4F);
        rightBoot.addBox(-2.1F, 0.0F, -2.7F, 4.2F, 2.0F, 4.8F);
        rightShin.addChild(rightBoot);
    }

    @Override
    public void setupAnim(RiftGuardianEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float walk = Math.min(1.0F, limbSwingAmount);
        float gait = limbSwing * 0.52F;
        float idle = MathHelper.sin(ageInTicks * 0.07F);
        int state = entity.getAttackState();
        float timer = entity.getAttackAnimationTicks();
        // The slam and sweep both deal damage at timer 11: raise, strike, then recover.
        // Make the strongest weapon motion land on that frame rather than several ticks later.
        float weaponRaise = MathHelper.clamp((24.0F - timer) / 10.0F, 0.0F, 1.0F)
                * MathHelper.clamp((timer - 11.0F) / 3.0F, 0.0F, 1.0F);
        float weaponStrike = MathHelper.clamp((14.0F - timer) / 3.0F, 0.0F, 1.0F)
                * MathHelper.clamp((timer - 1.0F) / 10.0F, 0.0F, 1.0F);
        float windup = state == 1 ? weaponRaise : 0.0F;
        float impact = state == 1 ? weaponStrike : 0.0F;
        float volley = state == 2 ? 1.0F : 0.0F;
        float chargeWindup = state == 4 ? MathHelper.clamp((timer - 12.0F) / 12.0F, 0.0F, 1.0F) : 0.0F;
        float chargeRush = state == 4 ? 1.0F - chargeWindup : 0.0F;
        float stagger = state == 5 ? MathHelper.clamp(timer / 50.0F, 0.0F, 1.0F) : 0.0F;
        float phaseShift = state == 6
                ? MathHelper.sin(MathHelper.clamp(1.0F - timer / 48.0F, 0.0F, 1.0F) * (float)Math.PI)
                : 0.0F;
        float sweepWindup = state == 3 ? weaponRaise : 0.0F;
        float sweep = state == 3 ? weaponStrike : 0.0F;
        float rage = entity.getPhase() >= 2 ? 1.0F : 0.0F;
        // A colder void hue makes the second phase apparent even between attacks.
        this.phaseTintRed = rage > 0.0F ? 0.78F : 1.0F;
        this.phaseTintGreen = rage > 0.0F ? 0.68F : 1.0F;
        float death = MathHelper.clamp(entity.deathTime / 20.0F, 0.0F, 1.0F);

        head.yRot = netHeadYaw * ((float)Math.PI / 180.0F);
        head.xRot = headPitch * ((float)Math.PI / 180.0F) - windup * 0.12F
                + impact * 0.2F + stagger * 0.18F + death * 0.28F;
        jaw.xRot = MathHelper.sin(ageInTicks * 0.08F) * 0.035F + rage * 0.1F;
        hornLeft.zRot = -0.2F - rage * 0.45F + idle * 0.025F;
        hornRight.zRot = -hornLeft.zRot;
        hornLeft.xRot = -rage * 0.12F;
        hornRight.xRot = -rage * 0.12F;

        body.y = idle * 0.14F + windup * 0.35F - impact * 0.4F;
        body.xRot = -windup * 0.14F + impact * 0.24F + chargeWindup * 0.12F
                - chargeRush * 0.48F + stagger * 0.22F - phaseShift * 0.24F
                + death * 0.82F;
        chestPlate.xRot = -windup * 0.05F + impact * 0.1F - phaseShift * 0.32F
                - rage * 0.08F;
        core.y = -0.2F - phaseShift * 0.62F
                + MathHelper.sin(ageInTicks * (0.16F + rage * 0.12F)) * (0.3F + rage * 0.25F);
        core.xRot = phaseShift * 0.7F;
        core.zRot = ageInTicks * (0.025F + rage * 0.035F + phaseShift * 0.13F);
        backPlate.xRot = 0.08F + rage * 0.62F + phaseShift * 0.18F;
        backPlate.yRot = MathHelper.sin(ageInTicks * 0.045F) * rage * 0.08F;
        for (int i = 0; i < chestRibs.length; i++) {
            float ribSide = i == 1 ? 0.0F : (i == 0 ? -1.0F : 1.0F);
            chestRibs[i].zRot = MathHelper.sin(ageInTicks * (0.05F + rage * 0.035F) + i) * 0.025F
                    + ribSide * (rage * 0.20F + phaseShift * 0.08F);
        }

        leftArm.xRot = -MathHelper.cos(gait) * 0.3F * walk - windup * 1.25F
                + impact * 1.45F - volley * 0.45F + stagger * 0.72F - phaseShift * 0.9F
                + death * 1.0F;
        rightArm.xRot = MathHelper.cos(gait) * 0.3F * walk - windup * 1.55F + impact * 1.65F
                - volley * 0.35F - sweepWindup * 1.35F + sweep * 2.0F - chargeWindup * 1.5F
                + chargeRush * 1.0F + stagger * 0.78F + phaseShift * 0.9F + death * 1.0F;
        leftArm.zRot = -0.06F - rage * 0.12F + sweep * 0.42F - phaseShift * 0.42F;
        rightArm.zRot = 0.08F + rage * 0.08F + sweepWindup * 0.32F - sweep * 1.45F
                + chargeWindup * 0.18F - chargeRush * 0.12F + phaseShift * 0.42F;
        leftForearm.xRot = 0.1F + windup * 0.45F + impact * 0.45F;
        rightForearm.xRot = 0.12F + windup * 0.5F + impact * 0.5F + sweepWindup * 0.55F - sweep * 0.45F;
        leftHand.xRot = -0.05F - impact * 0.12F;
        rightHand.xRot = -0.08F - sweep * 0.28F;
        leftShoulder.xRot = leftArm.xRot * 0.2F - rage * 0.08F;
        rightShoulder.xRot = rightArm.xRot * 0.2F - rage * 0.08F;
        leftShoulder.zRot = -rage * 0.1F;
        rightShoulder.zRot = rage * 0.1F;
        leftSpike.xRot = -0.12F - rage * 0.42F;
        rightSpike.xRot = -0.12F - rage * 0.42F;
        leftSpike.zRot = -rage * 0.28F;
        rightSpike.zRot = rage * 0.28F;

        leftLeg.x = 3.6F + rage * 0.80F;
        rightLeg.x = -3.6F - rage * 0.80F;
        leftLeg.xRot = MathHelper.cos(gait) * 0.46F * walk + impact * 0.15F;
        rightLeg.xRot = MathHelper.cos(gait + (float)Math.PI) * 0.46F * walk + impact * 0.15F;
        leftShin.xRot = Math.max(0.0F, -leftLeg.xRot) * 0.5F;
        rightShin.xRot = Math.max(0.0F, -rightLeg.xRot) * 0.5F;
        leftBoot.xRot = -leftShin.xRot * 0.35F;
        rightBoot.xRot = -rightShin.xRot * 0.35F;
        // Give each heavy attack a readable weapon silhouette before the hit lands.
        // The blade is a child of the right hand, so these rotations layer onto the arm pose.
        greatsword.xRot = -windup * 0.95F - sweepWindup * 0.72F
                + impact * 0.62F + sweep * 1.18F
                - chargeWindup * 0.28F + chargeRush * 0.82F
                + stagger * 0.34F + phaseShift * 0.52F - rage * 0.12F;
        greatsword.yRot = -sweepWindup * 0.22F + sweep * 0.68F
                + chargeRush * 0.16F + phaseShift * 0.24F;
        greatsword.zRot = -0.12F + windup * 0.22F + impact * 0.18F
                + sweepWindup * 0.38F - sweep * 0.82F
                + chargeWindup * 0.28F - chargeRush * 0.18F
                + stagger * 0.24F + phaseShift * 0.36F + death * 0.72F
                - rage * 0.08F;
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.matrix.MatrixStack stack,
                               com.mojang.blaze3d.vertex.IVertexBuilder buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        float tintedRed = red * this.phaseTintRed;
        float tintedGreen = green * this.phaseTintGreen;
        head.render(stack, buffer, packedLight, packedOverlay, tintedRed, tintedGreen, blue, alpha);
        neck.render(stack, buffer, packedLight, packedOverlay, tintedRed, tintedGreen, blue, alpha);
        body.render(stack, buffer, packedLight, packedOverlay, tintedRed, tintedGreen, blue, alpha);
        leftShoulder.render(stack, buffer, packedLight, packedOverlay, tintedRed, tintedGreen, blue, alpha);
        rightShoulder.render(stack, buffer, packedLight, packedOverlay, tintedRed, tintedGreen, blue, alpha);
        leftArm.render(stack, buffer, packedLight, packedOverlay, tintedRed, tintedGreen, blue, alpha);
        rightArm.render(stack, buffer, packedLight, packedOverlay, tintedRed, tintedGreen, blue, alpha);
        leftLeg.render(stack, buffer, packedLight, packedOverlay, tintedRed, tintedGreen, blue, alpha);
        rightLeg.render(stack, buffer, packedLight, packedOverlay, tintedRed, tintedGreen, blue, alpha);
    }
}
