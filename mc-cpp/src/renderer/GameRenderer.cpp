#include "renderer/GameRenderer.hpp"
#include "core/Minecraft.hpp"
#include "renderer/LevelRenderer.hpp"
#include "renderer/Tesselator.hpp"
#include "renderer/Textures.hpp"
#include "entity/LocalPlayer.hpp"
#include "world/Level.hpp"
#include "gamemode/GameMode.hpp"
#include "item/Inventory.hpp"
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
    , itemHeight(1.0f), oItemHeight(1.0f)
    , lastSelectedSlot(-1)
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

    // Forcefully disable all client states that might interfere
    glDisableClientState(GL_VERTEX_ARRAY);
    glDisableClientState(GL_COLOR_ARRAY);
    glDisableClientState(GL_TEXTURE_COORD_ARRAY);
    glDisableClientState(GL_NORMAL_ARRAY);

    // GL state setup (matching Java)
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glDisable(GL_TEXTURE_2D);
    glDisable(GL_LIGHTING);
    glDisable(GL_ALPHA_TEST);  // Disable alpha test so low alpha values aren't discarded
    glDepthMask(GL_FALSE);  // Don't write to depth buffer
    glDepthRange(0.0, 0.9999);  // Slight bias toward camera
    glLineWidth(2.0f);

    // Black, 40% alpha (matching Java)
    glColor4f(0.0f, 0.0f, 0.0f, 0.4f);

    // Get block coordinates
    int bx = hitResult.x;
    int by = hitResult.y;
    int bz = hitResult.z;

    // Expansion to prevent z-fighting (Java uses 0.002F)
    float ss = 0.002f;

    // Get the tile's AABB and expand it
    float x0 = static_cast<float>(bx) - ss;
    float y0 = static_cast<float>(by) - ss;
    float z0 = static_cast<float>(bz) - ss;
    float x1 = static_cast<float>(bx) + 1.0f + ss;
    float y1 = static_cast<float>(by) + 1.0f + ss;
    float z1 = static_cast<float>(bz) + 1.0f + ss;

    // Draw with immediate mode
    // Bottom face
    glBegin(GL_LINE_STRIP);
    glVertex3f(x0, y0, z0);
    glVertex3f(x1, y0, z0);
    glVertex3f(x1, y0, z1);
    glVertex3f(x0, y0, z1);
    glVertex3f(x0, y0, z0);
    glEnd();

    // Top face
    glBegin(GL_LINE_STRIP);
    glVertex3f(x0, y1, z0);
    glVertex3f(x1, y1, z0);
    glVertex3f(x1, y1, z1);
    glVertex3f(x0, y1, z1);
    glVertex3f(x0, y1, z0);
    glEnd();

    // Vertical edges
    glBegin(GL_LINES);
    glVertex3f(x0, y0, z0);
    glVertex3f(x0, y1, z0);
    glVertex3f(x1, y0, z0);
    glVertex3f(x1, y1, z0);
    glVertex3f(x1, y0, z1);
    glVertex3f(x1, y1, z1);
    glVertex3f(x0, y0, z1);
    glVertex3f(x0, y1, z1);
    glEnd();

    // Restore state
    glDepthRange(0.0, 1.0);
    glDepthMask(GL_TRUE);
    glEnable(GL_TEXTURE_2D);
    glEnable(GL_ALPHA_TEST);
    glDisable(GL_BLEND);
}

void GameRenderer::tick() {
    // Update item switch animation (matching Java ItemInHandRenderer.tick)
    oItemHeight = itemHeight;

    LocalPlayer* player = minecraft->player;
    if (!player || !player->inventory) return;

    int currentSlot = player->inventory->selected;
    (void)player->inventory->getSelected();  // Referenced later for item changes

    // Check if slot changed
    bool slotChanged = (lastSelectedSlot != currentSlot);

    // For now, we only care if the slot changed
    float targetHeight = 1.0f;
    if (slotChanged) {
        targetHeight = 0.0f;  // Drop the item when switching
    }

    // Smooth transition
    float maxDelta = 0.4f;
    float delta = targetHeight - itemHeight;
    delta = std::max(-maxDelta, std::min(maxDelta, delta));
    itemHeight += delta;

    // Update last slot when item is lowered
    if (itemHeight < 0.1f) {
        lastSelectedSlot = currentSlot;
    }
}

