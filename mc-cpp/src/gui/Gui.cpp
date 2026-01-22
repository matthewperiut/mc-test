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
#include "renderer/backend/RenderDevice.hpp"
#include "world/tile/Tile.hpp"
#include "item/Inventory.hpp"
#include "item/Item.hpp"
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

    auto& device = RenderDevice::get();
    device.setDepthTest(false);
    device.setCullFace(false);
    device.setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);

    ShaderManager::getInstance().useGuiShader();
    ShaderManager::getInstance().updateMatrices();
    ShaderManager::getInstance().setUseTexture(true);

    LocalPlayer* player = minecraft->player;
    if (player) {
        int selectedSlot = player->selectedSlot;

        blitOffset = -90.0f;

        // Batch hotbar rendering (2 blits into 1 draw call)
        Textures::getInstance().bind("resources/gui/gui.png");
        Tesselator& t = Tesselator::getInstance();
        t.begin(DrawMode::Quads);

        float us = 0.00390625f;
        float vs = 0.00390625f;

        // Hotbar background
        int hx = scaledWidth / 2 - 91, hy = scaledHeight - 22;
        t.color(1.0f, 1.0f, 1.0f, 1.0f);
        t.tex(0.0f, 22.0f * vs); t.vertex(static_cast<float>(hx), static_cast<float>(hy + 22), blitOffset);
        t.tex(182.0f * us, 22.0f * vs); t.vertex(static_cast<float>(hx + 182), static_cast<float>(hy + 22), blitOffset);
        t.tex(182.0f * us, 0.0f); t.vertex(static_cast<float>(hx + 182), static_cast<float>(hy), blitOffset);
        t.tex(0.0f, 0.0f); t.vertex(static_cast<float>(hx), static_cast<float>(hy), blitOffset);

        // Selection highlight
        int sx = scaledWidth / 2 - 91 - 1 + selectedSlot * 20, sy = scaledHeight - 22 - 1;
        t.tex(0.0f, 44.0f * vs); t.vertex(static_cast<float>(sx), static_cast<float>(sy + 22), blitOffset);
        t.tex(24.0f * us, 44.0f * vs); t.vertex(static_cast<float>(sx + 24), static_cast<float>(sy + 22), blitOffset);
        t.tex(24.0f * us, 22.0f * vs); t.vertex(static_cast<float>(sx + 24), static_cast<float>(sy), blitOffset);
        t.tex(0.0f, 22.0f * vs); t.vertex(static_cast<float>(sx), static_cast<float>(sy), blitOffset);

        t.end();

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

        device.setCullFace(true);
    }

    // Switch back to gui shader after inventory item rendering
    ShaderManager::getInstance().useGuiShader();
    ShaderManager::getInstance().updateMatrices();
    ShaderManager::getInstance().setUseTexture(true);

    // Crosshair and hearts/armor all use icons.png - batch where possible
    Textures::getInstance().bind("resources/gui/icons.png");

    // Crosshair (special blend mode)
    device.setBlend(true, BlendFactor::OneMinusDstColor, BlendFactor::OneMinusSrcColor);
    blit(scaledWidth / 2 - 7, scaledHeight / 2 - 7, 0, 0, 16, 16);

    // Hearts and armor (normal blend mode, same texture)
    device.setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);

    if (player) {
        renderHearts();
        renderArmor();
    }

    // Text rendering (uses font texture)
    if (minecraft->showDebug) {
        renderDebugInfo();
    } else {
        font.drawShadow("Minecraft Beta 1.2_02 (C++)", 2, 2, 0xFFFFFF);
    }

    device.setBlend(false);
    device.setDepthTest(true);
    device.setCullFace(true);

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
    (void)texture;
    // Legacy function - textures are now bound through Textures::getInstance().bind()
}

