package com.aetherium.aetherium.client.renderer;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.client.model.AstralDevourerModel;
import com.aetherium.aetherium.client.model.RiftGuardianModel;
import com.aetherium.aetherium.content.boss.AstralDevourerEntity;
import com.aetherium.aetherium.content.boss.BossMirageEntity;
import com.aetherium.aetherium.content.boss.RiftGuardianEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

/**
 * Independent translucent render pass for each synced mirage entity.
 *
 * Rendering fakes from the real boss's renderer made them disappear when its
 * bounding box was culled and forced them to look fully solid. Their own
 * renderer keeps all three copies visible and correctly interpolated.
 */
public class BossMirageRenderer extends EntityRenderer<BossMirageEntity> {
    private static final ResourceLocation WINGED_TEXTURE = new ResourceLocation(
            AetheriumMod.MOD_ID, "textures/entity/boss/winged_executioner.png");
    private static final ResourceLocation GUARDIAN_TEXTURE = new ResourceLocation(
            AetheriumMod.MOD_ID, "textures/entity/boss/rift_guardian.png");

    private final AstralDevourerModel wingedModel = new AstralDevourerModel();
    private final RiftGuardianModel guardianModel = new RiftGuardianModel();

    public BossMirageRenderer(EntityRendererManager manager) {
        super(manager);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(BossMirageEntity fake, float yaw, float partialTicks,
                       MatrixStack stack, IRenderTypeBuffer buffers, int packedLight) {
        net.minecraft.entity.Entity owner = fake.getBoss();
        if (owner == null || !owner.isAlive()) return;

        float alpha;
        if (owner instanceof AstralDevourerEntity) {
            AstralDevourerEntity boss = (AstralDevourerEntity)owner;
            float lostHealth = 1.0F - boss.getHealth() / Math.max(1.0F, boss.getMaxHealth());
            // Barely visible at first; becomes convincingly opaque at low HP.
            alpha = Math.min(0.86F, 0.16F + lostHealth * 0.73F);
            alpha *= Math.min(1.0F, fake.tickCount / 10.0F);
            alpha *= Math.min(1.0F, (170 - fake.tickCount) / 12.0F);
        } else if (owner instanceof RiftGuardianEntity) {
            alpha = 0.50F;
            alpha *= Math.min(1.0F, fake.tickCount / 8.0F);
        } else return;
        alpha = Math.max(0.0F, alpha);
        if (alpha < 0.005F) return;

        stack.pushPose();
        stack.mulPose(Vector3f.YP.rotationDegrees(180.0F - yaw));
        // Equivalent to LivingRenderer's biped/model transform in 1.16.5.
        stack.scale(-1.0F, -1.0F, 1.0F);
        stack.translate(0.0D, -1.5D, 0.0D);

        if (owner instanceof AstralDevourerEntity) {
            AstralDevourerEntity boss = (AstralDevourerEntity)owner;
            wingedModel.setupAnim(boss, 0.0F, 0.0F,
                    boss.tickCount + partialTicks, 0.0F, 0.0F);
            IVertexBuilder out = buffers.getBuffer(RenderType.entityTranslucent(WINGED_TEXTURE));
            wingedModel.renderToBuffer(stack, out, packedLight,
                    OverlayTexture.NO_OVERLAY, 0.89F, 0.79F, 0.84F, alpha);
        } else {
            RiftGuardianEntity boss = (RiftGuardianEntity)owner;
            guardianModel.setupAnim(boss, 0.0F, 0.0F,
                    boss.tickCount + partialTicks, 0.0F, 0.0F);
            IVertexBuilder out = buffers.getBuffer(RenderType.entityTranslucent(GUARDIAN_TEXTURE));
            guardianModel.renderToBuffer(stack, out, packedLight,
                    OverlayTexture.NO_OVERLAY, 0.82F, 0.88F, 0.98F, alpha);
        }

        stack.popPose();
        super.render(fake, yaw, partialTicks, stack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(BossMirageEntity fake) {
        return fake.getBoss() instanceof AstralDevourerEntity
                ? WINGED_TEXTURE : GUARDIAN_TEXTURE;
    }
}
