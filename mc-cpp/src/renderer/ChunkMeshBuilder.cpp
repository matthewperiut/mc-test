#include "renderer/ChunkMeshBuilder.hpp"
#include "renderer/Chunk.hpp"
#include "world/Level.hpp"
#include "world/tile/Tile.hpp"
#include <algorithm>

namespace mc {

// ChunkMeshData move operations
ChunkMeshData::ChunkMeshData(ChunkMeshData&& other) noexcept
    : solid(std::move(other.solid))
    , cutout(std::move(other.cutout))
    , water(std::move(other.water))
{
    ready.store(other.ready.load());
}

ChunkMeshData& ChunkMeshData::operator=(ChunkMeshData&& other) noexcept {
    if (this != &other) {
        solid = std::move(other.solid);
        cutout = std::move(other.cutout);
        water = std::move(other.water);
        ready.store(other.ready.load());
    }
    return *this;
}

// ChunkMeshBuilder implementation
ChunkMeshBuilder::ChunkMeshBuilder(int numThreads) {
    if (numThreads <= 0) {
        numThreads = std::max(1, static_cast<int>(std::thread::hardware_concurrency()) - 1);
    }

    // Initialize the tesselator pool with enough tesselators for all workers
    TesselatorPool::getInstance().initialize(numThreads);

    // Start worker threads
    for (int i = 0; i < numThreads; i++) {
        workers.emplace_back(&ChunkMeshBuilder::workerFunction, this);
    }
}

ChunkMeshBuilder::~ChunkMeshBuilder() {
    running = false;
    workAvailable.notify_all();

    for (auto& thread : workers) {
        if (thread.joinable()) {
            thread.join();
        }
    }

    TesselatorPool::getInstance().destroy();
}

void ChunkMeshBuilder::queueChunk(Chunk* chunk, Level* level, int priority) {
    if (!chunk || !level) return;

    ChunkTask task;
    task.chunk = chunk;
    task.priority = priority;
    // Capture snapshot on main thread (safe Level access)
    task.snapshot.capture(level, chunk->x0, chunk->y0, chunk->z0);

    {
        std::lock_guard<std::mutex> lock(queueMutex);
        taskQueue.push(std::move(task));
    }
    workAvailable.notify_one();
}

std::vector<Chunk*> ChunkMeshBuilder::getCompletedChunks() {
    std::lock_guard<std::mutex> lock(completedMutex);
    std::vector<Chunk*> result = std::move(completedChunks);
    completedChunks.clear();
    return result;
}

bool ChunkMeshBuilder::hasPendingWork() const {
    std::lock_guard<std::mutex> lock(queueMutex);
    return !taskQueue.empty();
}

size_t ChunkMeshBuilder::getPendingCount() const {
    std::lock_guard<std::mutex> lock(queueMutex);
    return taskQueue.size();
}

size_t ChunkMeshBuilder::getCompletedCount() const {
    std::lock_guard<std::mutex> lock(completedMutex);
    return completedChunks.size();
}

void ChunkMeshBuilder::workerFunction() {
    while (running) {
        ChunkTask task;
        bool hasWork = false;

        {
            std::unique_lock<std::mutex> lock(queueMutex);
            if (!taskQueue.empty()) {
                task = std::move(const_cast<ChunkTask&>(taskQueue.top()));
                taskQueue.pop();
                hasWork = true;
            }
        }

        if (hasWork) {
            buildChunkMesh(task);

            // Mark as completed
            {
                std::lock_guard<std::mutex> lock(completedMutex);
                completedChunks.push_back(task.chunk);
            }
        } else {
            std::unique_lock<std::mutex> lock(queueMutex);
            workAvailable.wait_for(lock, std::chrono::milliseconds(10),
                [this] { return !running || !taskQueue.empty(); });
        }
    }
}

// Helper class for building mesh from snapshot (similar to TileRenderer but thread-safe)
class SnapshotMeshBuilder {
public:
    SnapshotMeshBuilder(Tesselator& t, const ChunkSnapshot& snap)
        : t(t), snapshot(snap) {}

