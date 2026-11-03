package com.aetherium.aetherium.content.item;

import com.aetherium.aetherium.content.boss.AetherBoltEntity;
import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.server.ServerWorld;

/** Astral edge: every fourth hit releases a visible, three-pronged bolt fan. */
public class AstralSwordItem extends SwordItem {
    private static final String COMBO_KEY = "AetheriumAstralCombo";
    private static final String COMBO_TIME_KEY = "AetheriumAstralComboTime";

    public AstralSwordItem(net.minecraft.item.IItemTier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hit = super.hurtEnemy(stack, target, attacker);
        if (!hit || target.level.isClientSide) return hit;

        attacker.heal(1.2F);
        target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 30, 0, true, true));
        long now = attacker.level.getGameTime();
        long lastHit = attacker.getPersistentData().getLong(COMBO_TIME_KEY);
        int combo = (now - lastHit > 40L ? 0 : attacker.getPersistentData().getInt(COMBO_KEY)) + 1;
        attacker.getPersistentData().putLong(COMBO_TIME_KEY, now);
        if (combo >= 4) {
            attacker.getPersistentData().putInt(COMBO_KEY, 0);
            attacker.getPersistentData().putLong(COMBO_TIME_KEY, 0L);
            releaseAstralFan(target, attacker);
        } else {
            attacker.getPersistentData().putInt(COMBO_KEY, combo);
            if (target.level instanceof ServerWorld) {
                ((ServerWorld) target.level).sendParticles(ParticleTypes.DRAGON_BREATH,
                        target.getX(), target.getY(0.55D), target.getZ(), 5,
                        0.20D, 0.25D, 0.20D, 0.012D);
            }
        }
        target.level.playSound(null, target.blockPosition(), RegistryHandler.ASTRAL_BLADE_ARC.get(),
                SoundCategory.PLAYERS, 0.8F, 0.9F + combo * 0.04F);
        return true;
    }

    private static void releaseAstralFan(LivingEntity target, LivingEntity attacker) {
        if (!(attacker.level instanceof ServerWorld)) return;
        ServerWorld server = (ServerWorld) attacker.level;
        attacker.heal(2.8F);
        double originY = attacker.getY() + attacker.getEyeHeight() * 0.72D;
        server.sendParticles(ParticleTypes.END_ROD, attacker.getX(), originY, attacker.getZ(),
                18, 0.25D, 0.25D, 0.25D, 0.035D);
        server.sendParticles(ParticleTypes.DRAGON_BREATH, attacker.getX(), originY, attacker.getZ(),
                20, 0.30D, 0.28D, 0.30D, 0.018D);

        for (int i = -1; i <= 1; i++) {
            AetherBoltEntity bolt = new AetherBoltEntity(attacker.level, attacker);
            bolt.setPos(attacker.getX(), originY, attacker.getZ());
            double spread = i * 1.1D;
            double dx = target.getX() + spread - bolt.getX();
            double dy = target.getY(0.52D) - bolt.getY();
            double dz = target.getZ() - bolt.getZ() - spread * 0.35D;
            bolt.shoot(dx, dy, dz, 1.28F, 0.22F);
            attacker.level.addFreshEntity(bolt);
        }
        target.hurt(DamageSource.MAGIC, 3.0F);
        target.addEffect(new EffectInstance(Effects.WEAKNESS, 50, 0, true, true));
        server.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.55D), target.getZ(),
                14, 0.3D, 0.34D, 0.3D, 0.06D);
        attacker.level.playSound(null, attacker.blockPosition(), RegistryHandler.ASTRAL_BLADE_ARC.get(),
                SoundCategory.PLAYERS, 1.05F, 0.72F);
    }
}
