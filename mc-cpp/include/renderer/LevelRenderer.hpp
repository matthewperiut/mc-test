#pragma once

#include "world/Level.hpp"
#include "renderer/Chunk.hpp"
#include "renderer/TileRenderer.hpp"
#include "renderer/Frustum.hpp"
#include <vector>
#include <memory>

namespace mc {

class Minecraft;

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

    // Display lists for sky rendering (matches Java)
    unsigned int starList;
    unsigned int skyList;
    unsigned int darkList;
    bool skyListsInitialized;

    LevelRenderer(Minecraft* minecraft, Level* level);
    ~LevelRenderer();

    // Set level
    void setLevel(Level* level);

    // Rendering
    void render(float partialTick, int pass);
    void renderSky(float partialTick);
    void renderClouds(float partialTick);
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

    // Get chunk at position
    Chunk* getChunkAt(int x, int y, int z);

private:
    void createChunks();
    void disposeChunks();
    void sortChunks();

    // Sky rendering helpers
    void initSkyDisplayLists();
    void buildStarList();
    void buildSkyList();
    void buildDarkList();
    float getTimeOfDay() const;
    void getSkyColor(float timeOfDay, float& r, float& g, float& b) const;
    float* getSunriseColor(float timeOfDay) const;
    float getStarBrightness(float timeOfDay) const;
};

} // namespace mc