void GameRenderer::renderHand(float partialTick) {
    LocalPlayer* player = minecraft->player;
    if (!player || !player->inventory) return;

    const ItemStack& selected = player->inventory->getSelected();

    // Only render hand when slot is empty (no item held)
    // TODO: Implement item rendering for non-empty slots
    if (!selected.isEmpty()) return;

    // Clear depth buffer so hand renders in front of everything (matching Java)
    glClear(GL_DEPTH_BUFFER_BIT);

    // Reset modelview matrix to render in screen space (matching Java renderItemInHand)
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    // Apply view bobbing (matching Java GameRenderer.bobView exactly)
    if (minecraft->options.viewBobbing) {
        // Interpolate walk distance for phase (oscillation)
        float walkDistDelta = player->walkDist - player->oWalkDist;
        float walkDistInterp = player->oWalkDist + walkDistDelta * partialTick;

        // Interpolate bob amount for amplitude
        float bobAmount = player->getBobbing(partialTick);

        // Interpolate tilt
        float tiltAmount = player->getTilt(partialTick);

        // Apply bobbing transforms (matching Java exactly)
        glTranslatef(
            Mth::sin(walkDistInterp * Mth::PI) * bobAmount * 0.5f,
            -std::abs(Mth::cos(walkDistInterp * Mth::PI) * bobAmount),
            0.0f
        );
        glRotatef(Mth::sin(walkDistInterp * Mth::PI) * bobAmount * 3.0f, 0.0f, 0.0f, 1.0f);
        glRotatef(std::abs(Mth::cos(walkDistInterp * Mth::PI + 0.2f) * bobAmount) * 5.0f, 1.0f, 0.0f, 0.0f);
        glRotatef(tiltAmount, 1.0f, 0.0f, 0.0f);
    }

    // Get interpolated item height for smooth animation
    float h = oItemHeight + (itemHeight - oItemHeight) * partialTick;

    // Get brightness at player position
    float br = 1.0f;
    if (minecraft->level) {
        br = minecraft->level->getBrightness(
            Mth::floor(player->x),
            Mth::floor(player->y),
            Mth::floor(player->z)
        );
    }

    // Setup lighting (matching Java Lighting.turnOn exactly)
    // Light positions must be set WITH camera rotation applied so they're in world space
    // This makes the lighting change as you look around
    glEnable(GL_LIGHTING);
    glEnable(GL_LIGHT0);
    glEnable(GL_LIGHT1);
    glEnable(GL_COLOR_MATERIAL);
    glColorMaterial(GL_FRONT, GL_AMBIENT_AND_DIFFUSE);

    // Light parameters from Java Lighting.turnOn()
    float ambient = 0.4f;   // Global ambient
    float diffuse = 0.6f;   // Light diffuse intensity
    float specular = 0.0f;  // No specular

    // Apply camera rotation before setting light positions
    // This puts lights in world space so lighting responds to camera rotation
    glPushMatrix();
    glRotatef(player->getInterpolatedXRot(partialTick), 1.0f, 0.0f, 0.0f);
    glRotatef(player->getInterpolatedYRot(partialTick) + 180.0f, 0.0f, 1.0f, 0.0f);

    // Light 0 direction: normalized (0.2, 1.0, -0.7)
    float len0 = std::sqrt(0.2f*0.2f + 1.0f*1.0f + 0.7f*0.7f);
    float light0Pos[] = {0.2f/len0, 1.0f/len0, -0.7f/len0, 0.0f};
    float light0Diffuse[] = {diffuse, diffuse, diffuse, 1.0f};
    float light0Ambient[] = {0.0f, 0.0f, 0.0f, 1.0f};
    float light0Specular[] = {specular, specular, specular, 1.0f};

    glLightfv(GL_LIGHT0, GL_POSITION, light0Pos);
    glLightfv(GL_LIGHT0, GL_DIFFUSE, light0Diffuse);
    glLightfv(GL_LIGHT0, GL_AMBIENT, light0Ambient);
    glLightfv(GL_LIGHT0, GL_SPECULAR, light0Specular);

    // Light 1 direction: normalized (-0.2, 1.0, 0.7)
    float len1 = std::sqrt(0.2f*0.2f + 1.0f*1.0f + 0.7f*0.7f);
    float light1Pos[] = {-0.2f/len1, 1.0f/len1, 0.7f/len1, 0.0f};
    float light1Diffuse[] = {diffuse, diffuse, diffuse, 1.0f};
    float light1Ambient[] = {0.0f, 0.0f, 0.0f, 1.0f};
    float light1Specular[] = {specular, specular, specular, 1.0f};

    glLightfv(GL_LIGHT1, GL_POSITION, light1Pos);
    glLightfv(GL_LIGHT1, GL_DIFFUSE, light1Diffuse);
    glLightfv(GL_LIGHT1, GL_AMBIENT, light1Ambient);
    glLightfv(GL_LIGHT1, GL_SPECULAR, light1Specular);

    glPopMatrix();  // Remove camera rotation, keep light positions in world space

    // Global ambient light model
    float globalAmbient[] = {ambient, ambient, ambient, 1.0f};
    glLightModelfv(GL_LIGHT_MODEL_AMBIENT, globalAmbient);
    glShadeModel(GL_FLAT);

    // Set hand color based on world brightness
    glColor4f(br, br, br, 1.0f);

    // Now render the hand (matching Java ItemInHandRenderer.render empty hand case)
    glPushMatrix();

    // Get swing animation (matching Java player.getAttackAnim(a))
    float swing = player->getAttackAnim(partialTick);
    float swing1 = Mth::sin(swing * Mth::PI);
    float swing2 = Mth::sin(Mth::sqrt(swing) * Mth::PI);

    // Apply swing offset (matching Java ItemInHandRenderer line 177)
    glTranslatef(
        -swing2 * 0.3f,
        Mth::sin(Mth::sqrt(swing) * Mth::PI * 2.0f) * 0.4f,
        -swing1 * 0.4f
    );

    // Base hand position (matching Java line 178)
    float d = 0.8f;
    glTranslatef(0.8f * d, -0.75f * d - (1.0f - h) * 0.6f, -0.9f * d);

    // Base rotation (matching Java line 179)
    glRotatef(45.0f, 0.0f, 1.0f, 0.0f);

    // Enable rescale normal for proper lighting after scaling
    glEnable(GL_RESCALE_NORMAL);

    // Apply swing rotations (matching Java lines 181-185)
    // Note: Java recalculates swing here for the rotations
    swing = player->getAttackAnim(partialTick);
    swing1 = Mth::sin(swing * swing * Mth::PI);
    swing2 = Mth::sin(Mth::sqrt(swing) * Mth::PI);
    glRotatef(swing2 * 70.0f, 0.0f, 1.0f, 0.0f);
    glRotatef(-swing1 * 20.0f, 0.0f, 0.0f, 1.0f);

    // Bind player skin texture (matching Java line 186)
    Textures::getInstance().bind("resources/mob/char.png");

    // Final transformations to position arm correctly (matching Java lines 187-192)
    glTranslatef(-1.0f, 3.6f, 3.5f);
    glRotatef(120.0f, 0.0f, 0.0f, 1.0f);
    glRotatef(200.0f, 1.0f, 0.0f, 0.0f);
    glRotatef(-135.0f, 0.0f, 1.0f, 0.0f);
    glScalef(1.0f, 1.0f, 1.0f);
    glTranslatef(5.6f, 0.0f, 0.0f);

    // Render the arm at model scale 0.0625 (1/16) (matching Java line 197)
    renderArmModel(0.0625f);

    glDisable(GL_RESCALE_NORMAL);

    // Disable lighting (matching Java Lighting.turnOff)
    glDisable(GL_LIGHTING);
    glDisable(GL_LIGHT0);
    glDisable(GL_LIGHT1);
    glDisable(GL_COLOR_MATERIAL);

    glPopMatrix();
}

