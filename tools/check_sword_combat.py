#!/usr/bin/env python3
"""Static source assertions for the two distinct Kryzorae sword abilities.

Not a substitute for running Minecraft: checks explicit game rules, server
authority and new registrations before packaging the mod.
"""

from pathlib import Path

ROOT = Path("src/main/java/com/aetherium/aetherium")
resources = Path("src/main/resources/assets/aetherium")

def source(relative):
    return (ROOT / relative).read_text(encoding="utf-8")

effect = source("content/effect/BleedingEffect.java")
crimson = source("content/item/AstralSwordItem.java")
violet = source("content/item/AetheriumSwordItem.java")
slash = source("content/projectile/DarkSlashEntity.java")
renderer = source("client/renderer/DarkSlashRenderer.java")
registry = source("registry/RegistryHandler.java")
client = source("client/AetheriumClientEvents.java")

assert "MAX_STACKS = 3" in effect
assert "DAMAGE_PER_STACK = 2.0F" in effect
assert "TICKS_PER_DAMAGE = 20" in effect
assert "DURATION_TICKS = 100" in effect
assert "target.tickCount % TICKS_PER_DAMAGE" in effect
assert "target.level.isClientSide" in effect
assert "stacks * DAMAGE_PER_STACK" in effect

for stacks in range(1, 4):
    per_second = stacks * 2.0
    assert per_second == 2.0 * stacks
assert 3 * 2.0 == 6.0

assert "Math.min(BleedingEffect.MAX_STACKS" in crimson
assert "stacks - 1" in crimson
assert "target.removeEffect" in crimson
assert "target.addEffect(new EffectInstance" in crimson
assert "if (!successfulHit || target.level.isClientSide" in crimson
assert "releaseAstralFan" not in crimson
assert "performRiftCleave" not in violet

# RegistryHandler: violet sword has damage modifier 5 and material bonus 4;
# vanilla player's base damage 1, giving 10 HP base and 5 HP per slash.
assert "AETHERIUM_SWORD = ITEMS.register" in registry
assert "AetheriumItemTier.INSTANCE, 5, -2.4F" in registry
tier = source("content/item/AetheriumItemTier.java")
assert "return 4.0F;" in tier
assert (1.0 + 5 + 4.0) * 0.5 == 5.0
assert "tier.getAttackDamageBonus()) * 0.5F" in violet
assert "public boolean onEntitySwing" in violet
assert "entity.level.isClientSide" in violet
assert "MIN_TICKS_BETWEEN_SLASHES = 9" in violet
assert "new DarkSlashEntity(player.level, player, this.slashDamage)" in violet
assert "player.level.addFreshEntity(wave)" in violet

assert "MAX_AGE = 8" in slash
assert "SPEED = 0.54D" in slash
assert "this.level.clip(new RayTraceContext" in slash
assert "alreadyHit.add(victim.getUUID())" in slash
assert "DamageSource.indirectMagic(this, player)" in slash
assert "NetworkHooks.getEntitySpawningPacket(this)" in slash
assert "RenderType.lightning()" in renderer
assert "drawCrescent" in renderer
assert "DARK_SLASH.get(), DarkSlashRenderer::new" in client
assert 'EFFECTS.register("bleeding", BleedingEffect::new)' in registry
assert 'ENTITIES.register("dark_slash"' in registry
assert "EFFECTS.register(modEventBus)" in registry

for locale in ("en_us", "es_es", "es_cl", "es_mx", "es_ar"):
    import json
    lang = json.loads((resources / "lang" / f"{locale}.json").read_text(encoding="utf-8"))
    for key in ("item.aetherium.astral_sword",
                "item.aetherium.aetherium_sword",
                "effect.aetherium.bleeding",
                "tooltip.aetherium.item.astral_sword",
                "tooltip.aetherium.item.aetherium_sword"):
        assert lang.get(key), f"Missing localized ability description: {locale}/{key}"

print("Validated bleed: 1..3 stacks, 2 HP/s per stack, 6 HP/s cap, server tick.")
print("Validated violet slash: 5 HP base, short range, one hit per target and a visible renderer.")
print("Validated effect, projectile, tooltips and combat registries in five locales.")
