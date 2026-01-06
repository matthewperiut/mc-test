#include "renderer/GameRenderer.hpp"
#include "core/Minecraft.hpp"
#include "renderer/LevelRenderer.hpp"
#include "renderer/Tesselator.hpp"
#include "renderer/Textures.hpp"
#include "entity/LocalPlayer.hpp"
#include "world/Level.hpp"
#include "gamemode/GameMode.hpp"
#include "util/Mth.hpp"
#include <GL/glew.h>

namespace mc {

GameRenderer::GameRenderer(Minecraft* minecraft)
    : minecraft(minecraft)
    , cameraPitch(0), cameraYaw(0)
    , fogRed(0.5f), fogGreen(0.8f), fogBlue(1.0f)
    , fogStart(20.0f), fogEnd(80.0f)
    , bobbing(0), tilt(0)
    , screenWidth(854), screenHeight(480)
{
}

void GameRenderer::resize(int width, int height) {
    screenWidth = width;
    screenHeight = height;
    glViewport(0, 0, width, height);
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
    float fov = 70.0f;  // Could be from options
    float nearPlane = 0.05f;
    float farPlane = 256.0f;  // Could be based on render distance

    setupProjection(fov, nearPlane, farPlane);

    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    orientCamera(partialTick);
}

void GameRenderer::setupProjection(float fov, float nearPlane, float farPlane) {
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();

    float aspect = static_cast<float>(screenWidth) / static_cast<float>(screenHeight);
    float ymax = nearPlane * std::tan(fov * Mth::DEG_TO_RAD * 0.5f);
    float xmax = ymax * aspect;

    glFrustum(-xmax, xmax, -ymax, ymax, nearPlane, farPlane);
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

    glRotatef(pitch, 1.0f, 0.0f, 0.0f);
    glRotatef(yaw + 180.0f, 0.0f, 1.0f, 0.0f);

    // Apply camera position (at eye level)
    double eyeX = player->getInterpolatedX(partialTick);
    double eyeY = player->getInterpolatedY(partialTick) + player->eyeHeight;
    double eyeZ = player->getInterpolatedZ(partialTick);

    glTranslated(-eyeX, -eyeY, -eyeZ);
}

void GameRenderer::applyBobbing(float partialTick) {
    LocalPlayer* player = minecraft->player;
    if (!player) return;

    float bob = player->getBobbing(partialTick);
    float tiltVal = player->getTilt(partialTick);

    // Apply bobbing
    float bobAngle = bob * Mth::PI;
    glTranslatef(
        Mth::sin(bobAngle) * bob * 0.5f,
        -std::abs(Mth::cos(bobAngle) * bob),
        0.0f
    );
    glRotatef(Mth::sin(bobAngle) * bob * 3.0f, 0.0f, 0.0f, 1.0f);
    glRotatef(std::abs(Mth::cos(bobAngle - 0.2f) * bob) * 5.0f, 1.0f, 0.0f, 0.0f);

    // Apply tilt (for damage effects)
    glRotatef(tiltVal, 0.0f, 0.0f, 1.0f);
}

void GameRenderer::setupFog() {
    // Calculate fog color based on time of day (matching Java Dimension.getFogColor)
    if (minecraft && minecraft->level) {
        float timeOfDay = minecraft->level->getTimeOfDay();
        float brightness = std::cos(timeOfDay * 3.14159265f * 2.0f) * 2.0f + 0.5f;
        brightness = std::max(0.0f, std::min(1.0f, brightness));

        // Java fog base color: (0.7529412, 0.84705883, 1.0)
        // Multiplied by (brightness * 0.94 + 0.06) for R/G, (brightness * 0.91 + 0.09) for B
        fogRed = 0.7529412f * (brightness * 0.94f + 0.06f);
        fogGreen = 0.84705883f * (brightness * 0.94f + 0.06f);
        fogBlue = 1.0f * (brightness * 0.91f + 0.09f);
    }

    // Calculate fog distance based on render distance
    int renderDist = minecraft->options.renderDistance;
    float distMultiplier = 1.0f - renderDist * 0.2f;

    fogStart = 20.0f * distMultiplier;
    fogEnd = 80.0f * distMultiplier;

    glEnable(GL_FOG);
    glFogi(GL_FOG_MODE, GL_LINEAR);

    float fogColor[] = {fogRed, fogGreen, fogBlue, 1.0f};
    glFogfv(GL_FOG_COLOR, fogColor);
    glFogf(GL_FOG_START, fogStart);
    glFogf(GL_FOG_END, fogEnd);
}

void GameRenderer::pick(float partialTick) {
    LocalPlayer* player = minecraft->player;
    Level* level = minecraft->level.get();

    if (!player || !level) {
        hitResult = HitResult();
        return;
    }

    // Get player view
    Vec3 eyePos = player->getEyePosition();
    Vec3 look = player->getLookVector();

    // Ray length
    float reach = 5.0f;

    Vec3 start(
        player->getInterpolatedX(partialTick),
        player->getInterpolatedY(partialTick) + player->eyeHeight,
        player->getInterpolatedZ(partialTick)
    );
    Vec3 end = start.add(look.x * reach, look.y * reach, look.z * reach);

    // Raycast
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

    // Enable required states for legacy fixed-function pipeline
    glEnable(GL_TEXTURE_2D);
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);
    glCullFace(GL_BACK);

