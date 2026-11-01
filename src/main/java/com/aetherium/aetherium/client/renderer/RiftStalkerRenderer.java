package com.aetherium.aetherium.client.renderer;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.client.model.RiftStalkerModel;
import com.aetherium.aetherium.content.entity.RiftStalkerEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RiftStalkerRenderer extends MobRenderer<RiftStalkerEntity, RiftStalkerModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(AetheriumMod.MOD_ID,
            "textures/entity/rift_stalker.png");

    public RiftStalkerRenderer(EntityRendererManager manager) {
        super(manager, new RiftStalkerModel(), 0.42F);
    }

    @Override
    public ResourceLocation getTextureLocation(RiftStalkerEntity entity) {
        return TEXTURE;
    }
}
