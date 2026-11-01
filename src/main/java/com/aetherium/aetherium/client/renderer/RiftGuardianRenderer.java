package com.aetherium.aetherium.client.renderer;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.client.model.RiftGuardianModel;
import com.aetherium.aetherium.content.boss.RiftGuardianEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RiftGuardianRenderer extends MobRenderer<RiftGuardianEntity, RiftGuardianModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(AetheriumMod.MOD_ID,
            "textures/entity/boss/rift_guardian.png");

    public RiftGuardianRenderer(EntityRendererManager manager) {
        super(manager, new RiftGuardianModel(), 0.78F);
    }

    @Override
    public ResourceLocation getTextureLocation(RiftGuardianEntity entity) {
        return TEXTURE;
    }
}
