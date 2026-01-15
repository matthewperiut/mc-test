#include "gui/InventoryScreen.hpp"
#include "gui/Gui.hpp"
#include "core/Minecraft.hpp"
#include "entity/LocalPlayer.hpp"
#include "renderer/Textures.hpp"
#include "renderer/TileRenderer.hpp"
#include "renderer/Tesselator.hpp"
#include "renderer/MatrixStack.hpp"
#include "renderer/ShaderManager.hpp"
#include "world/tile/Tile.hpp"
#include "item/Item.hpp"
#include <GLFW/glfw3.h>
#include <algorithm>
#include <cctype>

#ifdef MC_RENDERER_METAL
#include "renderer/backend/RenderDevice.hpp"
#else
#include <GL/glew.h>
#endif

namespace mc {

InventoryScreen::InventoryScreen()
    : guiLeft(0)
    , guiTop(0)
    , mouseX(0)
    , mouseY(0)
    , draggedItem()
    , draggedFromSlot(-1)
{
}

void InventoryScreen::init(Minecraft* mc, int w, int h) {
    Screen::init(mc, w, h);

    guiLeft = (width - GUI_WIDTH) / 2;
    guiTop = (height - GUI_HEIGHT) / 2;
}

void InventoryScreen::render(int mx, int my, float /*partialTick*/) {
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

    // Render inventory background
    renderBackground();

    // Render inventory slots with items
    renderSlots();

    // Render dragged item at cursor position
    if (!draggedItem.isEmpty()) {
        renderDraggedItem();
    }

    // Render tooltip for hovered item (only when not dragging)
    if (draggedItem.isEmpty()) {
        renderTooltip();
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

void InventoryScreen::renderBackground() {
    Textures::getInstance().bind("resources/gui/inventory.png");

#ifdef MC_RENDERER_METAL
    RenderDevice::get().setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);
#else
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
#endif

    blit(guiLeft, guiTop, 0, 0, GUI_WIDTH, GUI_HEIGHT);

#ifdef MC_RENDERER_METAL
    RenderDevice::get().setBlend(false);
#else
    glDisable(GL_BLEND);
#endif
}

void InventoryScreen::renderSlots() {
    if (!minecraft->player || !minecraft->player->inventory) return;

    int hoveredSlot = getSlotAtPosition(mouseX, mouseY);
    int highlightX = 0, highlightY = 0;
    bool hasHighlight = false;

    for (int i = 0; i < 9; i++) {
        int x = guiLeft + 8 + i * SLOT_SIZE;
        int y = guiTop + 142;
        renderSlot(i, x, y);
        if (hoveredSlot == i) {
            highlightX = x;
            highlightY = y;
            hasHighlight = true;
        }
    }

    for (int row = 0; row < 3; row++) {
        for (int col = 0; col < 9; col++) {
            int slot = 9 + row * 9 + col;
            int x = guiLeft + 8 + col * SLOT_SIZE;
            int y = guiTop + 84 + row * SLOT_SIZE;
            renderSlot(slot, x, y);
            if (hoveredSlot == slot) {
                highlightX = x;
                highlightY = y;
                hasHighlight = true;
            }
        }
    }

    for (int i = 0; i < 4; i++) {
        int slot = 36 + (3 - i);
        int x = guiLeft + 8;
        int y = guiTop + 8 + i * SLOT_SIZE;
        renderSlot(slot, x, y);
        if (hoveredSlot == slot) {
            highlightX = x;
            highlightY = y;
            hasHighlight = true;
        }
    }

    if (hasHighlight) {
        renderSlotHighlight(highlightX, highlightY);
    }
}

void InventoryScreen::renderSlot(int slotIndex, int x, int y) {
    if (!minecraft->player || !minecraft->player->inventory) return;

    Inventory* inv = minecraft->player->inventory.get();
    const ItemStack& item = inv->getItem(slotIndex);

    if (item.isEmpty()) return;

    TileRenderer tileRenderer;
    Gui::renderGuiItem(item, x, y, 100.0f, font, tileRenderer);
}

void InventoryScreen::renderDraggedItem() {
    if (draggedItem.isEmpty()) return;

    int x = mouseX - 8;
    int y = mouseY - 8;

    TileRenderer tileRenderer;
    Gui::renderGuiItem(draggedItem, x, y, 200.0f, font, tileRenderer);
}

void InventoryScreen::blit(int x, int y, int u, int v, int w, int h) {
    float texWidth = 256.0f;
    float texHeight = 256.0f;

    float u0 = static_cast<float>(u) / texWidth;
    float v0 = static_cast<float>(v) / texHeight;
    float u1 = static_cast<float>(u + w) / texWidth;
    float v1 = static_cast<float>(v + h) / texHeight;

    Tesselator& t = Tesselator::getInstance();
    t.begin(GL_QUADS);
    t.color(1.0f, 1.0f, 1.0f, 1.0f);
    t.tex(u0, v0); t.vertex(static_cast<float>(x), static_cast<float>(y), 0.0f);
    t.tex(u1, v0); t.vertex(static_cast<float>(x + w), static_cast<float>(y), 0.0f);
    t.tex(u1, v1); t.vertex(static_cast<float>(x + w), static_cast<float>(y + h), 0.0f);
    t.tex(u0, v1); t.vertex(static_cast<float>(x), static_cast<float>(y + h), 0.0f);
    t.end();
}

void InventoryScreen::renderSlotHighlight(int x, int y) {
#ifdef MC_RENDERER_METAL
    RenderDevice::get().setDepthTest(false);
    RenderDevice::get().setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);
#else
    glDisable(GL_DEPTH_TEST);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
#endif

    ShaderManager::getInstance().useGuiShader();
    ShaderManager::getInstance().updateMatrices();
    ShaderManager::getInstance().setUseTexture(false);

    Tesselator& t = Tesselator::getInstance();
    t.begin(GL_QUADS);
    t.color(1.0f, 1.0f, 1.0f, 0.5f);
    t.vertex(static_cast<float>(x), static_cast<float>(y), 200.0f);
    t.vertex(static_cast<float>(x + 16), static_cast<float>(y), 200.0f);
    t.vertex(static_cast<float>(x + 16), static_cast<float>(y + 16), 200.0f);
    t.vertex(static_cast<float>(x), static_cast<float>(y + 16), 200.0f);
    t.end();

    ShaderManager::getInstance().setUseTexture(true);
#ifdef MC_RENDERER_METAL
    RenderDevice::get().setBlend(false);
#else
    glDisable(GL_BLEND);
#endif
}

void InventoryScreen::renderTooltip() {
    if (!minecraft->player || !minecraft->player->inventory) return;

    int hoveredSlot = getSlotAtPosition(mouseX, mouseY);
    if (hoveredSlot < 0) return;

    const ItemStack& item = minecraft->player->inventory->getItem(hoveredSlot);
    if (item.isEmpty()) return;

    std::string itemName;
    if (item.id > 0 && item.id < 256) {
        Tile* tile = Tile::tiles[item.id];
        if (tile && !tile->name.empty()) {
            itemName = tile->name;
        }
    } else if (item.id >= 256) {
        Item* itemDef = Item::byId(item.id);
        if (itemDef) {
            itemName = itemDef->getDescriptionId();
            if (itemName.rfind("item.", 0) == 0) {
                itemName = itemName.substr(5);
            }
            if (!itemName.empty()) {
                itemName[0] = static_cast<char>(std::toupper(static_cast<unsigned char>(itemName[0])));
            }
        }
    }

    if (itemName.empty()) return;

    int tooltipX = mouseX + 12;
    int tooltipY = mouseY - 12;
    int textWidth = font->getWidth(itemName);

    ShaderManager::getInstance().useGuiShader();
    ShaderManager::getInstance().updateMatrices();
    ShaderManager::getInstance().setUseTexture(false);

#ifdef MC_RENDERER_METAL
    RenderDevice::get().setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);
#else
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
#endif

