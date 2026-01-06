#include "gui/OptionsScreen.hpp"
#include "gui/PauseScreen.hpp"
#include "gui/Gui.hpp"
#include "core/Minecraft.hpp"
#include "core/Options.hpp"
#include <GL/glew.h>
#include <GLFW/glfw3.h>

namespace mc {

OptionsScreen::OptionsScreen(Screen* parent)
    : parentScreen(parent)
    , mouseX(0)
    , mouseY(0)
{
}

void OptionsScreen::init(Minecraft* mc, int w, int h) {
    Screen::init(mc, w, h);
    initButtons();
}

void OptionsScreen::initButtons() {
    buttons.clear();

    int centerX = width / 2;
    int startY = height / 6;

    // Row 1: Music and Sound (left and right)
    buttons.push_back(std::make_unique<Button>(
        BUTTON_MUSIC, centerX - 155, startY + 24, 150, 20,
        minecraft->options.music ? "Music: ON" : "Music: OFF"));

    buttons.push_back(std::make_unique<Button>(
        BUTTON_SOUND, centerX + 5, startY + 24, 150, 20,
        minecraft->options.sound ? "Sound: ON" : "Sound: OFF"));

    // Row 2: Render Distance and GUI Scale
    buttons.push_back(std::make_unique<Button>(
        BUTTON_RENDER_DISTANCE, centerX - 155, startY + 48, 150, 20,
        getRenderDistanceLabel()));

    buttons.push_back(std::make_unique<Button>(
        BUTTON_GUI_SCALE, centerX + 5, startY + 48, 150, 20,
        getGuiScaleLabel()));

    // Row 3: FOV
    buttons.push_back(std::make_unique<Button>(
        BUTTON_FOV, centerX - 155, startY + 72, 150, 20,
        getFovLabel()));

    // Done button at bottom
    buttons.push_back(std::make_unique<Button>(
        BUTTON_DONE, centerX - 100, startY + 168, 200, 20, "Done"));
}

void OptionsScreen::updateButtonLabels() {
    for (auto& btn : buttons) {
        switch (btn->id) {
            case BUTTON_MUSIC:
                btn->message = minecraft->options.music ? "Music: ON" : "Music: OFF";
                break;
            case BUTTON_SOUND:
                btn->message = minecraft->options.sound ? "Sound: ON" : "Sound: OFF";
                break;
            case BUTTON_RENDER_DISTANCE:
                btn->message = getRenderDistanceLabel();
                break;
            case BUTTON_GUI_SCALE:
                btn->message = getGuiScaleLabel();
                break;
            case BUTTON_FOV:
                btn->message = getFovLabel();
                break;
        }
    }
}

std::string OptionsScreen::getGuiScaleLabel() const {
    switch (minecraft->options.guiScale) {
        case 0: return "GUI Scale: Auto";
        case 1: return "GUI Scale: Small";
        case 2: return "GUI Scale: Normal";
        case 3: return "GUI Scale: Large";
        default: return "GUI Scale: Auto";
    }
}

std::string OptionsScreen::getRenderDistanceLabel() const {
    switch (minecraft->options.renderDistance) {
        case 0: return "Render Distance: Far";
        case 1: return "Render Distance: Normal";
        case 2: return "Render Distance: Short";
        case 3: return "Render Distance: Tiny";
        default: return "Render Distance: Normal";
    }
}

std::string OptionsScreen::getFovLabel() const {
    int fov = static_cast<int>(minecraft->options.fov);
    if (fov == 70) return "FOV: Normal";
    if (fov >= 110) return "FOV: Quake Pro";
    return "FOV: " + std::to_string(fov);
}

void OptionsScreen::render(int mx, int my, float partialTick) {
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
    drawCenteredString("Options", width / 2, 20, 0xFFFFFF);

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

void OptionsScreen::removed() {
    // Save options when leaving screen
    minecraft->options.save("options.txt");
}

void OptionsScreen::keyPressed(int key, int scancode, int action, int mods) {
    (void)scancode;
    (void)mods;

    if (action == GLFW_PRESS) {
        if (key == GLFW_KEY_ESCAPE) {
            minecraft->options.save("options.txt");
            if (parentScreen) {
                minecraft->setScreen(new PauseScreen());
            } else {
                minecraft->closeScreen();
            }
        }
    }
}

void OptionsScreen::mouseClicked(int button, int action) {
    if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
        for (auto& btn : buttons) {
            if (btn->active && btn->isMouseOver(mouseX, mouseY)) {
                buttonClicked(btn->id);
                break;
            }
        }
    }
}

void OptionsScreen::mouseMoved(double x, double y) {
    mouseX = static_cast<int>(x);
    mouseY = static_cast<int>(y);
}

void OptionsScreen::buttonClicked(int buttonId) {
    switch (buttonId) {
        case BUTTON_DONE:
            minecraft->options.save("options.txt");
            if (parentScreen) {
                minecraft->setScreen(new PauseScreen());
            } else {
                minecraft->closeScreen();
            }
            break;

        case BUTTON_GUI_SCALE:
            // Cycle through 0-3 (Auto, Small, Normal, Large)
            minecraft->options.guiScale = (minecraft->options.guiScale + 1) % 4;
            // Reinitialize screen with new scaled dimensions
            minecraft->gui->calculateScale();
            width = minecraft->gui->getScaledWidth();
            height = minecraft->gui->getScaledHeight();
            initButtons();
            break;

        case BUTTON_RENDER_DISTANCE:
            // Cycle through 0-3 (Far, Normal, Short, Tiny)
            minecraft->options.renderDistance = (minecraft->options.renderDistance + 1) % 4;
            updateButtonLabels();
            break;

        case BUTTON_FOV:
            // Cycle FOV: 70 -> 80 -> 90 -> 100 -> 110 -> 70
            minecraft->options.fov += 10.0f;
            if (minecraft->options.fov > 110.0f) {
                minecraft->options.fov = 70.0f;
            }
            updateButtonLabels();
            break;

        case BUTTON_MUSIC:
            minecraft->options.music = !minecraft->options.music;
            updateButtonLabels();
            break;

        case BUTTON_SOUND:
            minecraft->options.sound = !minecraft->options.sound;
            updateButtonLabels();
            break;
    }
}

} // namespace mc
