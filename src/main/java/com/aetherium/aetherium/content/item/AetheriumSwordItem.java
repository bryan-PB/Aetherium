package com.aetherium.aetherium.content.item;

import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.server.ServerWorld;

/** Rift-forged blade: controlled life siphon with a visible chance to lock a target in place. */
public class AetheriumSwordItem extends SwordItem {
    public AetheriumSwordItem(net.minecraft.item.IItemTier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hit = super.hurtEnemy(stack, target, attacker);
        if (!hit || target.level.isClientSide) return hit;

        float healing = Math.min(2.0F, Math.max(0.5F, target.getMaxHealth() * 0.035F));
        attacker.heal(healing);
        target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 35, 0, true, true));

        if (target.level instanceof ServerWorld) {
            ServerWorld server = (ServerWorld) target.level;
            server.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.55D), target.getZ(),
                    8, 0.24D, 0.28D, 0.24D, 0.025D);
            server.sendParticles(ParticleTypes.END_ROD, attacker.getX(), attacker.getY(0.8D), attacker.getZ(),
                    6, 0.18D, 0.25D, 0.18D, 0.025D);
        }

        // A clear, occasional rift-lock: short heavy slow and weakness, shown by a jagged energy trace.
        if (attacker.getRandom().nextFloat() < 0.20F) {
            target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 26, 3, true, true));
            target.addEffect(new EffectInstance(Effects.WEAKNESS, 35, 0, true, true));
            if (target.level instanceof ServerWorld) {
                ServerWorld server = (ServerWorld) target.level;
                double sx = attacker.getX(), sy = attacker.getY(0.72D), sz = attacker.getZ();
                double ex = target.getX(), ey = target.getY(0.55D), ez = target.getZ();
                for (int i = 0; i <= 12; i++) {
                    double t = i / 12.0D;
                    double zig = (i % 2 == 0 ? -1.0D : 1.0D) * 0.14D;
                    server.sendParticles(ParticleTypes.END_ROD,
                            sx + (ex - sx) * t + zig, sy + (ey - sy) * t,
                            sz + (ez - sz) * t - zig, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
                server.sendParticles(ParticleTypes.CRIT, ex, ey, ez, 9, 0.25D, 0.3D, 0.25D, 0.08D);
            }
        }

        target.level.playSound(null, target.blockPosition(), RegistryHandler.AETHERIUM_BLADE_HIT.get(),
                SoundCategory.PLAYERS, 0.78F, 0.9F + attacker.getRandom().nextFloat() * 0.2F);
        return true;
    }
}
