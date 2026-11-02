package com.aetherium.aetherium.content.item;

import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.server.ServerWorld;

/** Heavy rift axe: its impact briefly roots and weakens a target. */
public class AetheriumAxeItem extends AxeItem {
    public AetheriumAxeItem(IItemTier tier, float damage, float speed, Properties properties) {
        super(tier, damage, speed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hit = super.hurtEnemy(stack, target, attacker);
        if (hit && !target.level.isClientSide) {
            attacker.heal(1.0F);
            target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 32, 1, true, true));
            if (attacker.getRandom().nextFloat() < 0.22F) {
                target.addEffect(new EffectInstance(Effects.WEAKNESS, 45, 0, true, true));
                target.addEffect(new EffectInstance(Effects.GLOWING, 25, 0, true, false));
                if (target.level instanceof ServerWorld) {
                    ServerWorld server = (ServerWorld) target.level;
                    server.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.5D), target.getZ(),
                            16, 0.38D, 0.42D, 0.38D, 0.055D);
                    server.sendParticles(ParticleTypes.CRIT_MAGIC, target.getX(), target.getY(0.5D), target.getZ(),
                            8, 0.25D, 0.35D, 0.25D, 0.07D);
                }
            }
            target.level.playSound(null, target.blockPosition(), RegistryHandler.AETHERIUM_BLADE_HIT.get(),
                    SoundCategory.PLAYERS, 0.62F, 0.62F);
        }
        return hit;
    }
}
