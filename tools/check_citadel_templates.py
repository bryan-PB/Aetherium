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
    "minecraft:iron_block", "minecraft:iron_bars", "minecraft:spawner",
    "minecraft:magma_block", "minecraft:cobweb", "minecraft:ladder",
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
    assert palette and len(palette)<=32, room["id"]
    names=[entry["Name"] for entry in palette]
    assert set(names).issubset(VALID), (room["id"], set(names)-VALID)
    blocks=root["blocks"]
    w,h,d=room["size"]
    assert len(blocks)==w*h*d, "Missing / duplicated volume: "+room["id"]
    coords={}
    chests=[]
    spawners=[]
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
        elif name=="minecraft:spawner":
            assert "nbt" in item
            entity=item["nbt"]
            assert entity["id"]=="minecraft:mob_spawner"
            assert entity["SpawnData"]["id"] in {
                "minecraft:zombie","minecraft:skeleton","minecraft:spider"}
            assert entity["SpawnCount"]<=2
            assert entity["MaxNearbyEntities"]<=3
            assert entity["RequiredPlayerRange"]<=16
            assert entity["MinSpawnDelay"]>=600
            spawners.append((list(pos),entity["SpawnData"]["id"]))
        elif name=="minecraft:jigsaw":
            assert "nbt" in item
            assert item["nbt"]["id"]=="minecraft:jigsaw"
            assert item["nbt"]["final_state"]=="minecraft:air"
            assert item["nbt"]["name"].startswith("aetherium:citadel/")
            assert item["nbt"]["target"].startswith("aetherium:citadel/")
            assert item["nbt"]["pool"]=="minecraft:empty" or (
                item["nbt"]["pool"].startswith("aetherium:citadel/"))
        else:
            assert "nbt" not in item, "Unexpected block entity: "+name
    expected=[]
    if room["chest"]:
        expected.append((room["chest"],room["loot"]))
    expected.extend((entry["pos"],entry["loot"])
                    for entry in room.get("extraChests",[]))
    assert sorted(chests)==sorted(expected),(room["id"],chests,expected)
    expect_spawners=[]
    if room.get("spawner"):
        info=room["spawner"]
        expect_spawners.append((info["pos"],info["mob"]))
    assert spawners==expect_spawners,(room["id"],spawners,expect_spawners)
    # Every declared door is exactly 3 wide x 4 high; the center is a
    # processed Jigsaw block. No undeclared exit can expose a cliff.
    doors=room.get("doors",{})
    connectors=[(tuple(it["pos"]),it["nbt"]) for it in blocks
                if names[it["state"]]=="minecraft:jigsaw"]
    assert len(connectors)==len(doors), (room["id"],connectors,doors)
    for side in ("north","south","east","west"):
        connected=side in doors
        if side=="north":
            border=[(x,y,0) for x in range(w//2-1,w//2+2) for y in range(1,5)]
            center=(w//2,2,0)
        elif side=="south":
            border=[(x,y,d-1) for x in range(w//2-1,w//2+2) for y in range(1,5)]
            center=(w//2,2,d-1)
        elif side=="west":
            border=[(0,y,z) for z in range(d//2-1,d//2+2) for y in range(1,5)]
            center=(0,2,d//2)
        else:
            border=[(w-1,y,z) for z in range(d//2-1,d//2+2) for y in range(1,5)]
            center=(w-1,2,d//2)
        matches=[data for pos,data in connectors if pos==center]
        assert len(matches)==int(connected), (room["id"],side,matches)
        if connected:
            assert all(coords[p] in ("minecraft:air","minecraft:jigsaw")
                       for p in border), (room["id"],side,"door blocked")
            data=matches[0]
            assert data["joint"]=="aligned"
            assert data["name"]==("aetherium:citadel/exit"
                                  if side in ("south","east")
                                  else "aetherium:citadel/entry")
            assert data["target"]==("aetherium:citadel/entry"
                                    if side in ("south","east")
                                    else "aetherium:citadel/exit")
            if side in ("south","east"):
                target=doors[side]
                target_pool={"gateway":"start","arena_shell":"arena"}.get(target,target)
                assert data["pool"]=="aetherium:citadel/"+target_pool
            else:
                assert data["pool"]=="minecraft:empty"
            state=palette[next(it["state"] for it in blocks
                               if tuple(it["pos"])==center)]
            assert state["Properties"]["orientation"]==side+"_up"
        elif side in room.get("exterior", []):
            assert room["id"]=="gateway" and side=="north"
            assert all(coords[p]=="minecraft:air" for p in border), (
                room["id"],side,"front entry blocked")
        else:
            assert all(coords[p] not in ("minecraft:air","minecraft:jigsaw")
                       for p in border), (room["id"],side,"dangling door")
    if room["id"] in ("crypt","archive"):
        assert coords[(4,0,4)]=="minecraft:air", "Upper ladder has no entry"
    if room["id"] in ("catacombs","archive_vault"):
        assert coords[(4,h-1,4)]=="minecraft:ladder", "Underground shaft sealed"
    if room["roof"]=="open":
        assert coords[(mid,h-2,d//2)]=="minecraft:air"
    else:
        assert coords[(mid,h-2,d//2)]!="minecraft:air"
    return len(blocks)


def validate_loot(read):
    for which in ("entrance","guardhouse","archive","supplies","reliquary"):
        name="data/aetherium/loot_tables/chests/citadel_"+which+".json"
        data=json.loads(read(name).decode("utf-8"))
        assert data["type"]=="minecraft:chest"
        assert len(data["pools"]) in (1,2,3)
        assert len(data["pools"])>=1
        for pool in data["pools"]:
            rolls=pool["rolls"]
            if isinstance(rolls,dict):
                assert 0<=rolls["min"]<=rolls["max"]<=5
            else:
                assert 0<=rolls<=5
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
    assert SPEC["status"]=="registered-overworld-jigsaw-worldgen"
    assert SPEC["naturalGeneration"] is True
    assert SPEC["maxConnectedModules"]==17
    rooms=SPEC["rooms"]
    assert len(rooms)==17
    room_ids={r["id"] for r in rooms}
    assert len(room_ids)==len(rooms)
    pool_names=SPEC["pools"]
    assert len(pool_names)==len(rooms)
    assert len(set(pool_names))==len(pool_names)
    for pool_id in pool_names:
        path="data/aetherium/worldgen/template_pool/citadel/"+pool_id+".json"
        pool=json.loads(lookup(path).decode("utf-8"))
        assert pool["name"]=="aetherium:citadel/"+pool_id
        assert pool["fallback"]=="minecraft:empty"
        assert len(pool["elements"])==1
        entry=pool["elements"][0]
        assert entry["weight"]==1
        element=entry["element"]
        assert element["element_type"]=="minecraft:single_pool_element"
        mapped={"start":"gateway","arena":"arena_shell"}.get(pool_id,pool_id)
        assert element["location"]=="aetherium:citadel/"+mapped
        assert element["projection"]=="rigid"
        assert element["processors"]=="minecraft:empty"
    count_spawners=0
    count_chests=0
    for room in rooms:
        assert 5<=room["size"][0]<=31
        assert 8<=room["size"][1]<=24
        assert 5<=room["size"][2]<=31
        assert room["rotation"] in (
            "NONE","CLOCKWISE_90","CLOCKWISE_180","COUNTERCLOCKWISE_90")
        assert room["mirror"] in ("NONE","FRONT_BACK","LEFT_RIGHT")
        assert len(room["offset"])==3
        assert all(id_ in room_ids for id_ in room["doors"].values())
        total+=validate_room(room,lookup)
        count_chests+=int(bool(room["chest"]))+len(room.get("extraChests",[]))
        count_spawners+=int(bool(room.get("spawner")))
    assert count_chests>=17, "Mega Citadel needs distributed room loot"
    assert 2<=count_spawners<=4, "Only a few bounded vanilla spawners"
    assert sum(r["offset"][1]<0 for r in rooms)>=2, "Missing underground chambers"
    assert sum(r["kind"]=="tower" for r in rooms)>=2, "Missing towers"
    validate_loot(lookup)
    return total,count_chests,count_spawners

source_count,source_chests,source_spawners=validate(lambda path:(RES/path).read_bytes())
jars=[x for x in (ROOT/"build/libs").glob("*.jar")
      if not x.name.endswith("-sources.jar")]
assert jars,"No Forge build JAR"
for jar in jars:
    with ZipFile(jar) as z:
        assert z.testzip() is None
        jar_count,jar_chests,jar_spawners=validate(lambda path:z.read(path))
        assert (jar_count,jar_chests,jar_spawners)==(
            source_count,source_chests,source_spawners)
        for room in SPEC["rooms"]:
            path=room_path(room)
            assert z.read(path)==(RES/path).read_bytes(), "NBT changed in build"
        for name in SPEC["pools"]:
            path="data/aetherium/worldgen/template_pool/citadel/"+name+".json"
            assert z.read(path)==(RES/path).read_bytes()
print("Citadel OK:",len(SPEC["rooms"]),"rooms,",source_count,
      "NBT cells,",source_chests,"chests,",source_spawners,
      "bounded spawners, 5 safe loot tables, all Jigsaw ports source/JAR verified")
