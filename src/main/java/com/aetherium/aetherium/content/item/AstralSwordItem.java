package com.aetherium.aetherium.content.item;

import com.aetherium.aetherium.content.effect.BleedingEffect;
import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.world.server.ServerWorld;

/**
 * Red / Crimson sword. Each successful melee hit adds exactly one bleeding
 * stack (maximum five) and renews its six-second duration. No old astral
 * projectile fan, hidden bonus hit, life steal or additional attack sound.
 */
public class AstralSwordItem extends SwordItem {
    public AstralSwordItem(IItemTier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean successfulHit = super.hurtEnemy(stack, target, attacker);
        if (!successfulHit || target.level.isClientSide || !target.isAlive()) return successfulHit;

        EffectInstance previous = target.getEffect(RegistryHandler.BLEEDING.get());
        int stacks = previous == null ? 1
                : Math.min(BleedingEffect.MAX_STACKS, previous.getAmplifier() + 2);

        // Remove before replacing to prevent an old weaker 'hidden effect'
        // resurfacing and to make the cap predictable across repeated hits.
        if (previous != null) target.removeEffect(RegistryHandler.BLEEDING.get());
        target.addEffect(new EffectInstance(RegistryHandler.BLEEDING.get(),
                BleedingEffect.DURATION_TICKS, stacks - 1, false, true, true));

        if (target.level instanceof ServerWorld) {
            ((ServerWorld) target.level).sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                    target.getX(), target.getY(0.55D), target.getZ(),
                    3 + stacks, 0.20D, 0.22D, 0.20D, 0.025D);
        }
        return true;
    }
}
