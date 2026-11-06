#!/usr/bin/env python3
"""Generate real 1.16.5 Java-Edition jigsaw NBT modules for native worldgen.

Uses only the Python standard library. The templates are real, static structure
block assets, generated before Gradle packages the mod. Vanilla jigsaw code
handles connected multi-chunk placement; no worldgen cascades from our script.
"""
from pathlib import Path
from collections import OrderedDict
import gzip
import json
import struct

ROOT = Path(__file__).resolve().parent.parent
BLUEPRINT = ROOT / "docs/citadel/rooms.json"
OUT = ROOT / "src/main/resources/data/aetherium/structures/citadel"
DATA_VERSION = 2586  # Java Edition 1.16.5


def utf(value):
    encoded = value.encode("utf-8")
    assert len(encoded) < 65536
    return struct.pack(">H", len(encoded)) + encoded


def named(kind, name, payload):
    return bytes((kind,)) + utf(name) + payload


def compound_payload(entries):
    return b"".join(entries) + b"\x00"


def string(name, value):
    return named(8, name, utf(value))


def integer(name, value):
    return named(3, name, struct.pack(">i", value))


def short(name, value):
    return named(2, name, struct.pack(">h", value))


def tag_fields(data):
    """Correct 1.16.5 block-entity NBT types, including spawner SpawnData."""
    fields = []
    for key, value in data.items():
        if isinstance(value, dict):
            fields.append(compound(key, tag_fields(value)))
        elif isinstance(value, int):
            fields.append(short(key, value) if key in (
                "Delay", "MinSpawnDelay", "MaxSpawnDelay", "SpawnCount",
                "MaxNearbyEntities", "RequiredPlayerRange", "SpawnRange"
            ) else integer(key, value))
        elif isinstance(value, str):
            fields.append(string(key, value))
        else:
            raise TypeError("Unsupported NBT tag: " + repr((key, value)))
    return fields


def int_list(name, values):
    return named(9, name, bytes((3,)) + struct.pack(">i", len(values)) +
                 b"".join(struct.pack(">i", n) for n in values))


def compound(name, entries):
    return named(10, name, compound_payload(entries))


def compound_list(name, entries):
    return named(9, name, bytes((10,)) + struct.pack(">i", len(entries)) +
                 b"".join(compound_payload(e) for e in entries))


