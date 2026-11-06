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
architecture=Path("tools/citadel_architecture.py").read_text()
nbt_generator=Path("tools/generate_citadel_templates.py").read_text()
assert "from citadel_architecture import design_room" in nbt_generator
assert "design_room(room, put)" in nbt_generator
assert "def design_room(room, put):" in architecture
assert "center_clear()" in architecture
assert "inputs.file('tools/citadel_architecture.py')" in gradle, (
    "Gradle incremental Citadel NBTs must rebuild when architecture changes")


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
    "MinecraftForge.EVENT_BUS.register(CitadelWorldGen.class)",
):
    assert text in gen or text in main, "Missing Forge 1.16.5 structure wiring: " + text
for text in (
    "new CitadelPieces.Room(",
    "this.calculateBoundingBox()",
    "Heightmap.Type.WORLD_SURFACE_WG",
    "GenerationStage.Decoration.SURFACE_STRUCTURES",
    "height < 60 || height > 126",
    "canGenerateMegaSite(generator, x, z)",
    "surface.getFluidState().isEmpty()",
):
    assert text in structure, text

pieces=(root/"world/CitadelPieces.java").read_text()
for phrase in (
    "extends TemplateStructurePiece",
    "IStructurePieceType.setPieceId(",
    "JigsawReplacementStructureProcessor.INSTANCE",
    "CitadelRotation",
    "CitadelMirror",
    "SUPPORT_DEPTH = 20",
    "postProcess(ISeedReader world",
    'nbt.putString("CitadelTemplate"',
    'nbt.getString("CitadelTemplate")',
    "this.setup(template, room.position(origin), placement(rotation, mirror))",
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
assert "int findPlacementY(ChunkGenerator generator, int x, int z)" in structure
assert "MAX_RELIEF = 5" in structure
assert "for (CitadelLayout.RoomSpec room : CitadelLayout.rooms())" in structure
assert "java.util.Collections.sort(centralHeights)" in structure
assert "Math.abs(height - chosenY) > MAX_RELIEF" in structure
assert "isDryGround(generator, sx, sz, height)" in structure
assert "return canGenerateMegaSite(generator, x, z);" in structure
assert 'Commands.literal("preview")' in commands
assert 'source.hasPermission(2)' in commands
assert 'new CitadelPieces.Room(' in structure
assert 'manager.get(new ResourceLocation(' in commands
assert 'JigsawReplacementStructureProcessor.INSTANCE' in commands
assert 'CitadelCommands::register' in main
assert "@SubscribeEvent(priority = EventPriority.HIGH)" in gen
assert "@SubscribeEvent" in gen
assert "category == Biome.Category.OCEAN" in gen
assert "category == Biome.Category.NETHER" in gen
assert "category != Biome.Category.FOREST" not in gen

assert "JigsawManager.addPieces(" not in structure, (
    "Cannot fall back to the previous unreliable jigsaw-only layout")

# The 17-room 3D blueprint must be chunk-safe, rotation-safe and fully
# connected. Parse the actual Java layout and compare it with the NBT manifest.
import json
blueprint=json.loads(Path("docs/citadel/rooms.json").read_text())
rooms={r["id"]:r for r in blueprint["rooms"]}
assert len(rooms)==17
layout=(root/"world/CitadelLayout.java").read_text()
regex=(r'new RoomSpec[(]"([a-z_]+)",[ ]*(-?[0-9]+),[ ]*(-?[0-9]+),'
       r'[ ]*(-?[0-9]+),[ ]*Rotation[.]([A-Z0-9_]+),[ ]*'
       r'Mirror[.]([A-Z0-9_]+),[ ]*([0-9]+),[ ]*([0-9]+)[)]')
entries=re.findall(regex,layout)
assert len(entries)==17, ("Expected 17 Java room specs",entries)
specs={name:(int(x),int(y),int(z),rot,mirror,int(w),int(d))
       for name,x,y,z,rot,mirror,w,d in entries}
assert len(specs)==17
assert set(specs)==set(rooms)
for name,(x,y,z,rot,mirror,w,d) in specs.items():
    room=rooms[name]
    assert [x,y,z]==room["offset"],("Offset drift",name)
    assert (w,d)==(room["size"][0],room["size"][2])
    assert rot==room["rotation"] and mirror==room["mirror"]

def transform(x,z,rotation,mirror):
    if mirror=="FRONT_BACK": x=-x
    if mirror=="LEFT_RIGHT": z=-z
    if rotation=="CLOCKWISE_90": return -z,x
    if rotation=="CLOCKWISE_180": return -x,-z
    if rotation=="COUNTERCLOCKWISE_90": return z,-x
    return x,z

def room_geometry(name):
    room=rooms[name]
    ox,oy,oz,rot,mirror,w,d=specs[name]
    transformed=[transform(x,z,rot,mirror)
                 for x in (0,w-1) for z in (0,d-1)]
    minx,minz=min(x for x,z in transformed),min(z for x,z in transformed)
    maxx,maxz=max(x for x,z in transformed),max(z for x,z in transformed)
    pivot=(ox-minx,oz-minz)
    return (ox,oy,oz,ox+maxx-minx,oy+room["size"][1]-1,oz+maxz-minz),pivot

boxes={name:room_geometry(name)[0] for name in rooms}
for a,aa in boxes.items():
    for b,bb in boxes.items():
        if a>=b: continue
        x=(aa[0]<=bb[3] and bb[0]<=aa[3])
        y=(aa[1]<=bb[4] and bb[1]<=aa[4])
        z=(aa[2]<=bb[5] and bb[2]<=aa[5])
        assert not (x and y and z),("Citadel 3D overlap",a,b)

# Door centers, including transformed east/west wings, must be exactly one
# block apart along their outward normal, with identical world Y.
directions={"north":(0,-1),"south":(0,1),"west":(-1,0),"east":(1,0)}
def ports(name):
    room=rooms[name]
    x,y,z,rot,mirror,w,d=specs[name]
    pivot=room_geometry(name)[1]
    door_positions={}
    for side,target in room["doors"].items():
        lx,lz=({"north":(w//2,0),"south":(w//2,d-1),
                 "east":(w-1,d//2),"west":(0,d//2)})[side]
        dx,dz=transform(lx,lz,rot,mirror)
        nx,nz=transform(*directions[side],rot,mirror)
        door_positions[target]=(pivot[0]+dx,y+2,pivot[1]+dz,nx,nz)
    return door_positions

for name,room in rooms.items():
    for target in room["doors"].values():
        assert target in rooms,(name,target)
        assert name in ports(target),("Missing reciprocal door",name,target)
        x,y,z,nx,nz=ports(name)[target]
        tx,ty,tz,tnx,tnz=ports(target)[name]
        assert (tx,ty,tz)==(x+nx,y,z+nz),("Misaligned door",name,target)
        assert (tnx,tnz)==(-nx,-nz),("Incompatible door direction",name,target)
        role=[v for k,v in room["doorRoles"].items()
              if room["doors"][k]==target]
        inverse=[v for k,v in rooms[target]["doorRoles"].items()
                 if rooms[target]["doors"][k]==name]
        assert len(role)==len(inverse)==1 and role[0]!=inverse[0], (
            "Entry/exit Jigsaw roles do not match",name,target)

# Upper rooms provide ladder shafts into distinct, non-overlapping undercrofts.
for upper,lower in (("crypt","catacombs"),("archive","archive_vault")):
    assert rooms[upper]["offset"][:1]==rooms[lower]["offset"][:1]
    assert rooms[upper]["offset"][2]==rooms[lower]["offset"][2]
    assert rooms[upper]["offset"][1]==0
    assert rooms[lower]["offset"][1]+rooms[lower]["size"][1]==0
assert "for (CitadelLayout.RoomSpec room : CitadelLayout.rooms())" in structure
assert "new CitadelPieces.Room(templates, room, origin)" in structure
assert "candidate.getBoundingBox()" in structure
assert "this.calculateBoundingBox()" in structure
assert "room.position(base)" in commands
assert "setRotation(room.rotation)" in commands
assert "setMirror(room.mirror)" in commands
assert "CitadelLayout.rooms().size()" in commands
assert "CitadelLayout.originForChunk(chunkX, chunkZ, y)" in structure
assert "int y = findPlacementY(" in structure
assert "generator, chunkX * 16 + 8, chunkZ * 16 + 8" in structure
assert "findPlacementY(" in commands
assert "new CitadelPieces.Room(manager, room, base)" in commands
assert ".placePreviewFoundations(world)" in commands
assert "private void placeFoundations(" in pieces
assert "supportColumn(world, chunkBox, x, bounds.z0, y);" in pieces
assert "supportColumn(world, chunkBox, x, bounds.z1, y);" in pieces
assert 'id.endsWith("/crypt") || id.endsWith("/archive")' in pieces
assert "chunkBox.isInside(below)" in pieces
assert "int[][] locations" not in commands
assert 'if (!templates.get(i).placeInWorld(' in commands
assert "CitadelWorldGen.register(" in main
assert "CitadelWorldGen::onCommonSetup" in main
assert "MinecraftForge.EVENT_BUS.register(CitadelWorldGen.class)" in main
assert "@SubscribeEvent\n    public static void onWorldLoad" in gen
assert '"citadelEnabled", true' in config
assert '"citadelSpacingChunks", 26, 16, 128' in config
assert '"citadelMinSeparationChunks", 10, 4, 16' in config
assert "accessTransformer = file(" in gradle
for field in ("field_236191_b_", "field_202247_j", "field_236193_d_"):
    assert field in at, "Missing 1.16.5 access transformer: "+field

# No unnatural mass generation or custom per-tick simulations in the Citadel.
assert re.search(r"\b(?:onTick|playerTick|chunkTick|EntityJoinWorldEvent)\b",gen) is None
print("Mega Citadel worldgen verified: Overworld registration, 17 non-overlapping 3D pieces, transformed 3x4 doors, bounded foundations and spacing.")
