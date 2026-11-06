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

    # Two 3x4 connected entrances, independent of the surrounding wall type.
    center = w//2
    for z in (0,d-1):
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

    # Jigsaw blocks only exist as connectors during world generation;
    # the vanilla jigsaw processor replaces both with final_state AIR.
    links = ["corridor", "guardhouse", "archive", "arena_shell", None]
    ordering = ["gateway", "corridor", "guardhouse", "archive", "arena_shell"]
    index = ordering.index(room["id"])
    if index > 0:
        put(center,2,0,"minecraft:jigsaw",
            {"orientation":"north_up"},
            {"id":"minecraft:jigsaw",
             "name":"aetherium:citadel/entry",
             "target":"aetherium:citadel/exit",
             "pool":"minecraft:empty",
             "final_state":"minecraft:air",
             "joint":"aligned"})
    if links[index]:
        target_pool = "arena" if links[index] == "arena_shell" else links[index]
        put(center,2,d-1,"minecraft:jigsaw",
            {"orientation":"south_up"},
            {"id":"minecraft:jigsaw",
             "name":"aetherium:citadel/exit",
             "target":"aetherium:citadel/entry",
             "pool":"aetherium:citadel/"+target_pool,
             "final_state":"minecraft:air",
             "joint":"aligned"})

    if room["chest"]:
        x,y,z = room["chest"]
        assert kind not in ("arena","connector")
        put(x,y,z,"minecraft:chest",
            {"facing":"north","type":"single","waterlogged":"false"},
            {"id":"minecraft:chest","LootTable":room["loot"]})

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
            fields.append(compound("nbt",[string(k,v) for k,v in entity.items()]))
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
