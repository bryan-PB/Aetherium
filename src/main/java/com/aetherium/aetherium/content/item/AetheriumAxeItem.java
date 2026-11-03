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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

/** Heavy rift axe: every third connected hit creates a visible ground-shattering shockwave. */
public class AetheriumAxeItem extends AxeItem {
    private static final String COMBO_KEY = "AetheriumAxeCombo";
    private static final String COMBO_TIME_KEY = "AetheriumAxeComboTime";

    public AetheriumAxeItem(IItemTier tier, float damage, float speed, Properties properties) {
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
        target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 22, 0, true, true));

        if (combo >= 3) {
            stack.getOrCreateTag().putInt(COMBO_KEY, 0);
            stack.getOrCreateTag().putLong(COMBO_TIME_KEY, 0L);
            performRiftQuake(target, attacker);
        } else {
            stack.getOrCreateTag().putInt(COMBO_KEY, combo);
            if (target.level instanceof ServerWorld) {
                ((ServerWorld) target.level).sendParticles(ParticleTypes.CRIT,
                        target.getX(), target.getY(0.5D), target.getZ(), 8,
                        0.2D, 0.22D, 0.2D, 0.025D);
            }
        }
        target.level.playSound(null, target.blockPosition(), RegistryHandler.AETHERIUM_BLADE_HIT.get(),
                SoundCategory.PLAYERS, combo >= 3 ? 1.0F : 0.65F, combo >= 3 ? 0.48F : 0.78F);
        return true;
    }

    /** Sends a circular fracture through the ground, damaging and knocking back nearby enemies. */
    private static void performRiftQuake(LivingEntity primary, LivingEntity attacker) {
        if (!(primary.level instanceof ServerWorld)) return;
        ServerWorld server = (ServerWorld) primary.level;
        DamageSource source = attacker instanceof PlayerEntity
                ? DamageSource.playerAttack((PlayerEntity) attacker) : DamageSource.mobAttack(attacker);
        attacker.heal(2.0F);

        AxisAlignedBB area = attacker.getBoundingBox().inflate(5.2D, 1.6D, 5.2D);
        List<LivingEntity> victims = attacker.level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != attacker && entity.isAlive() && !entity.isAlliedTo(attacker));
        for (LivingEntity victim : victims) {
            double dx = victim.getX() - attacker.getX();
            double dz = victim.getZ() - attacker.getZ();
            double distance = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
            if (distance > 5.0D || Math.abs(victim.getY() - attacker.getY()) > 2.8D) continue;
            victim.hurt(source, victim == primary ? 5.0F : 4.0F);
            victim.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 55, 1, true, true));
            victim.setDeltaMovement(victim.getDeltaMovement().add(dx / distance * 0.85D, 0.32D, dz / distance * 0.85D));
            victim.hurtMarked = true;
            server.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY(0.45D), victim.getZ(),
                    12, 0.28D, 0.24D, 0.28D, 0.045D);
            server.sendParticles(ParticleTypes.POOF,
                    victim.getX(), victim.getY() + 0.12D, victim.getZ(), 8,
                    0.24D, 0.08D, 0.24D, 0.025D);
        }

        // Three expanding rings make the slam readable even when no enemy is nearby.
        for (int ring = 0; ring < 3; ring++) {
            double radius = 1.6D + ring * 1.35D;
            for (int i = 0; i < 28; i++) {
                double angle = Math.PI * 2.0D * i / 28.0D;
                double x = attacker.getX() + Math.cos(angle) * radius;
                double z = attacker.getZ() + Math.sin(angle) * radius;
                server.sendParticles(ParticleTypes.SWEEP_ATTACK, x, attacker.getY() + 0.18D + ring * 0.06D, z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
                server.sendParticles(ParticleTypes.CRIT, x, attacker.getY() + 0.18D, z,
                        1, 0.025D, 0.08D, 0.025D, 0.015D);
            }
        }
        server.sendParticles(ParticleTypes.EXPLOSION, attacker.getX(), attacker.getY(0.5D),
                attacker.getZ(), 2, 0.22D, 0.2D, 0.22D, 0.0D);
        attacker.level.playSound(null, attacker.blockPosition(), RegistryHandler.RIFT_GUARDIAN_SLAM.get(),
                SoundCategory.PLAYERS, 0.9F, 0.62F);
    }
}
