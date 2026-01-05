#pragma once

#include "gui/Font.hpp"

namespace mc {

class Minecraft;

class Gui {
public:
    Minecraft* minecraft;
    Font font;

    int screenWidth;
    int screenHeight;

    Gui(Minecraft* minecraft);

    void init();
    void render(float partialTick);

    // Draw primitives
    void fill(int x0, int y0, int x1, int y1, int color);
    void fillGradient(int x0, int y0, int x1, int y1, int colorTop, int colorBottom);
    void drawTexture(int x, int y, int u, int v, int width, int height);

    // Draw HUD elements
    void renderHearts();
    void renderHotbar();
    void renderCrosshair();
    void renderDebugInfo();
    void renderChat();

protected:
    void setupOrtho();
    void restoreProjection();
};

} // namespace mc
