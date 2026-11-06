#!/usr/bin/env python3
"""Validate actual 1.16.5 Kryzorae Citadel NBT assets and chest loot in source/JAR."""
from pathlib import Path
from zipfile import ZipFile
import gzip
import json
import struct

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "src/main/resources"
SPEC = json.loads((ROOT/"docs/citadel/rooms.json").read_text(encoding="utf-8"))
FORBIDDEN = {
    "aetherium:rift_core", "aetherium:astral_heart",
    "aetherium:astral_ingot", "aetherium:rift_sigil",
    "aetherium:astral_seal", "aetherium:citadel_threshold_seal",
}
VALID = {
    "minecraft:stone_bricks", "minecraft:cracked_stone_bricks",
    "minecraft:polished_blackstone_bricks", "minecraft:chiseled_stone_bricks",
    "minecraft:purple_stained_glass", "minecraft:crying_obsidian",
    "minecraft:soul_lantern", "minecraft:air", "minecraft:chest",
    "minecraft:bookshelf", "minecraft:dark_oak_planks",
}


class NBT:
    def __init__(self, blob):
        self.buf = memoryview(gzip.decompress(blob))
        self.pos = 0

    def take(self, n):
        assert n>=0 and self.pos+n<=len(self.buf), "Truncated structure NBT"
        v=self.buf[self.pos:self.pos+n]
        self.pos+=n
        return v

    def byte(self):
        return int(self.take(1)[0])

    def number(self):
        return struct.unpack(">i",self.take(4))[0]

    def txt(self):
        n=struct.unpack(">H",self.take(2))[0]
        return bytes(self.take(n)).decode("utf-8")

    def value(self,kind):
        if kind == 1: return self.byte()
        if kind == 2: return struct.unpack(">h",self.take(2))[0]
        if kind == 3: return self.number()
        if kind == 4: return struct.unpack(">q",self.take(8))[0]
        if kind == 7:
            n=self.number()
            return bytes(self.take(n))
        if kind == 8: return self.txt()
        if kind == 9:
            sub=self.byte()
            n=self.number()
            assert 0<=n<=100000, "Unbounded NBT list"
            return [self.value(sub) for _ in range(n)]
        if kind == 10:
            fields={}
            while True:
                typ=self.byte()
                if typ == 0: break
                name=self.txt()
                assert name not in fields, "Duplicate NBT key"
                fields[name]=self.value(typ)
            return fields
        if kind == 11:
            n=self.number()
            assert 0<=n<=100000
            return [self.number() for _ in range(n)]
        raise AssertionError("Unexpected template NBT tag "+str(kind))

    def root(self):
        assert self.byte()==10
        assert self.txt()==""
        value=self.value(10)
        assert self.pos==len(self.buf), "Trailing NBT bytes"
        return value


def room_path(room):
    return "data/aetherium/structures/citadel/"+room["id"]+".nbt"


def validate_room(room, read):
    root=NBT(read(room_path(room))).root()
    assert root["DataVersion"]==2586, room["id"]
    assert root["size"]==room["size"], room["id"]
    assert root["author"]=="Kryzorae"
    assert root["entities"]==[], "Templates must not spawn active entities"
    palette=root["palette"]
    assert palette and len(palette)<=20, room["id"]
    names=[entry["Name"] for entry in palette]
    assert set(names).issubset(VALID), (room["id"], set(names)-VALID)
    blocks=root["blocks"]
    w,h,d=room["size"]
    assert len(blocks)==w*h*d, "Missing / duplicated volume: "+room["id"]
    coords={}
    chests=[]
    for item in blocks:
        pos=tuple(item["pos"])
        assert len(pos)==3
        x,y,z=pos
        assert 0<=x<w and 0<=y<h and 0<=z<d, "Out of bounds: "+room["id"]
        assert pos not in coords, "Overlapping block: "+room["id"]
        state=item["state"]
        assert 0<=state<len(palette)
        name=names[state]
        coords[pos]=name
        if name=="minecraft:chest":
            assert "nbt" in item
            assert item["nbt"]["id"]=="minecraft:chest"
            chests.append((list(pos),item["nbt"]["LootTable"]))
        else:
            assert "nbt" not in item, "Unwanted ticking block entity: "+name
    expected=[(room["chest"],room["loot"])] if room["chest"] else []
    assert chests==expected,(room["id"],chests,expected)
    mid=w//2
    for side in (0,d-1):
        for x in range(mid-1,mid+2):
            for y in range(1,5):
                assert coords[(x,y,side)]=="minecraft:air", (
                    room["id"],"door clearance",x,y,side)
    if room["roof"]=="open":
        assert coords[(mid,h-2,d//2)]=="minecraft:air"
    else:
        assert coords[(mid,h-2,d//2)]!="minecraft:air"
    return len(blocks)


def validate_loot(read):
    for which in ("entrance","guardhouse","archive"):
        name="data/aetherium/loot_tables/chests/citadel_"+which+".json"
        data=json.loads(read(name).decode("utf-8"))
        assert data["type"]=="minecraft:chest"
        assert len(data["pools"]) in (1,2,3)
        assert len(data["pools"])>=1
        for pool in data["pools"]:
            rolls=pool["rolls"]
            if isinstance(rolls,dict):
                assert 0<=rolls["min"]<=rolls["max"]<=3
            else:
                assert 0<=rolls<=3
            for entry in pool["entries"]:
                assert entry["type"]=="minecraft:item"
                name=entry["name"]
                assert name not in FORBIDDEN, "Boss-exclusive drop leaked into chest: "+name
                assert name.startswith("minecraft:") or name in {
                    "aetherium:raw_brumacero",
                    "aetherium:raw_umbracita",
                    "aetherium:brumacero_ingot",
                    "aetherium:prismalita_shard",
                    "aetherium:aether_dust",
                }, "Unregistered loot item: "+name
                assert 1<=entry.get("weight",1)<=20
                for fn in entry.get("functions",[]):
                    assert fn["function"]=="minecraft:set_count"
                    count=fn["count"]
                    assert 1<=count["min"]<=count["max"]<=6
                for cond in entry.get("conditions",[]):
                    assert cond["condition"]=="minecraft:random_chance"
                    assert 0<cond["chance"]<=1


def validate(lookup):
    total=0
    assert SPEC["namespace"]=="aetherium"
    assert SPEC["status"]=="templates-only-no-natural-worldgen"
    assert SPEC["roomCapFuture"]<=9
    rooms=SPEC["rooms"]
    assert len(rooms)==5
    assert len({r["id"] for r in rooms})==len(rooms)
    for room in rooms:
        assert 5<=room["size"][0]<=31
        assert 8<=room["size"][1]<=24
        assert 5<=room["size"][2]<=31
        total+=validate_room(room,lookup)
    validate_loot(lookup)
    return total


source_count=validate(lambda path:(RES/path).read_bytes())
jars=[x for x in (ROOT/"build/libs").glob("*.jar")
      if not x.name.endswith("-sources.jar")]
assert jars,"No Forge build JAR"
for jar in jars:
    with ZipFile(jar) as z:
        assert z.testzip() is None
        jar_count=validate(lambda path:z.read(path))
        assert jar_count==source_count
        for room in SPEC["rooms"]:
            path=room_path(room)
            assert z.read(path)==(RES/path).read_bytes(), "NBT changed in build"
print("Citadel OK:",len(SPEC["rooms"]),"rooms,",source_count,
      "NBT cells, 3 safe loot tables, source/JAR verified; natural worldgen pending")
