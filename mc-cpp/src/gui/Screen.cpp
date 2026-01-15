#include "gui/Screen.hpp"
#include "core/Minecraft.hpp"
#include "gui/Gui.hpp"
#include "renderer/Tesselator.hpp"
#include "renderer/ShaderManager.hpp"

#ifdef MC_RENDERER_METAL
#include "renderer/backend/RenderDevice.hpp"
#else
#include <GL/glew.h>
#endif

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
}

void Screen::keyPressed(int /*key*/, int /*scancode*/, int /*action*/, int /*mods*/) {
}

void Screen::mouseClicked(int /*button*/, int /*action*/) {
}

void Screen::mouseMoved(double /*x*/, double /*y*/) {
}

void Screen::mouseScrolled(double /*xoffset*/, double /*yoffset*/) {
}

void Screen::charTyped(unsigned int /*codepoint*/) {
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

#ifdef MC_RENDERER_METAL
    RenderDevice::get().setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);
#else
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
#endif

    ShaderManager::getInstance().useGuiShader();
    ShaderManager::getInstance().updateMatrices();
    ShaderManager::getInstance().setUseTexture(false);

    Tesselator& t = Tesselator::getInstance();
    t.begin(GL_QUADS);
    t.color(r, g, b, a);
    t.vertex(static_cast<float>(x0), static_cast<float>(y1), 0.0f);
    t.vertex(static_cast<float>(x1), static_cast<float>(y1), 0.0f);
    t.vertex(static_cast<float>(x1), static_cast<float>(y0), 0.0f);
    t.vertex(static_cast<float>(x0), static_cast<float>(y0), 0.0f);
    t.end();

    ShaderManager::getInstance().setUseTexture(true);
#ifdef MC_RENDERER_METAL
    RenderDevice::get().setBlend(false);
#else
    glDisable(GL_BLEND);
#endif
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

#ifdef MC_RENDERER_METAL
    RenderDevice::get().setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);
#else
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
#endif

    ShaderManager::getInstance().useGuiShader();
    ShaderManager::getInstance().updateMatrices();
    ShaderManager::getInstance().setUseTexture(false);

    Tesselator& t = Tesselator::getInstance();
    t.begin(GL_QUADS);
    t.color(r1, g1, b1, a1);
    t.vertex(static_cast<float>(x1), static_cast<float>(y0), 0.0f);
    t.color(r1, g1, b1, a1);
    t.vertex(static_cast<float>(x0), static_cast<float>(y0), 0.0f);
    t.color(r2, g2, b2, a2);
    t.vertex(static_cast<float>(x0), static_cast<float>(y1), 0.0f);
    t.color(r2, g2, b2, a2);
    t.vertex(static_cast<float>(x1), static_cast<float>(y1), 0.0f);
    t.end();

    ShaderManager::getInstance().setUseTexture(true);
#ifdef MC_RENDERER_METAL
    RenderDevice::get().setBlend(false);
#else
    glDisable(GL_BLEND);
#endif
}

} // namespace mc
