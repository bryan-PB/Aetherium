package com.aetherium.aetherium.client.model;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Armor with visible three-dimensional brows, projecting shoulder plates,
 * articulated chest plating, reinforced knees and sabaton tips. Child cubes
 * inherit vanilla BipedModel player/armor stand animation and work in 1.16.5.
 *
 * The two sets differ in silhouette, not just in their colors: the upgraded
 * tier has more pronounced cheek armor, layered chest and longer pauldrons.
 */
@OnlyIn(Dist.CLIENT)
public final class KryzoraeArmorModel extends BipedModel<LivingEntity> {
    private static KryzoraeArmorModel forgeOuter, forgeInner, executionerOuter, executionerInner;

    private KryzoraeArmorModel(boolean elite, float padding) {
        super(padding);
        texWidth = 64;
        texHeight = 32;

        // A flat, reinforced visor with restrained cheek guards. No horned helmet.
        add(head, 0, 0, -4.7F, -8.1F, -4.8F, 9.4F, 1.6F, 1.2F);
        add(head, 0, 16, -4.5F, -5.8F, -4.6F, elite ? 1.9F : 1.3F, 5.4F, 1.1F);
        add(head, 0, 16, elite ? 2.6F : 3.2F, -5.8F, -4.6F,
                elite ? 1.9F : 1.3F, 5.4F, 1.1F);
        if (elite) {
            add(head, 28, 0, -1.0F, -8.8F, -4.4F, 2.0F, 2.0F, 1.3F);
        }

        // Breastplate follows vanilla body rotation (including crouch and swim).
        add(body, 16, 16, -4.4F, 1.1F, -3.8F, 8.8F, 5.6F, elite ? 1.5F : 1.0F);
        add(body, 16, 16, -3.7F, 7.0F, -3.5F, 7.4F, 3.2F, 0.9F);
        if (elite) {
            // Additional split chest ribs and central triangular spine.
            add(body, 34, 17, -4.7F, 0.8F, -4.0F, 1.7F, 8.3F, 1.4F);
            add(body, 34, 17, 3.0F, 0.8F, -4.0F, 1.7F, 8.3F, 1.4F);
            add(body, 0, 17, -0.85F, 0.2F, -4.2F, 1.7F, 7.7F, 1.2F);
        }

        // Distinct shoulder width, but not enough to obscure the player view.
        add(rightArm, 0, 16, -3.3F, -2.3F, -2.9F,
                elite ? 5.8F : 5.0F, elite ? 4.7F : 3.8F, 5.8F);
        add(leftArm, 0, 16, -2.5F, -2.3F, -2.9F,
                elite ? 5.8F : 5.0F, elite ? 4.7F : 3.8F, 5.8F);
        add(rightArm, 40, 16, -2.8F, 5.1F, -2.8F, 4.5F, 3.7F, 5.2F);
        add(leftArm, 40, 16, -1.7F, 5.1F, -2.8F, 4.5F, 3.7F, 5.2F);

        // Knee caps and hip tassets are attached to the separate moving legs.
        add(rightLeg, 0, 16, -2.35F, 2.0F, -2.6F, 4.7F, 4.0F, 1.2F);
        add(leftLeg, 0, 16, -2.35F, 2.0F, -2.6F, 4.7F, 4.0F, 1.2F);
        add(rightLeg, 16, 16, -2.4F, 6.4F, -2.7F, 4.8F, 2.2F, 1.3F);
        add(leftLeg, 16, 16, -2.4F, 6.4F, -2.7F, 4.8F, 2.2F, 1.3F);

        // Armored boot toe extends a little ahead of vanilla player feet.
        add(rightLeg, 32, 16, -2.4F, 9.0F, -3.0F, 4.8F, 3.4F, elite ? 6.0F : 5.5F);
        add(leftLeg, 32, 16, -2.4F, 9.0F, -3.0F, 4.8F, 3.4F, elite ? 6.0F : 5.5F);
    }

    private void add(ModelRenderer parent, int u, int v,
                     float x, float y, float z, float w, float h, float d) {
        ModelRenderer piece = new ModelRenderer(this, u, v);
        piece.addBox(x, y, z, w, h, d);
        parent.addChild(piece);
    }

    /**
     * Reuse one model per material and inner/outer armor layer. The game asks for
     * one equipment slot at a time, so all other model parts must be hidden here.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <A extends BipedModel<?>> A forSlot(boolean elite, EquipmentSlotType slot, A original) {
        KryzoraeArmorModel model;
        if (elite) {
            if (slot == EquipmentSlotType.LEGS) {
                if (executionerInner == null) executionerInner = new KryzoraeArmorModel(true, 0.5F);
                model = executionerInner;
            } else {
                if (executionerOuter == null) executionerOuter = new KryzoraeArmorModel(true, 1.0F);
                model = executionerOuter;
            }
        } else {
            if (slot == EquipmentSlotType.LEGS) {
                if (forgeInner == null) forgeInner = new KryzoraeArmorModel(false, 0.5F);
                model = forgeInner;
            } else {
                if (forgeOuter == null) forgeOuter = new KryzoraeArmorModel(false, 1.0F);
                model = forgeOuter;
            }
        }
        ((BipedModel) original).copyPropertiesTo(model);
        model.setAllVisible(false);
        switch (slot) {
            case HEAD:
                model.head.visible = true;
                break;
            case CHEST:
                model.body.visible = true;
                model.rightArm.visible = true;
                model.leftArm.visible = true;
                break;
            case LEGS:
            case FEET:
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
                break;
            default:
                break;
        }
        return (A) model;
    }
}
