package com.aetherium.aetherium.client.model;

import com.aetherium.aetherium.content.boss.AstralDevourerEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/** A winged, many-tentacled eye monster inspired by classic arena bosses. */
public class AstralDevourerModel extends EntityModel<AstralDevourerEntity> {
    private final ModelRenderer eyeShell, upperLid, lowerLid, iris, pupil, glint, jaw, fangLeft, fangRight;
    private final ModelRenderer wingLeft, wingRight, wingTipLeft, wingTipRight, sideEyeLeft, sideEyeRight;
    private final ModelRenderer[] tendrils = new ModelRenderer[6];
    private final ModelRenderer[] barbs = new ModelRenderer[4];
    private final ModelRenderer[] awakenedCrown = new ModelRenderer[5];
    private final ModelRenderer wingRibLeft, wingRibRight, ascendedHornLeft, ascendedHornRight;
    private float phaseTintRed = 1.0F;
    private float phaseTintGreen = 1.0F;
    private float phaseTintBlue = 1.0F;

    public AstralDevourerModel() {
        texWidth = 64;
        texHeight = 64;

        eyeShell = new ModelRenderer(this, 0, 0);
        eyeShell.setPos(0.0F, -1.0F, 0.0F);
        eyeShell.addBox(-9.0F, -7.5F, -4.0F, 18.0F, 15.0F, 8.0F);

        upperLid = new ModelRenderer(this, 32, 0);
        upperLid.setPos(0.0F, -1.2F, -4.25F);
        upperLid.addBox(-8.0F, -7.0F, -0.55F, 16.0F, 3.0F, 1.1F);
        eyeShell.addChild(upperLid);
        lowerLid = new ModelRenderer(this, 32, 6);
        lowerLid.setPos(0.0F, -1.0F, -4.25F);
        lowerLid.addBox(-8.0F, 4.0F, -0.55F, 16.0F, 3.0F, 1.1F);
        eyeShell.addChild(lowerLid);

        iris = new ModelRenderer(this, 0, 42);
        iris.setPos(0.0F, -1.0F, -4.65F);
        iris.addBox(-4.2F, -4.2F, -0.35F, 8.4F, 8.4F, 0.7F);
        eyeShell.addChild(iris);
        pupil = new ModelRenderer(this, 18, 42);
        pupil.setPos(0.0F, -1.0F, -5.05F);
        pupil.addBox(-1.6F, -2.4F, -0.35F, 3.2F, 4.8F, 0.7F);
        eyeShell.addChild(pupil);
        glint = new ModelRenderer(this, 28, 42);
        glint.setPos(-1.0F, -2.0F, -5.45F);
        glint.addBox(-0.5F, -0.5F, -0.2F, 1.0F, 1.0F, 0.4F);
        eyeShell.addChild(glint);

        jaw = new ModelRenderer(this, 0, 54);
        jaw.setPos(0.0F, 6.2F, -3.0F);
        jaw.addBox(-4.0F, 0.0F, -1.4F, 8.0F, 2.1F, 2.0F);
        eyeShell.addChild(jaw);
        fangLeft = new ModelRenderer(this, 20, 54);
        fangLeft.setPos(-2.3F, 1.0F, -1.4F);
        fangLeft.addBox(-0.55F, -0.2F, -0.7F, 1.1F, 2.6F, 1.3F);
        jaw.addChild(fangLeft);
        fangRight = new ModelRenderer(this, 24, 54);
        fangRight.setPos(2.3F, 1.0F, -1.4F);
        fangRight.addBox(-0.55F, -0.2F, -0.7F, 1.1F, 2.6F, 1.3F);
        jaw.addChild(fangRight);

        sideEyeLeft = new ModelRenderer(this, 32, 14);
        sideEyeLeft.setPos(-7.0F, 0.4F, -3.2F);
        sideEyeLeft.addBox(-1.8F, -1.7F, -0.6F, 3.6F, 3.4F, 1.2F);
        eyeShell.addChild(sideEyeLeft);
        sideEyeRight = new ModelRenderer(this, 42, 14);
        sideEyeRight.setPos(7.0F, 0.4F, -3.2F);
        sideEyeRight.addBox(-1.8F, -1.7F, -0.6F, 3.6F, 3.4F, 1.2F);
        eyeShell.addChild(sideEyeRight);

        wingLeft = new ModelRenderer(this, 0, 48);
        wingLeft.setPos(-7.0F, -3.2F, 2.6F);
        wingLeft.addBox(-10.0F, -2.0F, -0.5F, 10.0F, 9.0F, 1.0F);
        eyeShell.addChild(wingLeft);
        wingRight = new ModelRenderer(this, 22, 48);
        wingRight.setPos(7.0F, -3.2F, 2.6F);
        wingRight.addBox(0.0F, -2.0F, -0.5F, 10.0F, 9.0F, 1.0F);
        eyeShell.addChild(wingRight);
        wingTipLeft = new ModelRenderer(this, 44, 48);
        wingTipLeft.setPos(-9.0F, 6.0F, 0.0F);
        wingTipLeft.addBox(-4.0F, -1.0F, -0.4F, 4.0F, 7.0F, 0.8F);
        wingLeft.addChild(wingTipLeft);
        wingTipRight = new ModelRenderer(this, 52, 48);
        wingTipRight.setPos(9.0F, 6.0F, 0.0F);
        wingTipRight.addBox(0.0F, -1.0F, -0.4F, 4.0F, 7.0F, 0.8F);
        wingRight.addChild(wingTipRight);

        // An articulated crown rises above the central eye in the awakened forms.
        for (int i = 0; i < awakenedCrown.length; i++) {
            float x = (i - 2) * 3.1F;
            awakenedCrown[i] = new ModelRenderer(this, 52, 0);
            awakenedCrown[i].setPos(x, -6.5F, -2.7F);
            awakenedCrown[i].addBox(-0.65F, -3.8F - (2 - Math.abs(i - 2)) * 1.1F,
                    -0.65F, 1.3F, 4.0F + (2 - Math.abs(i - 2)) * 1.1F, 1.3F);
            awakenedCrown[i].zRot = (i - 2) * 0.19F;
            eyeShell.addChild(awakenedCrown[i]);
        }

        // Ribs give the otherwise flat wings a segmented insect-like profile.
        wingRibLeft = new ModelRenderer(this, 40, 32);
        wingRibLeft.setPos(-1.0F, 0.0F, -0.4F);
        wingRibLeft.addBox(-8.4F, -0.6F, -0.6F, 8.4F, 1.2F, 1.2F);
        wingLeft.addChild(wingRibLeft);
        wingRibRight = new ModelRenderer(this, 40, 32);
        wingRibRight.setPos(1.0F, 0.0F, -0.4F);
        wingRibRight.addBox(0.0F, -0.6F, -0.6F, 8.4F, 1.2F, 1.2F);
        wingRight.addChild(wingRibRight);

        // Phase III grows a second pair of massive outward horns.
        ascendedHornLeft = new ModelRenderer(this, 52, 0);
        ascendedHornLeft.setPos(-8.0F, -4.6F, -1.2F);
        ascendedHornLeft.addBox(-1.2F, -7.5F, -0.8F, 2.4F, 8.0F, 1.6F);
        eyeShell.addChild(ascendedHornLeft);
        ascendedHornRight = new ModelRenderer(this, 52, 0);
        ascendedHornRight.setPos(8.0F, -4.6F, -1.2F);
        ascendedHornRight.addBox(-1.2F, -7.5F, -0.8F, 2.4F, 8.0F, 1.6F);
        eyeShell.addChild(ascendedHornRight);

        for (int i = 0; i < tendrils.length; i++) {
            float side = i % 2 == 0 ? -1.0F : 1.0F;
            float x = side * (i < 2 ? 4.0F : (i < 4 ? 7.0F : 5.5F));
            float y = i < 2 ? 4.0F : (i < 4 ? 1.0F : 6.0F);
            float z = i < 2 ? 1.5F : 3.0F;
            tendrils[i] = new ModelRenderer(this, 44, 32);
            tendrils[i].setPos(x, y, z);
            tendrils[i].addBox(-1.1F, 0.0F, -1.1F, 2.2F, 10.0F + (i % 2) * 3.0F, 2.2F);
            // Keep every appendage in the same moving coordinate space as the eye shell.
            eyeShell.addChild(tendrils[i]);
        }

        for (int i = 0; i < barbs.length; i++) {
            float angle = i * (float)Math.PI * 0.5F;
            barbs[i] = new ModelRenderer(this, 52, 0);
            barbs[i].setPos(MathHelper.cos(angle) * 8.0F, MathHelper.sin(angle) * 5.5F, 2.5F);
            barbs[i].addBox(-0.8F, -3.2F, -0.8F, 1.6F, 6.4F, 1.6F);
            eyeShell.addChild(barbs[i]);
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
        // Textures stay unchanged, but each awakening gets an unmistakable color identity.
        this.phaseTintRed = phase == 2 ? 0.72F : 1.0F;
        this.phaseTintGreen = phase == 2 ? 0.78F : (phase == 3 ? 0.60F : 1.0F);
        this.phaseTintBlue = phase == 3 ? 0.76F : 1.0F;
        for (int i = 0; i < awakenedCrown.length; i++) {
            awakenedCrown[i].visible = phase >= 2;
            awakenedCrown[i].xRot = MathHelper.sin(ageInTicks * 0.09F + i) * 0.08F;
        }
        wingRibLeft.visible = phase >= 2;
        wingRibRight.visible = phase >= 2;
        ascendedHornLeft.visible = phase >= 3;
        ascendedHornRight.visible = phase >= 3;
        ascendedHornLeft.zRot = -0.50F - MathHelper.sin(ageInTicks * 0.08F) * 0.05F;
        ascendedHornRight.zRot = -ascendedHornLeft.zRot;
        float idle = MathHelper.sin(ageInTicks * 0.08F);
        float cast = state == 1 ? 1.0F : 0.0F;
        float charge = state == 2 ? MathHelper.clamp(timer / 22.0F, 0.0F, 1.0F) : 0.0F;
        float beam = state == 3 ? 1.0F : 0.0F;
        float pull = state == 4 ? 1.0F : 0.0F;
        float nova = state == 5 ? 1.0F : 0.0F;
        float areaCharge = (pull + nova) > 0.0F
                ? MathHelper.clamp((24.0F - timer) / 23.0F, 0.0F, 1.0F) : 0.0F;
        float dashCharge = state == 6 ? 1.0F : 0.0F;
        float dash = state == 7 ? 1.0F : 0.0F;
        float stagger = state == 8 ? MathHelper.clamp(timer / 50.0F, 0.0F, 1.0F) : 0.0F;
        float phaseShift = state == 9
                ? MathHelper.sin(MathHelper.clamp(1.0F - timer / 54.0F, 0.0F, 1.0F) * (float)Math.PI)
                : 0.0F;
        float death = MathHelper.clamp(entity.deathTime / 20.0F, 0.0F, 1.0F);

        // The awakened forms spread the creature's silhouette instead of relying on a tint.
        eyeShell.y = -1.0F + idle * 0.55F - charge * 0.4F + stagger * 0.38F
                - phaseShift * 0.48F - awakened * 0.22F - ascended * 0.18F;
        eyeShell.xRot = headPitch * ((float)Math.PI / 180.0F) * 0.08F
                - dashCharge * 0.12F + dash * 0.48F - nova * (0.08F + areaCharge * 0.3F)
                + pull * areaCharge * 0.12F + stagger * 0.2F + phaseShift * 0.22F
                + death * 1.05F;
        eyeShell.yRot = netHeadYaw * ((float)Math.PI / 180.0F) * 0.28F
                + MathHelper.sin(ageInTicks * 0.035F) * (0.06F + ascended * 0.1F);
        eyeShell.zRot = MathHelper.sin(ageInTicks * 0.06F) * 0.035F + dash * 0.12F
                + MathHelper.sin(ageInTicks * 0.045F) * (awakened * 0.045F + ascended * 0.07F);

        float open = 1.0F - charge * 0.35F - dashCharge * 0.2F + beam * 0.12F
                + ascended * 0.08F + nova * areaCharge * 0.22F - stagger * 0.48F
                + phaseShift * 0.42F;
        upperLid.y = -1.2F - open * 0.45F;
        lowerLid.y = -1.0F + open * 0.25F;
        iris.yRot = MathHelper.sin(ageInTicks * (0.06F + awakened * 0.035F)) * 0.12F
                + dashCharge * 0.12F + beam * 0.16F;
        pupil.xRot = MathHelper.sin(ageInTicks * 0.12F) * 0.05F - charge * 0.15F;
        pupil.zRot = ageInTicks * (0.025F + ascended * 0.04F);
        glint.y = -2.0F + MathHelper.sin(ageInTicks * 0.16F) * 0.18F;
        jaw.xRot = nova * (0.12F + areaCharge * 0.55F) + beam * 0.22F
                + phaseShift * 0.38F + MathHelper.sin(ageInTicks * 0.1F) * 0.035F
                + death * 0.65F;
        fangLeft.xRot = -0.08F - nova * 0.25F;
        fangRight.xRot = fangLeft.xRot;

        float flap = MathHelper.sin(ageInTicks * (0.18F + awakened * 0.06F + ascended * 0.04F));
        wingLeft.zRot = -0.35F - flap * (0.14F + awakened * 0.12F + ascended * 0.08F)
                - awakened * 0.28F - ascended * 0.34F
                - dashCharge * 0.55F + dash * 0.4F + nova * (0.08F + areaCharge * 0.38F)
                - pull * areaCharge * 0.18F - stagger * 0.38F - phaseShift * 0.62F
                + death * 0.95F;
        wingRight.zRot = 0.35F + flap * (0.14F + awakened * 0.12F + ascended * 0.08F)
                + awakened * 0.28F + ascended * 0.34F
                + dashCharge * 0.55F - dash * 0.4F - nova * (0.08F + areaCharge * 0.38F)
                + pull * areaCharge * 0.18F + stagger * 0.38F + phaseShift * 0.62F
                - death * 0.95F;
        wingLeft.xRot = 0.08F + awakened * 0.12F + ascended * 0.18F
                + beam * 0.12F + dash * 0.2F - phaseShift * 0.25F;
        wingRight.xRot = -wingLeft.xRot;
        wingTipLeft.zRot = -0.28F - awakened * 0.20F - ascended * 0.34F
                - dashCharge * 0.3F + dash * 0.2F;
        wingTipRight.zRot = -wingTipLeft.zRot;
        wingTipLeft.xRot = -awakened * 0.16F - ascended * 0.3F + dash * 0.18F;
        wingTipRight.xRot = -wingTipLeft.xRot;

        sideEyeLeft.xRot = MathHelper.sin(ageInTicks * 0.09F) * 0.08F + cast * 0.16F;
        sideEyeRight.xRot = -sideEyeLeft.xRot;
        sideEyeLeft.yRot = -awakened * 0.30F - ascended * 0.42F
                + MathHelper.sin(ageInTicks * 0.07F) * 0.08F;
        sideEyeRight.yRot = -sideEyeLeft.yRot;
        for (int i = 0; i < tendrils.length; i++) {
            float wave = ageInTicks * (0.075F + awakened * 0.025F + ascended * 0.045F) + i * 1.0F;
            tendrils[i].xRot = 0.35F + MathHelper.sin(wave) * (0.22F + ascended * 0.14F)
                    - charge * 0.45F - dashCharge * 0.25F + dash * 0.75F
                    + nova * (0.12F + areaCharge * 0.55F) - pull * areaCharge * 0.18F
                    + stagger * (0.42F + (i % 2 == 0 ? 0.08F : -0.08F))
                    + phaseShift * (i % 2 == 0 ? -0.5F : 0.5F) + death * 0.85F;
            float fan = i % 2 == 0 ? -1.0F : 1.0F;
            tendrils[i].zRot = MathHelper.cos(wave * 0.8F) * (0.18F + awakened * 0.12F + ascended * 0.1F)
                    + fan * (awakened * 0.25F + ascended * 0.38F + dash * 0.32F + pull * 0.22F);
            tendrils[i].yRot = fan * (awakened * 0.12F + ascended * 0.2F);
        }
        for (int i = 0; i < barbs.length; i++) {
            barbs[i].xRot = MathHelper.sin(ageInTicks * 0.07F + i) * 0.05F
                    + awakened * 0.1F + ascended * 0.24F;
            barbs[i].yRot = ageInTicks * (0.02F + awakened * 0.012F + ascended * 0.025F)
                    + i * 1.57F;
        }
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.matrix.MatrixStack stack,
                               com.mojang.blaze3d.vertex.IVertexBuilder buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        eyeShell.render(stack, buffer, packedLight, packedOverlay,
                red * this.phaseTintRed, green * this.phaseTintGreen,
                blue * this.phaseTintBlue, alpha);
    }
}
