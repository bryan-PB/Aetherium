package com.aetherium.aetherium.events;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.config.AetheriumConfig;
import com.aetherium.aetherium.content.item.AetheriumMagnetItem;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/** Low-frequency, opt-in pickup magnet. Work occurs only for players carrying an enabled magnet. */
@Mod.EventBusSubscriber(modid = AetheriumMod.MOD_ID)
public final class AetheriumUtilityEvents {
    private AetheriumUtilityEvents() { }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide) return;
        PlayerEntity player = event.player;
        if (player.tickCount % 10 != 0) return;

        boolean enabled = false;
        for (ItemStack stack : player.inventory.items) {
            if (AetheriumMagnetItem.isActive(stack)) {
                enabled = true;
                break;
            }
        }
        if (!enabled) {
            for (ItemStack stack : player.inventory.offhand) {
                if (AetheriumMagnetItem.isActive(stack)) {
                    enabled = true;
                    break;
                }
            }
        }
        if (!enabled) return;

        double radius = AetheriumConfig.MAGNET_RADIUS.get();
        AxisAlignedBB area = player.getBoundingBox().inflate(radius);
        List<ItemEntity> nearby = player.level.getEntitiesOfClass(ItemEntity.class, area,
                item -> item.isAlive() && !item.hasPickUpDelay());
        int pulled = 0;
        int maxItems = AetheriumConfig.MAGNET_MAX_ITEMS.get();
        Vector3d playerCenter = player.position().add(0.0D, player.getBbHeight() * 0.45D, 0.0D);
        for (ItemEntity item : nearby) {
            if (pulled >= maxItems) break;
            Vector3d delta = playerCenter.subtract(item.position());
            double distanceSqr = delta.lengthSqr();
            if (distanceSqr < 0.25D || distanceSqr > radius * radius) continue;
            Vector3d pull = delta.normalize().scale(0.12D);
            item.setDeltaMovement(item.getDeltaMovement().scale(0.65D).add(pull));
            item.hasImpulse = true;
            pulled++;
        }
    }
}
