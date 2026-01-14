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
#include <GL/glew.h>
#include <cstdio>
#include <algorithm>

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
    glViewport(0, 0, w, h);
}

void GameRenderer::render(float partialTick) {
    // Clear screen
    glClearColor(fogRed, fogGreen, fogBlue, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

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

    Vec3 look = player->getLookVector();
    float reach = 5.0f;

    Vec3 start(
        player->getInterpolatedX(partialTick),
        player->getInterpolatedY(partialTick) + player->eyeHeight,
        player->getInterpolatedZ(partialTick)
    );
    Vec3 end = start.add(look.x * reach, look.y * reach, look.z * reach);

    hitResult = level->clip(start, end);
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
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);
    glCullFace(GL_BACK);

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
    ShaderManager::getInstance().updateMatrices();
    ShaderManager::getInstance().setSkyBrightness(skyBrightness);  // Re-set after shader switch

    // Render opaque geometry
    levelRenderer->render(partialTick, 0);

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

    // Render transparent geometry (water)
    ShaderManager::getInstance().setAlphaTest(0.0f);
    ShaderManager::getInstance().setSkyBrightness(skyBrightness);  // Ensure sky brightness is set
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    levelRenderer->render(partialTick, 1);
    glDisable(GL_BLEND);

    // Render dropped items and entities
    levelRenderer->renderEntities(partialTick);

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
    glEnable(GL_BLEND);
    glBlendFunc(GL_DST_COLOR, GL_SRC_COLOR);
    glDisable(GL_CULL_FACE);
    glDepthMask(GL_FALSE);

    glPolygonOffset(-3.0f, -3.0f);
    glEnable(GL_POLYGON_OFFSET_FILL);

    ShaderManager::getInstance().useWorldShader();
    ShaderManager::getInstance().setAlphaTest(0.0f);
    ShaderManager::getInstance().updateMatrices();

    Tesselator& t = Tesselator::getInstance();

    // Bottom face
    t.begin(GL_QUADS);
    t.color(1.0f, 1.0f, 1.0f, 1.0f);
    t.tex(texU, texV + texSize); t.vertex(x0 - e, y0 - e, z1 + e);
    t.tex(texU + texSize, texV + texSize); t.vertex(x1 + e, y0 - e, z1 + e);
    t.tex(texU + texSize, texV); t.vertex(x1 + e, y0 - e, z0 - e);
    t.tex(texU, texV); t.vertex(x0 - e, y0 - e, z0 - e);
    t.end();

    // Top face
    t.begin(GL_QUADS);
    t.color(1.0f, 1.0f, 1.0f, 1.0f);
    t.tex(texU, texV); t.vertex(x0 - e, y1 + e, z0 - e);
    t.tex(texU + texSize, texV); t.vertex(x1 + e, y1 + e, z0 - e);
    t.tex(texU + texSize, texV + texSize); t.vertex(x1 + e, y1 + e, z1 + e);
    t.tex(texU, texV + texSize); t.vertex(x0 - e, y1 + e, z1 + e);
    t.end();

    // North face
    t.begin(GL_QUADS);
    t.color(1.0f, 1.0f, 1.0f, 1.0f);
    t.tex(texU + texSize, texV); t.vertex(x0 - e, y1 + e, z0 - e);
    t.tex(texU, texV); t.vertex(x1 + e, y1 + e, z0 - e);
    t.tex(texU, texV + texSize); t.vertex(x1 + e, y0 - e, z0 - e);
    t.tex(texU + texSize, texV + texSize); t.vertex(x0 - e, y0 - e, z0 - e);
    t.end();

    // South face
    t.begin(GL_QUADS);
    t.color(1.0f, 1.0f, 1.0f, 1.0f);
    t.tex(texU, texV); t.vertex(x0 - e, y1 + e, z1 + e);
    t.tex(texU, texV + texSize); t.vertex(x0 - e, y0 - e, z1 + e);
    t.tex(texU + texSize, texV + texSize); t.vertex(x1 + e, y0 - e, z1 + e);
    t.tex(texU + texSize, texV); t.vertex(x1 + e, y1 + e, z1 + e);
    t.end();

    // West face
    t.begin(GL_QUADS);
    t.color(1.0f, 1.0f, 1.0f, 1.0f);
    t.tex(texU + texSize, texV); t.vertex(x0 - e, y1 + e, z1 + e);
    t.tex(texU, texV); t.vertex(x0 - e, y1 + e, z0 - e);
    t.tex(texU, texV + texSize); t.vertex(x0 - e, y0 - e, z0 - e);
    t.tex(texU + texSize, texV + texSize); t.vertex(x0 - e, y0 - e, z1 + e);
    t.end();

    // East face
    t.begin(GL_QUADS);
    t.color(1.0f, 1.0f, 1.0f, 1.0f);
    t.tex(texU, texV); t.vertex(x1 + e, y1 + e, z0 - e);
    t.tex(texU + texSize, texV); t.vertex(x1 + e, y1 + e, z1 + e);
    t.tex(texU + texSize, texV + texSize); t.vertex(x1 + e, y0 - e, z1 + e);
    t.tex(texU, texV + texSize); t.vertex(x1 + e, y0 - e, z0 - e);
    t.end();

    glPolygonOffset(0.0f, 0.0f);
    glDisable(GL_POLYGON_OFFSET_FILL);
    glDepthMask(GL_TRUE);
    glEnable(GL_CULL_FACE);
    glDisable(GL_BLEND);
    ShaderManager::getInstance().setAlphaTest(0.5f);
}

