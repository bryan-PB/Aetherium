package com.aetherium.aetherium.events;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Low-cost full-set bonus: checks only once every 40 ticks on the logical server. */
@Mod.EventBusSubscriber(modid = AetheriumMod.MOD_ID)
public final class AetheriumArmorSetHandler {
    private AetheriumArmorSetHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide) {
            return;
        }

        PlayerEntity player = event.player;
        if (player.tickCount % 40 != 0 || !hasFullAetheriumSet(player)) {
            return;
        }

        player.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, 65, 0, true, false));
        player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 65, 0, true, false));
        player.addEffect(new EffectInstance(Effects.NIGHT_VISION, 260, 0, true, false));
    }

    private static boolean hasFullAetheriumSet(PlayerEntity player) {
        return player.getItemBySlot(EquipmentSlotType.HEAD).getItem() == RegistryHandler.AETHERIUM_HELMET.get()
                && player.getItemBySlot(EquipmentSlotType.CHEST).getItem() == RegistryHandler.AETHERIUM_CHESTPLATE.get()
                && player.getItemBySlot(EquipmentSlotType.LEGS).getItem() == RegistryHandler.AETHERIUM_LEGGINGS.get()
                && player.getItemBySlot(EquipmentSlotType.FEET).getItem() == RegistryHandler.AETHERIUM_BOOTS.get();
    }
}