void Gui::fill(int x0, int y0, int x1, int y1, int color) {
    float a = static_cast<float>((color >> 24) & 0xFF) / 255.0f;
    float r = static_cast<float>((color >> 16) & 0xFF) / 255.0f;
    float g = static_cast<float>((color >> 8) & 0xFF) / 255.0f;
    float b = static_cast<float>(color & 0xFF) / 255.0f;

    RenderDevice::get().setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);

    ShaderManager::getInstance().useGuiShader();
    ShaderManager::getInstance().updateMatrices();
    ShaderManager::getInstance().setUseTexture(false);

    Tesselator& t = Tesselator::getInstance();
    t.begin(DrawMode::Quads);
    t.color(r, g, b, a);
    t.vertex(static_cast<float>(x0), static_cast<float>(y1), 0.0f);
    t.vertex(static_cast<float>(x1), static_cast<float>(y1), 0.0f);
    t.vertex(static_cast<float>(x1), static_cast<float>(y0), 0.0f);
    t.vertex(static_cast<float>(x0), static_cast<float>(y0), 0.0f);
    t.end();

    ShaderManager::getInstance().setUseTexture(true);
    RenderDevice::get().setBlend(false);
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

    RenderDevice::get().setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);

    ShaderManager::getInstance().useGuiShader();
    ShaderManager::getInstance().updateMatrices();
    ShaderManager::getInstance().setUseTexture(false);

    Tesselator& t = Tesselator::getInstance();
    t.begin(DrawMode::Quads);
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
    RenderDevice::get().setBlend(false);
}

void Gui::blit(int x, int y, int u, int v, int width, int height) {
    float us = 0.00390625f;
    float vs = 0.00390625f;

    Tesselator& t = Tesselator::getInstance();
    t.begin(DrawMode::Quads);
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

    // Batch all heart blits into a single draw call
    Tesselator& t = Tesselator::getInstance();
    t.begin(DrawMode::Quads);

    float us = 0.00390625f;
    float vs = 0.00390625f;

    for (int i = 0; i < 10; i++) {
        int heartX = scaledWidth / 2 - 91 + i * 8;
        int heartY = baseY;

        if (health <= 4) {
            heartY += random() % 2;
        }

        // Background heart
        int u = 16, v = 0, w = 9, h = 9;
        t.color(1.0f, 1.0f, 1.0f, 1.0f);
        t.tex(static_cast<float>(u) * us, static_cast<float>(v + h) * vs);
        t.vertex(static_cast<float>(heartX), static_cast<float>(heartY + h), blitOffset);
        t.tex(static_cast<float>(u + w) * us, static_cast<float>(v + h) * vs);
        t.vertex(static_cast<float>(heartX + w), static_cast<float>(heartY + h), blitOffset);
        t.tex(static_cast<float>(u + w) * us, static_cast<float>(v) * vs);
        t.vertex(static_cast<float>(heartX + w), static_cast<float>(heartY), blitOffset);
        t.tex(static_cast<float>(u) * us, static_cast<float>(v) * vs);
        t.vertex(static_cast<float>(heartX), static_cast<float>(heartY), blitOffset);

        // Full or half heart overlay
        if (i * 2 + 1 < health) {
            u = 52; // Full heart
        } else if (i * 2 + 1 == health) {
            u = 61; // Half heart
        } else {
            continue; // No overlay needed
        }

        t.tex(static_cast<float>(u) * us, static_cast<float>(v + h) * vs);
        t.vertex(static_cast<float>(heartX), static_cast<float>(heartY + h), blitOffset);
        t.tex(static_cast<float>(u + w) * us, static_cast<float>(v + h) * vs);
        t.vertex(static_cast<float>(heartX + w), static_cast<float>(heartY + h), blitOffset);
        t.tex(static_cast<float>(u + w) * us, static_cast<float>(v) * vs);
        t.vertex(static_cast<float>(heartX + w), static_cast<float>(heartY), blitOffset);
        t.tex(static_cast<float>(u) * us, static_cast<float>(v) * vs);
        t.vertex(static_cast<float>(heartX), static_cast<float>(heartY), blitOffset);
    }

    t.end();  // Single draw call for all hearts
}

