package com.aetherium.aetherium.events;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/** Balanced set bonuses without continuous particle effects or entity searches. */
@Mod.EventBusSubscriber(modid = AetheriumMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class IntermediateSetEvents {
    private static final UUID UMBRACITA_SPEED_ID =
            UUID.fromString("d2d9481c-7e57-4940-9202-bebd5d4968aa");
    private static final AttributeModifier UMBRACITA_SPEED =
            new AttributeModifier(UMBRACITA_SPEED_ID, "Kryzorae Umbracita full set speed",
                    0.10D, AttributeModifier.Operation.MULTIPLY_TOTAL);

    private IntermediateSetEvents() {}

    private static boolean isSet(PlayerEntity player, boolean umbracita) {
        return player.getItemBySlot(EquipmentSlotType.HEAD).getItem() ==
                (umbracita ? RegistryHandler.UMBRACITA_HELMET.get() : RegistryHandler.BRUMACERO_HELMET.get())
                && player.getItemBySlot(EquipmentSlotType.CHEST).getItem() ==
                (umbracita ? RegistryHandler.UMBRACITA_CHESTPLATE.get() : RegistryHandler.BRUMACERO_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlotType.LEGS).getItem() ==
                (umbracita ? RegistryHandler.UMBRACITA_LEGGINGS.get() : RegistryHandler.BRUMACERO_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlotType.FEET).getItem() ==
                (umbracita ? RegistryHandler.UMBRACITA_BOOTS.get() : RegistryHandler.BRUMACERO_BOOTS.get());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide
                || event.player.tickCount % 20 != 0) return;
        AttributeInstance speed = event.player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        boolean equipped = isSet(event.player, true);
        boolean applied = speed.getModifier(UMBRACITA_SPEED_ID) != null;
        if (equipped && !applied) speed.addTransientModifier(UMBRACITA_SPEED);
        else if (!equipped && applied) speed.removeModifier(UMBRACITA_SPEED_ID);
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity)
                || event.getEntityLiving().level.isClientSide
                || !(event.getSource().getEntity() instanceof MonsterEntity)) return;
        if (isSet((PlayerEntity) event.getEntityLiving(), false)) {
            event.setAmount(event.getAmount() * 0.90F);
        }
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity)
                || event.getEntityLiving().level.isClientSide) return;
        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        if (!isSet(player, true)) return;
        Vector3d movement = player.getDeltaMovement();
        player.setDeltaMovement(movement.x, movement.y * 1.05D, movement.z);
        player.hasImpulse = true;
    }
}
