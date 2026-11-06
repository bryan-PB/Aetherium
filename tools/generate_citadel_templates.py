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
from citadel_architecture import design_room

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

    # All design/furnishing belongs in a separate deterministic, testable
    # architectural pass. It supersedes the old scattered cubes, isolated
    # bars, plain floors and cavernous rooms with flat empty ceilings.
    design_room(room, put)

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
