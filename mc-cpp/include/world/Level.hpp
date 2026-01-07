#pragma once

#include "phys/AABB.hpp"
#include "phys/Vec3.hpp"
#include "phys/HitResult.hpp"
#include <vector>
#include <memory>
#include <functional>

namespace mc {

class Entity;
class Player;
class LevelChunk;
class Tile;

class LevelListener {
public:
    virtual ~LevelListener() = default;
    virtual void tileChanged(int x, int y, int z) {}
    virtual void allChanged() {}
    virtual void lightChanged(int x, int y, int z) {}
};

class Level {
public:
    // World dimensions
    static constexpr int CHUNK_SIZE = 16;
    static constexpr int MAX_HEIGHT = 128;

    // World size in chunks
    int xChunks, yChunks, zChunks;
    int width, height, depth;

    // Block data (simple flat array for now)
    std::vector<uint8_t> blocks;
    std::vector<uint8_t> data;      // Metadata
    std::vector<uint8_t> skyLight;
    std::vector<uint8_t> blockLight;
    std::vector<int> heightMap;

    // Entities
    std::vector<std::unique_ptr<Entity>> entities;
    std::vector<Player*> players;

    // World properties
    long long seed;
    long long worldTime;
    int spawnX, spawnY, spawnZ;

    // Time of day (0.0 to 1.0, where 0.0 = sunrise, 0.25 = noon, 0.5 = sunset, 0.75 = midnight)
    float getTimeOfDay() const { return static_cast<float>(worldTime % 24000) / 24000.0f; }

    // Listeners
    std::vector<LevelListener*> listeners;

    // Weather
    bool raining;
    bool thundering;
    float rainLevel;

    Level(int width, int height, int depth, long long seed = 0);
    ~Level();

    // Block access
    int getTile(int x, int y, int z) const;
    bool setTile(int x, int y, int z, int tileId);
    bool setTileWithData(int x, int y, int z, int tileId, int metadata);
    int getData(int x, int y, int z) const;
    bool setData(int x, int y, int z, int metadata);

    // Block queries
    bool isAir(int x, int y, int z) const;
    bool isSolid(int x, int y, int z) const;
    bool isLiquid(int x, int y, int z) const;
    Tile* getTileAt(int x, int y, int z) const;

    // Bounds checking
    bool isInBounds(int x, int y, int z) const;
    int getIndex(int x, int y, int z) const;

    // Collision
    std::vector<AABB> getCollisionBoxes(Entity* entity, const AABB& area) const;
    HitResult clip(const Vec3& start, const Vec3& end, bool stopOnLiquid = false) const;

    // Lighting
    int getSkyLight(int x, int y, int z) const;
    int getBlockLight(int x, int y, int z) const;
    int getSkyDarken() const;  // How much to darken sky light based on time of day (0-11)
    float getBrightness(int x, int y, int z) const;
    void updateLightAt(int x, int y, int z);
    void updateLights();

    // Height map
    int getHeightAt(int x, int z) const;
    void updateHeightMap(int x, int z);

    // Entities
    void addEntity(std::unique_ptr<Entity> entity);
    void removeEntity(Entity* entity);
    Entity* getEntityById(int id);
    std::vector<Entity*> getEntitiesInArea(const AABB& area) const;

    // Block placement checks (matching Java Level)
    // Returns true if no entities with blocksBuilding=true are in the AABB
    bool isUnobstructed(const AABB& area) const;
    // Returns true if a tile can be placed at the given position
    bool mayPlace(int tileId, int x, int y, int z, bool ignoreBoundingBox = false) const;

    // Ticking
    void tick();
    void tickTiles();
    void tickEntities();

    // Listeners
    void addListener(LevelListener* listener);
    void removeListener(LevelListener* listener);
    void notifyBlockChanged(int x, int y, int z);

    // World generation
    void generateFlatWorld();
    void generateTerrain();

private:
    void initializeLight();
    void propagateSkyLight(int x, int z);
};

} // namespace mc
