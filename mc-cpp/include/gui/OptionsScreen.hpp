#pragma once

#include "gui/Screen.hpp"
#include "gui/Button.hpp"
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
    int mouseX, mouseY;

    void initButtons();
    void buttonClicked(int buttonId);
    void updateButtonLabels();

    std::string getGuiScaleLabel() const;
    std::string getRenderDistanceLabel() const;
    std::string getFovLabel() const;

    static constexpr int BUTTON_DONE = 0;
    static constexpr int BUTTON_GUI_SCALE = 1;
    static constexpr int BUTTON_RENDER_DISTANCE = 2;
    static constexpr int BUTTON_FOV = 3;
    static constexpr int BUTTON_MUSIC = 4;
    static constexpr int BUTTON_SOUND = 5;
};

} // namespace mc
