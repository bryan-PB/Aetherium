package com.aetherium.aetherium.content.item;

import com.aetherium.aetherium.client.GuideBookClient;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;
import java.util.List;

/** Lightweight standalone guide: the vanilla reading UI is opened only on demand. */
public final class GuideBookItem extends Item {
    public GuideBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (world.isClientSide) {
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> GuideBookClient::open);
        }
        return ActionResult.sidedSuccess(player.getItemInHand(hand), world.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world,
                                List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new TranslationTextComponent("tooltip.aetherium.guide_book")
                .withStyle(TextFormatting.GRAY));
    }
}
