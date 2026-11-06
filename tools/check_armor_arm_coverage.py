#!/usr/bin/env python3
"""Static Forge 1.16.5 armor regression check for visible player sleeves.

This is a geometric containment test, NOT an in-game rendering test. Minecraft's
right arm cube is x=-3..1 and left is x=-1..3 relative to the respective biped
pivot, both at y=-2..10 and z=-2..2. The vanilla secondary skin/sleeve layer is
inflated by 0.25px, and the armor mesh must fully cover even that outer layer.
"""

from pathlib import Path

path = Path("src/main/java/com/aetherium/aetherium/client/model/KryzoraeArmorModel.java")
source = path.read_text(encoding="utf-8")
arm = source.split("    private void buildArm(", 1)[1].split("    private void buildLeg(", 1)[0]

for required in (
    "final float vanillaOffsetX = left ? 1.0F : -1.0F;",
    "arm.addChild(sleeve);",
    "box(sleeve, RECESS, 64, -3.00F, -3.05F, -3.00F,",
    "6.00F, 15.20F, 6.00F);",
    "box(sleeve, LIMB, 0, -2.88F, -2.85F, -2.88F,",
    "5.76F, 14.80F, 5.76F);",
):
    if required not in arm:
        raise SystemExit("Armor arm coverage requirement is missing: " + required)

if "box(arm," in arm:
    raise SystemExit("Unshifted armor part remains attached directly to arm pivot")

for side, center, vanilla_min_x, vanilla_max_x in (
    ("right", -1.0, -3.0, 1.0),
    ("left", 1.0, -1.0, 3.0),
):
    margin = 0.25  # skin/sleeve outer layer inflation
    bounds = {
        "x": (center - 2.88, center + 2.88,
              vanilla_min_x - margin, vanilla_max_x + margin),
        "y": (-2.85, -2.85 + 14.80, -2.0 - margin, 10.0 + margin),
        "z": (-2.88, -2.88 + 5.76, -2.0 - margin, 2.0 + margin),
    }
    for axis, (armor_min, armor_max, skin_min, skin_max) in bounds.items():
        if armor_min > skin_min or armor_max < skin_max:
            raise SystemExit(
                f"{side} {axis}: armor [{armor_min}, {armor_max}] does not cover "
                f"skin [{skin_min}, {skin_max}]"
            )
    print(f"{side} arm: custom armor cube covers the vanilla sleeve in x/y/z")

print("Arm model offsets and conservative skin coverage validated; "
      "actual Minecraft animations still need visual verification.")
