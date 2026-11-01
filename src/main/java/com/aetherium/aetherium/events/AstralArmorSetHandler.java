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

/** Endgame set bonus, checked only every 40 server ticks. */
@Mod.EventBusSubscriber(modid = AetheriumMod.MOD_ID)
public final class AstralArmorSetHandler {
    private AstralArmorSetHandler() { }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide) return;
        PlayerEntity player = event.player;
        if (player.tickCount % 40 != 0 || !hasFullAstralSet(player)) return;
        player.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, 65, 1, true, false));
        player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 65, 0, true, false));
        player.addEffect(new EffectInstance(Effects.FIRE_RESISTANCE, 65, 0, true, false));
        player.addEffect(new EffectInstance(Effects.NIGHT_VISION, 260, 0, true, false));
    }

    private static boolean hasFullAstralSet(PlayerEntity player) {
        return player.getItemBySlot(EquipmentSlotType.HEAD).getItem() == RegistryHandler.ASTRAL_HELMET.get()
                && player.getItemBySlot(EquipmentSlotType.CHEST).getItem() == RegistryHandler.ASTRAL_CHESTPLATE.get()
                && player.getItemBySlot(EquipmentSlotType.LEGS).getItem() == RegistryHandler.ASTRAL_LEGGINGS.get()
                && player.getItemBySlot(EquipmentSlotType.FEET).getItem() == RegistryHandler.ASTRAL_BOOTS.get();
    }
}
