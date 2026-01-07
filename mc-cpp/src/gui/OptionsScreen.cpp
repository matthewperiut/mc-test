#include "gui/OptionsScreen.hpp"
#include "gui/PauseScreen.hpp"
#include "gui/Gui.hpp"
#include "core/Minecraft.hpp"
#include "core/Options.hpp"
#include "renderer/LevelRenderer.hpp"
#include <GL/glew.h>
#include <GLFW/glfw3.h>
#include <cmath>

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
    musicSlider = nullptr;
    soundSlider = nullptr;
    sensitivitySlider = nullptr;

    int centerX = width / 2;
    int startY = height / 6;

    // Layout matching Java: Options.Option enum order
    // Position formula: x = width/2 - 155 + (id % 2) * 160
    //                   y = height/6 + 24 * (id / 2)

    // Row 0: Music (0) and Sound (1) - SLIDERS
    auto music = std::make_unique<SlideButton>(
        BUTTON_MUSIC, centerX - 155, startY + 0, 150, 20,
        getMusicLabel(), minecraft->options.music);
    musicSlider = music.get();
    buttons.push_back(std::move(music));

    auto sound = std::make_unique<SlideButton>(
        BUTTON_SOUND, centerX + 5, startY + 0, 150, 20,
        getSoundLabel(), minecraft->options.sound);
    soundSlider = sound.get();
    buttons.push_back(std::move(sound));

    // Row 1: Invert Mouse (2) and Sensitivity (3) - Sensitivity is SLIDER
    buttons.push_back(std::make_unique<Button>(
        BUTTON_INVERT_MOUSE, centerX - 155, startY + 24, 150, 20, getInvertMouseLabel()));

    auto sensitivity = std::make_unique<SlideButton>(
        BUTTON_SENSITIVITY, centerX + 5, startY + 24, 150, 20,
        getSensitivityLabel(), minecraft->options.mouseSensitivity);
    sensitivitySlider = sensitivity.get();
    buttons.push_back(std::move(sensitivity));

    // Row 2: Render Distance (4) and View Bobbing (5)
    buttons.push_back(std::make_unique<Button>(
        BUTTON_RENDER_DISTANCE, centerX - 155, startY + 48, 150, 20, getRenderDistanceLabel()));
    buttons.push_back(std::make_unique<Button>(
        BUTTON_VIEW_BOBBING, centerX + 5, startY + 48, 150, 20, getViewBobbingLabel()));

    // Row 3: 3D Anaglyph (6) and Limit Framerate (7)
    buttons.push_back(std::make_unique<Button>(
        BUTTON_ANAGLYPH, centerX - 155, startY + 72, 150, 20, getAnaglyphLabel()));
    buttons.push_back(std::make_unique<Button>(
        BUTTON_LIMIT_FRAMERATE, centerX + 5, startY + 72, 150, 20, getLimitFramerateLabel()));

    // Row 4: Difficulty (8) and Graphics (9)
    buttons.push_back(std::make_unique<Button>(
        BUTTON_DIFFICULTY, centerX - 155, startY + 96, 150, 20, getDifficultyLabel()));
    buttons.push_back(std::make_unique<Button>(
        BUTTON_GRAPHICS, centerX + 5, startY + 96, 150, 20, getGraphicsLabel()));

    // Controls button (Java: height/6 + 120 + 12 = height/6 + 132)
    auto controlsBtn = std::make_unique<Button>(
        BUTTON_CONTROLS, centerX - 100, startY + 132, 200, 20, "Controls...");
    controlsBtn->active = false;  // Non-functional for now
    buttons.push_back(std::move(controlsBtn));

    // Done button (Java: height/6 + 168)
    buttons.push_back(std::make_unique<Button>(
        BUTTON_DONE, centerX - 100, startY + 168, 200, 20, "Done"));
}

