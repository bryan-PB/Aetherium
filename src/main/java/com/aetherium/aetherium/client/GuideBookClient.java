package com.aetherium.aetherium.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Opens a lightweight, purpose-built codex GUI only on player interaction. */
@OnlyIn(Dist.CLIENT)
public final class GuideBookClient {
    private GuideBookClient() {}

    public static void open() {
        Minecraft.getInstance().setScreen(new GuideBookScreen());
    }
}
