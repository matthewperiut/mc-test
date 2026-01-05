#include "renderer/LevelRenderer.hpp"
#include "core/Minecraft.hpp"
#include "renderer/Tesselator.hpp"
#include <GL/glew.h>
#include <algorithm>

namespace mc {

LevelRenderer::LevelRenderer(Minecraft* minecraft, Level* level)
    : level(nullptr)
    , minecraft(minecraft)
    , xChunks(0), yChunks(0), zChunks(0)
    , renderDistance(8)
    , chunksRendered(0)
    , chunksUpdated(0)
    , firstRebuild(true)
    , destroyProgress(0.0f)
    , destroyX(-1), destroyY(-1), destroyZ(-1)
{
    setLevel(level);
}

LevelRenderer::~LevelRenderer() {
    if (level) {
        level->removeListener(this);
    }
    disposeChunks();
}

void LevelRenderer::setLevel(Level* newLevel) {
    if (level) {
        level->removeListener(this);
    }

    level = newLevel;

    if (level) {
        level->addListener(this);
        tileRenderer.setLevel(level);
        createChunks();
    }
}

void LevelRenderer::createChunks() {
    disposeChunks();

    if (!level) return;

    xChunks = level->width / Chunk::SIZE;
    yChunks = level->height / Chunk::SIZE;
    zChunks = level->depth / Chunk::SIZE;

    chunks.reserve(xChunks * yChunks * zChunks);

    for (int x = 0; x < xChunks; x++) {
        for (int y = 0; y < yChunks; y++) {
            for (int z = 0; z < zChunks; z++) {
                chunks.push_back(std::make_unique<Chunk>(
                    level,
                    x * Chunk::SIZE,
                    y * Chunk::SIZE,
                    z * Chunk::SIZE
                ));
            }
        }
    }
}

void LevelRenderer::disposeChunks() {
    chunks.clear();
    visibleChunks.clear();
    dirtyChunks.clear();
}

Chunk* LevelRenderer::getChunkAt(int x, int y, int z) {
    int cx = x / Chunk::SIZE;
    int cy = y / Chunk::SIZE;
    int cz = z / Chunk::SIZE;

    if (cx < 0 || cx >= xChunks ||
        cy < 0 || cy >= yChunks ||
        cz < 0 || cz >= zChunks) {
        return nullptr;
    }

    int index = (cx * yChunks + cy) * zChunks + cz;
    return chunks[index].get();
}

void LevelRenderer::tileChanged(int x, int y, int z) {
    // Mark affected chunks as dirty
    int radius = 1;  // Blocks can affect adjacent chunks (lighting)

    for (int dx = -radius; dx <= radius; dx++) {
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Chunk* chunk = getChunkAt(x + dx * Chunk::SIZE, y + dy * Chunk::SIZE, z + dz * Chunk::SIZE);
                if (chunk) {
                    chunk->setDirty();
                }
            }
        }
    }

    // Also mark the specific chunk
    Chunk* chunk = getChunkAt(x, y, z);
    if (chunk) {
        chunk->setDirty();
    }
}

void LevelRenderer::allChanged() {
    rebuildAllChunks();
}

void LevelRenderer::lightChanged(int x, int y, int z) {
    tileChanged(x, y, z);
}

void LevelRenderer::rebuildAllChunks() {
    for (auto& chunk : chunks) {
        chunk->setDirty();
    }
}

void LevelRenderer::updateVisibleChunks(double camX, double camY, double camZ) {
    visibleChunks.clear();
    dirtyChunks.clear();

    Frustum& frustum = Frustum::getInstance();
    frustum.update();

    for (auto& chunk : chunks) {
        chunk->calculateDistance(camX, camY, camZ);

        // Check render distance
        float maxDist = static_cast<float>(renderDistance * Chunk::SIZE);
        if (chunk->distanceSq > maxDist * maxDist) {
            chunk->visible = false;
            continue;
        }

        // Frustum culling
        if (!frustum.isVisible(chunk->bb)) {
            chunk->visible = false;
            continue;
        }

        chunk->visible = true;
        visibleChunks.push_back(chunk.get());

        if (chunk->dirty) {
            dirtyChunks.push_back(chunk.get());
        }
    }

    // Sort by distance
    sortChunks();
}

void LevelRenderer::sortChunks() {
    // Sort visible chunks back-to-front for transparency
    std::sort(visibleChunks.begin(), visibleChunks.end(),
        [](Chunk* a, Chunk* b) { return a->distanceSq > b->distanceSq; });

    // Sort dirty chunks front-to-back (rebuild closest first)
    std::sort(dirtyChunks.begin(), dirtyChunks.end(),
        [](Chunk* a, Chunk* b) { return a->distanceSq < b->distanceSq; });
}

void LevelRenderer::updateDirtyChunks() {
    chunksUpdated = 0;

    // Rebuild more chunks on initial load, fewer during gameplay
    int maxUpdates = firstRebuild ? 256 : 8;

    int toUpdate = std::min(static_cast<int>(dirtyChunks.size()), maxUpdates);
    for (int i = 0; i < toUpdate; i++) {
        Chunk* chunk = dirtyChunks[i];
        chunk->rebuild(tileRenderer);
        chunksUpdated++;
    }

    if (firstRebuild && dirtyChunks.size() <= static_cast<size_t>(maxUpdates)) {
        firstRebuild = false;
    }
}

void LevelRenderer::render(float /*partialTick*/, int pass) {
    chunksRendered = 0;

    // Render all visible chunks
    for (Chunk* chunk : visibleChunks) {
        if (chunk->loaded) {
            chunk->render(pass);
            if (pass == 0) chunksRendered++;
        }
    }
}

void LevelRenderer::renderSky(float /*partialTick*/) {
    // Simple sky gradient
    glDisable(GL_TEXTURE_2D);
    glDisable(GL_FOG);

    Tesselator& t = Tesselator::getInstance();

    // Sky color (light blue)
    float skyR = 0.5f, skyG = 0.7f, skyB = 1.0f;

    // Horizon color (lighter)
    float horizonR = 0.8f, horizonG = 0.9f, horizonB = 1.0f;

    glBegin(GL_QUADS);

    // Sky dome (simplified as large quad)
    glColor3f(skyR, skyG, skyB);
    glVertex3f(-512.0f, 100.0f, -512.0f);
    glVertex3f(512.0f, 100.0f, -512.0f);
    glVertex3f(512.0f, 100.0f, 512.0f);
    glVertex3f(-512.0f, 100.0f, 512.0f);

    glEnd();

    glEnable(GL_TEXTURE_2D);
    glEnable(GL_FOG);
}

void LevelRenderer::renderClouds(float /*partialTick*/) {
    // Simple cloud layer
    glDisable(GL_CULL_FACE);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glDisable(GL_TEXTURE_2D);

    float cloudY = 108.0f;
    float cloudAlpha = 0.8f;

    glColor4f(1.0f, 1.0f, 1.0f, cloudAlpha);

    glBegin(GL_QUADS);
    glVertex3f(-512.0f, cloudY, -512.0f);
    glVertex3f(512.0f, cloudY, -512.0f);
    glVertex3f(512.0f, cloudY, 512.0f);
    glVertex3f(-512.0f, cloudY, 512.0f);
    glEnd();

    glEnable(GL_TEXTURE_2D);
    glDisable(GL_BLEND);
    glEnable(GL_CULL_FACE);
}

} // namespace mc
