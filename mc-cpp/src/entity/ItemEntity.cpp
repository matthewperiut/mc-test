#include "entity/ItemEntity.hpp"
#include "entity/Player.hpp"
#include "world/Level.hpp"
#include "util/Mth.hpp"
#include <cmath>

namespace mc {

ItemEntity::ItemEntity(Level* level, double x, double y, double z, int itemId, int count, int damage)
    : Entity(level)
    , itemId(itemId)
    , count(count)
    , damage(damage)
    , pickupDelay(10)  // 10 ticks before can be picked up (0.5 seconds)
    , age(0)
    , lifespan(6000)   // 5 minutes
    , bobOffset(Mth::random() * Mth::PI * 2.0f)
{
    setSize(0.25f, 0.25f);  // Small collision box
    this->x = x;
    this->y = y;
    this->z = z;
    setPos(x, y, z);

    // Random initial velocity (like Java)
    xd = (Mth::random() * 0.2 - 0.1);
    yd = 0.2;  // Pop up slightly
    zd = (Mth::random() * 0.2 - 0.1);

    yRot = Mth::random() * 360.0f;
}

void ItemEntity::tick() {
    // Store previous position
    prevX = x;
    prevY = y;
    prevZ = z;

    // Age the item
    age++;
    if (age >= lifespan) {
        removed = true;
        return;
    }

    // Decrease pickup delay
    if (pickupDelay > 0) {
        pickupDelay--;
    }

    // Apply gravity
    yd -= 0.04;  // Gravity for items (less than entities)

    // Apply movement
    move(xd, yd, zd);

    // Apply friction
    float friction = 0.98f;
    if (onGround) {
        friction = 0.6f * 0.98f;  // Ground friction
        xd *= friction;
        zd *= friction;
        yd *= -0.5;  // Bounce slightly
    } else {
        xd *= friction;
        zd *= friction;
    }
    yd *= 0.98;

    // Settle when very slow
    if (onGround) {
        if (std::abs(xd) < 0.001) xd = 0;
        if (std::abs(zd) < 0.001) zd = 0;
    }

    // Try to merge with nearby identical items
    if (age % 20 == 0) {  // Check every second
        tryMergeNearbyItems();
    }

    // Check for player pickup
    if (pickupDelay <= 0 && level) {
        for (Player* player : level->players) {
            if (player && canPickUp(player)) {
                playerPickUp(player);
                break;
            }
        }
    }
}

bool ItemEntity::canPickUp(Player* player) const {
    if (!player || pickupDelay > 0) return false;

    // Check distance - pickup radius is about 1.5 blocks
    double dx = player->x - x;
    double dy = (player->y + player->eyeHeight * 0.5) - y;
    double dz = player->z - z;
    double distSqr = dx * dx + dy * dy + dz * dz;

    return distSqr < 1.5 * 1.5;  // Within 1.5 blocks
}

bool ItemEntity::playerPickUp(Player* player) {
    if (!player || removed) return false;

    // Try to add to player inventory
    if (player->inventory) {
        if (player->inventory->add(itemId, count, damage)) {
            // Successfully added all items
            removed = true;

            // Play pickup sound
            playSound("random.pop", 0.2f,
                ((Mth::random() - Mth::random()) * 0.7f + 1.0f) * 2.0f);

            return true;
        }
    }

    return false;  // Inventory full
}

void ItemEntity::tryMergeNearbyItems() {
    if (!level || count >= 64) return;  // Stack is full

    // Find nearby items of same type
    AABB searchArea = bb.grow(0.5);
    auto entities = level->getEntitiesInArea(searchArea);

    for (Entity* entity : entities) {
        ItemEntity* other = dynamic_cast<ItemEntity*>(entity);
        if (!other || other == this || other->removed) continue;
        if (other->itemId != itemId || other->damage != damage) continue;

        // Merge into this stack
        int spaceLeft = 64 - count;
        int toMerge = std::min(spaceLeft, other->count);

        if (toMerge > 0) {
            count += toMerge;
            other->count -= toMerge;

            if (other->count <= 0) {
                other->removed = true;
            }
        }

        if (count >= 64) break;
    }
}

bool ItemEntity::shouldRender(double camX, double camY, double camZ, float maxDist) const {
    double dx = x - camX;
    double dy = y - camY;
    double dz = z - camZ;
    double distSqr = dx * dx + dy * dy + dz * dz;
    return distSqr < maxDist * maxDist;
}

} // namespace mc
