package com.aetherium.aetherium.content.item;

import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

/** Astral axe: a crushing blow that can discharge a short-range shock chain. */
public class AstralAxeItem extends AxeItem {
    public AstralAxeItem(IItemTier tier, float damage, float speed, Properties properties) {
        super(tier, damage, speed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hit = super.hurtEnemy(stack, target, attacker);
        if (hit && !target.level.isClientSide) {
            target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 40, 1, true, true));
            target.addEffect(new EffectInstance(Effects.WEAKNESS, 50, 0, true, true));
            if (attacker.getRandom().nextFloat() < 0.30F) {
                List<LivingEntity> nearby = target.level.getEntitiesOfClass(LivingEntity.class,
                        target.getBoundingBox().inflate(2.8D, 1.2D, 2.8D),
                        entity -> entity instanceof MonsterEntity && entity != target && entity != attacker && entity.isAlive());
                for (LivingEntity entity : nearby) {
                    entity.hurt(DamageSource.MAGIC, 3.0F);
                    entity.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 35, 1, true, true));
                    if (target.level instanceof ServerWorld) {
                        ServerWorld server = (ServerWorld) target.level;
                        for (int i = 0; i <= 8; i++) {
                            double t = i / 8.0D;
                            server.sendParticles(ParticleTypes.END_ROD,
                                    target.getX() + (entity.getX() - target.getX()) * t,
                                    target.getY(0.55D) + (entity.getY(0.55D) - target.getY(0.55D)) * t
                                            + (i % 2 == 0 ? -0.12D : 0.12D),
                                    target.getZ() + (entity.getZ() - target.getZ()) * t,
                                    1, 0.0D, 0.0D, 0.0D, 0.0D);
                        }
                    }
                }
                target.level.playSound(null, target.blockPosition(), RegistryHandler.ASTRAL_BLADE_ARC.get(),
                        SoundCategory.PLAYERS, 0.85F, 0.72F);
            }
            if (target.level instanceof ServerWorld) {
                ((ServerWorld) target.level).sendParticles(ParticleTypes.CRIT_MAGIC,
                        target.getX(), target.getY(0.5D), target.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.05D);
            }
        }
        return hit;
    }
}
