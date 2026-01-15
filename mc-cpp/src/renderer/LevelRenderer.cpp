#include "renderer/LevelRenderer.hpp"
#include "core/Minecraft.hpp"
#include "renderer/Tesselator.hpp"
#include "renderer/Textures.hpp"
#include "renderer/MatrixStack.hpp"
#include "renderer/ShaderManager.hpp"
#include "renderer/model/ChickenModel.hpp"
#include "entity/ItemEntity.hpp"
#include "entity/LocalPlayer.hpp"
#include "entity/Chicken.hpp"
#include "world/tile/Tile.hpp"
#include "item/Item.hpp"
#include "util/Mth.hpp"
#include "renderer/backend/RenderDevice.hpp"
#ifndef MC_RENDERER_METAL
#include <GL/glew.h>
#endif
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
    , starVAO(0), starVBO(0), starEBO(0)
    , skyVAO(0), skyVBO(0), skyEBO(0)
    , darkVAO(0), darkVBO(0), darkEBO(0)
    , starIndexCount(0), skyIndexCount(0), darkIndexCount(0)
    , skyVAOsInitialized(false)
{
    setLevel(level);
}

LevelRenderer::~LevelRenderer() {
    if (level) {
        level->removeListener(this);
    }
    disposeChunks();
    disposeSkyVAOs();
}

void LevelRenderer::disposeSkyVAOs() {
#ifndef MC_RENDERER_METAL
    if (starVAO) { glDeleteVertexArrays(1, &starVAO); starVAO = 0; }
    if (starVBO) { glDeleteBuffers(1, &starVBO); starVBO = 0; }
    if (starEBO) { glDeleteBuffers(1, &starEBO); starEBO = 0; }
    if (skyVAO) { glDeleteVertexArrays(1, &skyVAO); skyVAO = 0; }
    if (skyVBO) { glDeleteBuffers(1, &skyVBO); skyVBO = 0; }
    if (skyEBO) { glDeleteBuffers(1, &skyEBO); skyEBO = 0; }
    if (darkVAO) { glDeleteVertexArrays(1, &darkVAO); darkVAO = 0; }
    if (darkVBO) { glDeleteBuffers(1, &darkVBO); darkVBO = 0; }
    if (darkEBO) { glDeleteBuffers(1, &darkEBO); darkEBO = 0; }
#endif
    skyVAOsInitialized = false;
}

void LevelRenderer::setLevel(Level* newLevel) {
    if (level) {
        level->removeListener(this);
    }

    level = newLevel;
    particleEngine.setLevel(newLevel);

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

void LevelRenderer::addParticle(const std::string& name, double x, double y, double z,
                                 double xa, double ya, double za) {
    // Distance check - only add particles within 16 blocks of player
    if (minecraft && minecraft->player) {
        double dx = minecraft->player->x - x;
        double dy = minecraft->player->y - y;
        double dz = minecraft->player->z - z;
        double distSq = dx * dx + dy * dy + dz * dz;
        constexpr double maxDistSq = 16.0 * 16.0;  // 16 blocks
        if (distSq > maxDistSq) return;
    }

    particleEngine.addParticle(name, x, y, z, xa, ya, za);
}

void LevelRenderer::tickParticles() {
    particleEngine.tick();
}

void LevelRenderer::renderParticles(float partialTick) {
    if (!minecraft || !minecraft->player) return;
    particleEngine.render(minecraft->player, partialTick);
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

#ifndef MC_RENDERER_METAL
// Helper to setup a simple position-only VAO for sky geometry
static void setupSkyVAO(GLuint vao, GLuint vbo, GLuint ebo,
                        const std::vector<float>& vertices,
                        const std::vector<unsigned int>& indices) {
    glBindVertexArray(vao);

    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferData(GL_ARRAY_BUFFER, vertices.size() * sizeof(float), vertices.data(), GL_STATIC_DRAW);

    // Position attribute only (location 0) - 3 floats
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(0);

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.size() * sizeof(unsigned int), indices.data(), GL_STATIC_DRAW);

    glBindVertexArray(0);
}
#endif

void LevelRenderer::initSkyVAOs() {
    if (skyVAOsInitialized) return;

#ifndef MC_RENDERER_METAL
    // Generate VAOs/VBOs/EBOs
    glGenVertexArrays(1, &starVAO);
    glGenBuffers(1, &starVBO);
    glGenBuffers(1, &starEBO);

    glGenVertexArrays(1, &skyVAO);
    glGenBuffers(1, &skyVBO);
    glGenBuffers(1, &skyEBO);

    glGenVertexArrays(1, &darkVAO);
    glGenBuffers(1, &darkVBO);
    glGenBuffers(1, &darkEBO);

    buildStarVAO();
    buildSkyVAO();
    buildDarkVAO();
#endif

    skyVAOsInitialized = true;
}

#ifndef MC_RENDERER_METAL
void LevelRenderer::buildStarVAO() {
    std::vector<float> vertices;
    std::vector<unsigned int> indices;

    // Generate deterministic stars matching Java (seed 10842)
    std::srand(10842);
    unsigned int vertexIndex = 0;

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

            // 4 vertices per star quad
            for (int c = 0; c < 4; ++c) {
                float yo = static_cast<float>((c & 2) - 1) * ss;
                float zo = static_cast<float>(((c + 1) & 2) - 1) * ss;
                float _yo = yo * zCos - zo * zSin;
                float _zo = zo * zCos + yo * zSin;
                float __yo = _yo * xSin;
                float _xo = -_yo * xCos;
                float xo = _xo * ySin - _zo * yCos;
                float zo2 = _zo * ySin + _xo * yCos;
                vertices.push_back(xp + xo);
                vertices.push_back(yp + __yo);
                vertices.push_back(zp + zo2);
            }

            // 2 triangles per quad (6 indices)
            indices.push_back(vertexIndex);
            indices.push_back(vertexIndex + 1);
            indices.push_back(vertexIndex + 2);
            indices.push_back(vertexIndex);
            indices.push_back(vertexIndex + 2);
            indices.push_back(vertexIndex + 3);
            vertexIndex += 4;
        }
    }

    starIndexCount = static_cast<int>(indices.size());
    setupSkyVAO(starVAO, starVBO, starEBO, vertices, indices);
}
#endif

