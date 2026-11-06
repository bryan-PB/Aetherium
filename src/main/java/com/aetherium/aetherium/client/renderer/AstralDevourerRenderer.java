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

/** Winged Executioner: articulated model, chest-eye laser and independently hitable mirage. */
public class AstralDevourerRenderer extends MobRenderer<AstralDevourerEntity, AstralDevourerModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(AetheriumMod.MOD_ID,
            "textures/entity/boss/winged_executioner.png");

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
        // Decoys are rendered independently by BossMirageRenderer. This boss
        // renderer now draws ONLY the actual boss and its real chest beam.
        int state = entity.getAttackState();
        if (state != 2 && state != 3) return;

        // This origin is identical to the server's collision/particle origin.
        // Its small forward offset emerges visibly from the physical chest eye.
        Vector3d muzzle = entity.getBeamOrigin();
        Vector3d target = entity.getBeamRenderTarget();
        Vector3d axis = target.subtract(muzzle);
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
        // The telegraph thickens visibly as the firing time approaches.
        float warningProgress = Math.max(0.0F, Math.min(1.0F,
                1.0F - entity.getAttackAnimationTicks() / 26.0F));
        stack.pushPose();
        stack.translate(muzzle.x - entity.getX(),
                muzzle.y - entity.getY(), muzzle.z - entity.getZ());
        Matrix4f matrix = stack.last().pose();
        IVertexBuilder builder = buffers.getBuffer(RenderType.lightning());
        float halo = active ? 0.46F * flicker : 0.11F + warningProgress * 0.11F;
        float core = active ? 0.22F * flicker : 0.035F + warningProgress * 0.06F;

        // Four crossed planes form a readable, bright eight-sided energy column,
        // including when viewed from directly above or along the ground.
        for (int i = 0; i < 4; i++) {
            double angle = Math.PI * i / 4.0D;
            Vector3d normal = side.scale(Math.cos(angle)).add(up.scale(Math.sin(angle)));
            ribbon(builder, matrix, axis, normal, halo,
                    active ? 230 : 178, active ? 121 : 109,
                    active ? 60 : 64, active ? 225 : 115);
            ribbon(builder, matrix, axis, normal, core,
                    active ? 255 : 238, active ? 226 : 172,
                    active ? 154 : 99, 255);
        }
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
