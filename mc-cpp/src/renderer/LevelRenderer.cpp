#include "renderer/LevelRenderer.hpp"
#include "core/Minecraft.hpp"
#include "renderer/Tesselator.hpp"
#include "renderer/Textures.hpp"
#include "entity/ItemEntity.hpp"
#include "entity/LocalPlayer.hpp"
#include "world/tile/Tile.hpp"
#include <GL/glew.h>
#include <algorithm>
#include <cmath>
#include <cstdlib>

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
    , starList(0), skyList(0), darkList(0)
    , skyListsInitialized(false)
{
    setLevel(level);
}

LevelRenderer::~LevelRenderer() {
    if (level) {
        level->removeListener(this);
    }
    disposeChunks();

    // Delete sky display lists
    if (skyListsInitialized) {
        glDeleteLists(starList, 1);
        glDeleteLists(skyList, 1);
        glDeleteLists(darkList, 1);
    }
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

void LevelRenderer::renderSky(float partialTick) {
    // Match Java LevelRenderer.renderSky() exactly - uses display lists for performance
    if (!minecraft || !minecraft->player) return;

    // Initialize display lists on first call (requires OpenGL context)
    initSkyDisplayLists();

    LocalPlayer* player = minecraft->player;
    float camX = static_cast<float>(player->getInterpolatedX(partialTick));
    float camY = static_cast<float>(player->getInterpolatedY(partialTick) + player->eyeHeight);
    float camZ = static_cast<float>(player->getInterpolatedZ(partialTick));

    // Get time of day
    float timeOfDay = getTimeOfDay();

    // Calculate sky brightness
    float skyBrightness = std::cos(timeOfDay * 3.14159265f * 2.0f) * 2.0f + 0.5f;
    skyBrightness = std::max(0.0f, std::min(1.0f, skyBrightness));

    // Base sky color (light blue) - matches Java's sky color
    float skyR = 0.529f * skyBrightness;
    float skyG = 0.808f * skyBrightness;
    float skyB = 1.0f * skyBrightness;

    glDisable(GL_TEXTURE_2D);
    glDepthMask(GL_FALSE);
    glEnable(GL_FOG);  // Java enables fog here
    glColor3f(skyR, skyG, skyB);

    // Save modelview matrix and translate to camera position
    glPushMatrix();
    glTranslatef(camX, camY, camZ);

    // Render sky plane using display list (matches Java: glCallList(this.skyList))
    glCallList(skyList);

    glDisable(GL_FOG);
    glDisable(GL_ALPHA_TEST);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    // Render sunrise/sunset glow (Java disables texture for this)
    float* sunriseColor = getSunriseColor(timeOfDay);
    if (sunriseColor != nullptr) {
        glDisable(GL_TEXTURE_2D);  // Java: GL11.glDisable(3553)
        glShadeModel(GL_SMOOTH);   // Java: GL11.glShadeModel(7425)
        glPushMatrix();
        glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
        glRotatef(timeOfDay > 0.5f ? 180.0f : 0.0f, 0.0f, 0.0f, 1.0f);

        // Java uses Tesselator.begin(6) which is GL_TRIANGLE_FAN
        glBegin(GL_TRIANGLE_FAN);
        // Center vertex with full alpha
        glColor4f(sunriseColor[0], sunriseColor[1], sunriseColor[2], sunriseColor[3]);
        glVertex3f(0.0f, 100.0f, 0.0f);
        // Outer vertices with 0 alpha (set once, applies to all loop vertices)
        glColor4f(sunriseColor[0], sunriseColor[1], sunriseColor[2], 0.0f);

        for (int i = 0; i <= 16; ++i) {
            float a = static_cast<float>(i) * 3.14159265f * 2.0f / 16.0f;
            float sinA = std::sin(a);
            float cosA = std::cos(a);
            glVertex3f(sinA * 120.0f, cosA * 120.0f, -cosA * 40.0f * sunriseColor[3]);
        }
        glEnd();
        glPopMatrix();
        glShadeModel(GL_FLAT);  // Java: GL11.glShadeModel(7424)
        delete[] sunriseColor;
    }

    glEnable(GL_TEXTURE_2D);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE);  // Java: GL11.glBlendFunc(1, 1) - additive blending

    // Rotate sky objects based on time of day
    glPushMatrix();
    glRotatef(timeOfDay * 360.0f, 1.0f, 0.0f, 0.0f);

    // Render sun
    float ss = 30.0f;
    glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    if (Textures::getInstance().bindTexture("resources/terrain/sun.png")) {
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f); glVertex3f(-ss, 100.0f, -ss);
        glTexCoord2f(1.0f, 0.0f); glVertex3f(ss, 100.0f, -ss);
        glTexCoord2f(1.0f, 1.0f); glVertex3f(ss, 100.0f, ss);
        glTexCoord2f(0.0f, 1.0f); glVertex3f(-ss, 100.0f, ss);
        glEnd();
    }

    // Render moon
    ss = 20.0f;
    if (Textures::getInstance().bindTexture("resources/terrain/moon.png")) {
        glBegin(GL_QUADS);
        glTexCoord2f(1.0f, 1.0f); glVertex3f(-ss, -100.0f, ss);
        glTexCoord2f(0.0f, 1.0f); glVertex3f(ss, -100.0f, ss);
        glTexCoord2f(0.0f, 0.0f); glVertex3f(ss, -100.0f, -ss);
        glTexCoord2f(1.0f, 0.0f); glVertex3f(-ss, -100.0f, -ss);
        glEnd();
    }

    // Render stars at night using display list (matches Java: glCallList(this.starList))
    glDisable(GL_TEXTURE_2D);
    float starBrightness = getStarBrightness(timeOfDay);
    if (starBrightness > 0.0f) {
        glColor4f(starBrightness, starBrightness, starBrightness, starBrightness);
        glCallList(starList);
    }

    glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    glDisable(GL_BLEND);
    glEnable(GL_ALPHA_TEST);
    glEnable(GL_FOG);
    glPopMatrix();  // Pop time rotation

    // Java dark plane: using display list (matches Java: glCallList(this.darkList))
    glDisable(GL_TEXTURE_2D);
    float voidR = skyR * 0.2f + 0.04f;
    float voidG = skyG * 0.2f + 0.04f;
    float voidB = skyB * 0.6f + 0.1f;
    glColor3f(voidR, voidG, voidB);

    glCallList(darkList);

    glPopMatrix();  // Pop camera translation

    // Restore OpenGL states
    glEnable(GL_TEXTURE_2D);
    glDepthMask(GL_TRUE);
    glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
}

