package com.aetherium.aetherium.world;

import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Single source of truth for the seven-room Citadel prototype.
 *
 * World generation and the creative preview MUST use the same room ordering
 * and positions. A future architecture revision can change this layout in
 * one place without accidentally showing a different build in the preview.
 * The offsets are measured from the north-west corner of the gateway.
 */
public final class CitadelLayout {
    public static final class RoomSpec {
        public final String template;
        public final int offsetX;
        public final int offsetZ;

        private RoomSpec(String template, int offsetX, int offsetZ) {
            this.template = template;
            this.offsetX = offsetX;
            this.offsetZ = offsetZ;
        }

        public BlockPos position(BlockPos origin) {
            return origin.offset(offsetX, 0, offsetZ);
        }
    }

    private static final List<RoomSpec> ROOMS = Collections.unmodifiableList(Arrays.asList(
            new RoomSpec("gateway", 0, 0),
            new RoomSpec("corridor", 4, 15),
            new RoomSpec("guardhouse", 1, 32),
            new RoomSpec("archive", 1, 49),
            new RoomSpec("arena_shell", -4, 66),
            new RoomSpec("crypt", 15, 15),
            new RoomSpec("forge", -16, 32)
    ));

    private CitadelLayout() {}

    public static List<RoomSpec> rooms() {
        return ROOMS;
    }

    public static BlockPos originForChunk(int chunkX, int chunkZ, int y) {
        // Keep the gateway centered on the candidate chunk.
        return new BlockPos(chunkX * 16 + 8 - 9, y, chunkZ * 16 + 8 - 7);
    }
}
