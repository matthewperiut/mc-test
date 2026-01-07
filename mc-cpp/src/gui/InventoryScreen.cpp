#include "gui/InventoryScreen.hpp"
#include "core/Minecraft.hpp"
#include "entity/LocalPlayer.hpp"
#include "renderer/Textures.hpp"
#include "renderer/TileRenderer.hpp"
#include "renderer/Tesselator.hpp"
#include "world/tile/Tile.hpp"
#include <GL/glew.h>
#include <GLFW/glfw3.h>
#include <algorithm>

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

    // Center the GUI
    guiLeft = (width - GUI_WIDTH) / 2;
    guiTop = (height - GUI_HEIGHT) / 2;
}

void InventoryScreen::render(int mx, int my, float /*partialTick*/) {
    mouseX = mx;
    mouseY = my;

    // Set up 2D orthographic projection (matching Java/Gui.cpp)
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

    // Draw semi-transparent background (matching Java)
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
    glMatrixMode(GL_PROJECTION);
    glPopMatrix();
    glMatrixMode(GL_MODELVIEW);
    glPopMatrix();

    // Restore 3D state
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);
}

void InventoryScreen::renderBackground() {
    // Bind inventory texture
    Textures::getInstance().bind("resources/gui/inventory.png");

    glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    // Draw the inventory background (176x166 from texture)
    blit(guiLeft, guiTop, 0, 0, GUI_WIDTH, GUI_HEIGHT);

    glDisable(GL_BLEND);
}

void InventoryScreen::renderSlots() {
    if (!minecraft->player || !minecraft->player->inventory) return;

    // Get hovered slot for highlighting
    int hoveredSlot = getSlotAtPosition(mouseX, mouseY);
    int highlightX = 0, highlightY = 0;
    bool hasHighlight = false;

    // Hotbar slots (bottom row, slots 0-8)
    // Java position: x=8, y=142 within the GUI
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

    // Main inventory (3 rows of 9, slots 9-35)
    // Java position: starts at x=8, y=84
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

    // Armor slots (left side, slots 36-39) - matching Java inventory.png layout
    // Java position: x=8, y=8 for first armor slot
    for (int i = 0; i < 4; i++) {
        int slot = 36 + (3 - i);  // Reverse order: helmet at top
        int x = guiLeft + 8;
        int y = guiTop + 8 + i * SLOT_SIZE;
        renderSlot(slot, x, y);
        if (hoveredSlot == slot) {
            highlightX = x;
            highlightY = y;
            hasHighlight = true;
        }
    }

    // Render highlight AFTER all slots (Java does this during the loop but we need to avoid GL state conflicts)
    if (hasHighlight) {
        renderSlotHighlight(highlightX, highlightY);
    }
}

void InventoryScreen::renderSlot(int slotIndex, int x, int y) {
    if (!minecraft->player || !minecraft->player->inventory) return;

    Inventory* inv = minecraft->player->inventory.get();
    const ItemStack& item = inv->getItem(slotIndex);

    if (item.isEmpty()) return;

    // Only render blocks for now (id < 256)
    if (item.id > 0 && item.id < 256) {
        Tile* tile = Tile::tiles[item.id];
        if (!tile) return;

        Textures::getInstance().bind("resources/terrain.png");

        glEnable(GL_DEPTH_TEST);
        glClear(GL_DEPTH_BUFFER_BIT);

        glPushMatrix();

        // Transform for isometric block view (matching Java Gui.renderSlot and hotbar rendering)
        glTranslatef(static_cast<float>(x - 2), static_cast<float>(y + 3), 100.0f);
        glScalef(10.0f, 10.0f, 10.0f);
        glTranslatef(1.0f, 0.5f, 8.0f);
        glRotatef(-30.0f, 1.0f, 0.0f, 0.0f);
        glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
        glScalef(-1.0f, -1.0f, 1.0f);

        // Get brightness
        float brightness = 1.0f;

        TileRenderer tileRenderer;
        tileRenderer.renderAllFaces = true;

        Tesselator& t = Tesselator::getInstance();
        t.begin(GL_QUADS);
        tileRenderer.renderBlockItem(tile, brightness);
        t.end();

        glPopMatrix();
        glDisable(GL_DEPTH_TEST);

        // Render stack count if > 1
        if (item.count > 1) {
            std::string countStr = std::to_string(item.count);
            // Position count in bottom-right of slot
            int textX = x + 17 - font->getWidth(countStr);
            int textY = y + 9;
            font->drawShadow(countStr, textX, textY, 0xFFFFFF);
        }
    }
}

