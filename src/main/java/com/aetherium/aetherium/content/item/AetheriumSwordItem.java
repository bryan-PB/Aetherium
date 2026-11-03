package com.aetherium.aetherium.content.item;

import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

/** Rift-forged blade: a reliable three-hit cleave that rewards maintaining an attack rhythm. */
public class AetheriumSwordItem extends SwordItem {
    private static final String COMBO_KEY = "AetheriumRiftCombo";
    private static final String COMBO_TIME_KEY = "AetheriumRiftComboTime";

    public AetheriumSwordItem(net.minecraft.item.IItemTier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hit = super.hurtEnemy(stack, target, attacker);
        if (!hit || target.level.isClientSide) return hit;

        attacker.heal(1.0F);
        target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 28, 0, true, true));
        long now = attacker.level.getGameTime();
        long lastHit = stack.getOrCreateTag().getLong(COMBO_TIME_KEY);
        int combo = (now - lastHit > 40L ? 0 : stack.getOrCreateTag().getInt(COMBO_KEY)) + 1;
        stack.getOrCreateTag().putLong(COMBO_TIME_KEY, now);
        if (combo >= 3) {
            stack.getOrCreateTag().putInt(COMBO_KEY, 0);
            stack.getOrCreateTag().putLong(COMBO_TIME_KEY, 0L);
            performRiftCleave(target, attacker);
        } else {
            stack.getOrCreateTag().putInt(COMBO_KEY, combo);
            if (target.level instanceof ServerWorld) {
                ((ServerWorld) target.level).sendParticles(ParticleTypes.CRIT,
                        target.getX(), target.getY(0.55D), target.getZ(), 8,
                        0.22D, 0.24D, 0.22D, 0.025D);
            }
        }
        target.level.playSound(null, target.blockPosition(), RegistryHandler.AETHERIUM_BLADE_HIT.get(),
                SoundCategory.PLAYERS, 0.8F, 0.88F + combo * 0.06F);
        return true;
    }

    private static void performRiftCleave(LivingEntity primary, LivingEntity attacker) {
        if (!(primary.level instanceof ServerWorld)) return;
        ServerWorld server = (ServerWorld) primary.level;
        attacker.heal(2.5F);
        AxisAlignedBB area = attacker.getBoundingBox().inflate(4.4D, 1.8D, 4.4D);
        List<LivingEntity> victims = attacker.level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != attacker && entity.isAlive() && !entity.isAlliedTo(attacker));
        for (LivingEntity victim : victims) {
            victim.hurt(DamageSource.MAGIC, victim == primary ? 4.0F : 3.0F);
            victim.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 40, 1, true, true));
            double dx = victim.getX() - attacker.getX();
            double dz = victim.getZ() - attacker.getZ();
            double length = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
            victim.setDeltaMovement(victim.getDeltaMovement().add(dx / length * 0.48D, 0.16D, dz / length * 0.48D));
            victim.hurtMarked = true;
            server.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY(0.55D), victim.getZ(),
                    12, 0.26D, 0.32D, 0.26D, 0.05D);
        }

        for (int i = 0; i < 24; i++) {
            double angle = Math.PI * 2.0D * i / 24.0D;
            double x = attacker.getX() + Math.cos(angle) * 3.2D;
            double z = attacker.getZ() + Math.sin(angle) * 3.2D;
            server.sendParticles(ParticleTypes.SWEEP_ATTACK, x, attacker.getY(0.45D), z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            server.sendParticles(ParticleTypes.END_ROD, x, attacker.getY(0.45D), z,
                    1, 0.025D, 0.035D, 0.025D, 0.0D);
        }
        server.sendParticles(ParticleTypes.EXPLOSION, attacker.getX(), attacker.getY(0.55D),
                attacker.getZ(), 2, 0.25D, 0.25D, 0.25D, 0.0D);
        attacker.level.playSound(null, attacker.blockPosition(), RegistryHandler.AETHERIUM_BLADE_HIT.get(),
                SoundCategory.PLAYERS, 1.0F, 0.58F);
    }
}
