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
# All three new deposits become easier to find in newly generated chunks.
# Default 6x multiplier upgrades old configs transparently (2/1/1 -> 12/6/6).
assert 'NEW_ORE_DENSITY_MULTIPLIER' in conf
assert 'defineInRange("newOreDensityMultiplier", 6, 1, 8)' in conf
assert 'return Math.min(24, Math.max(0, configuredBase)' in gen
assert 'private static int expandedAttemptCount(int configuredBase)' in gen
for material in ("BRUMACERO", "UMBRACITA", "PRISMALITA"):
    expected = f"expandedAttemptCount(AetheriumConfig.{material}_VEINS_PER_CHUNK.get())"
    assert expected in gen, expected
assert "expandedAttemptCount(AetheriumConfig.ORE_VEINS_PER_CHUNK" not in gen, "Aetherium must stay rare"
for old, expected in ((2, 12), (1, 6), (0, 0), (6, 24)):
    assert min(24, max(0, old) * 6) == expected

for name in ["brumacero_ore","umbracita_ore","prismalita_ore","raw_brumacero","raw_umbracita","brumacero_ingot","umbracita_ingot","prismalita_shard"]:
    folder="block" if name.endswith("_ore") else "item"
    tex=r/"assets/aetherium/textures"/folder/(name+".png")
    raw=tex.read_bytes()
    assert raw[:8]==bytes([137,80,78,71,13,10,26,10])
    # Approved materials are richer static 48px sprites; the block remains 32px.
    assert unpack(">II",raw[16:24])==((32,32) if name.endswith("_ore") else (48,48)), name
# Asset regression: Minecraft's magenta/black blocks mean an ore model
# references a PNG that was omitted from the packaged resources.
# Check BOTH the four side textures AND their separate upper faces.
for ore in ("aetherium_ore", "brumacero_ore", "umbracita_ore", "prismalita_ore"):
    model=json.loads((r/"assets/aetherium/models/block"/(ore+".json")).read_text(encoding="utf-8"))
    assert model["parent"]=="minecraft:block/cube_bottom_top"
    for face in ("side","top","bottom"):
        texture=model["textures"][face]
        assert texture.startswith("aetherium:block/"), (ore,face,texture)
        png=r/"assets/aetherium/textures/block"/(texture.split("/")[-1]+".png")
        assert png.is_file(), "Missing "+str(png)
        data=png.read_bytes()
        assert data[:8]==bytes([137,80,78,71,13,10,26,10])
        assert unpack(">II",data[16:24])==(32,32), str(png)

# Lock seven approved resource sprites into the source repository, so CI
# reproduces the user-approved materials without manual post-build overlays.
import hashlib
approved_materials = {
    "brumacero_ingot": "4eba8741fcfc14a4349d30fadc0ab7d55cd90e893879a6bfc5c6a78b06fe5afb",
    "raw_brumacero": "7533112262ebdff20a64cb3e1e2557a5e4f238764478e97f0e68b0c313df8ccd",
    "umbracita_ingot": "44e5ed3de59ca051267bb0484b5d5a21777c13210786ecef0d25a39a4452d4d8",
    "raw_umbracita": "6e9255d141c13e5a9560ed7e03f3e0881e7214410e5b59249015b605b896a6ec",
    "prismalita_shard": "f964fc2725ddbfad7cf787f6aacd1a46c5d9e5eb95490e4363edfc23311bcea4",
    "aetherium_ingot": "49e7a292459aa559189db27465097180f3270005b27712e9c725c53874998df5",
    "raw_aetherium": "73c7b5461b676e9748b3ee6e0b355f321516d111cfab0421f8f73cee7be8a4cb",
}
for item, expected in approved_materials.items():
    p = r / "assets/aetherium/textures/item" / (item + ".png")
    data = p.read_bytes()
    assert unpack(">II", data[16:24]) == (48,48), item
    assert hashlib.sha256(data).hexdigest() == expected, "Approved material replaced: " + item

for loc in ("en_us","es_es","es_cl","es_mx","es_ar"):
    d=json.loads((r/"assets/aetherium/lang"/(loc+".json")).read_text(encoding="utf-8"))
    for name in materials:
        assert d[f"block.aetherium.{name}_ore"]
    for name in ["raw_brumacero","brumacero_ingot","raw_umbracita","umbracita_ingot","prismalita_shard"]:
        assert d[f"item.aetherium.{name}"]
print("First mineral expansion verified: registry, tier, worldgen, density, drops, textures and locales.")
