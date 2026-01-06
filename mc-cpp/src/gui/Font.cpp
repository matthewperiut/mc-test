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
    // Default character widths - Java uses rightmost_column + 2
    // Most characters use 6 columns (0-5), so width = 5 + 2 = 7
    for (int i = 0; i < 256; i++) {
        charWidths[i] = 6;
    }

    // Space (character 32) - Java hardcodes this to 4
    charWidths[' '] = 4;

    // Digits 0-9 typically use 5 columns (0-4), width = 4 + 2 = 6
    for (int i = '0'; i <= '9'; i++) {
        charWidths[i] = 6;
    }
    // '1' is narrower
    charWidths['1'] = 6;

    // Punctuation - narrower characters
    charWidths['!'] = 3;  // Uses 1 column, width = 0 + 2 = 2, but +1 for visible
    charWidths['.'] = 3;
    charWidths[','] = 3;
    charWidths['\''] = 4;
    charWidths[':'] = 3;
    charWidths[';'] = 3;
    charWidths['`'] = 4;
    charWidths['|'] = 4;

    // Lowercase narrow characters
    charWidths['i'] = 4;
    charWidths['l'] = 4;
    charWidths['t'] = 5;

    // Uppercase narrow
    charWidths['I'] = 5;

    // Wider characters
    charWidths['@'] = 8;
    charWidths['~'] = 8;
    charWidths['m'] = 7;
    charWidths['w'] = 7;
    charWidths['M'] = 7;
    charWidths['W'] = 7;
}

void Font::init() {
    if (initialized) return;

    fontTexture = Textures::getInstance().loadTexture("resources/font/default.png");
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
        // Java's charWidths already include spacing (rightmost_column + 2)
        xPos += charWidths[static_cast<unsigned char>(c)];
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
        width += charWidths[static_cast<unsigned char>(c)];
    }

    return std::max(maxWidth, width);
}

void Font::drawChar(char c, float x, float y, int /*color*/) {
    unsigned char uc = static_cast<unsigned char>(c);

    // Font texture is 128x128 pixels with 16x16 grid of 8x8 characters
    // Match Java's calculation exactly
    int ix = (uc % 16) * 8;  // Pixel X position in 128px texture
    int iy = (uc / 16) * 8;  // Pixel Y position in 128px texture

    // Java uses 7.99F to avoid texture bleeding at edges
    float s = 7.99f;

    float u0 = static_cast<float>(ix) / 128.0f;
    float v0 = static_cast<float>(iy) / 128.0f;
    float u1 = (static_cast<float>(ix) + s) / 128.0f;
    float v1 = (static_cast<float>(iy) + s) / 128.0f;

    // Match Java's vertex order exactly
    glBegin(GL_QUADS);
    glTexCoord2f(u0, v1); glVertex2f(x, y + s);          // bottom-left
    glTexCoord2f(u1, v1); glVertex2f(x + s, y + s);      // bottom-right
    glTexCoord2f(u1, v0); glVertex2f(x + s, y);          // top-right
    glTexCoord2f(u0, v0); glVertex2f(x, y);              // top-left
    glEnd();
}

} // namespace mc
