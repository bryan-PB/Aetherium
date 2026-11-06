package com.aetherium.aetherium.events;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/** Clearly perceptible early-game set bonuses without particles or entity searches. */
@Mod.EventBusSubscriber(modid = AetheriumMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class IntermediateSetEvents {
    private static final UUID BRUMACERO_STABILITY_ID =
            UUID.fromString("4f4d499e-85c0-43af-929e-a71243e8d2c4");
    private static final AttributeModifier BRUMACERO_STABILITY =
            new AttributeModifier(BRUMACERO_STABILITY_ID, "Kryzorae Brumacero full set stability",
                    0.25D, AttributeModifier.Operation.ADDITION);
    private static final UUID UMBRACITA_SPEED_ID =
            UUID.fromString("d2d9481c-7e57-4940-9202-bebd5d4968aa");
    private static final AttributeModifier UMBRACITA_SPEED =
            new AttributeModifier(UMBRACITA_SPEED_ID, "Kryzorae Umbracita full set speed",
                    0.15D, AttributeModifier.Operation.MULTIPLY_TOTAL);

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

        PlayerEntity player = event.player;
        // One inexpensive equipment check per second; no particles, scans,
        // or work proportional to nearby enemies.
        ModifiableAttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            boolean equipped = isSet(player, true);
            boolean applied = speed.getModifier(UMBRACITA_SPEED_ID) != null;
            if (equipped && !applied) {
                speed.addTransientModifier(UMBRACITA_SPEED);
                player.displayClientMessage(new TranslationTextComponent(
                        "message.aetherium.umbracita_set_active"), true);
            } else if (!equipped && applied) {
                speed.removeModifier(UMBRACITA_SPEED_ID);
            }
        }

        ModifiableAttributeInstance stability = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (stability != null) {
            boolean equipped = isSet(player, false);
            boolean applied = stability.getModifier(BRUMACERO_STABILITY_ID) != null;
            if (equipped && !applied) {
                stability.addTransientModifier(BRUMACERO_STABILITY);
                player.displayClientMessage(new TranslationTextComponent(
                        "message.aetherium.brumacero_set_active"), true);
            } else if (!equipped && applied) {
                stability.removeModifier(BRUMACERO_STABILITY_ID);
            }
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity)
                || event.getEntityLiving().level.isClientSide
                || !(event.getSource().getEntity() instanceof MonsterEntity)) return;
        if (isSet((PlayerEntity) event.getEntityLiving(), false)) {
            event.setAmount(event.getAmount() * 0.82F);
        }
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity)
                ) return;
        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        if (!isSet(player, true)) return;
        Vector3d movement = player.getDeltaMovement();
        // Apply on client AND server to avoid client-side jump prediction
        // overwriting a server-only impulse. No effect without four pieces.
        player.setDeltaMovement(movement.x, movement.y * 1.15D, movement.z);
        player.hasImpulse = true;
    }
}
