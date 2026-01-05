#include "gui/Gui.hpp"
#include "core/Minecraft.hpp"
#include "entity/LocalPlayer.hpp"
#include "renderer/LevelRenderer.hpp"
#include <GL/glew.h>
#include <sstream>
#include <iomanip>

namespace mc {

Gui::Gui(Minecraft* minecraft)
    : minecraft(minecraft)
    , screenWidth(854)
    , screenHeight(480)
{
}

void Gui::init() {
    font.init();
}

void Gui::render(float /*partialTick*/) {
    screenWidth = minecraft->screenWidth;
    screenHeight = minecraft->screenHeight;

    setupOrtho();

    glDisable(GL_DEPTH_TEST);
    glDisable(GL_LIGHTING);

    // Render HUD elements
    renderCrosshair();
    renderHotbar();
    renderHearts();

    if (minecraft->showDebug) {
        renderDebugInfo();
    }

    glEnable(GL_DEPTH_TEST);

    restoreProjection();
}

void Gui::setupOrtho() {
    glMatrixMode(GL_PROJECTION);
    glPushMatrix();
    glLoadIdentity();
    glOrtho(0, screenWidth, screenHeight, 0, -1, 1);

    glMatrixMode(GL_MODELVIEW);
    glPushMatrix();
    glLoadIdentity();
}

void Gui::restoreProjection() {
    glMatrixMode(GL_PROJECTION);
    glPopMatrix();
    glMatrixMode(GL_MODELVIEW);
    glPopMatrix();
}

void Gui::fill(int x0, int y0, int x1, int y1, int color) {
    float a = ((color >> 24) & 0xFF) / 255.0f;
    float r = ((color >> 16) & 0xFF) / 255.0f;
    float g = ((color >> 8) & 0xFF) / 255.0f;
    float b = (color & 0xFF) / 255.0f;

    glDisable(GL_TEXTURE_2D);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    glColor4f(r, g, b, a);
    glBegin(GL_QUADS);
    glVertex2f(static_cast<float>(x0), static_cast<float>(y1));
    glVertex2f(static_cast<float>(x1), static_cast<float>(y1));
    glVertex2f(static_cast<float>(x1), static_cast<float>(y0));
    glVertex2f(static_cast<float>(x0), static_cast<float>(y0));
    glEnd();

    glDisable(GL_BLEND);
    glEnable(GL_TEXTURE_2D);
    glColor4f(1, 1, 1, 1);
}

void Gui::fillGradient(int x0, int y0, int x1, int y1, int colorTop, int colorBottom) {
    float a1 = ((colorTop >> 24) & 0xFF) / 255.0f;
    float r1 = ((colorTop >> 16) & 0xFF) / 255.0f;
    float g1 = ((colorTop >> 8) & 0xFF) / 255.0f;
    float b1 = (colorTop & 0xFF) / 255.0f;

    float a2 = ((colorBottom >> 24) & 0xFF) / 255.0f;
    float r2 = ((colorBottom >> 16) & 0xFF) / 255.0f;
    float g2 = ((colorBottom >> 8) & 0xFF) / 255.0f;
    float b2 = (colorBottom & 0xFF) / 255.0f;

    glDisable(GL_TEXTURE_2D);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glShadeModel(GL_SMOOTH);

    glBegin(GL_QUADS);
    glColor4f(r1, g1, b1, a1);
    glVertex2f(static_cast<float>(x1), static_cast<float>(y0));
    glVertex2f(static_cast<float>(x0), static_cast<float>(y0));
    glColor4f(r2, g2, b2, a2);
    glVertex2f(static_cast<float>(x0), static_cast<float>(y1));
    glVertex2f(static_cast<float>(x1), static_cast<float>(y1));
    glEnd();

    glShadeModel(GL_FLAT);
    glDisable(GL_BLEND);
    glEnable(GL_TEXTURE_2D);
    glColor4f(1, 1, 1, 1);
}

void Gui::drawTexture(int x, int y, int u, int v, int width, int height) {
    float texW = 256.0f;  // GUI texture size
    float texH = 256.0f;

    float u0 = u / texW;
    float v0 = v / texH;
    float u1 = (u + width) / texW;
    float v1 = (v + height) / texH;

    glBegin(GL_QUADS);
    glTexCoord2f(u0, v0); glVertex2f(static_cast<float>(x), static_cast<float>(y));
    glTexCoord2f(u0, v1); glVertex2f(static_cast<float>(x), static_cast<float>(y + height));
    glTexCoord2f(u1, v1); glVertex2f(static_cast<float>(x + width), static_cast<float>(y + height));
    glTexCoord2f(u1, v0); glVertex2f(static_cast<float>(x + width), static_cast<float>(y));
    glEnd();
}

void Gui::renderCrosshair() {
    int cx = screenWidth / 2;
    int cy = screenHeight / 2;

    glEnable(GL_BLEND);
    glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_SRC_COLOR);

