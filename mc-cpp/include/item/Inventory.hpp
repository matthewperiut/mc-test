#pragma once

#include <array>

namespace mc {

class Player;

// Represents a stack of items
struct ItemStack {
    int id;      // Item/block ID (0 = empty)
    int count;   // Stack size
    int damage;  // Metadata/damage value

    ItemStack() : id(0), count(0), damage(0) {}
    ItemStack(int id, int count = 1, int damage = 0)
        : id(id), count(count), damage(damage) {}

    bool isEmpty() const { return id <= 0 || count <= 0; }

    // Max stack size (64 for most items, 16 for some, 1 for tools)
    int getMaxStackSize() const { return 64; }

    // Add to stack, returns leftover count
    int add(int amount) {
        int max = getMaxStackSize();
        int space = max - count;
        int toAdd = std::min(amount, space);
        count += toAdd;
        return amount - toAdd;
    }

    // Remove from stack, returns amount removed
    int remove(int amount) {
        int toRemove = std::min(amount, count);
        count -= toRemove;
        if (count <= 0) {
            id = 0;
            count = 0;
            damage = 0;
        }
        return toRemove;
    }
};

// Player inventory matching Java
class Inventory {
public:
    static constexpr int HOTBAR_SIZE = 9;
    static constexpr int MAIN_SIZE = 27;      // 3 rows of 9
    static constexpr int ARMOR_SIZE = 4;
    static constexpr int TOTAL_SIZE = HOTBAR_SIZE + MAIN_SIZE + ARMOR_SIZE;

    // Slots: 0-8 = hotbar, 9-35 = main, 36-39 = armor
    std::array<ItemStack, TOTAL_SIZE> items;

    // Currently selected hotbar slot
    int selected;

    // Owner
    Player* player;

    Inventory(Player* player);

    // Get item in slot
    ItemStack& getItem(int slot);
    const ItemStack& getItem(int slot) const;

    // Get currently held item (selected hotbar slot)
    ItemStack& getSelected();
    const ItemStack& getSelected() const;

    // Add item to inventory, returns true if fully added
    bool add(int itemId, int count = 1, int damage = 0);
    bool addToHotbar(int itemId, int count = 1, int damage = 0);

    // Set item in slot
    void setItem(int slot, const ItemStack& item);
    void setItem(int slot, int id, int count = 1, int damage = 0);

    // Remove item from slot
    ItemStack removeItem(int slot, int count = -1);

    // Find slot with item
    int findSlotWithItem(int itemId) const;

    // Check if has item
    bool hasItem(int itemId) const;

    // Swap slots
    void swapSlots(int slot1, int slot2);

    // Get armor slot (0=helmet, 1=chest, 2=legs, 3=boots)
    ItemStack& getArmor(int slot);
};

} // namespace mc