void LevelRenderer::initSkyDisplayLists() {
    if (skyListsInitialized) return;

    // Generate display lists (matching Java LevelRenderer constructor)
    starList = glGenLists(1);
    skyList = glGenLists(1);
    darkList = glGenLists(1);

    buildStarList();
    buildSkyList();
    buildDarkList();

    skyListsInitialized = true;
}

void LevelRenderer::buildStarList() {
    // Build star display list (matches Java exactly)
    glNewList(starList, GL_COMPILE);

    // Generate deterministic stars matching Java (seed 10842)
    std::srand(10842);

    glBegin(GL_QUADS);
    for (int i = 0; i < 1500; ++i) {
        float x = (static_cast<float>(std::rand()) / RAND_MAX) * 2.0f - 1.0f;
        float y = (static_cast<float>(std::rand()) / RAND_MAX) * 2.0f - 1.0f;
        float z = (static_cast<float>(std::rand()) / RAND_MAX) * 2.0f - 1.0f;
        float ss = 0.25f + (static_cast<float>(std::rand()) / RAND_MAX) * 0.25f;
        float d = x * x + y * y + z * z;

        if (d < 1.0f && d > 0.01f) {
            d = 1.0f / std::sqrt(d);
            x *= d;
            y *= d;
            z *= d;

            float xp = x * 100.0f;
            float yp = y * 100.0f;
            float zp = z * 100.0f;

            float yRot = std::atan2(x, z);
            float ySin = std::sin(yRot);
            float yCos = std::cos(yRot);
            float xRot = std::atan2(std::sqrt(x * x + z * z), y);
            float xSin = std::sin(xRot);
            float xCos = std::cos(xRot);
            float zRot = (static_cast<float>(std::rand()) / RAND_MAX) * 3.14159265f * 2.0f;
            float zSin = std::sin(zRot);
            float zCos = std::cos(zRot);

            for (int c = 0; c < 4; ++c) {
                float yo = static_cast<float>((c & 2) - 1) * ss;
                float zo = static_cast<float>(((c + 1) & 2) - 1) * ss;
                float _yo = yo * zCos - zo * zSin;
                float _zo = zo * zCos + yo * zSin;
                float __yo = _yo * xSin;
                float _xo = -_yo * xCos;
                float xo = _xo * ySin - _zo * yCos;
                float zo2 = _zo * ySin + _xo * yCos;
                glVertex3f(xp + xo, yp + __yo, zp + zo2);
            }
        }
    }
    glEnd();

    glEndList();
}

