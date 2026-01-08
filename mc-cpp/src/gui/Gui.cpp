#include "gui/Gui.hpp"
#include "core/Minecraft.hpp"
#include "core/Options.hpp"
#include "entity/LocalPlayer.hpp"
#include "renderer/LevelRenderer.hpp"
#include "renderer/Textures.hpp"
#include "renderer/TileRenderer.hpp"
#include "renderer/Tesselator.hpp"
#include "renderer/MatrixStack.hpp"
#include "renderer/ShaderManager.hpp"
#include "world/tile/Tile.hpp"
#include "item/Inventory.hpp"
#include "item/Item.hpp"
#include <GL/glew.h>
#include <sstream>
#include <iomanip>

namespace mc {

Gui::Gui(Minecraft* minecraft)
    : minecraft(minecraft)
    , screenWidth(854)
    , screenHeight(480)
    , scaledWidth(854)
    , scaledHeight(480)
    , guiScale(1)
    , progress(0.0f)
    , tickCount(0)
    , guiTexture(0)
    , iconsTexture(0)
    , blitOffset(0.0f)
{
}

void Gui::init() {
    font.init();
}

void Gui::tick() {
    tickCount++;
}

void Gui::calculateScale() {
    screenWidth = minecraft->screenWidth;
    screenHeight = minecraft->screenHeight;

    int scaleSetting = minecraft->options.guiScale;

    guiScale = 1;

    if (scaleSetting == 0) {
        while (guiScale < 4 &&
               screenWidth / (guiScale + 1) >= 320 &&
               screenHeight / (guiScale + 1) >= 240) {
            guiScale++;
        }
    } else {
        guiScale = scaleSetting;
    }

    while (screenWidth / guiScale < 320 || screenHeight / guiScale < 240) {
        if (guiScale <= 1) break;
        guiScale--;
    }

    scaledWidth = screenWidth / guiScale;
    scaledHeight = screenHeight / guiScale;
}

void Gui::render(float partialTick) {
    calculateScale();

    setupOrtho();

    glDisable(GL_DEPTH_TEST);
    glDisable(GL_CULL_FACE);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    ShaderManager::getInstance().useGuiShader();
    ShaderManager::getInstance().updateMatrices();
    ShaderManager::getInstance().setUseTexture(true);

    LocalPlayer* player = minecraft->player;
    if (player) {
        int selectedSlot = player->selectedSlot;

        blitOffset = -90.0f;

        Textures::getInstance().bind("resources/gui/gui.png");

        blit(scaledWidth / 2 - 91, scaledHeight - 22, 0, 0, 182, 22);
        blit(scaledWidth / 2 - 91 - 1 + selectedSlot * 20, scaledHeight - 22 - 1, 0, 22, 24, 22);

        if (player->inventory) {
            TileRenderer tileRenderer;

            for (int i = 0; i < 9; i++) {
                const ItemStack& item = player->inventory->getItem(i);
                if (item.isEmpty()) continue;

                int slotX = scaledWidth / 2 - 91 + 3 + i * 20;
                int slotY = scaledHeight - 19;

                renderGuiItem(item, slotX, slotY, blitOffset, &font, tileRenderer);
            }
        }

        glEnable(GL_CULL_FACE);
    }

    // Switch back to gui shader after inventory item rendering
    ShaderManager::getInstance().useGuiShader();
    ShaderManager::getInstance().updateMatrices();
    ShaderManager::getInstance().setUseTexture(true);

    Textures::getInstance().bind("resources/gui/icons.png");

    glEnable(GL_BLEND);
    glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_SRC_COLOR);

    blit(scaledWidth / 2 - 7, scaledHeight / 2 - 7, 0, 0, 16, 16);

    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    if (player) {
        renderHearts();
        renderArmor();
    }

    if (minecraft->showDebug) {
        renderDebugInfo();
    } else {
        font.drawShadow("Minecraft Beta 1.2_02 (C++)", 2, 2, 0xFFFFFF);
    }

    glDisable(GL_BLEND);
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);

    restoreProjection();
}

