#include "renderer/GameRenderer.hpp"
#include "core/Minecraft.hpp"
#include "renderer/LevelRenderer.hpp"
#include "renderer/Tesselator.hpp"
#include "renderer/Textures.hpp"
#include "renderer/MatrixStack.hpp"
#include "renderer/ShaderManager.hpp"
#include "entity/LocalPlayer.hpp"
#include "world/Level.hpp"
#include "world/tile/Tile.hpp"
#include "gamemode/GameMode.hpp"
#include "item/Inventory.hpp"
#include "item/Item.hpp"
#include "util/Mth.hpp"
#include "renderer/backend/RenderDevice.hpp"
#include <cstdio>
#include <algorithm>
#include <cmath>

namespace mc {

GameRenderer::GameRenderer(Minecraft* minecraft)
    : minecraft(minecraft)
    , cameraPitch(0), cameraYaw(0)
    , fogRed(0.5f), fogGreen(0.8f), fogBlue(1.0f)
    , fogStart(20.0f), fogEnd(80.0f)
    , bobbing(0), tilt(0)
    , height(0.0f), oHeight(0.0f)
    , lastSlot(-1)
    , selectedItem()
    , screenWidth(854), screenHeight(480)
{
}

void GameRenderer::resize(int w, int h) {
    screenWidth = w;
    screenHeight = h;
    RenderDevice::get().setViewport(0, 0, w, h);
}

void GameRenderer::render(float partialTick) {
    auto& device = RenderDevice::get();

    // Clear screen
    device.setClearColor(fogRed, fogGreen, fogBlue, 1.0f);
    device.clear(true, true);

    // Setup camera and render world
    setupCamera(partialTick);
    setupFog();

    // Pick (raycast)
    pick(partialTick);

    // Render world
    renderWorld(partialTick);

    // Render hand
    renderHand(partialTick);

    // Render GUI (HUD, crosshair, etc.)
    renderGui(partialTick);
}

void GameRenderer::setupCamera(float partialTick) {
    float fov = 70.0f;
    float nearPlane = 0.05f;
    float farPlane = 256.0f;

    setupProjection(fov, nearPlane, farPlane);

    MatrixStack::modelview().loadIdentity();

    orientCamera(partialTick);
}

void GameRenderer::setupProjection(float fov, float nearPlane, float farPlane) {
    float aspect = static_cast<float>(screenWidth) / static_cast<float>(screenHeight);
    MatrixStack::projection().loadIdentity();
    MatrixStack::projection().perspective(fov, aspect, nearPlane, farPlane);
}

void GameRenderer::orientCamera(float partialTick) {
    LocalPlayer* player = minecraft->player;
    if (!player) return;

    // Apply view bobbing
    if (minecraft->options.viewBobbing) {
        applyBobbing(partialTick);
    }

    // Apply camera rotation
    float pitch = player->getInterpolatedXRot(partialTick);
    float yaw = player->getInterpolatedYRot(partialTick);

    MatrixStack::modelview().rotate(pitch, 1.0f, 0.0f, 0.0f);
    MatrixStack::modelview().rotate(yaw + 180.0f, 0.0f, 1.0f, 0.0f);

    // Apply camera position (at eye level)
    double eyeX = player->getInterpolatedX(partialTick);
    double eyeY = player->getInterpolatedY(partialTick) + player->eyeHeight - player->getInterpolatedYSlideOffset(partialTick);
    double eyeZ = player->getInterpolatedZ(partialTick);

    MatrixStack::modelview().translate(static_cast<float>(-eyeX), static_cast<float>(-eyeY), static_cast<float>(-eyeZ));
}

void GameRenderer::applyBobbing(float partialTick) {
    LocalPlayer* player = minecraft->player;
    if (!player) return;

    float bob = player->getBobbing(partialTick);
    float tiltVal = player->getTilt(partialTick);

    // Apply bobbing
    float bobAngle = bob * Mth::PI;
    MatrixStack::modelview().translate(
        Mth::sin(bobAngle) * bob * 0.5f,
        -std::abs(Mth::cos(bobAngle) * bob),
        0.0f
    );
    MatrixStack::modelview().rotate(Mth::sin(bobAngle) * bob * 3.0f, 0.0f, 0.0f, 1.0f);
    MatrixStack::modelview().rotate(std::abs(Mth::cos(bobAngle - 0.2f) * bob) * 5.0f, 1.0f, 0.0f, 0.0f);

    // Apply tilt (for damage effects)
    MatrixStack::modelview().rotate(tiltVal, 0.0f, 0.0f, 1.0f);
}

void GameRenderer::setupFog() {
    // Calculate fog color based on time of day
    if (minecraft && minecraft->level) {
        float timeOfDay = minecraft->level->getTimeOfDay();
        float brightness = std::cos(timeOfDay * 3.14159265f * 2.0f) * 2.0f + 0.5f;
        brightness = std::max(0.0f, std::min(1.0f, brightness));

        fogRed = 0.7529412f * (brightness * 0.94f + 0.06f);
        fogGreen = 0.84705883f * (brightness * 0.94f + 0.06f);
        fogBlue = 1.0f * (brightness * 0.91f + 0.09f);
    }

    // Calculate fog distance based on render distance
    int renderDist = minecraft->options.renderDistance;
    float distMultiplier = 1.0f - renderDist * 0.2f;

    fogStart = 20.0f * distMultiplier;
    fogEnd = 80.0f * distMultiplier;

    // Update shader fog uniforms
    ShaderManager::getInstance().updateFog(fogStart, fogEnd, fogRed, fogGreen, fogBlue);
}

void GameRenderer::pick(float partialTick) {
    LocalPlayer* player = minecraft->player;
    Level* level = minecraft->level.get();

    if (!player || !level) {
        hitResult = HitResult();
        return;
    }

    // Get pick range from game mode
    double pickRange = minecraft->gameMode ? minecraft->gameMode->getPickRange() : 5.0f;

    // Start with block raycasting
    Vec3 eyePos(
        player->getInterpolatedX(partialTick),
        player->getInterpolatedY(partialTick) + player->eyeHeight - player->getInterpolatedYSlideOffset(partialTick),
        player->getInterpolatedZ(partialTick)
    );
    Vec3 look = player->getLookVector();
    Vec3 endPos = eyePos.add(look.x * pickRange, look.y * pickRange, look.z * pickRange);

    hitResult = level->clip(eyePos, endPos);

    // Calculate entity pick distance (capped at 3.0 for non-creative, or block hit distance)
    double entityPickRange = pickRange;
    if (hitResult.isTile()) {
        entityPickRange = hitResult.location.distanceTo(eyePos);
    }
    if (entityPickRange > 3.0) {
        entityPickRange = 3.0;
    }

    // Entity raycasting - matching Java GameRenderer.pick()
    Entity* hoveredEntity = nullptr;
    double closestDist = 0.0;

    // Get entities in expanded bounding box along view ray
    Vec3 entityEnd = eyePos.add(look.x * entityPickRange, look.y * entityPickRange, look.z * entityPickRange);
    float growAmount = 1.0f;
    AABB searchBox = player->bb.expand(look.x * entityPickRange, look.y * entityPickRange, look.z * entityPickRange)
                               .grow(growAmount);

    std::vector<Entity*> entities = level->getEntitiesInArea(searchBox);

    for (Entity* entity : entities) {
        if (entity == player) continue;  // Don't pick self
        if (!entity->isPickable()) continue;

        // Grow entity's AABB by its pick radius
        float pickRadius = entity->getPickRadius();
        AABB entityBox = entity->bb.grow(pickRadius);

        // Test ray-AABB intersection
        std::optional<Vec3> hitPoint = entityBox.clip(eyePos, entityEnd);

        if (entityBox.contains(eyePos.x, eyePos.y, eyePos.z)) {
            // Player is inside entity's box - entity is closest
            if (closestDist == 0.0) {
                hoveredEntity = entity;
                closestDist = 0.0;
            }
        } else if (hitPoint.has_value()) {
            double dist = eyePos.distanceTo(hitPoint.value());
            if (dist < closestDist || closestDist == 0.0) {
                hoveredEntity = entity;
                closestDist = dist;
            }
        }
    }

    // If we hit an entity and it's closer than the block, use entity hit
    if (hoveredEntity != nullptr) {
        hitResult = HitResult(hoveredEntity);
    }
}

void GameRenderer::renderWorld(float partialTick) {
    LevelRenderer* levelRenderer = minecraft->levelRenderer.get();
    if (!levelRenderer) return;

    LocalPlayer* player = minecraft->player;
    if (!player) return;

    // Update visible chunks based on camera position
    double camX = player->getInterpolatedX(partialTick);
    double camY = player->getInterpolatedY(partialTick) + player->eyeHeight;
    double camZ = player->getInterpolatedZ(partialTick);

    levelRenderer->updateVisibleChunks(camX, camY, camZ);
    levelRenderer->updateDirtyChunks();

    // Bind terrain texture
    Textures::getInstance().bind("resources/terrain.png");

    // Enable required states
    auto& device = RenderDevice::get();
    device.setDepthTest(true);
    device.setCullFace(true, CullMode::Back);

    // Use world shader with fog and alpha test
    ShaderManager::getInstance().useWorldShader();
    ShaderManager::getInstance().setAlphaTest(0.5f);
    ShaderManager::getInstance().updateMatrices();

    // Set sky brightness for dynamic terrain lighting (varies with time of day)
    float skyBrightness = 1.0f;
    if (minecraft->level) {
        skyBrightness = minecraft->level->getSkyBrightness();
    }
    ShaderManager::getInstance().setSkyBrightness(skyBrightness);

    // Render sky
    levelRenderer->renderSky(partialTick);

    // Re-bind terrain texture after sky rendering
    Textures::getInstance().bind("resources/terrain.png");
    ShaderManager::getInstance().useWorldShader();
    ShaderManager::getInstance().setAlphaTest(0.5f);  // Reset alpha test (sky sets it to 0)
    ShaderManager::getInstance().updateMatrices();
    ShaderManager::getInstance().setSkyBrightness(skyBrightness);  // Re-set after shader switch

    // Render opaque geometry (pass 0)
    levelRenderer->render(partialTick, 0);

    // Render cutout geometry (pass 1) - torches, flowers, etc. with alpha testing
    // Note: cross-shaped plants render both front and back faces in TileRenderer
    levelRenderer->render(partialTick, 1);

    // Render selection outline
    if (hitResult.isTile()) {
        renderHitOutline();

        // Render block breaking animation
        if (minecraft->gameMode) {
            float destroyProgress = minecraft->gameMode->getDestroyProgress();
            if (destroyProgress > 0.0f) {
                renderBreakingAnimation(destroyProgress);
            }
        }
    }

    // Render transparent geometry - water (pass 2)
    ShaderManager::getInstance().setAlphaTest(0.0f);
    ShaderManager::getInstance().setSkyBrightness(skyBrightness);  // Ensure sky brightness is set
    device.setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);
    levelRenderer->render(partialTick, 2);
    device.setBlend(false);

