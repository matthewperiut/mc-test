#include "renderer/LevelRenderer.hpp"
#include "renderer/ChunkMeshBuilder.hpp"
#include "core/Minecraft.hpp"
#include "renderer/Tesselator.hpp"
#include "renderer/Textures.hpp"
#include "renderer/MatrixStack.hpp"
#include "renderer/ShaderManager.hpp"
#include "renderer/model/ChickenModel.hpp"
#include "entity/Entity.hpp"
#include "entity/ItemEntity.hpp"
#include "entity/LocalPlayer.hpp"
#include "entity/Chicken.hpp"
#include "world/tile/Tile.hpp"
#include "item/Item.hpp"
#include "util/Mth.hpp"
#include "renderer/backend/RenderDevice.hpp"
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
    , meshBuilder(std::make_unique<ChunkMeshBuilder>())
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
    // Sky geometry is now rendered via Tesselator, no VAOs to dispose
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

    // Process completed chunks from async builder (GPU upload on main thread)
    auto completed = meshBuilder->getCompletedChunks();
    for (Chunk* chunk : completed) {
        if (chunk->uploadPendingMesh()) {
            chunksUpdated++;
        }
    }

    // Queue dirty chunks for async rebuilding (up to a limit)
    int maxQueue = firstRebuild ? 256 : 16;
    int queued = 0;

    for (Chunk* chunk : dirtyChunks) {
        if (queued >= maxQueue) break;
        // Only queue if not already being built asynchronously
        if (!chunk->isBuilding()) {
            chunk->setBuilding(true);
            // Queue chunk with distance-based priority (closer = higher priority)
            meshBuilder->queueChunk(chunk, level, static_cast<int>(chunk->distanceSq));
            queued++;
        }
    }

    // Update firstRebuild flag when initial load completes
    if (firstRebuild) {
        size_t pendingCount = meshBuilder->getPendingCount();
        if (dirtyChunks.empty() && pendingCount == 0 && completed.empty()) {
            firstRebuild = false;
        }
    }
}

void LevelRenderer::render(float /*partialTick*/, int pass) {
    chunksRendered = 0;

    // Pass 0 (solid): iterate front-to-back to maximize early-Z rejection
    // Pass 1 (water/transparent): iterate back-to-front for correct alpha blending
    if (pass == 0) {
        // visibleChunks is sorted back-to-front, so iterate in reverse for front-to-back
        for (auto it = visibleChunks.rbegin(); it != visibleChunks.rend(); ++it) {
            Chunk* chunk = *it;
            if (chunk->loaded) {
                chunk->render(pass);
                chunksRendered++;
            }
        }
    } else {
        // Back-to-front for transparent pass
        for (Chunk* chunk : visibleChunks) {
            if (chunk->loaded) {
                chunk->render(pass);
            }
        }
    }
}

void LevelRenderer::initSkyVAOs() {
    if (skyVAOsInitialized) return;

    // Generate star data for later rendering via Tesselator
    buildStarData();

    skyVAOsInitialized = true;
}

