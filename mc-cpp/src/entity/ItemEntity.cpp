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
    , throwTime(0)     // No additional delay for normal spawns
    , age(0)
    , lifespan(6000)   // 5 minutes
    , bobOffset(Mth::random() * Mth::PI * 2.0f)
    , beingPickedUp(false)
    , pickupTarget(nullptr)
    , pickupAnimTime(0)
    , pickupStartX(0), pickupStartY(0), pickupStartZ(0)
{
    setSize(0.25f, 0.25f);  // Small collision box
    this->x = x;
    this->y = y;
    this->z = z;
    setPos(x, y, z);

    // Shadow properties (matching Java ItemRenderer)
    shadowRadius = 0.15f;
    shadowStrength = 0.75f;

    // Random initial velocity (like Java)
    xd = (Mth::random() * 0.2 - 0.1);
    yd = 0.2;  // Pop up slightly
    zd = (Mth::random() * 0.2 - 0.1);

    yRot = Mth::random() * 360.0f;
}

ItemEntity::ItemEntity(Level* level, double x, double y, double z, int itemId, int count, int damage,
                       double vx, double vy, double vz, int throwDelay)
    : Entity(level)
    , itemId(itemId)
    , count(count)
    , damage(damage)
    , pickupDelay(0)        // Can be picked up immediately (throwTime handles delay)
    , throwTime(throwDelay) // 40 ticks for player drops
    , age(0)
    , lifespan(6000)        // 5 minutes
    , bobOffset(Mth::random() * Mth::PI * 2.0f)
    , beingPickedUp(false)
    , pickupTarget(nullptr)
    , pickupAnimTime(0)
    , pickupStartX(0), pickupStartY(0), pickupStartZ(0)
{
    setSize(0.25f, 0.25f);  // Small collision box
    this->x = x;
    this->y = y;
    this->z = z;
    setPos(x, y, z);

    // Shadow properties (matching Java ItemRenderer)
    shadowRadius = 0.15f;
    shadowStrength = 0.75f;

    // Use provided velocity
    xd = vx;
    yd = vy;
    zd = vz;

    yRot = Mth::random() * 360.0f;
}

void ItemEntity::tick() {
    // Handle pickup animation
    if (beingPickedUp) {
        pickupAnimTime++;
        if (pickupAnimTime >= 3) {
            // Animation complete - actually remove the item
            removed = true;
        }
        return;  // Don't do normal physics during pickup animation
    }

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

    // Decrease throw time
    if (throwTime > 0) {
        throwTime--;
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
    if (pickupDelay <= 0 && throwTime <= 0 && level) {
        for (Player* player : level->players) {
            if (player && canPickUp(player)) {
                playerPickUp(player);
                break;
            }
        }
    }
}

bool ItemEntity::canPickUp(Player* player) const {
    if (!player || pickupDelay > 0 || throwTime > 0 || beingPickedUp) return false;

    // Check distance - pickup radius is about 1.5 blocks
    double dx = player->x - x;
    double dy = (player->y + player->eyeHeight * 0.5) - y;
    double dz = player->z - z;
    double distSqr = dx * dx + dy * dy + dz * dz;

    return distSqr < 1.5 * 1.5;  // Within 1.5 blocks
}

bool ItemEntity::playerPickUp(Player* player) {
    if (!player || removed || beingPickedUp) return false;

    // Try to add to player inventory
    if (player->inventory) {
        if (player->inventory->add(itemId, count, damage)) {
            // Successfully added all items - start pickup animation
            startPickupAnimation(player);

            // Play pickup sound
            playSound("random.pop", 0.2f,
                ((Mth::random() - Mth::random()) * 0.7f + 1.0f) * 2.0f);

            return true;
        }
    }

    return false;  // Inventory full
}

void ItemEntity::startPickupAnimation(Player* player) {
    beingPickedUp = true;
    pickupTarget = player;
    pickupAnimTime = 0;
    pickupStartX = x;
    pickupStartY = y;
    pickupStartZ = z;
}

void ItemEntity::getPickupAnimatedPos(float partialTick, double& outX, double& outY, double& outZ) const {
    if (!beingPickedUp || !pickupTarget) {
        outX = prevX + (x - prevX) * partialTick;
        outY = prevY + (y - prevY) * partialTick;
        outZ = prevZ + (z - prevZ) * partialTick;
        return;
    }

    // Calculate interpolation factor (0 to 1 over 3 ticks)
    float t = (static_cast<float>(pickupAnimTime) + partialTick) / 3.0f;
    t = t * t;  // Quadratic ease-in (matching Java TakeAnimationParticle)

    // Get player's interpolated position
    double playerX = pickupTarget->prevX + (pickupTarget->x - pickupTarget->prevX) * partialTick;
    double playerY = pickupTarget->prevY + (pickupTarget->y - pickupTarget->prevY) * partialTick;
    playerY += pickupTarget->eyeHeight * 0.5;  // Aim for chest area
    double playerZ = pickupTarget->prevZ + (pickupTarget->z - pickupTarget->prevZ) * partialTick;

    // Interpolate from start position to player
    outX = pickupStartX + (playerX - pickupStartX) * t;
    outY = pickupStartY + (playerY - pickupStartY) * t;
    outZ = pickupStartZ + (playerZ - pickupStartZ) * t;
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
        if (other->beingPickedUp) continue;  // Don't merge with items being picked up

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
