#include "world/Level.hpp"
#include "world/tile/Tile.hpp"
#include "entity/Entity.hpp"
#include "entity/Player.hpp"
#include "pathfinder/PathFinder.hpp"
#include "util/Mth.hpp"
#include <algorithm>
#include <cmath>

namespace mc {

Level::Level(int width, int height, int depth, long long seed)
    : xChunks(width / CHUNK_SIZE)
    , yChunks(height / CHUNK_SIZE)
    , zChunks(depth / CHUNK_SIZE)
    , width(width)
    , height(height)
    , depth(depth)
    , seed(seed)
    , worldTime(0)
    , spawnX(width / 2)
    , spawnY(height / 2 + 16)
    , spawnZ(depth / 2)
    , raining(false)
    , thundering(false)
    , rainLevel(0.0f)
{
    size_t totalBlocks = static_cast<size_t>(width) * height * depth;
    blocks.resize(totalBlocks, 0);
    data.resize(totalBlocks, 0);
    skyLight.resize(totalBlocks, 0);   // Initialize to 0, will be calculated properly
    blockLight.resize(totalBlocks, 0);
    heightMap.resize(static_cast<size_t>(width) * depth, 0);

    // Initialize lighting engine
    lightingEngine = std::make_unique<LightingEngine>();
    lightingEngine->setLevel(this);

    // Initialize async pathfinder (2 worker threads)
    asyncPathFinder = std::make_unique<AsyncPathFinder>(2);

    Mth::setSeed(static_cast<uint64_t>(seed));
}

Level::~Level() {
    entities.clear();
    listeners.clear();  // Clear listener references to prevent use-after-free
}

int Level::getIndex(int x, int y, int z) const {
    return (y * depth + z) * width + x;
}

bool Level::isInBounds(int x, int y, int z) const {
    return x >= 0 && x < width &&
           y >= 0 && y < height &&
           z >= 0 && z < depth;
}

int Level::getTile(int x, int y, int z) const {
    if (!isInBounds(x, y, z)) return 0;
    return blocks[getIndex(x, y, z)];
}

bool Level::setTile(int x, int y, int z, int tileId) {
    if (!isInBounds(x, y, z)) return false;

    int index = getIndex(x, y, z);
    int oldTile = blocks[index];

    if (oldTile == tileId) return false;

    blocks[index] = static_cast<uint8_t>(tileId);

    // Smart heightmap update: O(1) for most cases, O(height) only when removing highest block
    int oldHeight = heightMap[z * width + x];
    bool oldBlocksLight = (oldTile > 0 && oldTile < 256) ? Tile::lightBlock[oldTile] != 0 : false;
    bool newBlocksLight = (tileId > 0 && tileId < 256) ? Tile::lightBlock[tileId] != 0 : false;

    if (newBlocksLight && y + 1 > oldHeight) {
        // Placed a light-blocking block above current height - O(1) update
        heightMap[z * width + x] = y + 1;
    } else if (oldBlocksLight && !newBlocksLight && y + 1 == oldHeight) {
        // Removed the highest light-blocking block - need full scan
        updateHeightMap(x, z);
    }
    // Otherwise, height map doesn't change (block is below highest or non-light-blocking)

    // Update lighting BEFORE notifying listeners
    // This ensures chunks rebuild with correct light values, preventing flash-to-black
    updateLightAt(x, y, z);

    // Now notify listeners (chunk will rebuild with updated light)
    notifyBlockChanged(x, y, z);

    // Notify neighboring blocks of the change (matching Java Level.updateNeighborsAt)
    notifyNeighborsAt(x, y, z, tileId);

    return true;
}

bool Level::setTileWithData(int x, int y, int z, int tileId, int metadata) {
    if (!isInBounds(x, y, z)) return false;

    int index = getIndex(x, y, z);
    int oldTile = blocks[index];
    blocks[index] = static_cast<uint8_t>(tileId);
    data[index] = static_cast<uint8_t>(metadata);

    // Smart heightmap update: O(1) for most cases, O(height) only when removing highest block
    int oldHeight = heightMap[z * width + x];
    bool oldBlocksLight = (oldTile > 0 && oldTile < 256) ? Tile::lightBlock[oldTile] != 0 : false;
    bool newBlocksLight = (tileId > 0 && tileId < 256) ? Tile::lightBlock[tileId] != 0 : false;

    if (newBlocksLight && y + 1 > oldHeight) {
        // Placed a light-blocking block above current height - O(1) update
        heightMap[z * width + x] = y + 1;
    } else if (oldBlocksLight && !newBlocksLight && y + 1 == oldHeight) {
        // Removed the highest light-blocking block - need full scan
        updateHeightMap(x, z);
    }

    updateLightAt(x, y, z);  // Update light before notifying
    notifyBlockChanged(x, y, z);

    return true;
}

int Level::getData(int x, int y, int z) const {
    if (!isInBounds(x, y, z)) return 0;
    return data[getIndex(x, y, z)];
}

bool Level::setData(int x, int y, int z, int metadata) {
    if (!isInBounds(x, y, z)) return false;
    data[getIndex(x, y, z)] = static_cast<uint8_t>(metadata);
    notifyBlockChanged(x, y, z);
    return true;
}

bool Level::isAir(int x, int y, int z) const {
    return getTile(x, y, z) == 0;
}

bool Level::isSolid(int x, int y, int z) const {
    Tile* tile = getTileAt(x, y, z);
    return tile && tile->solid;
}

bool Level::isLiquid(int x, int y, int z) const {
    int id = getTile(x, y, z);
    return id == Tile::WATER || id == Tile::STILL_WATER ||
           id == Tile::LAVA || id == Tile::STILL_LAVA;
}

Tile* Level::getTileAt(int x, int y, int z) const {
    int id = getTile(x, y, z);
    return (id >= 0 && id < 256) ? Tile::tiles[id].get() : nullptr;
}

std::vector<AABB> Level::getCollisionBoxes(Entity* /*entity*/, const AABB& area) const {
    // Check collision cache first
    constexpr double EPSILON = 0.0001;
    for (const auto& entry : collisionCache) {
        if (entry.frameId == collisionCacheFrameId &&
            std::abs(entry.queryArea.x0 - area.x0) < EPSILON &&
            std::abs(entry.queryArea.y0 - area.y0) < EPSILON &&
            std::abs(entry.queryArea.z0 - area.z0) < EPSILON &&
            std::abs(entry.queryArea.x1 - area.x1) < EPSILON &&
            std::abs(entry.queryArea.y1 - area.y1) < EPSILON &&
            std::abs(entry.queryArea.z1 - area.z1) < EPSILON) {
            return entry.boxes;
        }
    }

    // Cache miss - compute collision boxes
    std::vector<AABB> boxes;

    int x0 = static_cast<int>(std::floor(area.x0));
    int y0 = static_cast<int>(std::floor(area.y0));
    int z0 = static_cast<int>(std::floor(area.z0));
    int x1 = static_cast<int>(std::floor(area.x1)) + 1;
    int y1 = static_cast<int>(std::floor(area.y1)) + 1;
    int z1 = static_cast<int>(std::floor(area.z1)) + 1;

    for (int x = x0; x < x1; x++) {
        for (int y = y0; y < y1; y++) {
            for (int z = z0; z < z1; z++) {
                Tile* tile = getTileAt(x, y, z);
                if (tile && tile->canCollide()) {
                    AABB box = tile->getCollisionBox(x, y, z);
                    if (box.x1 > box.x0 && area.intersects(box)) {
                        boxes.push_back(box);
                    }
                }
            }
        }
    }

    // Store in cache (LRU: replace oldest entry if full)
    if (collisionCache.size() >= COLLISION_CACHE_SIZE) {
        // Find oldest entry (lowest frameId) or first entry from current frame
        size_t replaceIdx = 0;
        int oldestFrame = collisionCache[0].frameId;
        for (size_t i = 1; i < collisionCache.size(); ++i) {
            if (collisionCache[i].frameId < oldestFrame) {
                oldestFrame = collisionCache[i].frameId;
                replaceIdx = i;
            }
        }
        collisionCache[replaceIdx] = {area, boxes, collisionCacheFrameId};
    } else {
        collisionCache.push_back({area, boxes, collisionCacheFrameId});
    }

    return boxes;
}

HitResult Level::clip(const Vec3& start, const Vec3& end, bool stopOnLiquid) const {
    // 3D DDA (Digital Differential Analyzer) raycasting
    // Based on Java Minecraft's clip implementation

    double x = start.x;
    double y = start.y;
    double z = start.z;

    double dx = end.x - start.x;
    double dy = end.y - start.y;
    double dz = end.z - start.z;

    double length = std::sqrt(dx * dx + dy * dy + dz * dz);
    if (length < 0.0001) return HitResult();

    // Normalize direction
    dx /= length;
    dy /= length;
    dz /= length;

    // Current block position
    int blockX = static_cast<int>(std::floor(x));
    int blockY = static_cast<int>(std::floor(y));
    int blockZ = static_cast<int>(std::floor(z));

    // Step direction (-1 or +1)
    int stepX = (dx > 0) ? 1 : -1;
    int stepY = (dy > 0) ? 1 : -1;
    int stepZ = (dz > 0) ? 1 : -1;

    // Distance to next block boundary
    double tMaxX = dx != 0 ? ((dx > 0 ? (blockX + 1 - x) : (x - blockX)) / std::abs(dx)) : 1e30;
    double tMaxY = dy != 0 ? ((dy > 0 ? (blockY + 1 - y) : (y - blockY)) / std::abs(dy)) : 1e30;
    double tMaxZ = dz != 0 ? ((dz > 0 ? (blockZ + 1 - z) : (z - blockZ)) / std::abs(dz)) : 1e30;

    // Distance to traverse one block
    double tDeltaX = dx != 0 ? std::abs(1.0 / dx) : 1e30;
    double tDeltaY = dy != 0 ? std::abs(1.0 / dy) : 1e30;
    double tDeltaZ = dz != 0 ? std::abs(1.0 / dz) : 1e30;

    Direction face = Direction::UP;
    double traveled = 0;

    // Max distance to check
    double maxDist = length;

    for (int i = 0; i < 200 && traveled < maxDist; i++) {
        // Check current block
        Tile* tile = getTileAt(blockX, blockY, blockZ);
        if (tile) {
            // For solid blocks, always hit
            if (tile->solid || (stopOnLiquid && tile->renderShape == TileShape::LIQUID)) {
                Vec3 hitPos(
                    start.x + dx * traveled,
                    start.y + dy * traveled,
                    start.z + dz * traveled
                );
                return HitResult(blockX, blockY, blockZ, face, hitPos);
            }

            // For non-solid blocks with selection boxes (like torches, flowers),
            // check if the ray intersects the selection box
            AABB selBox = tile->getSelectionBox(const_cast<Level*>(this), blockX, blockY, blockZ);

            // Check if selection box is valid (non-empty)
            if (selBox.x1 > selBox.x0 && selBox.y1 > selBox.y0 && selBox.z1 > selBox.z0) {
                // Check ray intersection with the selection box
                auto clipResult = selBox.clip(start, end);
                if (clipResult.has_value()) {
                    // Determine which face was hit based on the hit position
                    Vec3 hitPos = clipResult.value();
                    double epsilon = 0.001;

                    Direction hitFace = Direction::UP;
                    if (std::abs(hitPos.y - selBox.y1) < epsilon) hitFace = Direction::UP;
                    else if (std::abs(hitPos.y - selBox.y0) < epsilon) hitFace = Direction::DOWN;
                    else if (std::abs(hitPos.z - selBox.z0) < epsilon) hitFace = Direction::NORTH;
                    else if (std::abs(hitPos.z - selBox.z1) < epsilon) hitFace = Direction::SOUTH;
                    else if (std::abs(hitPos.x - selBox.x0) < epsilon) hitFace = Direction::WEST;
                    else if (std::abs(hitPos.x - selBox.x1) < epsilon) hitFace = Direction::EAST;

                    return HitResult(blockX, blockY, blockZ, hitFace, hitPos);
                }
            }
        }

        // Step to next block
        if (tMaxX < tMaxY) {
            if (tMaxX < tMaxZ) {
                traveled = tMaxX;
                tMaxX += tDeltaX;
                blockX += stepX;
                face = (stepX > 0) ? Direction::WEST : Direction::EAST;
            } else {
                traveled = tMaxZ;
                tMaxZ += tDeltaZ;
                blockZ += stepZ;
                face = (stepZ > 0) ? Direction::NORTH : Direction::SOUTH;
            }
        } else {
            if (tMaxY < tMaxZ) {
                traveled = tMaxY;
                tMaxY += tDeltaY;
                blockY += stepY;
                face = (stepY > 0) ? Direction::DOWN : Direction::UP;
            } else {
                traveled = tMaxZ;
                tMaxZ += tDeltaZ;
                blockZ += stepZ;
                face = (stepZ > 0) ? Direction::NORTH : Direction::SOUTH;
            }
        }
    }

    return HitResult();  // No hit
}

int Level::getSkyLight(int x, int y, int z) const {
    if (!isInBounds(x, y, z)) return 15;
    return skyLight[getIndex(x, y, z)];
}

int Level::getBlockLight(int x, int y, int z) const {
    if (!isInBounds(x, y, z)) return 0;
    return blockLight[getIndex(x, y, z)];
}

int Level::getSkyDarken() const {
    // Match Java Level.getSkyDarken() exactly
    float timeOfDay = getTimeOfDay();

    // Java: float var3 = 1.0F - (Mth.cos(var2 * (float)Math.PI * 2.0F) * 2.0F + 0.5F)
    float celestialAngle = 1.0f - (std::cos(timeOfDay * Mth::PI * 2.0f) * 2.0f + 0.5f);

    // Clamp to 0-1
    if (celestialAngle < 0.0f) celestialAngle = 0.0f;
    if (celestialAngle > 1.0f) celestialAngle = 1.0f;

    // Java: return (int)(var3 * 11.0F)
    return static_cast<int>(celestialAngle * 11.0f);
}

// brightnessRamp from Java Dimension.updateLightRamp()
// Formula: (1.0 - var3) / (var3 * 3.0 + 1.0) * (1.0 - 0.05) + 0.05
// where var3 = 1.0 - i/15.0
static const float brightnessRamp[16] = {
    0.05f,     // 0
    0.0672f,   // 1
    0.0859f,   // 2
    0.1072f,   // 3
    0.1323f,   // 4
    0.1622f,   // 5
    0.1988f,   // 6
    0.2441f,   // 7
    0.3014f,   // 8
    0.3753f,   // 9
    0.4729f,   // 10
    0.6063f,   // 11
    0.7969f,   // 12
    0.9500f,   // 13 (actually converges toward 1.0)
    0.9500f,   // 14
    1.0f       // 15
};

float Level::getSkyBrightness() const {
    // Calculate sky brightness factor (0-1) based on time of day
    // This is the inverse of skyDarken - how bright the sky is
    int skyDarken = getSkyDarken();
    // skyDarken ranges from 0 (full day) to 11 (darkest night)
    // Map this to a brightness factor: 0 -> 1.0, 11 -> ~0.2
    return brightnessRamp[15 - skyDarken];
}

float Level::getBrightness(int x, int y, int z) const {
    // Match Java Level.getBrightness() exactly:
    // return this.dimension.brightnessRamp[this.getRawBrightness(x, y, z)]

    int sky = getSkyLight(x, y, z);
    int block = getBlockLight(x, y, z);

    // Apply sky darkening based on time of day (Java: getRawBrightness subtracts skyDarken)
    int skyDarken = getSkyDarken();
    int adjustedSky = sky - skyDarken;
    if (adjustedSky < 0) adjustedSky = 0;

    // Use max of adjusted sky light and block light
    int light = std::max(adjustedSky, block);

    return brightnessRamp[light];
}

float Level::getBrightnessForChunk(int x, int y, int z) const {
    // Same as getBrightness but always assumes full daylight (skyDarken = 0)
    // Used for chunk building so lighting can be dynamically adjusted by shader

    int sky = getSkyLight(x, y, z);
    int block = getBlockLight(x, y, z);

    // Use max of sky light and block light (no sky darken adjustment)
    int light = std::max(sky, block);

    return brightnessRamp[light];
}

void Level::updateLightAt(int x, int y, int z) {
    // Queue light update at this position for both sky and block light
    if (lightingEngine) {
        lightingEngine->queueUpdateAt(x, y, z);
    }
}

void Level::updateLights() {
    // Process pending light updates
    if (lightingEngine) {
        lightingEngine->processUpdates(500);  // Process up to 500 updates per tick
    }
}

int Level::getHeightAt(int x, int z) const {
    if (x < 0 || x >= width || z < 0 || z >= depth) return 0;
    return heightMap[z * width + x];
}

void Level::updateHeightMap(int x, int z) {
    // Matching Java LevelChunk.recalcHeight() logic
    if (x < 0 || x >= width || z < 0 || z >= depth) return;

    // Find new heightmap value: highest block with non-zero lightBlock
    int newHeight = 0;
    for (int y = height - 1; y > 0; y--) {
        int tileId = getTile(x, y, z);
        int blockValue = (tileId > 0 && tileId < 256) ? Tile::lightBlock[tileId] : 0;
        if (blockValue != 0) {
            newHeight = y + 1;
            break;
        }
    }

    heightMap[z * width + x] = newHeight;

    // Don't modify light values here - let the BFS propagation in updateLightAt handle it
    // This prevents the "flash to black" issue where we set light to 0 before calculating the correct value
}

void Level::addEntity(std::unique_ptr<Entity> entity) {
    entity->level = this;
    if (auto* player = dynamic_cast<Player*>(entity.get())) {
        players.push_back(player);
    }
    entitySpatialHash.insert(entity.get());
    entities.push_back(std::move(entity));
}

void Level::removeEntity(Entity* entity) {
    if (auto* player = dynamic_cast<Player*>(entity)) {
        players.erase(std::remove(players.begin(), players.end(), player), players.end());
    }

    entitySpatialHash.remove(entity);

    entities.erase(
        std::remove_if(entities.begin(), entities.end(),
            [entity](const std::unique_ptr<Entity>& e) { return e.get() == entity; }),
        entities.end()
    );
}

Entity* Level::getEntityById(int id) {
    for (auto& entity : entities) {
        if (entity->entityId == id) return entity.get();
    }
    return nullptr;
}

std::vector<Entity*> Level::getEntitiesInArea(const AABB& area) const {
    // Use spatial hash for O(cells + entities) instead of O(n) linear scan
    return entitySpatialHash.query(area);
}

bool Level::isUnobstructed(const AABB& area) const {
    // Match Java Level.isUnobstructed() exactly
    // Returns false if any entity with blocksBuilding=true is in the area
    std::vector<Entity*> entitiesInArea = getEntitiesInArea(area);

    for (Entity* entity : entitiesInArea) {
        if (!entity->removed && entity->blocksBuilding) {
            return false;
        }
    }

    return true;
}

bool Level::mayPlace(int tileId, int x, int y, int z, bool ignoreBoundingBox) const {
    // Match Java Level.mayPlace() exactly
    if (!isInBounds(x, y, z)) return false;

    int existingTile = getTile(x, y, z);
    Tile* existing = (existingTile >= 0 && existingTile < 256) ? Tile::tiles[existingTile].get() : nullptr;
    Tile* newTile = (tileId >= 0 && tileId < 256) ? Tile::tiles[tileId].get() : nullptr;

    if (!newTile) return false;

    // Get the collision box of the tile to be placed
    AABB tileBox = newTile->getCollisionBox(x, y, z);

    // If not ignoring bounding box, check for entity collisions
    if (!ignoreBoundingBox && tileBox.x1 > tileBox.x0) {
        if (!isUnobstructed(tileBox)) {
            return false;
        }
    }

    // Allow placing in water, lava, fire, or snow layer
    if (existingTile == Tile::WATER || existingTile == Tile::STILL_WATER ||
        existingTile == Tile::LAVA || existingTile == Tile::STILL_LAVA ||
        existingTile == Tile::FIRE || existingTile == Tile::SNOW_LAYER) {
        return true;
    }

    // Otherwise, require air (or no existing tile)
    return tileId > 0 && existing == nullptr;
}

void Level::tick() {
    worldTime+=50;
    ++collisionCacheFrameId;  // Invalidate old collision cache entries
    tickEntities();
    tickTiles();
    updateLights();  // Process queued light updates
}

void Level::tickEntities() {
    for (auto& entity : entities) {
        // Store old bounding box for spatial hash update
        AABB oldBB = entity->bb;
        entity->tick();
        // Update spatial hash if entity moved
        entitySpatialHash.update(entity.get(), oldBB);
    }

    // Remove dead entities (also remove from spatial hash)
    for (auto& entity : entities) {
        if (entity->removed) {
            entitySpatialHash.remove(entity.get());
        }
    }

    entities.erase(
        std::remove_if(entities.begin(), entities.end(),
            [](const std::unique_ptr<Entity>& e) { return e->removed; }),
        entities.end()
    );
}

void Level::tickTiles() {
    // Random tile ticks for grass growth, etc.
    for (int i = 0; i < 400; i++) {
        int x = Mth::random(0, width - 1);
        int y = Mth::random(0, height - 1);
        int z = Mth::random(0, depth - 1);

        int tileId = getTile(x, y, z);
        if (tileId > 0 && Tile::shouldTick[tileId]) {
            Tile* tile = Tile::tiles[tileId].get();
            if (tile) {
                tile->tick(this, x, y, z);
            }
        }
    }
}

void Level::animateTick(int centerX, int centerY, int centerZ) {
    // Called each tick to spawn visual particle effects for nearby tiles
    // Matches Java Level.animateTick exactly: 1000 iterations with triangular distribution

    for (int i = 0; i < 1000; i++) {
        // Java uses: var1 + this.random.nextInt(16) - this.random.nextInt(16)
        // This creates a triangular distribution centered at 0, range -15 to +15
        // Positions closer to the player are more likely to be selected
        int x = centerX + Mth::random(0, 15) - Mth::random(0, 15);
        int y = centerY + Mth::random(0, 15) - Mth::random(0, 15);
        int z = centerZ + Mth::random(0, 15) - Mth::random(0, 15);

        if (!isInBounds(x, y, z)) continue;

        int tileId = getTile(x, y, z);
        if (tileId > 0) {
            Tile* tile = Tile::tiles[tileId].get();
            if (tile) {
                tile->animateTick(this, x, y, z);
            }
        }
    }
}

void Level::addListener(LevelListener* listener) {
    listeners.push_back(listener);
}

void Level::removeListener(LevelListener* listener) {
    listeners.erase(std::remove(listeners.begin(), listeners.end(), listener), listeners.end());
}

void Level::notifyBlockChanged(int x, int y, int z) {
    for (auto* listener : listeners) {
        listener->tileChanged(x, y, z);
    }
}

void Level::notifyLightChanged(int x, int y, int z) {
    for (auto* listener : listeners) {
        listener->lightChanged(x, y, z);
    }
}

void Level::notifyNeighborsAt(int x, int y, int z, int tileId) {
    // Matching Java Level.updateNeighborsAt - notify all 6 adjacent blocks
    // This is called when a block changes, so neighbors can react (e.g., torch falls)
    static const int offsets[6][3] = {
        {-1, 0, 0}, {1, 0, 0},  // West, East
        {0, -1, 0}, {0, 1, 0},  // Down, Up
        {0, 0, -1}, {0, 0, 1}   // North, South
    };

    for (int i = 0; i < 6; i++) {
        int nx = x + offsets[i][0];
        int ny = y + offsets[i][1];
        int nz = z + offsets[i][2];

        if (!isInBounds(nx, ny, nz)) continue;

        Tile* neighbor = getTileAt(nx, ny, nz);
        if (neighbor) {
            neighbor->onNeighborChange(this, nx, ny, nz, tileId);
        }
    }
}

void Level::addParticle(const std::string& name, double x, double y, double z,
                        double xa, double ya, double za) {
    // Notify all listeners (LevelRenderer will handle particle creation)
    for (auto* listener : listeners) {
        listener->addParticle(name, x, y, z, xa, ya, za);
    }
}

void Level::generateFlatWorld() {
    // Simple flat world generation
    int groundLevel = 64;

    // Temporarily disable lighting updates during generation
    auto tempEngine = std::move(lightingEngine);

    for (int x = 0; x < width; x++) {
        for (int z = 0; z < depth; z++) {
            // Bedrock
            int index = getIndex(x, 0, z);
            blocks[index] = Tile::BEDROCK;

            // Stone
            for (int y = 1; y < groundLevel - 4; y++) {
                index = getIndex(x, y, z);
                blocks[index] = Tile::STONE;
            }

            // Dirt
            for (int y = groundLevel - 4; y < groundLevel; y++) {
                index = getIndex(x, y, z);
                blocks[index] = Tile::DIRT;
            }

            // Grass
            index = getIndex(x, groundLevel, z);
            blocks[index] = Tile::GRASS;

            // Update height map
            heightMap[z * width + x] = groundLevel + 1;
        }
    }

    // Restore lighting engine and initialize lighting
    lightingEngine = std::move(tempEngine);
    if (lightingEngine) {
        lightingEngine->initializeLighting();
    }

    // Update spawn point
    spawnY = groundLevel + 2;
}

void Level::generateTerrain() {
    // Would use Perlin noise for terrain generation
    // For now, just call flat world
    generateFlatWorld();
}

void Level::initializeLight() {
    // Use lighting engine for proper initialization
    if (lightingEngine) {
        lightingEngine->initializeLighting();
    }
}

void Level::propagateSkyLight(int x, int z) {
    // Legacy method - now handled by LightingEngine
    if (lightingEngine) {
        lightingEngine->calculateSkyLightColumn(x, z);
    }
}

std::unique_ptr<Path> Level::findPath(Entity* entity, Entity* target, float maxDist) {
    PathFinder finder(this);
    return finder.findPath(entity, target, maxDist);
}

std::unique_ptr<Path> Level::findPath(Entity* entity, int x, int y, int z, float maxDist) {
    PathFinder finder(this);
    return finder.findPath(entity, x, y, z, maxDist);
}

} // namespace mc