    // Render dropped items and entities
    levelRenderer->renderEntities(partialTick);

    // Render entity hitboxes (F3+B)
    renderEntityHitboxes(partialTick);

    // Render particles
    levelRenderer->renderParticles(partialTick);

    // Render clouds
    levelRenderer->renderClouds(partialTick);
}

void GameRenderer::renderBreakingAnimation(float progress) {
    if (!hitResult.isTile()) return;

    int breakStage = static_cast<int>(progress * 10.0f);
    if (breakStage > 9) breakStage = 9;
    int textureIndex = 240 + breakStage;

    float texU = static_cast<float>(textureIndex % 16) / 16.0f;
    float texV = static_cast<float>(textureIndex / 16) / 16.0f;
    float texSize = 1.0f / 16.0f;

    int bx = hitResult.x;
    int by = hitResult.y;
    int bz = hitResult.z;

    float e = 0.002f;

    // Get the tile's selection box for the breaking animation
    float x0, y0, z0, x1, y1, z1;
    Tile* tile = minecraft->level ? minecraft->level->getTileAt(bx, by, bz) : nullptr;
    if (tile) {
        AABB selBox = tile->getSelectionBox(minecraft->level.get(), bx, by, bz);
        x0 = static_cast<float>(selBox.x0);
        y0 = static_cast<float>(selBox.y0);
        z0 = static_cast<float>(selBox.z0);
        x1 = static_cast<float>(selBox.x1);
        y1 = static_cast<float>(selBox.y1);
        z1 = static_cast<float>(selBox.z1);
    } else {
        // Fallback to full block
        x0 = static_cast<float>(bx);
        y0 = static_cast<float>(by);
        z0 = static_cast<float>(bz);
        x1 = static_cast<float>(bx) + 1.0f;
        y1 = static_cast<float>(by) + 1.0f;
        z1 = static_cast<float>(bz) + 1.0f;
    }

    Textures::getInstance().bind("resources/terrain.png");
    auto& device = RenderDevice::get();
    device.setBlend(true, BlendFactor::DstColor, BlendFactor::SrcColor);
    device.setCullFace(false);
    device.setDepthWrite(false);

    device.setPolygonOffset(true, -3.0f, -3.0f);

    ShaderManager::getInstance().useWorldShader();
    ShaderManager::getInstance().setAlphaTest(0.0f);
    ShaderManager::getInstance().updateMatrices();

    Tesselator& t = Tesselator::getInstance();

    // Bottom face
    t.begin(DrawMode::Quads);
    t.color(1.0f, 1.0f, 1.0f, 1.0f);
    t.tex(texU, texV + texSize); t.vertex(x0 - e, y0 - e, z1 + e);
    t.tex(texU + texSize, texV + texSize); t.vertex(x1 + e, y0 - e, z1 + e);
    t.tex(texU + texSize, texV); t.vertex(x1 + e, y0 - e, z0 - e);
    t.tex(texU, texV); t.vertex(x0 - e, y0 - e, z0 - e);
    t.end();

    // Top face
    t.begin(DrawMode::Quads);
    t.color(1.0f, 1.0f, 1.0f, 1.0f);
    t.tex(texU, texV); t.vertex(x0 - e, y1 + e, z0 - e);
    t.tex(texU + texSize, texV); t.vertex(x1 + e, y1 + e, z0 - e);
    t.tex(texU + texSize, texV + texSize); t.vertex(x1 + e, y1 + e, z1 + e);
    t.tex(texU, texV + texSize); t.vertex(x0 - e, y1 + e, z1 + e);
    t.end();

    // North face
    t.begin(DrawMode::Quads);
    t.color(1.0f, 1.0f, 1.0f, 1.0f);
    t.tex(texU + texSize, texV); t.vertex(x0 - e, y1 + e, z0 - e);
    t.tex(texU, texV); t.vertex(x1 + e, y1 + e, z0 - e);
    t.tex(texU, texV + texSize); t.vertex(x1 + e, y0 - e, z0 - e);
    t.tex(texU + texSize, texV + texSize); t.vertex(x0 - e, y0 - e, z0 - e);
    t.end();

    // South face
    t.begin(DrawMode::Quads);
    t.color(1.0f, 1.0f, 1.0f, 1.0f);
    t.tex(texU, texV); t.vertex(x0 - e, y1 + e, z1 + e);
    t.tex(texU, texV + texSize); t.vertex(x0 - e, y0 - e, z1 + e);
    t.tex(texU + texSize, texV + texSize); t.vertex(x1 + e, y0 - e, z1 + e);
    t.tex(texU + texSize, texV); t.vertex(x1 + e, y1 + e, z1 + e);
    t.end();

    // West face
    t.begin(DrawMode::Quads);
    t.color(1.0f, 1.0f, 1.0f, 1.0f);
    t.tex(texU + texSize, texV); t.vertex(x0 - e, y1 + e, z1 + e);
    t.tex(texU, texV); t.vertex(x0 - e, y1 + e, z0 - e);
    t.tex(texU, texV + texSize); t.vertex(x0 - e, y0 - e, z0 - e);
    t.tex(texU + texSize, texV + texSize); t.vertex(x0 - e, y0 - e, z1 + e);
    t.end();

    // East face
    t.begin(DrawMode::Quads);
    t.color(1.0f, 1.0f, 1.0f, 1.0f);
    t.tex(texU, texV); t.vertex(x1 + e, y1 + e, z0 - e);
    t.tex(texU + texSize, texV); t.vertex(x1 + e, y1 + e, z1 + e);
    t.tex(texU + texSize, texV + texSize); t.vertex(x1 + e, y0 - e, z1 + e);
    t.tex(texU, texV + texSize); t.vertex(x1 + e, y0 - e, z0 - e);
    t.end();

    device.setPolygonOffset(false);
    device.setDepthWrite(true);
    device.setCullFace(true, CullMode::Back);
    device.setBlend(false);
    ShaderManager::getInstance().setAlphaTest(0.5f);
}