void LevelRenderer::buildStarData() {
    starVertices.clear();

    // Generate deterministic stars matching Java (seed 10842)
    std::srand(10842);

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
                starVertices.push_back(xp + xo);
                starVertices.push_back(yp + __yo);
                starVertices.push_back(zp + zo2);
            }
        }
    }
}

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
    t.begin(DrawMode::Quads);
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

    auto sunriseColor = getSunriseColor(timeOfDay);
    if (sunriseColor[3] > 0.0f) {  // Check if alpha is non-zero (sunrise/sunset active)
        MatrixStack::modelview().push();
        MatrixStack::modelview().rotate(90.0f, 1.0f, 0.0f, 0.0f);
        MatrixStack::modelview().rotate(timeOfDay > 0.5f ? 180.0f : 0.0f, 0.0f, 0.0f, 1.0f);

        ShaderManager::getInstance().updateMatrices();
        ShaderManager::getInstance().setUseTexture(false);
        ShaderManager::getInstance().setUseVertexColor(true);  // Use per-vertex colors for gradient

        Tesselator& t = Tesselator::getInstance();
        t.begin(DrawMode::TriangleFan);
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
        t.begin(DrawMode::Quads);
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
        t.begin(DrawMode::Quads);
        t.color(1.0f, 1.0f, 1.0f, 1.0f);
        t.tex(1.0f, 1.0f); t.vertex(-ss, -100.0f, ss);
        t.tex(0.0f, 1.0f); t.vertex(ss, -100.0f, ss);
        t.tex(0.0f, 0.0f); t.vertex(ss, -100.0f, -ss);
        t.tex(1.0f, 0.0f); t.vertex(-ss, -100.0f, -ss);
        t.end();
    }

    // Render stars at night using Tesselator
    float starBrightness = getStarBrightness(timeOfDay);
    if (starBrightness > 0.0f && !starVertices.empty()) {
        ShaderManager::getInstance().setUseTexture(false);
        ShaderManager::getInstance().setSkyColor(starBrightness, starBrightness, starBrightness, starBrightness);

        // Render stars as quads via Tesselator
        t.begin(DrawMode::Quads);
        t.color(starBrightness, starBrightness, starBrightness, starBrightness);
        for (size_t i = 0; i + 11 < starVertices.size(); i += 12) {
            // 4 vertices per quad, 3 floats per vertex
            t.vertex(starVertices[i], starVertices[i+1], starVertices[i+2]);
            t.vertex(starVertices[i+3], starVertices[i+4], starVertices[i+5]);
            t.vertex(starVertices[i+6], starVertices[i+7], starVertices[i+8]);
            t.vertex(starVertices[i+9], starVertices[i+10], starVertices[i+11]);
        }
        t.end();
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
    t.begin(DrawMode::Quads);
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

std::array<float, 4> LevelRenderer::getSunriseColor(float timeOfDay) const {
    float threshold = 0.4f;
    float cosVal = std::cos(timeOfDay * 3.14159265f * 2.0f);

    if (cosVal >= -threshold && cosVal <= threshold) {
        float var6 = (cosVal / threshold) * 0.5f + 0.5f;
        float var7 = 1.0f - (1.0f - std::sin(var6 * 3.14159265f)) * 0.99f;
        var7 *= var7;

        return {
            var6 * 0.3f + 0.7f,
            var6 * var6 * 0.7f + 0.2f,
            var6 * var6 * 0.0f + 0.2f,
            var7
        };
    }
    return {0.0f, 0.0f, 0.0f, 0.0f};
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
    t.begin(DrawMode::Quads);
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

    // Bind cloud texture with NEAREST filtering (no mipmaps)
    Textures::getInstance().bind("resources/environment/clouds.png", 0, false);
    auto& device = RenderDevice::get();
    device.setCullFace(true, CullMode::Back);
    device.setFrontFace(FrontFace::CounterClockwise);

    ShaderManager::getInstance().useWorldShader();
    ShaderManager::getInstance().updateMatrices();

    // Two-pass rendering for proper transparency
    for (int pass = 0; pass < 2; ++pass) {
        if (pass == 0) {
            device.setColorMask(false, false, false, false);
            device.setDepthWrite(true);
            device.setBlend(false);
            ShaderManager::getInstance().setAlphaTest(0.5f);
        } else {
            device.setColorMask(true, true, true, true);
            device.setDepthFunc(CompareFunc::Equal);
            device.setDepthWrite(false);
            device.setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);
            ShaderManager::getInstance().setAlphaTest(0.5f);
        }

        Tesselator& t = Tesselator::getInstance();
        t.begin(DrawMode::Quads);

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
    device.setColorMask(true, true, true, true);
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
    Tesselator& t = Tesselator::getInstance();

    // === SHADOW PASS ===
    // Render entity shadows before the entities themselves
    device.setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);
    device.setDepthWrite(false);  // Don't write to depth buffer for shadows

    ShaderManager::getInstance().useWorldShader();
    ShaderManager::getInstance().setAlphaTest(0.0f);  // No alpha test for shadows

    // Bind shadow texture with clamping (prevents texture repeat at edges)
    Textures::getInstance().bind("resources/misc/shadow.png", 0, false, true);

    // Use identity modelview (camera is already set up in projection)
    ShaderManager::getInstance().updateMatrices();

    t.begin(DrawMode::Quads);

    for (const auto& entity : level->entities) {
        if (entity->removed) continue;
        if (entity->getShadowRadius() <= 0.0f) continue;

        // Calculate distance-based shadow power (fades over 256 blocks)
        double distSqr = entity->distanceToSqr(camX, camY - player->eyeHeight, camZ);
        float power = static_cast<float>((1.0 - distSqr / (256.0 * 256.0)) * entity->getShadowStrength());

        if (power > 0.0f) {
            // Get interpolated position for smooth shadow movement
            double shadowX = entity->prevX + (entity->x - entity->prevX) * partialTick;
            double shadowY = entity->prevY + (entity->y - entity->prevY) * partialTick;
            double shadowZ = entity->prevZ + (entity->z - entity->prevZ) * partialTick;

            // For items being picked up, override with the animated position
            ItemEntity* item = dynamic_cast<ItemEntity*>(entity.get());
            if (item && item->beingPickedUp) {
                item->getPickupAnimatedPos(partialTick, shadowX, shadowY, shadowZ);
            }

            renderEntityShadow(entity.get(), shadowX, shadowY, shadowZ, power, partialTick);
        }
    }

    t.end();

    device.setDepthWrite(true);  // Re-enable depth writing

    // === ENTITY PASS ===
    device.setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);

    ShaderManager::getInstance().useWorldShader();
    ShaderManager::getInstance().setAlphaTest(0.1f);

    for (const auto& entity : level->entities) {
        ItemEntity* item = dynamic_cast<ItemEntity*>(entity.get());
        if (!item || item->removed) continue;

        // Get interpolated position (uses pickup animation if active)
        double ix, iy, iz;
        item->getPickupAnimatedPos(partialTick, ix, iy, iz);

        double dx = ix - camX;
        double dy = iy - camY;
        double dz = iz - camZ;
        if (dx * dx + dy * dy + dz * dz > 64 * 64) continue;

        // Bobbing animation (disabled during pickup)
        float bob = 0.0f;
        float spin = 0.0f;
        if (!item->beingPickedUp) {
            bob = std::sin((static_cast<float>(item->age) + partialTick) / 10.0f + item->bobOffset) * 0.1f + 0.1f;
            spin = ((static_cast<float>(item->age) + partialTick) / 20.0f + item->bobOffset) * 57.29578f;
        }

        int copies = 1;
        if (item->count > 1) copies = 2;
        if (item->count > 5) copies = 3;
        if (item->count > 20) copies = 4;

        unsigned int randomSeed = 187;

        // Height offset to prevent clipping into ground (Java: heightOffset = bbHeight / 2.0 = 0.125)
        float heightOffset = 0.125f;

        // Get light level at entity position for proper world lighting
        int lightBlockX = static_cast<int>(std::floor(ix));
        int lightBlockY = static_cast<int>(std::floor(iy));
        int lightBlockZ = static_cast<int>(std::floor(iz));
        int entitySkyLight = level->getSkyLight(lightBlockX, lightBlockY, lightBlockZ);
        int entityBlockLight = level->getBlockLight(lightBlockX, lightBlockY, lightBlockZ);

        MatrixStack::modelview().push();
        MatrixStack::modelview().translate(static_cast<float>(ix), static_cast<float>(iy + bob + heightOffset), static_cast<float>(iz));

        if (item->itemId > 0 && item->itemId < 256) {
            Tile* tile = Tile::tiles[item->itemId].get();
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
                    t.begin(DrawMode::Quads);
                    t.lightLevel(entitySkyLight, entityBlockLight);
                    tileRenderer.renderBlockItem(tile, 1.0f);
                    t.end();
                    MatrixStack::modelview().pop();
                }
            } else {
                Textures::getInstance().bind("resources/terrain.png");
                int icon = tile ? tile->getTexture(0) : 0;
                ShaderManager::getInstance().updateMatrices();
                renderDroppedItemSprite(icon, copies, player->yRot, randomSeed, entitySkyLight, entityBlockLight);
            }
        } else if (item->itemId >= 256) {
            Textures::getInstance().bind("resources/gui/items.png");
            Item* itemDef = Item::byId(item->itemId);
            int icon = itemDef ? itemDef->getIcon() : 0;
            ShaderManager::getInstance().updateMatrices();
            renderDroppedItemSprite(icon, copies, player->yRot, randomSeed, entitySkyLight, entityBlockLight);
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

        // Get light level at entity position for proper world lighting
        int chickenBlockX = static_cast<int>(std::floor(cx));
        int chickenBlockY = static_cast<int>(std::floor(cy));
        int chickenBlockZ = static_cast<int>(std::floor(cz));
        int chickenSkyLight = level->getSkyLight(chickenBlockX, chickenBlockY, chickenBlockZ);
        int chickenBlockLight = level->getBlockLight(chickenBlockX, chickenBlockY, chickenBlockZ);

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

        chickenModel.render(t, scale, chickenSkyLight, chickenBlockLight);

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

            chickenModel.render(t, scale, br, 0.0f, 0.0f, 0.4f, chickenSkyLight, chickenBlockLight);

            // Restore state
            device.setDepthFunc(CompareFunc::LessEqual);
            device.setBlend(false);
        }

        device.setCullFace(true, CullMode::Back);
        MatrixStack::modelview().pop();
    }

    device.setBlend(false);
}

