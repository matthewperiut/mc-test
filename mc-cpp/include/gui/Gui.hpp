#pragma once

#include "gui/Font.hpp"
#include <GL/glew.h>
#include <random>

namespace mc {

class Minecraft;

class Gui {
public:
    Minecraft* minecraft;
    Font font;

    int screenWidth;
    int screenHeight;

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

protected:
    void setupOrtho();
    void restoreProjection();
    void bindTexture(GLuint texture);
};

} // namespace mc
