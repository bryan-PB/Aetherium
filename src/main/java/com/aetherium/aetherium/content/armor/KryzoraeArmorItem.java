package com.aetherium.aetherium.content.armor;

import com.aetherium.aetherium.client.model.KryzoraeArmorModel;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Custom silhouette for both equipment tiers. The existing item and material IDs
 * remain unchanged, preserving recipes, inventories and world compatibility.
 */
public final class KryzoraeArmorItem extends ArmorItem {
    private final boolean elite;

    public KryzoraeArmorItem(IArmorMaterial material, EquipmentSlotType slot,
                            Properties properties, boolean elite) {
        super(material, slot, properties);
        this.elite = elite;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <A extends BipedModel<?>> A getArmorModel(LivingEntity entity, ItemStack stack,
                                                       EquipmentSlotType slot, A original) {
        return KryzoraeArmorModel.forSlot(elite, slot, original);
    }
}
