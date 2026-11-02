package com.aetherium.aetherium.content.item;

import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

/** Astral edge: stronger life siphon, brief stun and a visible chain-lightning strike. */
public class AstralSwordItem extends SwordItem {
    public AstralSwordItem(net.minecraft.item.IItemTier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hit = super.hurtEnemy(stack, target, attacker);
        if (!hit || target.level.isClientSide) return hit;

        attacker.heal(3.0F);
        target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 24, 0, true, true));
        if (target.level instanceof ServerWorld) {
            ServerWorld server = (ServerWorld) target.level;
            server.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.55D), target.getZ(),
                    12, 0.34D, 0.38D, 0.34D, 0.04D);
            server.sendParticles(ParticleTypes.PORTAL, attacker.getX(), attacker.getY(0.8D), attacker.getZ(),
                    9, 0.22D, 0.28D, 0.22D, 0.035D);
        }

        if (attacker.getRandom().nextFloat() < 0.24F) {
            List<LivingEntity> nearby = target.level.getEntitiesOfClass(LivingEntity.class,
                    target.getBoundingBox().inflate(3.2D, 1.6D, 3.2D),
                    entity -> entity instanceof MonsterEntity && entity != target && entity != attacker && entity.isAlive());
            for (LivingEntity entity : nearby) {
                entity.hurt(DamageSource.MAGIC, 4.0F);
                entity.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 55, 1, true, true));
                entity.addEffect(new EffectInstance(Effects.WEAKNESS, 45, 0, true, true));
                if (target.level instanceof ServerWorld) {
                    drawArc((ServerWorld) target.level, target, entity);
                }
            }
            target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 45, 1, true, true));
            target.addEffect(new EffectInstance(Effects.WEAKNESS, 35, 0, true, true));
            target.level.playSound(null, target.blockPosition(), RegistryHandler.ASTRAL_BLADE_ARC.get(),
                    SoundCategory.PLAYERS, 0.95F, 0.86F + attacker.getRandom().nextFloat() * 0.24F);
        }
        return true;
    }

    private static void drawArc(ServerWorld world, LivingEntity from, LivingEntity to) {
        double sx = from.getX(), sy = from.getY(0.58D), sz = from.getZ();
        double ex = to.getX(), ey = to.getY(0.58D), ez = to.getZ();
        for (int i = 0; i <= 14; i++) {
            double t = i / 14.0D;
            double zig = (i % 2 == 0 ? -0.16D : 0.16D);
            world.sendParticles(ParticleTypes.END_ROD,
                    sx + (ex - sx) * t + zig, sy + (ey - sy) * t,
                    sz + (ez - sz) * t - zig, 1, 0.012D, 0.012D, 0.012D, 0.0D);
        }
        world.sendParticles(ParticleTypes.CRIT, ex, ey, ez, 10, 0.32D, 0.36D, 0.32D, 0.05D);
    }
}