    void buildSolid(Tesselator::VertexData& outData);
    void buildCutout(Tesselator::VertexData& outData);
    void buildWater(Tesselator::VertexData& outData);

private:
    void renderTile(Tile* tile, int lx, int ly, int lz);
    void renderCube(Tile* tile, int lx, int ly, int lz);
    void renderCross(Tile* tile, int lx, int ly, int lz);
    void renderTorch(Tile* tile, int lx, int ly, int lz, int metadata);
    void renderLiquid(Tile* tile, int lx, int ly, int lz);
    void renderCactus(Tile* tile, int lx, int ly, int lz);

    bool shouldRenderFace(int lx, int ly, int lz, int face);
    void getUV(int textureIndex, float& u0, float& v0, float& u1, float& v1);

    void renderFaceDown(Tile* tile, double x, double y, double z, int texture);
    void renderFaceUp(Tile* tile, double x, double y, double z, int texture);
    void renderFaceNorth(Tile* tile, double x, double y, double z, int texture);
    void renderFaceSouth(Tile* tile, double x, double y, double z, int texture);
    void renderFaceWest(Tile* tile, double x, double y, double z, int texture);
    void renderFaceEast(Tile* tile, double x, double y, double z, int texture);

    Tesselator& t;
    const ChunkSnapshot& snapshot;
};

void SnapshotMeshBuilder::getUV(int textureIndex, float& u0, float& v0, float& u1, float& v1) {
    int xt = (textureIndex & 15) << 4;
    int yt = textureIndex & 240;
    u0 = static_cast<float>(xt) / 256.0f;
    u1 = (static_cast<float>(xt) + 15.99f) / 256.0f;
    v0 = static_cast<float>(yt) / 256.0f;
    v1 = (static_cast<float>(yt) + 15.99f) / 256.0f;
}

bool SnapshotMeshBuilder::shouldRenderFace(int lx, int ly, int lz, int face) {
    int nx = lx, ny = ly, nz = lz;
    switch (face) {
        case 0: ny--; break;
        case 1: ny++; break;
        case 2: nz--; break;
        case 3: nz++; break;
        case 4: nx--; break;
        case 5: nx++; break;
    }
    return snapshot.isTransparent(nx, ny, nz);
}

void SnapshotMeshBuilder::buildSolid(Tesselator::VertexData& outData) {
    t.begin(DrawMode::Quads);

    for (int ly = 0; ly < 16; ly++) {
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                uint8_t tileId = snapshot.getTile(lx, ly, lz);
                if (tileId == 0) continue;

                Tile* tile = Tile::tiles[tileId].get();
                if (!tile) continue;

                // Skip water/lava and cutout for solid pass
                if (tile->renderShape == TileShape::LIQUID) continue;
                if (tile->renderLayer == TileLayer::CUTOUT) continue;

                renderTile(tile, lx, ly, lz);
            }
        }
    }

    outData = t.getVertexData();
}

void SnapshotMeshBuilder::buildCutout(Tesselator::VertexData& outData) {
    t.begin(DrawMode::Quads);

    for (int ly = 0; ly < 16; ly++) {
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                uint8_t tileId = snapshot.getTile(lx, ly, lz);
                if (tileId == 0) continue;

                Tile* tile = Tile::tiles[tileId].get();
                if (!tile) continue;

                // Only cutout tiles
                if (tile->renderLayer != TileLayer::CUTOUT) continue;

                renderTile(tile, lx, ly, lz);
            }
        }
    }

    outData = t.getVertexData();
}

void SnapshotMeshBuilder::buildWater(Tesselator::VertexData& outData) {
    t.begin(DrawMode::Quads);

    for (int ly = 0; ly < 16; ly++) {
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                uint8_t tileId = snapshot.getTile(lx, ly, lz);
                if (tileId == 0) continue;

                Tile* tile = Tile::tiles[tileId].get();
                if (!tile) continue;

                // Only liquids
                if (tile->renderShape != TileShape::LIQUID) continue;

                renderTile(tile, lx, ly, lz);
            }
        }
    }

    outData = t.getVertexData();
}

