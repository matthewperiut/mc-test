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
    int throwTime;    // Additional delay for player-thrown items (40 ticks in Java)
    int age;          // How long item has existed
    int lifespan;     // Max lifetime before despawning (6000 ticks = 5 minutes)

    // Animation
    float bobOffset;  // Bobbing animation offset

    // Pickup animation state
    bool beingPickedUp;      // True when pickup animation is playing
    Player* pickupTarget;    // Player picking up the item
    int pickupAnimTime;      // Current animation tick (0-3)
    double pickupStartX;     // Position when pickup started
    double pickupStartY;
    double pickupStartZ;

    // Basic constructor - random initial velocity
    ItemEntity(Level* level, double x, double y, double z, int itemId, int count = 1, int damage = 0);

    // Constructor with velocity - for player drops
    ItemEntity(Level* level, double x, double y, double z, int itemId, int count, int damage,
               double vx, double vy, double vz, int throwDelay);

    void tick() override;

    // Check if player can pick this up
    bool canPickUp(Player* player) const;

    // Attempt pickup by player - returns true if successful
    bool playerPickUp(Player* player);

    // Start the pickup animation
    void startPickupAnimation(Player* player);

    // Override rendering helpers
    bool shouldRender(double camX, double camY, double camZ, float maxDist) const override;
    bool isPickable() const override { return true; }

    // Get interpolated position during pickup animation
    void getPickupAnimatedPos(float partialTick, double& outX, double& outY, double& outZ) const;

private:
    void tryMergeNearbyItems();
};

} // namespace mc
