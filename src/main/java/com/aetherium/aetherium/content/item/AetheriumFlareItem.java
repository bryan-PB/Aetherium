package com.aetherium.aetherium.content.item;

import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import java.util.List;

/** One-shot exploration flare: short night vision and a bounded reveal pulse for nearby hostiles. */
public class AetheriumFlareItem extends Item {
    private static final double RADIUS = 12.0D;
    private static final int MAX_REVEALED_MOBS = 12;

    public AetheriumFlareItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            player.addEffect(new EffectInstance(Effects.NIGHT_VISION, 20 * 30, 0, false, true));
            List<MonsterEntity> hostiles = world.getEntitiesOfClass(MonsterEntity.class,
                    player.getBoundingBox().inflate(RADIUS), MonsterEntity::isAlive);
            int revealed = 0;
            for (MonsterEntity hostile : hostiles) {
                if (revealed >= MAX_REVEALED_MOBS) break;
                hostile.addEffect(new EffectInstance(Effects.GLOWING, 20 * 7, 0, true, false));
                revealed++;
            }
            if (!player.abilities.instabuild) stack.shrink(1);
            player.getCooldowns().addCooldown(this, 20 * 12);
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    RegistryHandler.FLARE_ACTIVATION.get(), SoundCategory.PLAYERS, 0.82F, 1.0F);
        }
        return ActionResult.sidedSuccess(stack, world.isClientSide);
    }
    @Override
    public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.add(new TranslationTextComponent("tooltip.aetherium.flare"));
    }

}
