#include "renderer/ChunkSnapshot.hpp"
#include "world/Level.hpp"
#include "world/tile/Tile.hpp"

namespace mc {

ChunkSnapshot::ChunkSnapshot()
    : baseX(0), baseY(0), baseZ(0)
{
    blocks.fill(0);
    metadata.fill(0);
    skyLight.fill(15);  // Default to full sky light
    blockLight.fill(0);
}

void ChunkSnapshot::capture(Level* level, int chunkX, int chunkY, int chunkZ) {
    if (!level) return;

    baseX = chunkX;
    baseY = chunkY;
    baseZ = chunkZ;

    // Capture 18x18x18 region: chunk + 1-block margin
    for (int ly = -1; ly <= 16; ly++) {
        for (int lz = -1; lz <= 16; lz++) {
            for (int lx = -1; lx <= 16; lx++) {
                int wx = baseX + lx;
                int wy = baseY + ly;
                int wz = baseZ + lz;

                int idx = getIndex(lx, ly, lz);

                if (level->isInBounds(wx, wy, wz)) {
                    blocks[idx] = static_cast<uint8_t>(level->getTile(wx, wy, wz));
                    metadata[idx] = static_cast<uint8_t>(level->getData(wx, wy, wz));
                    skyLight[idx] = static_cast<uint8_t>(level->getSkyLight(wx, wy, wz));
                    blockLight[idx] = static_cast<uint8_t>(level->getBlockLight(wx, wy, wz));
                } else {
                    // Out of bounds: air with full sky light
                    blocks[idx] = 0;
                    metadata[idx] = 0;
                    skyLight[idx] = 15;
                    blockLight[idx] = 0;
                }
            }
        }
    }
}

uint8_t ChunkSnapshot::getTile(int localX, int localY, int localZ) const {
    if (localX < -1 || localX > 16 || localY < -1 || localY > 16 || localZ < -1 || localZ > 16) {
        return 0;  // Air for out of snapshot bounds
    }
    return blocks[getIndex(localX, localY, localZ)];
}

uint8_t ChunkSnapshot::getMetadata(int localX, int localY, int localZ) const {
    if (localX < -1 || localX > 16 || localY < -1 || localY > 16 || localZ < -1 || localZ > 16) {
        return 0;
    }
    return metadata[getIndex(localX, localY, localZ)];
}

uint8_t ChunkSnapshot::getSkyLight(int localX, int localY, int localZ) const {
    if (localX < -1 || localX > 16 || localY < -1 || localY > 16 || localZ < -1 || localZ > 16) {
        return 15;  // Full sky light outside bounds
    }
    return skyLight[getIndex(localX, localY, localZ)];
}

uint8_t ChunkSnapshot::getBlockLight(int localX, int localY, int localZ) const {
    if (localX < -1 || localX > 16 || localY < -1 || localY > 16 || localZ < -1 || localZ > 16) {
        return 0;
    }
    return blockLight[getIndex(localX, localY, localZ)];
}

bool ChunkSnapshot::isTransparent(int localX, int localY, int localZ) const {
    uint8_t tileId = getTile(localX, localY, localZ);
    if (tileId == 0) return true;  // Air is transparent

    Tile* tile = Tile::tiles[tileId].get();
    return !tile || tile->transparent;
}

} // namespace mc
