package com.aetherium.aetherium.content.item;

import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

/** Astral edge: stronger life siphon and a restrained chain-arc on some strikes. */
public class AstralSwordItem extends SwordItem {
    public AstralSwordItem(net.minecraft.item.IItemTier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hit = super.hurtEnemy(stack, target, attacker);
        if (hit && !target.level.isClientSide) {
            attacker.heal(3.0F);
            if (target.level instanceof ServerWorld) {
                ServerWorld server = (ServerWorld) target.level;
                server.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.55D), target.getZ(),
                        10, 0.32D, 0.35D, 0.32D, 0.035D);
                server.sendParticles(ParticleTypes.PORTAL, attacker.getX(), attacker.getY(0.8D), attacker.getZ(),
                        7, 0.2D, 0.25D, 0.2D, 0.02D);
            }
            if (attacker.getRandom().nextFloat() < 0.24F) {
                List<LivingEntity> nearby = target.level.getEntitiesOfClass(LivingEntity.class,
                        target.getBoundingBox().inflate(2.7D, 1.3D, 2.7D),
                        entity -> entity != target && entity != attacker && entity.isAlive());
                for (LivingEntity entity : nearby) {
                    entity.hurt(DamageSource.MAGIC, 3.0F);
                    if (target.level instanceof ServerWorld) {
                        ServerWorld server = (ServerWorld) target.level;
                        for (int i = 0; i <= 5; i++) {
                            double t = i / 5.0D;
                            server.sendParticles(ParticleTypes.END_ROD,
                                    target.getX() + (entity.getX() - target.getX()) * t,
                                    target.getY(0.55D) + (entity.getY(0.55D) - target.getY(0.55D)) * t,
                                    target.getZ() + (entity.getZ() - target.getZ()) * t,
                                    1, 0.025D, 0.025D, 0.025D, 0.0D);
                        }
                    }
                }
                target.level.playSound(null, target.blockPosition(), RegistryHandler.ASTRAL_BLADE_ARC.get(),
                        SoundCategory.PLAYERS, 0.9F, 0.9F + attacker.getRandom().nextFloat() * 0.2F);
            }
        }
        return hit;
    }
}
