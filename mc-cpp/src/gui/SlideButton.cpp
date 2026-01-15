#include "gui/SlideButton.hpp"
#include "gui/Font.hpp"
#include "renderer/Textures.hpp"
#include "renderer/Tesselator.hpp"
#include "renderer/ShaderManager.hpp"

#ifdef MC_RENDERER_METAL
#include "renderer/backend/RenderDevice.hpp"
#else
#include <GL/glew.h>
#endif

namespace mc {

SlideButton::SlideButton(int id, int x, int y, int width, int height,
                         const std::string& message, float initialValue)
    : Button(id, x, y, width, height, message)
    , value(initialValue)
    , sliding(false)
{
}

SlideButton::SlideButton(int id, int x, int y, const std::string& message, float initialValue)
    : SlideButton(id, x, y, 150, 20, message, initialValue)
{
}

bool SlideButton::mousePressed(int mouseX, int mouseY) {
    if (!active || !visible) return false;

    if (isMouseOver(mouseX, mouseY)) {
        value = static_cast<float>(mouseX - (x + 4)) / static_cast<float>(width - 8);
        if (value < 0.0f) value = 0.0f;
        if (value > 1.0f) value = 1.0f;
        sliding = true;
        return true;
    }
    return false;
}

void SlideButton::mouseDragged(int mouseX, int mouseY) {
    if (!sliding) return;

    value = static_cast<float>(mouseX - (x + 4)) / static_cast<float>(width - 8);
    if (value < 0.0f) value = 0.0f;
    if (value > 1.0f) value = 1.0f;
}

void SlideButton::mouseReleased() {
    sliding = false;
}

void SlideButton::render(Font* font, int mouseX, int mouseY) {
    if (!visible) return;

    hovered = isMouseOver(mouseX, mouseY);

    if (sliding) {
        value = static_cast<float>(mouseX - (x + 4)) / static_cast<float>(width - 8);
        if (value < 0.0f) value = 0.0f;
        if (value > 1.0f) value = 1.0f;
    }

    Textures::getInstance().bind("resources/gui/gui.png");

#ifdef MC_RENDERER_METAL
    RenderDevice::get().setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);
#else
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
#endif

    ShaderManager::getInstance().useGuiShader();
    ShaderManager::getInstance().updateMatrices();
    ShaderManager::getInstance().setUseTexture(true);

    float texScale = 1.0f / 256.0f;
    int texY = 46;
    int halfWidth = width / 2;

    Tesselator& t = Tesselator::getInstance();

    // Draw button background using two halves
    t.begin(GL_QUADS);
    t.color(1.0f, 1.0f, 1.0f, 1.0f);

    // Left half
    t.tex(0.0f, static_cast<float>(texY) * texScale);
    t.vertex(static_cast<float>(x), static_cast<float>(y), 0.0f);

    t.tex(static_cast<float>(halfWidth) * texScale, static_cast<float>(texY) * texScale);
    t.vertex(static_cast<float>(x + halfWidth), static_cast<float>(y), 0.0f);

    t.tex(static_cast<float>(halfWidth) * texScale, static_cast<float>(texY + height) * texScale);
    t.vertex(static_cast<float>(x + halfWidth), static_cast<float>(y + height), 0.0f);

    t.tex(0.0f, static_cast<float>(texY + height) * texScale);
    t.vertex(static_cast<float>(x), static_cast<float>(y + height), 0.0f);

    // Right half
    int texX2 = 200 - halfWidth;
    t.tex(static_cast<float>(texX2) * texScale, static_cast<float>(texY) * texScale);
    t.vertex(static_cast<float>(x + halfWidth), static_cast<float>(y), 0.0f);

    t.tex(200.0f * texScale, static_cast<float>(texY) * texScale);
    t.vertex(static_cast<float>(x + width), static_cast<float>(y), 0.0f);

    t.tex(200.0f * texScale, static_cast<float>(texY + height) * texScale);
    t.vertex(static_cast<float>(x + width), static_cast<float>(y + height), 0.0f);

    t.tex(static_cast<float>(texX2) * texScale, static_cast<float>(texY + height) * texScale);
    t.vertex(static_cast<float>(x + halfWidth), static_cast<float>(y + height), 0.0f);

    t.end();

    // Render slider knob
    renderSliderKnob(mouseX, mouseY);

#ifdef MC_RENDERER_METAL
    RenderDevice::get().setBlend(false);
#else
    glDisable(GL_BLEND);
#endif

    // Draw label text centered
    int textColor = active ? (hovered ? 0xFFFFA0 : 0xE0E0E0) : 0xA0A0A0;
    int textX = x + (width - font->getWidth(message)) / 2;
    int textY = y + (height - 8) / 2;
    font->drawShadow(message, textX, textY, textColor);
}

void SlideButton::renderSliderKnob(int mouseX, int mouseY) {
    (void)mouseX;
    (void)mouseY;

    float texScale = 1.0f / 256.0f;
    int knobX = x + static_cast<int>(value * static_cast<float>(width - 8));
    int knobTexY = 66;

    Tesselator& t = Tesselator::getInstance();
    t.begin(GL_QUADS);
    t.color(1.0f, 1.0f, 1.0f, 1.0f);

    // Left part of knob: 4 pixels wide from texture (0, 66)
    t.tex(0.0f, static_cast<float>(knobTexY) * texScale);
    t.vertex(static_cast<float>(knobX), static_cast<float>(y), 0.0f);

    t.tex(4.0f * texScale, static_cast<float>(knobTexY) * texScale);
    t.vertex(static_cast<float>(knobX + 4), static_cast<float>(y), 0.0f);

    t.tex(4.0f * texScale, static_cast<float>(knobTexY + 20) * texScale);
    t.vertex(static_cast<float>(knobX + 4), static_cast<float>(y + height), 0.0f);

    t.tex(0.0f, static_cast<float>(knobTexY + 20) * texScale);
    t.vertex(static_cast<float>(knobX), static_cast<float>(y + height), 0.0f);

    // Right part of knob: 4 pixels wide from texture (196, 66)
    t.tex(196.0f * texScale, static_cast<float>(knobTexY) * texScale);
    t.vertex(static_cast<float>(knobX + 4), static_cast<float>(y), 0.0f);

    t.tex(200.0f * texScale, static_cast<float>(knobTexY) * texScale);
    t.vertex(static_cast<float>(knobX + 8), static_cast<float>(y), 0.0f);

    t.tex(200.0f * texScale, static_cast<float>(knobTexY + 20) * texScale);
    t.vertex(static_cast<float>(knobX + 8), static_cast<float>(y + height), 0.0f);

    t.tex(196.0f * texScale, static_cast<float>(knobTexY + 20) * texScale);
    t.vertex(static_cast<float>(knobX + 4), static_cast<float>(y + height), 0.0f);

    t.end();
}

} // namespace mc
