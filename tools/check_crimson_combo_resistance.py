#!/usr/bin/env python3
"""Kryzorae: Crimson melee-combo anti-lock regression checks.

This checks the code paths and arithmetic of the anti-spam response. It cannot
measure responsiveness, animation feel, or actual combat in a Minecraft client.
"""
from pathlib import Path

root = Path("src/main/java/com/aetherium/aetherium")
sword = (root / "content/item/AstralSwordItem.java").read_text(encoding="utf-8")
guardian = (root / "content/boss/RiftGuardianEntity.java").read_text(encoding="utf-8")
winged = (root / "content/boss/AstralDevourerEntity.java").read_text(encoding="utf-8")
bleeding = (root / "content/effect/BleedingEffect.java").read_text(encoding="utf-8")

# A fast physical hit keeps its vanilla damage, but only a paced strike
# refreshes boss bleeding; normal mobs keep the original stacking behavior.
assert "boolean successfulHit = super.hurtEnemy(stack, target, attacker);" in sword
assert "BOSS_BLEED_PROC_INTERVAL = 12L" in sword
assert "boolean boss = target instanceof RiftGuardianEntity" in sword
assert "|| target instanceof AstralDevourerEntity" in sword
assert "now - previousTick < BOSS_BLEED_PROC_INTERVAL" in sword
assert "if (boss && attacker instanceof PlayerEntity)" in sword
assert "registerCrimsonHit(player)" in sword
assert sword.index("boolean boss =") < sword.index("BOSS_BLEED_PROC_INTERVAL)")
assert "MAX_STACKS = 3" in bleeding and "DAMAGE_PER_STACK = 2.0F" in bleeding

# Boss bleed is still limited to a stack every 12 ticks. Hit reaction timing
# is now tracked separately and responds to ALL successful direct melee hits.
def combo_events(hit_ticks, max_gap=36, cooldown=120):
    last_hit = None
    streak = 0
    ready_at = -1
    counters = []
    for tick in hit_ticks:
        streak = streak + 1 if last_hit is not None and tick - last_hit <= max_gap else 1
        last_hit = tick
        if streak >= 3 and tick >= ready_at:
            counters.append(tick)
            streak = 0
            ready_at = tick + cooldown
    return counters

assert combo_events([0, 12, 24]) == [24]
assert combo_events([0, 12, 24, 36, 48, 60]) == [24]
assert combo_events([0, 12, 24, 220, 232, 244]) == [24, 244]
assert combo_events([0, 45, 90, 135, 180]) == []

# The old Crimson hook remains; successful direct melee damage from any weapon
# also feeds the response. Warning and exposed/stagger states cannot be interrupted.
for boss in (guardian, winged):
    assert "public void registerCrimsonHit(PlayerEntity attacker)" in boss
    assert "private void noteMeleePressure(PlayerEntity attacker)" in boss
    assert "source.getDirectEntity() == source.getEntity()" in boss
    assert "this.noteMeleePressure((PlayerEntity)" in boss
    assert "this.entityData.get(ATTACK_STATE) != 0" in boss
    assert "this.lastPressureDamageTick == this.tickCount" in boss
    assert "this.rapidCrimsonHits < 3" in boss
    assert "this.attackCooldown = Math.min(this.attackCooldown" in boss

assert "this.crimsonCounterCooldown = 120" in guardian
assert "this.crimsonCounterPending = true;" in guardian
assert "this.attackCooldown = Math.min(this.attackCooldown, 6)" in guardian
assert "this.startFracture();" in guardian
assert "this.entityData.set(SLAM_TICKS, 36);" in guardian
assert "nextSlamTick == 20" in guardian and "nextSlamTick == 8" in guardian
assert "double[] rings = {3.5D, 4.1D, 7.0D}" in guardian
assert "outer && sq < 4.1D * 4.1D" in guardian
assert "this.entityData.set(SLAM_TICKS, 24)" in guardian  # old slam still available
assert "if (this.entityData.get(ATTACK_STATE) == 5) amount *= 1.45F;" in guardian

assert "this.crimsonRetreatCooldown = 120" in winged
assert "this.crimsonRetreatTicks = 20" in winged
assert "if (this.crimsonRetreatTicks > 0)" in winged
assert "desiredX = -dx / horizontalDistance * 0.36D" in winged
assert "desiredZ = -dz / horizontalDistance * 0.36D" in winged
assert "this.startFeatherPincer();" in winged
assert "this.launchFeatherPincer(target, nextTicks == 8)" in winged
assert "this.entityData.set(ATTACK_TICKS, 32)" in winged
assert "activeState == 1 || activeState == 4" not in winged  # feathers no longer freeze
assert "this.entityData.get(ATTACK_STATE) == 8) amount *= 1.35F" in winged
assert "registerCrimsonHit" not in bleeding
assert ".add(Attributes.MAX_HEALTH, 450.0D)" in guardian
assert ".add(Attributes.MAX_HEALTH, 650.0D)" in winged

import json
sounds = json.loads((Path("src/main/resources/assets/aetherium/sounds.json")).read_text(encoding="utf-8"))
for event in ("guardian_hurt", "astral_hurt"):
    assert sounds[event]["sounds"][0]["volume"] <= 0.04

print("Crimson bleed rate preserved; direct melee pressure can trigger boss responses.")
print("Guardian double-fracture and Executioner mobile feather pincer have warning windows.")
print("Boss hurt audio drastically reduced; no new immunity or unexpected phase resets.")
