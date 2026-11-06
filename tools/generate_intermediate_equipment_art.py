#!/usr/bin/env python3
"""Generate original low-cost pixel art for intermediate Kryzorae gear.

No Pillow dependency; all 32x32 sprites and 128x64 vanilla-biped armor
atlases are ordinary RGBA PNGs. Executed at build time, never at runtime.
This avoids both duplicate binary sources and per-frame rendering effects.
"""
from pathlib import Path
import struct
import zlib

ASSETS = Path("src/main/resources/assets/aetherium/textures")
COLORS = {
    "brumacero": {
        "outline": "#141923", "under": "#293449", "shadow": "#3c4d66",
        "base": "#68819b", "light": "#a9bccd", "shine": "#e7eef0",
        "trim": "#58708f", "gem": "#81e6f5", "darkgem": "#167b96",
        "leather": "#382e2f", "wood": "#72564f"},
    "umbracita": {
        "outline": "#120e1d", "under": "#251930", "shadow": "#36234d",
        "base": "#583478", "light": "#9b62ca", "shine": "#e8bdf8",
        "trim": "#73439d", "gem": "#f198ff", "darkgem": "#6932b5",
        "leather": "#2d253a", "wood": "#493050"},
}


def rgba(hex_color):
    h = hex_color.lstrip("#")
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4)) + (255,)


