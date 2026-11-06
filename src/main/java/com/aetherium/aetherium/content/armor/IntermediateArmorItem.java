package com.aetherium.aetherium.content.armor;

import com.aetherium.aetherium.client.model.IntermediateArmorModel;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Custom articulated geometry, like the approved Violet/Crimson sets.
 * Existing registries, material stats, crafting, bonuses and saved items stay intact.
 */
public final class IntermediateArmorItem extends ArmorItem {
    private final boolean umbracita;

    public IntermediateArmorItem(IntermediateArmorMaterial material,
                                 EquipmentSlotType slot, Properties properties) {
        super(material, slot, properties);
        this.umbracita = material == IntermediateArmorMaterial.UMBRACITA;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <A extends BipedModel<?>> A getArmorModel(LivingEntity entity, ItemStack stack,
                                                      EquipmentSlotType slot, A original) {
        return IntermediateArmorModel.forSlot(umbracita, slot, original);
    }
}
