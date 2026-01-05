#include "gui/Screen.hpp"
#include "core/Minecraft.hpp"
#include "gui/Gui.hpp"
#include <GL/glew.h>

namespace mc {

Screen::Screen()
    : minecraft(nullptr)
    , font(nullptr)
    , width(0)
    , height(0)
{
}

void Screen::init(Minecraft* mc, int w, int h) {
    minecraft = mc;
    width = w;
    height = h;
    font = &minecraft->gui->font;
}

void Screen::render(int /*mouseX*/, int /*mouseY*/, float /*partialTick*/) {
    // Base render - override in subclasses
}

void Screen::keyPressed(int /*key*/, int /*scancode*/, int /*action*/, int /*mods*/) {
    // Override in subclasses
}

void Screen::mouseClicked(int /*button*/, int /*action*/) {
    // Override in subclasses
}

void Screen::mouseMoved(double /*x*/, double /*y*/) {
    // Override in subclasses
}

void Screen::mouseScrolled(double /*xoffset*/, double /*yoffset*/) {
    // Override in subclasses
}

void Screen::charTyped(unsigned int /*codepoint*/) {
    // Override in subclasses
}

void Screen::drawCenteredString(const std::string& text, int x, int y, int color) {
    if (font) {
        font->drawCentered(text, x, y, color);
    }
}

void Screen::drawString(const std::string& text, int x, int y, int color) {
    if (font) {
        font->drawShadow(text, x, y, color);
    }
}

void Screen::fill(int x0, int y0, int x1, int y1, int color) {
    float a = ((color >> 24) & 0xFF) / 255.0f;
    float r = ((color >> 16) & 0xFF) / 255.0f;
    float g = ((color >> 8) & 0xFF) / 255.0f;
    float b = (color & 0xFF) / 255.0f;

    glDisable(GL_TEXTURE_2D);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    glColor4f(r, g, b, a);
    glBegin(GL_QUADS);
    glVertex2f(static_cast<float>(x0), static_cast<float>(y1));
    glVertex2f(static_cast<float>(x1), static_cast<float>(y1));
    glVertex2f(static_cast<float>(x1), static_cast<float>(y0));
    glVertex2f(static_cast<float>(x0), static_cast<float>(y0));
    glEnd();

    glDisable(GL_BLEND);
    glEnable(GL_TEXTURE_2D);
    glColor4f(1, 1, 1, 1);
}

void Screen::fillGradient(int x0, int y0, int x1, int y1, int colorTop, int colorBottom) {
    float a1 = ((colorTop >> 24) & 0xFF) / 255.0f;
    float r1 = ((colorTop >> 16) & 0xFF) / 255.0f;
    float g1 = ((colorTop >> 8) & 0xFF) / 255.0f;
    float b1 = (colorTop & 0xFF) / 255.0f;

    float a2 = ((colorBottom >> 24) & 0xFF) / 255.0f;
    float r2 = ((colorBottom >> 16) & 0xFF) / 255.0f;
    float g2 = ((colorBottom >> 8) & 0xFF) / 255.0f;
    float b2 = (colorBottom & 0xFF) / 255.0f;

    glDisable(GL_TEXTURE_2D);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glShadeModel(GL_SMOOTH);

    glBegin(GL_QUADS);
    glColor4f(r1, g1, b1, a1);
    glVertex2f(static_cast<float>(x1), static_cast<float>(y0));
    glVertex2f(static_cast<float>(x0), static_cast<float>(y0));
    glColor4f(r2, g2, b2, a2);
    glVertex2f(static_cast<float>(x0), static_cast<float>(y1));
    glVertex2f(static_cast<float>(x1), static_cast<float>(y1));
    glEnd();

    glShadeModel(GL_FLAT);
    glDisable(GL_BLEND);
    glEnable(GL_TEXTURE_2D);
    glColor4f(1, 1, 1, 1);
}

} // namespace mc
