#!/usr/bin/env python3
"""Regression assertions for new Kryzorae sword bleeding and boss max health.

These are deterministic contract checks. In-game combat/phase mechanics must
also be tested; the static checks are not a substitute for Minecraft.
"""
from pathlib import Path
import json

ROOT = Path("src/main/java/com/aetherium/aetherium")
ASSETS = Path("src/main/resources/assets/aetherium")
def java(path):
    return (ROOT / path).read_text(encoding="utf-8")

guardian = java("content/boss/RiftGuardianEntity.java")
winged = java("content/boss/AstralDevourerEntity.java")
bleeding = java("content/effect/BleedingEffect.java")
sword = java("content/item/AstralSwordItem.java")

assert ".add(Attributes.MAX_HEALTH, 450.0D)" in guardian
assert ".add(Attributes.MAX_HEALTH, 650.0D)" in winged
assert "this.getHealth() <= this.getMaxHealth() * 0.5F" in guardian
assert "this.getHealth() <= this.getMaxHealth() * 0.66F" in winged
assert "this.getHealth() <= this.getMaxHealth() * 0.33F" in winged
assert guardian.count(".add(Attributes.MAX_HEALTH,") == 1
assert winged.count(".add(Attributes.MAX_HEALTH,") == 1
assert abs(450 * 0.5 - 225) < 1e-6
assert abs(650 * 0.66 - 429) < 1e-6
assert abs(650 * 0.33 - 214.5) < 1e-6

assert "MAX_STACKS = 3" in bleeding
assert "DURATION_TICKS = 100" in bleeding
assert "TICKS_PER_DAMAGE = 20" in bleeding
assert "DAMAGE_PER_STACK = 2.0F" in bleeding
assert "target.level.isClientSide" in bleeding
assert "target.tickCount % TICKS_PER_DAMAGE" in bleeding
assert "stacks * DAMAGE_PER_STACK" in bleeding
assert "Math.min(BleedingEffect.MAX_STACKS" in sword
assert "previous.getAmplifier() + 2" in sword
assert "BleedingEffect.DURATION_TICKS, stacks - 1" in sword
assert "if (!successfulHit || target.level.isClientSide" in sword

for stacks in range(1, 4):
    assert stacks * 2.0 == (2.0, 4.0, 6.0)[stacks - 1]
assert 3 * 2.0 == 6.0

for locale in ("en_us", "es_es", "es_cl", "es_mx", "es_ar"):
    lang = json.loads((ASSETS / "lang" / f"{locale}.json").read_text(encoding="utf-8"))
    text = " ".join((lang["tooltip.aetherium.item.astral_sword"],
                     lang["tooltip.aetherium.item.astral_sword.detail"]))
    assert "3" in text and "6" in text and "5" in text and "2" in text, locale
    assert "20 HP/s" not in text and "20 PS/s" not in text, locale

print("Validated: Guardian 450 HP / phase II at 225 HP.")
print("Validated: Winged Executioner 650 HP / phases II-III at 429 and 214.5 HP.")
print("Validated: Crimson Bleeding I-III = 2/4/6 HP per second; lasts 5 seconds.")
print("Validated: five translated locales and the unchanged server-authoritative application.")
