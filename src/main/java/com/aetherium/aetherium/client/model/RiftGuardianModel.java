package com.aetherium.aetherium.client.model;

import com.aetherium.aetherium.content.boss.RiftGuardianEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/** Rift Paladin: a hulking, horned arena boss with a brutal wind-up and slamming attack. */
public class RiftGuardianModel extends EntityModel<RiftGuardianEntity> {
    private final ModelRenderer head, jaw, mask, eyeLeft, eyeRight, hornLeft, hornRight, crown;
    private final ModelRenderer neck, body, chest, heart, backCrest, leftChest, rightChest;
    private final ModelRenderer leftShoulder, rightShoulder, leftArm, rightArm;
    private final ModelRenderer leftForearm, rightForearm, leftHand, rightHand;
    private final ModelRenderer leftLeg, rightLeg, leftShin, rightShin, leftFoot, rightFoot;
    private final ModelRenderer[] leftClaws = new ModelRenderer[3];
    private final ModelRenderer[] rightClaws = new ModelRenderer[3];
    private final ModelRenderer[] orbitBlades = new ModelRenderer[4];
    private final ModelRenderer[] ribs = new ModelRenderer[3];

    public RiftGuardianModel() {
        texWidth = 64;
        texHeight = 64;

        head = new ModelRenderer(this, 0, 0);
        head.setPos(0.0F, -14.0F, 0.0F);
        head.addBox(-5.0F, -5.0F, -4.5F, 10.0F, 10.0F, 9.0F);

        jaw = new ModelRenderer(this, 38, 0);
        jaw.setPos(0.0F, 3.0F, -4.2F);
        jaw.addBox(-3.5F, 0.0F, -0.8F, 7.0F, 2.0F, 1.3F);
        head.addChild(jaw);

        mask = new ModelRenderer(this, 0, 20);
        mask.setPos(0.0F, -0.6F, -4.65F);
        mask.addBox(-4.3F, -2.2F, -0.35F, 8.6F, 4.4F, 0.7F);
        head.addChild(mask);

        eyeLeft = new ModelRenderer(this, 40, 0);
        eyeLeft.setPos(-2.2F, -1.3F, -5.05F);
        eyeLeft.addBox(-1.0F, -0.45F, -0.2F, 2.0F, 0.9F, 0.4F);
        head.addChild(eyeLeft);
        eyeRight = new ModelRenderer(this, 40, 3);
        eyeRight.setPos(2.2F, -1.3F, -5.05F);
        eyeRight.addBox(-1.0F, -0.45F, -0.2F, 2.0F, 0.9F, 0.4F);
        head.addChild(eyeRight);

        hornLeft = new ModelRenderer(this, 48, 0);
        hornLeft.setPos(-3.5F, -3.6F, 0.0F);
        hornLeft.addBox(-1.0F, -7.5F, -1.0F, 2.0F, 8.0F, 2.0F);
        hornLeft.zRot = -0.28F;
        head.addChild(hornLeft);
        hornRight = new ModelRenderer(this, 48, 10);
        hornRight.setPos(3.5F, -3.6F, 0.0F);
        hornRight.addBox(-1.0F, -7.5F, -1.0F, 2.0F, 8.0F, 2.0F);
        hornRight.zRot = 0.28F;
        head.addChild(hornRight);

        crown = new ModelRenderer(this, 32, 20);
        crown.setPos(0.0F, -4.7F, 0.0F);
        crown.addBox(-1.6F, -4.2F, -1.5F, 3.2F, 4.5F, 3.0F);
        head.addChild(crown);

        neck = new ModelRenderer(this, 0, 0);
        neck.setPos(0.0F, -9.0F, 0.0F);
        neck.addBox(-2.8F, -0.6F, -2.8F, 5.6F, 4.5F, 5.6F);

        body = new ModelRenderer(this, 0, 32);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.addBox(-7.0F, -8.0F, -4.5F, 14.0F, 17.0F, 9.0F);

        chest = new ModelRenderer(this, 36, 32);
        chest.setPos(0.0F, -4.0F, -4.7F);
        chest.addBox(-5.7F, -2.0F, -0.45F, 11.4F, 7.0F, 0.9F);
        body.addChild(chest);

        leftChest = new ModelRenderer(this, 0, 20);
        leftChest.setPos(-2.8F, -4.0F, -5.05F);
        leftChest.addBox(-2.4F, -1.5F, -0.4F, 4.8F, 4.5F, 0.8F);
        body.addChild(leftChest);
        rightChest = new ModelRenderer(this, 0, 20);
        rightChest.setPos(2.8F, -4.0F, -5.05F);
        rightChest.addBox(-2.4F, -1.5F, -0.4F, 4.8F, 4.5F, 0.8F);
        body.addChild(rightChest);

        heart = new ModelRenderer(this, 48, 20);
        heart.setPos(0.0F, -0.2F, -5.25F);
        heart.addBox(-2.1F, -2.1F, -0.45F, 4.2F, 4.2F, 0.9F);
        body.addChild(heart);

        for (int i = 0; i < ribs.length; i++) {
            ribs[i] = new ModelRenderer(this, 36, 20);
            ribs[i].setPos(0.0F, 2.3F + i * 2.0F, -4.62F);
            ribs[i].addBox(-4.2F + i * 0.35F, -0.6F, -0.25F,
                    8.4F - i * 0.7F, 1.2F, 0.5F);
            body.addChild(ribs[i]);
        }

        backCrest = new ModelRenderer(this, 0, 0);
        backCrest.setPos(0.0F, -5.0F, 4.3F);
        backCrest.addBox(-5.0F, -3.5F, 0.0F, 10.0F, 7.0F, 1.5F);
        body.addChild(backCrest);

        leftShoulder = new ModelRenderer(this, 0, 20);
        leftShoulder.setPos(7.0F, -6.0F, 0.0F);
        leftShoulder.addBox(-0.8F, -3.8F, -3.8F, 6.0F, 7.0F, 7.6F);
        rightShoulder = new ModelRenderer(this, 20, 20);
        rightShoulder.setPos(-7.0F, -6.0F, 0.0F);
        rightShoulder.addBox(-5.2F, -3.8F, -3.8F, 6.0F, 7.0F, 7.6F);

        leftArm = new ModelRenderer(this, 0, 0);
        leftArm.setPos(8.2F, -4.7F, 0.0F);
        leftArm.addBox(-2.0F, -1.0F, -2.4F, 4.2F, 7.5F, 4.8F);
        rightArm = new ModelRenderer(this, 20, 0);
        rightArm.setPos(-8.2F, -4.7F, 0.0F);
        rightArm.addBox(-2.2F, -1.0F, -2.4F, 4.2F, 7.5F, 4.8F);

        leftForearm = new ModelRenderer(this, 0, 14);
        leftForearm.setPos(0.0F, 6.0F, 0.0F);
        leftForearm.addBox(-2.2F, -0.4F, -2.9F, 4.4F, 7.5F, 5.8F);
        leftArm.addChild(leftForearm);
        rightForearm = new ModelRenderer(this, 20, 14);
        rightForearm.setPos(0.0F, 6.0F, 0.0F);
        rightForearm.addBox(-2.2F, -0.4F, -2.9F, 4.4F, 7.5F, 5.8F);
        rightArm.addChild(rightForearm);

        leftHand = new ModelRenderer(this, 40, 10);
        leftHand.setPos(0.0F, 6.7F, -0.2F);
        leftHand.addBox(-1.8F, 0.0F, -2.2F, 3.6F, 3.0F, 4.4F);
        leftForearm.addChild(leftHand);
        rightHand = new ModelRenderer(this, 40, 15);
        rightHand.setPos(0.0F, 6.7F, -0.2F);
        rightHand.addBox(-1.8F, 0.0F, -2.2F, 3.6F, 3.0F, 4.4F);
        rightForearm.addChild(rightHand);

        for (int i = 0; i < 3; i++) {
            leftClaws[i] = new ModelRenderer(this, 52, 0);
            leftClaws[i].setPos(-1.15F + i * 1.15F, 2.6F, -2.0F);
            leftClaws[i].addBox(-0.3F, 0.0F, -0.35F, 0.6F, 2.8F, 0.7F);
            leftHand.addChild(leftClaws[i]);
            rightClaws[i] = new ModelRenderer(this, 52, 5);
            rightClaws[i].setPos(-1.15F + i * 1.15F, 2.6F, -2.0F);
            rightClaws[i].addBox(-0.3F, 0.0F, -0.35F, 0.6F, 2.8F, 0.7F);
            rightHand.addChild(rightClaws[i]);
        }

        leftLeg = new ModelRenderer(this, 0, 0);
        leftLeg.setPos(3.6F, 8.0F, 0.0F);
        leftLeg.addBox(-2.5F, 0.0F, -2.6F, 5.0F, 8.0F, 5.2F);
        rightLeg = new ModelRenderer(this, 20, 0);
        rightLeg.setPos(-3.6F, 8.0F, 0.0F);
        rightLeg.addBox(-2.5F, 0.0F, -2.6F, 5.0F, 8.0F, 5.2F);
        leftShin = new ModelRenderer(this, 0, 18);
        leftShin.setPos(0.0F, 7.0F, 0.0F);
        leftShin.addBox(-2.0F, -0.2F, -2.1F, 4.0F, 8.0F, 4.2F);
        leftLeg.addChild(leftShin);
        rightShin = new ModelRenderer(this, 20, 18);
        rightShin.setPos(0.0F, 7.0F, 0.0F);
        rightShin.addBox(-2.0F, -0.2F, -2.1F, 4.0F, 8.0F, 4.2F);
        rightLeg.addChild(rightShin);

        leftFoot = new ModelRenderer(this, 40, 28);
        leftFoot.setPos(0.0F, 7.3F, -0.3F);
        leftFoot.addBox(-2.3F, 0.0F, -2.8F, 4.6F, 2.0F, 4.8F);
        leftShin.addChild(leftFoot);
        rightFoot = new ModelRenderer(this, 40, 36);
        rightFoot.setPos(0.0F, 7.3F, -0.3F);
        rightFoot.addBox(-2.3F, 0.0F, -2.8F, 4.6F, 2.0F, 4.8F);
        rightShin.addChild(rightFoot);

        for (int i = 0; i < orbitBlades.length; i++) {
            orbitBlades[i] = new ModelRenderer(this, 52, 12);
            float angle = (float)(Math.PI * 2.0D * i / orbitBlades.length);
            orbitBlades[i].setPos(MathHelper.cos(angle) * 10.0F, -3.0F + (i % 2) * 5.0F, 2.0F);
            orbitBlades[i].addBox(-0.8F, -4.0F, -0.8F, 1.6F, 8.0F, 1.6F);
        }
    }

