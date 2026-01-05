#include "gui/Font.hpp"
#include "renderer/Textures.hpp"
#include "renderer/Tesselator.hpp"

namespace mc {

Font::Font()
    : fontTexture(0)
    , initialized(false)
{
    loadCharWidths();
}

void Font::loadCharWidths() {
    // Default character widths (most are 6 pixels for Minecraft font)
    for (int i = 0; i < 256; i++) {
        charWidths[i] = 6;
    }

    // Space is narrower
    charWidths[' '] = 4;

    // Some punctuation
    charWidths['.'] = 2;
    charWidths[','] = 2;
    charWidths['!'] = 2;
    charWidths['\''] = 2;
    charWidths[':'] = 2;
    charWidths[';'] = 2;
    charWidths['|'] = 2;
    charWidths['i'] = 2;
    charWidths['l'] = 3;
    charWidths['I'] = 4;

    // Wider characters
    charWidths['@'] = 7;
    charWidths['~'] = 7;
}

void Font::init() {
    if (initialized) return;

    fontTexture = Textures::getInstance().loadTexture("resources/default.png");
    initialized = true;
}

void Font::destroy() {
    // Texture is managed by Textures class
    initialized = false;
}

void Font::draw(const std::string& text, int x, int y, int color) {
    if (!initialized) return;

    Textures::getInstance().bind(fontTexture);

    glEnable(GL_TEXTURE_2D);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    float r = ((color >> 16) & 0xFF) / 255.0f;
    float g = ((color >> 8) & 0xFF) / 255.0f;
    float b = (color & 0xFF) / 255.0f;
    float a = ((color >> 24) & 0xFF) / 255.0f;
    if (a == 0) a = 1.0f;

    glColor4f(r, g, b, a);

    float xPos = static_cast<float>(x);
    float yPos = static_cast<float>(y);

    for (char c : text) {
        if (c == '\n') {
            xPos = static_cast<float>(x);
            yPos += charHeight + 1;
            continue;
        }

        drawChar(c, xPos, yPos, color);
        xPos += charWidths[static_cast<unsigned char>(c)] + 1;
    }

    glColor4f(1, 1, 1, 1);
    glDisable(GL_BLEND);
}

void Font::drawShadow(const std::string& text, int x, int y, int color) {
    // Draw shadow (darker version offset by 1 pixel)
    int shadowColor = (color & 0xFF000000) |
                      ((color & 0xFCFCFC) >> 2);  // Darken by shifting
    draw(text, x + 1, y + 1, shadowColor);

    // Draw main text
    draw(text, x, y, color);
}

void Font::drawCentered(const std::string& text, int x, int y, int color) {
    int width = getWidth(text);
    drawShadow(text, x - width / 2, y, color);
}

int Font::getWidth(const std::string& text) const {
    int width = 0;
    int maxWidth = 0;

    for (char c : text) {
        if (c == '\n') {
            maxWidth = std::max(maxWidth, width);
            width = 0;
            continue;
        }
        width += charWidths[static_cast<unsigned char>(c)] + 1;
    }

    return std::max(maxWidth, width);
}

void Font::drawChar(char c, float x, float y, int /*color*/) {
    unsigned char uc = static_cast<unsigned char>(c);

    // Font texture is 16x16 characters
    int charX = uc % 16;
    int charY = uc / 16;

    float u0 = charX / 16.0f;
    float v0 = charY / 16.0f;
    float u1 = (charX + 1) / 16.0f;
    float v1 = (charY + 1) / 16.0f;

    float w = static_cast<float>(charWidth);
    float h = static_cast<float>(charHeight);

    glBegin(GL_QUADS);
    glTexCoord2f(u0, v0); glVertex2f(x, y);
    glTexCoord2f(u0, v1); glVertex2f(x, y + h);
    glTexCoord2f(u1, v1); glVertex2f(x + w, y + h);
    glTexCoord2f(u1, v0); glVertex2f(x + w, y);
    glEnd();
}

} // namespace mc