void GameRenderer::renderArmModel(float scale) {
    // Render arm0 from HumanoidModel: Cube(40, 16) with addBox(-3, -2, -2, 4, 12, 4)
    // arm0.setPos(-5.0F, 2.0F, 0.0F) - position relative to body

    // Apply arm position translation (matching Java Cube.render)
    // The Cube.render() method translates by (x * scale, y * scale, z * scale)
    float armX = -5.0f;
    float armY = 2.0f;
    float armZ = 0.0f;
    glTranslatef(armX * scale, armY * scale, armZ * scale);

    // Box dimensions from addBox(-3, -2, -2, 4, 12, 4)
    float x0 = -3.0f, y0 = -2.0f, z0 = -2.0f;
    float x1 = x0 + 4.0f;  // = 1.0f
    float y1 = y0 + 12.0f; // = 10.0f
    float z1 = z0 + 4.0f;  // = 2.0f

    // Apply scale
    x0 *= scale; y0 *= scale; z0 *= scale;
    x1 *= scale; y1 *= scale; z1 *= scale;

    // Texture coordinates for arm (texture offset 40, 16)
    // Player texture is 64x32
    // UV layout for a box with dimensions (w=4, h=12, d=4):
    // - d=4, w=4, h=12
    // - Each face has specific UV regions in the texture

    int texU = 40;
    int texV = 16;
    int w = 4, h = 12, d = 4;

    // Calculate UV coordinates for each face (normalized to 64x32 texture)
    // Small inset to prevent texture bleeding
    float us = 0.001f;  // u seam fix
    float vs = 0.002f;  // v seam fix

    Tesselator& t = Tesselator::getInstance();

    // Right face (+X): polygon[0] - vertices l1, u1, u2, l2
    // UV: (d+w, d) to (d+w+d, d+h) = (8, 4) to (12, 16) offset by (40, 16)
    {
        float u0 = (texU + d + w) / 64.0f + us;
        float v0 = (texV + d) / 32.0f + vs;
        float u1 = (texU + d + w + d) / 64.0f - us;
        float v1 = (texV + d + h) / 32.0f - vs;

        t.begin(GL_QUADS);
        t.normal(1.0f, 0.0f, 0.0f);
        t.tex(u1, v0); t.vertex(x1, y0, z1);
        t.tex(u0, v0); t.vertex(x1, y0, z0);
        t.tex(u0, v1); t.vertex(x1, y1, z0);
        t.tex(u1, v1); t.vertex(x1, y1, z1);
        t.end();
    }

    // Left face (-X): polygon[1] - vertices u0, l0, l3, u3
    // UV: (0, d) to (d, d+h) = (0, 4) to (4, 16) offset by (40, 16)
    {
        float u0 = (texU + 0) / 64.0f + us;
        float v0 = (texV + d) / 32.0f + vs;
        float u1 = (texU + d) / 64.0f - us;
        float v1 = (texV + d + h) / 32.0f - vs;

        t.begin(GL_QUADS);
        t.normal(-1.0f, 0.0f, 0.0f);
        t.tex(u1, v0); t.vertex(x0, y0, z0);
        t.tex(u0, v0); t.vertex(x0, y0, z1);
        t.tex(u0, v1); t.vertex(x0, y1, z1);
        t.tex(u1, v1); t.vertex(x0, y1, z0);
        t.end();
    }

    // Top face (-Y in model space, but rendered as top): polygon[2] - vertices l1, l0, u0, u1
    // UV: (d, 0) to (d+w, d) = (4, 0) to (8, 4) offset by (40, 16)
    {
        float u0 = (texU + d) / 64.0f + us;
        float v0 = (texV + 0) / 32.0f + vs;
        float u1 = (texU + d + w) / 64.0f - us;
        float v1 = (texV + d) / 32.0f - vs;

        t.begin(GL_QUADS);
        t.normal(0.0f, -1.0f, 0.0f);
        t.tex(u1, v0); t.vertex(x1, y0, z1);
        t.tex(u0, v0); t.vertex(x0, y0, z1);
        t.tex(u0, v1); t.vertex(x0, y0, z0);
        t.tex(u1, v1); t.vertex(x1, y0, z0);
        t.end();
    }

    // Bottom face (+Y): polygon[3] - vertices u2, u3, l3, l2
    // UV: (d+w, 0) to (d+w+w, d) = (8, 0) to (12, 4) offset by (40, 16)
    {
        float u0 = (texU + d + w) / 64.0f + us;
        float v0 = (texV + 0) / 32.0f + vs;
        float u1 = (texU + d + w + w) / 64.0f - us;
        float v1 = (texV + d) / 32.0f - vs;

        t.begin(GL_QUADS);
        t.normal(0.0f, 1.0f, 0.0f);
        t.tex(u0, v0); t.vertex(x1, y1, z0);
        t.tex(u1, v0); t.vertex(x0, y1, z0);
        t.tex(u1, v1); t.vertex(x0, y1, z1);
        t.tex(u0, v1); t.vertex(x1, y1, z1);
        t.end();
    }

    // Front face (-Z): polygon[4] - vertices u1, u0, u3, u2
    // UV: (d, d) to (d+w, d+h) = (4, 4) to (8, 16) offset by (40, 16)
    {
        float u0 = (texU + d) / 64.0f + us;
        float v0 = (texV + d) / 32.0f + vs;
        float u1 = (texU + d + w) / 64.0f - us;
        float v1 = (texV + d + h) / 32.0f - vs;

        t.begin(GL_QUADS);
        t.normal(0.0f, 0.0f, -1.0f);
        t.tex(u1, v0); t.vertex(x1, y0, z0);
        t.tex(u0, v0); t.vertex(x0, y0, z0);
        t.tex(u0, v1); t.vertex(x0, y1, z0);
        t.tex(u1, v1); t.vertex(x1, y1, z0);
        t.end();
    }

    // Back face (+Z): polygon[5] - vertices l0, l1, l2, l3
    // UV: (d+w+d, d) to (d+w+d+w, d+h) = (12, 4) to (16, 16) offset by (40, 16)
    {
        float u0 = (texU + d + w + d) / 64.0f + us;
        float v0 = (texV + d) / 32.0f + vs;
        float u1 = (texU + d + w + d + w) / 64.0f - us;
        float v1 = (texV + d + h) / 32.0f - vs;

        t.begin(GL_QUADS);
        t.normal(0.0f, 0.0f, 1.0f);
        t.tex(u0, v0); t.vertex(x0, y0, z1);
        t.tex(u1, v0); t.vertex(x1, y0, z1);
        t.tex(u1, v1); t.vertex(x1, y1, z1);
        t.tex(u0, v1); t.vertex(x0, y1, z1);
        t.end();
    }
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

    // GUI elements rendered via Gui class

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