#ifndef MC_RENDERER_METAL
void LevelRenderer::buildSkyVAO() {
    std::vector<float> vertices;
    std::vector<unsigned int> indices;

    int s = 64;
    int d = 256 / s + 2;
    float yy = 16.0f;

    unsigned int vertexIndex = 0;
    for (int xx = -s * d; xx <= s * d; xx += s) {
        for (int zz = -s * d; zz <= s * d; zz += s) {
            // 4 vertices per quad
            vertices.push_back(static_cast<float>(xx));
            vertices.push_back(yy);
            vertices.push_back(static_cast<float>(zz));

            vertices.push_back(static_cast<float>(xx + s));
            vertices.push_back(yy);
            vertices.push_back(static_cast<float>(zz));

            vertices.push_back(static_cast<float>(xx + s));
            vertices.push_back(yy);
            vertices.push_back(static_cast<float>(zz + s));

            vertices.push_back(static_cast<float>(xx));
            vertices.push_back(yy);
            vertices.push_back(static_cast<float>(zz + s));

            // 2 triangles (6 indices)
            indices.push_back(vertexIndex);
            indices.push_back(vertexIndex + 1);
            indices.push_back(vertexIndex + 2);
            indices.push_back(vertexIndex);
            indices.push_back(vertexIndex + 2);
            indices.push_back(vertexIndex + 3);
            vertexIndex += 4;
        }
    }

    skyIndexCount = static_cast<int>(indices.size());
    setupSkyVAO(skyVAO, skyVBO, skyEBO, vertices, indices);
}

void LevelRenderer::buildDarkVAO() {
    std::vector<float> vertices;
    std::vector<unsigned int> indices;

    int s = 64;
    int d = 256 / s + 2;
    float yy = -16.0f;

    unsigned int vertexIndex = 0;
    for (int xx = -s * d; xx <= s * d; xx += s) {
        for (int zz = -s * d; zz <= s * d; zz += s) {
            // 4 vertices per quad (reversed winding)
            vertices.push_back(static_cast<float>(xx + s));
            vertices.push_back(yy);
            vertices.push_back(static_cast<float>(zz));

            vertices.push_back(static_cast<float>(xx));
            vertices.push_back(yy);
            vertices.push_back(static_cast<float>(zz));

            vertices.push_back(static_cast<float>(xx));
            vertices.push_back(yy);
            vertices.push_back(static_cast<float>(zz + s));

            vertices.push_back(static_cast<float>(xx + s));
            vertices.push_back(yy);
            vertices.push_back(static_cast<float>(zz + s));

            // 2 triangles (6 indices)
            indices.push_back(vertexIndex);
            indices.push_back(vertexIndex + 1);
            indices.push_back(vertexIndex + 2);
            indices.push_back(vertexIndex);
            indices.push_back(vertexIndex + 2);
            indices.push_back(vertexIndex + 3);
            vertexIndex += 4;
        }
    }

    darkIndexCount = static_cast<int>(indices.size());
    setupSkyVAO(darkVAO, darkVBO, darkEBO, vertices, indices);
}
#endif

