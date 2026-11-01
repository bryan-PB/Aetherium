package com.aetherium.aetherium.content.item;

import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.SoundCategory;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.server.ServerWorld;

/** Rift-forged blade: a modest life-steal pulse and a slowing cut. */
public class AetheriumSwordItem extends SwordItem {
    public AetheriumSwordItem(net.minecraft.item.IItemTier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hit = super.hurtEnemy(stack, target, attacker);
        if (hit && !target.level.isClientSide) {
            float healing = Math.min(2.0F, Math.max(0.5F, target.getMaxHealth() * 0.035F));
            attacker.heal(healing);
            target.addEffect(new net.minecraft.potion.EffectInstance(
                    net.minecraft.potion.Effects.MOVEMENT_SLOWDOWN, 35, 0, true, true));
            if (target.level instanceof ServerWorld) {
                ServerWorld server = (ServerWorld) target.level;
                server.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.55D), target.getZ(),
                        7, 0.22D, 0.25D, 0.22D, 0.02D);
                server.sendParticles(ParticleTypes.END_ROD, attacker.getX(), attacker.getY(0.8D), attacker.getZ(),
                        4, 0.18D, 0.25D, 0.18D, 0.015D);
            }
            target.level.playSound(null, target.blockPosition(), RegistryHandler.AETHERIUM_BLADE_HIT.get(),
                    SoundCategory.PLAYERS, 0.75F, 0.92F + attacker.getRandom().nextFloat() * 0.16F);
        }
        return hit;
    }
}