    Tesselator& t = Tesselator::getInstance();
    t.begin(GL_QUADS);
    t.color(0.0f, 0.0f, 0.0f, 0.75f);
    t.vertex(static_cast<float>(tooltipX - 3), static_cast<float>(tooltipY - 3), 0.0f);
    t.vertex(static_cast<float>(tooltipX + textWidth + 3), static_cast<float>(tooltipY - 3), 0.0f);
    t.vertex(static_cast<float>(tooltipX + textWidth + 3), static_cast<float>(tooltipY + 8 + 3), 0.0f);
    t.vertex(static_cast<float>(tooltipX - 3), static_cast<float>(tooltipY + 8 + 3), 0.0f);
    t.end();

    ShaderManager::getInstance().setUseTexture(true);
#ifdef MC_RENDERER_METAL
    RenderDevice::get().setBlend(false);
#else
    glDisable(GL_BLEND);
#endif

    font->drawShadow(itemName, tooltipX, tooltipY, 0xFFFFFF);
}

bool InventoryScreen::isHovering(int slotX, int slotY, int mx, int my) {
    return mx >= slotX - 1 && mx < slotX + 16 + 1 &&
           my >= slotY - 1 && my < slotY + 16 + 1;
}

int InventoryScreen::getSlotAtPosition(int mx, int my) {
    for (int i = 0; i < 9; i++) {
        int slotX = guiLeft + 8 + i * SLOT_SIZE;
        int slotY = guiTop + 142;
        if (isHovering(slotX, slotY, mx, my)) {
            return i;
        }
    }

    for (int row = 0; row < 3; row++) {
        for (int col = 0; col < 9; col++) {
            int slot = 9 + row * 9 + col;
            int slotX = guiLeft + 8 + col * SLOT_SIZE;
            int slotY = guiTop + 84 + row * SLOT_SIZE;
            if (isHovering(slotX, slotY, mx, my)) {
                return slot;
            }
        }
    }

    for (int i = 0; i < 4; i++) {
        int slot = 36 + (3 - i);
        int slotX = guiLeft + 8;
        int slotY = guiTop + 8 + i * SLOT_SIZE;
        if (isHovering(slotX, slotY, mx, my)) {
            return slot;
        }
    }

    return -1;
}

