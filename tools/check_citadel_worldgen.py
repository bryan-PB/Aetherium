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
    "JigsawManager.addPieces(",
    '"citadel/start"',
    "AbstractVillagePiece::new",
    "this.calculateBoundingBox()",
    "Heightmap.Type.WORLD_SURFACE_WG",
    "GenerationStage.Decoration.SURFACE_STRUCTURES",
    "if (this.pieces.isEmpty()) return;",
    "new VillageConfig(",
):
    assert text in structure, text
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
print("Native Citadel worldgen registry verified: Overworld, jigsaw pools, biome and spacing.")
