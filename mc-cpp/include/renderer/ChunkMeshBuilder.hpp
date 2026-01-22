#pragma once

#include "renderer/Tesselator.hpp"
#include "renderer/ChunkSnapshot.hpp"
#include <vector>
#include <queue>
#include <mutex>
#include <thread>
#include <condition_variable>
#include <atomic>
#include <memory>
#include <functional>

namespace mc {

class Chunk;
class Level;

/**
 * ChunkMeshData - Contains the generated mesh data for a chunk.
 * Built on worker thread, uploaded to GPU on main thread.
 */
struct ChunkMeshData {
    Tesselator::VertexData solid;
    Tesselator::VertexData cutout;
    Tesselator::VertexData water;
    std::atomic<bool> ready{false};

    ChunkMeshData() = default;
    ~ChunkMeshData() = default;

    // Non-copyable but movable
    ChunkMeshData(const ChunkMeshData&) = delete;
    ChunkMeshData& operator=(const ChunkMeshData&) = delete;
    ChunkMeshData(ChunkMeshData&& other) noexcept;
    ChunkMeshData& operator=(ChunkMeshData&& other) noexcept;
};

/**
 * ChunkTask - A chunk pending mesh generation.
 */
struct ChunkTask {
    Chunk* chunk;
    ChunkSnapshot snapshot;
    int priority;  // Lower = higher priority (distance-based)

    bool operator<(const ChunkTask& other) const {
        // Priority queue gives highest first, we want lowest priority first
        return priority > other.priority;
    }
};

/**
 * ChunkMeshBuilder - Thread pool for parallel chunk mesh generation.
 * Workers build mesh data from snapshots, main thread uploads to GPU.
 */
class ChunkMeshBuilder {
public:
    // numThreads = 0 means auto (hardware_concurrency - 1, minimum 1)
    explicit ChunkMeshBuilder(int numThreads = 0);
    ~ChunkMeshBuilder();

    // Queue a chunk for mesh rebuild (snapshot captured immediately)
    void queueChunk(Chunk* chunk, Level* level, int priority);

    // Get chunks that have completed mesh generation (call from main thread)
    std::vector<Chunk*> getCompletedChunks();

    // Check if any work is pending
    bool hasPendingWork() const;

    // Get stats
    size_t getPendingCount() const;
    size_t getCompletedCount() const;

private:
    void workerFunction();
    void buildChunkMesh(ChunkTask& task);

    // Worker threads
    std::vector<std::thread> workers;
    std::atomic<bool> running{true};

    // Task queue (priority queue - closest chunks first)
    std::priority_queue<ChunkTask> taskQueue;
    mutable std::mutex queueMutex;
    std::condition_variable workAvailable;

    // Completed chunks ready for GPU upload
    std::vector<Chunk*> completedChunks;
    mutable std::mutex completedMutex;
};

} // namespace mc
