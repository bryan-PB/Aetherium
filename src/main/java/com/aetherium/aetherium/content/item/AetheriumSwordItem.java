package com.aetherium.aetherium.content.item;

import com.aetherium.aetherium.content.projectile.DarkSlashEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

/**
 * Violet / purple sword. A valid main-hand swing emits a visible short-range
 * diagonal wave that deals exactly 50% of the sword's BASE attack damage,
 * not the player's enchantment- or potion-inflated total.
 *
 * A small server-side cooldown prevents spam and duplicate hand packet calls.
 * It works even if the vanilla melee swing misses; sword melee damage remains
 * fully vanilla and is separate from the projectile.
 */
public class AetheriumSwordItem extends SwordItem {
    private static final String LAST_SLASH_TICK = "KryzoraeVioletSlashLastTick";
    private static final int MIN_TICKS_BETWEEN_SLASHES = 9;
    private final float slashDamage;

    public AetheriumSwordItem(IItemTier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
        // The base player melee damage is 1 HP and the sword adds:
        // attackDamage + material damage. Current violet sword is 10 HP base,
        // so the short-range slash is 5 HP before the target's mitigations.
        this.slashDamage = (1.0F + attackDamage + tier.getAttackDamageBonus()) * 0.5F;
    }

    public float getSlashDamage() {
        return this.slashDamage;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity.level.isClientSide || !(entity instanceof PlayerEntity)
                || entity.getMainHandItem() != stack) return false;
        PlayerEntity player = (PlayerEntity) entity;
        long now = player.level.getGameTime();

        if (player.getPersistentData().contains(LAST_SLASH_TICK)
                && now - player.getPersistentData().getLong(LAST_SLASH_TICK)
                < MIN_TICKS_BETWEEN_SLASHES) return false;
        player.getPersistentData().putLong(LAST_SLASH_TICK, now);

        if (!(player.level instanceof ServerWorld)) return false;
        Vector3d look = player.getLookAngle().normalize();
        if (look.lengthSqr() < 0.0001D) return false;

        DarkSlashEntity wave = new DarkSlashEntity(player.level, player, this.slashDamage);
        Vector3d start = player.position().add(0.0D, player.getEyeHeight() * 0.73D, 0.0D)
                .add(look.scale(0.82D));
        wave.setPos(start.x, start.y, start.z);
        wave.setDeltaMovement(look.scale(DarkSlashEntity.SPEED));
        wave.yRot = player.yRot;
        wave.xRot = player.xRot;
        player.level.addFreshEntity(wave);
        return false; // Preserve the vanilla sword swing and melee attack.
    }
}
