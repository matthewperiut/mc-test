#include "renderer/LevelRenderer.hpp"
#include "core/Minecraft.hpp"
#include "renderer/Tesselator.hpp"
#include "renderer/Textures.hpp"
#include "entity/ItemEntity.hpp"
#include "entity/LocalPlayer.hpp"
#include "world/tile/Tile.hpp"
#include "item/Item.hpp"
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
    // Dispatch to appropriate cloud renderer based on graphics setting
    if (!minecraft || !minecraft->player) return;

    // Check if dimension is foggy (no clouds in Nether-like dimensions)
    // For now, always render clouds
    if (minecraft->options.fancyGraphics) {
        renderAdvancedClouds(partialTick);
    } else {
        renderFastClouds(partialTick);
    }
}

void LevelRenderer::renderFastClouds(float partialTick) {
    // Fast graphics: flat cloud layer at Y=120 (matching Java's renderClouds fast mode)
    if (!minecraft || !minecraft->player) return;

    LocalPlayer* player = minecraft->player;

    // Player position
    double playerX = player->prevX + (player->x - player->prevX) * partialTick;
    double playerZ = player->prevZ + (player->z - player->prevZ) * partialTick;

    // Cloud grid parameters (matching Java exactly)
    int s = 32;        // Cell size in blocks
    int d = 256 / s;   // Grid divisions = 8

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

    // UV scale: 1/2048 (Java: 4.8828125E-4F)
    float scale = 1.0f / 2048.0f;

    // Cloud drift
    double cloudDrift = (minecraft->ticks + partialTick) * 0.03;

    // Cloud altitude in world space
    float cloudY = 120.0f;

    // Center cloud grid on player position (snapped to grid)
    double centerX = std::floor(playerX / s) * s;
    double centerZ = std::floor(playerZ / s) * s;

    // Render flat cloud grid at world coordinates
    glBegin(GL_QUADS);
    glColor4f(cr, cg, cb, 0.8f);

    for (int gx = -d; gx < d; gx++) {
        for (int gz = -d; gz < d; gz++) {
            // World coordinates of this cloud cell
            float x0 = static_cast<float>(centerX + gx * s);
            float x1 = static_cast<float>(centerX + (gx + 1) * s);
            float z0 = static_cast<float>(centerZ + gz * s);
            float z1 = static_cast<float>(centerZ + (gz + 1) * s);

            // UV coords based on world position + cloud drift
            float u0 = static_cast<float>(x0 + cloudDrift) * scale;
            float u1 = static_cast<float>(x1 + cloudDrift) * scale;
            float v0 = z0 * scale;
            float v1 = z1 * scale;

            glTexCoord2f(u0, v1); glVertex3f(x0, cloudY, z1);
            glTexCoord2f(u1, v1); glVertex3f(x1, cloudY, z1);
            glTexCoord2f(u1, v0); glVertex3f(x1, cloudY, z0);
            glTexCoord2f(u0, v0); glVertex3f(x0, cloudY, z0);
        }
    }

    glEnd();

    // Restore state
    glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    glDisable(GL_BLEND);
    glEnable(GL_CULL_FACE);
}