void LevelRenderer::renderDroppedItemSprite(int icon, int copies, float playerYRot, unsigned int randomSeed, int skyLight, int blockLight) {
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

        t.begin(DrawMode::Quads);
        t.lightLevel(skyLight, blockLight);
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

void LevelRenderer::renderEntityShadow(Entity* entity, double x, double y, double z, float power, float partialTick) {
    if (!entity || !level) return;

    float r = entity->getShadowRadius();
    if (r <= 0.0f) return;

    // Use the passed position (which may be animated) plus height offset
    double ex = x;
    double ey = y + entity->getShadowHeightOffset();
    double ez = z;

    // Define search bounds around entity
    int x0 = Mth::floor(ex - r);
    int x1 = Mth::floor(ex + r);
    int z0 = Mth::floor(ez - r);
    int z1 = Mth::floor(ez + r);

    // For Y, search further down to allow shadow to fade over distance
    // Shadow fades based on vertical distance, so search up to ~2 blocks below entity
    float shadowDropDistance = 2.0f;
    int y0 = Mth::floor(ey - shadowDropDistance);
    int y1 = Mth::floor(ey);

    // Iterate through all blocks in the search area
    for (int xt = x0; xt <= x1; ++xt) {
        for (int yt = y0; yt <= y1; ++yt) {
            for (int zt = z0; zt <= z1; ++zt) {
                int tileId = level->getTile(xt, yt - 1, zt);  // Get block below this position
                if (tileId > 0 && level->getSkyLight(xt, yt, zt) > 3) {  // Only if block exists & lit
                    Tile* tile = Tile::tiles[tileId].get();
                    if (tile) {
                        // Pass yt - 1 as the tile Y position (where the actual block is)
                        renderTileShadow(tile, ex, ey, ez, xt, yt - 1, zt, power, r);
                    }
                }
            }
        }
    }
}

void LevelRenderer::renderTileShadow(Tile* tile, double entityX, double entityY, double entityZ,
                                      int tileX, int tileY, int tileZ, float power, float radius) {
    if (!tile || !tile->isFullCube()) return;

    Tesselator& t = Tesselator::getInstance();

    // Get collision box for bounds (most tiles are 0-1 range)
    AABB box = tile->getCollisionBox(tileX, tileY, tileZ);

    // Calculate shadow opacity based on distance from entity center
    double verticalDist = entityY - box.y1;
    if (verticalDist < 0.0) return;  // Block is above entity, skip

    // Smoother fade over distance - shadow fades linearly over shadowDropDistance (2 blocks)
    // At distance 0: full shadow, at distance 2: no shadow
    float distanceFade = 1.0f - static_cast<float>(verticalDist / 2.0);
    if (distanceFade <= 0.0f) return;

    // Check brightness at space above the block (tileY + 1) where the shadow is cast
    float brightness = level->getBrightness(tileX, tileY + 1, tileZ);
    float alpha = power * distanceFade * 0.5f * brightness;

    if (alpha < 0.0f) return;
    if (alpha > 1.0f) alpha = 1.0f;

    // Get block bounds from collision box
    double x0 = box.x0;
    double x1 = box.x1;
    double y0 = box.y1 + 0.015625;  // Slight offset to prevent z-fighting
    double z0 = box.z0;
    double z1 = box.z1;

    // Calculate UV coordinates based on entity position and shadow radius
    float u0 = static_cast<float>((entityX - x0) / 2.0 / radius + 0.5);
    float u1 = static_cast<float>((entityX - x1) / 2.0 / radius + 0.5);
    float v0 = static_cast<float>((entityZ - z0) / 2.0 / radius + 0.5);
    float v1 = static_cast<float>((entityZ - z1) / 2.0 / radius + 0.5);

    // Render shadow quad on top of block
    t.color(1.0f, 1.0f, 1.0f, alpha);
    t.tex(u0, v0); t.vertex(static_cast<float>(x0), static_cast<float>(y0), static_cast<float>(z0));
    t.tex(u0, v1); t.vertex(static_cast<float>(x0), static_cast<float>(y0), static_cast<float>(z1));
    t.tex(u1, v1); t.vertex(static_cast<float>(x1), static_cast<float>(y0), static_cast<float>(z1));
    t.tex(u1, v0); t.vertex(static_cast<float>(x1), static_cast<float>(y0), static_cast<float>(z0));
}

} // namespace mc
