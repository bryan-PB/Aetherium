package com.aetherium.aetherium.content.item;

import com.aetherium.aetherium.content.boss.AstralDevourerEntity;
import com.aetherium.aetherium.content.boss.RiftGuardianEntity;
import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;

/** Consumed on block use to summon one arena boss; prevents duplicate nearby encounters. */
public class BossSummoningItem extends Item {
    private final boolean astral;

    public BossSummoningItem(boolean astral, Properties properties) {
        super(properties.stacksTo(16));
        this.astral = astral;
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World world = context.getLevel();
        PlayerEntity player = context.getPlayer();
        if (world.isClientSide) return ActionResultType.SUCCESS;

        BlockPos spawnPos = context.getClickedPos().above();
        if (!world.getBlockState(spawnPos).isAir() || !world.getBlockState(spawnPos.above()).isAir()) {
            return ActionResultType.FAIL;
        }

        AxisAlignedBB search = new AxisAlignedBB(spawnPos).inflate(40.0D, 24.0D, 40.0D);
        List<MonsterEntity> activeBosses = world.getEntitiesOfClass(MonsterEntity.class, search,
                entity -> entity instanceof RiftGuardianEntity || entity instanceof AstralDevourerEntity);
        if (!activeBosses.isEmpty()) {
            if (player != null) player.displayClientMessage(
                    new TranslationTextComponent("message.aetherium.boss_already_nearby"), true);
            return ActionResultType.FAIL;
        }

        Entity boss = this.astral
                ? RegistryHandler.ASTRAL_DEVOURER.get().create(world)
                : RegistryHandler.RIFT_GUARDIAN.get().create(world);
        if (boss == null) return ActionResultType.FAIL;
        boss.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                player == null ? 0.0F : player.yRot, 0.0F);
        if (!world.noCollision(boss)) {
            boss.remove();
            return ActionResultType.FAIL;
        }

        if (world.addFreshEntity(boss)) {
            if (player != null && !player.abilities.instabuild) {
                ItemStack stack = context.getItemInHand();
                stack.shrink(1);
            }
            world.playSound(null, spawnPos, SoundEvents.END_PORTAL_SPAWN,
                    SoundCategory.HOSTILE, 0.9F, this.astral ? 0.72F : 0.9F);
            return ActionResultType.CONSUME;
        }
        return ActionResultType.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, World world, List<net.minecraft.util.text.ITextComponent> tooltip,
                                net.minecraft.client.util.ITooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.add(new TranslationTextComponent("tooltip.aetherium.boss_summon"));
        if (this.astral) tooltip.add(new TranslationTextComponent("tooltip.aetherium.astral_seal"));
    }
}
