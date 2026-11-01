package com.aetherium.aetherium.content.item;

import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.item.IItemTier;
import net.minecraft.item.crafting.Ingredient;

/** Final-tier tool material; powerful, intentionally expensive to craft and repair. */
public final class AstralItemTier implements IItemTier {
    public static final AstralItemTier INSTANCE = new AstralItemTier();

    private AstralItemTier() { }

    @Override public int getUses() { return 6000; }
    @Override public float getSpeed() { return 13.0F; }
    @Override public float getAttackDamageBonus() { return 5.0F; }
    @Override public int getLevel() { return 6; }
    @Override public int getEnchantmentValue() { return 22; }
    @Override public Ingredient getRepairIngredient() {
        return Ingredient.of(RegistryHandler.ASTRAL_INGOT.get());
    }
}
