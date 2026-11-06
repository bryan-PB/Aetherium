package com.aetherium.aetherium.events;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Give the guide once per player; never run an inventory scan every tick. */
@Mod.EventBusSubscriber(modid = AetheriumMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GuideBookEvents {
    private static final String STARTER_FLAG = "KryzoraeGuideReceived";

    private GuideBookEvents() {}

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        CompoundNBT persistent = player.getPersistentData()
                .getCompound(PlayerEntity.PERSISTED_NBT_TAG);
        if (persistent.getBoolean(STARTER_FLAG)) return;

        // Record the delivery BEFORE adding the item, preventing duplicates on relog.
        persistent.putBoolean(STARTER_FLAG, true);
        player.getPersistentData().put(PlayerEntity.PERSISTED_NBT_TAG, persistent);
        ItemStack guide = new ItemStack(RegistryHandler.GUIDE_BOOK.get());
        if (!player.inventory.add(guide)) player.drop(guide, false);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundNBT original = event.getOriginal().getPersistentData()
                .getCompound(PlayerEntity.PERSISTED_NBT_TAG);
        CompoundNBT existing = event.getPlayer().getPersistentData()
                .getCompound(PlayerEntity.PERSISTED_NBT_TAG);
        existing.merge(original.copy());
        event.getPlayer().getPersistentData()
                .put(PlayerEntity.PERSISTED_NBT_TAG, existing);
    }
}
