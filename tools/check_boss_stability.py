#!/usr/bin/env python3
"""Regression checks for low-overhead Guardian water escape and illusion lifespan."""
from pathlib import Path
import re

root = Path("src/main/java/com/aetherium/aetherium")
guardian = (root / "content/boss/RiftGuardianEntity.java").read_text(encoding="utf-8")
winged = (root / "content/boss/AstralDevourerEntity.java").read_text(encoding="utf-8")
mirage = (root / "content/boss/BossMirageEntity.java").read_text(encoding="utf-8")
violet = (root / "content/item/AetheriumSwordItem.java").read_text(encoding="utf-8")
crimson = (root / "content/item/AstralSwordItem.java").read_text(encoding="utf-8")

tick = guardian.split("public void tick()", 1)[1].split("public static", 1)[0]
water = tick.split("if (this.isInWater())", 1)[0]
assert "if (this.level.isClientSide) return;" in water, "Water search must only run on server"
assert "private BlockPos waterExitTarget;" in guardian
assert "private int waterSearchCooldown = 0;" in guardian
assert "if (this.waterSearchCooldown <= 0)" in guardian
assert "this.waterSearchCooldown = 25;" in guardian
assert "this.waterExitTarget = null;" in guardian
assert "this.findNearestDryPosition();" in guardian
assert "motion.x * 0.55D + shoreX / shoreLength * 0.20D" in guardian

# Attackable decoys remain alive even when the parent takes damage.
hurt = guardian.split("public boolean hurt(DamageSource source, float amount)", 1)[1]
hurt = hurt.split("public void startSeenByPlayer", 1)[0]
assert "this.dispelMirage();" not in hurt
assert "this.noteMeleePressure((PlayerEntity) source.getEntity());" in hurt
assert "this.dispelMirage();" in guardian.split("public void die(DamageSource source)", 1)[1]
assert "this.expire();" in mirage.split("public boolean hurt(DamageSource source", 1)[1]
assert "this.onMirageDispelled" not in guardian

# Don't accidentally remove the other boss' established safe mirages or
# reintroduce unrestricted crimson bleeding while stabilizing.
winged_hurt = winged.split("public boolean hurt(DamageSource source", 1)[1]
winged_hurt = winged_hurt.split("public void startSeenByPlayer", 1)[0]
assert "this.dispelMirage();" not in winged_hurt
assert "this.crimsonRetreatCooldown = 120" in winged
assert "MIN_TICKS_BETWEEN_SLASHES = 9" in violet
assert "BOSS_BLEED_PROC_INTERVAL = 12L" in crimson
print("Guardian: server-only cached water escapes; decoys survive damage to their owner.")
print("Winged: existing safe illusions and sword anti-combo cooldowns preserved.")