void LevelRenderer::renderAdvancedClouds(float partialTick) {
    if (!minecraft || !minecraft->player) return;

    LocalPlayer* player = minecraft->player;

    // Player position (matching flat clouds exactly)
    double playerX = player->prevX + (player->x - player->prevX) * partialTick;
    double playerZ = player->prevZ + (player->z - player->prevZ) * partialTick;

    // Cloud parameters - cell size matches texture pixel resolution
    // UV scale 1/2048, texture 256x256, so 1 texel = 2048/256 = 8 blocks
    int cellSize = 8;               // One texture pixel = 8 blocks
    int gridExtent = 32;            // Render ~256 blocks out
    float cloudTop = 120.0f;        // Top matches flat clouds at Y=120
    float cloudHeight = 4.0f;       // Thickness of cloud layer
    float cloudY = cloudTop - cloudHeight;
    float renderDist = 256.0f;

    // Cloud drift - IDENTICAL to flat clouds
    double cloudDrift = (minecraft->ticks + partialTick) * 0.03;

    // UV scale - same as flat clouds (1/2048)
    float uvScale = 1.0f / 2048.0f;

    // Center grid on player (snapped to cell size)
    double centerX = std::floor(playerX / cellSize) * cellSize;
    double centerZ = std::floor(playerZ / cellSize) * cellSize;

    // Fractional drift for smooth geometry movement
    // This offsets geometry so side walls move smoothly with the texture
    double driftOffset = std::fmod(cloudDrift, static_cast<double>(cellSize));

    // Get time of day for color (same as flat clouds)
    float timeOfDay = getTimeOfDay();
    float dayBrightness = std::cos(timeOfDay * 3.14159265f * 2.0f) * 0.5f + 0.5f;
    dayBrightness = std::max(0.2f, std::min(1.0f, dayBrightness));

    // Cloud color - IDENTICAL to flat clouds
    float cloudR = 0.9f * dayBrightness + 0.1f;
    float cloudG = 0.9f * dayBrightness + 0.1f;
    float cloudB = 1.0f * dayBrightness + 0.1f;

    // Sunset/sunrise tinting
    float sunHeight = std::cos(timeOfDay * 3.14159265f * 2.0f);
    if (sunHeight < 0.3f && sunHeight > -0.3f) {
        float t = 1.0f - std::abs(sunHeight) / 0.3f;
        cloudR += t * 0.4f;
        cloudG += t * 0.1f;
    }

    // Face brightness multipliers
    float topBright = 1.0f;
    float bottomBright = 0.7f;
    float sideBright = 0.9f;

    // Setup GL state - use texture with alpha test
    glEnable(GL_TEXTURE_2D);
    Textures::getInstance().bindTexture("resources/environment/clouds.png");
    glEnable(GL_CULL_FACE);
    glCullFace(GL_BACK);
    glFrontFace(GL_CCW);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    // Two-pass rendering for proper transparency
    for (int pass = 0; pass < 2; ++pass) {
        if (pass == 0) {
            // Depth-only pass with alpha test
            glColorMask(GL_FALSE, GL_FALSE, GL_FALSE, GL_FALSE);
            glDepthMask(GL_TRUE);
            glDisable(GL_BLEND);
            glEnable(GL_ALPHA_TEST);
            glAlphaFunc(GL_GREATER, 0.5f);
        } else {
            // Color pass
            glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
            glDepthMask(GL_FALSE);
            glDepthFunc(GL_EQUAL);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glEnable(GL_ALPHA_TEST);
            glAlphaFunc(GL_GREATER, 0.5f);
        }

        glBegin(GL_QUADS);

        for (int gx = -gridExtent; gx < gridExtent; ++gx) {
            for (int gz = -gridExtent; gz < gridExtent; ++gz) {
                // World coordinates of this cloud cell (offset by drift for smooth movement)
                float x0 = static_cast<float>(centerX + gx * cellSize - driftOffset);
                float x1 = x0 + cellSize;
                float z0 = static_cast<float>(centerZ + gz * cellSize);
                float z1 = z0 + cellSize;

                // Distance check (culling only, no alpha fade)
                float cellCenterX = x0 + cellSize * 0.5f;
                float cellCenterZ = z0 + cellSize * 0.5f;
                float dx = cellCenterX - static_cast<float>(playerX);
                float dz = cellCenterZ - static_cast<float>(playerZ);
                float dist = std::sqrt(dx * dx + dz * dz);
                if (dist > renderDist) continue;

                // Fixed alpha - same as flat clouds (0.8)
                float alpha = 0.8f;

                float y0 = cloudY;
                float y1 = cloudTop;

                // UV coordinates (same calculation as flat clouds)
                float u0 = static_cast<float>(x0 + cloudDrift) * uvScale;
                float u1 = static_cast<float>(x1 + cloudDrift) * uvScale;
                float v0 = z0 * uvScale;
                float v1 = z1 * uvScale;

                // Center UV for side faces (sample center of texel)
                float uCenter = (u0 + u1) * 0.5f;
                float vCenter = (v0 + v1) * 0.5f;

                // Top face (brightest)
                glColor4f(cloudR * topBright, cloudG * topBright, cloudB * topBright, alpha);
                glTexCoord2f(u0, v1); glVertex3f(x0, y1, z1);
                glTexCoord2f(u1, v1); glVertex3f(x1, y1, z1);
                glTexCoord2f(u1, v0); glVertex3f(x1, y1, z0);
                glTexCoord2f(u0, v0); glVertex3f(x0, y1, z0);

                // Bottom face (darker)
                glColor4f(cloudR * bottomBright, cloudG * bottomBright, cloudB * bottomBright, alpha);
                glTexCoord2f(u0, v0); glVertex3f(x0, y0, z0);
                glTexCoord2f(u1, v0); glVertex3f(x1, y0, z0);
                glTexCoord2f(u1, v1); glVertex3f(x1, y0, z1);
                glTexCoord2f(u0, v1); glVertex3f(x0, y0, z1);

                // Side faces all sample from cell center (same texel as top/bottom)
                glColor4f(cloudR * sideBright, cloudG * sideBright, cloudB * sideBright, alpha);

                // -X face
                glTexCoord2f(uCenter, vCenter); glVertex3f(x0, y0, z1);
                glTexCoord2f(uCenter, vCenter); glVertex3f(x0, y1, z1);
                glTexCoord2f(uCenter, vCenter); glVertex3f(x0, y1, z0);
                glTexCoord2f(uCenter, vCenter); glVertex3f(x0, y0, z0);

                // +X face
                glTexCoord2f(uCenter, vCenter); glVertex3f(x1, y0, z0);
                glTexCoord2f(uCenter, vCenter); glVertex3f(x1, y1, z0);
                glTexCoord2f(uCenter, vCenter); glVertex3f(x1, y1, z1);
                glTexCoord2f(uCenter, vCenter); glVertex3f(x1, y0, z1);

                // -Z face
                glTexCoord2f(uCenter, vCenter); glVertex3f(x0, y0, z0);
                glTexCoord2f(uCenter, vCenter); glVertex3f(x0, y1, z0);
                glTexCoord2f(uCenter, vCenter); glVertex3f(x1, y1, z0);
                glTexCoord2f(uCenter, vCenter); glVertex3f(x1, y0, z0);

                // +Z face
                glTexCoord2f(uCenter, vCenter); glVertex3f(x1, y0, z1);
                glTexCoord2f(uCenter, vCenter); glVertex3f(x1, y1, z1);
                glTexCoord2f(uCenter, vCenter); glVertex3f(x0, y1, z1);
                glTexCoord2f(uCenter, vCenter); glVertex3f(x0, y0, z1);
            }
        }

        glEnd();
    }

    // Restore GL state
    glDepthFunc(GL_LEQUAL);
    glDepthMask(GL_TRUE);
    glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
    glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    glDisable(GL_BLEND);
    glDisable(GL_ALPHA_TEST);
    glDisable(GL_CULL_FACE);
    glEnable(GL_TEXTURE_2D);
    glEnable(GL_DEPTH_TEST);
}

