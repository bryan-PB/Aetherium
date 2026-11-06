package com.aetherium.aetherium.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ReadBookScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Uses Minecraft's built-in book interface. Generated only when opened:
 * zero ticking, no extra packets and no external UI dependency.
 */
@OnlyIn(Dist.CLIENT)
public final class GuideBookClient {
    private GuideBookClient() {}

    public static void open() {
        ItemStack virtualBook = new ItemStack(Items.WRITTEN_BOOK);
        CompoundNBT tag = virtualBook.getOrCreateTag();
        tag.putString("title", "Kryzorae");
        tag.putString("author", "Kryzorae");
        tag.putBoolean("resolved", true);
        ListNBT pages = new ListNBT();
        for (int page = 1; page <= 8; page++) {
            ITextComponent text = new TranslationTextComponent("guide.aetherium.page." + page);
            pages.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(text)));
        }
        tag.put("pages", pages);
        Minecraft.getInstance().setScreen(
                new ReadBookScreen(new ReadBookScreen.WrittenBookInfo(virtualBook)));
    }
}
