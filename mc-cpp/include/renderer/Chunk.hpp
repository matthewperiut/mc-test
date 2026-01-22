#pragma once

#include "phys/AABB.hpp"
#include "renderer/Tesselator.hpp"
#include <memory>
#include <mutex>
#include <atomic>

namespace mc {

class Level;
class TileRenderer;
class VertexBuffer;
class IndexBuffer;
struct ChunkMeshData;

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

    // Async mesh building support
    void setPendingMesh(std::unique_ptr<ChunkMeshData> mesh);
    bool hasPendingMesh() const;      // True if mesh is ready to upload
    bool isBuilding() const;          // True if async build is in progress
    void setBuilding(bool building);  // Mark as being built asynchronously
    bool uploadPendingMesh();         // Upload to GPU, returns true if uploaded

private:
    void uploadData(VertexBuffer* vbo, IndexBuffer* ebo,
                    const Tesselator::VertexData& data);

    // Pending mesh from async builder
    std::unique_ptr<ChunkMeshData> pendingMesh;
    mutable std::mutex meshMutex;
    std::atomic<bool> buildingAsync{false};   // True while queued/building
    std::atomic<bool> dirtyWhileBuilding{false};  // True if modified during async build

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
