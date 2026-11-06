package com.aetherium.aetherium.world;

import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The complete 17-piece Citadel blueprint.
 *
 * Every offset describes the MIN corner of the piece's transformed bounding
 * rectangle. Minecraft rotates NBT blocks around local (0, 0, 0), not around
 * their center; originFor(...) compensates for negative rotated coordinates.
 * That makes adjacent doors meet without a one-block overlap or gap.
 */
public final class CitadelLayout {
    public static final class RoomSpec {
        public final String template;
        public final int offsetX, offsetY, offsetZ;
        public final Rotation rotation;
        public final Mirror mirror;
        public final int width, depth;

        private RoomSpec(String template, int offsetX, int offsetY, int offsetZ,
                         Rotation rotation, Mirror mirror, int width, int depth) {
            this.template = template;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.rotation = rotation;
            this.mirror = mirror;
            this.width = width;
            this.depth = depth;
        }

        /**
         * TemplateStructurePiece.setup expects the original unrotated pivot,
         * not the new bounding-box min corner. Offset by the negative minima
         * of the four transformed template corners.
         */
        public BlockPos position(BlockPos origin) {
            int minX = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            for (int x : new int[] {0, width - 1}) {
                for (int z : new int[] {0, depth - 1}) {
                    int mx = mirror == Mirror.FRONT_BACK ? -x : x;
                    int mz = mirror == Mirror.LEFT_RIGHT ? -z : z;
                    int tx = mx, tz = mz;
                    if (rotation == Rotation.CLOCKWISE_90) {
                        tx = -mz;
                        tz = mx;
                    } else if (rotation == Rotation.CLOCKWISE_180) {
                        tx = -mx;
                        tz = -mz;
                    } else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
                        tx = mz;
                        tz = -mx;
                    }
                    minX = Math.min(minX, tx);
                    minZ = Math.min(minZ, tz);
                }
            }
            return origin.offset(offsetX - minX, offsetY, offsetZ - minZ);
        }
    }

    // Main route, exterior wings and finally the two undercrofts.
    // An NBT template is never larger than 31x24x31.
    private static final List<RoomSpec> ROOMS = Collections.unmodifiableList(Arrays.asList(
            new RoomSpec("gateway", 0, 0, 0, Rotation.NONE, Mirror.NONE, 19, 15),
            new RoomSpec("corridor", 4, 0, 15, Rotation.NONE, Mirror.NONE, 11, 17),
            new RoomSpec("guardhouse", 1, 0, 32, Rotation.NONE, Mirror.NONE, 17, 17),
            new RoomSpec("courtyard", -4, 0, 49, Rotation.NONE, Mirror.NONE, 27, 27),
            new RoomSpec("archive", 1, 0, 76, Rotation.NONE, Mirror.NONE, 17, 17),
            new RoomSpec("arena_shell", -4, 0, 93, Rotation.NONE, Mirror.NONE, 27, 27),
            new RoomSpec("threshold", -2, 0, 120, Rotation.NONE, Mirror.NONE, 23, 23),
            new RoomSpec("sanctum", -6, 0, 143, Rotation.NONE, Mirror.NONE, 31, 31),
            new RoomSpec("crypt", 15, 0, 15, Rotation.NONE, Mirror.NONE, 17, 17),
            new RoomSpec("forge", -16, 0, 32, Rotation.NONE, Mirror.NONE, 17, 17),
            new RoomSpec("watchtower", 23, 0, 57, Rotation.NONE, Mirror.NONE, 11, 11),
            new RoomSpec("barracks", -21, 0, 54, Rotation.CLOCKWISE_180, Mirror.NONE, 17, 17),
            new RoomSpec("prison", 18, 0, 76, Rotation.NONE, Mirror.NONE, 17, 17),
            new RoomSpec("east_bastion", 23, 0, 101, Rotation.CLOCKWISE_180, Mirror.NONE, 11, 11),
            new RoomSpec("west_gallery", -21, 0, 98, Rotation.CLOCKWISE_180, Mirror.NONE, 17, 17),
            new RoomSpec("catacombs", 15, -10, 15, Rotation.NONE, Mirror.NONE, 17, 17),
            new RoomSpec("archive_vault", 1, -9, 76, Rotation.NONE, Mirror.NONE, 17, 17)
    ));

    private CitadelLayout() {}

    public static List<RoomSpec> rooms() {
        return ROOMS;
    }

    public static BlockPos originForChunk(int chunkX, int chunkZ, int y) {
        return new BlockPos(chunkX * 16 + 8 - 9, y, chunkZ * 16 + 8 - 7);
    }
}