void OptionsScreen::updateButtonLabels() {
    for (auto& btn : buttons) {
        switch (btn->id) {
            case BUTTON_MUSIC:
                btn->message = getMusicLabel();
                break;
            case BUTTON_SOUND:
                btn->message = getSoundLabel();
                break;
            case BUTTON_INVERT_MOUSE:
                btn->message = getInvertMouseLabel();
                break;
            case BUTTON_SENSITIVITY:
                btn->message = getSensitivityLabel();
                break;
            case BUTTON_RENDER_DISTANCE:
                btn->message = getRenderDistanceLabel();
                break;
            case BUTTON_VIEW_BOBBING:
                btn->message = getViewBobbingLabel();
                break;
            case BUTTON_ANAGLYPH:
                btn->message = getAnaglyphLabel();
                break;
            case BUTTON_LIMIT_FRAMERATE:
                btn->message = getLimitFramerateLabel();
                break;
            case BUTTON_DIFFICULTY:
                btn->message = getDifficultyLabel();
                break;
            case BUTTON_GRAPHICS:
                btn->message = getGraphicsLabel();
                break;
        }
    }
}

// Label generators matching Java's Options.getMessage()

std::string OptionsScreen::getMusicLabel() const {
    float value = minecraft->options.music;
    if (value == 0.0f) return "Music: OFF";
    return "Music: " + std::to_string(static_cast<int>(value * 100.0f)) + "%";
}

std::string OptionsScreen::getSoundLabel() const {
    float value = minecraft->options.sound;
    if (value == 0.0f) return "Sound: OFF";
    return "Sound: " + std::to_string(static_cast<int>(value * 100.0f)) + "%";
}

std::string OptionsScreen::getInvertMouseLabel() const {
    return minecraft->options.invertYMouse ? "Invert Mouse: ON" : "Invert Mouse: OFF";
}

