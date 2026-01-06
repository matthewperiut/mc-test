#pragma once

#include "gui/Screen.hpp"
#include "item/Inventory.hpp"

namespace mc {

class InventoryScreen : public Screen {
public:
    // GUI dimensions matching Java
    static constexpr int GUI_WIDTH = 176;
    static constexpr int GUI_HEIGHT = 166;
    static constexpr int SLOT_SIZE = 18;

    InventoryScreen();
    virtual ~InventoryScreen() = default;

    void init(Minecraft* minecraft, int width, int height) override;
    void render(int mouseX, int mouseY, float partialTick) override;
    void tick() override;
    void removed() override;

    void keyPressed(int key, int scancode, int action, int mods) override;
    void mouseClicked(int button, int action) override;
    void mouseMoved(double x, double y) override;

protected:
    bool pausesGame() const { return false; }

private:
    // GUI position (centered)
    int guiLeft;
    int guiTop;

    // Mouse position
    int mouseX;
    int mouseY;

    // Dragged item state (for click-to-pick-up, click-to-place)
    ItemStack draggedItem;
    int draggedFromSlot;

    // Rendering helpers
    void renderBackground();
    void renderSlots();
    void renderSlot(int slotIndex, int x, int y);
    void renderSlotHighlight(int x, int y);
    void renderDraggedItem();
    void blit(int x, int y, int u, int v, int w, int h);

    // Slot interaction
    int getSlotAtPosition(int mx, int my);
    void handleSlotClick(int slot, int button);
};

} // namespace mc
