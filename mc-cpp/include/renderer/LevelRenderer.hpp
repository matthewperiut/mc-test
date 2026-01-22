#pragma once

#include "world/Level.hpp"
#include "renderer/Chunk.hpp"
#include "renderer/TileRenderer.hpp"
#include "renderer/Frustum.hpp"
#include "particle/ParticleEngine.hpp"
#include <vector>
#include <memory>
#include <array>
#include <cstdint>

namespace mc {

class Minecraft;
class Entity;
class Tile;
class ChunkMeshBuilder;

class LevelRenderer : public LevelListener {
public:
    Level* level;
    Minecraft* minecraft;

    // Chunks
    std::vector<std::unique_ptr<Chunk>> chunks;
    int xChunks, yChunks, zChunks;

    // Render lists
    std::vector<Chunk*> visibleChunks;
    std::vector<Chunk*> dirtyChunks;

    // Tile renderer
    TileRenderer tileRenderer;

    // Particle engine
    ParticleEngine particleEngine;

    // Render distance
    int renderDistance;

    // Stats
    int chunksRendered;
    int chunksUpdated;

    // First rebuild flag - rebuild more chunks initially
    bool firstRebuild;

    // Block breaking progress (0.0 - 1.0)
    float destroyProgress;
    int destroyX, destroyY, destroyZ;

    LevelRenderer(Minecraft* minecraft, Level* level);
    ~LevelRenderer();

    // Set level
    void setLevel(Level* level);

    // Rendering
    void render(float partialTick, int pass);
    void renderSky(float partialTick);
    void renderClouds(float partialTick);
    void renderFastClouds(float partialTick);     // Flat clouds for Fast graphics
    void renderAdvancedClouds(float partialTick); // 3D clouds for Fancy graphics
    void renderEntities(float partialTick);

    // Update chunks
    void updateDirtyChunks();
    void rebuildAllChunks();

    // Culling
    void updateVisibleChunks(double camX, double camY, double camZ);

    // LevelListener implementation
    void tileChanged(int x, int y, int z) override;
    void allChanged() override;
    void lightChanged(int x, int y, int z) override;
    void addParticle(const std::string& name, double x, double y, double z,
                     double xa, double ya, double za) override;

    // Particle rendering and management
    void tickParticles();
    void renderParticles(float partialTick);

    // Get chunk at position
    Chunk* getChunkAt(int x, int y, int z);

    // Sky color calculation (public for fog blending in GameRenderer)
    void getSkyColor(float timeOfDay, float& r, float& g, float& b) const;
    void getCloudColor(float partialTick, float& r, float& g, float& b) const;

private:
    void createChunks();
    void disposeChunks();
    void sortChunks();

    // Sky rendering initialization
    void initSkyVAOs();
    void disposeSkyVAOs();
    void buildStarData();
    float getTimeOfDay() const;
    std::array<float, 4> getSunriseColor(float timeOfDay) const;
    float getStarBrightness(float timeOfDay) const;

    // Entity rendering helpers
    void renderDroppedItemSprite(int icon, int copies, float playerYRot, unsigned int randomSeed, int skyLight, int blockLight);

    // Shadow rendering
    void renderEntityShadow(Entity* entity, double x, double y, double z, float power, float partialTick);
    void renderTileShadow(Tile* tile, double entityX, double entityY, double entityZ,
                          int tileX, int tileY, int tileZ, float power, float radius);

    // Star vertex data for rendering via Tesselator
    std::vector<float> starVertices;

    // Multithreaded chunk mesh builder
    std::unique_ptr<ChunkMeshBuilder> meshBuilder;

    // Legacy VAO handles (kept for initialization, not used)
    uint32_t starVAO, starVBO, starEBO;
    uint32_t skyVAO, skyVBO, skyEBO;
    uint32_t darkVAO, darkVBO, darkEBO;
    int starIndexCount;
    int skyIndexCount;
    int darkIndexCount;
    bool skyVAOsInitialized;
};

} // namespace mc