    @Override
    public void setupAnim(RiftGuardianEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float walk = Math.min(1.0F, limbSwingAmount);
        float gait = limbSwing * 0.52F;
        float idle = MathHelper.sin(ageInTicks * 0.075F);
        int state = entity.getAttackState();
        float timer = entity.getAttackAnimationTicks();
        float windup = state == 1 ? MathHelper.clamp((timer - 12.0F) / 12.0F, 0.0F, 1.0F) : 0.0F;
        float impact = state == 1 ? MathHelper.clamp((12.0F - timer) / 12.0F, 0.0F, 1.0F) : 0.0F;
        float volley = state == 2 ? 1.0F : 0.0F;
        float rage = entity.getPhase() >= 2 ? 1.0F : 0.0F;

        head.yRot = netHeadYaw * ((float)Math.PI / 180.0F);
        head.xRot = headPitch * ((float)Math.PI / 180.0F) - windup * 0.12F + impact * 0.28F;
        jaw.xRot = MathHelper.sin(ageInTicks * 0.08F) * 0.035F + rage * 0.12F;
        crown.zRot = idle * 0.035F + rage * 0.13F;
        hornLeft.zRot = -0.28F - rage * 0.10F + idle * 0.025F;
        hornRight.zRot = -hornLeft.zRot;

        body.y = idle * 0.16F + windup * 0.35F - impact * 0.45F;
        body.xRot = -windup * 0.16F + impact * 0.28F;
        chest.xRot = -windup * 0.08F + impact * 0.12F;
        heart.y = -0.2F + MathHelper.sin(ageInTicks * (0.18F + rage * 0.12F)) * (0.35F + rage * 0.35F);
        heart.zRot = ageInTicks * (0.035F + rage * 0.05F);
        for (int i = 0; i < ribs.length; i++) {
            ribs[i].zRot = MathHelper.sin(ageInTicks * 0.07F + i) * 0.025F + impact * 0.04F;
        }
        backCrest.xRot = 0.10F + rage * 0.28F + windup * 0.15F;

        leftArm.xRot = -MathHelper.cos(gait) * 0.34F * walk - windup * 2.15F + impact * 2.25F - volley * 0.75F;
        rightArm.xRot = MathHelper.cos(gait) * 0.34F * walk - windup * 2.15F + impact * 2.25F - volley * 0.75F;
        leftArm.zRot = -0.08F - rage * 0.16F - volley * 0.48F;
        rightArm.zRot = 0.08F + rage * 0.16F + volley * 0.48F;
        leftForearm.xRot = 0.10F + windup * 0.55F + impact * 0.45F + volley * 0.85F;
        rightForearm.xRot = 0.10F + windup * 0.55F + impact * 0.45F + volley * 0.85F;
        leftHand.xRot = -0.08F - impact * 0.22F - volley * 0.2F;
        rightHand.xRot = leftHand.xRot;
        leftShoulder.xRot = leftArm.xRot * 0.25F;
        rightShoulder.xRot = rightArm.xRot * 0.25F;
        leftShoulder.zRot = -rage * 0.12F;
        rightShoulder.zRot = rage * 0.12F;

        leftLeg.xRot = MathHelper.cos(gait) * 0.48F * walk + impact * 0.18F;
        rightLeg.xRot = MathHelper.cos(gait + (float)Math.PI) * 0.48F * walk + impact * 0.18F;
        leftShin.xRot = Math.max(0.0F, -leftLeg.xRot) * 0.55F;
        rightShin.xRot = Math.max(0.0F, -rightLeg.xRot) * 0.55F;
        leftFoot.xRot = -leftShin.xRot * 0.35F;
        rightFoot.xRot = -rightShin.xRot * 0.35F;

        for (int i = 0; i < orbitBlades.length; i++) {
            float angle = ageInTicks * (0.035F + rage * 0.04F) + i * (float)Math.PI * 0.5F;
            orbitBlades[i].x = MathHelper.cos(angle) * (10.0F + rage * 1.5F);
            orbitBlades[i].z = 2.0F + MathHelper.sin(angle) * 2.5F;
            orbitBlades[i].y = -3.0F + MathHelper.sin(angle * 1.5F) * 2.0F;
            orbitBlades[i].yRot = -angle;
            orbitBlades[i].zRot = MathHelper.sin(angle * 2.0F) * 0.18F;
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
        for (ModelRenderer blade : orbitBlades) blade.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
