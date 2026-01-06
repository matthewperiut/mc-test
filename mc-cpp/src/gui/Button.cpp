#include "gui/Button.hpp"
#include "gui/Font.hpp"
#include "renderer/Textures.hpp"
#include <GL/glew.h>

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

    // Bind gui texture for button background
    Textures::getInstance().bind("resources/gui/gui.png");

    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    // Java uses full white color - no tinting
    glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

    // Get button texture Y offset based on state (matching Java getYImage)
    // yImage: 0 = disabled, 1 = normal, 2 = hovered
    // Texture Y position: 46 + yImage * 20
    int yImage = 1;  // Normal
    if (!active) {
        yImage = 0;  // Disabled
    } else if (hovered) {
        yImage = 2;  // Hovered
    }

    float texScale = 1.0f / 256.0f;
    int texY = 46 + yImage * 20;
    int halfWidth = width / 2;

    // Draw button using two halves (matching Java blit calls)
    // Left half: blit(x, y, 0, 46 + yImage * 20, w/2, h)
    glBegin(GL_QUADS);
    glTexCoord2f(0.0f, (float)texY * texScale);
    glVertex2f((float)x, (float)y);
    glTexCoord2f((float)halfWidth * texScale, (float)texY * texScale);
    glVertex2f((float)(x + halfWidth), (float)y);
    glTexCoord2f((float)halfWidth * texScale, (float)(texY + height) * texScale);
    glVertex2f((float)(x + halfWidth), (float)(y + height));
    glTexCoord2f(0.0f, (float)(texY + height) * texScale);
    glVertex2f((float)x, (float)(y + height));

    // Right half: blit(x + w/2, y, 200 - w/2, 46 + yImage * 20, w/2, h)
    int texX2 = 200 - halfWidth;
    glTexCoord2f((float)texX2 * texScale, (float)texY * texScale);
    glVertex2f((float)(x + halfWidth), (float)y);
    glTexCoord2f(200.0f * texScale, (float)texY * texScale);
    glVertex2f((float)(x + width), (float)y);
    glTexCoord2f(200.0f * texScale, (float)(texY + height) * texScale);
    glVertex2f((float)(x + width), (float)(y + height));
    glTexCoord2f((float)texX2 * texScale, (float)(texY + height) * texScale);
    glVertex2f((float)(x + halfWidth), (float)(y + height));
    glEnd();

    glDisable(GL_BLEND);

    // Draw button text centered (matching Java colors)
    // Disabled: -6250336 = 0xFFA0A0A0, Hovered: 16777120 = 0xFFFFA0, Normal: 14737632 = 0xE0E0E0
    int textColor;
    if (!active) {
        textColor = 0xA0A0A0;  // Gray for disabled
    } else if (hovered) {
        textColor = 0xFFFFA0;  // Yellow for hovered
    } else {
        textColor = 0xE0E0E0;  // Light gray for normal
    }

    int textX = x + (width - font->getWidth(message)) / 2;
    int textY = y + (height - 8) / 2;
    font->drawShadow(message, textX, textY, textColor);
}

void Button::onClick() {
    // Override in subclasses or handle via callback
}

} // namespace mc
