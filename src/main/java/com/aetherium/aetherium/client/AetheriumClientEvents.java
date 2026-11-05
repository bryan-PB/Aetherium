package com.aetherium.aetherium.client;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.client.renderer.LumiveloRenderer;
import com.aetherium.aetherium.client.renderer.RiftStalkerRenderer;
import com.aetherium.aetherium.client.renderer.RiftGuardianRenderer;
import com.aetherium.aetherium.client.renderer.AstralDevourerRenderer;
import com.aetherium.aetherium.client.renderer.BossMirageRenderer;
import com.aetherium.aetherium.client.renderer.DarkSlashRenderer;
import com.aetherium.aetherium.client.renderer.AetherBoltRenderer;
import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = AetheriumMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AetheriumClientEvents {
    private AetheriumClientEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            RenderingRegistry.registerEntityRenderingHandler(RegistryHandler.RIFT_STALKER.get(), RiftStalkerRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(RegistryHandler.LUMIVELO.get(), LumiveloRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(RegistryHandler.RIFT_GUARDIAN.get(), RiftGuardianRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(RegistryHandler.ASTRAL_DEVOURER.get(), AstralDevourerRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(RegistryHandler.BOSS_MIRAGE.get(), BossMirageRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(RegistryHandler.DARK_SLASH.get(), DarkSlashRenderer::new);
            // Each boss now has its own volumetric blade/air-crescent attack mesh.
            RenderingRegistry.registerEntityRenderingHandler(RegistryHandler.AETHER_BOLT.get(),
                    AetherBoltRenderer::new);
        });
    }
}