void LevelRenderer::buildSkyList() {
    // Build sky plane display list (matches Java)
    glNewList(skyList, GL_COMPILE);

    int s = 64;
    int d = 256 / s + 2;
    float yy = 16.0f;

    glBegin(GL_QUADS);
    for (int xx = -s * d; xx <= s * d; xx += s) {
        for (int zz = -s * d; zz <= s * d; zz += s) {
            glVertex3f(static_cast<float>(xx), yy, static_cast<float>(zz));
            glVertex3f(static_cast<float>(xx + s), yy, static_cast<float>(zz));
            glVertex3f(static_cast<float>(xx + s), yy, static_cast<float>(zz + s));
            glVertex3f(static_cast<float>(xx), yy, static_cast<float>(zz + s));
        }
    }
    glEnd();

    glEndList();
}

void LevelRenderer::buildDarkList() {
    // Build dark plane display list (matches Java)
    glNewList(darkList, GL_COMPILE);

    int s = 64;
    int d = 256 / s + 2;
    float yy = -16.0f;

    glBegin(GL_QUADS);
    for (int xx = -s * d; xx <= s * d; xx += s) {
        for (int zz = -s * d; zz <= s * d; zz += s) {
            glVertex3f(static_cast<float>(xx + s), yy, static_cast<float>(zz));
            glVertex3f(static_cast<float>(xx), yy, static_cast<float>(zz));
            glVertex3f(static_cast<float>(xx), yy, static_cast<float>(zz + s));
            glVertex3f(static_cast<float>(xx + s), yy, static_cast<float>(zz + s));
        }
    }
    glEnd();

    glEndList();
}

float LevelRenderer::getTimeOfDay() const {
    // Simple day/night cycle - full day every 20 minutes (24000 ticks)
    // For now use a fixed daytime value, can be made dynamic later
    if (minecraft && minecraft->level) {
        return minecraft->level->getTimeOfDay();
    }
    return 0.25f;  // Default to noon
}

void LevelRenderer::getSkyColor(float timeOfDay, float& r, float& g, float& b) const {
    // Calculate sky color based on time of day
    // Daytime: light blue, sunset/sunrise: orange-ish, night: dark blue

    float dayBrightness = std::cos(timeOfDay * 3.14159265f * 2.0f) * 0.5f + 0.5f;

    // Base sky color (day)
    r = 0.529f * dayBrightness;
    g = 0.808f * dayBrightness;
    b = 1.0f * dayBrightness;

    // Minimum night brightness
    r = std::max(r, 0.02f);
    g = std::max(g, 0.02f);
    b = std::max(b, 0.06f);
}

float* LevelRenderer::getSunriseColor(float timeOfDay) const {
    // Match Java Dimension.getSunriseColor() exactly
    float threshold = 0.4f;
    float cosVal = std::cos(timeOfDay * 3.14159265f * 2.0f);

    if (cosVal >= -threshold && cosVal <= threshold) {
        // var6 in Java: (cos / 0.4) * 0.5 + 0.5, ranges from 0 to 1
        float var6 = (cosVal / threshold) * 0.5f + 0.5f;

        // var7 in Java: alpha calculation
        float var7 = 1.0f - (1.0f - std::sin(var6 * 3.14159265f)) * 0.99f;
        var7 *= var7;

        float* color = new float[4];
        color[0] = var6 * 0.3f + 0.7f;           // Red: 0.7 to 1.0
        color[1] = var6 * var6 * 0.7f + 0.2f;    // Green: 0.2 to 0.9
        color[2] = var6 * var6 * 0.0f + 0.2f;    // Blue: constant 0.2
        color[3] = var7;                          // Alpha
        return color;
    }
    return nullptr;
}

float LevelRenderer::getStarBrightness(float timeOfDay) const {
    // Stars visible at night
    float cos = std::cos(timeOfDay * 3.14159265f * 2.0f);
    if (cos < 0.0f) {
        return -cos;  // Brighter at midnight
    }
    return 0.0f;
}

