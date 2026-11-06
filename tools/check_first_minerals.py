#!/usr/bin/env python3
"""Check first three ores, recipes, texture signatures, tool tiers and locale keys."""
from pathlib import Path
from struct import unpack
import json
r=Path("src/main/resources")
j=Path("src/main/java/com/aetherium/aetherium")
reg=(j/"registry/RegistryHandler.java").read_text(encoding="utf-8")
gen=(j/"world/AetheriumWorldGen.java").read_text(encoding="utf-8")
conf=(j/"config/AetheriumConfig.java").read_text(encoding="utf-8")
materials=["brumacero","umbracita","prismalita"]
for name,level in [("brumacero",1),("umbracita",2),("prismalita",2)]:
    assert f'BLOCKS.register("{name}_ore"' in reg
    segment=reg.split(f'BLOCKS.register("{name}_ore"',1)[1].split("));",1)[0]
    assert f".harvestLevel({level})" in segment
    assert f'"{name}_ore"' in gen and f'configured{name.capitalize()}Ore' in gen
    assert f"{name.upper()}_VEINS_PER_CHUNK" in conf
    assert f"{name.upper()}_VEIN_SIZE" in conf
    assert f"{name.upper()}_MIN_Y" in conf and f"{name.upper()}_MAX_Y" in conf
    for folder in ("blockstates","models/block","models/item"):
        path=r/"assets/aetherium"/folder/f"{name}_ore.json"
        assert json.loads(path.read_text(encoding="utf-8"))
    loot=json.loads((r/"data/aetherium/loot_tables/blocks"/f"{name}_ore.json").read_text())
    entries=loot["pools"][0]["entries"][0]["children"]
    assert entries[0]["name"]==f"aetherium:{name}_ore"
    assert entries[1]["name"]==f"aetherium:{'prismalita_shard' if name=='prismalita' else 'raw_'+name}"
for name in ("brumacero","umbracita"):
    for item in (f"raw_{name}",f"{name}_ingot"):
        assert f'ITEMS.register("{item}"' in reg
    for smelt in ("smelting","blasting"):
        d=json.loads((r/"data/aetherium/recipes"/f"{name}_ingot_{smelt}.json").read_text())
        assert d["result"]==f"aetherium:{name}_ingot"
        assert d["ingredient"]["item"]==f"aetherium:raw_{name}"
assert 'ITEMS.register("prismalita_shard"' in reg
assert "Biome.Category.THEEND" in gen and "Biome.Category.NETHER" in gen
assert 'configuredAetheriumOre = registerOre("aetherium_ore"' in gen
for name in ["brumacero_ore","umbracita_ore","prismalita_ore","raw_brumacero","raw_umbracita","brumacero_ingot","umbracita_ingot","prismalita_shard"]:
    folder="block" if name.endswith("_ore") else "item"
    tex=r/"assets/aetherium/textures"/folder/(name+".png")
    raw=tex.read_bytes()
    assert raw[:8]==bytes([137,80,78,71,13,10,26,10])
    assert unpack(">II",raw[16:24])==(16,16)
for loc in ("en_us","es_es","es_cl","es_mx","es_ar"):
    d=json.loads((r/"assets/aetherium/lang"/(loc+".json")).read_text(encoding="utf-8"))
    for name in materials:
        assert d[f"block.aetherium.{name}_ore"]
    for name in ["raw_brumacero","brumacero_ingot","raw_umbracita","umbracita_ingot","prismalita_shard"]:
        assert d[f"item.aetherium.{name}"]
print("First mineral expansion verified: registry, tool level, worldgen, loot, textures, smelting and locales.")
