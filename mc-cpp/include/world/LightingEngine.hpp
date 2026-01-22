#pragma once

#include <vector>
#include <deque>
#include <queue>
#include <mutex>
#include <atomic>
#include <thread>
#include <condition_variable>
#include <functional>

namespace mc {

class Level;

// Light layer type (matching Java LightLayer)
enum class LightLayer {
    SKY,    // Natural sunlight (surrounding = 15)
    BLOCK   // Artificial block light (surrounding = 0)
};

// A rectangular region needing light update (matching Java LightUpdate)
struct LightUpdate {
    LightLayer layer;
    int x0, y0, z0;
    int x1, y1, z1;

    LightUpdate() : layer(LightLayer::SKY), x0(0), y0(0), z0(0), x1(0), y1(0), z1(0) {}
    LightUpdate(LightLayer layer, int x0, int y0, int z0, int x1, int y1, int z1)
        : layer(layer), x0(x0), y0(y0), z0(z0), x1(x1), y1(y1), z1(z1) {}

    // Try to expand this update to contain another region
    bool expandToContain(int nx0, int ny0, int nz0, int nx1, int ny1, int nz1);

    // Get volume of this update region
    int getVolume() const { return (x1 - x0 + 1) * (y1 - y0 + 1) * (z1 - z0 + 1); }
};

// Multithreaded lighting engine
class LightingEngine {
public:
    LightingEngine();
    ~LightingEngine();

    // Set the level to work with
    void setLevel(Level* level);

    // Queue a light update for a region
    void queueUpdate(LightLayer layer, int x0, int y0, int z0, int x1, int y1, int z1);

    // Queue light update at a single block position
    void queueUpdateAt(int x, int y, int z);

    // Process pending light updates (call from main thread, processes up to maxUpdates)
    void processUpdates(int maxUpdates = 500);

    // Initialize lighting for the entire level (called after world generation)
    void initializeLighting();

    // Calculate sky light for a column (top-down propagation)
    void calculateSkyLightColumn(int x, int z);

    // Initialize block light for entire level (matching Java lightLava)
    void initializeBlockLight();

    // Calculate block light emanating from a source
    void propagateBlockLight(int x, int y, int z);

    // Enable/disable multithreading
    void setMultithreaded(bool enabled);
    bool isMultithreaded() const { return multithreaded; }

    // Get number of pending updates
    size_t getPendingUpdateCount() const;

private:
    Level* level;

    // Light update queue
    std::deque<LightUpdate> updateQueue;
    mutable std::mutex queueMutex;

    // Multithreading
    bool multithreaded;
    std::vector<std::thread> workerThreads;
    std::atomic<bool> running;
    std::condition_variable workAvailable;
    std::mutex workMutex;

    // Worker thread function
    void workerFunction();

    // Process a single light update
    void processUpdate(const LightUpdate& update);

    // Propagate light in a region (core algorithm matching Java LightUpdate.update)
    void propagateLightInRegion(LightLayer layer, int x0, int y0, int z0, int x1, int y1, int z1);

    // BFS flood-fill light propagation (proper algorithm)
    void propagateLightBFS(LightLayer layer, int x0, int y0, int z0, int x1, int y1, int z1);

    // Calculate what light value should be at a position
    int calculateLightAt(LightLayer layer, int x, int y, int z);

    // Get brightness at position for a specific layer
    int getBrightness(LightLayer layer, int x, int y, int z);

    // Set brightness at position for a specific layer
    void setBrightness(LightLayer layer, int x, int y, int z, int value);

    // Get light source value at position (emission for block, sky exposure for sky)
    int getLightSource(LightLayer layer, int x, int y, int z);

    // Check if position is sky-lit (above heightmap)
    bool isSkyLit(int x, int y, int z);

    // Update light if different from expected value
    void updateLightIfOtherThan(LightLayer layer, int x, int y, int z, int expectedValue);

    // Propagate block light to a neighbor (for initialization flood fill)
    void propagateBlockLightTo(int x, int y, int z, int lightLevel);

    // Propagate sky light to a neighbor (for horizontal propagation under overhangs)
    void propagateSkyLightTo(int x, int y, int z, int lightLevel);

    // Immediate BFS propagation from a position (for instant light updates)
    void propagateLightImmediateBFS(LightLayer layer, int startX, int startY, int startZ);

    // Remove light when a source is removed (darkness propagation)
    void removeLightBFS(LightLayer layer, int startX, int startY, int startZ, int oldLightValue);

    // Recursive limit for light propagation
    static constexpr int MAX_RECURSE = 50;
    int recurseCount;
};

} // namespace mc
