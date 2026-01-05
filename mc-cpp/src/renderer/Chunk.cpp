#include "renderer/Chunk.hpp"
#include "renderer/TileRenderer.hpp"
#include "renderer/Tesselator.hpp"
#include "world/Level.hpp"
#include "world/tile/Tile.hpp"
#include <GL/glew.h>

namespace mc {

Chunk::Chunk(Level* level, int x0, int y0, int z0)
    : x0(x0), y0(y0), z0(z0)
    , x1(x0 + SIZE), y1(y0 + SIZE), z1(z0 + SIZE)
    , displayList(0), displayListWater(0)
    , dirty(true), loaded(false), visible(false)
    , solidVertexCount(0), waterVertexCount(0)
    , distanceSq(0.0f)
    , level(level)
{
    bb = AABB(x0, y0, z0, x1, y1, z1);
}

Chunk::~Chunk() {
    dispose();
}

void Chunk::dispose() {
    if (displayList) {
        glDeleteLists(displayList, 1);
        displayList = 0;
    }
    if (displayListWater) {
        glDeleteLists(displayListWater, 1);
        displayListWater = 0;
    }
}

void Chunk::setDirty() {
    dirty = true;
}

bool Chunk::contains(int x, int y, int z) const {
    return x >= x0 && x < x1 &&
           y >= y0 && y < y1 &&
           z >= z0 && z < z1;
}

void Chunk::calculateDistance(double camX, double camY, double camZ) {
    double dx = (x0 + x1) / 2.0 - camX;
    double dy = (y0 + y1) / 2.0 - camY;
    double dz = (z0 + z1) / 2.0 - camZ;
    distanceSq = static_cast<float>(dx * dx + dy * dy + dz * dz);
}

void Chunk::rebuild(TileRenderer& renderer) {
    if (!level) return;

    rebuildSolid(renderer);
    rebuildWater(renderer);
    dirty = false;
    loaded = true;
}

void Chunk::rebuildSolid(TileRenderer& renderer) {
    // Create display list if needed
    if (!displayList) {
        displayList = glGenLists(1);
    }

    glNewList(displayList, GL_COMPILE);

    Tesselator& t = Tesselator::getInstance();
    t.begin(GL_QUADS);

    solidVertexCount = 0;
    bool hasContent = false;

    for (int x = x0; x < x1; x++) {
        for (int y = y0; y < y1; y++) {
            for (int z = z0; z < z1; z++) {
                int tileId = level->getTile(x, y, z);
                if (tileId <= 0) continue;

                Tile* tile = Tile::tiles[tileId];
                if (!tile) continue;

                // Skip water/lava for solid pass
                if (tile->renderShape == TileShape::LIQUID) continue;

                renderer.renderTile(tile, x, y, z);
                hasContent = true;
            }
        }
    }

    solidVertexCount = t.getVertexCount();
    t.end();

    glEndList();

    if (!hasContent) {
        solidVertexCount = 0;
    }
}

void Chunk::rebuildWater(TileRenderer& renderer) {
    // Create display list if needed
    if (!displayListWater) {
        displayListWater = glGenLists(1);
    }

    glNewList(displayListWater, GL_COMPILE);

    Tesselator& t = Tesselator::getInstance();
    t.begin(GL_QUADS);

    waterVertexCount = 0;
    bool hasContent = false;

    for (int x = x0; x < x1; x++) {
        for (int y = y0; y < y1; y++) {
            for (int z = z0; z < z1; z++) {
                int tileId = level->getTile(x, y, z);
                if (tileId <= 0) continue;

                Tile* tile = Tile::tiles[tileId];
                if (!tile) continue;

                // Only render water/lava in this pass
                if (tile->renderShape != TileShape::LIQUID) continue;

                renderer.renderTile(tile, x, y, z);
                hasContent = true;
            }
        }
    }

    waterVertexCount = t.getVertexCount();
    t.end();

    glEndList();

    if (!hasContent) {
        waterVertexCount = 0;
    }
}

void Chunk::render(int pass) {
    if (!loaded) return;

    if (pass == 0) {
        // Solid pass
        if (solidVertexCount > 0 && displayList) {
            glCallList(displayList);
        }
    } else {
        // Water pass
        if (waterVertexCount > 0 && displayListWater) {
            glCallList(displayListWater);
        }
    }
}

} // namespace mc