void SnapshotMeshBuilder::renderTile(Tile* tile, int lx, int ly, int lz) {
    switch (tile->renderShape) {
        case TileShape::CUBE:
            renderCube(tile, lx, ly, lz);
            break;
        case TileShape::CROSS:
            renderCross(tile, lx, ly, lz);
            break;
        case TileShape::TORCH:
            renderTorch(tile, lx, ly, lz, snapshot.getMetadata(lx, ly, lz));
            break;
        case TileShape::LIQUID:
            renderLiquid(tile, lx, ly, lz);
            break;
        case TileShape::CACTUS:
            renderCactus(tile, lx, ly, lz);
            break;
        default:
            renderCube(tile, lx, ly, lz);
            break;
    }
}

void SnapshotMeshBuilder::renderCube(Tile* tile, int lx, int ly, int lz) {
    // World coordinates
    int x = snapshot.baseX + lx;
    int y = snapshot.baseY + ly;
    int z = snapshot.baseZ + lz;

    float c0 = 0.5f, c1 = 1.0f, c2 = 0.8f, c3 = 0.6f;
    bool isGrass = (tile->id == Tile::GRASS);
    float grassR = 0.486f, grassG = 0.741f, grassB = 0.420f;

    if (shouldRenderFace(lx, ly, lz, 0)) {
        t.lightLevel(snapshot.getSkyLight(lx, ly - 1, lz), snapshot.getBlockLight(lx, ly - 1, lz));
        t.color(c0, c0, c0);
        renderFaceDown(tile, x, y, z, tile->getTexture(0));
    }
    if (shouldRenderFace(lx, ly, lz, 1)) {
        t.lightLevel(snapshot.getSkyLight(lx, ly + 1, lz), snapshot.getBlockLight(lx, ly + 1, lz));
        if (isGrass) {
            t.color(c1 * grassR, c1 * grassG, c1 * grassB);
        } else {
            t.color(c1, c1, c1);
        }
        renderFaceUp(tile, x, y, z, tile->getTexture(1));
    }
    if (shouldRenderFace(lx, ly, lz, 2)) {
        t.lightLevel(snapshot.getSkyLight(lx, ly, lz - 1), snapshot.getBlockLight(lx, ly, lz - 1));
        t.color(c2, c2, c2);
        renderFaceNorth(tile, x, y, z, tile->getTexture(2));
    }
    if (shouldRenderFace(lx, ly, lz, 3)) {
        t.lightLevel(snapshot.getSkyLight(lx, ly, lz + 1), snapshot.getBlockLight(lx, ly, lz + 1));
        t.color(c2, c2, c2);
        renderFaceSouth(tile, x, y, z, tile->getTexture(3));
    }
    if (shouldRenderFace(lx, ly, lz, 4)) {
        t.lightLevel(snapshot.getSkyLight(lx - 1, ly, lz), snapshot.getBlockLight(lx - 1, ly, lz));
        t.color(c3, c3, c3);
        renderFaceWest(tile, x, y, z, tile->getTexture(4));
    }
    if (shouldRenderFace(lx, ly, lz, 5)) {
        t.lightLevel(snapshot.getSkyLight(lx + 1, ly, lz), snapshot.getBlockLight(lx + 1, ly, lz));
        t.color(c3, c3, c3);
        renderFaceEast(tile, x, y, z, tile->getTexture(5));
    }
}

void SnapshotMeshBuilder::renderFaceDown(Tile*, double x, double y, double z, int texture) {
    float u0, v0, u1, v1;
    getUV(texture, u0, v0, u1, v1);
    t.vertexUV(x, y, z + 1, u0, v1);
    t.vertexUV(x, y, z, u0, v0);
    t.vertexUV(x + 1, y, z, u1, v0);
    t.vertexUV(x + 1, y, z + 1, u1, v1);
}

void SnapshotMeshBuilder::renderFaceUp(Tile*, double x, double y, double z, int texture) {
    float u0, v0, u1, v1;
    getUV(texture, u0, v0, u1, v1);
    t.vertexUV(x, y + 1, z, u0, v0);
    t.vertexUV(x, y + 1, z + 1, u0, v1);
    t.vertexUV(x + 1, y + 1, z + 1, u1, v1);
    t.vertexUV(x + 1, y + 1, z, u1, v0);
}

