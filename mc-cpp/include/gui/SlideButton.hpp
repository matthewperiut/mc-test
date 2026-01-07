#pragma once

#include "gui/Button.hpp"

namespace mc {

// Slider button for progress-based options (music, sound, sensitivity)
// Matches Java's SlideButton behavior from b1.2_02
class SlideButton : public Button {
public:
    float value;      // Current value (0.0 - 1.0)
    bool sliding;     // Whether user is currently dragging the slider

    SlideButton(int id, int x, int y, int width, int height,
                const std::string& message, float initialValue);
    SlideButton(int id, int x, int y, const std::string& message, float initialValue);

    void render(Font* font, int mouseX, int mouseY) override;

    // Call when mouse is pressed - returns true if slider was clicked
    bool mousePressed(int mouseX, int mouseY);

    // Call when mouse is dragged while sliding
    void mouseDragged(int mouseX, int mouseY);

    // Call when mouse is released
    void mouseReleased();

private:
    void renderSliderKnob(int mouseX, int mouseY);
};

} // namespace mc