std::string OptionsScreen::getSensitivityLabel() const {
    float value = minecraft->options.mouseSensitivity;
    if (value == 0.0f) return "Sensitivity: *yawn*";
    if (value == 1.0f) return "Sensitivity: HYPER";
    return "Sensitivity: " + std::to_string(static_cast<int>(value * 200.0f)) + "%";
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

std::string OptionsScreen::getViewBobbingLabel() const {
    return minecraft->options.viewBobbing ? "View Bobbing: ON" : "View Bobbing: OFF";
}

std::string OptionsScreen::getAnaglyphLabel() const {
    return minecraft->options.anaglyph3d ? "3D Anaglyph: ON" : "3D Anaglyph: OFF";
}

std::string OptionsScreen::getLimitFramerateLabel() const {
    return minecraft->options.limitFramerate ? "Limit Framerate: ON" : "Limit Framerate: OFF";
}

std::string OptionsScreen::getDifficultyLabel() const {
    switch (minecraft->options.difficulty) {
        case 0: return "Difficulty: Peaceful";
        case 1: return "Difficulty: Easy";
        case 2: return "Difficulty: Normal";
        case 3: return "Difficulty: Hard";
        default: return "Difficulty: Normal";
    }
}

std::string OptionsScreen::getGraphicsLabel() const {
    return minecraft->options.fancyGraphics ? "Graphics: Fancy" : "Graphics: Fast";
}

void OptionsScreen::render(int mx, int my, float partialTick) {
    (void)partialTick;
    mouseX = mx;
    mouseY = my;

    // Update slider values and labels while dragging
    if (musicSlider && musicSlider->sliding) {
        minecraft->options.music = musicSlider->value;
        musicSlider->message = getMusicLabel();
    }
    if (soundSlider && soundSlider->sliding) {
        minecraft->options.sound = soundSlider->value;
        soundSlider->message = getSoundLabel();
    }
    if (sensitivitySlider && sensitivitySlider->sliding) {
        minecraft->options.mouseSensitivity = sensitivitySlider->value;
        sensitivitySlider->message = getSensitivityLabel();
    }

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
    if (button == GLFW_MOUSE_BUTTON_LEFT) {
        if (action == GLFW_PRESS) {
            // Check sliders first (they handle their own clicking)
            if (musicSlider && musicSlider->mousePressed(mouseX, mouseY)) {
                minecraft->options.music = musicSlider->value;
                musicSlider->message = getMusicLabel();
                return;
            }
            if (soundSlider && soundSlider->mousePressed(mouseX, mouseY)) {
                minecraft->options.sound = soundSlider->value;
                soundSlider->message = getSoundLabel();
                return;
            }
            if (sensitivitySlider && sensitivitySlider->mousePressed(mouseX, mouseY)) {
                minecraft->options.mouseSensitivity = sensitivitySlider->value;
                sensitivitySlider->message = getSensitivityLabel();
                return;
            }

            // Check regular buttons
            for (auto& btn : buttons) {
                if (btn->active && btn->isMouseOver(mouseX, mouseY)) {
                    // Skip sliders (already handled above)
                    if (btn->id == BUTTON_MUSIC || btn->id == BUTTON_SOUND || btn->id == BUTTON_SENSITIVITY) {
                        continue;
                    }
                    buttonClicked(btn->id);
                    break;
                }
            }
        } else if (action == GLFW_RELEASE) {
            // Release all sliders
            if (musicSlider) musicSlider->mouseReleased();
            if (soundSlider) soundSlider->mouseReleased();
            if (sensitivitySlider) sensitivitySlider->mouseReleased();
        }
    }
}

void OptionsScreen::mouseMoved(double x, double y) {
    mouseX = static_cast<int>(x);
    mouseY = static_cast<int>(y);

    // Update sliders while dragging
    if (musicSlider && musicSlider->sliding) {
        musicSlider->mouseDragged(mouseX, mouseY);
        minecraft->options.music = musicSlider->value;
        musicSlider->message = getMusicLabel();
    }
    if (soundSlider && soundSlider->sliding) {
        soundSlider->mouseDragged(mouseX, mouseY);
        minecraft->options.sound = soundSlider->value;
        soundSlider->message = getSoundLabel();
    }
    if (sensitivitySlider && sensitivitySlider->sliding) {
        sensitivitySlider->mouseDragged(mouseX, mouseY);
        minecraft->options.mouseSensitivity = sensitivitySlider->value;
        sensitivitySlider->message = getSensitivityLabel();
    }
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

        case BUTTON_CONTROLS:
            // TODO: Open controls screen
            // minecraft->setScreen(new ControlsScreen(this, &minecraft->options));
            break;

        case BUTTON_INVERT_MOUSE:
            minecraft->options.invertYMouse = !minecraft->options.invertYMouse;
            updateButtonLabels();
            break;

        case BUTTON_RENDER_DISTANCE:
            // Cycle: Far -> Normal -> Short -> Tiny -> Far
            minecraft->options.renderDistance = (minecraft->options.renderDistance + 1) % 4;
            updateButtonLabels();
            break;

        case BUTTON_VIEW_BOBBING:
            minecraft->options.viewBobbing = !minecraft->options.viewBobbing;
            updateButtonLabels();
            break;

        case BUTTON_ANAGLYPH:
            minecraft->options.anaglyph3d = !minecraft->options.anaglyph3d;
            // TODO: Reload textures for anaglyph mode
            // minecraft->textures.reloadAll();
            updateButtonLabels();
            break;

        case BUTTON_LIMIT_FRAMERATE:
            minecraft->options.limitFramerate = !minecraft->options.limitFramerate;
            updateButtonLabels();
            break;

        case BUTTON_DIFFICULTY:
            // Cycle: Peaceful -> Easy -> Normal -> Hard -> Peaceful
            minecraft->options.difficulty = (minecraft->options.difficulty + 1) % 4;
            updateButtonLabels();
            break;

        case BUTTON_GRAPHICS:
            // Toggle Fancy/Fast graphics
            minecraft->options.fancyGraphics = !minecraft->options.fancyGraphics;
            // Rebuild chunks when graphics mode changes (like Java)
            if (minecraft->levelRenderer) {
                minecraft->levelRenderer->allChanged();
            }
            updateButtonLabels();
            break;
    }
}

} // namespace mc
