#pragma once

#include "entity/Entity.hpp"

namespace mc {

class Player;

// Represents a dropped item in the world
class ItemEntity : public Entity {
public:
    // Item data
    int itemId;
    int count;
    int damage;  // Item metadata/damage

    // Pickup timing
    int pickupDelay;  // Ticks before can be picked up (default 10)
    int age;          // How long item has existed
    int lifespan;     // Max lifetime before despawning (6000 ticks = 5 minutes)

    // Animation
    float bobOffset;  // Bobbing animation offset

    ItemEntity(Level* level, double x, double y, double z, int itemId, int count = 1, int damage = 0);

    void tick() override;

    // Check if player can pick this up
    bool canPickUp(Player* player) const;

    // Attempt pickup by player - returns true if successful
    bool playerPickUp(Player* player);

    // Override rendering helpers
    bool shouldRender(double camX, double camY, double camZ, float maxDist) const override;
    bool isPickable() const override { return true; }

private:
    void tryMergeNearbyItems();
};

} // namespace mc
