package com.aetherium.aetherium.content.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;

/** Toggleable pickup helper. The bounded pull logic lives in AetheriumUtilityEvents. */
public class AetheriumMagnetItem extends Item {
    public static final String ACTIVE_TAG = "AetheriumMagnetActive";

    public AetheriumMagnetItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            boolean active = !stack.getOrCreateTag().getBoolean(ACTIVE_TAG);
            stack.getOrCreateTag().putBoolean(ACTIVE_TAG, active);
            player.displayClientMessage(new TranslationTextComponent(
                    active ? "message.aetherium.magnet_on" : "message.aetherium.magnet_off"), true);
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    active ? SoundEvents.ENDER_EYE_LAUNCH : SoundEvents.LEVER_CLICK,
                    SoundCategory.PLAYERS, 0.55F, active ? 1.25F : 0.8F);
        }
        return ActionResult.sidedSuccess(stack, world.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.add(new TranslationTextComponent("tooltip.aetherium.magnet"));
        tooltip.add(new TranslationTextComponent(stack.getOrCreateTag().getBoolean(ACTIVE_TAG)
                ? "tooltip.aetherium.magnet_active" : "tooltip.aetherium.magnet_inactive"));
    }

    public static boolean isActive(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof AetheriumMagnetItem
                && stack.getOrCreateTag().getBoolean(ACTIVE_TAG);
    }
}
