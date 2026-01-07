#pragma once

#include "gui/Font.hpp"
#include "item/Inventory.hpp"
#include <GL/glew.h>
#include <random>

namespace mc {

class Minecraft;
class TileRenderer;

class Gui {
public:
    Minecraft* minecraft;
    Font font;

    int screenWidth;
    int screenHeight;
    int scaledWidth;   // GUI-scaled width
    int scaledHeight;  // GUI-scaled height
    int guiScale;      // Current GUI scale factor

    // Break progress (from GameMode)
    float progress;

    // Tick counter for randomness
    int tickCount;

    // Random for heart shake
    std::mt19937 random;

    // Texture IDs
    GLuint guiTexture;
    GLuint iconsTexture;

    // Blit offset (for layering)
    float blitOffset;

    Gui(Minecraft* minecraft);

    void init();
    void tick();
    void render(float partialTick);

    // Draw primitives (matching Java GuiComponent)
    void fill(int x0, int y0, int x1, int y1, int color);
    void fillGradient(int x0, int y0, int x1, int y1, int colorTop, int colorBottom);
    void blit(int x, int y, int u, int v, int width, int height);

    // Draw HUD elements (matching Java Gui)
    void renderHearts();
    void renderArmor();
    void renderHotbar();
    void renderCrosshair();
    void renderDebugInfo();
    void renderChat();

    // Calculate GUI scale based on options
    void calculateScale();
    int getScaledWidth() const { return scaledWidth; }
    int getScaledHeight() const { return scaledHeight; }
    int getGuiScale() const { return guiScale; }

    // Static method for rendering items in GUI (shared by Gui and InventoryScreen)
    // Renders at position (x, y) with optional stack count display
    static void renderGuiItem(const ItemStack& item, int x, int y, float z, Font* font, TileRenderer& tileRenderer);

protected:
    void setupOrtho();
    void restoreProjection();
    void bindTexture(GLuint texture);
};

} // namespace mc