// Helper to generate a camera-facing quad for a line segment with screen-space pixel thickness
// ox, oy, oz: outward offset direction (should be normalized), scaled by 'outwardDist'
static void drawLineAsQuad(Tesselator& t,
                           float x0, float y0, float z0,
                           float x1, float y1, float z1,
                           float camX, float camY, float camZ,
                           float pixelWidth, float screenHeight, float fovRadians,
                           float ox, float oy, float oz, float outwardDist) {
    // Apply outward offset to push the line away from block center
    x0 += ox * outwardDist;
    y0 += oy * outwardDist;
    z0 += oz * outwardDist;
    x1 += ox * outwardDist;
    y1 += oy * outwardDist;
    z1 += oz * outwardDist;

    // Line direction
    float dirX = x1 - x0;
    float dirY = y1 - y0;
    float dirZ = z1 - z0;

    // Midpoint of line
    float midX = (x0 + x1) * 0.5f;
    float midY = (y0 + y1) * 0.5f;
    float midZ = (z0 + z1) * 0.5f;

    // View direction (from midpoint to camera)
    float viewX = camX - midX;
    float viewY = camY - midY;
    float viewZ = camZ - midZ;

    // Distance from camera to line midpoint
    float dist = std::sqrt(viewX * viewX + viewY * viewY + viewZ * viewZ);
    if (dist < 0.001f) dist = 0.001f;

    // Calculate world-space thickness for constant screen-space pixel width
    // thickness = pixelWidth * 2 * distance * tan(fov/2) / screenHeight
    float thickness = pixelWidth * 2.0f * dist * std::tan(fovRadians * 0.5f) / screenHeight;

    // Cross product: perpendicular = dir × view
    float perpX = dirY * viewZ - dirZ * viewY;
    float perpY = dirZ * viewX - dirX * viewZ;
    float perpZ = dirX * viewY - dirY * viewX;

    // Normalize perpendicular
    float perpLen = std::sqrt(perpX * perpX + perpY * perpY + perpZ * perpZ);
    if (perpLen < 0.0001f) {
        // Line is pointing at camera, use arbitrary perpendicular
        perpX = 1.0f; perpY = 0.0f; perpZ = 0.0f;
    } else {
        perpX /= perpLen;
        perpY /= perpLen;
        perpZ /= perpLen;
    }

    // Half thickness
    float hw = thickness * 0.5f;

    // Four corners of the quad
    float ax = x0 - perpX * hw, ay = y0 - perpY * hw, az = z0 - perpZ * hw;
    float bx = x0 + perpX * hw, by = y0 + perpY * hw, bz = z0 + perpZ * hw;
    float cx = x1 + perpX * hw, cy = y1 + perpY * hw, cz = z1 + perpZ * hw;
    float dx = x1 - perpX * hw, dy = y1 - perpY * hw, dz = z1 - perpZ * hw;

    // Two triangles (a, b, c) and (a, c, d)
    t.vertex(ax, ay, az);
    t.vertex(bx, by, bz);
    t.vertex(cx, cy, cz);

    t.vertex(ax, ay, az);
    t.vertex(cx, cy, cz);
    t.vertex(dx, dy, dz);
}

// Helper to check if a block position is solid (has a tile)
static bool isSolidBlock(Level* level, int x, int y, int z) {
    if (!level) return false;
    Tile* tile = level->getTileAt(x, y, z);
    return tile != nullptr && tile->solid;
}

// Compute the actual vertex position for a corner, pushing outward only where there's empty space
// bx, by, bz: block position
// sx, sy, sz: corner direction from block center (-1 or +1)
// baseX/Y/Z: the corner's base position on the block surface
// expand: how much to expand outward in free directions
// contract: how much to pull back from neighbor blocks
static void computeVertexPosition(Level* level, int bx, int by, int bz,
                                   int sx, int sy, int sz,
                                   float baseX, float baseY, float baseZ,
                                   float expand, float contract,
                                   float& outX, float& outY, float& outZ) {
    // Check face neighbors - if a neighbor exists, contract away from it instead of expanding
    bool faceX = isSolidBlock(level, bx + sx, by, bz);
    bool faceY = isSolidBlock(level, bx, by + sy, bz);
    bool faceZ = isSolidBlock(level, bx, by, bz + sz);

    // Start at block surface (no expansion)
    outX = baseX;
    outY = baseY;
    outZ = baseZ;

    // Expand outward in free directions, contract inward where neighbors exist
    if (!faceX) outX += static_cast<float>(sx) * expand;
    else        outX -= static_cast<float>(sx) * contract;  // Pull back from neighbor

    if (!faceY) outY += static_cast<float>(sy) * expand;
    else        outY -= static_cast<float>(sy) * contract;

    if (!faceZ) outZ += static_cast<float>(sz) * expand;
    else        outZ -= static_cast<float>(sz) * contract;
}

