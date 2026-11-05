#!/usr/bin/env python3
"""Static preflight for the Kryzorae boss readability overhaul.

These checks protect projectiles, clone counts and dash warning state from
silent regressions. They DO NOT stand in for playing Minecraft.
"""

from pathlib import Path

ROOT = Path("src/main/java/com/aetherium/aetherium")
def read(path):
    return (ROOT / path).read_text(encoding="utf-8")

winged = read("content/boss/AstralDevourerEntity.java")
guardian = read("content/boss/RiftGuardianEntity.java")
mirage = read("content/boss/BossMirageEntity.java")
mirage_renderer = read("client/renderer/BossMirageRenderer.java")
winged_renderer = read("client/renderer/AstralDevourerRenderer.java")
guardian_renderer = read("client/renderer/RiftGuardianRenderer.java")
winged_model = read("client/model/AstralDevourerModel.java")
guardian_model = read("client/model/RiftGuardianModel.java")
bolts = read("content/boss/AetherBoltEntity.java")
bolt_renderer = read("client/renderer/AetherBoltRenderer.java")
client = read("client/AetheriumClientEvents.java")

# Three independent, harmless mirrors for the Winged Executioner.
assert "for (int i = 0; i < 3; i++)" in winged.split("private void summonMirage()", 1)[1]
assert "this.activeMirages.add(fake.getId())" in winged
assert "this.activeMirages.remove(Integer.valueOf(fakeId))" in winged
assert "this.dispelMirage();" not in winged.split(
    "public boolean hurt(DamageSource source, float amount)", 1)[1].split("return damaged;", 1)[0]
assert "this.getCopySlot() + 1" in mirage
assert "target.getX() + Math.cos(theta)" in mirage
assert "target.getZ() + Math.sin(theta)" in mirage
assert "getCopySlot()" in mirage
assert "this.expiring" in mirage
assert "if (owner instanceof RiftGuardianEntity)" in mirage
assert "RenderType.entityTranslucent(WINGED_TEXTURE)" in mirage_renderer
assert "RenderType.entityTranslucent(GUARDIAN_TEXTURE)" in mirage_renderer
assert "lostHealth * 0.73F" in mirage_renderer
assert "OverlayTexture.NO_OVERLAY" in mirage_renderer

# Models render each fake independently, not within the real boss's culling box.
assert "BossMirageEntity" not in winged_renderer
assert "BossMirageEntity" not in guardian_renderer
assert "super.render(entity, yaw, partialTicks, stack, buffers, packedLight);" in winged_renderer

# Dash aim and mesh both follow one locked direction, with a visible tell.
assert "this.lockDashFacing();" in winged
assert "this.yBodyRot = yaw;" in winged
assert "this.yHeadRot = yaw;" in winged
assert "if (this.dashChargeTicks > 0)" in winged
assert "this.setDeltaMovement(Vector3d.ZERO);" in winged
assert "expandTowards(this.getDeltaMovement())" in winged
assert "RayTraceContext.BlockMode.COLLIDER" in winged
assert "dashReady * 0.70F + dash * 1.25F" in winged_model
assert "dash * 0.96F" in winged_model
assert "dash * 1.05F" in winged_model
assert "windRecoil" in guardian_model

# Both crystal sprites replaced by networked, independently rendered meshes.
assert "bolt.setStyle(AetherBoltEntity.FEATHER_BLADE);" in winged
assert "bolt.setStyle(AetherBoltEntity.WIND_SLICE);" in guardian
assert "bolt.setItem" not in winged
assert "bolt.setItem" not in guardian
assert "FEATHER_BLADE = 1" in bolts
assert "WIND_SLICE = 2" in bolts
assert "this.entityData.define(STYLE, FEATHER_BLADE);" in bolts
assert "NetworkHooks.getEntitySpawningPacket(this)" in bolts
assert "drawIronFeather" in bolt_renderer
assert "drawWindCrescent" in bolt_renderer
assert "RenderType.lightning()" in bolt_renderer
assert "AetherBoltRenderer::new" in client
assert "SpriteRenderer" not in client
assert "castCrystals" not in winged_model
assert "castFeathers" in winged_model

# Guardian holds still for 12 ticks before its first air cut.
assert "nextSlamTick == 16 || nextSlamTick == 7" in guardian
assert "state == 2" in guardian and "Vector3d.ZERO" in guardian
assert "this.sendWindCannonWarning();" in guardian

print("Validated 3 translucent Winged Executioner mirrors and safe decoy hits.")
print("Validated locked dash posture, warning direction and swept wall-safe collisions.")
print("Validated winged iron-feather meshes and Guardian compressed-wind crescents.")
print("Reminder: visual fidelity, audio and hit timing still require a Minecraft client.")