void LevelRenderer::renderSky(float partialTick) {
    if (!minecraft || !minecraft->player) return;

    initSkyVAOs();

    LocalPlayer* player = minecraft->player;
    float camX = static_cast<float>(player->getInterpolatedX(partialTick));
    float camY = static_cast<float>(player->getInterpolatedY(partialTick) + player->eyeHeight);
    float camZ = static_cast<float>(player->getInterpolatedZ(partialTick));

    float timeOfDay = getTimeOfDay();

    // Calculate sky brightness
    float skyBrightness = std::cos(timeOfDay * 3.14159265f * 2.0f) * 2.0f + 0.5f;
    skyBrightness = std::max(0.0f, std::min(1.0f, skyBrightness));

    // Base sky color
    float skyR = 0.529f * skyBrightness;
    float skyG = 0.808f * skyBrightness;
    float skyB = 1.0f * skyBrightness;

    auto& device = RenderDevice::get();
    device.setDepthWrite(false);

    // Save modelview matrix and translate to camera position
    MatrixStack::modelview().push();
    MatrixStack::modelview().translate(camX, camY, camZ);

    // Use world shader for sky plane (has fog which creates gradient effect)
    ShaderManager::getInstance().useWorldShader();
    ShaderManager::getInstance().updateMatrices();
    ShaderManager::getInstance().setUseTexture(false);  // No texture, just vertex color + fog
    ShaderManager::getInstance().setAlphaTest(0.0f);

    // Render sky plane using Tesselator (provides proper vertex format for fog)
    int s = 64;
    int d = 256 / s + 2;
    float yy = 16.0f;

    Tesselator& t = Tesselator::getInstance();
    t.begin(GL_QUADS);
    t.color(skyR, skyG, skyB, 1.0f);
    for (int xx = -s * d; xx <= s * d; xx += s) {
        for (int zz = -s * d; zz <= s * d; zz += s) {
            t.vertex(static_cast<float>(xx), yy, static_cast<float>(zz));
            t.vertex(static_cast<float>(xx + s), yy, static_cast<float>(zz));
            t.vertex(static_cast<float>(xx + s), yy, static_cast<float>(zz + s));
            t.vertex(static_cast<float>(xx), yy, static_cast<float>(zz + s));
        }
    }
    t.end();

    // Render sunrise/sunset glow
    device.setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);

    float* sunriseColor = getSunriseColor(timeOfDay);
    if (sunriseColor != nullptr) {
        MatrixStack::modelview().push();
        MatrixStack::modelview().rotate(90.0f, 1.0f, 0.0f, 0.0f);
        MatrixStack::modelview().rotate(timeOfDay > 0.5f ? 180.0f : 0.0f, 0.0f, 0.0f, 1.0f);

        ShaderManager::getInstance().updateMatrices();
        ShaderManager::getInstance().setUseTexture(false);
        ShaderManager::getInstance().setUseVertexColor(true);  // Use per-vertex colors for gradient

        Tesselator& t = Tesselator::getInstance();
        t.begin(GL_TRIANGLE_FAN);
        // Center vertex with full alpha
        t.color(sunriseColor[0], sunriseColor[1], sunriseColor[2], sunriseColor[3]);
        t.vertex(0.0f, 100.0f, 0.0f);
        // Outer vertices with 0 alpha
        for (int i = 0; i <= 16; ++i) {
            float a = static_cast<float>(i) * 3.14159265f * 2.0f / 16.0f;
            float sinA = std::sin(a);
            float cosA = std::cos(a);
            t.color(sunriseColor[0], sunriseColor[1], sunriseColor[2], 0.0f);
            t.vertex(sinA * 120.0f, cosA * 120.0f, -cosA * 40.0f * sunriseColor[3]);
        }
        t.end();

        MatrixStack::modelview().pop();
        delete[] sunriseColor;
    }

    device.setBlend(true, BlendFactor::SrcAlpha, BlendFactor::One);  // Additive blending

    // Rotate sky objects based on time of day
    MatrixStack::modelview().push();
    MatrixStack::modelview().rotate(timeOfDay * 360.0f, 1.0f, 0.0f, 0.0f);

    // Use sky shader for sun/moon (no fog applied unlike world shader)
    ShaderManager::getInstance().useSkyShader();
    ShaderManager::getInstance().updateMatrices();
    ShaderManager::getInstance().setUseTexture(true);

    // Render sun (Java size: 30.0f)
    float ss = 30.0f;
    if (Textures::getInstance().bindTexture("resources/terrain/sun.png")) {
        t.begin(GL_QUADS);
        t.color(1.0f, 1.0f, 1.0f, 1.0f);
        t.tex(0.0f, 0.0f); t.vertex(-ss, 100.0f, -ss);
        t.tex(1.0f, 0.0f); t.vertex(ss, 100.0f, -ss);
        t.tex(1.0f, 1.0f); t.vertex(ss, 100.0f, ss);
        t.tex(0.0f, 1.0f); t.vertex(-ss, 100.0f, ss);
        t.end();
    }

    // Render moon (Java size: 20.0f)
    ss = 20.0f;
    if (Textures::getInstance().bindTexture("resources/terrain/moon.png")) {
        t.begin(GL_QUADS);
        t.color(1.0f, 1.0f, 1.0f, 1.0f);
        t.tex(1.0f, 1.0f); t.vertex(-ss, -100.0f, ss);
        t.tex(0.0f, 1.0f); t.vertex(ss, -100.0f, ss);
        t.tex(0.0f, 0.0f); t.vertex(ss, -100.0f, -ss);
        t.tex(1.0f, 0.0f); t.vertex(-ss, -100.0f, -ss);
        t.end();
    }

    // Render stars at night using VAO
    float starBrightness = getStarBrightness(timeOfDay);
    if (starBrightness > 0.0f) {
        ShaderManager::getInstance().setUseTexture(false);
        ShaderManager::getInstance().setSkyColor(starBrightness, starBrightness, starBrightness, starBrightness);

#ifndef MC_RENDERER_METAL
        glBindVertexArray(starVAO);
        glDrawElements(GL_TRIANGLES, starIndexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
#endif
    }

    device.setBlend(false);
    MatrixStack::modelview().pop();  // Pop time rotation

    // Render dark plane below using world shader (fog creates gradient)
    float voidR = skyR * 0.2f + 0.04f;
    float voidG = skyG * 0.2f + 0.04f;
    float voidB = skyB * 0.6f + 0.1f;

    ShaderManager::getInstance().useWorldShader();
    ShaderManager::getInstance().updateMatrices();
    ShaderManager::getInstance().setUseTexture(false);  // No texture, just vertex color + fog
    ShaderManager::getInstance().setAlphaTest(0.0f);

    float yyDark = -16.0f;
    t.begin(GL_QUADS);
    t.color(voidR, voidG, voidB, 1.0f);
    for (int xx = -s * d; xx <= s * d; xx += s) {
        for (int zz = -s * d; zz <= s * d; zz += s) {
            // Reversed winding for bottom-facing plane
            t.vertex(static_cast<float>(xx + s), yyDark, static_cast<float>(zz));
            t.vertex(static_cast<float>(xx), yyDark, static_cast<float>(zz));
            t.vertex(static_cast<float>(xx), yyDark, static_cast<float>(zz + s));
            t.vertex(static_cast<float>(xx + s), yyDark, static_cast<float>(zz + s));
        }
    }
    t.end();

    MatrixStack::modelview().pop();  // Pop camera translation

    // Restore state
    device.setDepthWrite(true);
    device.setBlend(false);

    // Switch back to world shader for terrain
    ShaderManager::getInstance().useWorldShader();
    ShaderManager::getInstance().updateMatrices();
}

