package com.aetherium.aetherium.client.renderer;

import com.aetherium.aetherium.content.boss.AetherBoltEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Real world-space projectile geometry, never a flying crystal item sprite.
 * Winged Executioner fires serrated black feathers with a red metal vein;
 * Guardian fires broad, luminous pressure crescents from its wind cannon.
 */
public final class AetherBoltRenderer extends EntityRenderer<AetherBoltEntity> {
    private static final ResourceLocation NO_TEXTURE =
            new ResourceLocation("minecraft", "textures/particle/particles.png");

    public AetherBoltRenderer(EntityRendererManager manager) {
        super(manager);
        this.shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(AetherBoltEntity projectile) {
        return NO_TEXTURE;
    }

    @Override
    public void render(AetherBoltEntity projectile, float yaw, float partialTicks,
                       MatrixStack stack, IRenderTypeBuffer buffers, int light) {
        stack.pushPose();
        Vector3d motion = projectile.getDeltaMovement();
        if (motion.lengthSqr() > 0.0001D) {
            float horizontal = (float)Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            float heading = (float)(Math.atan2(motion.z, motion.x)
                    * 180.0D / Math.PI - 90.0D);
            float pitch = (float)(-Math.atan2(motion.y, horizontal)
                    * 180.0D / Math.PI);
            stack.mulPose(Vector3f.YP.rotationDegrees(-heading));
            stack.mulPose(Vector3f.XP.rotationDegrees(pitch));
        } else {
            stack.mulPose(Vector3f.YP.rotationDegrees(-yaw));
        }
        boolean wind = projectile.getStyle() == AetherBoltEntity.WIND_SLICE;
        if (!wind) {
            // Each metal feather rotates slowly about its forward axis.
            stack.mulPose(Vector3f.ZP.rotationDegrees(
                    (projectile.tickCount + partialTicks) * 16.0F));
        }
        IVertexBuilder out = buffers.getBuffer(RenderType.lightning());
        for (int plane = 0; plane < 2; plane++) {
            stack.pushPose();
            if (plane == 1) stack.mulPose(Vector3f.YP.rotationDegrees(78.0F));
            Matrix4f m = stack.last().pose();
            if (wind) {
                drawWindCrescent(out, m);
            } else {
                drawIronFeather(out, m);
            }
            stack.popPose();
        }
        stack.popPose();
        super.render(projectile, yaw, partialTicks, stack, buffers, light);
    }

    private static void drawIronFeather(IVertexBuilder out, Matrix4f m) {
        // Black/iron outer feathers, not purple magical or crystal geometry.
        quad(out, m, -0.13F, 0.56F, 0.23F, 0.19F,
                0.08F, -0.68F, -0.25F, -0.08F,
                29, 26, 29, 245);
        quad(out, m, -0.08F, 0.45F, 0.12F, 0.15F,
                0.025F, -0.52F, -0.12F, -0.07F,
                126, 37, 46, 245);
        // Narrow metallic cutting ridge and orange-red stress line.
        quad(out, m, -0.02F, 0.52F, 0.06F, 0.22F,
                0.025F, -0.57F, -0.02F, -0.26F,
                223, 185, 157, 245);
        quad(out, m, -0.02F, 0.25F, 0.02F, 0.18F,
                0.00F, -0.35F, -0.015F, -0.27F,
                239, 76, 73, 255);
    }

    private static void drawWindCrescent(IVertexBuilder out, Matrix4f m) {
        // A concave arc of compressed air; grey/white pressure edges with
        // faint teal inside, reminiscent of a short cannon muzzle wave.
        final float[] x = {-0.84F, -0.62F, -0.28F, 0.12F, 0.48F, 0.83F};
        final float[] y = {-0.36F, -0.01F, 0.22F, 0.30F, 0.16F, -0.19F};
        for (int i = 0; i < x.length - 1; i++) {
            float dx = x[i + 1] - x[i], dy = y[i + 1] - y[i];
            float len = (float)Math.sqrt(dx * dx + dy * dy);
            float nx = -dy / len, ny = dx / len;
            float taper0 = (i == 0 ? 0.30F : 1.0F);
            float taper1 = (i == x.length - 2 ? 0.30F : 1.0F);
            quad(out, m,
                    x[i] + nx * 0.19F * taper0, y[i] + ny * 0.19F * taper0,
                    x[i] - nx * 0.19F * taper0, y[i] - ny * 0.19F * taper0,
                    x[i + 1] - nx * 0.19F * taper1, y[i + 1] - ny * 0.19F * taper1,
                    x[i + 1] + nx * 0.19F * taper1, y[i + 1] + ny * 0.19F * taper1,
                    98, 145, 155, 190);
            quad(out, m,
                    x[i] + nx * 0.075F * taper0, y[i] + ny * 0.075F * taper0,
                    x[i] - nx * 0.075F * taper0, y[i] - ny * 0.075F * taper0,
                    x[i + 1] - nx * 0.075F * taper1, y[i + 1] - ny * 0.075F * taper1,
                    x[i + 1] + nx * 0.075F * taper1, y[i + 1] + ny * 0.075F * taper1,
                    233, 247, 238, 250);
        }
    }

    private static void quad(IVertexBuilder out, Matrix4f matrix,
                             float x0, float y0, float x1, float y1,
                             float x2, float y2, float x3, float y3,
                             int r, int g, int b, int a) {
        out.vertex(matrix, x0, y0, 0.0F).color(r, g, b, a).endVertex();
        out.vertex(matrix, x1, y1, 0.0F).color(r, g, b, a).endVertex();
        out.vertex(matrix, x2, y2, 0.0F).color(r, g, b, a).endVertex();
        out.vertex(matrix, x3, y3, 0.0F).color(r, g, b, a).endVertex();
        out.vertex(matrix, x3, y3, 0.0F).color(r, g, b, a).endVertex();
        out.vertex(matrix, x2, y2, 0.0F).color(r, g, b, a).endVertex();
        out.vertex(matrix, x1, y1, 0.0F).color(r, g, b, a).endVertex();
        out.vertex(matrix, x0, y0, 0.0F).color(r, g, b, a).endVertex();
    }
}
