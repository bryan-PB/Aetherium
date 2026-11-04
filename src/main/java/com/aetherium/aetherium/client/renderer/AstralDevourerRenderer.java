package com.aetherium.aetherium.client.renderer;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.client.model.AstralDevourerModel;
import com.aetherium.aetherium.content.boss.AstralDevourerEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

/** Renders a real two-layer, world-space laser instead of a dotted particle approximation. */
public class AstralDevourerRenderer extends MobRenderer<AstralDevourerEntity, AstralDevourerModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(AetheriumMod.MOD_ID,
            "textures/entity/boss/astral_devourer.png");

    public AstralDevourerRenderer(EntityRendererManager manager) {
        super(manager, new AstralDevourerModel(), 1.02F);
    }

    @Override
    public ResourceLocation getTextureLocation(AstralDevourerEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(AstralDevourerEntity entity, float yaw, float partialTicks,
                       MatrixStack stack, IRenderTypeBuffer buffers, int packedLight) {
        super.render(entity, yaw, partialTicks, stack, buffers, packedLight);
        int state = entity.getAttackState();
        if (state != 2 && state != 3) return;

        double originY = entity.getBbHeight() * 0.58D;
        Vector3d target = entity.getBeamRenderTarget();
        Vector3d axis = target.subtract(entity.getX(), entity.getY() + originY, entity.getZ());
        double length = axis.length();
        if (length < 0.1D || length > 40.0D) return;

        // Stable perpendicular axes keep the beam visible from above, below and the sides.
        Vector3d forward = axis.scale(1.0D / length);
        Vector3d side = forward.cross(new Vector3d(0.0D, 1.0D, 0.0D));
        if (side.lengthSqr() < 0.0001D) side = new Vector3d(1.0D, 0.0D, 0.0D);
        side = side.normalize();
        Vector3d up = forward.cross(side).normalize();
        boolean active = state == 3;
        float flicker = 0.94F + 0.06F * (float)Math.sin((entity.tickCount + partialTicks) * 1.7D);
        stack.pushPose();
        stack.translate(0.0D, originY, 0.0D);
        Matrix4f matrix = stack.last().pose();
        IVertexBuilder builder = buffers.getBuffer(RenderType.lightning());

        // Two crossed camera-independent ribbons give the laser actual screen width.
        float halo = active ? 0.45F * flicker : 0.13F;
        ribbon(builder, matrix, axis, side, halo, active ? 164 : 128,
                active ? 44 : 70, active ? 222 : 198, active ? 220 : 115);
        ribbon(builder, matrix, axis, up, halo, active ? 164 : 128,
                active ? 44 : 70, active ? 222 : 198, active ? 220 : 115);
        float core = active ? 0.20F * flicker : 0.045F;
        ribbon(builder, matrix, axis, side, core, active ? 255 : 215,
                active ? 194 : 130, active ? 240 : 245, 255);
        ribbon(builder, matrix, axis, up, core, active ? 255 : 215,
                active ? 194 : 130, active ? 240 : 245, 255);
        stack.popPose();
    }

    /** A double-sided quad, colored using the lightning render type (POSITION_COLOR). */
    private static void ribbon(IVertexBuilder builder, Matrix4f matrix, Vector3d end,
                               Vector3d normal, float width, int r, int g, int b, int a) {
        float x = (float)(normal.x * width);
        float y = (float)(normal.y * width);
        float z = (float)(normal.z * width);
        float ex = (float)end.x;
        float ey = (float)end.y;
        float ez = (float)end.z;
        builder.vertex(matrix, -x, -y, -z).color(r, g, b, a).endVertex();
        builder.vertex(matrix,  x,  y,  z).color(r, g, b, a).endVertex();
        builder.vertex(matrix, ex + x, ey + y, ez + z).color(r, g, b, a).endVertex();
        builder.vertex(matrix, ex - x, ey - y, ez - z).color(r, g, b, a).endVertex();
        builder.vertex(matrix, ex - x, ey - y, ez - z).color(r, g, b, a).endVertex();
        builder.vertex(matrix, ex + x, ey + y, ez + z).color(r, g, b, a).endVertex();
        builder.vertex(matrix,  x,  y,  z).color(r, g, b, a).endVertex();
        builder.vertex(matrix, -x, -y, -z).color(r, g, b, a).endVertex();
    }
}