void SnapshotMeshBuilder::renderFaceNorth(Tile*, double x, double y, double z, int texture) {
    float u0, v0, u1, v1;
    getUV(texture, u0, v0, u1, v1);
    t.vertexUV(x, y + 1, z, u1, v0);
    t.vertexUV(x + 1, y + 1, z, u0, v0);
    t.vertexUV(x + 1, y, z, u0, v1);
    t.vertexUV(x, y, z, u1, v1);
}

void SnapshotMeshBuilder::renderFaceSouth(Tile*, double x, double y, double z, int texture) {
    float u0, v0, u1, v1;
    getUV(texture, u0, v0, u1, v1);
    t.vertexUV(x, y, z + 1, u0, v1);
    t.vertexUV(x + 1, y, z + 1, u1, v1);
    t.vertexUV(x + 1, y + 1, z + 1, u1, v0);
    t.vertexUV(x, y + 1, z + 1, u0, v0);
}

void SnapshotMeshBuilder::renderFaceWest(Tile*, double x, double y, double z, int texture) {
    float u0, v0, u1, v1;
    getUV(texture, u0, v0, u1, v1);
    t.vertexUV(x, y + 1, z + 1, u1, v0);
    t.vertexUV(x, y + 1, z, u0, v0);
    t.vertexUV(x, y, z, u0, v1);
    t.vertexUV(x, y, z + 1, u1, v1);
}

void SnapshotMeshBuilder::renderFaceEast(Tile*, double x, double y, double z, int texture) {
    float u0, v0, u1, v1;
    getUV(texture, u0, v0, u1, v1);
    t.vertexUV(x + 1, y, z + 1, u0, v1);
    t.vertexUV(x + 1, y, z, u1, v1);
    t.vertexUV(x + 1, y + 1, z, u1, v0);
    t.vertexUV(x + 1, y + 1, z + 1, u0, v0);
}

void SnapshotMeshBuilder::renderCross(Tile* tile, int lx, int ly, int lz) {
    int x = snapshot.baseX + lx;
    int y = snapshot.baseY + ly;
    int z = snapshot.baseZ + lz;

    float u0, v0, u1, v1;
    getUV(tile->textureIndex, u0, v0, u1, v1);

    t.lightLevel(snapshot.getSkyLight(lx, ly, lz), snapshot.getBlockLight(lx, ly, lz));
    t.color(1.0f, 1.0f, 1.0f);

    double d = 0.45;

    t.vertexUV(x + 0.5 - d, y, z + 0.5 - d, u0, v1);
    t.vertexUV(x + 0.5 - d, y + 1, z + 0.5 - d, u0, v0);
    t.vertexUV(x + 0.5 + d, y + 1, z + 0.5 + d, u1, v0);
    t.vertexUV(x + 0.5 + d, y, z + 0.5 + d, u1, v1);

    t.vertexUV(x + 0.5 + d, y, z + 0.5 + d, u0, v1);
    t.vertexUV(x + 0.5 + d, y + 1, z + 0.5 + d, u0, v0);
    t.vertexUV(x + 0.5 - d, y + 1, z + 0.5 - d, u1, v0);
    t.vertexUV(x + 0.5 - d, y, z + 0.5 - d, u1, v1);

    t.vertexUV(x + 0.5 - d, y, z + 0.5 + d, u0, v1);
    t.vertexUV(x + 0.5 - d, y + 1, z + 0.5 + d, u0, v0);
    t.vertexUV(x + 0.5 + d, y + 1, z + 0.5 - d, u1, v0);
    t.vertexUV(x + 0.5 + d, y, z + 0.5 - d, u1, v1);

    t.vertexUV(x + 0.5 + d, y, z + 0.5 - d, u0, v1);
    t.vertexUV(x + 0.5 + d, y + 1, z + 0.5 - d, u0, v0);
    t.vertexUV(x + 0.5 - d, y + 1, z + 0.5 + d, u1, v0);
    t.vertexUV(x + 0.5 - d, y, z + 0.5 + d, u1, v1);
}

