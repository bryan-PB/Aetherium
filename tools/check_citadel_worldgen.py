#!/usr/bin/env python3
"""Guard the native Forge 1.16.5 overworld Citadel registration pipeline."""
from pathlib import Path
import re

root = Path("src/main/java/com/aetherium/aetherium")
structure=(root/"world/CitadelStructure.java").read_text()
gen=(root/"world/CitadelWorldGen.java").read_text()
main=(root/"AetheriumMod.java").read_text()
config=(root/"config/AetheriumConfig.java").read_text()
gradle=Path("build.gradle").read_text()
at=Path("src/main/resources/META-INF/accesstransformer.cfg").read_text()

for text in (
    'STRUCTURES.register("citadel"',
    "ForgeRegistries.STRUCTURE_FEATURES",
    "Structure.STRUCTURES_REGISTRY.put(",
    "WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE",
    "FlatGenerationSettings.STRUCTURE_FEATURES.put(",
    "DimensionStructuresSettings.DEFAULTS =",
    "WorldGenRegistries.NOISE_GENERATOR_SETTINGS.entrySet()",
    "world.getChunkSource().generator.getSettings().structureConfig",
    "World.OVERWORLD.equals(world.dimension())",
    "instanceof FlatChunkGenerator",
    "CitadelWorldGen::onBiomeLoading",
):
    assert text in gen or text in main, "Missing Forge 1.16.5 structure wiring: " + text
for text in (
    "new CitadelPieces.Room(",
    "this.calculateBoundingBox()",
    "Heightmap.Type.WORLD_SURFACE_WG",
    "GenerationStage.Decoration.SURFACE_STRUCTURES",
    "height < 60 || height > 126",
    "surface.getFluidState().isEmpty()",
):
    assert text in structure, text

pieces=(root/"world/CitadelPieces.java").read_text()
for phrase in (
    "extends TemplateStructurePiece",
    "IStructurePieceType.setPieceId(",
    "JigsawReplacementStructureProcessor.INSTANCE",
    'nbt.putString("CitadelTemplate"',
    'nbt.getString("CitadelTemplate")',
    "this.setup(template, position, placement())",
    "this.setup(manager.getOrCreate(this.templateId),",
):
    assert phrase in pieces, phrase
assert "CitadelPieces.register();" in gen
commands=(root/"world/CitadelCommands.java").read_text()
assert 'Commands.literal("kryzorae")' in commands
assert 'Commands.literal("citadel")' in commands
assert 'Commands.literal("status")' in commands
assert 'Commands.literal("probe")' in commands
assert "biome.getGenerationSettings()" in commands
assert ".isValidStart(CitadelWorldGen.CITADEL.get())" in commands
assert "CitadelStructure.canGenerateOnColumn(" in commands
assert "public static boolean canGenerateOnColumn(" in structure
assert "return canGenerateOnColumn(generator," in structure
assert 'Commands.literal("preview")' in commands
assert 'source.hasPermission(2)' in commands
assert 'new CitadelPieces.Room(' in structure
assert 'manager.get(new ResourceLocation(' in commands
assert 'JigsawReplacementStructureProcessor.INSTANCE' in commands
assert 'CitadelCommands::register' in main
assert "category == Biome.Category.OCEAN" in gen
assert "category == Biome.Category.NETHER" in gen
assert "category != Biome.Category.FOREST" not in gen

assert "JigsawManager.addPieces(" not in structure, (
    "Cannot fall back to the previous unreliable jigsaw-only layout")

# Seven fixed pieces have non-intersecting 2D volumes and guaranteed door
# connections. This is stronger than merely checking JSON pools exist.
import json, re
blueprint=json.loads(Path("docs/citadel/rooms.json").read_text())
spec={r["id"]:r for r in blueprint["rooms"]}
# Exactly one room layout powers both natural generation and the creative
# preview. Parse the production Java coordinates rather than comparing them to
# a second, manually copied Python dictionary that could hide discrepancies.
layout=(root/"world/CitadelLayout.java").read_text()
entries=re.findall(r'new RoomSpec[(]"([a-z_]+)",[ ]*(-?[0-9]+),[ ]*(-?[0-9]+)[)]',layout)
assert len(entries)==7, ("Expected seven Java room specs",entries)
positions={name:(int(x),int(z)) for name,x,z in entries}
assert len(positions)==len(entries), "Duplicate Citadel room IDs in shared layout"
assert set(positions)==set(spec), "Citadel layout differs from NBT room manifest"
assert "for (CitadelLayout.RoomSpec room : CitadelLayout.rooms())" in structure
assert "room.position(origin)" in structure
assert "CitadelLayout.originForChunk(chunkX, chunkZ, y)" in structure
assert "for (CitadelLayout.RoomSpec room : CitadelLayout.rooms())" in commands
assert "CitadelLayout.rooms().get(i).position(base)" in commands
assert "int[][] locations" not in commands, "Preview must not duplicate room offsets"
assert "player.getX(), z) + 1" not in commands, "Preview must use first free surface Y"
assert 'if (!templates.get(i).placeInWorld(' in commands, "Preview must report failed room placement"
boxes={k:(x,z,x+spec[k]["size"][0]-1,z+spec[k]["size"][2]-1)
       for k,(x,z) in positions.items()}
for a,aa in boxes.items():
    for b,bb in boxes.items():
        if a >= b: continue
        assert aa[2]<bb[0] or bb[2]<aa[0] or aa[3]<bb[1] or bb[3]<aa[1], (
            "Citadel rooms overlap",a,b)
chain=["gateway","corridor","guardhouse","archive","arena_shell"]
for a,b in zip(chain,chain[1:]):
    ax,az,ax2,az2=boxes[a]
    bx,bz,bx2,bz2=boxes[b]
    assert az2+1==bz
    assert (ax+ax2)//2 == (bx+bx2)//2
cx,cz,cx2,cz2=boxes["corridor"]
xx,xz,xx2,xz2=boxes["crypt"]
assert cx2+1==xx and (cz+cz2)//2==(xz+xz2)//2
gx,gz,gx2,gz2=boxes["guardhouse"]
fx,fz,fx2,fz2=boxes["forge"]
assert fx2+1==gx and (gz+gz2)//2==(fz+fz2)//2
assert "CitadelWorldGen.register(" in main
assert "CitadelWorldGen::onCommonSetup" in main
assert "CitadelWorldGen::onBiomeLoading" in main
assert "CitadelWorldGen::onWorldLoad" in main
assert '"citadelEnabled", true' in config
assert '"citadelSpacingChunks", 26, 16, 128' in config
assert '"citadelMinSeparationChunks", 10, 4, 16' in config
assert "accessTransformer = file(" in gradle
for field in ("field_236191_b_", "field_202247_j", "field_236193_d_"):
    assert field in at, "Missing 1.16.5 access transformer: "+field

# No unnatural mass generation or custom per-tick simulations in the Citadel.
assert re.search(r"\b(?:onTick|playerTick|chunkTick|EntityJoinWorldEvent)\b",gen) is None
print("Citadel worldgen verified: Overworld registration, seven non-overlapping deterministic NBT pieces, 3x4 connections, and spacing.")
