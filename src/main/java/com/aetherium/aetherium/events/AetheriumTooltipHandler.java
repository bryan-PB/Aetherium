package com.aetherium.aetherium.events;

import com.aetherium.aetherium.AetheriumMod;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Adds localized, one-line ability and set descriptions to Aetherium items. */
@Mod.EventBusSubscriber(modid = AetheriumMod.MOD_ID)
public final class AetheriumTooltipHandler {
    private AetheriumTooltipHandler() { }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ResourceLocation id = event.getItemStack().getItem().getRegistryName();
        if (id == null || !AetheriumMod.MOD_ID.equals(id.getNamespace())) return;
        String key = "tooltip.aetherium.item." + id.getPath();
        event.getToolTip().add(new TranslationTextComponent(key).withStyle(TextFormatting.GRAY));
        // Sword mechanics need two short, readable lines rather than one very
        // wide tooltip that can extend beyond Minecraft's inventory screen.
        if ("aetherium_sword".equals(id.getPath()) || "astral_sword".equals(id.getPath())) {
            event.getToolTip().add(new TranslationTextComponent(key + ".detail")
                    .withStyle(TextFormatting.DARK_GRAY));
        }
    }
}
