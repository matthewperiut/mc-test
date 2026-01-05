#pragma once

#include "gui/Font.hpp"
#include <string>

struct GLFWwindow;

namespace mc {

class Minecraft;

class Screen {
public:
    Minecraft* minecraft;
    Font* font;

    int width;
    int height;

    Screen();
    virtual ~Screen() = default;

    virtual void init(Minecraft* minecraft, int width, int height);
    virtual void tick() {}
    virtual void render(int mouseX, int mouseY, float partialTick);
    virtual void removed() {}

    // Input handling
    virtual void keyPressed(int key, int scancode, int action, int mods);
    virtual void mouseClicked(int button, int action);
    virtual void mouseMoved(double x, double y);
    virtual void mouseScrolled(double xoffset, double yoffset);
    virtual void charTyped(unsigned int codepoint);

    // Helpers
    void drawCenteredString(const std::string& text, int x, int y, int color);
    void drawString(const std::string& text, int x, int y, int color);
    void fill(int x0, int y0, int x1, int y1, int color);
    void fillGradient(int x0, int y0, int x1, int y1, int colorTop, int colorBottom);

protected:
    bool pausesGame() const { return true; }
};

} // namespace mc
