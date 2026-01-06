#include "item/Inventory.hpp"
#include "entity/Player.hpp"

namespace mc {

Inventory::Inventory(Player* player)
    : selected(0)
    , player(player)
{
    // Initialize with some starting items (like creative)
    items[0] = ItemStack(1, 64);  // Stone
    items[1] = ItemStack(4, 64);  // Cobblestone
    items[2] = ItemStack(3, 64);  // Dirt
    items[3] = ItemStack(2, 64);  // Grass (will drop dirt though)
    items[4] = ItemStack(5, 64);  // Planks
    items[5] = ItemStack(17, 64); // Wood log
    items[6] = ItemStack(24, 64); // Sandstone
    items[7] = ItemStack(45, 64); // Bricks
    items[8] = ItemStack(49, 64); // Obsidian
}

ItemStack& Inventory::getItem(int slot) {
    if (slot < 0 || slot >= TOTAL_SIZE) {
        static ItemStack empty;
        return empty;
    }
    return items[slot];
}

const ItemStack& Inventory::getItem(int slot) const {
    if (slot < 0 || slot >= TOTAL_SIZE) {
        static const ItemStack empty;
        return empty;
    }
    return items[slot];
}

ItemStack& Inventory::getSelected() {
    return items[selected];
}

const ItemStack& Inventory::getSelected() const {
    return items[selected];
}

bool Inventory::add(int itemId, int count, int damage) {
    // First try to stack with existing items
    for (int i = 0; i < HOTBAR_SIZE + MAIN_SIZE; i++) {
        if (items[i].id == itemId && items[i].damage == damage) {
            int leftover = items[i].add(count);
            if (leftover == 0) return true;
            count = leftover;
        }
    }

    // Then find empty slots
    for (int i = 0; i < HOTBAR_SIZE + MAIN_SIZE; i++) {
        if (items[i].isEmpty()) {
            int max = ItemStack(itemId, count, damage).getMaxStackSize();
            int toAdd = std::min(count, max);
            items[i] = ItemStack(itemId, toAdd, damage);
            count -= toAdd;
            if (count <= 0) return true;
        }
    }

    return count <= 0;  // Return true if everything was added
}

bool Inventory::addToHotbar(int itemId, int count, int damage) {
    // First try to stack with existing items in hotbar
    for (int i = 0; i < HOTBAR_SIZE; i++) {
        if (items[i].id == itemId && items[i].damage == damage) {
            int leftover = items[i].add(count);
            if (leftover == 0) return true;
            count = leftover;
        }
    }

    // Then find empty hotbar slots
    for (int i = 0; i < HOTBAR_SIZE; i++) {
        if (items[i].isEmpty()) {
            items[i] = ItemStack(itemId, count, damage);
            return true;
        }
    }

    // Fall back to full inventory
    return add(itemId, count, damage);
}

void Inventory::setItem(int slot, const ItemStack& item) {
    if (slot >= 0 && slot < TOTAL_SIZE) {
        items[slot] = item;
    }
}

void Inventory::setItem(int slot, int id, int count, int damage) {
    setItem(slot, ItemStack(id, count, damage));
}

ItemStack Inventory::removeItem(int slot, int count) {
    if (slot < 0 || slot >= TOTAL_SIZE || items[slot].isEmpty()) {
        return ItemStack();
    }

    if (count < 0 || count >= items[slot].count) {
        // Remove entire stack
        ItemStack removed = items[slot];
        items[slot] = ItemStack();
        return removed;
    }

    // Remove partial stack
    ItemStack removed(items[slot].id, count, items[slot].damage);
    items[slot].count -= count;
    if (items[slot].count <= 0) {
        items[slot] = ItemStack();
    }
    return removed;
}

int Inventory::findSlotWithItem(int itemId) const {
    for (int i = 0; i < HOTBAR_SIZE + MAIN_SIZE; i++) {
        if (items[i].id == itemId) {
            return i;
        }
    }
    return -1;
}

bool Inventory::hasItem(int itemId) const {
    return findSlotWithItem(itemId) >= 0;
}

void Inventory::swapSlots(int slot1, int slot2) {
    if (slot1 >= 0 && slot1 < TOTAL_SIZE &&
        slot2 >= 0 && slot2 < TOTAL_SIZE) {
        std::swap(items[slot1], items[slot2]);
    }
}

ItemStack& Inventory::getArmor(int slot) {
    return items[HOTBAR_SIZE + MAIN_SIZE + slot];
}

} // namespace mc