void GameRenderer::renderHitOutline() {
    if (!hitResult.isTile()) return;

    // Use line shader for selection outline
    ShaderManager::getInstance().useLineShader();
    ShaderManager::getInstance().updateMatrices();

    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glDepthMask(GL_FALSE);
    glDepthRange(0.0, 0.9999);
    glLineWidth(2.0f);

    int bx = hitResult.x;
    int by = hitResult.y;
    int bz = hitResult.z;

    float ss = 0.002f;

    // Get the tile's selection box (supports custom hitboxes like torches)
    float x0, y0, z0, x1, y1, z1;
    Tile* tile = minecraft->level ? minecraft->level->getTileAt(bx, by, bz) : nullptr;
    if (tile) {
        AABB selBox = tile->getSelectionBox(minecraft->level.get(), bx, by, bz);
        x0 = static_cast<float>(selBox.x0) - ss;
        y0 = static_cast<float>(selBox.y0) - ss;
        z0 = static_cast<float>(selBox.z0) - ss;
        x1 = static_cast<float>(selBox.x1) + ss;
        y1 = static_cast<float>(selBox.y1) + ss;
        z1 = static_cast<float>(selBox.z1) + ss;
    } else {
        // Fallback to full block
        x0 = static_cast<float>(bx) - ss;
        y0 = static_cast<float>(by) - ss;
        z0 = static_cast<float>(bz) - ss;
        x1 = static_cast<float>(bx) + 1.0f + ss;
        y1 = static_cast<float>(by) + 1.0f + ss;
        z1 = static_cast<float>(bz) + 1.0f + ss;
    }

    Tesselator& t = Tesselator::getInstance();

    // Bottom face
    t.begin(GL_LINE_STRIP);
    t.color(0.0f, 0.0f, 0.0f, 0.4f);
    t.vertex(x0, y0, z0);
    t.vertex(x1, y0, z0);
    t.vertex(x1, y0, z1);
    t.vertex(x0, y0, z1);
    t.vertex(x0, y0, z0);
    t.end();

    // Top face
    t.begin(GL_LINE_STRIP);
    t.color(0.0f, 0.0f, 0.0f, 0.4f);
    t.vertex(x0, y1, z0);
    t.vertex(x1, y1, z0);
    t.vertex(x1, y1, z1);
    t.vertex(x0, y1, z1);
    t.vertex(x0, y1, z0);
    t.end();

    // Vertical edges
    t.begin(GL_LINES);
    t.color(0.0f, 0.0f, 0.0f, 0.4f);
    t.vertex(x0, y0, z0);
    t.vertex(x0, y1, z0);
    t.vertex(x1, y0, z0);
    t.vertex(x1, y1, z0);
    t.vertex(x1, y0, z1);
    t.vertex(x1, y1, z1);
    t.vertex(x0, y0, z1);
    t.vertex(x0, y1, z1);
    t.end();

    glDepthRange(0.0, 1.0);
    glDepthMask(GL_TRUE);
    glDisable(GL_BLEND);

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

    glClear(GL_DEPTH_BUFFER_BIT);

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

        t.begin(GL_QUADS);
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

        t.begin(GL_QUADS);
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

        t.begin(GL_QUADS);
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

        t.begin(GL_QUADS);
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

        t.begin(GL_QUADS);
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

        t.begin(GL_QUADS);
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
            t.begin(GL_QUADS);
            t.color(1.0f, 1.0f, 1.0f, 1.0f);
            t.normal(0.0f, 0.0f, 1.0f);
            t.tex(u1, v1); t.vertex(0.0f, 0.0f, 0.0f);
            t.tex(u0, v1); t.vertex(r, 0.0f, 0.0f);
            t.tex(u0, v0); t.vertex(r, 1.0f, 0.0f);
            t.tex(u1, v0); t.vertex(0.0f, 1.0f, 0.0f);
            t.end();

            // Back face
            t.begin(GL_QUADS);
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

                t.begin(GL_QUADS);
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

                t.begin(GL_QUADS);
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

                t.begin(GL_QUADS);
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

                t.begin(GL_QUADS);
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
        t.begin(GL_QUADS);
        t.color(1.0f, 1.0f, 1.0f, 1.0f);
        t.normal(0.0f, 0.0f, 1.0f);
        t.tex(u1, v1); t.vertex(0.0f, 0.0f, 0.0f);
        t.tex(u0, v1); t.vertex(r, 0.0f, 0.0f);
        t.tex(u0, v0); t.vertex(r, 1.0f, 0.0f);
        t.tex(u1, v0); t.vertex(0.0f, 1.0f, 0.0f);
        t.end();

        // Back face
        t.begin(GL_QUADS);
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

            t.begin(GL_QUADS);
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

            t.begin(GL_QUADS);
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

            t.begin(GL_QUADS);
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

            t.begin(GL_QUADS);
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

    glDisable(GL_DEPTH_TEST);

    ShaderManager::getInstance().useGuiShader();
    ShaderManager::getInstance().updateMatrices();

    // GUI elements rendered via Gui class

    glEnable(GL_DEPTH_TEST);

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
