#!/usr/bin/env python3
"""Guard exact approved Kryzorae sprites in source AND the compiled Forge JAR.

This prevents a recurring regression where a successful CI run still packages
old mineral/item artwork and needs manual post-build texture overlays.
No dependencies outside Python standard library. No effect on the game.
"""
from pathlib import Path
from hashlib import sha256
from zipfile import ZipFile
import json
import struct
import zlib

BASE = Path("src/main/resources/assets/aetherium")
EXPECTED = {
    "textures/block/aetherium_block.png": "0223e566e1d5079d463c7f0879e1cb36050296358a5eca87369f461e2a705f79",
    "textures/block/prismalita_ore_top.png": "3894c35db7aa57f6bf5cf09221756d5f9eecb27f8f0c93615380896ede0d24d9",
    "textures/item/aether_dust.png": "ba35cc02d1768055a4ba9f6388100f647359d7082489057436ed5e393e83012e",
    "textures/item/aetherium_amulet.png": "6b18eafba14fc702f85175724d25a358abe536b10a1bf93bd938c17dca36b864",
    "textures/item/aetherium_flare.png": "21e1ce396c0c0667e6f60a7d67159e15a30253ad981b38463b23c97f899bc4fa",
    "textures/item/aetherium_ingot.png": "49e7a292459aa559189db27465097180f3270005b27712e9c725c53874998df5",
    "textures/item/aetherium_magnet.png": "18b241ffc241fc4bdfb5b5528077fd374236148036d2322dbc74577d9a643048",
    "textures/item/aetherium_nugget.png": "18d881e6c2bc70cd5a7847e6d429245dd82ac3361b2d22359828b842f2ef2202",
    "textures/item/aetherium_ring.png": "8212400f2bd4c6c30c76a244d8d800be545abb207bac7b35fd30d7b8a1910152",
    "textures/item/astral_fragment.png": "ffbe229b273769615cdba4d048ba4060bcb690b0ea848adb0d149d5390c6c36e",
    "textures/item/astral_heart.png": "92b2e49b60f4ff8aa11882a7c658ad54c27a2dea5fbbc3bb6511e0fa1d14d432",
    "textures/item/astral_ingot.png": "ec47a65984a01eb9bad137e4aeb5b8264cbb251704924cdbfcad92469f09dd74",
    "textures/item/astral_seal.png": "0cacf9e22bd3b989cc8fe7ec97b526e608502e3e14e3446b4c1702e93f871ed6",
    "textures/item/brumacero_ingot.png": "4eba8741fcfc14a4349d30fadc0ab7d55cd90e893879a6bfc5c6a78b06fe5afb",
    "textures/item/prismalita_shard.png": "f964fc2725ddbfad7cf787f6aacd1a46c5d9e5eb95490e4363edfc23311bcea4",
    "textures/item/raw_aetherium.png": "73c7b5461b676e9748b3ee6e0b355f321516d111cfab0421f8f73cee7be8a4cb",
    "textures/item/raw_brumacero.png": "7533112262ebdff20a64cb3e1e2557a5e4f238764478e97f0e68b0c313df8ccd",
    "textures/item/raw_umbracita.png": "6e9255d141c13e5a9560ed7e03f3e0881e7214410e5b59249015b605b896a6ec",
    "textures/item/rift_core.png": "5f2336bd0fad4a7d48984d142d9dd815dc79b3e6423804d39ae2e71f34fbf95b",
    "textures/item/rift_fragment.png": "2dbc381e14f6fa678a1220823c8ff45778ac0eb41157005f8a492520d64b083f",
    "textures/item/rift_sigil.png": "bf5bbbeddf18c2a99b14bf23a3a223deaab4521a84bd3ed05aa3faf3d889572a",
    "textures/item/umbracita_ingot.png": "44e5ed3de59ca051267bb0484b5d5a21777c13210786ecef0d25a39a4452d4d8",
    "textures/item/brumacero_axe.png": "a26c9d5d18853351acfb2ee3153a8f2d45bda1e8463527c83d627c66369142ad",
    "textures/item/umbracita_axe.png": "a95f64960904eed18562b756c61d7e08c929c0592fd8fa50f8c58ceb3891defe"
}

def check_png(label, blob, size):
    assert blob.startswith(b"\x89PNG\r\n\x1a\n"), "Bad PNG header: " + label
    w, h = struct.unpack(">II", blob[16:24])
    assert (w,h) == size, f"Invalid dimensions for {label}: {w}x{h}"
    offset = 8
    ended = False
    while offset+12 <= len(blob):
        length = struct.unpack(">I", blob[offset:offset+4])[0]
        end = offset+12+length
        assert end <= len(blob), "Truncated PNG " + label
        kind = blob[offset+4:offset+8]
        payload = blob[offset+8:offset+8+length]
        crc = struct.unpack(">I",blob[offset+8+length:end])[0]
        assert zlib.crc32(kind+payload)&0xffffffff==crc, "PNG CRC mismatch: " + label
        offset = end
        if kind == b"IEND":
            ended=True
            break
    assert ended, "Incomplete PNG: " + label

def validate_archive(blob_reader):
    for name, wanted in EXPECTED.items():
        data = blob_reader(name)
        assert sha256(data).hexdigest()==wanted, "Approved sprite replaced: " + name
        size = (64,64) if name.endswith("_axe.png") else (32,32) if name.startswith("textures/block/") else (48,48)
        check_png(name,data,size)
    # Four natural cube ore blocks must reference real side/top/bottom PNGs.
    for ore in ("aetherium_ore","brumacero_ore","umbracita_ore","prismalita_ore"):
        model=json.loads(blob_reader("models/block/"+ore+".json"))
        assert model["parent"]=="minecraft:block/cube_bottom_top"
        for face in ("side","top","bottom"):
            ref=model["textures"][face]
            assert ref.startswith("aetherium:block/"), (ore,face)
            name="textures/block/"+ref.split("/")[-1]+".png"
            check_png(name,blob_reader(name),(32,32))

validate_archive(lambda name: (BASE/name).read_bytes())
jars=[p for p in Path("build/libs").glob("*.jar") if not p.name.endswith("-sources.jar")]
assert jars, "No compiled mod JAR to inspect"
for jar in jars:
    with ZipFile(jar) as z:
        assert z.testzip() is None, "Damaged ZIP container: "+str(jar)
        validate_archive(lambda name: z.read("assets/aetherium/"+name))
print(f"{len(EXPECTED)} approved art assets and all 4 ore models verified in source and JAR")
