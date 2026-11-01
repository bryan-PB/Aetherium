package com.aetherium.aetherium.content.armor;

import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

/** The rare endgame armor material, repaired with Astral Ingots. */
public final class AstralArmorMaterial implements IArmorMaterial {
    public static final AstralArmorMaterial INSTANCE = new AstralArmorMaterial();
    private static final int DURABILITY_MULTIPLIER = 52;
    private static final int[] BASE_DURABILITY = {13, 15, 16, 11};
    private static final int[] DEFENSE = {5, 10, 8, 5};

    private AstralArmorMaterial() { }

    @Override public int getDurabilityForSlot(EquipmentSlotType slot) {
        return BASE_DURABILITY[slot.getIndex()] * DURABILITY_MULTIPLIER;
    }
    @Override public int getDefenseForSlot(EquipmentSlotType slot) { return DEFENSE[slot.getIndex()]; }
    @Override public int getEnchantmentValue() { return 22; }
    @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_NETHERITE; }
    @Override public Ingredient getRepairIngredient() { return Ingredient.of(RegistryHandler.ASTRAL_INGOT.get()); }
    @Override public String getName() { return "aetherium:astral"; }
    @Override public float getToughness() { return 5.0F; }
    @Override public float getKnockbackResistance() { return 0.25F; }
}