void SnapshotMeshBuilder::renderTorch(Tile* tile, int lx, int ly, int lz, int metadata) {
    int x = snapshot.baseX + lx;
    int y = snapshot.baseY + ly;
    int z = snapshot.baseZ + lz;

    int tex = tile->textureIndex;
    int xt = (tex & 15) << 4;
    int yt = tex & 240;

    float u0 = static_cast<float>(xt) / 256.0f;
    float u1 = (static_cast<float>(xt) + 15.99f) / 256.0f;
    float v0 = static_cast<float>(yt) / 256.0f;
    float v1 = (static_cast<float>(yt) + 15.99f) / 256.0f;

    double uc0 = static_cast<double>(u0) + 0.02734375;
    double vc0 = static_cast<double>(v0) + 0.0234375;
    double uc1 = static_cast<double>(u0) + 0.03515625;
    double vc1 = static_cast<double>(v0) + 0.03125;

    t.lightLevel(snapshot.getSkyLight(lx, ly, lz), snapshot.getBlockLight(lx, ly, lz));
    t.color(1.0f, 1.0f, 1.0f);

    double px = static_cast<double>(x);
    double py = static_cast<double>(y);
    double pz = static_cast<double>(z);
    double xxa = 0.0, zza = 0.0;

    double tiltAmount = 0.4;
    double heightOffset = 0.2;
    double posOffset = 0.1;

    switch (metadata) {
        case 1: px = x - posOffset; py = y + heightOffset; xxa = -tiltAmount; break;
        case 2: px = x + posOffset; py = y + heightOffset; xxa = tiltAmount; break;
        case 3: pz = z - posOffset; py = y + heightOffset; zza = -tiltAmount; break;
        case 4: pz = z + posOffset; py = y + heightOffset; zza = tiltAmount; break;
        default: break;
    }

    px += 0.5;
    pz += 0.5;

    double bx0 = px - 0.5, bx1 = px + 0.5;
    double bz0 = pz - 0.5, bz1 = pz + 0.5;
    double r = 0.0625, h = 0.625;

    double topOffX = xxa * (1.0 - h);
    double topOffZ = zza * (1.0 - h);
    t.vertexUV(px + topOffX - r, py + h, pz + topOffZ - r, uc0, vc0);
    t.vertexUV(px + topOffX - r, py + h, pz + topOffZ + r, uc0, vc1);
    t.vertexUV(px + topOffX + r, py + h, pz + topOffZ + r, uc1, vc1);
    t.vertexUV(px + topOffX + r, py + h, pz + topOffZ - r, uc1, vc0);

    t.vertexUV(px - r, py + 1.0, bz0, u0, v0);
    t.vertexUV(px - r + xxa, py, bz0 + zza, u0, v1);
    t.vertexUV(px - r + xxa, py, bz1 + zza, u1, v1);
    t.vertexUV(px - r, py + 1.0, bz1, u1, v0);

    t.vertexUV(px + r, py + 1.0, bz1, u0, v0);
    t.vertexUV(px + r + xxa, py, bz1 + zza, u0, v1);
    t.vertexUV(px + r + xxa, py, bz0 + zza, u1, v1);
    t.vertexUV(px + r, py + 1.0, bz0, u1, v0);

    t.vertexUV(bx0, py + 1.0, pz + r, u0, v0);
    t.vertexUV(bx0 + xxa, py, pz + r + zza, u0, v1);
    t.vertexUV(bx1 + xxa, py, pz + r + zza, u1, v1);
    t.vertexUV(bx1, py + 1.0, pz + r, u1, v0);

    t.vertexUV(bx1, py + 1.0, pz - r, u0, v0);
    t.vertexUV(bx1 + xxa, py, pz - r + zza, u0, v1);
    t.vertexUV(bx0 + xxa, py, pz - r + zza, u1, v1);
    t.vertexUV(bx0, py + 1.0, pz - r, u1, v0);
}

