#pragma once

#include "phys/HitResult.hpp"
#include "renderer/TileRenderer.hpp"
#include "item/Inventory.hpp"  // For ItemStack
#include <memory>

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

    // Item in hand animation (matching Java ItemInHandRenderer exactly)
    float height;           // Current item height (0=lowered, 1=raised) - Java: height
    float oHeight;          // Previous item height for interpolation - Java: oHeight
    int lastSlot;           // Last selected hotbar slot - Java: lastSlot
    ItemStack selectedItem; // Item currently being rendered (only changes when hand is down) - Java: selectedItem

    // Screen dimensions
    int screenWidth, screenHeight;

    // Tile renderer for held blocks
    TileRenderer tileRenderer;

    GameRenderer(Minecraft* minecraft);

    // Main render
    void render(float partialTick);
    void tick();  // Update per-tick animations
    void itemPlaced();  // Called when item is placed, triggers hand-down animation

    // Setup
    void setupCamera(float partialTick);
    void setupFog();

    // Render stages
    void renderWorld(float partialTick);
    void renderHand(float partialTick);
    void renderHitOutline();
    void renderEntityHitboxes(float partialTick);
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
    void renderItem(const ItemStack& item);  // Render held item (block or flat sprite)
};

} // namespace mc