float LevelRenderer::getTimeOfDay() const {
    if (minecraft && minecraft->level) {
        return minecraft->level->getTimeOfDay();
    }
    return 0.25f;  // Default to noon
}

void LevelRenderer::getSkyColor(float timeOfDay, float& r, float& g, float& b) const {
    float dayBrightness = std::cos(timeOfDay * 3.14159265f * 2.0f) * 0.5f + 0.5f;

    r = 0.529f * dayBrightness;
    g = 0.808f * dayBrightness;
    b = 1.0f * dayBrightness;

    r = std::max(r, 0.02f);
    g = std::max(g, 0.02f);
    b = std::max(b, 0.06f);
}

float* LevelRenderer::getSunriseColor(float timeOfDay) const {
    float threshold = 0.4f;
    float cosVal = std::cos(timeOfDay * 3.14159265f * 2.0f);

    if (cosVal >= -threshold && cosVal <= threshold) {
        float var6 = (cosVal / threshold) * 0.5f + 0.5f;
        float var7 = 1.0f - (1.0f - std::sin(var6 * 3.14159265f)) * 0.99f;
        var7 *= var7;

        float* color = new float[4];
        color[0] = var6 * 0.3f + 0.7f;
        color[1] = var6 * var6 * 0.7f + 0.2f;
        color[2] = var6 * var6 * 0.0f + 0.2f;
        color[3] = var7;
        return color;
    }
    return nullptr;
}

float LevelRenderer::getStarBrightness(float timeOfDay) const {
    // Java formula: brightness = 1.0 - (cos * 2.0 + 0.75), then squared * 0.5
    float cosVal = std::cos(timeOfDay * 3.14159265f * 2.0f);
    float brightness = 1.0f - (cosVal * 2.0f + 0.75f);
    brightness = std::max(0.0f, std::min(1.0f, brightness));
    return brightness * brightness * 0.5f;
}

void LevelRenderer::getCloudColor(float partialTick, float& r, float& g, float& b) const {
    // Match Java Level.getCloudColor() (Level.java:1179-1197)
    float timeOfDay = getTimeOfDay();
    float brightness = std::cos(timeOfDay * 3.14159265f * 2.0f) * 2.0f + 0.5f;
    brightness = std::max(0.0f, std::min(1.0f, brightness));

    // Base cloud color (white-ish) with time-based modulation
    r = 1.0f * (brightness * 0.9f + 0.1f);
    g = 1.0f * (brightness * 0.9f + 0.1f);
    b = 1.0f * (brightness * 0.85f + 0.15f);
}

