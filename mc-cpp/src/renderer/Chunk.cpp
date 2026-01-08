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
    , dirty(true), loaded(false), visible(false)
    , solidVertexCount(0), waterVertexCount(0)
    , solidIndexCount(0), waterIndexCount(0)
    , distanceSq(0.0f)
    , level(level)
    , solidVAO(0), solidVBO(0), solidEBO(0)
    , waterVAO(0), waterVBO(0), waterEBO(0)
    , vaoInitialized(false)
{
    bb = AABB(x0, y0, z0, x1, y1, z1);
}

Chunk::~Chunk() {
    dispose();
}

void Chunk::dispose() {
    if (solidVAO) {
        glDeleteVertexArrays(1, &solidVAO);
        solidVAO = 0;
    }
    if (solidVBO) {
        glDeleteBuffers(1, &solidVBO);
        solidVBO = 0;
    }
    if (solidEBO) {
        glDeleteBuffers(1, &solidEBO);
        solidEBO = 0;
    }
    if (waterVAO) {
        glDeleteVertexArrays(1, &waterVAO);
        waterVAO = 0;
    }
    if (waterVBO) {
        glDeleteBuffers(1, &waterVBO);
        waterVBO = 0;
    }
    if (waterEBO) {
        glDeleteBuffers(1, &waterEBO);
        waterEBO = 0;
    }
    vaoInitialized = false;
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

void Chunk::setupVAO(GLuint vao, GLuint vbo, GLuint ebo) {
    glBindVertexArray(vao);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);

    // Position attribute (location 0) - 3 floats at offset 0
    glVertexAttribPointer(Tesselator::ATTRIB_POSITION, 3, GL_FLOAT, GL_FALSE, 32, (void*)0);
    glEnableVertexAttribArray(Tesselator::ATTRIB_POSITION);

    // TexCoord attribute (location 1) - 2 floats at offset 12
    glVertexAttribPointer(Tesselator::ATTRIB_TEXCOORD, 2, GL_FLOAT, GL_FALSE, 32, (void*)12);
    glEnableVertexAttribArray(Tesselator::ATTRIB_TEXCOORD);

    // Color attribute (location 2) - 4 unsigned bytes normalized at offset 20
    glVertexAttribPointer(Tesselator::ATTRIB_COLOR, 4, GL_UNSIGNED_BYTE, GL_TRUE, 32, (void*)20);
    glEnableVertexAttribArray(Tesselator::ATTRIB_COLOR);

    // Normal attribute (location 3) - 3 signed bytes normalized at offset 24
    glVertexAttribPointer(Tesselator::ATTRIB_NORMAL, 3, GL_BYTE, GL_TRUE, 32, (void*)24);
    glEnableVertexAttribArray(Tesselator::ATTRIB_NORMAL);

    // Light attribute (location 4) - 2 unsigned bytes at offset 28 (skyLight, blockLight)
    glVertexAttribPointer(Tesselator::ATTRIB_LIGHT, 2, GL_UNSIGNED_BYTE, GL_FALSE, 32, (void*)28);
    glEnableVertexAttribArray(Tesselator::ATTRIB_LIGHT);

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
    glBindVertexArray(0);
}

void Chunk::uploadData(GLuint vao, GLuint vbo, GLuint ebo,
                       const Tesselator::VertexData& data) {
    glBindVertexArray(vao);

    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferData(GL_ARRAY_BUFFER, data.vertices.size() * sizeof(int),
                 data.vertices.data(), GL_STATIC_DRAW);

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, data.indices.size() * sizeof(unsigned int),
                 data.indices.data(), GL_STATIC_DRAW);

    glBindVertexArray(0);
}

void Chunk::rebuild(TileRenderer& renderer) {
    if (!level) return;

    // Create VAOs if needed
    if (!vaoInitialized) {
        glGenVertexArrays(1, &solidVAO);
        glGenBuffers(1, &solidVBO);
        glGenBuffers(1, &solidEBO);
        setupVAO(solidVAO, solidVBO, solidEBO);

        glGenVertexArrays(1, &waterVAO);
        glGenBuffers(1, &waterVBO);
        glGenBuffers(1, &waterEBO);
        setupVAO(waterVAO, waterVBO, waterEBO);

        vaoInitialized = true;
    }

    rebuildSolid(renderer);
    rebuildWater(renderer);
    dirty = false;
    loaded = true;
}

void Chunk::rebuildSolid(TileRenderer& renderer) {
    Tesselator& t = Tesselator::getInstance();
    t.begin(GL_QUADS);

    solidVertexCount = 0;

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
            }
        }
    }

    solidVertexCount = t.getVertexCount();

    // Get vertex data without drawing
    Tesselator::VertexData data = t.getVertexData();
    solidIndexCount = static_cast<int>(data.indices.size());

    if (solidIndexCount > 0) {
        uploadData(solidVAO, solidVBO, solidEBO, data);
    }
}

void Chunk::rebuildWater(TileRenderer& renderer) {
    Tesselator& t = Tesselator::getInstance();
    t.begin(GL_QUADS);

    waterVertexCount = 0;

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
            }
        }
    }

    waterVertexCount = t.getVertexCount();

    // Get vertex data without drawing
    Tesselator::VertexData data = t.getVertexData();
    waterIndexCount = static_cast<int>(data.indices.size());

    if (waterIndexCount > 0) {
        uploadData(waterVAO, waterVBO, waterEBO, data);
    }
}

void Chunk::render(int pass) {
    if (!loaded) return;

    if (pass == 0) {
        // Solid pass
        if (solidIndexCount > 0 && solidVAO) {
            glBindVertexArray(solidVAO);
            glDrawElements(GL_TRIANGLES, solidIndexCount, GL_UNSIGNED_INT, 0);
            glBindVertexArray(0);
        }
    } else {
        // Water pass
        if (waterIndexCount > 0 && waterVAO) {
            glBindVertexArray(waterVAO);
            glDrawElements(GL_TRIANGLES, waterIndexCount, GL_UNSIGNED_INT, 0);
            glBindVertexArray(0);
        }
    }
}

} // namespace mc
