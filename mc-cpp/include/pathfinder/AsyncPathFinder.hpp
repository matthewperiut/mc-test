#pragma once

#include "pathfinder/Path.hpp"
#include "pathfinder/Node.hpp"
#include "pathfinder/BinaryHeap.hpp"
#include <vector>
#include <queue>
#include <thread>
#include <mutex>
#include <condition_variable>
#include <atomic>
#include <memory>
#include <unordered_map>

namespace mc {

class Entity;
class Level;

/**
 * Snapshot of level blocks for thread-safe pathfinding.
 * Captures a region of blocks at the time of request.
 */
class BlockSnapshot {
public:
    BlockSnapshot(Level* level, int centerX, int centerZ, int radius, int height);

    // Get tile at world coordinates (returns 0 if outside snapshot)
    int getTile(int x, int y, int z) const;

private:
    std::vector<uint8_t> blocks;
    int minX, minZ, maxX, maxZ;
    int width, depth, height;
};

/**
 * Request for async pathfinding.
 */
struct PathfindingRequest {
    int requestId;
    int entityId;

    // Entity info needed for pathfinding
    float bbWidth, bbHeight;
    double startX, startY, startZ;  // Entity bounding box min corner
    double targetX, targetY, targetZ;
    float maxDist;

    // Block snapshot for thread-safe access
    std::shared_ptr<BlockSnapshot> snapshot;

    // Priority for queue (lower = higher priority)
    float distanceToTarget;

    bool operator>(const PathfindingRequest& other) const {
        return distanceToTarget > other.distanceToTarget;
    }
};

/**
 * Result of async pathfinding.
 */
struct PathfindingResult {
    int requestId;
    int entityId;
    std::unique_ptr<Path> path;  // nullptr if no path found
};

/**
 * Async pathfinder with worker thread pool.
 * Follows the ChunkMeshBuilder pattern for thread management.
 */
class AsyncPathFinder {
public:
    AsyncPathFinder(int numWorkers = 2);
    ~AsyncPathFinder();

    // Queue a pathfinding request, returns request ID
    int queueRequest(Entity* entity, double targetX, double targetY, double targetZ,
                     float maxDist, Level* level);

    // Poll for completed paths (call from main thread)
    std::vector<PathfindingResult> getCompletedPaths();

    // Cancel all pending requests for an entity
    void cancelRequests(int entityId);

    // Check if there's a pending request for an entity
    bool hasPendingRequest(int entityId) const;

private:
    void workerLoop();

    // Pathfinding implementation using snapshot
    std::unique_ptr<Path> findPath(const PathfindingRequest& request);
    int isFree(const BlockSnapshot& snapshot, int x, int y, int z, int sizeX, int sizeY, int sizeZ);
    Node* getNode(const BlockSnapshot& snapshot, int x, int y, int z, int sizeX, int sizeY, int sizeZ, int stepUp,
                  std::unordered_map<int, std::unique_ptr<Node>>& nodes);
    Node* getOrCreateNode(int x, int y, int z, std::unordered_map<int, std::unique_ptr<Node>>& nodes);

    std::vector<std::thread> workers;
    std::priority_queue<PathfindingRequest, std::vector<PathfindingRequest>, std::greater<PathfindingRequest>> requestQueue;
    std::vector<PathfindingResult> completedPaths;

    mutable std::mutex queueMutex;
    mutable std::mutex completedMutex;
    std::condition_variable workAvailable;

    std::atomic<bool> running;
    std::atomic<int> nextRequestId;

    // Track pending requests per entity
    std::unordered_map<int, int> pendingRequests;  // entityId -> requestId
};

} // namespace mc