void LevelRenderer::renderClouds(float partialTick) {
    if (!minecraft || !minecraft->player) return;

    if (minecraft->options.fancyGraphics) {
        renderAdvancedClouds(partialTick);
    } else {
        renderFastClouds(partialTick);
    }
}

void LevelRenderer::renderFastClouds(float partialTick) {
    if (!minecraft || !minecraft->player) return;

    LocalPlayer* player = minecraft->player;

    double playerX = player->prevX + (player->x - player->prevX) * partialTick;
    double playerZ = player->prevZ + (player->z - player->prevZ) * partialTick;

    int s = 32;
    int d = 256 / s;

    auto& device = RenderDevice::get();
    device.setCullFace(false);
    Textures::getInstance().bindTexture("resources/environment/clouds.png");
    device.setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);

    float timeOfDay = getTimeOfDay();
    float dayBrightness = std::cos(timeOfDay * 3.14159265f * 2.0f) * 0.5f + 0.5f;
    float cr = 0.9f * dayBrightness + 0.1f;
    float cg = 0.9f * dayBrightness + 0.1f;
    float cb = 1.0f * dayBrightness + 0.1f;

    float scale = 1.0f / 2048.0f;
    double cloudDrift = (minecraft->ticks + partialTick) * 0.03;
    float cloudY = 120.0f;

    double centerX = std::floor(playerX / s) * s;
    double centerZ = std::floor(playerZ / s) * s;

    ShaderManager::getInstance().useWorldShader();
    ShaderManager::getInstance().updateMatrices();
    ShaderManager::getInstance().setAlphaTest(0.0f);

    Tesselator& t = Tesselator::getInstance();
    t.begin(GL_QUADS);
    t.color(cr, cg, cb, 0.8f);

    for (int gx = -d; gx < d; gx++) {
        for (int gz = -d; gz < d; gz++) {
            float x0 = static_cast<float>(centerX + gx * s);
            float x1 = static_cast<float>(centerX + (gx + 1) * s);
            float z0 = static_cast<float>(centerZ + gz * s);
            float z1 = static_cast<float>(centerZ + (gz + 1) * s);

            float u0 = static_cast<float>(x0 + cloudDrift) * scale;
            float u1 = static_cast<float>(x1 + cloudDrift) * scale;
            float v0 = z0 * scale;
            float v1 = z1 * scale;

            t.tex(u0, v1); t.vertex(x0, cloudY, z1);
            t.tex(u1, v1); t.vertex(x1, cloudY, z1);
            t.tex(u1, v0); t.vertex(x1, cloudY, z0);
            t.tex(u0, v0); t.vertex(x0, cloudY, z0);
        }
    }

    t.end();

    device.setBlend(false);
    device.setCullFace(true, CullMode::Back);
}

