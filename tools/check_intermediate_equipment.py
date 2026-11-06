#!/usr/bin/env python3
"""Regression checks for early equipment, vanilla-biped armor and crafting."""
from pathlib import Path
from struct import unpack
import json

java=Path("src/main/java/com/aetherium/aetherium")
assets=Path("src/main/resources/assets/aetherium")
data=Path("src/main/resources/data/aetherium/recipes")
reg=(java/"registry/RegistryHandler.java").read_text(encoding="utf-8")
tiers=(java/"content/item/IntermediateItemTier.java").read_text(encoding="utf-8")
armor=(java/"content/armor/IntermediateArmorMaterial.java").read_text(encoding="utf-8")
bonuses=(java/"events/IntermediateSetEvents.java").read_text(encoding="utf-8")
parts=("helmet","chestplate","leggings","boots","sword","pickaxe","axe","shovel","hoe")
patterns={
    "helmet":["MMM","M M"],
    "chestplate":["M M","MMM","MMM"],
    "leggings":["MMM","M M","M M"],
    "boots":["M M","M M"],
    "sword":[" M "," M "," S "],
    "pickaxe":["MMM"," S "," S "],
    "axe":["MM ","MS "," S "],
    "shovel":[" M "," S "," S "],
    "hoe":["MM "," S "," S "],
}
assert "BRUMACERO(425, 6.5F, 2.0F, 2, 12)" in tiers
assert "UMBRACITA(950, 7.4F, 2.5F, 2, 17)" in tiers
assert "new int[]{3, 5, 6, 2}" in armor
assert "new int[]{2, 4, 6, 2}" in armor
assert "event.setAmount(event.getAmount() * 0.90F)" in bonuses
assert "0.10D, AttributeModifier.Operation.MULTIPLY_TOTAL" in bonuses
assert "if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide" in bonuses
assert "event.player.tickCount % 20 != 0" in bonuses
assert "movement.y * 1.05D" in bonuses
assert "level.isClientSide" in bonuses
assert "UUID.fromString" in bonuses

for material in ("brumacero","umbracita"):
    for part in parts:
        id=material+"_"+part
        assert f'ITEMS.register("{id}"' in reg, id
        model=json.loads((assets/"models/item"/(id+".json")).read_text(encoding="utf-8"))
        expected_parent="item/handheld" if part in parts[4:] else "item/generated"
        assert model["parent"]==expected_parent
        assert model["textures"]["layer0"]=="aetherium:item/"+id
        path=assets/"textures/item"/(id+".png")
        raw=path.read_bytes()
        assert raw[:8]==bytes([137,80,78,71,13,10,26,10]), id
        assert unpack(">II",raw[16:24])==(32,32), id
        d=json.loads((data/(id+".json")).read_text(encoding="utf-8"))
        assert d["type"]=="minecraft:crafting_shaped"
        assert d["pattern"]==patterns[part],id
        assert d["result"]["item"]=="aetherium:"+id
        assert d["key"]["M"]["item"]=="aetherium:"+material+"_ingot"
        if part in parts[4:]:
            assert d["key"]["S"]["item"]=="minecraft:stick"

    for layer in (1,2):
        path=assets/"textures/models/armor"/f"{material}_layer_{layer}.png"
        raw=path.read_bytes()
        assert raw[:8]==bytes([137,80,78,71,13,10,26,10])
        assert unpack(">II",raw[16:24])==(256,128)

    for loc in ("en_us","es_es","es_cl","es_mx","es_ar"):
        lang=json.loads((assets/"lang"/(loc+".json")).read_text(encoding="utf-8"))
        for part in parts:
            assert lang["item.aetherium."+material+"_"+part]
        assert lang["tooltip.aetherium."+material+"_set"]

# Do not change the items and resource identity of the approved endgame sets.
assert 'ITEMS.register("aetherium_sword"' in reg
assert 'ITEMS.register("astral_sword"' in reg
assert 'ITEMS.register("aetherium_helmet"' in reg
assert 'ITEMS.register("astral_helmet"' in reg

