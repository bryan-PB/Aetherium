package com.aetherium.aetherium.client.renderer;

import com.aetherium.aetherium.content.projectile.DarkSlashEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

/**
 * Real mesh, not an invisible projectile with a few optional particles.
 * Two crossing, double-sided diagonal crescents give the cut depth and a
 * distinct silhouette against both light and dark Minecraft backgrounds.
 */
public final class DarkSlashRenderer extends EntityRenderer<DarkSlashEntity> {
    private static final ResourceLocation UNUSED_TEXTURE =
            new ResourceLocation("minecraft", "textures/particle/particles.png");

    public DarkSlashRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public ResourceLocation getTextureLocation(DarkSlashEntity entity) {
        return UNUSED_TEXTURE;
    }

    @Override
    public void render(DarkSlashEntity entity, float yaw, float partialTicks,
                       MatrixStack stack, IRenderTypeBuffer buffers, int light) {
        stack.pushPose();
        // The slash plane stays perpendicular to forward motion.
        stack.mulPose(Vector3f.YP.rotationDegrees(180.0F - entity.yRot));
        stack.mulPose(Vector3f.XP.rotationDegrees(entity.xRot));
        float shrink = Math.min(1.0F, (9.0F - entity.tickCount - partialTicks) / 3.0F);
        stack.scale(shrink, shrink, shrink);
        IVertexBuilder vertices = buffers.getBuffer(RenderType.lightning());
        for (int plane = 0; plane < 2; plane++) {
            stack.pushPose();
            if (plane == 1) stack.mulPose(Vector3f.YP.rotationDegrees(86.0F));
            Matrix4f matrix = stack.last().pose();
            drawCrescent(vertices, matrix, 0.21F, 50, 15, 91, 210);
            drawCrescent(vertices, matrix, 0.10F, 175, 77, 248, 245);
            drawCrescent(vertices, matrix, 0.038F, 238, 188, 255, 255);
            stack.popPose();
        }
        stack.popPose();
        super.render(entity, yaw, partialTicks, stack, buffers, light);
    }

    private static void drawCrescent(IVertexBuilder builder, Matrix4f pose,
                                     float width, int red, int green, int blue, int alpha) {
        // Six diagonal segments form a hooked sweep, close to the violet
        // reference blade. Width tapers to clear pointed endpoints.
        final float[] xs = {-1.02F, -0.81F, -0.52F, -0.17F, 0.20F, 0.54F, 0.89F};
        final float[] ys = {-0.87F, -0.40F, -0.01F, 0.34F, 0.59F, 0.78F, 0.81F};
        for (int i = 0; i < xs.length - 1; i++) {
            float w0 = width * (0.4F + (float)Math.sin(Math.PI * i / 6.0D));
            float w1 = width * (0.4F + (float)Math.sin(Math.PI * (i + 1) / 6.0D));
            float dx = xs[i + 1] - xs[i], dy = ys[i + 1] - ys[i];
            float len = (float)Math.sqrt(dx * dx + dy * dy);
            float nx = -dy / len, ny = dx / len;
            quad(builder, pose,
                    xs[i] + nx * w0, ys[i] + ny * w0,
                    xs[i] - nx * w0, ys[i] - ny * w0,
                    xs[i + 1] - nx * w1, ys[i + 1] - ny * w1,
                    xs[i + 1] + nx * w1, ys[i + 1] + ny * w1,
                    red, green, blue, alpha);
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