void Gui::renderArmor() {
    LocalPlayer* player = minecraft->player;
    if (!player) return;

    int armor = 0;
    if (armor <= 0) return;

    int baseY = scaledHeight - 32;

    // Batch all armor blits into a single draw call
    Tesselator& t = Tesselator::getInstance();
    t.begin(DrawMode::Quads);

    float us = 0.00390625f;
    float vs = 0.00390625f;

    for (int i = 0; i < 10; i++) {
        int armorX = scaledWidth / 2 + 91 - i * 8 - 9;

        int u, v = 9, w = 9, h = 9;
        if (i * 2 + 1 < armor) {
            u = 34; // Full armor
        } else if (i * 2 + 1 == armor) {
            u = 25; // Half armor
        } else {
            u = 16; // Empty armor
        }

        t.color(1.0f, 1.0f, 1.0f, 1.0f);
        t.tex(static_cast<float>(u) * us, static_cast<float>(v + h) * vs);
        t.vertex(static_cast<float>(armorX), static_cast<float>(baseY + h), blitOffset);
        t.tex(static_cast<float>(u + w) * us, static_cast<float>(v + h) * vs);
        t.vertex(static_cast<float>(armorX + w), static_cast<float>(baseY + h), blitOffset);
        t.tex(static_cast<float>(u + w) * us, static_cast<float>(v) * vs);
        t.vertex(static_cast<float>(armorX + w), static_cast<float>(baseY), blitOffset);
        t.tex(static_cast<float>(u) * us, static_cast<float>(v) * vs);
        t.vertex(static_cast<float>(armorX), static_cast<float>(baseY), blitOffset);
    }

    t.end();  // Single draw call for all armor
}

void Gui::renderDebugInfo() {
    LocalPlayer* player = minecraft->player;

    std::stringstream ss;

    // Batch all debug text into a single draw call
    font.beginBatch();

    ss << "Minecraft Beta 1.2_02 (C++) (" << minecraft->fps << " fps)";
    font.addText(ss.str(), 2, 2, 0xFFFFFF, true);

    if (player) {
        ss.str("");
        ss << std::fixed << std::setprecision(5);
        ss << "x: " << player->x;
        font.addText(ss.str(), 2, 64, 0xE0E0E0, true);

        ss.str("");
        ss << "y: " << player->y;
        font.addText(ss.str(), 2, 72, 0xE0E0E0, true);

        ss.str("");
        ss << "z: " << player->z;
        font.addText(ss.str(), 2, 80, 0xE0E0E0, true);

        ss.str("");
        ss << std::setprecision(2);
        ss << "xRot: " << player->xRot;
        font.addText(ss.str(), 2, 88, 0xE0E0E0, true);

        ss.str("");
        ss << "yRot: " << player->yRot;
        font.addText(ss.str(), 2, 96, 0xE0E0E0, true);
    }

    if (minecraft->levelRenderer) {
        ss.str("");
        ss << "C: " << minecraft->levelRenderer->chunksRendered
           << "/" << minecraft->levelRenderer->chunks.size()
           << " U: " << minecraft->levelRenderer->chunksUpdated;
        font.addText(ss.str(), 2, 12, 0xFFFFFF, true);
    }

    long totalMem = 512;
    long usedMem = 256;
    ss.str("");
    ss << "Used memory: " << (usedMem * 100 / totalMem) << "% (" << usedMem << "MB) of " << totalMem << "MB";
    int memWidth = font.getWidth(ss.str());
    font.addText(ss.str(), scaledWidth - memWidth - 2, 2, 0xE0E0E0, true);

    font.endBatch();  // Single draw call for all debug text
}

void Gui::renderChat() {
}

void Gui::renderGuiItem(const ItemStack& item, int x, int y, float z, Font* font, TileRenderer& tileRenderer) {
    if (item.isEmpty()) return;

    auto& device = RenderDevice::get();
    device.setDepthTest(true);
    device.clear(false, true);

    if (item.id > 0 && item.id < 256) {
        Tile* tile = Tile::tiles[item.id].get();
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
                t.begin(DrawMode::Quads);
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
            t.begin(DrawMode::Quads);
            t.color(1.0f, 1.0f, 1.0f, 1.0f);
            t.tex(u0, v1); t.vertex(static_cast<float>(x), static_cast<float>(y + 16), z);
            t.tex(u1, v1); t.vertex(static_cast<float>(x + 16), static_cast<float>(y + 16), z);
            t.tex(u1, v0); t.vertex(static_cast<float>(x + 16), static_cast<float>(y), z);
            t.tex(u0, v0); t.vertex(static_cast<float>(x), static_cast<float>(y), z);
            t.end();
        }
    }

    device.setDepthTest(false);

    if (item.count > 1 && font) {
        std::string countStr = std::to_string(item.count);
        int textX = x + 17 - font->getWidth(countStr);
        int textY = y + 9;
        device.setDepthTest(false);
        font->drawShadow(countStr, textX, textY, 0xFFFFFF);
    }
}

} // namespace mc
