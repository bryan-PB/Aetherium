package com.aetherium.aetherium.client.renderer;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.client.model.AstralDevourerModel;
import com.aetherium.aetherium.content.boss.AstralDevourerEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class AstralDevourerRenderer extends MobRenderer<AstralDevourerEntity, AstralDevourerModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(AetheriumMod.MOD_ID,
            "textures/entity/boss/astral_devourer.png");

    public AstralDevourerRenderer(EntityRendererManager manager) {
        super(manager, new AstralDevourerModel(), 0.82F);
    }

    @Override
    public ResourceLocation getTextureLocation(AstralDevourerEntity entity) {
        return TEXTURE;
    }
}
