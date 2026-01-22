#pragma once

#include <string>
#include "renderer/Textures.hpp"

namespace mc {

class Tesselator;

class Font {
public:
    Font();

    void init();
    void destroy();

    // Draw text (standalone - binds texture, begins/ends batch)
    void draw(const std::string& text, int x, int y, int color);
    void draw(const std::string& text, int x, int y, int color, bool darken);
    void drawShadow(const std::string& text, int x, int y, int color);
    void drawCentered(const std::string& text, int x, int y, int color);

    // Batch rendering - for drawing multiple strings in one draw call
    // Usage: beginBatch(); addText(...); addText(...); endBatch();
    void beginBatch();
    void addText(const std::string& text, int x, int y, int color, bool shadow = false);
    void endBatch();

    // Text metrics
    int getWidth(const std::string& text) const;
    int getHeight() const { return charHeight; }

    // Bind font texture (for external batch rendering)
    void bindTexture();

    // Character dimensions
    static constexpr int charWidth = 8;
    static constexpr int charHeight = 8;

    // Acceptable characters string (maps char to texture position)
    static const std::string acceptableLetters;

private:
    TextureHandle fontTexture;
    bool initialized;
    bool inBatch;

    int charWidths[256];  // Width of each character

    void drawChar(int charIndex, float x, float y);
    void drawChar(int charIndex, float x, float y, float r, float g, float b, float a);
    void calculateCharWidths(const unsigned char* pixels, int width, int height);

    // Internal: add text to current batch (no begin/end)
    void addTextToBatch(Tesselator& t, const std::string& text, int x, int y, int color, bool darken);
};

} // namespace mc
