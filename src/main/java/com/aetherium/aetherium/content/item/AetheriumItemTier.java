package com.aetherium.aetherium.content.item;

import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.item.IItemTier;
import net.minecraft.item.crafting.Ingredient;

/** Strong late-game tier: deliberately powerful, but expensive and limited to 3,200 uses. */
public final class AetheriumItemTier implements IItemTier {
    public static final AetheriumItemTier INSTANCE = new AetheriumItemTier();

    private AetheriumItemTier() {
    }

    @Override
    public int getUses() {
        return 3200;
    }

    @Override
    public float getSpeed() {
        return 10.5F;
    }

    @Override
    public float getAttackDamageBonus() {
        return 4.0F;
    }

    @Override
    public int getLevel() {
        return 5;
    }

    @Override
    public int getEnchantmentValue() {
        return 18;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(RegistryHandler.AETHERIUM_INGOT.get());
    }
}