void LevelRenderer::renderClouds(float partialTick) {
    // Match Java's renderAdvancedClouds() - 3D extruded clouds
    if (!minecraft || !minecraft->player) return;

    LocalPlayer* player = minecraft->player;

    // Java cloud parameters
    float ss = 12.0f;  // Cloud cell scale in world units
    float h = 4.0f;    // Cloud height/thickness
    float cloudAltitude = 108.0f;  // World Y position of cloud bottom

    // Cloud movement - use game ticks for consistent movement (matching Java)
    // Java: (this.ticks + alpha) * 0.03F
    double cloudDrift = (minecraft->ticks + partialTick) * 0.03;

    // Player position
    double baseX = player->prevX + (player->x - player->prevX) * partialTick;
    double baseZ = player->prevZ + (player->z - player->prevZ) * partialTick;

    // Setup rendering
    glDisable(GL_CULL_FACE);
    glEnable(GL_TEXTURE_2D);
    Textures::getInstance().bindTexture("resources/environment/clouds.png");
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    // Get cloud color based on time of day
    float timeOfDay = getTimeOfDay();
    float dayBrightness = std::cos(timeOfDay * 3.14159265f * 2.0f) * 0.5f + 0.5f;
    float cr = 0.9f * dayBrightness + 0.1f;
    float cg = 0.9f * dayBrightness + 0.1f;
    float cb = 1.0f * dayBrightness + 0.1f;

    // Render radius and chunk size
    int radius = 4;
    int cellsPerChunk = 8;
    float chunkWorldSize = cellsPerChunk * ss;  // 96 blocks per chunk

    // UV scale: cloud texture tiles every 2048 blocks (like Java)
    float uvScale = 1.0f / 2048.0f;

    // Render cloud layer centered on player
    for (int pass = 0; pass < 2; ++pass) {
        // Pass 0: depth only, Pass 1: color
        if (pass == 0) {
            glColorMask(GL_FALSE, GL_FALSE, GL_FALSE, GL_FALSE);
        } else {
            glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
        }

        for (int cx = -radius; cx <= radius; ++cx) {
            for (int cz = -radius; cz <= radius; ++cz) {
                // Calculate world position of this cloud chunk (centered on player)
                float chunkX = static_cast<float>(baseX) + cx * chunkWorldSize;
                float chunkZ = static_cast<float>(baseZ) + cz * chunkWorldSize;

                // UV coordinates based on world position + cloud drift
                // Using world position ensures seamless tiling
                float u0 = static_cast<float>(chunkX + cloudDrift) * uvScale;
                float v0 = chunkZ * uvScale;
                float u1 = static_cast<float>(chunkX + cloudDrift + chunkWorldSize) * uvScale;
                float v1 = (chunkZ + chunkWorldSize) * uvScale;

                float x0 = chunkX;
                float x1 = chunkX + cellsPerChunk * ss;
                float z0 = chunkZ;
                float z1 = chunkZ + cellsPerChunk * ss;
                float y0 = cloudAltitude;
                float y1 = cloudAltitude + h;

                glBegin(GL_QUADS);

                // Bottom face (darkest) - visible from below
                glColor4f(cr * 0.7f, cg * 0.7f, cb * 0.7f, 0.8f);
                glTexCoord2f(u0, v1); glVertex3f(x0, y0, z1);
                glTexCoord2f(u1, v1); glVertex3f(x1, y0, z1);
                glTexCoord2f(u1, v0); glVertex3f(x1, y0, z0);
                glTexCoord2f(u0, v0); glVertex3f(x0, y0, z0);

                // Top face (brightest) - visible from above
                glColor4f(cr, cg, cb, 0.8f);
                glTexCoord2f(u0, v0); glVertex3f(x0, y1, z0);
                glTexCoord2f(u1, v0); glVertex3f(x1, y1, z0);
                glTexCoord2f(u1, v1); glVertex3f(x1, y1, z1);
                glTexCoord2f(u0, v1); glVertex3f(x0, y1, z1);

                // Side faces - rendered as vertical slices for 3D effect
                float sliceUVStep = ss * uvScale;  // UV step per slice

                glColor4f(cr * 0.9f, cg * 0.9f, cb * 0.9f, 0.8f);

                // West side (-X)
                for (int i = 0; i < cellsPerChunk; ++i) {
                    float xi = x0 + i * ss;
                    float ui = u0 + i * sliceUVStep;
                    glTexCoord2f(ui, v0); glVertex3f(xi, y0, z0);
                    glTexCoord2f(ui, v0); glVertex3f(xi, y1, z0);
                    glTexCoord2f(ui, v1); glVertex3f(xi, y1, z1);
                    glTexCoord2f(ui, v1); glVertex3f(xi, y0, z1);
                }

                // East side (+X)
                for (int i = 0; i < cellsPerChunk; ++i) {
                    float xi = x0 + (i + 1) * ss;
                    float ui = u0 + (i + 1) * sliceUVStep;
                    glTexCoord2f(ui, v1); glVertex3f(xi, y0, z1);
                    glTexCoord2f(ui, v1); glVertex3f(xi, y1, z1);
                    glTexCoord2f(ui, v0); glVertex3f(xi, y1, z0);
                    glTexCoord2f(ui, v0); glVertex3f(xi, y0, z0);
                }

                glColor4f(cr * 0.8f, cg * 0.8f, cb * 0.8f, 0.8f);

                // North side (-Z)
                for (int i = 0; i < cellsPerChunk; ++i) {
                    float zi = z0 + i * ss;
                    float vi = v0 + i * sliceUVStep;
                    glTexCoord2f(u1, vi); glVertex3f(x1, y0, zi);
                    glTexCoord2f(u1, vi); glVertex3f(x1, y1, zi);
                    glTexCoord2f(u0, vi); glVertex3f(x0, y1, zi);
                    glTexCoord2f(u0, vi); glVertex3f(x0, y0, zi);
                }

                // South side (+Z)
                for (int i = 0; i < cellsPerChunk; ++i) {
                    float zi = z0 + (i + 1) * ss;
                    float vi = v0 + (i + 1) * sliceUVStep;
                    glTexCoord2f(u0, vi); glVertex3f(x0, y0, zi);
                    glTexCoord2f(u0, vi); glVertex3f(x0, y1, zi);
                    glTexCoord2f(u1, vi); glVertex3f(x1, y1, zi);
                    glTexCoord2f(u1, vi); glVertex3f(x1, y0, zi);
                }

                glEnd();
            }
        }
    }

    // Restore state
    glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
    glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    glDisable(GL_BLEND);
    glEnable(GL_CULL_FACE);
}

