#pragma once

#include "gui/Screen.hpp"
#include "gui/Button.hpp"
#include "gui/SlideButton.hpp"
#include <vector>
#include <memory>

namespace mc {

class OptionsScreen : public Screen {
public:
    OptionsScreen(Screen* parent);
    virtual ~OptionsScreen() = default;

    void init(Minecraft* minecraft, int width, int height) override;
    void render(int mouseX, int mouseY, float partialTick) override;
    void removed() override;
    void keyPressed(int key, int scancode, int action, int mods) override;
    void mouseClicked(int button, int action) override;
    void mouseMoved(double x, double y) override;

private:
    Screen* parentScreen;
    std::vector<std::unique_ptr<Button>> buttons;
    // Pointers to slider buttons for easy access (owned by buttons vector)
    SlideButton* musicSlider = nullptr;
    SlideButton* soundSlider = nullptr;
    SlideButton* sensitivitySlider = nullptr;
    int mouseX, mouseY;

    void initButtons();
    void buttonClicked(int buttonId);
    void updateButtonLabels();

    // Label generators matching Java Option enum
    std::string getMusicLabel() const;
    std::string getSoundLabel() const;
    std::string getInvertMouseLabel() const;
    std::string getSensitivityLabel() const;
    std::string getRenderDistanceLabel() const;
    std::string getViewBobbingLabel() const;
    std::string getAnaglyphLabel() const;
    std::string getVsyncLabel() const;
    std::string getDifficultyLabel() const;
    std::string getGraphicsLabel() const;

    // Button IDs matching Java Option.ordinal() order
    // Progress options (sliders in Java, cycle buttons here for now)
    static constexpr int BUTTON_MUSIC = 0;
    static constexpr int BUTTON_SOUND = 1;
    static constexpr int BUTTON_INVERT_MOUSE = 2;
    static constexpr int BUTTON_SENSITIVITY = 3;
    static constexpr int BUTTON_RENDER_DISTANCE = 4;
    static constexpr int BUTTON_VIEW_BOBBING = 5;
    static constexpr int BUTTON_ANAGLYPH = 6;
    static constexpr int BUTTON_VSYNC = 7;
    static constexpr int BUTTON_DIFFICULTY = 8;
    static constexpr int BUTTON_GRAPHICS = 9;
    // Special buttons
    static constexpr int BUTTON_CONTROLS = 100;
    static constexpr int BUTTON_DONE = 200;
};

} // namespace mc
