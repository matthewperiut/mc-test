#pragma once

#include <array>
#include <cstdint>

namespace mc {

class Level;

/**
 * ChunkSnapshot - Immutable snapshot of chunk data for thread-safe mesh building.
 * Captures a 18x18x18 region (16x16x16 chunk + 1-block margin for neighbor access).
 */
struct ChunkSnapshot {
    // 18^3 = 5832 blocks (16 + 1 margin on each side)
    static constexpr int SNAPSHOT_SIZE = 18;
    static constexpr int VOLUME = SNAPSHOT_SIZE * SNAPSHOT_SIZE * SNAPSHOT_SIZE;

    std::array<uint8_t, VOLUME> blocks;      // Tile IDs
    std::array<uint8_t, VOLUME> metadata;    // Block metadata
    std::array<uint8_t, VOLUME> skyLight;    // Sky light levels (0-15)
    std::array<uint8_t, VOLUME> blockLight;  // Block light levels (0-15)

    // Base world coordinates (corner of the 16x16x16 chunk)
    int baseX, baseY, baseZ;

    ChunkSnapshot();

    // Capture snapshot from level (call from main thread)
    void capture(Level* level, int chunkX, int chunkY, int chunkZ);

    // Get data at local coordinates (-1 to 16 inclusive = 0 to 17 in snapshot space)
    inline int getIndex(int localX, int localY, int localZ) const {
        // Shift coordinates: local -1 becomes snapshot 0
        int sx = localX + 1;
        int sy = localY + 1;
        int sz = localZ + 1;
        return (sy * SNAPSHOT_SIZE + sz) * SNAPSHOT_SIZE + sx;
    }

    uint8_t getTile(int localX, int localY, int localZ) const;
    uint8_t getMetadata(int localX, int localY, int localZ) const;
    uint8_t getSkyLight(int localX, int localY, int localZ) const;
    uint8_t getBlockLight(int localX, int localY, int localZ) const;

    // Check if neighbor is transparent (for face culling)
    bool isTransparent(int localX, int localY, int localZ) const;
};

} // namespace mc
