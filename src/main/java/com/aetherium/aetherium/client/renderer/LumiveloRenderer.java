package com.aetherium.aetherium.client.renderer;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.client.model.LumiveloModel;
import com.aetherium.aetherium.content.entity.LumiveloEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class LumiveloRenderer extends MobRenderer<LumiveloEntity, LumiveloModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(AetheriumMod.MOD_ID,
            "textures/entity/lumivelo.png");

    public LumiveloRenderer(EntityRendererManager manager) {
        super(manager, new LumiveloModel(), 0.32F);
    }

    @Override
    public ResourceLocation getTextureLocation(LumiveloEntity entity) {
        return TEXTURE;
    }
}