    glDisable(GL_TEXTURE_2D);
    glColor4f(1, 1, 1, 1);

    int size = 10;
    int thickness = 1;

    // Horizontal line
    glBegin(GL_QUADS);
    glVertex2f(static_cast<float>(cx - size), static_cast<float>(cy - thickness));
    glVertex2f(static_cast<float>(cx - size), static_cast<float>(cy + thickness));
    glVertex2f(static_cast<float>(cx + size), static_cast<float>(cy + thickness));
    glVertex2f(static_cast<float>(cx + size), static_cast<float>(cy - thickness));
    glEnd();

    // Vertical line
    glBegin(GL_QUADS);
    glVertex2f(static_cast<float>(cx - thickness), static_cast<float>(cy - size));
    glVertex2f(static_cast<float>(cx - thickness), static_cast<float>(cy + size));
    glVertex2f(static_cast<float>(cx + thickness), static_cast<float>(cy + size));
    glVertex2f(static_cast<float>(cx + thickness), static_cast<float>(cy - size));
    glEnd();

    glEnable(GL_TEXTURE_2D);
    glDisable(GL_BLEND);
}

void Gui::renderHotbar() {
    // Simplified hotbar rendering
    int hotbarWidth = 182;
    int hotbarHeight = 22;
    int x = (screenWidth - hotbarWidth) / 2;
    int y = screenHeight - hotbarHeight - 2;

    // Draw hotbar background
    fill(x, y, x + hotbarWidth, y + hotbarHeight, 0x80000000);
}

void Gui::renderHearts() {
    LocalPlayer* player = minecraft->player;
    if (!player) return;

    int hearts = player->health;
    int maxHearts = player->maxHealth;

    int x = screenWidth / 2 - 91;
    int y = screenHeight - 39;

    glDisable(GL_TEXTURE_2D);

    // Draw heart containers (empty)
    for (int i = 0; i < maxHearts / 2; i++) {
        fill(x + i * 8, y, x + i * 8 + 7, y + 7, 0xFF333333);
    }

    // Draw filled hearts
    for (int i = 0; i < hearts / 2; i++) {
        fill(x + i * 8 + 1, y + 1, x + i * 8 + 6, y + 6, 0xFFFF0000);
    }

    // Half heart
    if (hearts % 2 == 1) {
        int i = hearts / 2;
        fill(x + i * 8 + 1, y + 1, x + i * 8 + 3, y + 6, 0xFFFF0000);
    }

    glEnable(GL_TEXTURE_2D);
}

void Gui::renderDebugInfo() {
    LocalPlayer* player = minecraft->player;

    std::stringstream ss;
    ss << "Minecraft C++ Port";
    font.drawShadow(ss.str(), 2, 2, 0xFFFFFF);

    ss.str("");
    ss << std::fixed << std::setprecision(1);
    ss << "FPS: " << minecraft->fps;
    font.drawShadow(ss.str(), 2, 12, 0xFFFFFF);

    if (player) {
        ss.str("");
        ss << "XYZ: " << std::setprecision(2)
           << player->x << " / "
           << player->y << " / "
           << player->z;
        font.drawShadow(ss.str(), 2, 22, 0xFFFFFF);

        ss.str("");
        ss << "Chunk: " << static_cast<int>(player->x) / 16 << " "
           << static_cast<int>(player->y) / 16 << " "
           << static_cast<int>(player->z) / 16;
        font.drawShadow(ss.str(), 2, 32, 0xFFFFFF);
    }

    if (minecraft->levelRenderer) {
        ss.str("");
        ss << "Chunks: " << minecraft->levelRenderer->chunksRendered
           << "/" << minecraft->levelRenderer->chunks.size();
        font.drawShadow(ss.str(), 2, 42, 0xFFFFFF);
    }
}

void Gui::renderChat() {
    // Chat messages would be rendered here
}

} // namespace mc
