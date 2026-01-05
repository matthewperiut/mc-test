#include "gui/Gui.hpp"
#include "core/Minecraft.hpp"
#include "entity/LocalPlayer.hpp"
#include "renderer/LevelRenderer.hpp"
#include "renderer/Textures.hpp"
#include <GL/glew.h>
#include <sstream>
#include <iomanip>

namespace mc {

Gui::Gui(Minecraft* minecraft)
    : minecraft(minecraft)
    , screenWidth(854)
    , screenHeight(480)
    , progress(0.0f)
    , tickCount(0)
    , guiTexture(0)
    , iconsTexture(0)
    , blitOffset(0.0f)
{
}

void Gui::init() {
    font.init();
    // Textures will be loaded on demand via Textures::getInstance()
}

void Gui::tick() {
    tickCount++;
}

void Gui::render(float partialTick) {
    screenWidth = minecraft->screenWidth;
    screenHeight = minecraft->screenHeight;

    setupOrtho();

    glDisable(GL_DEPTH_TEST);
    glDisable(GL_LIGHTING);
    glEnable(GL_BLEND);

    // === Render HUD matching Java Gui.render() exactly ===

    // Reset color
    glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

    // Bind gui.png and render hotbar
    Textures::getInstance().bind("resources/gui/gui.png");

    LocalPlayer* player = minecraft->player;
    if (player) {
        int selectedSlot = player->selectedSlot;

        blitOffset = -90.0f;

        // Hotbar background (Java: this.blit(var6 / 2 - 91, var7 - 22, 0, 0, 182, 22))
        blit(screenWidth / 2 - 91, screenHeight - 22, 0, 0, 182, 22);

        // Selection highlight (Java: this.blit(var6 / 2 - 91 - 1 + var11.selected * 20, var7 - 22 - 1, 0, 22, 24, 22))
        blit(screenWidth / 2 - 91 - 1 + selectedSlot * 20, screenHeight - 22 - 1, 0, 22, 24, 22);
    }

    // Bind icons.png for crosshair and hearts
    Textures::getInstance().bind("resources/gui/icons.png");

    // Crosshair with inverse blend (Java exact blend mode)
    glEnable(GL_BLEND);
    glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_SRC_COLOR);

    // Crosshair (Java: this.blit(var6 / 2 - 7, var7 / 2 - 7, 0, 0, 16, 16))
    blit(screenWidth / 2 - 7, screenHeight / 2 - 7, 0, 0, 16, 16);

    glDisable(GL_BLEND);

    // Render hearts and armor if player can take damage
    if (player) {
        renderHearts();
        renderArmor();
    }

    // Debug info (F3)
    if (minecraft->showDebug) {
        renderDebugInfo();
    } else {
        // Version string (Java: var8.drawShadow("Minecraft Beta 1.2_02", 2, 2, 16777215))
        font.drawShadow("Minecraft Beta 1.2_02 (C++)", 2, 2, 0xFFFFFF);
    }

