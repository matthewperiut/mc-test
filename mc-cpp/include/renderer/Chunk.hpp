#pragma once

#include "phys/AABB.hpp"
#include "renderer/Tesselator.hpp"
#include <memory>

namespace mc {

class Level;
class TileRenderer;
class VertexBuffer;
class IndexBuffer;

class Chunk {
public:
    // Chunk position in world (block coordinates)
    int x0, y0, z0;
    int x1, y1, z1;

    // Chunk size
    static constexpr int SIZE = 16;

    // State flags
    bool dirty;
    bool loaded;
    bool visible;

    // Bounding box for culling
    AABB bb;

    // Stats
    int solidVertexCount;
    int cutoutVertexCount;
    int waterVertexCount;
    int solidIndexCount;
    int cutoutIndexCount;
    int waterIndexCount;

    // Distance for sorting
    float distanceSq;

    Level* level;

    Chunk(Level* level, int x0, int y0, int z0);
    ~Chunk();

    // Rebuild the chunk mesh
    void rebuild(TileRenderer& renderer);
    void rebuildSolid(TileRenderer& renderer);
    void rebuildCutout(TileRenderer& renderer);
    void rebuildWater(TileRenderer& renderer);

    // Render the chunk
    void render(int pass);

    // Mark as needing rebuild
    void setDirty();

    // Check if position is in this chunk
    bool contains(int x, int y, int z) const;

    // Calculate distance to camera
    void calculateDistance(double camX, double camY, double camZ);

    // Delete resources
    void dispose();

private:
    void uploadData(VertexBuffer* vbo, IndexBuffer* ebo,
                    const Tesselator::VertexData& data);

    // RenderDevice buffers for solid geometry
    std::unique_ptr<VertexBuffer> solidVBO;
    std::unique_ptr<IndexBuffer> solidEBO;

    // RenderDevice buffers for cutout geometry (torches, flowers, etc.)
    std::unique_ptr<VertexBuffer> cutoutVBO;
    std::unique_ptr<IndexBuffer> cutoutEBO;

    // RenderDevice buffers for water geometry
    std::unique_ptr<VertexBuffer> waterVBO;
    std::unique_ptr<IndexBuffer> waterEBO;

    bool vaoInitialized;
};

} // namespace mc