void LevelRenderer::renderEntities(float partialTick) {
    if (!level || !minecraft || !minecraft->player) return;

    LocalPlayer* player = minecraft->player;
    double camX = player->getInterpolatedX(partialTick);
    double camY = player->getInterpolatedY(partialTick) + player->eyeHeight;
    double camZ = player->getInterpolatedZ(partialTick);

    // Bind terrain texture for item rendering
    Textures::getInstance().bind("resources/terrain.png");

    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    Tesselator& t = Tesselator::getInstance();

    for (const auto& entity : level->entities) {
        ItemEntity* item = dynamic_cast<ItemEntity*>(entity.get());
        if (!item || item->removed) continue;

        // Get interpolated position
        double ix = item->prevX + (item->x - item->prevX) * partialTick;
        double iy = item->prevY + (item->y - item->prevY) * partialTick;
        double iz = item->prevZ + (item->z - item->prevZ) * partialTick;

        // Skip if too far
        double dx = ix - camX;
        double dy = iy - camY;
        double dz = iz - camZ;
        if (dx * dx + dy * dy + dz * dz > 64 * 64) continue;

        // Get tile for this item
        Tile* tile = nullptr;
        if (item->itemId > 0 && item->itemId < 256) {
            tile = Tile::tiles[item->itemId];
        }
        if (!tile) continue;

        // Java: bob = sin((age + partialTick) / 10.0f + bobOffs) * 0.1f + 0.1f
        float bob = std::sin((static_cast<float>(item->age) + partialTick) / 10.0f + item->bobOffset) * 0.1f + 0.1f;

        // Java: spin = ((age + partialTick) / 20.0f + bobOffs) * (180.0f / PI)
        float spin = ((static_cast<float>(item->age) + partialTick) / 20.0f + item->bobOffset) * 57.29578f;

        glPushMatrix();
        // Position at item location with bob offset
        glTranslated(ix, iy + bob + 0.25, iz);  // +0.25 to raise block center above ground

        // Spin rotation around Y axis (like Java)
        glRotatef(spin, 0.0f, 1.0f, 0.0f);

        // Scale for dropped item (Java uses 0.25f for cube blocks)
        float scale = 0.25f;
        glScalef(scale, scale, scale);

        // Render 3D block using Tesselator
        t.begin(GL_QUADS);
        tileRenderer.renderBlockItem(tile, 1.0f);
        t.end();

        // Render multiple copies for stacks > 1 (like Java)
        int copies = 1;
        if (item->count > 1) copies = 2;
        if (item->count > 5) copies = 3;
        if (item->count > 20) copies = 4;

        for (int c = 1; c < copies; c++) {
            glPushMatrix();
            // Offset each copy slightly (Java uses random offsets)
            float offsetX = (std::sin(c * 1.5f) * 0.1f);
            float offsetY = (c * 0.1f);
            float offsetZ = (std::cos(c * 1.5f) * 0.1f);
            glTranslatef(offsetX, offsetY, offsetZ);

            t.begin(GL_QUADS);
            tileRenderer.renderBlockItem(tile, 1.0f);
            t.end();
            glPopMatrix();
        }

        glPopMatrix();
    }

    glDisable(GL_BLEND);
}

} // namespace mc
