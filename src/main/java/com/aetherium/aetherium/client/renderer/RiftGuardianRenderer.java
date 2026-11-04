package com.aetherium.aetherium.client.renderer;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.client.model.RiftGuardianModel;
import com.aetherium.aetherium.content.boss.RiftGuardianEntity;
import com.aetherium.aetherium.content.boss.BossMirageEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RiftGuardianRenderer extends MobRenderer<RiftGuardianEntity, RiftGuardianModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(AetheriumMod.MOD_ID,
            "textures/entity/boss/rift_guardian.png");

    public RiftGuardianRenderer(EntityRendererManager manager) {
        super(manager, new RiftGuardianModel(), 0.92F);
    }

    @Override
    public ResourceLocation getTextureLocation(RiftGuardianEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(RiftGuardianEntity entity, float yaw, float partialTicks,
                       MatrixStack stack, IRenderTypeBuffer buffers, int packedLight) {
        super.render(entity, yaw, partialTicks, stack, buffers, packedLight);
        int mirageId = entity.getMirageEntityId();
        if (mirageId < 0) return;
        Entity copy = entity.level.getEntity(mirageId);
        if (!(copy instanceof BossMirageEntity)) return;
        stack.pushPose();
        stack.translate(copy.getX() - entity.getX(), copy.getY() - entity.getY(),
                copy.getZ() - entity.getZ());
        super.render(entity, yaw, partialTicks, stack, buffers, packedLight);
        stack.popPose();
    }
}