void SnapshotMeshBuilder::renderLiquid(Tile* tile, int lx, int ly, int lz) {
    int x = snapshot.baseX + lx;
    int y = snapshot.baseY + ly;
    int z = snapshot.baseZ + lz;

    float u0, v0, u1, v1;
    getUV(tile->textureIndex, u0, v0, u1, v1);

    t.lightLevel(snapshot.getSkyLight(lx, ly, lz), snapshot.getBlockLight(lx, ly, lz));
    t.color(1.0f, 1.0f, 1.0f);

    double h = 0.875;

    if (shouldRenderFace(lx, ly, lz, 1)) {
        t.vertexUV(x, y + h, z, u0, v0);
        t.vertexUV(x, y + h, z + 1, u0, v1);
        t.vertexUV(x + 1, y + h, z + 1, u1, v1);
        t.vertexUV(x + 1, y + h, z, u1, v0);
    }

    if (shouldRenderFace(lx, ly, lz, 0)) renderFaceDown(tile, x, y, z, tile->textureIndex);
    if (shouldRenderFace(lx, ly, lz, 2)) renderFaceNorth(tile, x, y, z, tile->textureIndex);
    if (shouldRenderFace(lx, ly, lz, 3)) renderFaceSouth(tile, x, y, z, tile->textureIndex);
    if (shouldRenderFace(lx, ly, lz, 4)) renderFaceWest(tile, x, y, z, tile->textureIndex);
    if (shouldRenderFace(lx, ly, lz, 5)) renderFaceEast(tile, x, y, z, tile->textureIndex);
}

void SnapshotMeshBuilder::renderCactus(Tile* tile, int lx, int ly, int lz) {
    int x = snapshot.baseX + lx;
    int y = snapshot.baseY + ly;
    int z = snapshot.baseZ + lz;

    float u0, v0, u1, v1;
    double offset = 0.0625;

    t.lightLevel(snapshot.getSkyLight(lx, ly, lz), snapshot.getBlockLight(lx, ly, lz));

    getUV(tile->getTexture(2), u0, v0, u1, v1);
    t.color(0.8f, 0.8f, 0.8f);

    t.vertexUV(x, y + 1, z + offset, u1, v0);
    t.vertexUV(x + 1, y + 1, z + offset, u0, v0);
    t.vertexUV(x + 1, y, z + offset, u0, v1);
    t.vertexUV(x, y, z + offset, u1, v1);

    t.vertexUV(x, y, z + 1 - offset, u0, v1);
    t.vertexUV(x + 1, y, z + 1 - offset, u1, v1);
    t.vertexUV(x + 1, y + 1, z + 1 - offset, u1, v0);
    t.vertexUV(x, y + 1, z + 1 - offset, u0, v0);

    t.color(0.6f, 0.6f, 0.6f);

    t.vertexUV(x + offset, y + 1, z + 1, u1, v0);
    t.vertexUV(x + offset, y + 1, z, u0, v0);
    t.vertexUV(x + offset, y, z, u0, v1);
    t.vertexUV(x + offset, y, z + 1, u1, v1);

    t.vertexUV(x + 1 - offset, y, z + 1, u0, v1);
    t.vertexUV(x + 1 - offset, y, z, u1, v1);
    t.vertexUV(x + 1 - offset, y + 1, z, u1, v0);
    t.vertexUV(x + 1 - offset, y + 1, z + 1, u0, v0);

    if (shouldRenderFace(lx, ly, lz, 1)) {
        t.lightLevel(snapshot.getSkyLight(lx, ly + 1, lz), snapshot.getBlockLight(lx, ly + 1, lz));
        t.color(1.0f, 1.0f, 1.0f);
        renderFaceUp(tile, x, y, z, tile->getTexture(1));
    }
    if (shouldRenderFace(lx, ly, lz, 0)) {
        t.lightLevel(snapshot.getSkyLight(lx, ly - 1, lz), snapshot.getBlockLight(lx, ly - 1, lz));
        t.color(0.5f, 0.5f, 0.5f);
        renderFaceDown(tile, x, y, z, tile->getTexture(0));
    }
}

void ChunkMeshBuilder::buildChunkMesh(ChunkTask& task) {
    // Acquire a tesselator from the pool
    Tesselator* t = TesselatorPool::getInstance().acquire();

    // Create pending mesh data
    auto meshData = std::make_unique<ChunkMeshData>();

    // Build meshes using the snapshot
    SnapshotMeshBuilder builder(*t, task.snapshot);
    builder.buildSolid(meshData->solid);
    builder.buildCutout(meshData->cutout);
    builder.buildWater(meshData->water);

    meshData->ready.store(true);

    // Store pending mesh data in the chunk
    task.chunk->setPendingMesh(std::move(meshData));

    // Release tesselator back to pool
    TesselatorPool::getInstance().release(t);
}

} // namespace mc
