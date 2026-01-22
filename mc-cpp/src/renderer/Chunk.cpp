#include "renderer/Chunk.hpp"
#include "renderer/TileRenderer.hpp"
#include "renderer/Tesselator.hpp"
#include "renderer/backend/RenderDevice.hpp"
#include "renderer/backend/VertexBuffer.hpp"
#include "world/Level.hpp"
#include "world/tile/Tile.hpp"

namespace mc {

Chunk::Chunk(Level* level, int x0, int y0, int z0)
    : x0(x0), y0(y0), z0(z0)
    , x1(x0 + SIZE), y1(y0 + SIZE), z1(z0 + SIZE)
    , dirty(true), loaded(false), visible(false)
    , solidVertexCount(0), cutoutVertexCount(0), waterVertexCount(0)
    , solidIndexCount(0), cutoutIndexCount(0), waterIndexCount(0)
    , distanceSq(0.0f)
    , level(level)
    , solidVBO(nullptr), solidEBO(nullptr)
    , cutoutVBO(nullptr), cutoutEBO(nullptr)
    , waterVBO(nullptr), waterEBO(nullptr)
    , vaoInitialized(false)
{
    bb = AABB(x0, y0, z0, x1, y1, z1);
}

Chunk::~Chunk() {
    dispose();
}

void Chunk::dispose() {
    solidVBO.reset();
    solidEBO.reset();
    cutoutVBO.reset();
    cutoutEBO.reset();
    waterVBO.reset();
    waterEBO.reset();
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

void Chunk::uploadData(VertexBuffer* vbo, IndexBuffer* ebo,
                       const Tesselator::VertexData& data) {
    if (!vbo || !ebo) return;

    vbo->upload(data.vertices.data(), data.vertices.size() * sizeof(int), BufferUsage::Static);
    ebo->upload(data.indices.data(), data.indices.size(), BufferUsage::Static);
}

void Chunk::rebuild(TileRenderer& renderer) {
    if (!level) return;

    // Create buffers if needed
    if (!vaoInitialized) {
        auto& device = RenderDevice::get();
        solidVBO = device.createVertexBuffer();
        solidEBO = device.createIndexBuffer();
        cutoutVBO = device.createVertexBuffer();
        cutoutEBO = device.createIndexBuffer();
        waterVBO = device.createVertexBuffer();
        waterEBO = device.createIndexBuffer();
        vaoInitialized = true;
    }

    rebuildSolid(renderer);
    rebuildCutout(renderer);
    rebuildWater(renderer);
    dirty = false;
    loaded = true;
}

void Chunk::rebuildSolid(TileRenderer& renderer) {
    Tesselator& t = Tesselator::getInstance();
    t.begin(DrawMode::Quads);

    solidVertexCount = 0;

    for (int x = x0; x < x1; x++) {
        for (int y = y0; y < y1; y++) {
            for (int z = z0; z < z1; z++) {
                int tileId = level->getTile(x, y, z);
                if (tileId <= 0) continue;

                Tile* tile = Tile::tiles[tileId].get();
                if (!tile) continue;

                // Skip water/lava for solid pass
                if (tile->renderShape == TileShape::LIQUID) continue;

                // Skip cutout tiles (torches, flowers, etc.) for solid pass
                if (tile->renderLayer == TileLayer::CUTOUT) continue;

                renderer.renderTile(tile, x, y, z);
            }
        }
    }

    solidVertexCount = t.getVertexCount();

    // Get vertex data without drawing
    Tesselator::VertexData data = t.getVertexData();
    solidIndexCount = static_cast<int>(data.indices.size());

    if (solidIndexCount > 0) {
        uploadData(solidVBO.get(), solidEBO.get(), data);
    }
}

void Chunk::rebuildCutout(TileRenderer& renderer) {
    Tesselator& t = Tesselator::getInstance();
    t.begin(DrawMode::Quads);

    cutoutVertexCount = 0;

    for (int x = x0; x < x1; x++) {
        for (int y = y0; y < y1; y++) {
            for (int z = z0; z < z1; z++) {
                int tileId = level->getTile(x, y, z);
                if (tileId <= 0) continue;

                Tile* tile = Tile::tiles[tileId].get();
                if (!tile) continue;

                // Only render cutout tiles (torches, flowers, etc.) in this pass
                if (tile->renderLayer != TileLayer::CUTOUT) continue;

                renderer.renderTile(tile, x, y, z);
            }
        }
    }

    cutoutVertexCount = t.getVertexCount();

    // Get vertex data without drawing
    Tesselator::VertexData data = t.getVertexData();
    cutoutIndexCount = static_cast<int>(data.indices.size());

    if (cutoutIndexCount > 0) {
        uploadData(cutoutVBO.get(), cutoutEBO.get(), data);
    }
}

void Chunk::rebuildWater(TileRenderer& renderer) {
    Tesselator& t = Tesselator::getInstance();
    t.begin(DrawMode::Quads);

    waterVertexCount = 0;

    for (int x = x0; x < x1; x++) {
        for (int y = y0; y < y1; y++) {
            for (int z = z0; z < z1; z++) {
                int tileId = level->getTile(x, y, z);
                if (tileId <= 0) continue;

                Tile* tile = Tile::tiles[tileId].get();
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
        uploadData(waterVBO.get(), waterEBO.get(), data);
    }
}

void Chunk::render(int pass) {
    if (!loaded) return;

    auto& device = RenderDevice::get();

    if (pass == 0) {
        // Solid pass
        if (solidIndexCount > 0 && solidVBO && solidEBO) {
            solidVBO->bind();
            solidEBO->bind();
            device.setupVertexAttributes();
            device.drawIndexed(PrimitiveType::Triangles, solidIndexCount);
            solidVBO->unbind();
        }
    } else if (pass == 1) {
        // Cutout pass (alpha-tested: torches, flowers, etc.)
        if (cutoutIndexCount > 0 && cutoutVBO && cutoutEBO) {
            cutoutVBO->bind();
            cutoutEBO->bind();
            device.setupVertexAttributes();
            device.drawIndexed(PrimitiveType::Triangles, cutoutIndexCount);
            cutoutVBO->unbind();
        }
    } else {
        // Water pass (pass == 2)
        if (waterIndexCount > 0 && waterVBO && waterEBO) {
            waterVBO->bind();
            waterEBO->bind();
            device.setupVertexAttributes();
            device.drawIndexed(PrimitiveType::Triangles, waterIndexCount);
            waterVBO->unbind();
        }
    }
}

} // namespace mc
