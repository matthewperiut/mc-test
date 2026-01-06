#pragma once

#include <string>
#include <functional>

namespace mc {

class Font;

class Button {
public:
    int id;
    int x, y;
    int width, height;
    std::string message;
    bool active;
    bool visible;
    bool hovered;

    Button(int id, int x, int y, int width, int height, const std::string& message);
    Button(int id, int x, int y, const std::string& message);  // Default 200x20 size
    virtual ~Button() = default;

    virtual void render(Font* font, int mouseX, int mouseY);
    virtual void onClick();

    bool isMouseOver(int mouseX, int mouseY) const;
    bool isHovered() const { return hovered; }
};

} // namespace mc