    // Disable lighting - we use vertex colors directly
    glDisable(GL_LIGHTING);

    // Enable alpha test for transparent textures
    glEnable(GL_ALPHA_TEST);
    glAlphaFunc(GL_GREATER, 0.5f);

    // Set default color to white (vertex colors will override)
    glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

    // Render sky
    levelRenderer->renderSky(partialTick);

    // Re-bind terrain texture after sky rendering (sky binds sun/moon textures)
    Textures::getInstance().bind("resources/terrain.png");

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
    glDisable(GL_ALPHA_TEST);
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

    // Calculate break texture index (240-249 in terrain.png)
    int breakStage = static_cast<int>(progress * 10.0f);
    if (breakStage > 9) breakStage = 9;
    int textureIndex = 240 + breakStage;

    // Calculate UV coordinates for break texture in terrain.png (16x16 grid)
    float texU = static_cast<float>(textureIndex % 16) / 16.0f;
    float texV = static_cast<float>(textureIndex / 16) / 16.0f;
    float texSize = 1.0f / 16.0f;

    float x = static_cast<float>(hitResult.x);
    float y = static_cast<float>(hitResult.y);
    float z = static_cast<float>(hitResult.z);

    // Slightly expand the crack overlay to prevent z-fighting
    float e = 0.002f;

    // Setup for rendering crack overlay (matching Java)
    Textures::getInstance().bind("resources/terrain.png");
    glEnable(GL_BLEND);
    glBlendFunc(GL_DST_COLOR, GL_SRC_COLOR);  // Multiplicative blending
    glDisable(GL_ALPHA_TEST);
    glDisable(GL_CULL_FACE);  // Render both sides
    glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    glDepthMask(GL_FALSE);  // Don't write to depth buffer

    // Use polygon offset to prevent z-fighting
    glPolygonOffset(-3.0f, -3.0f);
    glEnable(GL_POLYGON_OFFSET_FILL);

    glBegin(GL_QUADS);

    // Bottom face (y = y) - render outward (visible from below)
    glTexCoord2f(texU, texV + texSize);
    glVertex3f(x - e, y - e, z + 1 + e);
    glTexCoord2f(texU + texSize, texV + texSize);
    glVertex3f(x + 1 + e, y - e, z + 1 + e);
    glTexCoord2f(texU + texSize, texV);
    glVertex3f(x + 1 + e, y - e, z - e);
    glTexCoord2f(texU, texV);
    glVertex3f(x - e, y - e, z - e);

    // Top face (y = y + 1) - render outward (visible from above)
    glTexCoord2f(texU, texV);
    glVertex3f(x - e, y + 1 + e, z - e);
    glTexCoord2f(texU + texSize, texV);
    glVertex3f(x + 1 + e, y + 1 + e, z - e);
    glTexCoord2f(texU + texSize, texV + texSize);
    glVertex3f(x + 1 + e, y + 1 + e, z + 1 + e);
    glTexCoord2f(texU, texV + texSize);
    glVertex3f(x - e, y + 1 + e, z + 1 + e);

    // North face (z = z) - visible from north
    glTexCoord2f(texU + texSize, texV);
    glVertex3f(x - e, y + 1 + e, z - e);
    glTexCoord2f(texU, texV);
    glVertex3f(x + 1 + e, y + 1 + e, z - e);
    glTexCoord2f(texU, texV + texSize);
    glVertex3f(x + 1 + e, y - e, z - e);
    glTexCoord2f(texU + texSize, texV + texSize);
    glVertex3f(x - e, y - e, z - e);

    // South face (z = z + 1) - visible from south
    glTexCoord2f(texU, texV);
    glVertex3f(x - e, y + 1 + e, z + 1 + e);
    glTexCoord2f(texU, texV + texSize);
    glVertex3f(x - e, y - e, z + 1 + e);
    glTexCoord2f(texU + texSize, texV + texSize);
    glVertex3f(x + 1 + e, y - e, z + 1 + e);
    glTexCoord2f(texU + texSize, texV);
    glVertex3f(x + 1 + e, y + 1 + e, z + 1 + e);

    // West face (x = x) - visible from west
    glTexCoord2f(texU + texSize, texV);
    glVertex3f(x - e, y + 1 + e, z + 1 + e);
    glTexCoord2f(texU, texV);
    glVertex3f(x - e, y + 1 + e, z - e);
    glTexCoord2f(texU, texV + texSize);
    glVertex3f(x - e, y - e, z - e);
    glTexCoord2f(texU + texSize, texV + texSize);
    glVertex3f(x - e, y - e, z + 1 + e);