void InventoryScreen::renderDraggedItem() {
    if (draggedItem.isEmpty()) return;

    // Only render blocks
    if (draggedItem.id > 0 && draggedItem.id < 256) {
        Tile* tile = Tile::tiles[draggedItem.id];
        if (!tile) return;

        Textures::getInstance().bind("resources/terrain.png");

        glEnable(GL_DEPTH_TEST);
        glClear(GL_DEPTH_BUFFER_BIT);

        glPushMatrix();

        // Render at cursor position (offset by half slot size to center)
        int x = mouseX - 8;
        int y = mouseY - 8;

        glTranslatef(static_cast<float>(x - 2), static_cast<float>(y + 3), 200.0f);  // Higher Z to render on top
        glScalef(10.0f, 10.0f, 10.0f);
        glTranslatef(1.0f, 0.5f, 8.0f);
        glRotatef(-30.0f, 1.0f, 0.0f, 0.0f);
        glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
        glScalef(-1.0f, -1.0f, 1.0f);

        TileRenderer tileRenderer;
        tileRenderer.renderAllFaces = true;

        Tesselator& t = Tesselator::getInstance();
        t.begin(GL_QUADS);
        tileRenderer.renderBlockItem(tile, 1.0f);
        t.end();

        glPopMatrix();
        glDisable(GL_DEPTH_TEST);

        // Render stack count
        if (draggedItem.count > 1) {
            std::string countStr = std::to_string(draggedItem.count);
            int textX = mouseX + 9 - font->getWidth(countStr);
            int textY = mouseY + 1;
            font->drawShadow(countStr, textX, textY, 0xFFFFFF);
        }
    }
}

void InventoryScreen::blit(int x, int y, int u, int v, int w, int h) {
    // Render a portion of the bound texture (256x256 texture atlas)
    float texWidth = 256.0f;
    float texHeight = 256.0f;

    float u0 = static_cast<float>(u) / texWidth;
    float v0 = static_cast<float>(v) / texHeight;
    float u1 = static_cast<float>(u + w) / texWidth;
    float v1 = static_cast<float>(v + h) / texHeight;

    glBegin(GL_QUADS);
    glTexCoord2f(u0, v0); glVertex2f(static_cast<float>(x), static_cast<float>(y));
    glTexCoord2f(u1, v0); glVertex2f(static_cast<float>(x + w), static_cast<float>(y));
    glTexCoord2f(u1, v1); glVertex2f(static_cast<float>(x + w), static_cast<float>(y + h));
    glTexCoord2f(u0, v1); glVertex2f(static_cast<float>(x), static_cast<float>(y + h));
    glEnd();
}

void InventoryScreen::renderSlotHighlight(int x, int y) {
    // Render semi-transparent white highlight over slot (matching Java AbstractContainerScreen)
    // Java: GL11.glDisable(2896) = lighting, GL11.glDisable(2929) = depth test
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_LIGHTING);
    glDisable(GL_TEXTURE_2D);
    glDisable(GL_ALPHA_TEST);  // Disable alpha test so 0.5 alpha isn't discarded
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    // Semi-transparent white (matching Java: -2130706433 = 0x80FFFFFF = 50% alpha)
    glColor4f(1.0f, 1.0f, 1.0f, 0.5f);

    // Render at z=200 to be in front of items (which render at z=100)
    glBegin(GL_QUADS);
    glVertex3f(static_cast<float>(x), static_cast<float>(y), 200.0f);
    glVertex3f(static_cast<float>(x + 16), static_cast<float>(y), 200.0f);
    glVertex3f(static_cast<float>(x + 16), static_cast<float>(y + 16), 200.0f);
    glVertex3f(static_cast<float>(x), static_cast<float>(y + 16), 200.0f);
    glEnd();

    // Restore state
    glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    glDisable(GL_BLEND);
    glEnable(GL_TEXTURE_2D);
}

void InventoryScreen::renderTooltip() {
    if (!minecraft->player || !minecraft->player->inventory) return;

    int hoveredSlot = getSlotAtPosition(mouseX, mouseY);
    if (hoveredSlot < 0) return;

    const ItemStack& item = minecraft->player->inventory->getItem(hoveredSlot);
    if (item.isEmpty()) return;

    // Get item name
    std::string itemName;
    if (item.id > 0 && item.id < 256) {
        Tile* tile = Tile::tiles[item.id];
        if (tile && !tile->name.empty()) {
            itemName = tile->name;
        }
    }

    if (itemName.empty()) return;

    // Position tooltip (Java: mouseX - guiLeft + 12, mouseY - guiTop - 12)
    int tooltipX = mouseX + 12;
    int tooltipY = mouseY - 12;
    int textWidth = font->getWidth(itemName);

    // Draw tooltip background (Java: -1073741824 = 0xC0000000 = dark semi-transparent)
    glDisable(GL_TEXTURE_2D);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glColor4f(0.0f, 0.0f, 0.0f, 0.75f);

    glBegin(GL_QUADS);
    glVertex2f(static_cast<float>(tooltipX - 3), static_cast<float>(tooltipY - 3));
    glVertex2f(static_cast<float>(tooltipX + textWidth + 3), static_cast<float>(tooltipY - 3));
    glVertex2f(static_cast<float>(tooltipX + textWidth + 3), static_cast<float>(tooltipY + 8 + 3));
    glVertex2f(static_cast<float>(tooltipX - 3), static_cast<float>(tooltipY + 8 + 3));
    glEnd();

    glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    glDisable(GL_BLEND);
    glEnable(GL_TEXTURE_2D);

    // Draw item name
    font->drawShadow(itemName, tooltipX, tooltipY, 0xFFFFFF);
}

