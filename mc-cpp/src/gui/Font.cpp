#include "gui/Font.hpp"
#include "renderer/Textures.hpp"
#include "renderer/Tesselator.hpp"
#include "renderer/ShaderManager.hpp"
#include "renderer/backend/RenderDevice.hpp"
#include <stb_image.h>

namespace mc {

const std::string Font::acceptableLetters =
    " !\"#$%&'()*+,-./"
    "0123456789:;<=>?"
    "@ABCDEFGHIJKLMNO"
    "PQRSTUVWXYZ[\\]^_"
    "'abcdefghijklmno"
    "pqrstuvwxyz{|}~";

Font::Font()
    : fontTexture(nullptr)
    , initialized(false)
    , inBatch(false)
{
    for (int i = 0; i < 256; i++) {
        charWidths[i] = 0;
    }
}

void Font::calculateCharWidths(const unsigned char* pixels, int width, int height) {
    for (int i = 0; i < 256; ++i) {
        int xt = i % 16;
        int yt = i / 16;

        int x;
        for (x = 7; x >= 0; --x) {
            int xPixel = xt * 8 + x;
            bool emptyColumn = true;

            for (int y = 0; y < 8 && emptyColumn; ++y) {
                int yPixel = (yt * 8 + y) * width;
                int pixelIndex = (xPixel + yPixel) * 4 + 3;
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

        if (i == 32) {
            x = 2;
        }

        charWidths[i] = x + 2;
    }
}

void Font::init() {
    if (initialized) return;

    int width, height, channels;
    unsigned char* pixels = stbi_load("resources/font/default.png", &width, &height, &channels, 4);

    if (pixels) {
        calculateCharWidths(pixels, width, height);
        stbi_image_free(pixels);
    } else {
        for (int i = 0; i < 256; i++) {
            charWidths[i] = 6;
        }
        charWidths[32] = 4;
    }

    fontTexture = Textures::getInstance().loadTexture("resources/font/default.png");
    initialized = true;
}

void Font::destroy() {
    initialized = false;
    inBatch = false;
}

void Font::bindTexture() {
    if (initialized) {
        Textures::getInstance().bind(fontTexture);
    }
}

void Font::beginBatch() {
    if (!initialized) return;
    bindTexture();
    Tesselator::getInstance().begin(DrawMode::Quads);
    inBatch = true;
}

void Font::endBatch() {
    if (!inBatch) return;
    Tesselator::getInstance().end();
    inBatch = false;
}

void Font::addTextToBatch(Tesselator& t, const std::string& text, int x, int y, int color, bool darken) {
    if (text.empty()) return;

    if (darken) {
        int oldAlpha = color & 0xFF000000;
        color = (color & 0xFCFCFC) >> 2;
        color += oldAlpha;
    }

    float r = ((color >> 16) & 0xFF) / 255.0f;
    float g = ((color >> 8) & 0xFF) / 255.0f;
    float b = (color & 0xFF) / 255.0f;
    float a = ((color >> 24) & 0xFF) / 255.0f;
    if (a == 0) a = 1.0f;

    float xOffset = static_cast<float>(x);
    float yOffset = static_cast<float>(y);
    constexpr float s = 7.99f;

    for (size_t i = 0; i < text.length(); ++i) {
        char c = text[i];

        if (c == '\n') {
            xOffset = static_cast<float>(x);
            yOffset += charHeight + 1;
            continue;
        }

        size_t charPos = acceptableLetters.find(c);
        if (charPos != std::string::npos) {
            int textureIndex = static_cast<int>(charPos) + 32;

            int ix = (textureIndex % 16) * 8;
            int iy = (textureIndex / 16) * 8;

            float u0 = static_cast<float>(ix) / 128.0f;
            float v0 = static_cast<float>(iy) / 128.0f;
            float u1 = (static_cast<float>(ix) + s) / 128.0f;
            float v1 = (static_cast<float>(iy) + s) / 128.0f;

            t.color(r, g, b, a);
            t.tex(u0, v1); t.vertex(xOffset, yOffset + s, 0.0f);
            t.tex(u1, v1); t.vertex(xOffset + s, yOffset + s, 0.0f);
            t.tex(u1, v0); t.vertex(xOffset + s, yOffset, 0.0f);
            t.tex(u0, v0); t.vertex(xOffset, yOffset, 0.0f);

            xOffset += static_cast<float>(charWidths[textureIndex]);
        }
    }
}

void Font::addText(const std::string& text, int x, int y, int color, bool shadow) {
    if (!inBatch || text.empty()) return;

    Tesselator& t = Tesselator::getInstance();

    if (shadow) {
        // Add shadow first (offset by 1,1)
        addTextToBatch(t, text, x + 1, y + 1, color, true);
    }
    // Add main text
    addTextToBatch(t, text, x, y, color, false);
}

void Font::draw(const std::string& text, int x, int y, int color) {
    draw(text, x, y, color, false);
}

void Font::draw(const std::string& text, int x, int y, int color, bool darken) {
    if (!initialized || text.empty()) return;

    bindTexture();
    Tesselator& t = Tesselator::getInstance();
    t.begin(DrawMode::Quads);
    addTextToBatch(t, text, x, y, color, darken);
    t.end();
}

void Font::drawShadow(const std::string& text, int x, int y, int color) {
    if (!initialized || text.empty()) return;

    bindTexture();
    Tesselator& t = Tesselator::getInstance();
    t.begin(DrawMode::Quads);
    // Shadow first
    addTextToBatch(t, text, x + 1, y + 1, color, true);
    // Main text
    addTextToBatch(t, text, x, y, color, false);
    t.end();
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

        size_t charPos = acceptableLetters.find(c);
        if (charPos != std::string::npos) {
            int textureIndex = static_cast<int>(charPos) + 32;
            width += charWidths[textureIndex];
        }
    }

    return std::max(maxWidth, width);
}

void Font::drawChar(int charIndex, float x, float y) {
    drawChar(charIndex, x, y, 1.0f, 1.0f, 1.0f, 1.0f);
}

void Font::drawChar(int charIndex, float x, float y, float r, float g, float b, float a) {
    int ix = (charIndex % 16) * 8;
    int iy = (charIndex / 16) * 8;

    float s = 7.99f;

    float u0 = static_cast<float>(ix) / 128.0f;
    float v0 = static_cast<float>(iy) / 128.0f;
    float u1 = (static_cast<float>(ix) + s) / 128.0f;
    float v1 = (static_cast<float>(iy) + s) / 128.0f;

    Tesselator& t = Tesselator::getInstance();
    t.begin(DrawMode::Quads);
    t.color(r, g, b, a);
    t.tex(u0, v1); t.vertex(x, y + s, 0.0f);
    t.tex(u1, v1); t.vertex(x + s, y + s, 0.0f);
    t.tex(u1, v0); t.vertex(x + s, y, 0.0f);
    t.tex(u0, v0); t.vertex(x, y, 0.0f);
    t.end();
}

} // namespace mc
