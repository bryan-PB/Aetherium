package com.aetherium.aetherium.client.renderer;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.content.boss.BossMirageEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

/** Invisible picking hitbox; its complete boss-shaped image is rendered by the owner. */
public class BossMirageRenderer extends EntityRenderer<BossMirageEntity> {
    private static final ResourceLocation PLACEHOLDER =
            new ResourceLocation(AetheriumMod.MOD_ID, "textures/entity/boss/rift_guardian.png");

    public BossMirageRenderer(EntityRendererManager manager) {
        super(manager);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(BossMirageEntity entity, float yaw, float partialTicks,
                       MatrixStack stack, IRenderTypeBuffer buffer, int packedLight) {
        // Geometry is rendered from the owning boss's renderer so both silhouettes match.
    }

    @Override
    public ResourceLocation getTextureLocation(BossMirageEntity entity) {
        return PLACEHOLDER;
    }
}
