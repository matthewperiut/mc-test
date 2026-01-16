#include "gui/Font.hpp"
#include "renderer/Textures.hpp"
#include "renderer/Tesselator.hpp"
#include "renderer/MatrixStack.hpp"
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
}

void Font::draw(const std::string& text, int x, int y, int color) {
    draw(text, x, y, color, false);
}

void Font::draw(const std::string& text, int x, int y, int color, bool darken) {
    if (!initialized || text.empty()) return;

    if (darken) {
        int oldAlpha = color & 0xFF000000;
        color = (color & 0xFCFCFC) >> 2;
        color += oldAlpha;
    }

    Textures::getInstance().bind(fontTexture);

    RenderDevice::get().setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);

    float r = ((color >> 16) & 0xFF) / 255.0f;
    float g = ((color >> 8) & 0xFF) / 255.0f;
    float b = (color & 0xFF) / 255.0f;
    float a = ((color >> 24) & 0xFF) / 255.0f;
    if (a == 0) a = 1.0f;

    ShaderManager::getInstance().useGuiShader();
    ShaderManager::getInstance().setUseTexture(true);

    MatrixStack::modelview().push();
    MatrixStack::modelview().translate(static_cast<float>(x), static_cast<float>(y), 0.0f);
    ShaderManager::getInstance().updateMatrices();

    float xOffset = 0.0f;
    int currentY = y;

    for (size_t i = 0; i < text.length(); ++i) {
        char c = text[i];

        if (c == '\n') {
            xOffset = 0.0f;
            currentY += charHeight + 1;
            MatrixStack::modelview().pop();
            MatrixStack::modelview().push();
            MatrixStack::modelview().translate(static_cast<float>(x), static_cast<float>(currentY), 0.0f);
            ShaderManager::getInstance().updateMatrices();
            continue;
        }

        size_t charPos = acceptableLetters.find(c);
        if (charPos != std::string::npos) {
            int textureIndex = static_cast<int>(charPos) + 32;
            drawChar(textureIndex, xOffset, 0, r, g, b, a);
            xOffset += static_cast<float>(charWidths[textureIndex]);
        }
    }

    MatrixStack::modelview().pop();

    RenderDevice::get().setBlend(false);
}

void Font::drawShadow(const std::string& text, int x, int y, int color) {
    draw(text, x + 1, y + 1, color, true);
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