void GameRenderer::renderHitOutline() {
    if (!hitResult.isTile()) return;

    LocalPlayer* player = minecraft->player;
    if (!player) return;

    // Use line shader for selection outline
    ShaderManager::getInstance().useLineShader();
    ShaderManager::getInstance().updateMatrices();

    auto& device = RenderDevice::get();
    device.setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);
    device.setDepthWrite(false);
    device.setCullFace(false);  // Quads should be visible from both sides

    int bx = hitResult.x;
    int by = hitResult.y;
    int bz = hitResult.z;

    // Get the tile's selection box (supports custom hitboxes like torches)
    // These are the BASE positions on the block surface (no expansion yet)
    float x0, y0, z0, x1, y1, z1;
    Tile* tile = minecraft->level ? minecraft->level->getTileAt(bx, by, bz) : nullptr;
    Level* level = minecraft->level.get();
    if (tile) {
        AABB selBox = tile->getSelectionBox(level, bx, by, bz);
        x0 = static_cast<float>(selBox.x0);
        y0 = static_cast<float>(selBox.y0);
        z0 = static_cast<float>(selBox.z0);
        x1 = static_cast<float>(selBox.x1);
        y1 = static_cast<float>(selBox.y1);
        z1 = static_cast<float>(selBox.z1);
    } else {
        // Fallback to full block
        x0 = static_cast<float>(bx);
        y0 = static_cast<float>(by);
        z0 = static_cast<float>(bz);
        x1 = static_cast<float>(bx) + 1.0f;
        y1 = static_cast<float>(by) + 1.0f;
        z1 = static_cast<float>(bz) + 1.0f;
    }

    // Camera position
    float camX = static_cast<float>(player->x);
    float camY = static_cast<float>(player->y + player->eyeHeight);
    float camZ = static_cast<float>(player->z);

    // Screen-space line parameters
    // Detect HiDPI scale factor (framebuffer pixels vs window coordinates)
    float scaleFactor = (minecraft->screenWidth > 0)
        ? static_cast<float>(minecraft->framebufferWidth) / static_cast<float>(minecraft->screenWidth)
        : 1.0f;
    float pixelWidth = 2.0f * scaleFactor;  // 2 pixels, doubled for HiDPI
    float screenHeightF = static_cast<float>(screenHeight);
    float fovRadians = 70.0f * 3.14159265f / 180.0f;  // 70 degree FOV in radians

    // How much to expand outward in free directions
    float expand = 0.002f;
    // How much to pull back from neighbor blocks
    float contract = 0.005f;

    // Compute vertex positions for all 8 corners
    // Each corner expands outward only in directions without neighbor blocks
    float vx, vy, vz;

    // LBF: x0, y0, z0 (corner direction: -1, -1, -1)
    computeVertexPosition(level, bx, by, bz, -1, -1, -1, x0, y0, z0, expand, contract, vx, vy, vz);
    float vLBF_x = vx, vLBF_y = vy, vLBF_z = vz;

    // RBF: x1, y0, z0 (corner direction: +1, -1, -1)
    computeVertexPosition(level, bx, by, bz, 1, -1, -1, x1, y0, z0, expand, contract, vx, vy, vz);
    float vRBF_x = vx, vRBF_y = vy, vRBF_z = vz;

    // RBK: x1, y0, z1 (corner direction: +1, -1, +1)
    computeVertexPosition(level, bx, by, bz, 1, -1, 1, x1, y0, z1, expand, contract, vx, vy, vz);
    float vRBK_x = vx, vRBK_y = vy, vRBK_z = vz;

    // LBK: x0, y0, z1 (corner direction: -1, -1, +1)
    computeVertexPosition(level, bx, by, bz, -1, -1, 1, x0, y0, z1, expand, contract, vx, vy, vz);
    float vLBK_x = vx, vLBK_y = vy, vLBK_z = vz;

    // LTF: x0, y1, z0 (corner direction: -1, +1, -1)
    computeVertexPosition(level, bx, by, bz, -1, 1, -1, x0, y1, z0, expand, contract, vx, vy, vz);
    float vLTF_x = vx, vLTF_y = vy, vLTF_z = vz;

    // RTF: x1, y1, z0 (corner direction: +1, +1, -1)
    computeVertexPosition(level, bx, by, bz, 1, 1, -1, x1, y1, z0, expand, contract, vx, vy, vz);
    float vRTF_x = vx, vRTF_y = vy, vRTF_z = vz;

    // RTK: x1, y1, z1 (corner direction: +1, +1, +1)
    computeVertexPosition(level, bx, by, bz, 1, 1, 1, x1, y1, z1, expand, contract, vx, vy, vz);
    float vRTK_x = vx, vRTK_y = vy, vRTK_z = vz;

    // LTK: x0, y1, z1 (corner direction: -1, +1, +1)
    computeVertexPosition(level, bx, by, bz, -1, 1, 1, x0, y1, z1, expand, contract, vx, vy, vz);
    float vLTK_x = vx, vLTK_y = vy, vLTK_z = vz;

    Tesselator& t = Tesselator::getInstance();
    t.begin(DrawMode::Triangles);
    t.color(0.0f, 0.0f, 0.0f, 0.4f);

    // Bottom face edges (4 lines)
    drawLineAsQuad(t, vLBF_x, vLBF_y, vLBF_z, vRBF_x, vRBF_y, vRBF_z, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);
    drawLineAsQuad(t, vRBF_x, vRBF_y, vRBF_z, vRBK_x, vRBK_y, vRBK_z, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);
    drawLineAsQuad(t, vRBK_x, vRBK_y, vRBK_z, vLBK_x, vLBK_y, vLBK_z, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);
    drawLineAsQuad(t, vLBK_x, vLBK_y, vLBK_z, vLBF_x, vLBF_y, vLBF_z, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);

    // Top face edges (4 lines)
    drawLineAsQuad(t, vLTF_x, vLTF_y, vLTF_z, vRTF_x, vRTF_y, vRTF_z, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);
    drawLineAsQuad(t, vRTF_x, vRTF_y, vRTF_z, vRTK_x, vRTK_y, vRTK_z, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);
    drawLineAsQuad(t, vRTK_x, vRTK_y, vRTK_z, vLTK_x, vLTK_y, vLTK_z, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);
    drawLineAsQuad(t, vLTK_x, vLTK_y, vLTK_z, vLTF_x, vLTF_y, vLTF_z, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);

    // Vertical edges (4 lines)
    drawLineAsQuad(t, vLBF_x, vLBF_y, vLBF_z, vLTF_x, vLTF_y, vLTF_z, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);
    drawLineAsQuad(t, vRBF_x, vRBF_y, vRBF_z, vRTF_x, vRTF_y, vRTF_z, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);
    drawLineAsQuad(t, vRBK_x, vRBK_y, vRBK_z, vRTK_x, vRTK_y, vRTK_z, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);
    drawLineAsQuad(t, vLBK_x, vLBK_y, vLBK_z, vLTK_x, vLTK_y, vLTK_z, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);

    t.end();

    device.setCullFace(true, CullMode::Back);
    device.setDepthWrite(true);
    device.setBlend(false);

    // Switch back to world shader
    ShaderManager::getInstance().useWorldShader();
    ShaderManager::getInstance().updateMatrices();
}

