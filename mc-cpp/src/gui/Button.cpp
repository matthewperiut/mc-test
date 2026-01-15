#include "gui/Button.hpp"
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

Button::Button(int id, int x, int y, int width, int height, const std::string& message)
    : id(id)
    , x(x)
    , y(y)
    , width(width)
    , height(height)
    , message(message)
    , active(true)
    , visible(true)
    , hovered(false)
{
}

Button::Button(int id, int x, int y, const std::string& message)
    : Button(id, x, y, 200, 20, message)
{
}

bool Button::isMouseOver(int mouseX, int mouseY) const {
    return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
}

void Button::render(Font* font, int mouseX, int mouseY) {
    if (!visible) return;

    hovered = isMouseOver(mouseX, mouseY);

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

    int yImage = 1;
    if (!active) {
        yImage = 0;
    } else if (hovered) {
        yImage = 2;
    }

    float texScale = 1.0f / 256.0f;
    int texY = 46 + yImage * 20;
    int halfWidth = width / 2;

    Tesselator& t = Tesselator::getInstance();

    // Draw button using two halves
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

#ifdef MC_RENDERER_METAL
    RenderDevice::get().setBlend(false);
#else
    glDisable(GL_BLEND);
#endif

    int textColor;
    if (!active) {
        textColor = 0xA0A0A0;
    } else if (hovered) {
        textColor = 0xFFFFA0;
    } else {
        textColor = 0xE0E0E0;
    }

    int textX = x + (width - font->getWidth(message)) / 2;
    int textY = y + (height - 8) / 2;
    font->drawShadow(message, textX, textY, textColor);
}

void Button::onClick() {
}

} // namespace mc