    // East face (x = x + 1) - visible from east
    glTexCoord2f(texU, texV);
    glVertex3f(x + 1 + e, y + 1 + e, z - e);
    glTexCoord2f(texU + texSize, texV);
    glVertex3f(x + 1 + e, y + 1 + e, z + 1 + e);
    glTexCoord2f(texU + texSize, texV + texSize);
    glVertex3f(x + 1 + e, y - e, z + 1 + e);
    glTexCoord2f(texU, texV + texSize);
    glVertex3f(x + 1 + e, y - e, z - e);

    glEnd();

    glPolygonOffset(0.0f, 0.0f);
    glDisable(GL_POLYGON_OFFSET_FILL);
    glDepthMask(GL_TRUE);
    glEnable(GL_CULL_FACE);
    glDisable(GL_BLEND);
    glEnable(GL_ALPHA_TEST);
    glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
}

void GameRenderer::renderHitOutline() {
    if (!hitResult.isTile()) return;

    glDisable(GL_TEXTURE_2D);
    glDisable(GL_DEPTH_TEST);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    float x = static_cast<float>(hitResult.x);
    float y = static_cast<float>(hitResult.y);
    float z = static_cast<float>(hitResult.z);

    float expand = 0.002f;

    glColor4f(0.0f, 0.0f, 0.0f, 0.4f);
    glLineWidth(2.0f);

    glBegin(GL_LINE_LOOP);
    // Bottom face
    glVertex3f(x - expand, y - expand, z - expand);
    glVertex3f(x + 1 + expand, y - expand, z - expand);
    glVertex3f(x + 1 + expand, y - expand, z + 1 + expand);
    glVertex3f(x - expand, y - expand, z + 1 + expand);
    glEnd();

    glBegin(GL_LINE_LOOP);
    // Top face
    glVertex3f(x - expand, y + 1 + expand, z - expand);
    glVertex3f(x + 1 + expand, y + 1 + expand, z - expand);
    glVertex3f(x + 1 + expand, y + 1 + expand, z + 1 + expand);
    glVertex3f(x - expand, y + 1 + expand, z + 1 + expand);
    glEnd();

    glBegin(GL_LINES);
    // Vertical edges
    glVertex3f(x - expand, y - expand, z - expand);
    glVertex3f(x - expand, y + 1 + expand, z - expand);

    glVertex3f(x + 1 + expand, y - expand, z - expand);
    glVertex3f(x + 1 + expand, y + 1 + expand, z - expand);

    glVertex3f(x + 1 + expand, y - expand, z + 1 + expand);
    glVertex3f(x + 1 + expand, y + 1 + expand, z + 1 + expand);

    glVertex3f(x - expand, y - expand, z + 1 + expand);
    glVertex3f(x - expand, y + 1 + expand, z + 1 + expand);
    glEnd();

    glLineWidth(1.0f);
    glDisable(GL_BLEND);
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_TEXTURE_2D);
}

void GameRenderer::renderHand(float /*partialTick*/) {
    // Would render first-person hand/item
    // Simplified - not implementing full hand rendering
}

void GameRenderer::renderGui(float /*partialTick*/) {
    // Switch to orthographic projection for 2D GUI
    glMatrixMode(GL_PROJECTION);
    glPushMatrix();
    glLoadIdentity();
    glOrtho(0, screenWidth, screenHeight, 0, -1, 1);

    glMatrixMode(GL_MODELVIEW);
    glPushMatrix();
    glLoadIdentity();

    glDisable(GL_DEPTH_TEST);
    glDisable(GL_LIGHTING);

    // Render crosshair
    glEnable(GL_BLEND);
    glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_SRC_COLOR);

    float cx = screenWidth / 2.0f;
    float cy = screenHeight / 2.0f;
    float size = 10.0f;

    glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    glDisable(GL_TEXTURE_2D);

    glBegin(GL_LINES);
    // Horizontal
    glVertex2f(cx - size, cy);
    glVertex2f(cx + size, cy);
    // Vertical
    glVertex2f(cx, cy - size);
    glVertex2f(cx, cy + size);
    glEnd();

    glEnable(GL_TEXTURE_2D);
    glDisable(GL_BLEND);
    glEnable(GL_DEPTH_TEST);

    // Restore matrices
    glMatrixMode(GL_PROJECTION);
    glPopMatrix();
    glMatrixMode(GL_MODELVIEW);
    glPopMatrix();
}

void GameRenderer::setCameraPosition(double /*x*/, double /*y*/, double /*z*/) {
    // Camera follows player, so this isn't typically used
}

void GameRenderer::setCameraRotation(float pitch, float yaw) {
    cameraPitch = pitch;
    cameraYaw = yaw;
}

} // namespace mc