void GameRenderer::renderEntityHitboxes(float partialTick) {
    if (!minecraft->showHitboxes) return;
    if (!minecraft->level) return;

    LocalPlayer* player = minecraft->player;
    if (!player) return;

    // Use line shader for hitbox outlines
    ShaderManager::getInstance().useLineShader();
    ShaderManager::getInstance().updateMatrices();

    auto& device = RenderDevice::get();
    device.setBlend(false);  // No transparency - solid white lines
    device.setDepthWrite(false);
    device.setCullFace(false);

    // Camera position
    float camX = static_cast<float>(player->getInterpolatedX(partialTick));
    float camY = static_cast<float>(player->getInterpolatedY(partialTick) + player->eyeHeight);
    float camZ = static_cast<float>(player->getInterpolatedZ(partialTick));

    // Screen-space line parameters
    float pixelWidth = 2.0f;
    float screenHeightF = static_cast<float>(screenHeight);
    float fovRadians = 70.0f * 3.14159265f / 180.0f;

    // Inset amount to shrink hitbox slightly (avoid z-fighting with ground)
    float inset = 0.002f;

    Tesselator& t = Tesselator::getInstance();

    // Render hitbox for each entity
    for (const auto& entity : minecraft->level->entities) {
        if (!entity || entity->removed) continue;

        // Use same interpolation as model rendering: prevPos + (pos - prevPos) * partialTick
        double ix = entity->prevX + (entity->x - entity->prevX) * partialTick;
        double iy = entity->prevY + (entity->y - entity->prevY) * partialTick;
        double iz = entity->prevZ + (entity->z - entity->prevZ) * partialTick;

        // Build hitbox at interpolated position (matching how bb is calculated)
        float hw = entity->bbWidth / 2.0f;
        float x0 = static_cast<float>(ix - hw) + inset;
        float y0 = static_cast<float>(iy - entity->heightOffset + entity->ySlideOffset) + inset;
        float z0 = static_cast<float>(iz - hw) + inset;
        float x1 = static_cast<float>(ix + hw) - inset;
        float y1 = static_cast<float>(iy - entity->heightOffset + entity->ySlideOffset + entity->bbHeight) - inset;
        float z1 = static_cast<float>(iz + hw) - inset;

        t.begin(DrawMode::Triangles);
        t.color(1.0f, 1.0f, 1.0f, 1.0f);  // Solid white

        // Bottom face edges (4 lines)
        drawLineAsQuad(t, x0, y0, z0, x1, y0, z0, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);
        drawLineAsQuad(t, x1, y0, z0, x1, y0, z1, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);
        drawLineAsQuad(t, x1, y0, z1, x0, y0, z1, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);
        drawLineAsQuad(t, x0, y0, z1, x0, y0, z0, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);

        // Top face edges (4 lines)
        drawLineAsQuad(t, x0, y1, z0, x1, y1, z0, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);
        drawLineAsQuad(t, x1, y1, z0, x1, y1, z1, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);
        drawLineAsQuad(t, x1, y1, z1, x0, y1, z1, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);
        drawLineAsQuad(t, x0, y1, z1, x0, y1, z0, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);

        // Vertical edges (4 lines)
        drawLineAsQuad(t, x0, y0, z0, x0, y1, z0, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);
        drawLineAsQuad(t, x1, y0, z0, x1, y1, z0, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);
        drawLineAsQuad(t, x1, y0, z1, x1, y1, z1, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);
        drawLineAsQuad(t, x0, y0, z1, x0, y1, z1, camX, camY, camZ, pixelWidth, screenHeightF, fovRadians, 0, 0, 0, 0);

        t.end();
    }

    device.setCullFace(true, CullMode::Back);
    device.setDepthWrite(true);

    // Switch back to world shader
    ShaderManager::getInstance().useWorldShader();
    ShaderManager::getInstance().updateMatrices();
}

void GameRenderer::tick() {
    this->oHeight = this->height;

    LocalPlayer* player = minecraft->player;
    if (!player || !player->inventory) return;

    ItemStack selected = player->inventory->getSelected();

    bool matches = (this->lastSlot == player->inventory->selected) &&
                   (selected.id == this->selectedItem.id && selected.damage == this->selectedItem.damage);

    if (this->selectedItem.isEmpty() && selected.isEmpty()) {
        matches = true;
    }

    if (!selected.isEmpty() && !this->selectedItem.isEmpty() &&
        !(selected.id == this->selectedItem.id && selected.damage == this->selectedItem.damage && selected.count == this->selectedItem.count) &&
        selected.id == this->selectedItem.id) {
        this->selectedItem = selected;
        matches = true;
    }

    float max = 0.4f;
    float tHeight = matches ? 1.0f : 0.0f;
    float dd = tHeight - this->height;

    if (dd < -max) dd = -max;
    if (dd > max) dd = max;

    this->height += dd;

    if (this->height < 0.1f) {
        this->selectedItem = selected;
        this->lastSlot = player->inventory->selected;
    }
}

void GameRenderer::itemPlaced() {
    this->height = 0.0f;
}

