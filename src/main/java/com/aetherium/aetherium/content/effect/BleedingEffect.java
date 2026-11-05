package com.aetherium.aetherium.content.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraft.world.server.ServerWorld;

/**
 * Carmesí bleeding: 2 base health points per second per stack.
 * Amplifier 0..2 means one to three stacks (up to 6 HP/s).
 *
 * Using entity.tickCount instead of effect duration means each reapplication
 * can refresh or increase the stack without postponing the next damage tick.
 * Ordinary armor is bypassed; protection from invulnerability/absorption and
 * other mods' damage events is still respected by the vanilla hurt pipeline.
 */
public final class BleedingEffect extends Effect {
    public static final int MAX_STACKS = 3;
    public static final int DURATION_TICKS = 100; // five seconds after the most recent hit
    public static final int TICKS_PER_DAMAGE = 20;
    public static final float DAMAGE_PER_STACK = 2.0F;

    private static final DamageSource BLEEDING_DAMAGE =
            new DamageSource("kryzorae_bleeding").bypassArmor().bypassMagic();

    public BleedingEffect() {
        super(EffectType.HARMFUL, 0xB92D42);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // applyEffectTick is called every tick; tickCount gates the damage on
        // a stable 20-tick clock even if the effect is refreshed every hit.
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
        if (target.level.isClientSide || !target.isAlive()
                || target.tickCount % TICKS_PER_DAMAGE != 0) return;

        int stacks = Math.min(MAX_STACKS, Math.max(1, amplifier + 1));
        float damage = stacks * DAMAGE_PER_STACK;
        if (target.hurt(BLEEDING_DAMAGE, damage) && target.level instanceof ServerWorld) {
            ((ServerWorld) target.level).sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                    target.getX(), target.getY(0.58D), target.getZ(),
                    Math.min(14, 2 + stacks * 2), 0.25D, 0.30D, 0.25D, 0.018D);
        }
    }
}
