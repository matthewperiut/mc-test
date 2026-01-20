#include "world/LightingEngine.hpp"
#include "world/Level.hpp"
#include "world/tile/Tile.hpp"
#include <algorithm>
#include <cmath>
#include <unordered_set>
#include <tuple>
#include <utility>

namespace mc {

// LightUpdate methods
bool LightUpdate::expandToContain(int nx0, int ny0, int nz0, int nx1, int ny1, int nz1) {
    // Matching Java LightUpdate.expandToContain exactly

    // First check: is new region completely contained within current region?
    if (nx0 >= x0 && ny0 >= y0 && nz0 >= z0 &&
        nx1 <= x1 && ny1 <= y1 && nz1 <= z1) {
        return true;  // Already contained, no expansion needed
    }

    // Second check: within 1-block margin AND volume increase <= 2?
    if (nx0 >= x0 - 1 && ny0 >= y0 - 1 && nz0 >= z0 - 1 &&
        nx1 <= x1 + 1 && ny1 <= y1 + 1 && nz1 <= z1 + 1) {

        // Calculate old volume
        int oldVolume = (x1 - x0 + 1) * (y1 - y0 + 1) * (z1 - z0 + 1);

        // Calculate new bounds
        int newX0 = std::min(x0, nx0);
        int newY0 = std::min(y0, ny0);
        int newZ0 = std::min(z0, nz0);
        int newX1 = std::max(x1, nx1);
        int newY1 = std::max(y1, ny1);
        int newZ1 = std::max(z1, nz1);

        // Calculate new volume
        int newVolume = (newX1 - newX0 + 1) * (newY1 - newY0 + 1) * (newZ1 - newZ0 + 1);

        // Only merge if volume increase is <= 2 (matching Java)
        if (newVolume - oldVolume <= 2) {
            x0 = newX0;
            y0 = newY0;
            z0 = newZ0;
            x1 = newX1;
            y1 = newY1;
            z1 = newZ1;
            return true;
        }
    }

    return false;
}

// LightingEngine methods
LightingEngine::LightingEngine()
    : level(nullptr)
    , multithreaded(false)
    , running(false)
    , recurseCount(0)
{
}

LightingEngine::~LightingEngine() {
    setMultithreaded(false);
}

void LightingEngine::setLevel(Level* newLevel) {
    level = newLevel;
}

void LightingEngine::setMultithreaded(bool enabled) {
    if (enabled == multithreaded) return;

    if (enabled) {
        running = true;
        int numThreads = std::max(1, static_cast<int>(std::thread::hardware_concurrency()) - 1);
        for (int i = 0; i < numThreads; i++) {
            workerThreads.emplace_back(&LightingEngine::workerFunction, this);
        }
        multithreaded = true;
    } else {
        running = false;
        workAvailable.notify_all();
        for (auto& thread : workerThreads) {
            if (thread.joinable()) {
                thread.join();
            }
        }
        workerThreads.clear();
        multithreaded = false;
    }
}

void LightingEngine::workerFunction() {
    while (running) {
        LightUpdate update;
        bool hasWork = false;

        {
            std::unique_lock<std::mutex> lock(queueMutex);
            if (!updateQueue.empty()) {
                update = updateQueue.front();
                updateQueue.erase(updateQueue.begin());
                hasWork = true;
            }
        }

        if (hasWork) {
            processUpdate(update);
        } else {
            std::unique_lock<std::mutex> lock(workMutex);
            workAvailable.wait_for(lock, std::chrono::milliseconds(10));
        }
    }
}

size_t LightingEngine::getPendingUpdateCount() const {
    std::lock_guard<std::mutex> lock(queueMutex);
    return updateQueue.size();
}

void LightingEngine::queueUpdate(LightLayer layer, int x0, int y0, int z0, int x1, int y1, int z1) {
    if (!level) return;

    // Clamp to world bounds
    x0 = std::max(0, std::min(x0, level->width - 1));
    y0 = std::max(0, std::min(y0, level->height - 1));
    z0 = std::max(0, std::min(z0, level->depth - 1));
    x1 = std::max(0, std::min(x1, level->width - 1));
    y1 = std::max(0, std::min(y1, level->height - 1));
    z1 = std::max(0, std::min(z1, level->depth - 1));

    // Volume check (Java limit: 32768)
    int volume = (x1 - x0 + 1) * (y1 - y0 + 1) * (z1 - z0 + 1);
    if (volume > 32768) {
        return;
    }

    std::lock_guard<std::mutex> lock(queueMutex);

    // Try to merge with last 5 updates (matching Java)
    int checkCount = std::min(5, static_cast<int>(updateQueue.size()));
    for (int i = 0; i < checkCount; i++) {
        int idx = static_cast<int>(updateQueue.size()) - i - 1;
        if (idx >= 0 && updateQueue[idx].layer == layer) {
            if (updateQueue[idx].expandToContain(x0, y0, z0, x1, y1, z1)) {
                return;
            }
        }
    }

    updateQueue.emplace_back(layer, x0, y0, z0, x1, y1, z1);

    // Limit queue size
    if (updateQueue.size() > 1000000) {
        updateQueue.erase(updateQueue.begin());
    }

    if (multithreaded) {
        workAvailable.notify_one();
    }
}

void LightingEngine::queueUpdateAt(int x, int y, int z) {
    if (!level) return;
    if (!level->isInBounds(x, y, z)) return;

    // Get old light values before any changes
    int oldBlockLight = getBrightness(LightLayer::BLOCK, x, y, z);

    // Get new source values (what the block itself produces)
    int newBlockSource = getLightSource(LightLayer::BLOCK, x, y, z);

    // If block light decreased (e.g., torch removed), remove old light first
    if (oldBlockLight > 0 && newBlockSource < oldBlockLight) {
        removeLightBFS(LightLayer::BLOCK, x, y, z, oldBlockLight);
    }

    // Handle sky light column-wise (matching Java behavior)
    // When a block is placed/removed, it affects the entire column below
    int tileId = level->getTile(x, y, z);
    int blockValue = (tileId > 0 && tileId < 256) ? Tile::lightBlock[tileId] : 0;

    if (blockValue > 0) {
        // Block was placed - remove sky light from column below using BFS darkness propagation
        // This properly handles light "flowing out" of the now-dark room

        // First handle the placed block position itself
        int currentLightAtPlaced = getBrightness(LightLayer::SKY, x, y, z);
        if (currentLightAtPlaced > 0 && !isSkyLit(x, y, z)) {
            removeLightBFS(LightLayer::SKY, x, y, z, currentLightAtPlaced);
        }

        // Then handle the column below the placed block
        for (int cy = y - 1; cy >= 0; cy--) {
            int currentLight = getBrightness(LightLayer::SKY, x, cy, z);
            if (currentLight > 0) {
                // Check if this position is still sky-lit (might be if block placed below heightmap)
                if (!isSkyLit(x, cy, z)) {
                    // Remove light using BFS - this will propagate darkness and re-light from other sources
                    removeLightBFS(LightLayer::SKY, x, cy, z, currentLight);
                }
            }
            // Stop if we hit a solid block (light below wouldn't have come from this column anyway)
            int tile = level->getTile(x, cy, z);
            int blockVal = (tile > 0 && tile < 256) ? Tile::lightBlock[tile] : 0;
            if (blockVal >= 15) break;
        }
    } else {
        // Block was removed - sky light can now propagate down
        // Set sky light for newly exposed column and track positions that need horizontal spread
        std::vector<std::pair<int, int>> litPositions;  // (y, lightLevel)

        int light = 15;
        for (int cy = level->height - 1; cy >= 0; cy--) {
            int tile = level->getTile(x, cy, z);
            int blockVal = (tile > 0 && tile < 256) ? Tile::lightBlock[tile] : 0;

            // Apply attenuation
            light -= blockVal;
            if (light < 0) light = 0;

            int currentLight = getBrightness(LightLayer::SKY, x, cy, z);
            if (light > currentLight) {
                setBrightness(LightLayer::SKY, x, cy, z, light);
                // Track this position for horizontal propagation
                if (light > 1) {
                    litPositions.emplace_back(cy, light);
                }
            }

            if (light <= 0 && cy < y) break;  // Stop if light exhausted below change point
        }

        // Propagate horizontally from ALL newly lit positions in the column
        // This ensures light spreads into rooms when ceiling is broken
        for (const auto& [cy, lightLevel] : litPositions) {
            // Directly propagate to horizontal neighbors
            static const int hdx[] = {-1, 1, 0, 0};
            static const int hdz[] = {0, 0, -1, 1};
            for (int i = 0; i < 4; i++) {
                int nx = x + hdx[i];
                int nz = z + hdz[i];
                if (level->isInBounds(nx, cy, nz)) {
                    propagateLightImmediateBFS(LightLayer::SKY, nx, cy, nz);
                }
            }
        }
    }

    // Propagate sky light from the changed position
    propagateLightImmediateBFS(LightLayer::SKY, x, y, z);

    // Propagate block light from the changed position
    propagateLightImmediateBFS(LightLayer::BLOCK, x, y, z);

    // Also propagate from all 6 neighbors so light can "flow around" the placed block
    static const int dx[] = {-1, 1, 0, 0, 0, 0};
    static const int dy[] = {0, 0, -1, 1, 0, 0};
    static const int dz[] = {0, 0, 0, 0, -1, 1};
    for (int i = 0; i < 6; i++) {
        int nx = x + dx[i];
        int ny = y + dy[i];
        int nz = z + dz[i];
        if (level->isInBounds(nx, ny, nz)) {
            propagateLightImmediateBFS(LightLayer::BLOCK, nx, ny, nz);
            propagateLightImmediateBFS(LightLayer::SKY, nx, ny, nz);
        }
    }
}

void LightingEngine::removeLightBFS(LightLayer layer, int startX, int startY, int startZ, int oldLightValue) {
    if (!level) return;

    // Direction offsets for 6 neighbors
    static const int dx[] = {-1, 1, 0, 0, 0, 0};
    static const int dy[] = {0, 0, -1, 1, 0, 0};
    static const int dz[] = {0, 0, 0, 0, -1, 1};

    // Queue for removing light: (x, y, z, lightValue)
    std::vector<std::tuple<int, int, int, int>> removeQueue;
    // Queue for re-adding light from other sources
    std::vector<std::tuple<int, int, int>> readdQueue;

    // Start by setting the source position to 0
    setBrightness(layer, startX, startY, startZ, 0);
    removeQueue.emplace_back(startX, startY, startZ, oldLightValue);

    size_t head = 0;
    while (head < removeQueue.size()) {
        auto [x, y, z, lightVal] = removeQueue[head++];

        // Check all 6 neighbors
        for (int i = 0; i < 6; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            int nz = z + dz[i];

            if (!level->isInBounds(nx, ny, nz)) continue;

            int neighborLight = getBrightness(layer, nx, ny, nz);

            if (neighborLight != 0 && neighborLight < lightVal) {
                // This neighbor was receiving light from the removed source
                // Remove its light and continue propagating darkness
                setBrightness(layer, nx, ny, nz, 0);
                removeQueue.emplace_back(nx, ny, nz, neighborLight);
            } else if (neighborLight > 0 && neighborLight >= lightVal) {
                // This neighbor has light from another source
                // Add it to re-propagation queue
                readdQueue.emplace_back(nx, ny, nz);
            }
        }

        // Safety limit
        if (removeQueue.size() > 50000) break;
    }

    // Re-propagate light from remaining sources
    for (const auto& [rx, ry, rz] : readdQueue) {
        propagateLightImmediateBFS(layer, rx, ry, rz);
    }
}

void LightingEngine::propagateLightImmediateBFS(LightLayer layer, int startX, int startY, int startZ) {
    if (!level) return;

    // BFS queue: positions to check
    std::vector<std::tuple<int, int, int>> queue;
    queue.emplace_back(startX, startY, startZ);

    // Track visited positions to avoid infinite loops
    std::unordered_set<int64_t> visited;
    auto posKey = [](int x, int y, int z) -> int64_t {
        return (static_cast<int64_t>(x) & 0xFFFFF) |
               ((static_cast<int64_t>(y) & 0xFF) << 20) |
               ((static_cast<int64_t>(z) & 0xFFFFF) << 28);
    };

    // Direction offsets for 6 neighbors
    static const int dx[] = {-1, 1, 0, 0, 0, 0};
    static const int dy[] = {0, 0, -1, 1, 0, 0};
    static const int dz[] = {0, 0, 0, 0, -1, 1};

    size_t head = 0;
    while (head < queue.size()) {
        auto [x, y, z] = queue[head++];

        int64_t key = posKey(x, y, z);
        if (visited.count(key)) continue;
        visited.insert(key);

        if (!level->isInBounds(x, y, z)) continue;

        // Get current light value
        int currentLight = getBrightness(layer, x, y, z);

        // Calculate what the light should be
        int newLight = calculateLightAt(layer, x, y, z);

        // Update if changed
        bool lightChanged = (currentLight != newLight);
        if (lightChanged) {
            setBrightness(layer, x, y, z, newLight);
        }

        // ALWAYS add neighbors if:
        // 1. This is a light source (has light to spread), OR
        // 2. Light changed at this position
        // This ensures light "flows around" obstacles
        bool isSource = (newLight > 0);
        if (lightChanged || isSource) {
            for (int i = 0; i < 6; i++) {
                int nx = x + dx[i];
                int ny = y + dy[i];
                int nz = z + dz[i];

                if (level->isInBounds(nx, ny, nz)) {
                    int64_t nkey = posKey(nx, ny, nz);
                    if (!visited.count(nkey)) {
                        // Only add if neighbor might need updating
                        int neighborLight = getBrightness(layer, nx, ny, nz);
                        int expectedFromHere = newLight - 1;
                        if (expectedFromHere < 0) expectedFromHere = 0;

                        // Add if neighbor could receive more light from us,
                        // or if neighbor has more light than expected (might need reduction)
                        if (neighborLight < expectedFromHere || lightChanged) {
                            queue.emplace_back(nx, ny, nz);
                        }
                    }
                }
            }
        }

        // Safety limit to prevent infinite propagation
        if (visited.size() > 50000) break;
    }
}

void LightingEngine::processUpdates(int maxUpdates) {
    if (!level) return;

    recurseCount = 0;

    int count = 0;
    while (count < maxUpdates) {
        LightUpdate update;
        {
            std::lock_guard<std::mutex> lock(queueMutex);
            if (updateQueue.empty()) break;
            update = updateQueue.front();
            updateQueue.erase(updateQueue.begin());
        }

        if (recurseCount < MAX_RECURSE) {
            processUpdate(update);
        }
        count++;
    }
}

void LightingEngine::processUpdate(const LightUpdate& update) {
    // Matching Java LightUpdate.update() exactly
    propagateLightInRegion(update.layer, update.x0, update.y0, update.z0,
                           update.x1, update.y1, update.z1);
}

void LightingEngine::propagateLightInRegion(LightLayer layer, int x0, int y0, int z0,
                                             int x1, int y1, int z1) {
    if (!level) return;

    // Calculate volume
    int width = x1 - x0 + 1;
    int height = y1 - y0 + 1;
    int depth = z1 - z0 + 1;
    int volume = width * height * depth;

    // Safety check (matching Java)
    if (volume > 32768) {
        return;
    }

    // Iterate in X, Z, Y order (matching Java)
    for (int x = x0; x <= x1; x++) {
        for (int z = z0; z <= z1; z++) {
            // Check if chunk exists at this position
            if (!level->isInBounds(x, 0, z)) continue;

            for (int y = y0; y <= y1; y++) {
                if (!level->isInBounds(x, y, z)) continue;

                // Get current brightness
                int currentLight = getBrightness(layer, x, y, z);

                // Calculate new light value
                int newLight = calculateLightAt(layer, x, y, z);

                // If light changed, update and propagate to neighbors
                if (currentLight != newLight) {
                    setBrightness(layer, x, y, z, newLight);

                    // Calculate threshold for neighbor updates
                    int threshold = newLight - 1;
                    if (threshold < 0) threshold = 0;

                    // Queue updates for neighbors (matching Java's pattern)
                    // Always update west, bottom, north neighbors
                    updateLightIfOtherThan(layer, x - 1, y, z, threshold);
                    updateLightIfOtherThan(layer, x, y - 1, z, threshold);
                    updateLightIfOtherThan(layer, x, y, z - 1, threshold);

                    // Only update east, top, south if at region edge
                    if (x + 1 >= x1) {
                        updateLightIfOtherThan(layer, x + 1, y, z, threshold);
                    }
                    if (y + 1 >= y1) {
                        updateLightIfOtherThan(layer, x, y + 1, z, threshold);
                    }
                    if (z + 1 >= z1) {
                        updateLightIfOtherThan(layer, x, y, z + 1, threshold);
                    }
                }
            }
        }
    }
}

int LightingEngine::calculateLightAt(LightLayer layer, int x, int y, int z) {
    if (!level || !level->isInBounds(x, y, z)) return 0;

    int tileId = level->getTile(x, y, z);

    // Get light source value
    int source = getLightSource(layer, x, y, z);

    // Get light blocking value (minimum 1, matching Java)
    int blockValue = (tileId > 0 && tileId < 256) ? Tile::lightBlock[tileId] : 0;
    if (blockValue == 0) {
        blockValue = 1;
    }

    // If fully opaque (blockValue >= 15) and no emission, light is 0
    if (blockValue >= 15 && source == 0) {
        return 0;
    }

    // Get max brightness from all 6 neighbors
    int maxNeighbor = 0;
    maxNeighbor = std::max(maxNeighbor, getBrightness(layer, x - 1, y, z));
    maxNeighbor = std::max(maxNeighbor, getBrightness(layer, x + 1, y, z));
    maxNeighbor = std::max(maxNeighbor, getBrightness(layer, x, y - 1, z));
    maxNeighbor = std::max(maxNeighbor, getBrightness(layer, x, y + 1, z));
    maxNeighbor = std::max(maxNeighbor, getBrightness(layer, x, y, z - 1));
    maxNeighbor = std::max(maxNeighbor, getBrightness(layer, x, y, z + 1));

    // Apply attenuation: newLight = maxNeighbor - blockValue
    int newLight = maxNeighbor - blockValue;
    if (newLight < 0) {
        newLight = 0;
    }

    // Light emission takes priority
    if (source > newLight) {
        newLight = source;
    }

    return newLight;
}

int LightingEngine::getBrightness(LightLayer layer, int x, int y, int z) {
    if (!level) return 0;

    // Out of bounds: return surrounding brightness
    if (!level->isInBounds(x, y, z)) {
        return (layer == LightLayer::SKY) ? 15 : 0;
    }

    if (layer == LightLayer::SKY) {
        return level->getSkyLight(x, y, z);
    } else {
        return level->getBlockLight(x, y, z);
    }
}

void LightingEngine::setBrightness(LightLayer layer, int x, int y, int z, int value) {
    if (!level || !level->isInBounds(x, y, z)) return;

    value = std::max(0, std::min(15, value));
    int index = level->getIndex(x, y, z);

    // Check if value actually changed
    uint8_t oldValue;
    if (layer == LightLayer::SKY) {
        oldValue = level->skyLight[index];
        level->skyLight[index] = static_cast<uint8_t>(value);
    } else {
        oldValue = level->blockLight[index];
        level->blockLight[index] = static_cast<uint8_t>(value);
    }

    // Notify listeners if light changed (so chunks rebuild)
    if (oldValue != value) {
        level->notifyLightChanged(x, y, z);
    }
}

int LightingEngine::getLightSource(LightLayer layer, int x, int y, int z) {
    if (!level) return 0;

    if (layer == LightLayer::SKY) {
        // Sky light source: 15 if above heightmap (sky-lit)
        return isSkyLit(x, y, z) ? 15 : 0;
    } else {
        // Block light source: tile's emission value
        int tileId = level->getTile(x, y, z);
        if (tileId > 0 && tileId < 256) {
            return Tile::lightEmission[tileId];
        }
        return 0;
    }
}

bool LightingEngine::isSkyLit(int x, int y, int z) {
    if (!level) return false;

    if (y < 0) return false;
    if (y >= level->height) return true;

    // Check heightmap
    int height = level->getHeightAt(x, z);
    return y >= height;
}

void LightingEngine::updateLightIfOtherThan(LightLayer layer, int x, int y, int z, int expectedValue) {
    // Matching Java Level.updateLightIfOtherThan() exactly
    if (!level || !level->isInBounds(x, y, z)) return;

    // Recalculate minimum required value based on layer type
    if (layer == LightLayer::SKY) {
        // If sky-lit, minimum is 15
        if (isSkyLit(x, y, z)) {
            expectedValue = 15;
        }
    } else if (layer == LightLayer::BLOCK) {
        // If block emits light, use emission as minimum
        int tileId = level->getTile(x, y, z);
        if (tileId > 0 && tileId < 256) {
            int emission = Tile::lightEmission[tileId];
            if (emission > expectedValue) {
                expectedValue = emission;
            }
        }
    }

    // Only queue update if current value differs
    int currentValue = getBrightness(layer, x, y, z);
    if (currentValue != expectedValue) {
        queueUpdate(layer, x, y, z, x, y, z);
    }
}

void LightingEngine::initializeLighting() {
    if (!level) return;

    // Step 1: Calculate heightmap and initialize sky light columns (vertical propagation)
    for (int x = 0; x < level->width; x++) {
        for (int z = 0; z < level->depth; z++) {
            calculateSkyLightColumn(x, z);
        }
    }

    // Step 2: Propagate sky light horizontally (for overhangs, caves with openings, etc.)
    // Process in decreasing light levels for correct propagation
    for (int lightLevel = 14; lightLevel >= 1; lightLevel--) {
        for (int x = 0; x < level->width; x++) {
            for (int z = 0; z < level->depth; z++) {
                for (int y = 0; y < level->height; y++) {
                    int currentLight = level->getSkyLight(x, y, z);
                    if (currentLight == lightLevel) {
                        // Propagate to neighbors
                        propagateSkyLightTo(x - 1, y, z, lightLevel);
                        propagateSkyLightTo(x + 1, y, z, lightLevel);
                        propagateSkyLightTo(x, y - 1, z, lightLevel);
                        propagateSkyLightTo(x, y + 1, z, lightLevel);
                        propagateSkyLightTo(x, y, z - 1, lightLevel);
                        propagateSkyLightTo(x, y, z + 1, lightLevel);
                    }
                }
            }
        }
    }

    // Step 3: Initialize block light from emitters (matching Java lightLava)
    initializeBlockLight();

    // Clear any queued updates from initialization
    {
        std::lock_guard<std::mutex> lock(queueMutex);
        updateQueue.clear();
    }
}

void LightingEngine::calculateSkyLightColumn(int x, int z) {
    if (!level) return;

    // Find heightmap value (highest non-transparent block)
    int heightmapValue = 0;
    for (int y = level->height - 1; y > 0; y--) {
        int tileId = level->getTile(x, y, z);
        int blockValue = (tileId > 0 && tileId < 256) ? Tile::lightBlock[tileId] : 0;
        if (blockValue != 0) {
            heightmapValue = y + 1;
            break;
        }
    }

    // Update heightmap
    if (x >= 0 && x < level->width && z >= 0 && z < level->depth) {
        level->heightMap[z * level->width + x] = heightmapValue;
    }

    // Propagate sky light from top down (matching Java)
    int light = 15;
    for (int y = level->height - 1; y >= 0; y--) {
        int tileId = level->getTile(x, y, z);
        int blockValue = (tileId > 0 && tileId < 256) ? Tile::lightBlock[tileId] : 0;

        // Apply attenuation
        light -= blockValue;
        if (light < 0) light = 0;

        // Set sky light
        setBrightness(LightLayer::SKY, x, y, z, light);

        // Stop if light is exhausted
        if (light <= 0) break;
    }
}

void LightingEngine::initializeBlockLight() {
    if (!level) return;

    // First pass: Set initial emission values at light sources
    for (int x = 0; x < level->width; x++) {
        for (int z = 0; z < level->depth; z++) {
            for (int y = 0; y < level->height; y++) {
                int tileId = level->getTile(x, y, z);
                if (tileId > 0 && tileId < 256) {
                    int emission = Tile::lightEmission[tileId];
                    if (emission > 0) {
                        setBrightness(LightLayer::BLOCK, x, y, z, emission);
                    }
                }
            }
        }
    }

    // Second pass: Propagate light from sources (matching Java lightLava pattern)
    // Process in decreasing light levels for correct propagation
    for (int lightLevel = 14; lightLevel >= 1; lightLevel--) {
        for (int x = 0; x < level->width; x++) {
            for (int z = 0; z < level->depth; z++) {
                for (int y = 0; y < level->height; y++) {
                    int currentLight = level->getBlockLight(x, y, z);
                    if (currentLight == lightLevel) {
                        // Propagate to neighbors
                        propagateBlockLightTo(x - 1, y, z, lightLevel);
                        propagateBlockLightTo(x + 1, y, z, lightLevel);
                        propagateBlockLightTo(x, y - 1, z, lightLevel);
                        propagateBlockLightTo(x, y + 1, z, lightLevel);
                        propagateBlockLightTo(x, y, z - 1, lightLevel);
                        propagateBlockLightTo(x, y, z + 1, lightLevel);
                    }
                }
            }
        }
    }
}

void LightingEngine::propagateBlockLight(int x, int y, int z) {
    if (!level) return;

    int tileId = level->getTile(x, y, z);
    if (tileId <= 0 || tileId >= 256) return;

    int emission = Tile::lightEmission[tileId];
    if (emission <= 0) return;

    setBrightness(LightLayer::BLOCK, x, y, z, emission);
    queueUpdate(LightLayer::BLOCK, x - emission, y - emission, z - emission,
                x + emission, y + emission, z + emission);
}

void LightingEngine::propagateBlockLightTo(int x, int y, int z, int lightLevel) {
    if (!level || lightLevel <= 0) return;
    if (!level->isInBounds(x, y, z)) return;

    int tileId = level->getTile(x, y, z);

    // Get light blocking value (minimum 1)
    int blockValue = (tileId > 0 && tileId < 256) ? Tile::lightBlock[tileId] : 0;
    if (blockValue == 0) blockValue = 1;

    // Fully opaque blocks stop propagation
    if (blockValue >= 15) return;

    // Calculate new light value
    int newLight = lightLevel - blockValue;
    if (newLight <= 0) return;

    // Get block's own emission
    int emission = (tileId > 0 && tileId < 256) ? Tile::lightEmission[tileId] : 0;
    if (emission > newLight) {
        newLight = emission;
    }

    // Only update if this would increase light
    int currentLight = level->getBlockLight(x, y, z);
    if (newLight > currentLight) {
        setBrightness(LightLayer::BLOCK, x, y, z, newLight);
    }
}

void LightingEngine::propagateSkyLightTo(int x, int y, int z, int lightLevel) {
    if (!level || lightLevel <= 0) return;
    if (!level->isInBounds(x, y, z)) return;

    int tileId = level->getTile(x, y, z);

    // Get light blocking value (minimum 1)
    int blockValue = (tileId > 0 && tileId < 256) ? Tile::lightBlock[tileId] : 0;
    if (blockValue == 0) blockValue = 1;

    // Fully opaque blocks stop propagation
    if (blockValue >= 15) return;

    // Calculate new light value
    int newLight = lightLevel - blockValue;
    if (newLight <= 0) return;

    // Sky-lit blocks always have light 15
    if (isSkyLit(x, y, z)) {
        newLight = 15;
    }

    // Only update if this would increase light
    int currentLight = level->getSkyLight(x, y, z);
    if (newLight > currentLight) {
        setBrightness(LightLayer::SKY, x, y, z, newLight);
    }
}

// BFS version kept for reference but not used
void LightingEngine::propagateLightBFS(LightLayer layer, int x0, int y0, int z0,
                                        int x1, int y1, int z1) {
    // Delegate to the iterative version which matches Java
    propagateLightInRegion(layer, x0, y0, z0, x1, y1, z1);
}

} // namespace mc
