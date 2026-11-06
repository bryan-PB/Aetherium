package com.aetherium.aetherium.content.item;

import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.item.IItemTier;
import net.minecraft.item.crafting.Ingredient;

/** Two early-game equipment tiers; neither overtakes Kryzorae's boss weapons. */
public enum IntermediateItemTier implements IItemTier {
    BRUMACERO(425, 6.5F, 2.0F, 2, 12),
    UMBRACITA(950, 7.4F, 2.5F, 2, 17);

    private final int uses;
    private final float speed;
    private final float damage;
    private final int level;
    private final int enchantability;

    IntermediateItemTier(int uses, float speed, float damage, int level, int enchantability) {
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.level = level;
        this.enchantability = enchantability;
    }

    @Override public int getUses() { return uses; }
    @Override public float getSpeed() { return speed; }
    @Override public float getAttackDamageBonus() { return damage; }
    @Override public int getLevel() { return level; }
    @Override public int getEnchantmentValue() { return enchantability; }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(this == BRUMACERO
                ? RegistryHandler.BRUMACERO_INGOT.get()
                : RegistryHandler.UMBRACITA_INGOT.get());
    }
}
