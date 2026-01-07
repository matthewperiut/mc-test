#pragma once

#include <array>
#include <string>

namespace mc {

// Forward declarations
class Level;
class Player;
class ItemStack;

// Item class matching Java Item
class Item {
public:
    static constexpr int MAX_ITEMS = 32000;
    static std::array<Item*, MAX_ITEMS> items;

    // Item IDs (items start at 256, blocks are 0-255)
    static constexpr int ID_OFFSET = 256;

    // Item instances
    static Item* stick;
    static Item* coal;
    static Item* ironIngot;
    static Item* goldIngot;
    static Item* emerald;  // Diamond
    static Item* feather;
    static Item* string;
    static Item* bone;

    // Item properties
    int id;
    int icon;           // Texture index in items.png (16x16 grid)
    int maxStackSize;
    int maxDamage;
    bool handEquipped;  // If true, renders like a tool (rotated in hand)
    std::string descriptionId;

    Item(int itemId);
    virtual ~Item() = default;

    // Setters that return this for chaining
    Item* setIcon(int iconIndex);
    Item* setIcon(int x, int y);  // Column, row in 16x16 grid
    Item* setMaxStackSize(int size);
    Item* setMaxDamage(int damage);
    Item* setHandEquipped();
    Item* setDescriptionId(const std::string& id);

    // Getters
    int getIcon() const { return icon; }
    int getMaxStackSize() const { return maxStackSize; }
    bool isHandEquipped() const { return handEquipped; }
    const std::string& getDescriptionId() const { return descriptionId; }
    virtual bool isMirroredArt() const { return false; }

    // Virtual methods for subclasses
    virtual bool useOn(Player* player, Level* level, int x, int y, int z, int face);
    virtual int getAttackDamage() const { return 1; }
    virtual float getDestroySpeed(int tileId) const { return 1.0f; }

    // Static initialization
    static void initItems();
    static void destroyItems();
    static Item* byId(int id);

    // Check if ID is an item (not a block)
    static bool isItem(int id) { return id >= ID_OFFSET; }
};

} // namespace mc
