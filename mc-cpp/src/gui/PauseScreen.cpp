#include "gui/PauseScreen.hpp"
#include "gui/OptionsScreen.hpp"
#include "core/Minecraft.hpp"
#include "audio/SoundEngine.hpp"
#include <GL/glew.h>
#include <GLFW/glfw3.h>

namespace mc {

PauseScreen::PauseScreen()
    : mouseX(0)
    , mouseY(0)
{
}

void PauseScreen::init(Minecraft* mc, int w, int h) {
    Screen::init(mc, w, h);
    initButtons();
}

void PauseScreen::initButtons() {
    buttons.clear();

    int centerX = width / 2;
    int centerY = height / 4;

    // Match Java layout exactly:
    // Back to Game at height/4 + 24
    buttons.push_back(std::make_unique<Button>(
        BUTTON_BACK, centerX - 100, centerY + 24, 200, 20, "Back to Game"));

    // Save and quit to title at height/4 + 48
    buttons.push_back(std::make_unique<Button>(
        BUTTON_QUIT, centerX - 100, centerY + 48, 200, 20, "Save and quit to title"));

    // Options at height/4 + 96
    buttons.push_back(std::make_unique<Button>(
        BUTTON_OPTIONS, centerX - 100, centerY + 96, 200, 20, "Options..."));
}

void PauseScreen::render(int mx, int my, float partialTick) {
    mouseX = mx;
    mouseY = my;

    // Set up 2D orthographic projection
    glMatrixMode(GL_PROJECTION);
    glPushMatrix();
    glLoadIdentity();
    glOrtho(0, width, height, 0, 1000.0, 3000.0);

    glMatrixMode(GL_MODELVIEW);
    glPushMatrix();
    glLoadIdentity();
    glTranslatef(0.0f, 0.0f, -2000.0f);

    // Set up 2D rendering state
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_LIGHTING);
    glDisable(GL_CULL_FACE);
    glDisable(GL_FOG);
    glEnable(GL_TEXTURE_2D);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

    // Draw semi-transparent background
    fillGradient(0, 0, width, height, 0xC0101010, 0xD0101010);

    // Draw title
    drawCenteredString("Game menu", width / 2, 40, 0xFFFFFF);

    // Draw buttons
    for (auto& button : buttons) {
        button->render(font, mouseX, mouseY);
    }

    // Restore projection
    glMatrixMode(GL_PROJECTION);
    glPopMatrix();
    glMatrixMode(GL_MODELVIEW);
    glPopMatrix();

    // Restore 3D state
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);
}

void PauseScreen::keyPressed(int key, int scancode, int action, int mods) {
    (void)scancode;
    (void)mods;

    if (action == GLFW_PRESS) {
        if (key == GLFW_KEY_ESCAPE) {
            // Return to game
            minecraft->closeScreen();
        }
    }
}

void PauseScreen::mouseClicked(int button, int action) {
    if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
        for (auto& btn : buttons) {
            if (btn->active && btn->isMouseOver(mouseX, mouseY)) {
                // Play click sound (matching Java playUI: volume * 0.25)
                SoundEngine::getInstance().playSound("random.click", 0.25f, 1.0f);
                buttonClicked(btn->id);
                break;
            }
        }
    }
}

void PauseScreen::mouseMoved(double x, double y) {
    mouseX = static_cast<int>(x);
    mouseY = static_cast<int>(y);
}

void PauseScreen::buttonClicked(int buttonId) {
    switch (buttonId) {
        case BUTTON_BACK:
            minecraft->closeScreen();
            break;

        case BUTTON_OPTIONS:
            minecraft->setScreen(new OptionsScreen(this));
            break;

        case BUTTON_QUIT:
            minecraft->running = false;
            break;
    }
}

} // namespace mc
