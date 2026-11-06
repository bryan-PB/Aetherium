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
        assert unpack(">II",raw[16:24])==(128,64)

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
print("18 registered crafted items, 18 original sprite textures, four worn atlases, five languages and server-light set bonuses validated.")
