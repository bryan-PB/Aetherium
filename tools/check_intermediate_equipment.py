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
assert "event.setAmount(event.getAmount() * 0.82F)" in bonuses
assert "0.15D, AttributeModifier.Operation.MULTIPLY_TOTAL" in bonuses
assert "if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide" in bonuses
assert "event.player.tickCount % 20 != 0" in bonuses
assert "movement.y * 1.15D" in bonuses
assert "level.isClientSide" in bonuses
assert "BRUMACERO_STABILITY_ID" in bonuses
assert "0.25D, AttributeModifier.Operation.ADDITION" in bonuses
assert "Attributes.KNOCKBACK_RESISTANCE" in bonuses
assert "stability.addTransientModifier(BRUMACERO_STABILITY)" in bonuses
assert "stability.removeModifier(BRUMACERO_STABILITY_ID)" in bonuses
assert "speed.removeModifier(UMBRACITA_SPEED_ID)" in bonuses
assert "event.player.tickCount % 20 != 0" in bonuses
assert "player.displayClientMessage(new TranslationTextComponent(" in bonuses
# The jump must run on client and server; otherwise prediction can hide it.
jump_handler=bonuses.split("public static void onJump(",1)[1]
assert "level.isClientSide" not in jump_handler

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
        assert unpack(">II",raw[16:24])==((64,64) if part in parts[4:] else (32,32)), id
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
        assert lang["message.aetherium."+material+"_set_active"]
        assert "+15" in lang["tooltip.aetherium.umbracita_set"] if material=="umbracita" else "18" in lang["tooltip.aetherium.brumacero_set"]

# The source sprites of both swords must remain truly distinct 64x64 PNGs.
# The build must NOT replace them with the earlier generic 32x32 swords.
steel=(assets/"textures/item/brumacero_sword.png").read_bytes()
shade=(assets/"textures/item/umbracita_sword.png").read_bytes()
assert steel != shade, "Different materials require different silhouettes"
assert len(steel) > 400 and len(shade) > 400, "Sword art seems truncated"
art_generator=Path("tools/generate_intermediate_equipment_art.py").read_text(encoding="utf-8")
assert 'for piece in ("pickaxe","axe","shovel","hoe"):' not in art_generator
assert 'for piece in ("sword","pickaxe"' not in art_generator
assert "10 source-controlled 64px weapon/tool PNGs preserved" in art_generator

# Inspect actual committed images, not a mock concept poster. Ensure every
# PNG has a valid zlib stream and CRC, transparency, and distinct per-part art.
import hashlib
import zlib
def validate_rgba_png(path):
    raw=path.read_bytes()
    cursor=8
    compressed=bytearray()
    while cursor < len(raw):
        length=unpack(">I",raw[cursor:cursor+4])[0]
        tag=raw[cursor+4:cursor+8]
        payload=raw[cursor+8:cursor+8+length]
        assert zlib.crc32(tag+payload) & 0xffffffff == unpack(">I",raw[cursor+8+length:cursor+12+length])[0], path
        if tag == b"IDAT":
            compressed.extend(payload)
        cursor += 12+length
        if tag == b"IEND": break
    pixels=zlib.decompress(compressed)
    assert len(pixels)==64*(1+64*4), path
    # 32 logical art pixels, losslessly doubled to 64: details cannot
    # degenerate into single-pixel fragments invisible in Minecraft's hotbar.
    assert all(pixels[y*257] == 0 for y in range(64)), path
    for y in range(0,64,2):
        first=pixels[y*257+1:(y+1)*257]
        second=pixels[(y+1)*257+1:(y+2)*257]
        assert first == second, "Partial vertical art pixel: " + str(path)
        for x in range(0,64,2):
            assert first[x*4:x*4+4] == first[(x+1)*4:(x+2)*4], (
                "Subpixel texture detail lost in hotbar: " + str(path))

    alpha=[pixels[y*(1+64*4)+1+x*4+3] for y in range(64) for x in range(64)]
    assert 200 < sum(a!=0 for a in alpha) < 3000, path
    assert 0 in alpha and 255 in alpha
    return hashlib.sha256(raw).hexdigest()

all_tool_hashes=set()
for material in ("brumacero","umbracita"):
    for part in ("sword","pickaxe","axe","shovel","hoe"):
        path=assets/"textures/item"/f"{material}_{part}.png"
        digest=validate_rgba_png(path)
        assert digest not in all_tool_hashes, "Duplicate tool sprite: "+str(path)
        all_tool_hashes.add(digest)
assert len(all_tool_hashes)==10


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
# Brumacero refinement remains a real articulated three-dimensional kit,
# not recolored vanilla armor. Enforce the separate visor, breastplates,
# abdominal lames, overlapping shoulders, kneecaps and armored toe.
for marker in (
        "private void knightHelm()",
        "private void knightChest()",
        "cube(head, FACE, ROW, -3.1F, -5.55F",
        "cube(head, ACCENT, 0, -0.60F, -10.28F",
        "cube(body, LIGHT, ROW, -4.27F, 1.53F",
        "cube(body, LIGHT, ROW, 0.61F, 1.53F",
        "cube(body, ACCENT, 0, -0.79F, 2.64F",
        "cube(body, PLATE, 0, -3.75F, 6.57F",
        "cube(body, PLATE, 0, -3.63F, 8.28F",
        "cube(arm, PLATE, 0, -3.88F, -3.62F",
        "cube(arm, PLATE, 0, -3.65F, -1.26F",
        "cube(root, PLATE, 0, -2.65F, 6.40F",
        "cube(root, PLATE, 0, -2.48F, 11.01F",
):
    assert marker in model_code, "Missing Brumacero armor detail: " + marker
assert "if (left)" in model_code
assert "tickCount" not in model_code, "The outfit must have no extra per-tick animation"
assert "minecraft.client.renderer" not in generator
for marker in ("Long OPEN COAT", "Fitted dark trousers", "Fashionable ankle boots",
               "Woven folds, stitched borders", "painted into the prebuilt atlas"):
    assert marker in generator, "Missing visual detail: "+marker
for marker in ("Polished forged steel", "closed noble helmet",
               "Gilded greave cuffs", "Layered symmetrical breast",
               "Separate shaped upper plates"):
    assert marker.lower() in generator.lower(), marker



for loc in ("en_us", "es_es", "es_cl", "es_mx", "es_ar"):
    lang=json.loads((assets/"lang"/(loc+".json")).read_text(encoding="utf-8"))
    assert ("Hat" if loc=="en_us" else "Sombrero") in lang["item.aetherium.umbracita_helmet"]
icon_generator=Path("tools/generate_custom_armor_art.py").read_text(encoding="utf-8")
assert "Miniature of the actual wide-brimmed" in icon_generator
assert "[(9,15),(10,10),(14,8),(22,8),(25,11),(24,16)]" in icon_generator
print("18 registered crafted items, 10 distinct 64px weapons/tools, eight armor sprites, four fitted 256x128 worn atlases, five languages and server-light set bonuses validated.")