void LevelRenderer::renderAdvancedClouds(float partialTick) {
    if (!minecraft || !minecraft->player) return;

    LocalPlayer* player = minecraft->player;

    double playerX = player->prevX + (player->x - player->prevX) * partialTick;
    double playerZ = player->prevZ + (player->z - player->prevZ) * partialTick;

    int cellSize = 8;
    int gridExtent = 32;
    float cloudTop = 120.0f;
    float cloudHeight = 4.0f;
    float cloudY = cloudTop - cloudHeight;
    float renderDist = 256.0f;

    double cloudDrift = (minecraft->ticks + partialTick) * 0.03;
    float uvScale = 1.0f / 2048.0f;

    double centerX = std::floor(playerX / cellSize) * cellSize;
    double centerZ = std::floor(playerZ / cellSize) * cellSize;
    double driftOffset = std::fmod(cloudDrift, static_cast<double>(cellSize));

    float timeOfDay = getTimeOfDay();
    float dayBrightness = std::cos(timeOfDay * 3.14159265f * 2.0f) * 0.5f + 0.5f;
    dayBrightness = std::max(0.2f, std::min(1.0f, dayBrightness));

    float cloudR = 0.9f * dayBrightness + 0.1f;
    float cloudG = 0.9f * dayBrightness + 0.1f;
    float cloudB = 1.0f * dayBrightness + 0.1f;

    float sunHeight = std::cos(timeOfDay * 3.14159265f * 2.0f);
    if (sunHeight < 0.3f && sunHeight > -0.3f) {
        float t = 1.0f - std::abs(sunHeight) / 0.3f;
        cloudR += t * 0.4f;
        cloudG += t * 0.1f;
    }

    float topBright = 1.0f;
    float bottomBright = 0.7f;
    float sideBright = 0.9f;

    Textures::getInstance().bindTexture("resources/environment/clouds.png");
    auto& device = RenderDevice::get();
    device.setCullFace(true, CullMode::Back);
#ifndef MC_RENDERER_METAL
    glFrontFace(GL_CCW);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
#endif

    ShaderManager::getInstance().useWorldShader();
    ShaderManager::getInstance().updateMatrices();

    // Two-pass rendering for proper transparency
    for (int pass = 0; pass < 2; ++pass) {
        if (pass == 0) {
#ifndef MC_RENDERER_METAL
            glColorMask(GL_FALSE, GL_FALSE, GL_FALSE, GL_FALSE);
#endif
            device.setDepthWrite(true);
            device.setBlend(false);
            ShaderManager::getInstance().setAlphaTest(0.5f);
        } else {
#ifndef MC_RENDERER_METAL
            glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
            glDepthFunc(GL_EQUAL);
#endif
            device.setDepthWrite(false);
            device.setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);
            ShaderManager::getInstance().setAlphaTest(0.5f);
        }

        Tesselator& t = Tesselator::getInstance();
        t.begin(GL_QUADS);

        for (int gx = -gridExtent; gx < gridExtent; ++gx) {
            for (int gz = -gridExtent; gz < gridExtent; ++gz) {
                float x0 = static_cast<float>(centerX + gx * cellSize - driftOffset);
                float x1 = x0 + cellSize;
                float z0 = static_cast<float>(centerZ + gz * cellSize);
                float z1 = z0 + cellSize;

                float cellCenterX = x0 + cellSize * 0.5f;
                float cellCenterZ = z0 + cellSize * 0.5f;
                float dx = cellCenterX - static_cast<float>(playerX);
                float dz = cellCenterZ - static_cast<float>(playerZ);
                float dist = std::sqrt(dx * dx + dz * dz);
                if (dist > renderDist) continue;

                float alpha = 0.8f;
                float y0 = cloudY;
                float y1 = cloudTop;

                float u0 = static_cast<float>(x0 + cloudDrift) * uvScale;
                float u1 = static_cast<float>(x1 + cloudDrift) * uvScale;
                float v0 = z0 * uvScale;
                float v1 = z1 * uvScale;

                float uCenter = (u0 + u1) * 0.5f;
                float vCenter = (v0 + v1) * 0.5f;

                // Top face
                t.color(cloudR * topBright, cloudG * topBright, cloudB * topBright, alpha);
                t.tex(u0, v1); t.vertex(x0, y1, z1);
                t.tex(u1, v1); t.vertex(x1, y1, z1);
                t.tex(u1, v0); t.vertex(x1, y1, z0);
                t.tex(u0, v0); t.vertex(x0, y1, z0);

                // Bottom face
                t.color(cloudR * bottomBright, cloudG * bottomBright, cloudB * bottomBright, alpha);
                t.tex(u0, v0); t.vertex(x0, y0, z0);
                t.tex(u1, v0); t.vertex(x1, y0, z0);
                t.tex(u1, v1); t.vertex(x1, y0, z1);
                t.tex(u0, v1); t.vertex(x0, y0, z1);

                // Side faces
                t.color(cloudR * sideBright, cloudG * sideBright, cloudB * sideBright, alpha);

                // -X face
                t.tex(uCenter, vCenter); t.vertex(x0, y0, z1);
                t.tex(uCenter, vCenter); t.vertex(x0, y1, z1);
                t.tex(uCenter, vCenter); t.vertex(x0, y1, z0);
                t.tex(uCenter, vCenter); t.vertex(x0, y0, z0);

                // +X face
                t.tex(uCenter, vCenter); t.vertex(x1, y0, z0);
                t.tex(uCenter, vCenter); t.vertex(x1, y1, z0);
                t.tex(uCenter, vCenter); t.vertex(x1, y1, z1);
                t.tex(uCenter, vCenter); t.vertex(x1, y0, z1);

                // -Z face
                t.tex(uCenter, vCenter); t.vertex(x0, y0, z0);
                t.tex(uCenter, vCenter); t.vertex(x0, y1, z0);
                t.tex(uCenter, vCenter); t.vertex(x1, y1, z0);
                t.tex(uCenter, vCenter); t.vertex(x1, y0, z0);

                // +Z face
                t.tex(uCenter, vCenter); t.vertex(x1, y0, z1);
                t.tex(uCenter, vCenter); t.vertex(x1, y1, z1);
                t.tex(uCenter, vCenter); t.vertex(x0, y1, z1);
                t.tex(uCenter, vCenter); t.vertex(x0, y0, z1);
            }
        }

        t.end();
    }

    // Restore GL state
    device.setDepthFunc(CompareFunc::LessEqual);
    device.setDepthWrite(true);
#ifndef MC_RENDERER_METAL
    glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
#endif
    device.setBlend(false);
    device.setCullFace(false);
    device.setDepthTest(true);
}

