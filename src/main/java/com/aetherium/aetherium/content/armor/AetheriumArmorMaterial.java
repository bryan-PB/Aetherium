package com.aetherium.aetherium.content.armor;

import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;

/** Aetherium armor is tougher than Netherite, balanced by a costly repair ingredient. */
public final class AetheriumArmorMaterial implements IArmorMaterial {
    public static final AetheriumArmorMaterial INSTANCE = new AetheriumArmorMaterial();

    private static final int DURABILITY_MULTIPLIER = 44;
    private static final int[] BASE_DURABILITY = {13, 15, 16, 11};
    private static final int[] DEFENSE = {4, 9, 7, 4};

    private AetheriumArmorMaterial() {
    }

    @Override
    public int getDurabilityForSlot(EquipmentSlotType slot) {
        return BASE_DURABILITY[slot.getIndex()] * DURABILITY_MULTIPLIER;
    }

    @Override
    public int getDefenseForSlot(EquipmentSlotType slot) {
        return DEFENSE[slot.getIndex()];
    }

    @Override
    public int getEnchantmentValue() {
        return 18;
    }

    @Override
    public SoundEvent getEquipSound() {
        return RegistryHandler.AETHERIUM_EQUIP.get();
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(RegistryHandler.AETHERIUM_INGOT.get());
    }

    @Override
    public String getName() {
        return "aetherium:aetherium";
    }

    @Override
    public float getToughness() {
        return 4.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.15F;
    }
}