    glDisable(GL_BLEND);
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

void Gui::bindTexture(GLuint texture) {
    glBindTexture(GL_TEXTURE_2D, texture);
}

void Gui::fill(int x0, int y0, int x1, int y1, int color) {
    // Match Java GuiComponent.fill() exactly
    float a = static_cast<float>((color >> 24) & 0xFF) / 255.0f;
    float r = static_cast<float>((color >> 16) & 0xFF) / 255.0f;
    float g = static_cast<float>((color >> 8) & 0xFF) / 255.0f;
    float b = static_cast<float>(color & 0xFF) / 255.0f;

    glEnable(GL_BLEND);
    glDisable(GL_TEXTURE_2D);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glColor4f(r, g, b, a);

    glBegin(GL_QUADS);
    glVertex2f(static_cast<float>(x0), static_cast<float>(y1));
    glVertex2f(static_cast<float>(x1), static_cast<float>(y1));
    glVertex2f(static_cast<float>(x1), static_cast<float>(y0));
    glVertex2f(static_cast<float>(x0), static_cast<float>(y0));
    glEnd();

    glEnable(GL_TEXTURE_2D);
    glDisable(GL_BLEND);
}

void Gui::fillGradient(int x0, int y0, int x1, int y1, int colorTop, int colorBottom) {
    // Match Java GuiComponent.fillGradient()
    float a1 = static_cast<float>((colorTop >> 24) & 0xFF) / 255.0f;
    float r1 = static_cast<float>((colorTop >> 16) & 0xFF) / 255.0f;
    float g1 = static_cast<float>((colorTop >> 8) & 0xFF) / 255.0f;
    float b1 = static_cast<float>(colorTop & 0xFF) / 255.0f;

    float a2 = static_cast<float>((colorBottom >> 24) & 0xFF) / 255.0f;
    float r2 = static_cast<float>((colorBottom >> 16) & 0xFF) / 255.0f;
    float g2 = static_cast<float>((colorBottom >> 8) & 0xFF) / 255.0f;
    float b2 = static_cast<float>(colorBottom & 0xFF) / 255.0f;

    glDisable(GL_TEXTURE_2D);
    glEnable(GL_BLEND);
    glDisable(GL_ALPHA_TEST);
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
    glEnable(GL_ALPHA_TEST);
    glEnable(GL_TEXTURE_2D);
}

void Gui::blit(int x, int y, int u, int v, int width, int height) {
    // Match Java GuiComponent.blit() exactly
    // Java uses 0.00390625 = 1/256 for texture scale
    float us = 0.00390625f;
    float vs = 0.00390625f;

    glBegin(GL_QUADS);
    glTexCoord2f(static_cast<float>(u) * us, static_cast<float>(v + height) * vs);
    glVertex3f(static_cast<float>(x), static_cast<float>(y + height), blitOffset);

    glTexCoord2f(static_cast<float>(u + width) * us, static_cast<float>(v + height) * vs);
    glVertex3f(static_cast<float>(x + width), static_cast<float>(y + height), blitOffset);

    glTexCoord2f(static_cast<float>(u + width) * us, static_cast<float>(v) * vs);
    glVertex3f(static_cast<float>(x + width), static_cast<float>(y), blitOffset);

    glTexCoord2f(static_cast<float>(u) * us, static_cast<float>(v) * vs);
    glVertex3f(static_cast<float>(x), static_cast<float>(y), blitOffset);
    glEnd();
}

void Gui::renderCrosshair() {
    // Crosshair is now rendered in main render() using blit
}

void Gui::renderHotbar() {
    // Hotbar is now rendered in main render() using blit
}

void Gui::renderHearts() {
    LocalPlayer* player = minecraft->player;
    if (!player) return;

    // Match Java Gui heart rendering exactly
    int health = player->health;
    // int lastHealth = player->health;  // Would track previous health for flash

    // Seed random for heart shake (Java: this.random.setSeed((long)(this.tickCount * 312871)))
    random.seed(tickCount * 312871);

    // Heart position (Java: int var19 = var6 / 2 - 91 + var16 * 8)
    int baseY = screenHeight - 32;

    Textures::getInstance().bind("resources/gui/icons.png");
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    for (int i = 0; i < 10; i++) {
        int heartX = screenWidth / 2 - 91 + i * 8;
        int heartY = baseY;

        // Shake hearts when low health (Java: if (var13 <= 4) var17 += this.random.nextInt(2))
        if (health <= 4) {
            heartY += random() % 2;
        }

        // Draw heart container (background)
        // Java: this.blit(var19, var17, 16 + var37 * 9, 0, 9, 9)
        // var37 is 0 normally, 1 when flashing (invulnerable)
        blit(heartX, heartY, 16, 0, 9, 9);

        // Draw filled heart
        // Java: if (var16 * 2 + 1 < var13) this.blit(var19, var17, 52, 0, 9, 9)
        if (i * 2 + 1 < health) {
            // Full heart
            blit(heartX, heartY, 52, 0, 9, 9);
        } else if (i * 2 + 1 == health) {
            // Half heart
            // Java: this.blit(var19, var17, 61, 0, 9, 9)
            blit(heartX, heartY, 61, 0, 9, 9);
        }
    }

    glDisable(GL_BLEND);
}

void Gui::renderArmor() {
    LocalPlayer* player = minecraft->player;
    if (!player) return;

    // Java armor rendering from icons.png
    // For now, simplified - would need armor tracking
    int armor = 0;  // player->getArmor() if implemented
    if (armor <= 0) return;

    int baseY = screenHeight - 32;

    Textures::getInstance().bind("resources/gui/icons.png");
    glEnable(GL_BLEND);

    for (int i = 0; i < 10; i++) {
        int armorX = screenWidth / 2 + 91 - i * 8 - 9;

        if (i * 2 + 1 < armor) {
            // Full armor icon (Java: this.blit(var18, var17, 34, 9, 9, 9))
            blit(armorX, baseY, 34, 9, 9, 9);
        } else if (i * 2 + 1 == armor) {
            // Half armor (Java: this.blit(var18, var17, 25, 9, 9, 9))
            blit(armorX, baseY, 25, 9, 9, 9);
        } else {
            // Empty armor slot (Java: this.blit(var18, var17, 16, 9, 9, 9))
            blit(armorX, baseY, 16, 9, 9, 9);
        }
    }

    glDisable(GL_BLEND);
}

void Gui::renderDebugInfo() {
    LocalPlayer* player = minecraft->player;

    // Match Java debug screen format exactly
    std::stringstream ss;

    // Java: var8.drawShadow("Minecraft Beta 1.2_02 (" + this.minecraft.fpsString + ")", 2, 2, 16777215)
    ss << "Minecraft Beta 1.2_02 (C++) (" << minecraft->fps << " fps)";
    font.drawShadow(ss.str(), 2, 2, 0xFFFFFF);

    if (player) {
        // Position info
        ss.str("");
        ss << std::fixed << std::setprecision(5);
        ss << "x: " << player->x;
        font.drawShadow(ss.str(), 2, 64, 0xE0E0E0);

        ss.str("");
        ss << "y: " << player->y;
        font.drawShadow(ss.str(), 2, 72, 0xE0E0E0);

        ss.str("");
        ss << "z: " << player->z;
        font.drawShadow(ss.str(), 2, 80, 0xE0E0E0);

        // Rotation info
        ss.str("");
        ss << std::setprecision(2);
        ss << "xRot: " << player->xRot;
        font.drawShadow(ss.str(), 2, 88, 0xE0E0E0);

        ss.str("");
        ss << "yRot: " << player->yRot;
        font.drawShadow(ss.str(), 2, 96, 0xE0E0E0);
    }

    // Chunk info
    if (minecraft->levelRenderer) {
        ss.str("");
        ss << "C: " << minecraft->levelRenderer->chunksRendered
           << "/" << minecraft->levelRenderer->chunks.size()
           << " U: " << minecraft->levelRenderer->chunksUpdated;
        font.drawShadow(ss.str(), 2, 12, 0xFFFFFF);
    }

    // Memory info (right side, like Java)
    long totalMem = 512;  // Placeholder - would get from system
    long usedMem = 256;   // Placeholder
    ss.str("");
    ss << "Used memory: " << (usedMem * 100 / totalMem) << "% (" << usedMem << "MB) of " << totalMem << "MB";
    int memWidth = font.getWidth(ss.str());
    font.drawShadow(ss.str(), screenWidth - memWidth - 2, 2, 0xE0E0E0);
}

void Gui::renderChat() {
    // Chat messages would be rendered here
}

} // namespace mc
