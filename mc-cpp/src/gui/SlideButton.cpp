#include "gui/SlideButton.hpp"
#include "gui/Font.hpp"
#include "renderer/Textures.hpp"
#include <GL/glew.h>

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
        // Calculate new value from click position
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

    // Update value based on mouse position (matching Java exactly)
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

    // Update value while sliding (matching Java's renderBg behavior)
    if (sliding) {
        value = static_cast<float>(mouseX - (x + 4)) / static_cast<float>(width - 8);
        if (value < 0.0f) value = 0.0f;
        if (value > 1.0f) value = 1.0f;
    }

    // Bind gui texture
    Textures::getInstance().bind("resources/gui/gui.png");

    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

    // Slider buttons always use yImage=0 for background (Java: getYImage returns 0)
    float texScale = 1.0f / 256.0f;
    int texY = 46;  // Always 46 for slider background
    int halfWidth = width / 2;

    // Draw button background using two halves (same as regular button but always yImage=0)
    glBegin(GL_QUADS);
    // Left half
    glTexCoord2f(0.0f, (float)texY * texScale);
    glVertex2f((float)x, (float)y);
    glTexCoord2f((float)halfWidth * texScale, (float)texY * texScale);
    glVertex2f((float)(x + halfWidth), (float)y);
    glTexCoord2f((float)halfWidth * texScale, (float)(texY + height) * texScale);
    glVertex2f((float)(x + halfWidth), (float)(y + height));
    glTexCoord2f(0.0f, (float)(texY + height) * texScale);
    glVertex2f((float)x, (float)(y + height));

    // Right half
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

    // Render slider knob (matching Java's renderBg)
    renderSliderKnob(mouseX, mouseY);

    glDisable(GL_BLEND);

    // Draw label text centered
    int textColor = active ? (hovered ? 0xFFFFA0 : 0xE0E0E0) : 0xA0A0A0;
    int textX = x + (width - font->getWidth(message)) / 2;
    int textY = y + (height - 8) / 2;
    font->drawShadow(message, textX, textY, textColor);
}

void SlideButton::renderSliderKnob(int mouseX, int mouseY) {
    (void)mouseX;
    (void)mouseY;

    // Java: blit(x + (int)(value * (w - 8)), y, 0, 66, 4, 20)
    //       blit(x + (int)(value * (w - 8)) + 4, y, 196, 66, 4, 20)
    // This draws the slider knob as two 4-pixel wide sections from texture row 66

    float texScale = 1.0f / 256.0f;
    int knobX = x + static_cast<int>(value * static_cast<float>(width - 8));
    int knobTexY = 66;

    glBegin(GL_QUADS);

    // Left part of knob: 4 pixels wide from texture (0, 66)
    glTexCoord2f(0.0f, (float)knobTexY * texScale);
    glVertex2f((float)knobX, (float)y);
    glTexCoord2f(4.0f * texScale, (float)knobTexY * texScale);
    glVertex2f((float)(knobX + 4), (float)y);
    glTexCoord2f(4.0f * texScale, (float)(knobTexY + 20) * texScale);
    glVertex2f((float)(knobX + 4), (float)(y + height));
    glTexCoord2f(0.0f, (float)(knobTexY + 20) * texScale);
    glVertex2f((float)knobX, (float)(y + height));

    // Right part of knob: 4 pixels wide from texture (196, 66)
    glTexCoord2f(196.0f * texScale, (float)knobTexY * texScale);
    glVertex2f((float)(knobX + 4), (float)y);
    glTexCoord2f(200.0f * texScale, (float)knobTexY * texScale);
    glVertex2f((float)(knobX + 8), (float)y);
    glTexCoord2f(200.0f * texScale, (float)(knobTexY + 20) * texScale);
    glVertex2f((float)(knobX + 8), (float)(y + height));
    glTexCoord2f(196.0f * texScale, (float)(knobTexY + 20) * texScale);
    glVertex2f((float)(knobX + 4), (float)(y + height));

    glEnd();
}

} // namespace mc