void Gui::setupOrtho() {
    MatrixStack::projection().push();
    MatrixStack::projection().loadIdentity();
    MatrixStack::projection().ortho(0, static_cast<float>(scaledWidth), static_cast<float>(scaledHeight), 0, 1000.0f, 3000.0f);

    MatrixStack::modelview().push();
    MatrixStack::modelview().loadIdentity();
    MatrixStack::modelview().translate(0.0f, 0.0f, -2000.0f);
}

void Gui::restoreProjection() {
    MatrixStack::projection().pop();
    MatrixStack::modelview().pop();
}

void Gui::bindTexture(GLuint texture) {
    glBindTexture(GL_TEXTURE_2D, texture);
}

void Gui::fill(int x0, int y0, int x1, int y1, int color) {
    float a = static_cast<float>((color >> 24) & 0xFF) / 255.0f;
    float r = static_cast<float>((color >> 16) & 0xFF) / 255.0f;
    float g = static_cast<float>((color >> 8) & 0xFF) / 255.0f;
    float b = static_cast<float>(color & 0xFF) / 255.0f;

    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    ShaderManager::getInstance().useGuiShader();
    ShaderManager::getInstance().updateMatrices();
    ShaderManager::getInstance().setUseTexture(false);

    Tesselator& t = Tesselator::getInstance();
    t.begin(GL_QUADS);
    t.color(r, g, b, a);
    t.vertex(static_cast<float>(x0), static_cast<float>(y1), 0.0f);
    t.vertex(static_cast<float>(x1), static_cast<float>(y1), 0.0f);
    t.vertex(static_cast<float>(x1), static_cast<float>(y0), 0.0f);
    t.vertex(static_cast<float>(x0), static_cast<float>(y0), 0.0f);
    t.end();

    ShaderManager::getInstance().setUseTexture(true);
    glDisable(GL_BLEND);
}

void Gui::fillGradient(int x0, int y0, int x1, int y1, int colorTop, int colorBottom) {
    float a1 = static_cast<float>((colorTop >> 24) & 0xFF) / 255.0f;
    float r1 = static_cast<float>((colorTop >> 16) & 0xFF) / 255.0f;
    float g1 = static_cast<float>((colorTop >> 8) & 0xFF) / 255.0f;
    float b1 = static_cast<float>(colorTop & 0xFF) / 255.0f;

    float a2 = static_cast<float>((colorBottom >> 24) & 0xFF) / 255.0f;
    float r2 = static_cast<float>((colorBottom >> 16) & 0xFF) / 255.0f;
    float g2 = static_cast<float>((colorBottom >> 8) & 0xFF) / 255.0f;
    float b2 = static_cast<float>(colorBottom & 0xFF) / 255.0f;

    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    ShaderManager::getInstance().useGuiShader();
    ShaderManager::getInstance().updateMatrices();
    ShaderManager::getInstance().setUseTexture(false);

    Tesselator& t = Tesselator::getInstance();
    t.begin(GL_QUADS);
    t.color(r1, g1, b1, a1);
    t.vertex(static_cast<float>(x1), static_cast<float>(y0), 0.0f);
    t.color(r1, g1, b1, a1);
    t.vertex(static_cast<float>(x0), static_cast<float>(y0), 0.0f);
    t.color(r2, g2, b2, a2);
    t.vertex(static_cast<float>(x0), static_cast<float>(y1), 0.0f);
    t.color(r2, g2, b2, a2);
    t.vertex(static_cast<float>(x1), static_cast<float>(y1), 0.0f);
    t.end();

    ShaderManager::getInstance().setUseTexture(true);
    glDisable(GL_BLEND);
}

void Gui::blit(int x, int y, int u, int v, int width, int height) {
    float us = 0.00390625f;
    float vs = 0.00390625f;

    Tesselator& t = Tesselator::getInstance();
    t.begin(GL_QUADS);
    t.color(1.0f, 1.0f, 1.0f, 1.0f);
    t.tex(static_cast<float>(u) * us, static_cast<float>(v + height) * vs);
    t.vertex(static_cast<float>(x), static_cast<float>(y + height), blitOffset);

    t.tex(static_cast<float>(u + width) * us, static_cast<float>(v + height) * vs);
    t.vertex(static_cast<float>(x + width), static_cast<float>(y + height), blitOffset);

    t.tex(static_cast<float>(u + width) * us, static_cast<float>(v) * vs);
    t.vertex(static_cast<float>(x + width), static_cast<float>(y), blitOffset);

    t.tex(static_cast<float>(u) * us, static_cast<float>(v) * vs);
    t.vertex(static_cast<float>(x), static_cast<float>(y), blitOffset);
    t.end();
}