# New fitted armor model rather than the old vanilla biped texture overlay:
model_code=(java/"client/model/IntermediateArmorModel.java").read_text(encoding="utf-8")
armor_item=(java/"content/armor/IntermediateArmorItem.java").read_text(encoding="utf-8")
assert "extends BipedModel<LivingEntity>" in model_code
assert "KryzoraeArmorModel" not in model_code, "Do not alter approved endgame armor models"
for token in ("knightHelm();", "knightChest();", "sorcererHat();", "shadowRobe();",
              "cube(head,", "cube(body,", "cube(arm,", "cube(root,",
              "model.setAllVisible(false)", "copyPropertiesTo(model)",
              "model.head.visible = true", "model.rightArm.visible = true",
              "model.leftLeg.visible = true"):
    assert token in model_code, token
assert "new IntermediateArmorModel(true,true)" in model_code
assert "new IntermediateArmorModel(false,true)" in model_code
assert "256" in model_code and "128" in model_code
assert "private void sorcererHat()" in model_code
assert "private void hood()" not in model_code
assert "15.40F, 0.45F, 13.30F" in model_code, "Hat brim must be noticeably wider than the skull"
# The earlier 1.75px trim was too subtle. Highest crown point
# is now at -12.17px vs -15.65px originally: 3.48px visibly shorter.
# Brim and mask dimensions are unchanged.
assert "cube(head, LIGHT, ROW, -2.10F, -12.17F, -2.55F, 4.35F, 0.35F, 5.10F)" in model_code
assert "cube(head, LIGHT, ROW, -2.10F, -13.90F" not in model_code
assert "cube(head, FABRIC, ROW, -3.65F, -11.00F, -3.55F, 7.30F, 2.50F, 7.10F)" in model_code
assert "cube(head, FABRIC, ROW, -4.30F, -2.18F, -4.79F, 8.60F, 1.38F, 0.59F)" in model_code

assert "face is deliberately open" in model_code.lower(), "Ensure upper face remains exposed"
assert "new IntermediateArmorModel(true,true)" in model_code

assert "IntermediateArmorModel.forSlot(umbracita, slot, original)" in armor_item
for material in ("BRUMACERO", "UMBRACITA"):
    for part in ("helmet","chestplate","leggings","boots"):
        expected=f'new IntermediateArmorItem(IntermediateArmorMaterial.{material},'
        assert expected in reg
assert "new ArmorItem(IntermediateArmorMaterial." not in reg

generator=Path("tools/generate_custom_armor_art.py").read_text(encoding="utf-8")
assert 'a.png(ASSETS/"models"/"armor"/f"{name}_layer_{layer}.png")' in generator
assert "build_atlas(family)" in generator
# Coat redesign: these elements must be distinct and attached to normal biped
# pivots so the look survives walking, combat and first/third-person swaps.
for marker in (
        "private void shadowRobe()",
        "ModelRenderer strap = part(2.83F, 1.05F, -3.64F)",
        "strap.zRot = 0.43F",
        "cube(strap, DETAIL, ROW",
        "cube(body, LIGHT, ROW, -4.81F, 17.64F",
        "cube(body, FABRIC, ROW, -3.95F, 10.82F, 2.65F",
        "private void leg(ModelRenderer root, boolean shade, boolean leggings,",
        "leg(leftLeg, shade, leggings, true)",
        "leg(rightLeg, shade, leggings, false)",
        "cube(root, DETAIL, ROW, -2.44F, 3.10F",
        "cube(root, LIGHT, ROW, -1.36F, 11.05F",
        "cube(arm, DETAIL, ROW, -2.89F, 8.42F",
):
    assert marker in model_code, "Missing fitted cloth detail: " + marker
assert "if (left)" in model_code
assert "tickCount" not in model_code, "The outfit must have no extra per-tick animation"
assert "minecraft.client.renderer" not in generator
for marker in ("Long OPEN COAT", "Fitted dark trousers", "Fashionable ankle boots",
               "Woven folds, stitched borders", "painted into the prebuilt atlas"):
    assert marker in generator, "Missing visual detail: "+marker


for loc in ("en_us", "es_es", "es_cl", "es_mx", "es_ar"):
    lang=json.loads((assets/"lang"/(loc+".json")).read_text(encoding="utf-8"))
    assert ("Hat" if loc=="en_us" else "Sombrero") in lang["item.aetherium.umbracita_helmet"]
icon_generator=Path("tools/generate_custom_armor_art.py").read_text(encoding="utf-8")
assert "Miniature of the actual wide-brimmed" in icon_generator
assert "[(9,15),(10,10),(14,8),(22,8),(25,11),(24,16)]" in icon_generator
print("18 registered crafted items, 18 original sprite textures, four fitted 256x128 worn atlases, five languages and server-light set bonuses validated.")
