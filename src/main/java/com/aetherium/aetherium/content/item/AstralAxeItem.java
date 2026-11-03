package com.aetherium.aetherium.content.item;

import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.server.ServerWorld;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Astral axe: every third hit releases a visible chain discharge that jumps between enemies. */
public class AstralAxeItem extends AxeItem {
    private static final String COMBO_KEY = "AetheriumAstralAxeCombo";
    private static final String COMBO_TIME_KEY = "AetheriumAstralAxeComboTime";

    public AstralAxeItem(IItemTier tier, float damage, float speed, Properties properties) {
        super(tier, damage, speed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hit = super.hurtEnemy(stack, target, attacker);
        if (!hit || target.level.isClientSide) return hit;

        long now = attacker.level.getGameTime();
        long lastHit = stack.getOrCreateTag().getLong(COMBO_TIME_KEY);
        int combo = (now - lastHit > 44L ? 0 : stack.getOrCreateTag().getInt(COMBO_KEY)) + 1;
        stack.getOrCreateTag().putLong(COMBO_TIME_KEY, now);
        target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 28, 0, true, true));

        if (combo >= 3) {
            stack.getOrCreateTag().putInt(COMBO_KEY, 0);
            stack.getOrCreateTag().putLong(COMBO_TIME_KEY, 0L);
            dischargeChain(target, attacker);
        } else {
            stack.getOrCreateTag().putInt(COMBO_KEY, combo);
            if (target.level instanceof ServerWorld) {
                ((ServerWorld) target.level).sendParticles(ParticleTypes.END_ROD,
                        target.getX(), target.getY(0.55D), target.getZ(), 6,
                        0.16D, 0.20D, 0.16D, 0.018D);
            }
        }
        target.level.playSound(null, target.blockPosition(), RegistryHandler.ASTRAL_BLADE_ARC.get(),
                SoundCategory.PLAYERS, combo >= 3 ? 1.0F : 0.65F, combo >= 3 ? 0.58F : 0.94F);
        return true;
    }

    /** Hits the primary target, then selects up to three nearby enemies one jump at a time. */
    private static void dischargeChain(LivingEntity first, LivingEntity attacker) {
        if (!(first.level instanceof ServerWorld)) return;
        ServerWorld server = (ServerWorld) first.level;
        DamageSource source = attacker instanceof PlayerEntity
                ? DamageSource.playerAttack((PlayerEntity) attacker) : DamageSource.mobAttack(attacker);
        attacker.heal(1.6F);

        List<LivingEntity> candidates = attacker.level.getEntitiesOfClass(LivingEntity.class,
                first.getBoundingBox().inflate(9.0D, 3.0D, 9.0D),
                entity -> entity != attacker && entity.isAlive() && !entity.isAlliedTo(attacker));
        Set<UUID> visited = new HashSet<>();
        LivingEntity previous = attacker;

        first.hurt(source, 4.0F);
        first.addEffect(new EffectInstance(Effects.WEAKNESS, 55, 0, true, true));
        first.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 42, 0, true, true));
        drawArc(server, previous, first);
        visited.add(first.getUUID());
        previous = first;

        int jumps = 0;
        while (jumps < 3) {
            LivingEntity next = null;
            double nearest = 6.5D * 6.5D;
            for (LivingEntity candidate : candidates) {
                if (visited.contains(candidate.getUUID()) || !candidate.isAlive()) continue;
                double distance = candidate.distanceToSqr(previous);
                if (distance < nearest) {
                    nearest = distance;
                    next = candidate;
                }
            }
            if (next == null) break;

            next.hurt(DamageSource.MAGIC, 4.5F);
            next.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 48, 1, true, true));
            next.addEffect(new EffectInstance(Effects.WEAKNESS, 40, 0, true, true));
            drawArc(server, previous, next);
            visited.add(next.getUUID());
            previous = next;
            jumps++;
        }

        server.sendParticles(ParticleTypes.EXPLOSION, first.getX(), first.getY(0.5D), first.getZ(),
                1, 0.16D, 0.16D, 0.16D, 0.0D);
        attacker.level.playSound(null, first.blockPosition(), RegistryHandler.ASTRAL_BLADE_ARC.get(),
                SoundCategory.PLAYERS, 1.0F, 0.58F);
    }

    /** Renders each link as a dense chain of particles instead of a single barely visible spark. */
    private static void drawArc(ServerWorld server, LivingEntity from, LivingEntity to) {
        double x1 = from.getX();
        double y1 = from.getY(0.58D);
        double z1 = from.getZ();
        double x2 = to.getX();
        double y2 = to.getY(0.58D);
        double z2 = to.getZ();
        for (int i = 0; i <= 16; i++) {
            double t = i / 16.0D;
            double wave = Math.sin(t * Math.PI * 5.0D) * 0.14D;
            double x = x1 + (x2 - x1) * t + wave;
            double y = y1 + (y2 - y1) * t + Math.cos(t * Math.PI * 5.0D) * 0.12D;
            double z = z1 + (z2 - z1) * t - wave;
            server.sendParticles(ParticleTypes.END_ROD, x, y, z,
                    1, 0.015D, 0.015D, 0.015D, 0.0D);
            if (i % 2 == 0) {
                server.sendParticles(ParticleTypes.CRIT, x, y, z,
                        1, 0.02D, 0.02D, 0.02D, 0.0D);
            }
        }
        server.sendParticles(ParticleTypes.PORTAL, x2, y2, z2,
                7, 0.14D, 0.18D, 0.14D, 0.015D);
    }
}
