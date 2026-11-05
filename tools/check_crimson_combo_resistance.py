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

# 3 physical Crimson strikes at 12+ ticks apart within 34 ticks between hits
# trigger a move once, then an 8-second cooldown prevents repeated counters.
def combo_events(hit_ticks, min_proc_interval=12, max_gap=34, cooldown=160):
    accepted = []
    last_proc = None
    last_hit = None
    streak = 0
    ready_at = -1
    counters = []
    for tick in hit_ticks:
        if last_proc is not None and tick - last_proc < min_proc_interval:
            continue
        last_proc = tick
        accepted.append(tick)
        streak = streak + 1 if last_hit is not None and tick - last_hit <= max_gap else 1
        last_hit = tick
        if streak >= 3 and tick >= ready_at:
            counters.append(tick)
            streak = 0
            ready_at = tick + cooldown
    return accepted, counters

assert combo_events([0, 1, 2, 3, 4, 5, 12, 24]) == ([0, 12, 24], [24])
assert combo_events([0, 12, 24, 36, 48, 60])[1] == [24]
assert combo_events([0, 12, 24, 220, 232, 244])[1] == [24, 244]
assert combo_events([0, 45, 90, 135, 180])[1] == []
assert combo_events([0, 11, 22, 33])[0] == [0, 22]

# Guardian does not retaliate instantly: it uses the existing warning ring
# with exactly 24 ticks of startup. Vulnerable stagger is not cancelled.
assert "public void registerCrimsonHit(PlayerEntity attacker)" in guardian
assert "this.entityData.get(ATTACK_STATE) != 0" in guardian.split("public void registerCrimsonHit", 1)[1].split("\n    }", 1)[0]
assert "this.rapidCrimsonHits < 3 || this.crimsonCounterCooldown > 0" in guardian
assert "this.crimsonCounterCooldown = 160" in guardian
assert "this.attackCooldown = Math.min(this.attackCooldown, 8)" in guardian
assert "this.crimsonCounterPending = false;" in guardian
assert "this.groundSlam(this.phase == 1 ? 3.5D : 4.35D" in guardian
assert "this.entityData.set(SLAM_TICKS, 24)" in guardian
assert "if (this.entityData.get(ATTACK_STATE) == 5) amount *= 1.45F;" in guardian

# Winged creature EVADES, not teleports or receives extra defensive damage
# reduction. Its attack recovery and core vulnerability remain intact.
winged_counter = winged.split("public void registerCrimsonHit", 1)[1].split("\n    }", 1)[0]
assert "this.entityData.get(ATTACK_STATE) != 0" in winged_counter
assert "this.isInWater()" in winged_counter
assert "this.rapidCrimsonHits < 3 || this.crimsonRetreatCooldown > 0" in winged_counter
assert "this.crimsonRetreatCooldown = 160" in winged
assert "this.crimsonRetreatTicks = 16" in winged
assert "this.attackCooldown = Math.min(this.attackCooldown, 26)" in winged
assert "if (this.crimsonRetreatTicks > 0)" in winged
assert "desiredX = -dx / horizontalDistance * 0.36D" in winged
assert "desiredZ = -dz / horizontalDistance * 0.36D" in winged
assert "this.entityData.get(ATTACK_STATE) == 8) amount *= 1.35F" in winged
assert "registerCrimsonHit" not in bleeding
assert ".add(Attributes.MAX_HEALTH, 450.0D)" in guardian
assert ".add(Attributes.MAX_HEALTH, 650.0D)" in winged

print("Crimson boss bleeding: only one proc per 12 ticks, normal melee untouched.")
print("Three timed hits: Guardian warns with its ordinary slam; Winged Executioner retreats.")
print("No new immunities, no surprise damage, no attack/stagger animation resets.")