void LevelRenderer::renderEntities(float partialTick) {
    if (!level || !minecraft || !minecraft->player) return;

    LocalPlayer* player = minecraft->player;
    double camX = player->getInterpolatedX(partialTick);
    double camY = player->getInterpolatedY(partialTick) + player->eyeHeight;
    double camZ = player->getInterpolatedZ(partialTick);

    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glEnable(GL_RESCALE_NORMAL);

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

        // Java: bob = sin((age + partialTick) / 10.0f + bobOffs) * 0.1f + 0.1f
        float bob = std::sin((static_cast<float>(item->age) + partialTick) / 10.0f + item->bobOffset) * 0.1f + 0.1f;

        // Java: spin = ((age + partialTick) / 20.0f + bobOffs) * (180.0f / PI)
        float spin = ((static_cast<float>(item->age) + partialTick) / 20.0f + item->bobOffset) * 57.29578f;

        // Calculate number of copies based on stack size (Java ItemRenderer)
        int copies = 1;
        if (item->count > 1) copies = 2;
        if (item->count > 5) copies = 3;
        if (item->count > 20) copies = 4;

        // Seed random for consistent offsets per entity
        unsigned int randomSeed = 187;

        glPushMatrix();
        // Position at item location with bob offset
        glTranslated(ix, iy + bob, iz);

        if (item->itemId > 0 && item->itemId < 256) {
            // Render as 3D block
            Tile* tile = Tile::tiles[item->itemId];
            if (tile && TileRenderer::canRender(static_cast<int>(tile->renderShape))) {
                Textures::getInstance().bind("resources/terrain.png");

                // Spin rotation around Y axis
                glRotatef(spin, 0.0f, 1.0f, 0.0f);

                // Scale for dropped block (Java: 0.25 for cubes, 0.5 for non-cubes)
                float scale = 0.25f;
                if (tile->renderShape != TileShape::CUBE) {
                    scale = 0.5f;
                }
                glScalef(scale, scale, scale);

                for (int c = 0; c < copies; c++) {
                    glPushMatrix();
                    if (c > 0) {
                        // Random offset for stacked items
                        float xo = ((randomSeed = randomSeed * 1103515245 + 12345) % 1000 / 500.0f - 1.0f) * 0.2f / scale;
                        float yo = ((randomSeed = randomSeed * 1103515245 + 12345) % 1000 / 500.0f - 1.0f) * 0.2f / scale;
                        float zo = ((randomSeed = randomSeed * 1103515245 + 12345) % 1000 / 500.0f - 1.0f) * 0.2f / scale;
                        glTranslatef(xo, yo, zo);
                    }
                    t.begin(GL_QUADS);
                    tileRenderer.renderBlockItem(tile, 1.0f);
                    t.end();
                    glPopMatrix();
                }
            } else {
                // Non-cube block - render as flat sprite from terrain.png
                Textures::getInstance().bind("resources/terrain.png");
                int icon = tile ? tile->getTexture(0) : 0;
                renderDroppedItemSprite(icon, copies, player->yRot, randomSeed);
            }
        } else if (item->itemId >= 256) {
            // Render as flat item sprite from items.png
            Textures::getInstance().bind("resources/gui/items.png");
            Item* itemDef = Item::byId(item->itemId);
            int icon = itemDef ? itemDef->getIcon() : 0;
            renderDroppedItemSprite(icon, copies, player->yRot, randomSeed);
        }

        glPopMatrix();
    }

    glDisable(GL_RESCALE_NORMAL);
    glDisable(GL_BLEND);
}

