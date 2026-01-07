#include "item/Item.hpp"

namespace mc {

// Static members
std::array<Item*, Item::MAX_ITEMS> Item::items = {};
Item* Item::stick = nullptr;
Item* Item::coal = nullptr;
Item* Item::ironIngot = nullptr;
Item* Item::goldIngot = nullptr;
Item* Item::emerald = nullptr;
Item* Item::feather = nullptr;
Item* Item::string = nullptr;
Item* Item::bone = nullptr;

Item::Item(int itemId)
    : id(ID_OFFSET + itemId)
    , icon(0)
    , maxStackSize(64)
    , maxDamage(0)
    , handEquipped(false)
{
    if (items[ID_OFFSET + itemId] != nullptr) {
        // Item ID conflict
    }
    items[ID_OFFSET + itemId] = this;
}

Item* Item::setIcon(int iconIndex) {
    icon = iconIndex;
    return this;
}

Item* Item::setIcon(int x, int y) {
    icon = x + y * 16;
    return this;
}

Item* Item::setMaxStackSize(int size) {
    maxStackSize = size;
    return this;
}

Item* Item::setMaxDamage(int damage) {
    maxDamage = damage;
    return this;
}

Item* Item::setHandEquipped() {
    handEquipped = true;
    return this;
}

Item* Item::setDescriptionId(const std::string& id) {
    descriptionId = "item." + id;
    return this;
}

bool Item::useOn(Player* /*player*/, Level* /*level*/, int /*x*/, int /*y*/, int /*z*/, int /*face*/) {
    return false;
}

Item* Item::byId(int id) {
    if (id < 0 || id >= MAX_ITEMS) return nullptr;
    return items[id];
}

void Item::initItems() {
    // Initialize items matching Java Item static block
    // Item IDs in Java start at 256 (0-255 are blocks)
    // The constructor takes the offset from 256

    // stick = new Item(24) -> id = 280
    // Java: setIcon(5, 3) = column 5, row 3 = icon index 53
    stick = (new Item(24))->setIcon(5, 3)->setHandEquipped()->setDescriptionId("stick");

    // coal = new CoalItem(7) -> id = 263
    coal = (new Item(7))->setIcon(7, 0)->setDescriptionId("coal");

    // ironIngot = new Item(9) -> id = 265
    ironIngot = (new Item(9))->setIcon(7, 1)->setDescriptionId("ingotIron");

    // goldIngot = new Item(10) -> id = 266
    goldIngot = (new Item(10))->setIcon(7, 2)->setDescriptionId("ingotGold");

    // emerald (diamond) = new Item(8) -> id = 264
    emerald = (new Item(8))->setIcon(7, 3)->setDescriptionId("emerald");

    // feather = new Item(32) -> id = 288
    feather = (new Item(32))->setIcon(8, 1)->setDescriptionId("feather");

    // string = new Item(31) -> id = 287
    string = (new Item(31))->setIcon(8, 0)->setDescriptionId("string");

    // bone = new Item(96) -> id = 352
    bone = (new Item(96))->setIcon(12, 1)->setHandEquipped()->setDescriptionId("bone");
}

void Item::destroyItems() {
    for (int i = 0; i < MAX_ITEMS; i++) {
        delete items[i];
        items[i] = nullptr;
    }
    stick = nullptr;
    coal = nullptr;
    ironIngot = nullptr;
    goldIngot = nullptr;
    emerald = nullptr;
    feather = nullptr;
    string = nullptr;
    bone = nullptr;
}

} // namespace mc
