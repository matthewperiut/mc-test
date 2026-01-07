#pragma once

#include "phys/HitResult.hpp"

namespace mc {

class Minecraft;
class Level;
class LocalPlayer;

class GameRenderer {
public:
    Minecraft* minecraft;

    // Camera
    float cameraPitch;
    float cameraYaw;

    // Fog
    float fogRed, fogGreen, fogBlue;
    float fogStart, fogEnd;

    // Selection
    HitResult hitResult;

    // View bobbing
    float bobbing;
    float tilt;

    // Item in hand animation
    float itemHeight;      // Current item height (0=lowered, 1=raised)
    float oItemHeight;     // Previous item height for interpolation
    int lastSelectedSlot;  // Last selected hotbar slot

    // Screen dimensions
    int screenWidth, screenHeight;

    GameRenderer(Minecraft* minecraft);

    // Main render
    void render(float partialTick);
    void tick();  // Update per-tick animations

    // Setup
    void setupCamera(float partialTick);
    void setupFog();

    // Render stages
    void renderWorld(float partialTick);
    void renderHand(float partialTick);
    void renderHitOutline();
    void renderBreakingAnimation(float progress);
    void renderGui(float partialTick);

    // Picking
    void pick(float partialTick);

    // Camera control
    void setCameraPosition(double x, double y, double z);
    void setCameraRotation(float pitch, float yaw);

    // Window resize
    void resize(int width, int height);

private:
    void setupProjection(float fov, float nearPlane, float farPlane);
    void orientCamera(float partialTick);
    void applyBobbing(float partialTick);
    void renderArmModel(float scale);  // Render the arm geometry for first-person hand
};

} // namespace mc