bool InventoryScreen::isHovering(int slotX, int slotY, int mx, int my) {
    // Match Java: extend hit area by 1 pixel in each direction (18x18 total)
    // This covers the gaps between slots
    return mx >= slotX - 1 && mx < slotX + 16 + 1 &&
           my >= slotY - 1 && my < slotY + 16 + 1;
}

int InventoryScreen::getSlotAtPosition(int mx, int my) {
    // Check hotbar slots (0-8)
    for (int i = 0; i < 9; i++) {
        int slotX = guiLeft + 8 + i * SLOT_SIZE;
        int slotY = guiTop + 142;
        if (isHovering(slotX, slotY, mx, my)) {
            return i;
        }
    }

    // Check main inventory (9-35)
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

    // Check armor slots (36-39)
    for (int i = 0; i < 4; i++) {
        int slot = 36 + (3 - i);
        int slotX = guiLeft + 8;
        int slotY = guiTop + 8 + i * SLOT_SIZE;
        if (isHovering(slotX, slotY, mx, my)) {
            return slot;
        }
    }

    return -1;  // No slot at this position
}

void InventoryScreen::handleSlotClick(int slot, int button) {
    if (slot < 0) return;
    if (!minecraft->player || !minecraft->player->inventory) return;

    Inventory* inv = minecraft->player->inventory.get();

    if (button == GLFW_MOUSE_BUTTON_LEFT) {
        if (draggedItem.isEmpty()) {
            // Pick up item from slot
            ItemStack slotItem = inv->getItem(slot);
            if (!slotItem.isEmpty()) {
                draggedItem = slotItem;
                draggedFromSlot = slot;
                inv->setItem(slot, ItemStack());
            }
        } else {
            // Try to place or swap
            ItemStack& slotItem = inv->items[slot];

            if (slotItem.isEmpty()) {
                // Place into empty slot
                inv->setItem(slot, draggedItem);
                draggedItem = ItemStack();
                draggedFromSlot = -1;
            } else if (slotItem.id == draggedItem.id && slotItem.count < 64) {
                // Stack items of same type
                int space = 64 - slotItem.count;
                int transfer = std::min(draggedItem.count, space);
                slotItem.count += transfer;
                draggedItem.count -= transfer;
                if (draggedItem.count <= 0) {
                    draggedItem = ItemStack();
                    draggedFromSlot = -1;
                }
            } else {
                // Swap items
                ItemStack temp = slotItem;
                inv->setItem(slot, draggedItem);
                draggedItem = temp;
                draggedFromSlot = slot;
            }
        }
    } else if (button == GLFW_MOUSE_BUTTON_RIGHT) {
        // Right-click: place one item or pick up half
        if (draggedItem.isEmpty()) {
            // Pick up half of stack
            ItemStack& slotItem = inv->items[slot];  // Use reference to modify actual slot
            if (!slotItem.isEmpty()) {
                int half = (slotItem.count + 1) / 2;
                draggedItem = ItemStack(slotItem.id, half, slotItem.damage);
                slotItem.count -= half;
                if (slotItem.count <= 0) {
                    slotItem = ItemStack();  // Clear the actual slot
                }
                draggedFromSlot = slot;
            }
        } else {
            // Place one item
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
    // Nothing to tick for now
}

void InventoryScreen::removed() {
    // Return dragged item to inventory if screen is closed
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
            // Close inventory
            minecraft->closeScreen();
        }
    }
}

void InventoryScreen::mouseClicked(int button, int action) {
    if (action == GLFW_PRESS) {
        int slot = getSlotAtPosition(mouseX, mouseY);

        // Check if clicking outside the inventory GUI (matching Java)
        bool outsideGui = mouseX < guiLeft || mouseY < guiTop ||
                          mouseX >= guiLeft + GUI_WIDTH || mouseY >= guiTop + GUI_HEIGHT;

        if (outsideGui && !draggedItem.isEmpty()) {
            // Drop item when clicking outside with a carried item (Java: slot -999)
            // For now, just clear the dragged item (TODO: spawn dropped item entity)
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