class Art:
    def __init__(self, w, h):
        self.w, self.h = w, h
        self.data = bytearray(w*h*4)

    def put(self, x, y, color):
        if not 0 <= x < self.w or not 0 <= y < self.h:
            return
        at = 4*(int(y)*self.w+int(x))
        self.data[at:at+4] = bytes(rgba(color) if isinstance(color, str) else color)

    def rect(self, a, b, c, d, color):
        for y in range(max(0, int(b)), min(self.h, int(d))):
            for x in range(max(0, int(a)), min(self.w, int(c))):
                self.put(x, y, color)

    def polygon(self, points, color):
        # Pixel-center scanline fill: non-antialiased deliberately.
        ys = [p[1] for p in points]
        for y in range(max(0, min(ys)), min(self.h, max(ys)+1)):
            xs = []
            for i, (ax, ay) in enumerate(points):
                bx, by = points[(i+1) % len(points)]
                if (ay <= y < by) or (by <= y < ay):
                    xs.append(ax + (y-ay)*(bx-ax)/(by-ay))
            xs.sort()
            for i in range(0, len(xs)-1, 2):
                for x in range(max(0, int(xs[i]+0.5)), min(self.w, int(xs[i+1]+0.5))):
                    self.put(x, y, color)

    def line(self, ax, ay, bx, by, color, thickness=1):
        distance = max(abs(bx-ax), abs(by-ay), 1)
        for step in range(distance+1):
            x = round(ax+(bx-ax)*step/distance)
            y = round(ay+(by-ay)*step/distance)
            for dx in range(-(thickness//2), (thickness+1)//2):
                for dy in range(-(thickness//2), (thickness+1)//2):
                    self.put(x+dx, y+dy, color)

    def diamond(self, x, y, size, color):
        self.polygon([(x, y-size), (x+size, y), (x, y+size), (x-size, y)], color)

    def png(self, path):
        path.parent.mkdir(parents=True, exist_ok=True)
        def chunk(tag, data):
            return struct.pack(">I",len(data)) + tag + data + struct.pack(
                ">I", zlib.crc32(tag+data)&0xffffffff)
        raw = b"".join(
            b"\0"+bytes(self.data[y*self.w*4:(y+1)*self.w*4])
            for y in range(self.h))
        path.write_bytes(
            b"\x89PNG\r\n\x1a\n"
            + chunk(b"IHDR", struct.pack(">IIBBBBB", self.w, self.h, 8, 6, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b""))


def armor_icon(kind, p, spectral):
    a = Art(32,32)
    if kind == "helmet":
        # War helm with flared cheek guards and one small inset crystal.
        a.polygon([(5,10),(9,5),(22,5),(27,10),(27,24),(23,28),
                   (20,25),(12,25),(9,28),(5,24)],p["outline"])
        a.polygon([(7,10),(11,7),(21,7),(25,11),(24,23),(20,24),
                   (12,24),(8,22)],p["base"])
        a.polygon([(8,10),(12,7),(20,7),(23,11),(20,13),(12,13)],p["light"])
        if spectral:
            a.polygon([(6,9),(9,3),(12,9)],p["shadow"])
            a.polygon([(23,9),(27,3),(26,12)],p["trim"])
            a.line(10,9,22,9,p["shine"],2)
        else:
            a.line(10,7,20,7,p["shine"],2)
            a.rect(7,16,10,23,p["shadow"]); a.rect(23,16,26,23,p["shadow"])
        a.polygon([(9,15),(23,15),(20,20),(12,20)],p["under"])
        a.rect(12,16,20,18,p["outline"])
        a.rect(13,16,19,17,p["gem"])
        a.diamond(16,11,2,p["darkgem"])
        a.put(16,10,p["shine"])
    elif kind == "chestplate":
        a.polygon([(5,4),(12,5),(16,8),(20,5),(27,4),(30,12),
                   (26,18),(25,29),(7,29),(6,18),(2,13)],p["outline"])
        a.polygon([(6,7),(12,7),(16,11),(20,7),(26,7),(27,14),
                   (23,16),(23,26),(9,26),(9,16),(5,14)],p["base"])
        a.polygon([(3,9),(12,7),(13,13),(9,17),(5,16)],p["light"])
        a.polygon([(29,9),(20,7),(19,13),(23,17),(27,16)],p["shadow"])
        a.polygon([(12,13),(16,12),(20,13),(22,21),(16,26),(10,21)],p["under"])
        a.line(10,22,16,26,p["trim"],2)
        a.line(16,26,22,22,p["light"],2)
        a.diamond(16,17,4,p["outline"])
        a.diamond(16,17,3,p["darkgem"])
        a.diamond(16,16,2,p["gem"])
        a.put(16,15,p["shine"])
        a.rect(10,26,22,29,p["shadow"])
        for x in (7,24):
            a.put(x,12,p["shine"])
    elif kind == "leggings":
        a.polygon([(7,3),(25,3),(27,9),(25,28),(18,30),(16,19),
                   (14,30),(7,28),(5,9)],p["outline"])
        a.rect(8,5,24,12,p["base"])
        a.rect(9,6,23,8,p["light"])
        a.rect(8,12,14,27,p["base"])
        a.rect(18,12,24,27,p["shadow"])
        a.line(8,17,14,17,p["trim"],2)
        a.line(18,17,24,17,p["light"],2)
        a.rect(9,24,14,27,p["under"])
        a.rect(18,24,23,27,p["under"])
        a.diamond(16,8,3,p["darkgem"]); a.put(16,6,p["gem"])
    elif kind == "boots":
        for x in (2,17):
            a.polygon([(x+5,8),(x+11,8),(x+12,17),(x+13,23),(x+13,28),
                       (x+1,28),(x,22),(x+3,18)],p["outline"])
            a.polygon([(x+5,10),(x+10,10),(x+10,19),(x+12,23),
                       (x+12,26),(x+3,26),(x+2,22),(x+4,18)],p["base"])
            a.line(x+5,11,x+10,11,p["shine"],2)
            a.line(x+4,20,x+11,20,p["trim"],2)
            a.rect(x+3,25,x+12,27,p["shadow"])
            a.put(x+7,17,p["gem"])
    # Pixel highlights and colored rivets are fixed, no runtime particle effects.
    return a


def tool_icon(kind, p, spectral):
    a=Art(32,32)
    if kind=="sword":
        a.polygon([(7,26),(11,27),(16,21),(13,18)],p["outline"])
        a.line(8,28,13,23,p["wood"],3)
        a.line(8,28,13,23,p["light"],1)
        a.polygon([(10,18),(13,15),(21,3),(27,2),(27,8),(17,19),(14,21)],p["outline"])
        a.polygon([(13,16),(22,5),(25,4),(24,8),(16,18)],p["light"])
        a.polygon([(16,18),(24,8),(25,4),(26,7),(17,20)],p["base"])
        a.line(22,6,15,17,p["shine"],1)
        a.polygon([(9,17),(13,20),(16,22),(14,25),(10,21),(7,20)],p["shadow"])
        a.diamond(12,21,2,p["gem"])
        a.put(24,5,p["gem"])
    else:
        # All tools have independent forged heads, angled wooden grips and small
        # inlays; the silhouette remains readable even in a 16px hotbar slot.
        a.line(7,28,22,8,p["outline"],5)
        a.line(7,28,22,8,p["wood"],3)
        a.line(8,27,20,11,p["light"],1)
        a.rect(7,26,9,29,p["trim"])
        if kind=="pickaxe":
            a.polygon([(4,9),(12,6),(21,5),(28,8),(30,13),
                       (25,12),(20,10),(12,11),(7,15)],p["outline"])
            a.polygon([(6,10),(13,7),(21,6),(27,9),(21,9),(12,10)],p["base"])
            a.line(12,7,24,8,p["shine"],1)
            a.polygon([(24,9),(28,11),(26,15)],p["light"])
            a.put(19,8,p["gem"])
        elif kind=="axe":
            a.polygon([(11,5),(21,4),(29,8),(26,19),(15,17),(12,13)],p["outline"])
            a.polygon([(13,7),(21,6),(27,9),(24,17),(17,16),(13,12)],p["base"])
            a.polygon([(13,7),(21,6),(27,9),(25,11),(13,10)],p["light"])
            a.line(26,11,24,16,p["shine"],1)
            a.diamond(20,12,2,p["gem"])
        elif kind=="shovel":
            a.polygon([(18,3),(23,2),(29,7),(28,13),(23,19),(17,13),(16,8)],p["outline"])
            a.polygon([(19,5),(23,4),(27,8),(26,12),(23,16),(18,12)],p["base"])
            a.polygon([(20,5),(23,4),(26,7),(21,10)],p["light"])
            a.line(23,6,20,10,p["shine"],1)
            a.put(24,12,p["gem"])
        elif kind=="hoe":
            a.polygon([(13,4),(27,3),(29,8),(26,12),(20,10),(19,8),(13,9)],p["outline"])
            a.polygon([(15,5),(26,5),(27,7),(23,9),(16,8)],p["base"])
            a.line(16,5,24,5,p["shine"],1)
            a.put(21,7,p["gem"])
    if spectral:
        a.put(25,8,p["gem"])
        a.put(23,9,p["shine"])
    return a


def worn_layer(which, p, second):
    # UV sheet for vanilla humanoid biped model. Higher resolution (2x) without
    # introducing custom geometry, renderers, or any per-tick animation.
    a=Art(128,64)
    for y in range(64):
        for x in range(128):
            # Slightly varied smith-forged surface, not an overly clean flat fill.
            n=(x*37+y*73+(x//6)*19+(y//6)*11)%19
            color=p["shadow"] if n<3 else p["base"] if n<11 else p["under"]
            a.put(x,y,color)
    # Segmented plating on helmet, torso, arms and greaves UV islands.
    a.rect(0,0,64,32,p["shadow"])
    a.rect(2,3,61,27,p["base"])
    for x in range(5,62,10):
        a.line(x,5,x+5,5,p["light"],2)
        a.line(x,25,x+5,25,p["under"],2)
    for x in (4,18,34,49):
        a.rect(x,11,x+7,20,p["under"])
        a.rect(x+1,11,x+6,13,p["trim"])
    a.rect(0,32,80,64,p["shadow"])
    for x in range(4,77,13):
        a.rect(x,36,x+9,58,p["base"])
        a.line(x+1,36,x+8,36,p["light"],2)
        a.rect(x+2,50,x+9,53,p["under"])
    for x in range(81,124,12):
        a.rect(x,33,x+9,63,p["base"])
        a.line(x,36,x+8,36,p["shine"],1)
        a.rect(x,54,x+9,57,p["under"])
    for x,y in [(14,15),(44,16),(27,43),(56,43),(93,47),(111,47)]:
        a.diamond(x,y,3,p["trim"])
        a.put(x,y-1,p["gem"])
    if second:
        # Different secondary layer on legs, with vertical thigh plating.
        for x in (10,24,38,52,68,84,103,116):
            a.rect(x,22,x+5,60,p["under"])
            a.line(x,25,x,55,p["light"],1)
            a.rect(x,42,x+5,45,p["trim"])
        for x in range(7,120,17):
            a.put(x,33,p["gem"])
    elif which=="umbracita":
        # Rare angular inlays, never a costly emissive render layer.
        for x,y in [(15,8),(41,8),(16,40),(45,40),(103,44)]:
            a.diamond(x,y,4,p["darkgem"])
            a.diamond(x,y,2,p["gem"])
    return a


def main():
    for name, pal in COLORS.items():
        for piece in ("helmet","chestplate","leggings","boots"):
            armor_icon(piece,pal,name=="umbracita").png(
                ASSETS/"item"/(name+"_"+piece+".png"))
        for piece in ("sword","pickaxe","axe","shovel","hoe"):
            tool_icon(piece,pal,name=="umbracita").png(
                ASSETS/"item"/(name+"_"+piece+".png"))
        for layer in (1,2):
            worn_layer(name,pal,layer==2).png(
                ASSETS/"models"/"armor"/(name+"_layer_"+str(layer)+".png"))
    print("Generated 18 equipment icons and 4 worn armor atlases (no rendering hooks).")


if __name__=="__main__":
    main()