void GameRenderer::renderHand(float partialTick) {
    LocalPlayer* player = minecraft->player;
    if (!player || !player->inventory) return;

    float h = this->oHeight + (this->height - this->oHeight) * partialTick;
    ItemStack item = this->selectedItem;

    RenderDevice::get().clear(false, true);  // Clear depth only

    MatrixStack::modelview().loadIdentity();

    // Apply view bobbing
    if (minecraft->options.viewBobbing) {
        float walkDistDelta = player->walkDist - player->oWalkDist;
        float walkDistInterp = player->oWalkDist + walkDistDelta * partialTick;
        float bobAmount = player->getBobbing(partialTick);
        float tiltAmount = player->getTilt(partialTick);

        MatrixStack::modelview().translate(
            Mth::sin(walkDistInterp * Mth::PI) * bobAmount * 0.5f,
            -std::abs(Mth::cos(walkDistInterp * Mth::PI) * bobAmount),
            0.0f
        );
        MatrixStack::modelview().rotate(Mth::sin(walkDistInterp * Mth::PI) * bobAmount * 3.0f, 0.0f, 0.0f, 1.0f);
        MatrixStack::modelview().rotate(std::abs(Mth::cos(walkDistInterp * Mth::PI + 0.2f) * bobAmount) * 5.0f, 1.0f, 0.0f, 0.0f);
        MatrixStack::modelview().rotate(tiltAmount, 1.0f, 0.0f, 0.0f);
    }

    // Use world shader for hand rendering
    ShaderManager::getInstance().useWorldShader();
    ShaderManager::getInstance().setAlphaTest(0.1f);

    // Get brightness at player position
    float br = 1.0f;
    if (minecraft->level) {
        br = minecraft->level->getBrightness(
            Mth::floor(player->x),
            Mth::floor(player->y),
            Mth::floor(player->z)
        );
    }

    // Setup lighting for hand/item (matching Java Lighting.turnOn)
    // Light directions in world space (from Java)
    float len0 = std::sqrt(0.2f*0.2f + 1.0f*1.0f + 0.7f*0.7f);
    float len1 = std::sqrt(0.2f*0.2f + 1.0f*1.0f + 0.7f*0.7f);
    glm::vec3 worldLight0(0.2f/len0, 1.0f/len0, -0.7f/len0);
    glm::vec3 worldLight1(-0.2f/len1, 1.0f/len1, 0.7f/len1);

    // Build camera rotation matrix to transform lights to view space
    float pitch = player->getInterpolatedXRot(partialTick);
    float yaw = player->getInterpolatedYRot(partialTick) + 180.0f;

    glm::mat4 cameraRot(1.0f);
    cameraRot = glm::rotate(cameraRot, glm::radians(pitch), glm::vec3(1.0f, 0.0f, 0.0f));
    cameraRot = glm::rotate(cameraRot, glm::radians(yaw), glm::vec3(0.0f, 1.0f, 0.0f));
    glm::mat3 rotMat = glm::mat3(cameraRot);

    // Transform light directions to view space
    glm::vec3 viewLight0 = rotMat * worldLight0;
    glm::vec3 viewLight1 = rotMat * worldLight1;

    // Enable lighting and set parameters
    ShaderManager::getInstance().enableLighting(true);
    ShaderManager::getInstance().setLightDirections(
        viewLight0.x, viewLight0.y, viewLight0.z,
        viewLight1.x, viewLight1.y, viewLight1.z
    );
    ShaderManager::getInstance().setLightParams(0.4f, 0.6f);  // ambient=0.4, diffuse=0.6 (from Java)
    ShaderManager::getInstance().setBrightness(br);  // World brightness at player position

    if (!item.isEmpty()) {
        // Render held item
        MatrixStack::modelview().push();

        float d = 0.8f;
        float swing = player->getAttackAnim(partialTick);
        float swing1 = Mth::sin(swing * Mth::PI);
        float swing2 = Mth::sin(Mth::sqrt(swing) * Mth::PI);

        MatrixStack::modelview().translate(
            -swing2 * 0.4f,
            Mth::sin(Mth::sqrt(swing) * Mth::PI * 2.0f) * 0.2f,
            -swing1 * 0.2f
        );

        MatrixStack::modelview().translate(0.7f * d, -0.65f * d - (1.0f - h) * 0.6f, -0.9f * d);
        MatrixStack::modelview().rotate(45.0f, 0.0f, 1.0f, 0.0f);

        swing = player->getAttackAnim(partialTick);
        swing1 = Mth::sin(swing * swing * Mth::PI);
        swing2 = Mth::sin(Mth::sqrt(swing) * Mth::PI);
        MatrixStack::modelview().rotate(-swing1 * 20.0f, 0.0f, 1.0f, 0.0f);
        MatrixStack::modelview().rotate(-swing2 * 20.0f, 0.0f, 0.0f, 1.0f);
        MatrixStack::modelview().rotate(-swing2 * 80.0f, 1.0f, 0.0f, 0.0f);

        float s = 0.4f;
        MatrixStack::modelview().scale(s, s, s);

        Item* itemPtr = item.getItem();
        if (itemPtr && itemPtr->isMirroredArt()) {
            MatrixStack::modelview().rotate(180.0f, 0.0f, 1.0f, 0.0f);
        }

        ShaderManager::getInstance().updateMatrices();
        ShaderManager::getInstance().updateNormalMatrix();
        renderItem(item);

        MatrixStack::modelview().pop();
    } else {
        // Render empty hand (arm only)
        MatrixStack::modelview().push();

        float swing = player->getAttackAnim(partialTick);
        float swing1 = Mth::sin(swing * Mth::PI);
        float swing2 = Mth::sin(Mth::sqrt(swing) * Mth::PI);

        MatrixStack::modelview().translate(
            -swing2 * 0.3f,
            Mth::sin(Mth::sqrt(swing) * Mth::PI * 2.0f) * 0.4f,
            -swing1 * 0.4f
        );

        float d = 0.8f;
        MatrixStack::modelview().translate(0.8f * d, -0.75f * d - (1.0f - h) * 0.6f, -0.9f * d);
        MatrixStack::modelview().rotate(45.0f, 0.0f, 1.0f, 0.0f);

        swing = player->getAttackAnim(partialTick);
        swing1 = Mth::sin(swing * swing * Mth::PI);
        swing2 = Mth::sin(Mth::sqrt(swing) * Mth::PI);
        MatrixStack::modelview().rotate(swing2 * 70.0f, 0.0f, 1.0f, 0.0f);
        MatrixStack::modelview().rotate(-swing1 * 20.0f, 0.0f, 0.0f, 1.0f);

        Textures::getInstance().bind("resources/mob/char.png");

        MatrixStack::modelview().translate(-1.0f, 3.6f, 3.5f);
        MatrixStack::modelview().rotate(120.0f, 0.0f, 0.0f, 1.0f);
        MatrixStack::modelview().rotate(200.0f, 1.0f, 0.0f, 0.0f);
        MatrixStack::modelview().rotate(-135.0f, 0.0f, 1.0f, 0.0f);
        MatrixStack::modelview().scale(1.0f, 1.0f, 1.0f);
        MatrixStack::modelview().translate(5.6f, 0.0f, 0.0f);

        ShaderManager::getInstance().updateMatrices();
        ShaderManager::getInstance().updateNormalMatrix();
        renderArmModel(0.0625f);

        MatrixStack::modelview().pop();
    }

    // Disable lighting after hand rendering
    ShaderManager::getInstance().enableLighting(false);
}

void GameRenderer::renderArmModel(float scale) {
    float armX = -5.0f;
    float armY = 2.0f;
    float armZ = 0.0f;
    MatrixStack::modelview().translate(armX * scale, armY * scale, armZ * scale);
    ShaderManager::getInstance().updateMatrices();

    float x0 = -3.0f, y0 = -2.0f, z0 = -2.0f;
    float x1 = x0 + 4.0f;
    float y1 = y0 + 12.0f;
    float z1 = z0 + 4.0f;

    x0 *= scale; y0 *= scale; z0 *= scale;
    x1 *= scale; y1 *= scale; z1 *= scale;

    int texU = 40;
    int texV = 16;
    int w = 4, h = 12, d = 4;

    float us = 0.001f;
    float vs = 0.002f;

    Tesselator& t = Tesselator::getInstance();

    // Right face (+X)
    {
        float u0 = (texU + d + w) / 64.0f + us;
        float v0 = (texV + d) / 32.0f + vs;
        float u1 = (texU + d + w + d) / 64.0f - us;
        float v1 = (texV + d + h) / 32.0f - vs;

        t.begin(DrawMode::Quads);
        t.color(1.0f, 1.0f, 1.0f, 1.0f);
        t.normal(1.0f, 0.0f, 0.0f);
        t.tex(u1, v0); t.vertex(x1, y0, z1);
        t.tex(u0, v0); t.vertex(x1, y0, z0);
        t.tex(u0, v1); t.vertex(x1, y1, z0);
        t.tex(u1, v1); t.vertex(x1, y1, z1);
        t.end();
    }

    // Left face (-X)
    {
        float u0 = (texU + 0) / 64.0f + us;
        float v0 = (texV + d) / 32.0f + vs;
        float u1 = (texU + d) / 64.0f - us;
        float v1 = (texV + d + h) / 32.0f - vs;

        t.begin(DrawMode::Quads);
        t.color(1.0f, 1.0f, 1.0f, 1.0f);
        t.normal(-1.0f, 0.0f, 0.0f);
        t.tex(u1, v0); t.vertex(x0, y0, z0);
        t.tex(u0, v0); t.vertex(x0, y0, z1);
        t.tex(u0, v1); t.vertex(x0, y1, z1);
        t.tex(u1, v1); t.vertex(x0, y1, z0);
        t.end();
    }

    // Top face (-Y)
    {
        float u0 = (texU + d) / 64.0f + us;
        float v0 = (texV + 0) / 32.0f + vs;
        float u1 = (texU + d + w) / 64.0f - us;
        float v1 = (texV + d) / 32.0f - vs;

        t.begin(DrawMode::Quads);
        t.color(1.0f, 1.0f, 1.0f, 1.0f);
        t.normal(0.0f, -1.0f, 0.0f);
        t.tex(u1, v0); t.vertex(x1, y0, z1);
        t.tex(u0, v0); t.vertex(x0, y0, z1);
        t.tex(u0, v1); t.vertex(x0, y0, z0);
        t.tex(u1, v1); t.vertex(x1, y0, z0);
        t.end();
    }

    // Bottom face (+Y)
    {
        float u0 = (texU + d + w) / 64.0f + us;
        float v0 = (texV + 0) / 32.0f + vs;
        float u1 = (texU + d + w + w) / 64.0f - us;
        float v1 = (texV + d) / 32.0f - vs;

        t.begin(DrawMode::Quads);
        t.color(1.0f, 1.0f, 1.0f, 1.0f);
        t.normal(0.0f, 1.0f, 0.0f);
        t.tex(u0, v0); t.vertex(x1, y1, z0);
        t.tex(u1, v0); t.vertex(x0, y1, z0);
        t.tex(u1, v1); t.vertex(x0, y1, z1);
        t.tex(u0, v1); t.vertex(x1, y1, z1);
        t.end();
    }

    // Front face (-Z)
    {
        float u0 = (texU + d) / 64.0f + us;
        float v0 = (texV + d) / 32.0f + vs;
        float u1 = (texU + d + w) / 64.0f - us;
        float v1 = (texV + d + h) / 32.0f - vs;

        t.begin(DrawMode::Quads);
        t.color(1.0f, 1.0f, 1.0f, 1.0f);
        t.normal(0.0f, 0.0f, -1.0f);
        t.tex(u1, v0); t.vertex(x1, y0, z0);
        t.tex(u0, v0); t.vertex(x0, y0, z0);
        t.tex(u0, v1); t.vertex(x0, y1, z0);
        t.tex(u1, v1); t.vertex(x1, y1, z0);
        t.end();
    }

    // Back face (+Z)
    {
        float u0 = (texU + d + w + d) / 64.0f + us;
        float v0 = (texV + d) / 32.0f + vs;
        float u1 = (texU + d + w + d + w) / 64.0f - us;
        float v1 = (texV + d + h) / 32.0f - vs;

        t.begin(DrawMode::Quads);
        t.color(1.0f, 1.0f, 1.0f, 1.0f);
        t.normal(0.0f, 0.0f, 1.0f);
        t.tex(u0, v0); t.vertex(x0, y0, z1);
        t.tex(u1, v0); t.vertex(x1, y0, z1);
        t.tex(u1, v1); t.vertex(x1, y1, z1);
        t.tex(u0, v1); t.vertex(x0, y1, z1);
        t.end();
    }
}