void LevelRenderer::renderEntities(float partialTick) {
    if (!level || !minecraft || !minecraft->player) return;

    LocalPlayer* player = minecraft->player;
    double camX = player->getInterpolatedX(partialTick);
    double camY = player->getInterpolatedY(partialTick) + player->eyeHeight;
    double camZ = player->getInterpolatedZ(partialTick);

    auto& device = RenderDevice::get();
    device.setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);

    ShaderManager::getInstance().useWorldShader();
    ShaderManager::getInstance().setAlphaTest(0.1f);

    Tesselator& t = Tesselator::getInstance();

    for (const auto& entity : level->entities) {
        ItemEntity* item = dynamic_cast<ItemEntity*>(entity.get());
        if (!item || item->removed) continue;

        double ix = item->prevX + (item->x - item->prevX) * partialTick;
        double iy = item->prevY + (item->y - item->prevY) * partialTick;
        double iz = item->prevZ + (item->z - item->prevZ) * partialTick;

        double dx = ix - camX;
        double dy = iy - camY;
        double dz = iz - camZ;
        if (dx * dx + dy * dy + dz * dz > 64 * 64) continue;

        float bob = std::sin((static_cast<float>(item->age) + partialTick) / 10.0f + item->bobOffset) * 0.1f + 0.1f;
        float spin = ((static_cast<float>(item->age) + partialTick) / 20.0f + item->bobOffset) * 57.29578f;

        int copies = 1;
        if (item->count > 1) copies = 2;
        if (item->count > 5) copies = 3;
        if (item->count > 20) copies = 4;

        unsigned int randomSeed = 187;

        MatrixStack::modelview().push();
        MatrixStack::modelview().translate(static_cast<float>(ix), static_cast<float>(iy + bob), static_cast<float>(iz));

        if (item->itemId > 0 && item->itemId < 256) {
            Tile* tile = Tile::tiles[item->itemId];
            if (tile && TileRenderer::canRender(static_cast<int>(tile->renderShape))) {
                Textures::getInstance().bind("resources/terrain.png");

                MatrixStack::modelview().rotate(spin, 0.0f, 1.0f, 0.0f);

                float scale = 0.25f;
                if (tile->renderShape != TileShape::CUBE) {
                    scale = 0.5f;
                }
                MatrixStack::modelview().scale(scale, scale, scale);

                ShaderManager::getInstance().updateMatrices();

                for (int c = 0; c < copies; c++) {
                    MatrixStack::modelview().push();
                    if (c > 0) {
                        float xo = ((randomSeed = randomSeed * 1103515245 + 12345) % 1000 / 500.0f - 1.0f) * 0.2f / scale;
                        float yo = ((randomSeed = randomSeed * 1103515245 + 12345) % 1000 / 500.0f - 1.0f) * 0.2f / scale;
                        float zo = ((randomSeed = randomSeed * 1103515245 + 12345) % 1000 / 500.0f - 1.0f) * 0.2f / scale;
                        MatrixStack::modelview().translate(xo, yo, zo);
                        ShaderManager::getInstance().updateMatrices();
                    }
                    t.begin(GL_QUADS);
                    tileRenderer.renderBlockItem(tile, 1.0f);
                    t.end();
                    MatrixStack::modelview().pop();
                }
            } else {
                Textures::getInstance().bind("resources/terrain.png");
                int icon = tile ? tile->getTexture(0) : 0;
                ShaderManager::getInstance().updateMatrices();
                renderDroppedItemSprite(icon, copies, player->yRot, randomSeed);
            }
        } else if (item->itemId >= 256) {
            Textures::getInstance().bind("resources/gui/items.png");
            Item* itemDef = Item::byId(item->itemId);
            int icon = itemDef ? itemDef->getIcon() : 0;
            ShaderManager::getInstance().updateMatrices();
            renderDroppedItemSprite(icon, copies, player->yRot, randomSeed);
        }

        MatrixStack::modelview().pop();
    }

    // Render chickens
    static ChickenModel chickenModel;

    for (const auto& entity : level->entities) {
        Chicken* chicken = dynamic_cast<Chicken*>(entity.get());
        if (!chicken || chicken->removed) continue;

        // Interpolate position
        double cx = chicken->prevX + (chicken->x - chicken->prevX) * partialTick;
        double cy = chicken->prevY + (chicken->y - chicken->prevY) * partialTick;
        double cz = chicken->prevZ + (chicken->z - chicken->prevZ) * partialTick;

        // Distance culling
        double dx = cx - camX;
        double dy = cy - camY;
        double dz = cz - camZ;
        if (dx * dx + dy * dy + dz * dz > 64 * 64) continue;

        // Interpolate body rotation
        float bodyRot = chicken->yBodyRotO + (chicken->yBodyRot - chicken->yBodyRotO) * partialTick;
        float headYRot = chicken->prevYRot + Mth::wrapDegrees(chicken->yRot - chicken->prevYRot) * partialTick;
        float headXRot = chicken->prevXRot + (chicken->xRot - chicken->prevXRot) * partialTick;

        // Walking animation
        float walkSpeed = chicken->walkAnimSpeedO + (chicken->walkAnimSpeed - chicken->walkAnimSpeedO) * partialTick;
        float walkPos = chicken->walkAnimPos - chicken->walkAnimSpeed * (1.0f - partialTick);
        if (walkSpeed > 1.0f) walkSpeed = 1.0f;

        // Wing flap bob - interpolate flap animation
        float flapPos = chicken->oFlap + (chicken->flap - chicken->oFlap) * partialTick;
        float flapSpd = chicken->oFlapSpeed + (chicken->flapSpeed - chicken->oFlapSpeed) * partialTick;
        float bob = (Mth::sin(flapPos) + 1.0f) * flapSpd;

        // Bind chicken texture (no mipmaps to avoid edge artifacts)
        Textures::getInstance().bind("resources/mob/chicken.png", 0, false);

        MatrixStack::modelview().push();
        MatrixStack::modelview().translate(static_cast<float>(cx), static_cast<float>(cy), static_cast<float>(cz));

        // Rotate to face body direction
        MatrixStack::modelview().rotate(180.0f - bodyRot, 0.0f, 1.0f, 0.0f);

        // Death animation - fall over on Z axis (matching Java MobRenderer.setupRotations)
        if (chicken->deathTime > 0) {
            float fall = (static_cast<float>(chicken->deathTime) + partialTick - 1.0f) / 20.0f * 1.6f;
            fall = Mth::sqrt(fall);
            if (fall > 1.0f) fall = 1.0f;
            MatrixStack::modelview().rotate(fall * 90.0f, 0.0f, 0.0f, 1.0f);
        }

        // Standard mob rendering scale
        constexpr float scale = 0.0625f;  // 1/16

        // Flip model (standard Minecraft mob rendering)
        MatrixStack::modelview().scale(-1.0f, -1.0f, 1.0f);
        MatrixStack::modelview().translate(0.0f, -24.0f * scale - 0.0078125f, 0.0f);

        ShaderManager::getInstance().updateMatrices();

        // Setup animation
        chickenModel.setupAnim(walkPos, walkSpeed, bob, headYRot - bodyRot, headXRot);

        // Disable backface culling for mob rendering (matching Java)
        device.setCullFace(false);

        chickenModel.render(t, scale);

        // Red flash overlay when hurt or dying (matching Java MobRenderer lines 64-80)
        if (chicken->hurtTime > 0 || chicken->deathTime > 0) {
            // Get brightness at entity position
            float br = level->getBrightness(
                static_cast<int>(std::floor(chicken->x)),
                static_cast<int>(std::floor(chicken->y)),
                static_cast<int>(std::floor(chicken->z))
            );

            // Enable blend for red overlay
            device.setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);
            device.setDepthFunc(CompareFunc::Equal);

            chickenModel.render(t, scale, br, 0.0f, 0.0f, 0.4f);

            // Restore state
            device.setDepthFunc(CompareFunc::LessEqual);
            device.setBlend(false);
        }

        device.setCullFace(true, CullMode::Back);
        MatrixStack::modelview().pop();
    }

    device.setBlend(false);
}