void LevelRenderer::renderDroppedItemSprite(int icon, int copies, float playerYRot, unsigned int randomSeed) {
    // Calculate UV coordinates (matching Java ItemRenderer)
    float u0 = static_cast<float>(icon % 16 * 16) / 256.0f;
    float u1 = (static_cast<float>(icon % 16 * 16) + 16.0f) / 256.0f;
    float v0 = static_cast<float>(icon / 16 * 16) / 256.0f;
    float v1 = (static_cast<float>(icon / 16 * 16) + 16.0f) / 256.0f;

    float r = 1.0f;
    float xo = 0.5f;
    float yo = 0.25f;

    // Scale for dropped item sprite
    glScalef(0.5f, 0.5f, 0.5f);

    Tesselator& t = Tesselator::getInstance();

    for (int c = 0; c < copies; c++) {
        glPushMatrix();
        if (c > 0) {
            // Random offset for stacked items
            float _xo = ((randomSeed = randomSeed * 1103515245 + 12345) % 1000 / 500.0f - 1.0f) * 0.3f;
            float _yo = ((randomSeed = randomSeed * 1103515245 + 12345) % 1000 / 500.0f - 1.0f) * 0.3f;
            float _zo = ((randomSeed = randomSeed * 1103515245 + 12345) % 1000 / 500.0f - 1.0f) * 0.3f;
            glTranslatef(_xo, _yo, _zo);
        }

        // Billboard toward player (rotate to face camera)
        glRotatef(180.0f - playerYRot, 0.0f, 1.0f, 0.0f);

        // Render flat sprite quad
        t.begin(GL_QUADS);
        t.normal(0.0f, 1.0f, 0.0f);
        t.tex(u0, v1); t.vertex(0.0f - xo, 0.0f - yo, 0.0f);
        t.tex(u1, v1); t.vertex(r - xo, 0.0f - yo, 0.0f);
        t.tex(u1, v0); t.vertex(r - xo, 1.0f - yo, 0.0f);
        t.tex(u0, v0); t.vertex(0.0f - xo, 1.0f - yo, 0.0f);
        t.end();

        glPopMatrix();
    }
}

} // namespace mc
