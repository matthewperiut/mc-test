#pragma once

#include <string>
#include <GL/glew.h>

namespace mc {

class Font {
public:
    Font();

    void init();
    void destroy();

    // Draw text
    void draw(const std::string& text, int x, int y, int color);
    void drawShadow(const std::string& text, int x, int y, int color);
    void drawCentered(const std::string& text, int x, int y, int color);

    // Text metrics
    int getWidth(const std::string& text) const;
    int getHeight() const { return charHeight; }

    // Character dimensions
    static constexpr int charWidth = 8;
    static constexpr int charHeight = 8;

private:
    GLuint fontTexture;
    bool initialized;

    int charWidths[256];  // Width of each character

    void drawChar(char c, float x, float y, int color);
    void loadCharWidths();
};

} // namespace mc