void InventoryScreen::handleSlotClick(int slot, int button) {
    if (slot < 0) return;
    if (!minecraft->player || !minecraft->player->inventory) return;

    Inventory* inv = minecraft->player->inventory.get();

    if (button == GLFW_MOUSE_BUTTON_LEFT) {
        if (draggedItem.isEmpty()) {
            ItemStack slotItem = inv->getItem(slot);
            if (!slotItem.isEmpty()) {
                draggedItem = slotItem;
                draggedFromSlot = slot;
                inv->setItem(slot, ItemStack());
            }
        } else {
            ItemStack& slotItem = inv->items[slot];

            if (slotItem.isEmpty()) {
                inv->setItem(slot, draggedItem);
                draggedItem = ItemStack();
                draggedFromSlot = -1;
            } else if (slotItem.id == draggedItem.id && slotItem.count < 64) {
                int space = 64 - slotItem.count;
                int transfer = std::min(draggedItem.count, space);
                slotItem.count += transfer;
                draggedItem.count -= transfer;
                if (draggedItem.count <= 0) {
                    draggedItem = ItemStack();
                    draggedFromSlot = -1;
                }
            } else {
                ItemStack temp = slotItem;
                inv->setItem(slot, draggedItem);
                draggedItem = temp;
                draggedFromSlot = slot;
            }
        }
    } else if (button == GLFW_MOUSE_BUTTON_RIGHT) {
        if (draggedItem.isEmpty()) {
            ItemStack& slotItem = inv->items[slot];
            if (!slotItem.isEmpty()) {
                int half = (slotItem.count + 1) / 2;
                draggedItem = ItemStack(slotItem.id, half, slotItem.damage);
                slotItem.count -= half;
                if (slotItem.count <= 0) {
                    slotItem = ItemStack();
                }
                draggedFromSlot = slot;
            }
        } else {
            ItemStack& slotItem = inv->items[slot];

            if (slotItem.isEmpty()) {
                inv->setItem(slot, ItemStack(draggedItem.id, 1, draggedItem.damage));
                draggedItem.count--;
                if (draggedItem.count <= 0) {
                    draggedItem = ItemStack();
                    draggedFromSlot = -1;
                }
            } else if (slotItem.id == draggedItem.id && slotItem.count < 64) {
                slotItem.count++;
                draggedItem.count--;
                if (draggedItem.count <= 0) {
                    draggedItem = ItemStack();
                    draggedFromSlot = -1;
                }
            }
        }
    }
}

void InventoryScreen::tick() {
}

void InventoryScreen::removed() {
    if (!draggedItem.isEmpty() && minecraft->player && minecraft->player->inventory) {
        minecraft->player->inventory->add(draggedItem.id, draggedItem.count, draggedItem.damage);
        draggedItem = ItemStack();
        draggedFromSlot = -1;
    }
}

void InventoryScreen::keyPressed(int key, int scancode, int action, int mods) {
    (void)scancode;
    (void)mods;

    if (action == GLFW_PRESS) {
        if (key == GLFW_KEY_ESCAPE || key == GLFW_KEY_E) {
            minecraft->closeScreen();
        }
    }
}

void InventoryScreen::mouseClicked(int button, int action) {
    if (action == GLFW_PRESS) {
        int slot = getSlotAtPosition(mouseX, mouseY);

        bool outsideGui = mouseX < guiLeft || mouseY < guiTop ||
                          mouseX >= guiLeft + GUI_WIDTH || mouseY >= guiTop + GUI_HEIGHT;

        if (outsideGui && !draggedItem.isEmpty()) {
            draggedItem = ItemStack();
            draggedFromSlot = -1;
        } else {
            handleSlotClick(slot, button);
        }
    }
}

void InventoryScreen::mouseMoved(double x, double y) {
    mouseX = static_cast<int>(x);
    mouseY = static_cast<int>(y);
}

} // namespace mc
