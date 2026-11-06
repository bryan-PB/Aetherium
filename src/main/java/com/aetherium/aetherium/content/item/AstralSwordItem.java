package com.aetherium.aetherium.content.item;

import com.aetherium.aetherium.content.effect.BleedingEffect;
import com.aetherium.aetherium.content.boss.AstralDevourerEntity;
import com.aetherium.aetherium.content.boss.RiftGuardianEntity;
import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.world.server.ServerWorld;

/**
 * Red / Crimson sword. Each successful melee hit adds exactly one bleeding
 * stack (maximum three) and renews its five-second duration. No old astral
 * projectile fan, hidden bonus hit, life steal or additional attack sound.
 */
public class AstralSwordItem extends SwordItem {
    // Restrict rapid re-stacking on bosses, not the ordinary physical sword hit.
    // 12 ticks approximately matches a fully charged 1.6 attacks/s sword.
    public static final long BOSS_BLEED_PROC_INTERVAL = 12L;
    private static final String LAST_BOSS_BLEED_PROC = "KryzoraeCrimsonLastBossProc";
    public AstralSwordItem(IItemTier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean successfulHit = super.hurtEnemy(stack, target, attacker);
        if (!successfulHit || target.level.isClientSide || !target.isAlive()) return successfulHit;

        boolean boss = target instanceof RiftGuardianEntity
                || target instanceof AstralDevourerEntity;
        if (boss) {
            // Fast repeated clicks still deal ordinary melee damage, but cannot
            // stack/refresh bleeding multiple times inside one attack cadence.
            // Persistent target NBT also prevents bypass with a second sword.
            CompoundNBT data = target.getPersistentData();
            long now = target.level.getGameTime();
            if (data.contains(LAST_BOSS_BLEED_PROC)) {
                long previousTick = data.getLong(LAST_BOSS_BLEED_PROC);
                if (now >= previousTick
                        && now - previousTick < BOSS_BLEED_PROC_INTERVAL) return true;
            }
            data.putLong(LAST_BOSS_BLEED_PROC, now);
        }

        EffectInstance previous = target.getEffect(RegistryHandler.BLEEDING.get());
        int stacks = previous == null ? 1
                : Math.min(BleedingEffect.MAX_STACKS, previous.getAmplifier() + 2);

        // Remove before replacing to prevent an old weaker 'hidden effect'
        // resurfacing and to make the cap predictable across repeated hits.
        if (previous != null) target.removeEffect(RegistryHandler.BLEEDING.get());
        target.addEffect(new EffectInstance(RegistryHandler.BLEEDING.get(),
                BleedingEffect.DURATION_TICKS, stacks - 1, false, true, true));

        // Three properly spaced Crimson strikes can trigger a telegraphed
        // defensive maneuver from a neutral boss; neither becomes immune.
        if (boss && attacker instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) attacker;
            if (target instanceof RiftGuardianEntity) {
                ((RiftGuardianEntity) target).registerCrimsonHit(player);
            } else if (target instanceof AstralDevourerEntity) {
                ((AstralDevourerEntity) target).registerCrimsonHit(player);
            }
        }

        if (target.level instanceof ServerWorld) {
            ((ServerWorld) target.level).sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                    target.getX(), target.getY(0.55D), target.getZ(),
                    3 + stacks, 0.20D, 0.22D, 0.20D, 0.025D);
        }
        return true;
    }
}
