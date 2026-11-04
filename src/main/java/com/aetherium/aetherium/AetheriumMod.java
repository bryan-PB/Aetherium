package com.aetherium.aetherium;

import com.aetherium.aetherium.config.AetheriumConfig;
import com.aetherium.aetherium.content.item.AetheriumCuriosIntegration;
import com.aetherium.aetherium.registry.RegistryHandler;
import com.aetherium.aetherium.world.AetheriumWorldGen;
import com.aetherium.aetherium.events.AetheriumEntityEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(AetheriumMod.MOD_ID)
public class AetheriumMod {
    public static final String MOD_ID = "aetherium";
    public static final Logger LOGGER = LogManager.getLogger();

    public AetheriumMod() {
        RegistryHandler.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AetheriumConfig.COMMON_SPEC,
                "aetherium-common.toml");

        FMLJavaModLoadingContext.get().getModEventBus().addListener(AetheriumWorldGen::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(AetheriumEntityEvents::onAttributeCreate);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(AetheriumEntityEvents::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(AetheriumCuriosIntegration::onInterModEnqueue);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, AetheriumWorldGen::onBiomeLoading);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, AetheriumEntityEvents::onBiomeLoading);
        LOGGER.info("Kryzorae is loading (legacy mod id: aetherium).");
    }
}
