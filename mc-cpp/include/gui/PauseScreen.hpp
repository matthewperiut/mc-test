#pragma once

#include "gui/Screen.hpp"
#include "gui/Button.hpp"
#include <vector>
#include <memory>

namespace mc {

class PauseScreen : public Screen {
public:
    PauseScreen();
    virtual ~PauseScreen() = default;

    void init(Minecraft* minecraft, int width, int height) override;
    void render(int mouseX, int mouseY, float partialTick) override;
    void keyPressed(int key, int scancode, int action, int mods) override;
    void mouseClicked(int button, int action) override;
    void mouseMoved(double x, double y) override;

private:
    std::vector<std::unique_ptr<Button>> buttons;
    int mouseX, mouseY;

    void initButtons();
    void buttonClicked(int buttonId);

    static constexpr int BUTTON_BACK = 0;
    static constexpr int BUTTON_OPTIONS = 1;
    static constexpr int BUTTON_QUIT = 2;
};

} // namespace mc