def room_blocks(room):
    w, h, d = room["size"]
    kind = room["kind"]
    roof = room["roof"]
    blocks = {}

    def put(x, y, z, name, properties=None, data=None):
        if 0 <= x < w and 0 <= y < h and 0 <= z < d:
            blocks[(x, y, z)] = (name, tuple(sorted((properties or {}).items())), data)

    # All volume cells are represented, including interior AIR: when loaded
    # underground the structure carves usable walkways rather than leaving dirt.
    for x in range(w):
        for z in range(d):
            for y in range(h):
                border = x in (0, w - 1) or z in (0, d - 1)
                if y == 0:
                    material = "minecraft:polished_blackstone_bricks" if (
                        x in (1, w-2) or z in (1, d-2)
                    ) else "minecraft:stone_bricks"
                elif border and y < h-2:
                    material = ("minecraft:cracked_stone_bricks"
                                if (x*7 + z*11 + y*13) % 17 in (0, 1)
                                else "minecraft:stone_bricks")
                elif roof == "solid" and y == h-2:
                    material = "minecraft:stone_bricks"
                elif border and y == h-1 and (x+z) % 3 == 0:
                    material = "minecraft:polished_blackstone_bricks"
                else:
                    material = "minecraft:air"
                put(x,y,z,material)

    # Open only declared 3x4 exits; no doorway leading into a hillside.
    center = w//2
    for z in [side for direction,side in (("north",0),("south",d-1))
              if direction in room.get("doors", {})
              or direction in room.get("exterior", [])]:
        for x in range(center-1,center+2):
            for y in range(1,5):
                put(x,y,z,"minecraft:air")
        for x in (center-2,center+2):
            for y in range(1,6):
                put(x,y,z,"minecraft:polished_blackstone_bricks")
        for x in range(center-2,center+3):
            put(x,5,z,"minecraft:chiseled_stone_bricks")

    # Squared supporting columns, placed away from the main connector aisle.
    for x in (2,w-3):
        for z in (2,d-3):
            for y in range(1,h-2):
                put(x,y,z,"minecraft:polished_blackstone_bricks")
            put(x,1,z,"minecraft:chiseled_stone_bricks")

    # Occasional static purple-stained glass in the side walls.
    for x in (0,w-1):
        for z in (d//2-1,d//2+2):
            for y in (3,4):
                put(x,y,z,"minecraft:purple_stained_glass")

    # Four static pedestal lamps, no tick-enabled blocks or custom renderer.
    for x in (4,w-5):
        for z in (4,d-5):
            if x in range(center-1,center+2):
                continue
            put(x,1,z,"minecraft:polished_blackstone_bricks")
            put(x,2,z,"minecraft:soul_lantern",
                {"hanging":"false","waterlogged":"false"})

    if kind == "entrance":
        # Two recognizable ruined gate towers and a raised parapet.
        for tower_x in (2, w-3):
            for tower_z in (2, d-4):
                for off_x in (-2,-1,0,1,2):
                    for off_z in (-2,-1,0,1,2):
                        x, z = tower_x+off_x, tower_z+off_z
                        if off_x in (-2,2) or off_z in (-2,2):
                            for y in range(2,h-1):
                                put(x,y,z,"minecraft:polished_blackstone_bricks")
                            if (off_x+off_z) % 2 == 0:
                                put(x,h-1,z,"minecraft:chiseled_stone_bricks")
        # Elevate the silhouette: dark crest across gateway.
        for x in range(center-4,center+5):
            if abs(x-center) in (0,2,4):
                put(x,h-1,0,"minecraft:polished_blackstone_bricks")
        for x in (center-5,center+5):
            for y in (1,2,3):
                put(x,y,2,"minecraft:crying_obsidian")
        for x in (center-4,center+4):
            put(x,1,6,"minecraft:chiseled_stone_bricks")

    elif kind == "combat":
        # Brumacero-style weapon stands / broken barracks furnishings.
        for z in (5,8,11):
            for x in (3,w-4):
                put(x,1,z,"minecraft:dark_oak_planks")
                put(x,2,z,"minecraft:air")

    elif kind == "puzzle":
        # Archive shelves stay clear of all chest coordinates and doorways.
        for x in (3,w-4):
            for z in range(5,d-5):
                for y in (1,2,3):
                    put(x,y,z,"minecraft:bookshelf")
        for x in (center-4,center+4):
            put(x,1,d//2,"minecraft:crying_obsidian")

    elif kind == "crypt":
        # Optional ruin: concentric ritual stone and a small silent altar.
        for x in range(3,w-3):
            for z in range(3,d-3):
                dx,dz=x-center,z-d//2
                if 15 <= dx*dx+dz*dz <= 24:
                    put(x,0,z,"minecraft:polished_blackstone_bricks")
        put(center,1,d//2,"minecraft:chiseled_stone_bricks")
        put(center,2,d//2,"minecraft:crying_obsidian")

    elif kind == "forge":
        # Inert forged metal and bars, no ticking furnace block entities.
        for x in (4, w-5):
            for z in (5, 8):
                for y in (1,2):
                    put(x,y,z,"minecraft:iron_block")
        for x in (3, w-4):
            for z in (3, d-4):
                for y in (2,3):
                    put(x,y,z,"minecraft:iron_bars")
        put(center+2,1,d//2,"minecraft:chiseled_stone_bricks")

    elif kind == "arena":
        # Open-air test arena: no roof; lots of unobstructed movement space.
        radius = min(w,d)//2 - 4
        for x in range(2,w-2):
            for z in range(2,d-2):
                dx, dz = x-center, z-d//2
                r2 = dx*dx+dz*dz
                if (radius-1)*(radius-1) <= r2 <= (radius+1)*(radius+1):
                    put(x,0,z,"minecraft:polished_blackstone_bricks")
        for x in (center-6,center+6):
            for z in (d//2-6,d//2+6):
                put(x,1,z,"minecraft:chiseled_stone_bricks")
                put(x,2,z,"minecraft:crying_obsidian")
        # Arena is empty on purpose. No boss or spawner is installed yet.

    elif kind == "courtyard":
        # A large open plaza with broken balustrades, a central compass and
        # raised lighting. The central 3-wide north/south path stays clear.
        for x in range(3,w-3):
            for z in range(3,d-3):
                dx, dz = x-center, z-d//2
                if 55 <= dx*dx+dz*dz <= 85:
                    put(x,0,z,"minecraft:polished_blackstone_bricks")
        for x in (4,w-5):
            for z in (4,d-5):
                for y in range(1,5):
                    put(x,y,z,"minecraft:polished_blackstone_bricks")
                put(x,5,z,"minecraft:soul_lantern",
                    {"hanging":"false","waterlogged":"false"})
        for x in (3,w-4):
            for z in (7,d-8):
                put(x,2,z,"minecraft:iron_bars")

    elif kind == "tower":
        # Three accessible storeys. A permanent ladder climbs a backed wall;
        # a dedicated 1x1 opening in each floor connects the levels.
        for floor in (8,15,21):
            if floor >= h-1:
                continue
            for x in range(1,w-1):
                for z in range(1,d-1):
                    if not (x==3 and z==3):
                        put(x,floor,z,"minecraft:polished_blackstone_bricks")
        for y in range(1,h-1):
            put(3,y,2,"minecraft:polished_blackstone_bricks")
            put(3,y,3,"minecraft:ladder",{"facing":"south","waterlogged":"false"})
        for y in (5,12,19):
            if y<h-1:
                for x in (0,w-1):
                    put(x,y,d//2,"minecraft:iron_bars")
        for x in range(1,w-1,2):
            for z in (0,d-1):
                put(x,h-1,z,"minecraft:polished_blackstone_bricks")

    elif kind in ("prison","catacombs"):
        # Visible hazard stripes: no unavoidable damage in the 3-wide center
        # path. Cobwebs telegraph the danger rather than hidden instant traps.
        for x in (4,w-5):
            for z in range(5,d-5):
                if z % 3:
                    put(x,0,z,"minecraft:magma_block")
                if z in (6,d-7):
                    put(x,1,z,"minecraft:cobweb")
        for x in (2,w-3):
            for z in (4,d-5):
                put(x,2,z,"minecraft:iron_bars")

    elif kind == "gallery":
        # Two side aisles with shelves and a cracked, illuminated altar.
        for x in (3,w-4):
            for z in range(4,d-4):
                for y in range(1,4):
                    put(x,y,z,"minecraft:bookshelf")
        for x in (center-3,center+3):
            put(x,1,d//2,"minecraft:chiseled_stone_bricks")
            put(x,2,d//2,"minecraft:soul_lantern",
                {"hanging":"false","waterlogged":"false"})

    elif kind == "threshold":
        # Later a seal/mini-boss encounter; currently a readable traversal
        # challenge, with no fake boss entity or automatic redstone loops.
        for x in range(4,w-4):
            for z in range(5,d-5):
                if x in (4,w-5) and z%4 in (0,1):
                    put(x,0,z,"minecraft:magma_block")
        for x in (4,w-5):
            for z in (6,d-7):
                for y in range(1,5):
                    put(x,y,z,"minecraft:polished_blackstone_bricks")
                put(x,5,z,"minecraft:crying_obsidian")

    elif kind == "sanctum":
        # Multi-aisle end chamber, open ceiling and a dark circular dais.
        # Boss spawning remains disabled until the actual Custodian is built.
        radius = 10
        for x in range(3,w-3):
            for z in range(3,d-3):
                dx,dz=x-center,z-d//2
                if (radius-1)**2 <= dx*dx+dz*dz <= (radius+1)**2:
                    put(x,0,z,"minecraft:polished_blackstone_bricks")
        for x in (5,w-6):
            for z in (5,d-6):
                for y in range(1,10):
                    put(x,y,z,"minecraft:polished_blackstone_bricks")
                put(x,10,z,"minecraft:crying_obsidian")
                put(x,11,z,"minecraft:soul_lantern",
                    {"hanging":"false","waterlogged":"false"})

    elif kind == "vault":
        # Compact underground reliquary, made accessible from the archive
        # ladder. Loot remains mid-tier until the later boss gate exists.
        for x in range(3,w-3):
            for z in (3,d-4):
                put(x,0,z,"minecraft:polished_blackstone_bricks")
        for x in (3,w-4):
            for z in range(4,d-4):
                if z % 3 == 0:
                    put(x,0,z,"minecraft:magma_block")
        put(center,1,d//2,"minecraft:chiseled_stone_bricks")
        put(center,2,d//2,"minecraft:crying_obsidian")

    # The connector metadata is preserved in the NBT for future real jigsaw
    # expansion, but the current Forge 1.16.5 structure uses a deterministic
    # prechecked layout rather than allowing jigsaw to overlap neighbor chunks.
    for direction, target in room.get("doors", {}).items():
        # A west-facing door may be an exit (forge wing), and a rotated
        # east-facing door may be an entry (east bastion). Do not guess from
        # the raw compass direction; the declared graph specifies the role.
        outgoing = room["doorRoles"][direction] == "exit"
        info = {
            "id": "minecraft:jigsaw",
            "name": "aetherium:citadel/" + ("exit" if outgoing else "entry"),
            "target": "aetherium:citadel/" + ("entry" if outgoing else "exit"),
            "pool": "aetherium:citadel/" + {
                "gateway":"start", "arena_shell":"arena"
            }.get(target, target) if outgoing else "minecraft:empty",
            "final_state": "minecraft:air",
            "joint": "aligned",
        }
        if direction in ("north","south"):
            z = 0 if direction=="north" else d-1
            put(center,2,z,"minecraft:jigsaw",{"orientation":direction+"_up"},info)
        else:
            x = 0 if direction=="west" else w-1
            for z in range(d//2-1,d//2+2):
                for y in range(1,5):
                    put(x,y,z,"minecraft:air")
            for z in range(d//2-2,d//2+3):
                put(x,5,z,"minecraft:chiseled_stone_bricks")
            put(x,2,d//2,"minecraft:jigsaw",
                {"orientation":direction+"_up"},info)

    # The undercroft ladders must be CONTINUOUS through the upper floor:
    # the last rung below is at world Y=-1 and the first upper rung is Y=0.
    # A one-block AIR opening here broke ladder climbing between levels.
    if room["id"] in ("crypt","archive"):
        for y in range(0,min(h-2,7)):
            put(4,y,3,"minecraft:stone_bricks")
            put(4,y,4,"minecraft:ladder",{"facing":"south","waterlogged":"false"})
    if room["id"] in ("catacombs","archive_vault"):
        for y in range(1,h):
            put(4,y,3,"minecraft:stone_bricks")
            put(4,y,4,"minecraft:ladder",{"facing":"south","waterlogged":"false"})

    chest_specs = []
    if room["chest"]:
        chest_specs.append({"pos": room["chest"], "loot": room["loot"]})
    chest_specs.extend(room.get("extraChests", []))
    for entry in chest_specs:
        x, y, z = entry["pos"]
        put(x,y,z,"minecraft:chest",
            {"facing":"north","type":"single","waterlogged":"false"},
            {"id":"minecraft:chest","LootTable":entry["loot"]})

    if room.get("spawner"):
        info=room["spawner"]
        x,y,z=info["pos"]
        # Native vanilla spawner: strictly bounded spawning and slow delay.
        # Unlike tick-based custom AI this runs only near a player.
        put(x,y,z,"minecraft:spawner",data={
            "id":"minecraft:mob_spawner",
            "SpawnData":{"id":info["mob"]},
            "Delay":200,"MinSpawnDelay":800,"MaxSpawnDelay":1400,
            "SpawnCount":2,"MaxNearbyEntities":3,
            "RequiredPlayerRange":14,"SpawnRange":3
        })

    return OrderedDict(sorted(blocks.items(),key=lambda p:(p[0][1],p[0][2],p[0][0])))


def encode_room(room):
    blockmap = room_blocks(room)
    palette = []
    palette_index = {}
    elements = []
    for position,(blockname,properties,entity) in blockmap.items():
        state = (blockname,properties)
        if state not in palette_index:
            palette_index[state] = len(palette)
            props = [string(k,v) for k,v in properties]
            fields = [string("Name",blockname)]
            if props:
                fields.append(compound("Properties",props))
            palette.append(fields)
        fields = [int_list("pos",position),
                  integer("state",palette_index[state])]
        if entity is not None:
            fields.append(compound("nbt",tag_fields(entity)))
        elements.append(fields)

    root = compound("",[
        integer("DataVersion",DATA_VERSION),
        string("author","Kryzorae"),
        int_list("size",room["size"]),
        compound_list("palette",palette),
        compound_list("blocks",elements),
        compound_list("entities",[]),
    ])
    return gzip.compress(root,compresslevel=9,mtime=0),len(elements),len(palette)


def main():
    spec = json.loads(BLUEPRINT.read_text(encoding="utf-8"))
    assert spec["namespace"] == "aetherium"
    assert spec["status"] == "registered-overworld-jigsaw-worldgen"
    OUT.mkdir(parents=True,exist_ok=True)
    for room in spec["rooms"]:
        assert room["id"].replace("_","").isalnum()
        assert all(5 <= x <= 31 for x in room["size"])
        blob,count,palette = encode_room(room)
        dest = OUT/(room["id"]+".nbt")
        dest.write_bytes(blob)
        print("Citadel NBT", dest.name, room["size"],
              count,"blocks",palette,"block states",len(blob),"compressed bytes")
    # No worldgen or other sprite is modified by this build-time generator.


if __name__ == "__main__":
    main()
