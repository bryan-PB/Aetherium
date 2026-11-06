"""Architectural passes for Kryzorae's 17 modular 1.16.5 Citadel NBTs.

Pure block placement: no world reads, random generation, ticking tile entities,
entities or structure cascades. Keep the 3x4 portals and ladder shafts clear.
The goal is *rooms with a readable purpose*, not empty stone boxes or scattered
one-block 'decorations'. Generated NBT size limits still apply.
"""
from math import hypot

STONE = "minecraft:stone_bricks"
BLACK = "minecraft:polished_blackstone_bricks"
DARK = "minecraft:polished_blackstone"
CRACKED = "minecraft:cracked_stone_bricks"
CHISELED = "minecraft:chiseled_stone_bricks"
SLAB = "minecraft:polished_blackstone_brick_slab"
STAIRS = "minecraft:polished_blackstone_brick_stairs"
WALL = "minecraft:polished_blackstone_brick_wall"
LIGHT = "minecraft:sea_lantern"
SOUL = "minecraft:soul_lantern"
GLASS = "minecraft:purple_stained_glass"
AIR = "minecraft:air"


def design_room(room, put):
    """Fill meaningful zones but never overwrite chest/spawner/door placement.

    The caller applies connector cutouts and interactive NBT blocks afterwards.
    """
    w, h, d = room["size"]
    c, m = w // 2, d // 2
    kind = room["kind"]

    def block(x, y, z, name=BLACK, props=None):
        put(x, y, z, name, props)

    def slab(x, y, z, upper=False):
        block(x, y, z, SLAB, {
            "type": "top" if upper else "bottom",
            "waterlogged": "false"
        })

    def stair(x, y, z, facing="north", upper=False):
        block(x, y, z, STAIRS, {
            "facing": facing,
            "half": "top" if upper else "bottom",
            "shape": "straight",
            "waterlogged": "false"
        })

    def lantern(x, y, z, hanging=False, bright=False):
        block(x, y, z, LIGHT if bright else SOUL,
              None if bright else {"hanging": "true" if hanging else "false",
                                  "waterlogged": "false"})

    def pillar(x, z, from_y=1, to_y=None, top=CHISELED):
        top_y = (h-3 if to_y is None else to_y)
        for y in range(from_y, top_y+1):
            block(x,y,z,BLACK if y%4 else CRACKED)
        block(x,top_y+1,z,top)

    def ring(cx, cz, radius, thickness=1, material=BLACK, y=0):
        for x in range(2,w-2):
            for z in range(2,d-2):
                dist=hypot(x-cx,z-cz)
                if radius-thickness <= dist <= radius+thickness:
                    block(x,y,z,material)

    def center_clear(ymax=5):
        # A 3-wide clear main route; side-wing rooms also get an east/west
        # passage. Clear AFTER decorating so no ornamental post blocks travel.
        for z in range(1,d-1):
            for x in range(c-1,c+2):
                for y in range(1,min(h-2,ymax)+1):
                    block(x,y,z,AIR)
        if "east" in room.get("doors", {}) or "west" in room.get("doors", {}):
            for x in range(1,w-1):
                for z in range(m-1,m+2):
                    for y in range(1,min(h-2,ymax)+1):
                        block(x,y,z,AIR)

    # I. Cohesive floor: dark grout / light traffic bands, with a legible
    # centre axis and a clean ring near the walls. Not a random gray expanse.
    for x in range(1,w-1):
        for z in range(1,d-1):
            if x in (1,w-2) or z in (1,d-2):
                material=BLACK
            elif (x+z)%11==0:
                material=CRACKED
            elif (x%6==0 and z%6==0):
                material=CHISELED
            elif abs(x-c)<=1:
                material=STONE
            elif (x//3+z//3)%2==0:
                material=STONE
            else:
                material=DARK
            block(x,0,z,material)

    # II. Structural language for every room. Pair all columns and wall
    # details symmetrically; no isolated floating iron bars or stone cubes.
    rib_positions=range(4,d-3,6)
    for z in rib_positions:
        for x in (1,w-2):
            pillar(x,z,1,h-4)
            if h>=10:
                stair(2 if x==1 else w-3,h-3,z,
                      "east" if x==1 else "west",upper=True)
        if room["roof"]=="solid":
            # Vault ribs at Y>=6; preserve >5 blocks of headroom.
            for x in range(3,w-3):
                if x in (c-1,c,c+1):
                    block(x,h-3,z,CHISELED)
                elif x%4==0:
                    block(x,h-3,z,BLACK)

    # Upper wall light strips are embedded, so lighting feels intentional and
    # nearby terrain never peeks through unsupported open windows.
    for z in (4,d-5):
        for x in (1,w-2):
            if h>8:
                lantern(x,3,z,bright=True)
                block(x,4,z,CHISELED)

    # Higher archwork; doors (3x4) will be cut by the caller after this pass.
    for z in (0,d-1):
        for x in range(c-3,c+4):
            block(x,6,z,BLACK if x in (c-3,c+3) else CHISELED)
    for x in (0,w-1):
        for z in range(m-3,m+4):
            if h>8:
                block(x,6,z,BLACK if z in (m-3,m+3) else CHISELED)

    # III. Distinct silhouettes and gameplay landmarks.
    if kind=="entrance":
        # Imposing paired vestibule plinths and vaulted entry (no central cube).
        for x in (4,w-5):
            for z in (4,d-5):
                pillar(x,z,1,h-4)
                lantern(x,2,z,bright=True)
                for off in (-1,1):
                    stair(x+off,1,z,"east" if off<0 else "west")
        for x in range(3,w-3):
            if abs(x-c)>2:
                block(x,1,7,BLACK)
        for z in (3,d-4):
            for x in (1,w-2):
                for y in range(4,h-3):
                    block(x,y,z,GLASS if y%3==0 else BLACK)

    elif kind=="connector":
        # Repeated high vaulted portal frames with lit alcoves.
        for z in (4,10,14):
            for x in (2,w-3):
                pillar(x,z,1,h-4)
                slab(x,1,z-1)
                slab(x,1,z+1)
                lantern(x,2,z,bright=True)
        for x in (2,w-3):
            for z in (7,8,9):
                block(x,1,z,"minecraft:bookshelf")

    elif kind=="combat":
        # Barracks training galleries: paired weapon racks, long benches and
        # storage alcoves. Side passages always remain 3 wide.
        for x in (2,w-3):
            for z in (4,7,10,13):
                for y in (1,2):
                    block(x,y,z,"minecraft:dark_oak_planks")
                block(x,3,z,CHISELED)
            for z in (5,6,11,12):
                slab(x+1 if x==2 else x-1,1,z)
        for x in (4,w-5):
            for z in (5,d-6):
                block(x,1,z,"minecraft:dark_oak_planks")
                slab(x,2,z)

    elif kind=="puzzle":
        # Archive: paired reading galleries and illuminated end niches.
        for x in (2,w-3):
            for z in list(range(5,7))+list(range(10,d-4)):
                for y in (1,2,3,4):
                    block(x,y,z,"minecraft:bookshelf")
            for z in (4,d-5):
                lantern(x,3,z,bright=True)
        for x in (4,w-5):
            for z in (7,d-8):
                slab(x,1,z)
            lantern(x,2,7)
        for z in (5,d-6):
            for x in range(c-3,c+4):
                block(x,6,z,BLACK)

    elif kind=="crypt":
        # Tombs with coherent stone lids / shallow niches, never a loose pile.
        for x0 in (2,w-6):
            for z0 in (6,11):
                for x in range(x0,x0+3):
                    for z in range(z0,z0+2):
                        block(x,1,z,BLACK)
                        slab(x,2,z,upper=True)
                block(x0+1,3,z0,CHISELED)
        for z in (3,d-4):
            for x in (2,w-3):
                block(x,1,z,CRACKED)
        lantern(w-3,3,m,bright=True)

    elif kind=="forge":
        # Two continuous workstations rather than isolated iron-block pixels.
        for x0 in (2,w-6):
            for z in (4,5,6):
                for x in range(x0,x0+3):
                    block(x,1,z,BLACK)
                    slab(x,2,z,upper=True)
            block(x0+1,3,5,"minecraft:iron_block")
            lantern(x0+1,4,5,bright=True)
        for x in (3,w-4):
            for z in (10,11,12):
                block(x,1,z,"minecraft:polished_basalt")
                block(x,2,z,BLACK)
            block(x,3,11,CHISELED)
        for z in (3,d-4):
            block(c-4,1,z,"minecraft:magma_block")
            block(c+4,1,z,"minecraft:magma_block")

    elif kind in ("courtyard","arena","sanctum"):
        # OPEN AIR: raised perimeter terraces, columns, battle plinths and a
        # recognizable concentric floor. Keep the arena centre unobstructed.
        radius=min(w,d)//2-5
        ring(c,m,radius,1,BLACK)
        ring(c,m,radius-4,0.6,CHISELED)
        for x in range(2,w-2):
            for z in range(2,d-2):
                near_edge=min(x,z,w-1-x,d-1-z)
                # Exterior spectators/benches, clear of north/south entry.
                if near_edge in (2,3) and abs(x-c)>2:
                    slab(x,1,z)
        for x in (4,w-5):
            for z in (4,d-5):
                for y in range(1,min(h-2,7)):
                    block(x,y,z,BLACK if y%3 else CHISELED)
                lantern(x,min(h-2,7),z,bright=True)
        if kind=="courtyard":
            # Two elevated flanking shrines; deliberately offset from the
            # north/south axis and the east/west interconnecting arches.
            for x in (6,w-7):
                for z in (7,d-8):
                    for dx in (-1,0,1):
                        for dz in (-1,0,1):
                            block(x+dx,1,z+dz,BLACK)
                    block(x,2,z,CHISELED)
                    lantern(x,3,z,bright=True)
        elif kind=="arena":
            # Shallow grandstands and protective rail on the LONG sides.
            for z in (5,d-6):
                for x in range(4,w-4):
                    if abs(x-c)>3:
                        stair(x,1,z,"north" if z==5 else "south")
                        block(x,2,z+(1 if z==5 else -1),BLACK)
            for x in (5,w-6):
                for z in range(8,d-8):
                    if abs(z-m)>4:
                        stair(x,1,z,"west" if x==5 else "east")
        else:  # sanctum
            # Ceremonial ring and the two pairs of ancient pillars.
            ring(c,m,9,1,BLACK)
            ring(c,m,6,0.6,CHISELED)
            for x in (7,w-8):
                for z in (8,d-9):
                    for y in range(1,11):
                        block(x,y,z,BLACK if y%4 else CHISELED)
                    lantern(x,11,z,bright=True)

    elif kind=="tower":
        # The side-door clearance removes low lights at Z4..6; put entrance
        # lights at Z2 and Z8 so the ground floor is visible without night vision.
        for x in (1,w-2):
            for z in (2,d-3):
                lantern(x,3,z,bright=True)
        # Real walkable storeys, ladder supported by solid blackstone at x3,z2.
        for floor in (8,15,21):
            if floor>=h-1: continue
            for x in range(1,w-1):
                for z in range(1,d-1):
                    if (x,z)!=(3,3):
                        block(x,floor,z,BLACK if (x+z)%3 else STONE)
            for x,z in ((2,6),(w-3,6),(6,2),(6,d-3)):
                lantern(x,floor+1,z,bright=True)
        for y in range(1,h-1):
            block(3,y,2,BLACK)
            block(3,y,3,"minecraft:ladder",{
                "facing":"south","waterlogged":"false"})
        for y in (4,12,19):
            if y<h-2:
                for x in (0,w-1):
                    block(x,y,m,GLASS)
                for z in (0,d-1):
                    block(c,y,z,GLASS)
        for x in range(1,w-1,2):
            block(x,h-1,0,BLACK)
            block(x,h-1,d-1,BLACK)
        for z in range(1,d-1,2):
            block(0,h-1,z,BLACK)
            block(w-1,h-1,z,BLACK)

    elif kind in ("prison","catacombs"):
        # Two long walled cell banks; coherent iron-bar fronts and gloomy
        # recess lighting. Bars never cross a doorway or the centre route.
        for z in (4,8,12):
            for x in (3,w-4):
                for y in range(1,5):
                    block(x,y,z,BLACK if y in (1,4) else "minecraft:iron_bars")
                block(x,5,z,CHISELED)
        for x in (4,w-5):
            for z in (5,6,10,11):
                for y in (1,2,3):
                    block(x,y,z,"minecraft:iron_bars")
        for x in (3,w-4):
            for z in (6,11):
                block(x,0,z,"minecraft:magma_block")
                block(x,1,z,"minecraft:cobweb")
        for x in (2,w-3):
            lantern(x,3,m-3,bright=True)
            lantern(x,3,m+3,bright=True)

    elif kind=="gallery":
        # Display alcoves spaced as a library instead of a long empty runway.
        for x in (2,w-3):
            for z in range(3,d-3):
                for y in range(1,4):
                    block(x,y,z,"minecraft:bookshelf" if z%4 else BLACK)
            for z in (5,d-6):
                block(x,4,z,CHISELED)
                lantern(x,3,z,bright=True)
        for x in (4,w-5):
            for z in (5,d-6):
                block(x,1,z,"minecraft:polished_basalt")
                slab(x,2,z)

    elif kind=="threshold":
        # Ritual aisle, large paired statues and telegraphed fire patterns.
        for x in (4,w-5):
            for z in (6,d-7):
                for y in range(1,6):
                    block(x,y,z,BLACK)
                block(x,6,z,CHISELED)
                lantern(x,7,z,bright=True)
        for x in (5,w-6):
            for z in range(5,d-5):
                if z%5 in (0,1):
                    block(x,0,z,"minecraft:magma_block")
        for x in (3,w-4):
            for z in (3,d-4):
                block(x,2,z,CHISELED)

    elif kind=="vault":
        # Lower reliquary: concentric raised dais, lit pillars and guarded
        # treasure niches, staying away from the access ladder at 4,4.
        ring(c,m,5,0.8,BLACK)
        for x in (4,w-5):
            for z in (7,d-6):
                block(x,1,z,BLACK)
                slab(x,2,z,upper=True)
                lantern(x,3,z,bright=True)
        for x in (3,w-4):
            for z in (10,d-4):
                block(x,1,z,CHISELED)
        lantern(c,2,m,bright=True)

    # IV. Door clearances are non-negotiable. The caller adds actual Jigsaw
    # blocks and all chests/spawners afterwards, so no metadata is lost.
    center_clear()