void GameRenderer::renderItem(const ItemStack& item) {
    MatrixStack::modelview().push();

    ShaderManager::getInstance().setAlphaTest(0.1f);

    if (item.isBlock()) {
        Tile* tile = item.getTile();
        if (tile && TileRenderer::canRender(static_cast<int>(tile->renderShape))) {
            Textures::getInstance().bind("resources/terrain.png");
            ShaderManager::getInstance().updateMatrices();
            ShaderManager::getInstance().updateNormalMatrix();
            tileRenderer.renderTileForGUI(tile, item.getAuxValue());
        } else {
            Textures::getInstance().bind("resources/terrain.png");
            int icon = item.getIcon();
            float u0 = (static_cast<float>(icon % 16 * 16) + 0.0f) / 256.0f;
            float u1 = (static_cast<float>(icon % 16 * 16) + 15.99f) / 256.0f;
            float v0 = (static_cast<float>(icon / 16 * 16) + 0.0f) / 256.0f;
            float v1 = (static_cast<float>(icon / 16 * 16) + 15.99f) / 256.0f;

            float r = 1.0f;
            float xo = 0.0f;
            float yo = 0.3f;
            MatrixStack::modelview().translate(-xo, -yo, 0.0f);
            float s = 1.5f;
            MatrixStack::modelview().scale(s, s, s);
            MatrixStack::modelview().rotate(50.0f, 0.0f, 1.0f, 0.0f);
            MatrixStack::modelview().rotate(335.0f, 0.0f, 0.0f, 1.0f);
            MatrixStack::modelview().translate(-0.9375f, -0.0625f, 0.0f);
            ShaderManager::getInstance().updateMatrices();
            ShaderManager::getInstance().updateNormalMatrix();

            float dd = 0.0625f;
            Tesselator& t = Tesselator::getInstance();

            // Front face
            t.begin(DrawMode::Quads);
            t.color(1.0f, 1.0f, 1.0f, 1.0f);
            t.normal(0.0f, 0.0f, 1.0f);
            t.tex(u1, v1); t.vertex(0.0f, 0.0f, 0.0f);
            t.tex(u0, v1); t.vertex(r, 0.0f, 0.0f);
            t.tex(u0, v0); t.vertex(r, 1.0f, 0.0f);
            t.tex(u1, v0); t.vertex(0.0f, 1.0f, 0.0f);
            t.end();

            // Back face
            t.begin(DrawMode::Quads);
            t.color(1.0f, 1.0f, 1.0f, 1.0f);
            t.normal(0.0f, 0.0f, -1.0f);
            t.tex(u1, v0); t.vertex(0.0f, 1.0f, -dd);
            t.tex(u0, v0); t.vertex(r, 1.0f, -dd);
            t.tex(u0, v1); t.vertex(r, 0.0f, -dd);
            t.tex(u1, v1); t.vertex(0.0f, 0.0f, -dd);
            t.end();

            // Side strips for thickness
            for (int i = 0; i < 16; ++i) {
                float p = static_cast<float>(i) / 16.0f;
                float uu = u1 + (u0 - u1) * p - 0.001953125f;
                float xx = r * p;

                t.begin(DrawMode::Quads);
                t.color(1.0f, 1.0f, 1.0f, 1.0f);
                t.normal(-1.0f, 0.0f, 0.0f);
                t.tex(uu, v1); t.vertex(xx, 0.0f, -dd);
                t.tex(uu, v1); t.vertex(xx, 0.0f, 0.0f);
                t.tex(uu, v0); t.vertex(xx, 1.0f, 0.0f);
                t.tex(uu, v0); t.vertex(xx, 1.0f, -dd);
                t.end();
            }

            for (int i = 0; i < 16; ++i) {
                float p = static_cast<float>(i) / 16.0f;
                float uu = u1 + (u0 - u1) * p - 0.001953125f;
                float xx = r * p + 0.0625f;

                t.begin(DrawMode::Quads);
                t.color(1.0f, 1.0f, 1.0f, 1.0f);
                t.normal(1.0f, 0.0f, 0.0f);
                t.tex(uu, v0); t.vertex(xx, 1.0f, -dd);
                t.tex(uu, v0); t.vertex(xx, 1.0f, 0.0f);
                t.tex(uu, v1); t.vertex(xx, 0.0f, 0.0f);
                t.tex(uu, v1); t.vertex(xx, 0.0f, -dd);
                t.end();
            }

            for (int i = 0; i < 16; ++i) {
                float p = static_cast<float>(i) / 16.0f;
                float vv = v1 + (v0 - v1) * p - 0.001953125f;
                float yy = r * p + 0.0625f;

                t.begin(DrawMode::Quads);
                t.color(1.0f, 1.0f, 1.0f, 1.0f);
                t.normal(0.0f, 1.0f, 0.0f);
                t.tex(u1, vv); t.vertex(0.0f, yy, 0.0f);
                t.tex(u0, vv); t.vertex(r, yy, 0.0f);
                t.tex(u0, vv); t.vertex(r, yy, -dd);
                t.tex(u1, vv); t.vertex(0.0f, yy, -dd);
                t.end();
            }

            for (int i = 0; i < 16; ++i) {
                float p = static_cast<float>(i) / 16.0f;
                float vv = v1 + (v0 - v1) * p - 0.001953125f;
                float yy = r * p;

                t.begin(DrawMode::Quads);
                t.color(1.0f, 1.0f, 1.0f, 1.0f);
                t.normal(0.0f, -1.0f, 0.0f);
                t.tex(u0, vv); t.vertex(r, yy, 0.0f);
                t.tex(u1, vv); t.vertex(0.0f, yy, 0.0f);
                t.tex(u1, vv); t.vertex(0.0f, yy, -dd);
                t.tex(u0, vv); t.vertex(r, yy, -dd);
                t.end();
            }
        }
    } else {
        // Render item sprite
        Textures::getInstance().bind("resources/gui/items.png");

        int icon = item.getIcon();
        float u0 = (static_cast<float>(icon % 16 * 16) + 0.0f) / 256.0f;
        float u1 = (static_cast<float>(icon % 16 * 16) + 15.99f) / 256.0f;
        float v0 = (static_cast<float>(icon / 16 * 16) + 0.0f) / 256.0f;
        float v1 = (static_cast<float>(icon / 16 * 16) + 15.99f) / 256.0f;

        float r = 1.0f;
        float xo = 0.0f;
        float yo = 0.3f;
        MatrixStack::modelview().translate(-xo, -yo, 0.0f);
        float s = 1.5f;
        MatrixStack::modelview().scale(s, s, s);
        MatrixStack::modelview().rotate(50.0f, 0.0f, 1.0f, 0.0f);
        MatrixStack::modelview().rotate(335.0f, 0.0f, 0.0f, 1.0f);
        MatrixStack::modelview().translate(-0.9375f, -0.0625f, 0.0f);
        ShaderManager::getInstance().updateMatrices();
        ShaderManager::getInstance().updateNormalMatrix();

        float dd = 0.0625f;
        Tesselator& t = Tesselator::getInstance();

        // Front face
        t.begin(DrawMode::Quads);
        t.color(1.0f, 1.0f, 1.0f, 1.0f);
        t.normal(0.0f, 0.0f, 1.0f);
        t.tex(u1, v1); t.vertex(0.0f, 0.0f, 0.0f);
        t.tex(u0, v1); t.vertex(r, 0.0f, 0.0f);
        t.tex(u0, v0); t.vertex(r, 1.0f, 0.0f);
        t.tex(u1, v0); t.vertex(0.0f, 1.0f, 0.0f);
        t.end();

        // Back face
        t.begin(DrawMode::Quads);
        t.color(1.0f, 1.0f, 1.0f, 1.0f);
        t.normal(0.0f, 0.0f, -1.0f);
        t.tex(u1, v0); t.vertex(0.0f, 1.0f, -dd);
        t.tex(u0, v0); t.vertex(r, 1.0f, -dd);
        t.tex(u0, v1); t.vertex(r, 0.0f, -dd);
        t.tex(u1, v1); t.vertex(0.0f, 0.0f, -dd);
        t.end();

        // Side strips
        for (int i = 0; i < 16; ++i) {
            float p = static_cast<float>(i) / 16.0f;
            float uu = u1 + (u0 - u1) * p - 0.001953125f;
            float xx = r * p;

            t.begin(DrawMode::Quads);
            t.color(1.0f, 1.0f, 1.0f, 1.0f);
            t.normal(-1.0f, 0.0f, 0.0f);
            t.tex(uu, v1); t.vertex(xx, 0.0f, -dd);
            t.tex(uu, v1); t.vertex(xx, 0.0f, 0.0f);
            t.tex(uu, v0); t.vertex(xx, 1.0f, 0.0f);
            t.tex(uu, v0); t.vertex(xx, 1.0f, -dd);
            t.end();
        }

        for (int i = 0; i < 16; ++i) {
            float p = static_cast<float>(i) / 16.0f;
            float uu = u1 + (u0 - u1) * p - 0.001953125f;
            float xx = r * p + 0.0625f;

            t.begin(DrawMode::Quads);
            t.color(1.0f, 1.0f, 1.0f, 1.0f);
            t.normal(1.0f, 0.0f, 0.0f);
            t.tex(uu, v0); t.vertex(xx, 1.0f, -dd);
            t.tex(uu, v0); t.vertex(xx, 1.0f, 0.0f);
            t.tex(uu, v1); t.vertex(xx, 0.0f, 0.0f);
            t.tex(uu, v1); t.vertex(xx, 0.0f, -dd);
            t.end();
        }

        for (int i = 0; i < 16; ++i) {
            float p = static_cast<float>(i) / 16.0f;
            float vv = v1 + (v0 - v1) * p - 0.001953125f;
            float yy = r * p + 0.0625f;

            t.begin(DrawMode::Quads);
            t.color(1.0f, 1.0f, 1.0f, 1.0f);
            t.normal(0.0f, 1.0f, 0.0f);
            t.tex(u1, vv); t.vertex(0.0f, yy, 0.0f);
            t.tex(u0, vv); t.vertex(r, yy, 0.0f);
            t.tex(u0, vv); t.vertex(r, yy, -dd);
            t.tex(u1, vv); t.vertex(0.0f, yy, -dd);
            t.end();
        }

        for (int i = 0; i < 16; ++i) {
            float p = static_cast<float>(i) / 16.0f;
            float vv = v1 + (v0 - v1) * p - 0.001953125f;
            float yy = r * p;

            t.begin(DrawMode::Quads);
            t.color(1.0f, 1.0f, 1.0f, 1.0f);
            t.normal(0.0f, -1.0f, 0.0f);
            t.tex(u0, vv); t.vertex(r, yy, 0.0f);
            t.tex(u1, vv); t.vertex(0.0f, yy, 0.0f);
            t.tex(u1, vv); t.vertex(0.0f, yy, -dd);
            t.tex(u0, vv); t.vertex(r, yy, -dd);
            t.end();
        }
    }

    MatrixStack::modelview().pop();
}

void GameRenderer::renderGui(float /*partialTick*/) {
    // Switch to orthographic projection for 2D GUI
    MatrixStack::projection().push();
    MatrixStack::projection().loadIdentity();
    MatrixStack::projection().ortho(0.0f, static_cast<float>(screenWidth), static_cast<float>(screenHeight), 0.0f, -1.0f, 1.0f);

    MatrixStack::modelview().push();
    MatrixStack::modelview().loadIdentity();

    auto& device = RenderDevice::get();
    device.setDepthTest(false);

    ShaderManager::getInstance().useGuiShader();
    ShaderManager::getInstance().updateMatrices();

    // GUI elements rendered via Gui class

    device.setDepthTest(true);

    // Restore matrices
    MatrixStack::projection().pop();
    MatrixStack::modelview().pop();
}

void GameRenderer::setCameraPosition(double /*x*/, double /*y*/, double /*z*/) {
    // Camera follows player, so this isn't typically used
}

void GameRenderer::setCameraRotation(float pitch, float yaw) {
    cameraPitch = pitch;
    cameraYaw = yaw;
}

} // namespace mc
