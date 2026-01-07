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
    // Fancy graphics: 3D cloud boxes - exact port from Java using Tesselator
    if (!minecraft || !minecraft->player) return;

    // Ensure proper GL state for cloud rendering
    glDisable(GL_CULL_FACE);
    glDepthMask(GL_TRUE);  // Ensure depth writing is enabled

    LocalPlayer* player = minecraft->player;
    Tesselator& t = Tesselator::getInstance();

    float ss = 12.0f;
    float h = 4.0f;

    // Java: yOffs = player interpolated Y position
    float yOffs = static_cast<float>(player->prevY + (player->y - player->prevY) * partialTick);

    // Java: xo and zo in scaled coordinates
    double xo = (player->prevX + (player->x - player->prevX) * partialTick +
                 (minecraft->ticks + partialTick) * 0.03) / ss;
    double zo = (player->prevZ + (player->z - player->prevZ) * partialTick) / ss + 0.33;

    // Java: yy = 108.0F - yOffs + 0.33F (camera-relative Y)
    float yy = 108.0f - yOffs + 0.33f;

    int xOffs = static_cast<int>(std::floor(xo / 2048.0));
    int zOffs = static_cast<int>(std::floor(zo / 2048.0));
    xo -= xOffs * 2048;
    zo -= zOffs * 2048;

    Textures::getInstance().bindTexture("resources/environment/clouds.png");
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    // Cloud color (simplified - Java uses level.getCloudColor)
    float timeOfDay = getTimeOfDay();
    float dayBrightness = std::cos(timeOfDay * 3.14159265f * 2.0f) * 0.5f + 0.5f;
    float cr = 0.9f * dayBrightness + 0.1f;
    float cg = 0.9f * dayBrightness + 0.1f;
    float cb = 1.0f * dayBrightness + 0.1f;

    float scale = 0.00390625f;  // 1/256
    float uo = static_cast<float>(std::floor(xo)) * scale;
    float vo = static_cast<float>(std::floor(zo)) * scale;
    float xoffs = static_cast<float>(xo - std::floor(xo));
    float zoffs = static_cast<float>(zo - std::floor(zo));
    int D = 8;
    int radius = 3;
    float e = 9.765625E-4f;

    // Java doesn't push/pop matrix - it applies glScalef directly on camera matrix
    // Java's camera has: Trans(0,0,-0.1) * Rot (from moveCameraToPlayer)
    // Our camera has: Rot * Trans(-playerPos)
    // We need rotation-only like Java, so create fresh matrix
    float pitch = player->getInterpolatedXRot(partialTick);
    float yaw = player->getInterpolatedYRot(partialTick);

    glMatrixMode(GL_MODELVIEW);
    glPushMatrix();
    glLoadIdentity();
    // Match Java's moveCameraToPlayer exactly:
    glTranslatef(0.0f, 0.0f, -0.1f);
    glRotatef(pitch, 1.0f, 0.0f, 0.0f);
    glRotatef(yaw + 180.0f, 0.0f, 1.0f, 0.0f);
    // Then Java applies glScalef(ss, 1, ss) for clouds
    glScalef(ss, 1.0f, ss);

    // DIAGNOSTIC: Single pass to test if side faces render at all
    // Two-pass rendering exactly like Java (lines 787-868)
    // Pass 0: Write depth only (color masked off)
    // Pass 1: Write color where GL_LEQUAL depth test passes
    for (int pass = 0; pass < 1; ++pass) {  // Changed to single pass for testing
        glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);  // Always write color
        // if (pass == 0) {
        //     glColorMask(GL_FALSE, GL_FALSE, GL_FALSE, GL_FALSE);
        // } else {
        //     glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
        // }

        for (int xPos = -radius + 1; xPos <= radius; ++xPos) {
            for (int zPos = -radius + 1; zPos <= radius; ++zPos) {
                t.begin(GL_QUADS);

                float xx = static_cast<float>(xPos * D);
                float zz = static_cast<float>(zPos * D);
                float xp = xx - xoffs;
                float zp = zz - zoffs;

                // Bottom face - using yy (relative Y) exactly like Java
                if (yy > -h - 1.0f) {
                    t.color(cr * 0.7f, cg * 0.7f, cb * 0.7f, 0.8f);
                    t.normal(0.0f, -1.0f, 0.0f);
                    t.vertexUV(xp + 0.0f, yy + 0.0f, zp + D, (xx + 0.0f) * scale + uo, (zz + D) * scale + vo);
                    t.vertexUV(xp + D, yy + 0.0f, zp + D, (xx + D) * scale + uo, (zz + D) * scale + vo);
                    t.vertexUV(xp + D, yy + 0.0f, zp + 0.0f, (xx + D) * scale + uo, (zz + 0.0f) * scale + vo);
                    t.vertexUV(xp + 0.0f, yy + 0.0f, zp + 0.0f, (xx + 0.0f) * scale + uo, (zz + 0.0f) * scale + vo);
                }

                // Top face
                if (yy <= h + 1.0f) {
                    t.color(cr, cg, cb, 0.8f);
                    t.normal(0.0f, 1.0f, 0.0f);
                    t.vertexUV(xp + 0.0f, yy + h - e, zp + D, (xx + 0.0f) * scale + uo, (zz + D) * scale + vo);
                    t.vertexUV(xp + D, yy + h - e, zp + D, (xx + D) * scale + uo, (zz + D) * scale + vo);
                    t.vertexUV(xp + D, yy + h - e, zp + 0.0f, (xx + D) * scale + uo, (zz + 0.0f) * scale + vo);
                    t.vertexUV(xp + 0.0f, yy + h - e, zp + 0.0f, (xx + 0.0f) * scale + uo, (zz + 0.0f) * scale + vo);
                }

                // West faces (-X) - internal slices
                t.color(cr * 0.9f, cg * 0.9f, cb * 0.9f, 0.8f);
                if (xPos > -1) {
                    t.normal(-1.0f, 0.0f, 0.0f);
                    for (int i = 0; i < D; ++i) {
                        t.vertexUV(xp + i + 0.0f, yy + 0.0f, zp + D, (xx + i + 0.5f) * scale + uo, (zz + D) * scale + vo);
                        t.vertexUV(xp + i + 0.0f, yy + h, zp + D, (xx + i + 0.5f) * scale + uo, (zz + D) * scale + vo);
                        t.vertexUV(xp + i + 0.0f, yy + h, zp + 0.0f, (xx + i + 0.5f) * scale + uo, (zz + 0.0f) * scale + vo);
                        t.vertexUV(xp + i + 0.0f, yy + 0.0f, zp + 0.0f, (xx + i + 0.5f) * scale + uo, (zz + 0.0f) * scale + vo);
                    }
                }

                // East faces (+X) - internal slices
                if (xPos <= 1) {
                    t.normal(1.0f, 0.0f, 0.0f);
                    for (int i = 0; i < D; ++i) {
                        t.vertexUV(xp + i + 1.0f - e, yy + 0.0f, zp + D, (xx + i + 0.5f) * scale + uo, (zz + D) * scale + vo);
                        t.vertexUV(xp + i + 1.0f - e, yy + h, zp + D, (xx + i + 0.5f) * scale + uo, (zz + D) * scale + vo);
                        t.vertexUV(xp + i + 1.0f - e, yy + h, zp + 0.0f, (xx + i + 0.5f) * scale + uo, (zz + 0.0f) * scale + vo);
                        t.vertexUV(xp + i + 1.0f - e, yy + 0.0f, zp + 0.0f, (xx + i + 0.5f) * scale + uo, (zz + 0.0f) * scale + vo);
                    }
                }

                // North faces (-Z) - internal slices
                t.color(cr * 0.8f, cg * 0.8f, cb * 0.8f, 0.8f);
                if (zPos > -1) {
                    t.normal(0.0f, 0.0f, -1.0f);
                    for (int i = 0; i < D; ++i) {
                        t.vertexUV(xp + 0.0f, yy + h, zp + i + 0.0f, (xx + 0.0f) * scale + uo, (zz + i + 0.5f) * scale + vo);
                        t.vertexUV(xp + D, yy + h, zp + i + 0.0f, (xx + D) * scale + uo, (zz + i + 0.5f) * scale + vo);
                        t.vertexUV(xp + D, yy + 0.0f, zp + i + 0.0f, (xx + D) * scale + uo, (zz + i + 0.5f) * scale + vo);
                        t.vertexUV(xp + 0.0f, yy + 0.0f, zp + i + 0.0f, (xx + 0.0f) * scale + uo, (zz + i + 0.5f) * scale + vo);
                    }
                }

                // South faces (+Z) - internal slices
                if (zPos <= 1) {
                    t.normal(0.0f, 0.0f, 1.0f);
                    for (int i = 0; i < D; ++i) {
                        t.vertexUV(xp + 0.0f, yy + h, zp + i + 1.0f - e, (xx + 0.0f) * scale + uo, (zz + i + 0.5f) * scale + vo);
                        t.vertexUV(xp + D, yy + h, zp + i + 1.0f - e, (xx + D) * scale + uo, (zz + i + 0.5f) * scale + vo);
                        t.vertexUV(xp + D, yy + 0.0f, zp + i + 1.0f - e, (xx + D) * scale + uo, (zz + i + 0.5f) * scale + vo);
                        t.vertexUV(xp + 0.0f, yy + 0.0f, zp + i + 1.0f - e, (xx + 0.0f) * scale + uo, (zz + i + 0.5f) * scale + vo);
                    }
                }

                t.end();
            }
        }
    }

    // Restore matrix and state
    glPopMatrix();
    glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    glDisable(GL_BLEND);
    glEnable(GL_CULL_FACE);
    glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
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