void Gui::renderCrosshair() {
}

void Gui::renderHotbar() {
}

void Gui::renderHearts() {
    LocalPlayer* player = minecraft->player;
    if (!player) return;

    int health = player->health;

    random.seed(tickCount * 312871);

    int baseY = scaledHeight - 32;

    Textures::getInstance().bind("resources/gui/icons.png");
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    for (int i = 0; i < 10; i++) {
        int heartX = scaledWidth / 2 - 91 + i * 8;
        int heartY = baseY;

        if (health <= 4) {
            heartY += random() % 2;
        }

        blit(heartX, heartY, 16, 0, 9, 9);

        if (i * 2 + 1 < health) {
            blit(heartX, heartY, 52, 0, 9, 9);
        } else if (i * 2 + 1 == health) {
            blit(heartX, heartY, 61, 0, 9, 9);
        }
    }

    glDisable(GL_BLEND);
}

void Gui::renderArmor() {
    LocalPlayer* player = minecraft->player;
    if (!player) return;

    int armor = 0;
    if (armor <= 0) return;

    int baseY = scaledHeight - 32;

    Textures::getInstance().bind("resources/gui/icons.png");
    glEnable(GL_BLEND);

    for (int i = 0; i < 10; i++) {
        int armorX = scaledWidth / 2 + 91 - i * 8 - 9;

        if (i * 2 + 1 < armor) {
            blit(armorX, baseY, 34, 9, 9, 9);
        } else if (i * 2 + 1 == armor) {
            blit(armorX, baseY, 25, 9, 9, 9);
        } else {
            blit(armorX, baseY, 16, 9, 9, 9);
        }
    }

    glDisable(GL_BLEND);
}

void Gui::renderDebugInfo() {
    LocalPlayer* player = minecraft->player;

    std::stringstream ss;

    ss << "Minecraft Beta 1.2_02 (C++) (" << minecraft->fps << " fps)";
    font.drawShadow(ss.str(), 2, 2, 0xFFFFFF);

    if (player) {
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

        ss.str("");
        ss << std::setprecision(2);
        ss << "xRot: " << player->xRot;
        font.drawShadow(ss.str(), 2, 88, 0xE0E0E0);

        ss.str("");
        ss << "yRot: " << player->yRot;
        font.drawShadow(ss.str(), 2, 96, 0xE0E0E0);
    }

    if (minecraft->levelRenderer) {
        ss.str("");
        ss << "C: " << minecraft->levelRenderer->chunksRendered
           << "/" << minecraft->levelRenderer->chunks.size()
           << " U: " << minecraft->levelRenderer->chunksUpdated;
        font.drawShadow(ss.str(), 2, 12, 0xFFFFFF);
    }

    long totalMem = 512;
    long usedMem = 256;
    ss.str("");
    ss << "Used memory: " << (usedMem * 100 / totalMem) << "% (" << usedMem << "MB) of " << totalMem << "MB";
    int memWidth = font.getWidth(ss.str());
    font.drawShadow(ss.str(), scaledWidth - memWidth - 2, 2, 0xE0E0E0);
}

void Gui::renderChat() {
}

