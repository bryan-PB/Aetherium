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
    "minecraft:bookshelf", "minecraft:dark_oak_planks", "minecraft:jigsaw",
    "minecraft:iron_block", "minecraft:iron_bars",
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
        elif name == "minecraft:jigsaw":
            assert item["nbt"]["id"] == "minecraft:jigsaw"
            assert item["nbt"]["final_state"] == "minecraft:air"
            assert item["nbt"]["name"].startswith("aetherium:citadel/")
            assert item["nbt"]["target"].startswith("aetherium:citadel/")
            assert item["nbt"]["pool"] == "minecraft:empty" or item["nbt"]["pool"].startswith("aetherium:citadel/")
        else:
            assert "nbt" not in item, "Unwanted ticking block entity: "+name
    expected=[(room["chest"],room["loot"])] if room["chest"] else []
    assert chests==expected,(room["id"],chests,expected)
    mid=w//2
    connectors=[(tuple(it["pos"]),it["nbt"]) for it in blocks
                if names[it["state"]]=="minecraft:jigsaw"]
    expected_connectors= {
        "gateway":1, "corridor":3, "guardhouse":3,
        "archive":2, "arena_shell":1, "crypt":1, "forge":1,
    }[room["id"]]
    assert len(connectors)==expected_connectors,(room["id"],connectors)
    if room["id"] not in ("gateway","crypt","forge"):
        assert any(p==(mid,2,0) and data["name"]=="aetherium:citadel/entry"
                   for p,data in connectors), "Missing entry connector"
    if room["id"] not in ("arena_shell","crypt","forge"):
        assert any(p==(mid,2,d-1) and data["name"]=="aetherium:citadel/exit"
                   for p,data in connectors), "Missing exit connector"
    expected_links = {
        "gateway": {"aetherium:citadel/corridor"},
        "corridor": {"aetherium:citadel/guardhouse", "aetherium:citadel/crypt", "minecraft:empty"},
        "guardhouse": {"aetherium:citadel/archive", "aetherium:citadel/forge", "minecraft:empty"},
        "archive": {"aetherium:citadel/arena", "minecraft:empty"},
        "arena_shell": {"minecraft:empty"},
        "crypt": {"minecraft:empty"},
        "forge": {"minecraft:empty"},
    }
    assert {data["pool"] for pos,data in connectors}==expected_links[room["id"]], room["id"]
    for pos,data in connectors:
        state = palette[next(it["state"] for it in blocks if tuple(it["pos"])==pos)]
        orientation = state["Properties"]["orientation"]
        x,y,z = pos
        if z==0: assert orientation=="north_up"
        elif z==d-1: assert orientation=="south_up"
        elif x==0: assert orientation=="west_up"
        elif x==w-1: assert orientation=="east_up"
        else: raise AssertionError("Jigsaw in room interior "+room["id"])
        assert y==2
        assert data["joint"]=="aligned"
        assert data["target"]==("aetherium:citadel/entry"
                                if data["pool"]!="minecraft:empty"
                                else "aetherium:citadel/exit")
    for side in (0,d-1):
        for x in range(mid-1,mid+2):
            for y in range(1,5):
                if room["id"] in ("crypt","forge") or (room["id"]=="arena_shell" and side==d-1):
                    assert coords[(x,y,side)] not in ("minecraft:air","minecraft:jigsaw")
                else:
                    assert coords[(x,y,side)] in ("minecraft:air","minecraft:jigsaw"), (
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
    pool_names=["start","corridor","guardhouse","archive","arena","crypt","forge"]
    expected_rooms=["gateway","corridor","guardhouse","archive","arena_shell","crypt","forge"]
    for name,room_id in zip(pool_names,expected_rooms):
        path="data/aetherium/worldgen/template_pool/citadel/"+name+".json"
        pool=json.loads(lookup(path).decode("utf-8"))
        assert pool["name"]=="aetherium:citadel/"+name
        assert pool["fallback"]=="minecraft:empty"
        assert len(pool["elements"])==1
        entry=pool["elements"][0]
        assert entry["weight"]==1
        element=entry["element"]
        assert element["element_type"]=="minecraft:single_pool_element"
        assert element["location"]=="aetherium:citadel/"+room_id
        assert element["projection"]=="rigid"
        assert element["processors"]=="minecraft:empty"
    assert SPEC["namespace"]=="aetherium"
    assert SPEC["status"]=="registered-overworld-jigsaw-worldgen"
    assert SPEC["naturalGeneration"] is True
    assert SPEC["maxConnectedModules"]==7
    assert SPEC["roomCapFuture"]<=9
    rooms=SPEC["rooms"]
    assert len(rooms)==7
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
        for name in SPEC["pools"]:
            path="data/aetherium/worldgen/template_pool/citadel/"+name+".json"
            assert z.read(path)==(RES/path).read_bytes()
print("Citadel OK:",len(SPEC["rooms"]),"rooms,",source_count,
      "NBT cells, 3 safe loot tables, connected jigsaw rooms source/JAR verified")