void LevelRenderer::renderDroppedItemSprite(int icon, int copies, float playerYRot, unsigned int randomSeed) {
    float u0 = static_cast<float>(icon % 16 * 16) / 256.0f;
    float u1 = (static_cast<float>(icon % 16 * 16) + 16.0f) / 256.0f;
    float v0 = static_cast<float>(icon / 16 * 16) / 256.0f;
    float v1 = (static_cast<float>(icon / 16 * 16) + 16.0f) / 256.0f;

    float r = 1.0f;
    float xo = 0.5f;
    float yo = 0.25f;

    MatrixStack::modelview().scale(0.5f, 0.5f, 0.5f);
    ShaderManager::getInstance().updateMatrices();

    Tesselator& t = Tesselator::getInstance();

    for (int c = 0; c < copies; c++) {
        MatrixStack::modelview().push();
        if (c > 0) {
            float _xo = ((randomSeed = randomSeed * 1103515245 + 12345) % 1000 / 500.0f - 1.0f) * 0.3f;
            float _yo = ((randomSeed = randomSeed * 1103515245 + 12345) % 1000 / 500.0f - 1.0f) * 0.3f;
            float _zo = ((randomSeed = randomSeed * 1103515245 + 12345) % 1000 / 500.0f - 1.0f) * 0.3f;
            MatrixStack::modelview().translate(_xo, _yo, _zo);
        }

        MatrixStack::modelview().rotate(180.0f - playerYRot, 0.0f, 1.0f, 0.0f);
        ShaderManager::getInstance().updateMatrices();

        t.begin(GL_QUADS);
        t.color(1.0f, 1.0f, 1.0f, 1.0f);
        t.normal(0.0f, 1.0f, 0.0f);
        t.tex(u0, v1); t.vertex(0.0f - xo, 0.0f - yo, 0.0f);
        t.tex(u1, v1); t.vertex(r - xo, 0.0f - yo, 0.0f);
        t.tex(u1, v0); t.vertex(r - xo, 1.0f - yo, 0.0f);
        t.tex(u0, v0); t.vertex(0.0f - xo, 1.0f - yo, 0.0f);
        t.end();

        MatrixStack::modelview().pop();
    }
}

} // namespace mc
