#include "gui/Font.hpp"
#include "renderer/Textures.hpp"
#include "renderer/Tesselator.hpp"
#include <stb_image.h>

namespace mc {

// Character mapping - same as font.txt (starts at texture index 32)
// Index 0 = space (texture pos 32), index 1 = '!' (texture pos 33), etc.
const std::string Font::acceptableLetters =
    " !\"#$%&'()*+,-./"
    "0123456789:;<=>?"
    "@ABCDEFGHIJKLMNO"
    "PQRSTUVWXYZ[\\]^_"
    "'abcdefghijklmno"
    "pqrstuvwxyz{|}~";

Font::Font()
    : fontTexture(0)
    , initialized(false)
{
    // Initialize all widths to 0
    for (int i = 0; i < 256; i++) {
        charWidths[i] = 0;
    }
}

void Font::calculateCharWidths(const unsigned char* pixels, int width, int height) {
    // Calculate character widths by scanning the font image
    // This matches Java's Font constructor exactly
    // Font texture is 128x128 with 16x16 grid of 8x8 characters

    for (int i = 0; i < 256; ++i) {
        int xt = i % 16;  // Column in character grid
        int yt = i / 16;  // Row in character grid

        // Scan from right to left to find rightmost non-empty column
        int x;
        for (x = 7; x >= 0; --x) {
            int xPixel = xt * 8 + x;
            bool emptyColumn = true;

            for (int y = 0; y < 8 && emptyColumn; ++y) {
                int yPixel = (yt * 8 + y) * width;
                // Get alpha channel (RGBA format, alpha is 4th byte)
                // Or if grayscale, just get the pixel value
                int pixelIndex = (xPixel + yPixel) * 4 + 3;  // Alpha channel
                if (pixelIndex < width * height * 4) {
                    int pixel = pixels[pixelIndex];
                    if (pixel > 0) {
                        emptyColumn = false;
                    }
                }
            }

            if (!emptyColumn) {
                break;
            }
        }

        // Space character (index 32) gets special width
        if (i == 32) {
            x = 2;
        }

        // Width = rightmost column + 2 (for spacing)
        charWidths[i] = x + 2;
    }
}

void Font::init() {
    if (initialized) return;

    // Load font image to calculate character widths
    int width, height, channels;
    unsigned char* pixels = stbi_load("resources/font/default.png", &width, &height, &channels, 4);

    if (pixels) {
        calculateCharWidths(pixels, width, height);
        stbi_image_free(pixels);
    } else {
        // Fallback: set default widths if image load fails
        for (int i = 0; i < 256; i++) {
            charWidths[i] = 6;
        }
        charWidths[32] = 4;  // Space
    }

    fontTexture = Textures::getInstance().loadTexture("resources/font/default.png");
    initialized = true;
}

void Font::destroy() {
    initialized = false;
}

void Font::draw(const std::string& text, int x, int y, int color) {
    draw(text, x, y, color, false);
}

void Font::draw(const std::string& text, int x, int y, int color, bool darken) {
    if (!initialized || text.empty()) return;

    // Apply darkening for shadow (matches Java: (color & 0xFCFCFC) >> 2)
    if (darken) {
        int oldAlpha = color & 0xFF000000;
        color = (color & 0xFCFCFC) >> 2;
        color += oldAlpha;
    }

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

    glPushMatrix();
    glTranslatef(static_cast<float>(x), static_cast<float>(y), 0.0f);

    for (size_t i = 0; i < text.length(); ++i) {
        char c = text[i];

        // Handle newlines
        if (c == '\n') {
            glPopMatrix();
            y += charHeight + 1;
            glPushMatrix();
            glTranslatef(static_cast<float>(x), static_cast<float>(y), 0.0f);
            continue;
        }

        // Find character index in acceptableLetters
        size_t charPos = acceptableLetters.find(c);
        if (charPos != std::string::npos) {
            // Texture index = position in acceptableLetters + 32
            int textureIndex = static_cast<int>(charPos) + 32;
            drawChar(textureIndex, 0, 0);
            // Advance by character width
            glTranslatef(static_cast<float>(charWidths[textureIndex]), 0.0f, 0.0f);
        }
    }

    glPopMatrix();

    glColor4f(1, 1, 1, 1);
    glDisable(GL_BLEND);
}

void Font::drawShadow(const std::string& text, int x, int y, int color) {
    // Draw shadow first (offset by 1, darkened)
    draw(text, x + 1, y + 1, color, true);
    // Draw main text
    draw(text, x, y, color, false);
}

void Font::drawCentered(const std::string& text, int x, int y, int color) {
    int w = getWidth(text);
    drawShadow(text, x - w / 2, y, color);
}

int Font::getWidth(const std::string& text) const {
    if (text.empty()) return 0;

    int width = 0;
    int maxWidth = 0;

    for (size_t i = 0; i < text.length(); ++i) {
        char c = text[i];

        if (c == '\n') {
            maxWidth = std::max(maxWidth, width);
            width = 0;
            continue;
        }

        // Find character index in acceptableLetters
        size_t charPos = acceptableLetters.find(c);
        if (charPos != std::string::npos) {
            int textureIndex = static_cast<int>(charPos) + 32;
            width += charWidths[textureIndex];
        }
    }

    return std::max(maxWidth, width);
}

void Font::drawChar(int charIndex, float x, float y) {
    // Font texture is 128x128 pixels with 16x16 grid of 8x8 characters
    int ix = (charIndex % 16) * 8;  // Pixel X position in texture
    int iy = (charIndex / 16) * 8;  // Pixel Y position in texture

    // Java uses 7.99F to avoid texture bleeding
    float s = 7.99f;

    float u0 = static_cast<float>(ix) / 128.0f;
    float v0 = static_cast<float>(iy) / 128.0f;
    float u1 = (static_cast<float>(ix) + s) / 128.0f;
    float v1 = (static_cast<float>(iy) + s) / 128.0f;

    // Match Java's vertex order exactly (using Tesselator pattern)
    glBegin(GL_QUADS);
    glTexCoord2f(u0, v1); glVertex2f(x, y + s);          // bottom-left
    glTexCoord2f(u1, v1); glVertex2f(x + s, y + s);      // bottom-right
    glTexCoord2f(u1, v0); glVertex2f(x + s, y);          // top-right
    glTexCoord2f(u0, v0); glVertex2f(x, y);              // top-left
    glEnd();
}

} // namespace mc
