package com.aetherium.aetherium.content.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;


/**
 * Accessory item with an optional Curios slot tag integration.
 * Without Curios, right-click activates a short defensive/utility pulse.
 */
public class AetheriumAccessoryItem extends Item {
    public enum Type { AMULET, RING }

    private final Type type;

    public AetheriumAccessoryItem(Type type, Properties properties) {
        super(properties.stacksTo(1));
        this.type = type;
    }

    /** Fallback ability for players using the mod without Curios installed. */
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            if (type == Type.AMULET) {
                player.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, 20 * 20, 0, false, true));
                player.getCooldowns().addCooldown(this, 20 * 90);
            } else {
                player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 20 * 20, 1, false, true));
                player.addEffect(new EffectInstance(Effects.NIGHT_VISION, 20 * 45, 0, false, true));
                player.getCooldowns().addCooldown(this, 20 * 90);
            }
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 0.7F, 1.15F);
        }
        return ActionResult.sidedSuccess(stack, world.isClientSide);
    }

}
