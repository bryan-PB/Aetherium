package com.aetherium.aetherium.content.armor;

import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

/**
 * Armor uses vanilla biped geometry and an independent pixel atlas per tier.
 * Slot order: feet, legs, chest, head.
 */
public enum IntermediateArmorMaterial implements IArmorMaterial {
    BRUMACERO("brumacero", 20, new int[]{3, 5, 6, 2}, 12, 0.5F),
    UMBRACITA("umbracita", 28, new int[]{2, 4, 6, 2}, 17, 1.0F);

    private static final int[] BASE_DURABILITY = {13, 15, 16, 11};
    private final String id;
    private final int durability;
    private final int[] defense;
    private final int enchantability;
    private final float toughness;

    IntermediateArmorMaterial(String id, int durability, int[] defense,
                              int enchantability, float toughness) {
        this.id = id;
        this.durability = durability;
        this.defense = defense;
        this.enchantability = enchantability;
        this.toughness = toughness;
    }

    @Override public int getDurabilityForSlot(EquipmentSlotType slot) {
        return BASE_DURABILITY[slot.getIndex()] * durability;
    }
    @Override public int getDefenseForSlot(EquipmentSlotType slot) {
        return defense[slot.getIndex()];
    }
    @Override public int getEnchantmentValue() { return enchantability; }
    @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_IRON; }
    @Override public Ingredient getRepairIngredient() {
        return Ingredient.of(this == BRUMACERO
                ? RegistryHandler.BRUMACERO_INGOT.get()
                : RegistryHandler.UMBRACITA_INGOT.get());
    }
    @Override public String getName() { return "aetherium:" + id; }
    @Override public float getToughness() { return toughness; }
    @Override public float getKnockbackResistance() { return 0.0F; }
}
