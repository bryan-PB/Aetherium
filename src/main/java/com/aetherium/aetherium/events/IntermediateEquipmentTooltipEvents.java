package com.aetherium.aetherium.events;

import com.aetherium.aetherium.AetheriumMod;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Small localized tooltips appear only when hovering the new equipment. */
@Mod.EventBusSubscriber(modid = AetheriumMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class IntermediateEquipmentTooltipEvents {
    private IntermediateEquipmentTooltipEvents() {}

    @SubscribeEvent
    public static void appendTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().isEmpty()) return;
        Item item = event.getItemStack().getItem();
        ResourceLocation name = item.getRegistryName();
        if (name == null || !AetheriumMod.MOD_ID.equals(name.getNamespace())) return;
        String family = name.getPath().startsWith("brumacero_") ? "brumacero"
                : name.getPath().startsWith("umbracita_") ? "umbracita" : null;
        if (family == null) return;

        if (item instanceof ArmorItem) {
            event.getToolTip().add(new TranslationTextComponent(
                    "tooltip.aetherium." + family + "_set").withStyle(TextFormatting.GRAY));
        } else if (item instanceof SwordItem || item instanceof PickaxeItem
                || item instanceof AxeItem || item instanceof ShovelItem
                || item instanceof HoeItem) {
            event.getToolTip().add(new TranslationTextComponent(
                    "tooltip.aetherium." + family + "_tool").withStyle(TextFormatting.GRAY));
        }
    }
}
