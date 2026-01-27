#include "pathfinder/AsyncPathFinder.hpp"
#include "entity/Entity.hpp"
#include "world/Level.hpp"
#include "world/tile/Tile.hpp"
#include "util/Mth.hpp"
#include <cmath>

namespace mc {

// BlockSnapshot implementation

BlockSnapshot::BlockSnapshot(Level* level, int centerX, int centerZ, int radius, int height)
    : minX(centerX - radius)
    , minZ(centerZ - radius)
    , maxX(centerX + radius)
    , maxZ(centerZ + radius)
    , width(radius * 2 + 1)
    , depth(radius * 2 + 1)
    , height(height)
{
    // Copy blocks from level
    blocks.resize(static_cast<size_t>(width) * height * depth, 0);

    for (int x = minX; x <= maxX; ++x) {
        for (int z = minZ; z <= maxZ; ++z) {
            for (int y = 0; y < height; ++y) {
                if (level->isInBounds(x, y, z)) {
                    int localX = x - minX;
                    int localZ = z - minZ;
                    int idx = (y * depth + localZ) * width + localX;
                    blocks[idx] = static_cast<uint8_t>(level->getTile(x, y, z));
                }
            }
        }
    }
}

int BlockSnapshot::getTile(int x, int y, int z) const {
    if (x < minX || x > maxX || z < minZ || z > maxZ || y < 0 || y >= height) {
        return 0;  // Air for out of bounds
    }

    int localX = x - minX;
    int localZ = z - minZ;
    int idx = (y * depth + localZ) * width + localX;
    return blocks[idx];
}

// AsyncPathFinder implementation

AsyncPathFinder::AsyncPathFinder(int numWorkers)
    : running(true)
    , nextRequestId(1)
{
    for (int i = 0; i < numWorkers; ++i) {
        workers.emplace_back(&AsyncPathFinder::workerLoop, this);
    }
}

AsyncPathFinder::~AsyncPathFinder() {
    running = false;
    workAvailable.notify_all();

    for (auto& worker : workers) {
        if (worker.joinable()) {
            worker.join();
        }
    }
}

int AsyncPathFinder::queueRequest(Entity* entity, double targetX, double targetY, double targetZ,
                                  float maxDist, Level* level) {
    if (!entity || !level) return -1;

    // Create block snapshot (32 block radius should cover most paths)
    int radius = static_cast<int>(maxDist) + 4;
    auto snapshot = std::make_shared<BlockSnapshot>(
        level,
        static_cast<int>(entity->x),
        static_cast<int>(entity->z),
        radius,
        level->height
    );

    PathfindingRequest request;
    request.requestId = nextRequestId++;
    request.entityId = entity->entityId;
    request.bbWidth = entity->bbWidth;
    request.bbHeight = entity->bbHeight;
    request.startX = entity->bb.x0;
    request.startY = entity->bb.y0;
    request.startZ = entity->bb.z0;
    request.targetX = targetX;
    request.targetY = targetY;
    request.targetZ = targetZ;
    request.maxDist = maxDist;
    request.snapshot = snapshot;

    // Calculate distance for priority
    double dx = targetX - entity->x;
    double dy = targetY - entity->y;
    double dz = targetZ - entity->z;
    request.distanceToTarget = static_cast<float>(std::sqrt(dx*dx + dy*dy + dz*dz));

    {
        std::lock_guard<std::mutex> lock(queueMutex);
        // Cancel any existing request for this entity
        pendingRequests[entity->entityId] = request.requestId;
        requestQueue.push(std::move(request));
    }

    workAvailable.notify_one();
    return request.requestId;
}

std::vector<PathfindingResult> AsyncPathFinder::getCompletedPaths(int entityId) {
    std::lock_guard<std::mutex> lock(completedMutex);
    std::vector<PathfindingResult> results;
    std::vector<PathfindingResult> remaining;
    for (auto& r : completedPaths) {
        if (r.entityId == entityId) {
            results.push_back(std::move(r));
        } else {
            remaining.push_back(std::move(r));
        }
    }
    completedPaths = std::move(remaining);
    return results;
}

void AsyncPathFinder::cancelRequests(int entityId) {
    std::lock_guard<std::mutex> lock(queueMutex);
    pendingRequests.erase(entityId);
}

bool AsyncPathFinder::hasPendingRequest(int entityId) const {
    std::lock_guard<std::mutex> lock(queueMutex);
    return pendingRequests.find(entityId) != pendingRequests.end();
}

void AsyncPathFinder::workerLoop() {
    while (running) {
        PathfindingRequest request;

        {
            std::unique_lock<std::mutex> lock(queueMutex);
            workAvailable.wait(lock, [this] {
                return !running || !requestQueue.empty();
            });

            if (!running && requestQueue.empty()) {
                return;
            }

            if (requestQueue.empty()) {
                continue;
            }

            request = std::move(const_cast<PathfindingRequest&>(requestQueue.top()));
            requestQueue.pop();

            // Check if request was cancelled
            auto it = pendingRequests.find(request.entityId);
            if (it == pendingRequests.end() || it->second != request.requestId) {
                continue;  // Request was cancelled or superseded
            }
        }

        // Compute path (outside lock)
        auto path = findPath(request);

        // Store result
        {
            std::lock_guard<std::mutex> lock(completedMutex);
            completedPaths.push_back({request.requestId, request.entityId, std::move(path)});
        }

        // Clear pending status
        {
            std::lock_guard<std::mutex> lock(queueMutex);
            auto it = pendingRequests.find(request.entityId);
            if (it != pendingRequests.end() && it->second == request.requestId) {
                pendingRequests.erase(it);
            }
        }
    }
}

std::unique_ptr<Path> AsyncPathFinder::findPath(const PathfindingRequest& request) {
    std::unordered_map<int, std::unique_ptr<Node>> nodes;
    BinaryHeap openSet;

    int sizeX = Mth::floor(request.bbWidth + 1.0f);
    int sizeY = Mth::floor(request.bbHeight + 1.0f);
    int sizeZ = Mth::floor(request.bbWidth + 1.0f);

    Node* start = getOrCreateNode(
        Mth::floor(request.startX),
        Mth::floor(request.startY),
        Mth::floor(request.startZ),
        nodes
    );

    Node* goal = getOrCreateNode(
        Mth::floor(request.targetX - request.bbWidth / 2.0f),
        Mth::floor(request.targetY),
        Mth::floor(request.targetZ - request.bbWidth / 2.0f),
        nodes
    );

    start->g = 0.0f;
    start->h = start->distanceTo(*goal);
    start->f = start->h;

    openSet.insert(start);
    Node* closest = start;

    while (!openSet.isEmpty()) {
        Node* current = openSet.pop();

        if (current->hash == goal->hash) {
            // Reconstruct path
            int length = 1;
            for (Node* n = goal; n->cameFrom != nullptr; n = n->cameFrom) {
                ++length;
            }

            std::vector<std::unique_ptr<Node>> pathNodes;
            pathNodes.resize(length);

            Node* n = goal;
            int idx = length - 1;
            while (n != nullptr) {
                pathNodes[idx] = std::make_unique<Node>(n->x, n->y, n->z);
                n = n->cameFrom;
                --idx;
            }

            return std::make_unique<Path>(std::move(pathNodes));
        }

        if (current->distanceTo(*goal) < closest->distanceTo(*goal)) {
            closest = current;
        }

        current->closed = true;

        // Get neighbors
        int stepUp = 0;
        if (isFree(*request.snapshot, current->x, current->y + 1, current->z, sizeX, sizeY, sizeZ) > 0) {
            stepUp = 1;
        }

        std::vector<Node*> neighbors;
        Node* n1 = getNode(*request.snapshot, current->x, current->y, current->z + 1, sizeX, sizeY, sizeZ, stepUp, nodes);
        Node* n2 = getNode(*request.snapshot, current->x - 1, current->y, current->z, sizeX, sizeY, sizeZ, stepUp, nodes);
        Node* n3 = getNode(*request.snapshot, current->x + 1, current->y, current->z, sizeX, sizeY, sizeZ, stepUp, nodes);
        Node* n4 = getNode(*request.snapshot, current->x, current->y, current->z - 1, sizeX, sizeY, sizeZ, stepUp, nodes);

        if (n1 && !n1->closed && n1->distanceTo(*goal) < request.maxDist) neighbors.push_back(n1);
        if (n2 && !n2->closed && n2->distanceTo(*goal) < request.maxDist) neighbors.push_back(n2);
        if (n3 && !n3->closed && n3->distanceTo(*goal) < request.maxDist) neighbors.push_back(n3);
        if (n4 && !n4->closed && n4->distanceTo(*goal) < request.maxDist) neighbors.push_back(n4);

        for (Node* neighbor : neighbors) {
            float tentativeG = current->g + current->distanceTo(*neighbor);

            if (!neighbor->inOpenSet() || tentativeG < neighbor->g) {
                neighbor->cameFrom = current;
                neighbor->g = tentativeG;
                neighbor->h = neighbor->distanceTo(*goal);

                if (neighbor->inOpenSet()) {
                    openSet.changeCost(neighbor, neighbor->g + neighbor->h);
                } else {
                    neighbor->f = neighbor->g + neighbor->h;
                    openSet.insert(neighbor);
                }
            }
        }
    }

    // If we couldn't reach the goal, return path to closest point
    if (closest == start) {
        return nullptr;
    }

    // Reconstruct path to closest
    int length = 1;
    for (Node* n = closest; n->cameFrom != nullptr; n = n->cameFrom) {
        ++length;
    }

    std::vector<std::unique_ptr<Node>> pathNodes;
    pathNodes.resize(length);

    Node* n = closest;
    int idx = length - 1;
    while (n != nullptr) {
        pathNodes[idx] = std::make_unique<Node>(n->x, n->y, n->z);
        n = n->cameFrom;
        --idx;
    }

    return std::make_unique<Path>(std::move(pathNodes));
}

int AsyncPathFinder::isFree(const BlockSnapshot& snapshot, int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
    for (int bx = x; bx < x + sizeX; ++bx) {
        for (int by = y; by < y + sizeY; ++by) {
            for (int bz = z; bz < z + sizeZ; ++bz) {
                int tileId = snapshot.getTile(bx, by, bz);
                if (tileId > 0 && tileId < 256) {
                    Tile* tile = Tile::tiles[tileId].get();
                    if (tile) {
                        if (tile->solid) {
                            return 0;  // Blocked
                        }
                        if (tileId == Tile::WATER || tileId == Tile::STILL_WATER ||
                            tileId == Tile::LAVA || tileId == Tile::STILL_LAVA) {
                            return -1;  // Liquid
                        }
                    }
                }
            }
        }
    }
    return 1;  // Free
}

Node* AsyncPathFinder::getNode(const BlockSnapshot& snapshot, int x, int y, int z,
                                int sizeX, int sizeY, int sizeZ, int stepUp,
                                std::unordered_map<int, std::unique_ptr<Node>>& nodes) {
    Node* node = nullptr;

    if (isFree(snapshot, x, y, z, sizeX, sizeY, sizeZ) > 0) {
        node = getOrCreateNode(x, y, z, nodes);
    }

    if (node == nullptr && isFree(snapshot, x, y + stepUp, z, sizeX, sizeY, sizeZ) > 0) {
        node = getOrCreateNode(x, y + stepUp, z, nodes);
        y += stepUp;
    }

    if (node != nullptr) {
        int fallCount = 0;
        int freeResult;

        while (y > 0 && (freeResult = isFree(snapshot, x, y - 1, z, sizeX, sizeY, sizeZ)) > 0) {
            if (freeResult < 0) {
                return nullptr;
            }
            ++fallCount;
            if (fallCount >= 4) {
                return nullptr;
            }
            --y;
        }

        if (y > 0) {
            node = getOrCreateNode(x, y, z, nodes);
        }
    }

    return node;
}

Node* AsyncPathFinder::getOrCreateNode(int x, int y, int z, std::unordered_map<int, std::unique_ptr<Node>>& nodes) {
    int hash = x | (y << 10) | (z << 20);
    auto it = nodes.find(hash);
    if (it != nodes.end()) {
        return it->second.get();
    }

    auto node = std::make_unique<Node>(x, y, z);
    Node* ptr = node.get();
    nodes[hash] = std::move(node);
    return ptr;
}

} // namespace mc
