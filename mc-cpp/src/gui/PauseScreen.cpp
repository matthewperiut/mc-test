#include "gui/PauseScreen.hpp"
#include "gui/OptionsScreen.hpp"
#include "core/Minecraft.hpp"
#include "audio/SoundEngine.hpp"
#include "renderer/MatrixStack.hpp"
#include "renderer/ShaderManager.hpp"
#include <GLFW/glfw3.h>

#ifdef MC_RENDERER_METAL
#include "renderer/backend/RenderDevice.hpp"
#else
#include <GL/glew.h>
#endif

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

    buttons.push_back(std::make_unique<Button>(
        BUTTON_BACK, centerX - 100, centerY + 24, 200, 20, "Back to Game"));

    buttons.push_back(std::make_unique<Button>(
        BUTTON_QUIT, centerX - 100, centerY + 48, 200, 20, "Save and quit to title"));

    buttons.push_back(std::make_unique<Button>(
        BUTTON_OPTIONS, centerX - 100, centerY + 96, 200, 20, "Options..."));
}

void PauseScreen::render(int mx, int my, float partialTick) {
    mouseX = mx;
    mouseY = my;

    // Set up 2D orthographic projection using MatrixStack
    MatrixStack::projection().push();
    MatrixStack::projection().loadIdentity();
    MatrixStack::projection().ortho(0, static_cast<float>(width), static_cast<float>(height), 0, 1000.0f, 3000.0f);

    MatrixStack::modelview().push();
    MatrixStack::modelview().loadIdentity();
    MatrixStack::modelview().translate(0.0f, 0.0f, -2000.0f);

    // Set up 2D rendering state
#ifdef MC_RENDERER_METAL
    RenderDevice::get().setDepthTest(false);
    RenderDevice::get().setCullFace(false);
    RenderDevice::get().setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);
#else
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_CULL_FACE);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
#endif

    ShaderManager::getInstance().useGuiShader();
    ShaderManager::getInstance().updateMatrices();
    ShaderManager::getInstance().setUseTexture(true);

    // Draw semi-transparent background
    fillGradient(0, 0, width, height, 0xC0101010, 0xD0101010);

    // Draw title
    drawCenteredString("Game menu", width / 2, 40, 0xFFFFFF);

    // Draw buttons
    for (auto& button : buttons) {
        button->render(font, mouseX, mouseY);
    }

    // Restore projection
    MatrixStack::projection().pop();
    MatrixStack::modelview().pop();

    // Restore 3D state
#ifdef MC_RENDERER_METAL
    RenderDevice::get().setDepthTest(true);
    RenderDevice::get().setCullFace(true);
#else
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);
#endif
}

void PauseScreen::keyPressed(int key, int scancode, int action, int mods) {
    (void)scancode;
    (void)mods;

    if (action == GLFW_PRESS) {
        if (key == GLFW_KEY_ESCAPE) {
            minecraft->closeScreen();
        }
    }
}

void PauseScreen::mouseClicked(int button, int action) {
    if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
        for (auto& btn : buttons) {
            if (btn->active && btn->isMouseOver(mouseX, mouseY)) {
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