void Gui::renderGuiItem(const ItemStack& item, int x, int y, float z, Font* font, TileRenderer& tileRenderer) {
    if (item.isEmpty()) return;

    glEnable(GL_DEPTH_TEST);
    glClear(GL_DEPTH_BUFFER_BIT);

    if (item.id > 0 && item.id < 256) {
        Tile* tile = Tile::tiles[item.id];
        if (tile) {
            // Check if this tile can be rendered as a 3D block in GUI (matching Java ItemRenderer.renderGuiItem)
            int renderShape = static_cast<int>(tile->renderShape);
            if (TileRenderer::canRender(renderShape)) {
                // Render as 3D isometric block
                Textures::getInstance().bind("resources/terrain.png");

                MatrixStack::modelview().push();
                MatrixStack::modelview().translate(static_cast<float>(x - 2), static_cast<float>(y + 3), z);
                MatrixStack::modelview().scale(10.0f, 10.0f, 10.0f);
                MatrixStack::modelview().translate(1.0f, 0.5f, 8.0f);
                MatrixStack::modelview().rotate(-210.0f, 1.0f, 0.0f, 0.0f);
                MatrixStack::modelview().rotate(-45.0f, 0.0f, 1.0f, 0.0f);

                ShaderManager::getInstance().useWorldShader();
                ShaderManager::getInstance().updateMatrices();
                // Disable fog for GUI blocks by pushing fog very far away
                ShaderManager::getInstance().updateFog(10000.0f, 20000.0f, 1.0f, 1.0f, 1.0f);

                // renderTileForGUIWithColors handles its own begin/end cycles per face
                tileRenderer.renderTileForGUIWithColors(tile, item.getAuxValue());

                MatrixStack::modelview().pop();
            } else {
                // Render as 2D sprite from terrain.png (for torches, crosses, etc.)
                Textures::getInstance().bind("resources/terrain.png");

                int icon = tile->textureIndex;
                int sx = (icon % 16) * 16;
                int sy = (icon / 16) * 16;

                ShaderManager::getInstance().useGuiShader();
                ShaderManager::getInstance().updateMatrices();
                ShaderManager::getInstance().setUseTexture(true);

                float u0 = static_cast<float>(sx) / 256.0f;
                float u1 = static_cast<float>(sx + 16) / 256.0f;
                float v0 = static_cast<float>(sy) / 256.0f;
                float v1 = static_cast<float>(sy + 16) / 256.0f;

                Tesselator& t = Tesselator::getInstance();
                t.begin(GL_QUADS);
                t.color(1.0f, 1.0f, 1.0f, 1.0f);
                t.tex(u0, v1); t.vertex(static_cast<float>(x), static_cast<float>(y + 16), z);
                t.tex(u1, v1); t.vertex(static_cast<float>(x + 16), static_cast<float>(y + 16), z);
                t.tex(u1, v0); t.vertex(static_cast<float>(x + 16), static_cast<float>(y), z);
                t.tex(u0, v0); t.vertex(static_cast<float>(x), static_cast<float>(y), z);
                t.end();
            }
        }
    } else if (item.id >= 256) {
        Item* itemDef = Item::byId(item.id);
        if (itemDef) {
            Textures::getInstance().bind("resources/gui/items.png");

            int icon = itemDef->getIcon();
            int sx = icon % 16 * 16;
            int sy = icon / 16 * 16;

            ShaderManager::getInstance().useGuiShader();
            ShaderManager::getInstance().updateMatrices();
            ShaderManager::getInstance().setUseTexture(true);

            float u0 = static_cast<float>(sx) / 256.0f;
            float u1 = static_cast<float>(sx + 16) / 256.0f;
            float v0 = static_cast<float>(sy) / 256.0f;
            float v1 = static_cast<float>(sy + 16) / 256.0f;

            Tesselator& t = Tesselator::getInstance();
            t.begin(GL_QUADS);
            t.color(1.0f, 1.0f, 1.0f, 1.0f);
            t.tex(u0, v1); t.vertex(static_cast<float>(x), static_cast<float>(y + 16), z);
            t.tex(u1, v1); t.vertex(static_cast<float>(x + 16), static_cast<float>(y + 16), z);
            t.tex(u1, v0); t.vertex(static_cast<float>(x + 16), static_cast<float>(y), z);
            t.tex(u0, v0); t.vertex(static_cast<float>(x), static_cast<float>(y), z);
            t.end();
        }
    }

    glDisable(GL_DEPTH_TEST);

    if (item.count > 1 && font) {
        std::string countStr = std::to_string(item.count);
        int textX = x + 17 - font->getWidth(countStr);
        int textY = y + 9;
        glDisable(GL_DEPTH_TEST);
        font->drawShadow(countStr, textX, textY, 0xFFFFFF);
    }
}

} // namespace mc
