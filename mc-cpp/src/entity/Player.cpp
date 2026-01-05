#include "entity/Player.hpp"
#include "world/Level.hpp"
#include "util/Mth.hpp"
#include <algorithm>

namespace mc {

Player::Player(Level* level)
    : Entity(level)
    , name("Player")
    , health(20)
    , maxHealth(20)
    , foodLevel(20)
    , foodSaturation(5.0f)
    , running(false)
    , sneaking(false)
    , flying(false)
    , creative(false)
    , selectedSlot(0)
    , swingProgress(0.0f)
    , swingProgressInt(0)
    , swinging(false)
    , moveStrafe(0.0f)
    , moveForward(0.0f)
    , jumping(false)
    , experienceLevel(0)
    , experienceProgress(0.0f)
    , totalExperience(0)
    , spawnX(0), spawnY(64), spawnZ(0)
    , walkSpeed(0.1f)
    , flySpeed(0.05f)
{
    setSize(0.6f, 1.8f);
    eyeHeight = 1.62f;
    stepHeight = 0.5f;
}

void Player::tick() {
    Entity::tick();

    // Update swing animation
    if (swinging) {
        swingProgressInt++;
        if (swingProgressInt >= 8) {
            swingProgressInt = 0;
            swinging = false;
        }
    } else {
        swingProgressInt = 0;
    }
    swingProgress = static_cast<float>(swingProgressInt) / 8.0f;

    // Handle movement
    updateMovement();
}

void Player::updateMovement() {
    float speed = getMovementSpeed();

    // Apply friction and movement
    if (flying) {
        // Flying movement
        float flyFriction = 0.91f;
        moveRelative(moveStrafe, moveForward, flySpeed * speed);

        xd *= flyFriction;
        yd *= flyFriction;
        zd *= flyFriction;

        if (jumping) {
            yd += 0.15;
        }
        if (sneaking) {
            yd -= 0.15;
        }
    } else {
        // Ground/air movement
        float friction = 0.91f;
        float groundFriction = 0.546f;  // 0.6 * 0.91

        if (onGround) {
            groundFriction = 0.546f;  // Could vary by block
            moveRelative(moveStrafe, moveForward, walkSpeed * speed);
        } else {
            moveRelative(moveStrafe, moveForward, 0.02f);  // Air control
        }

        // Gravity
        yd -= 0.08;

        // Friction
        xd *= friction;
        yd *= 0.98;  // Air resistance
        zd *= friction;

        if (onGround) {
            xd *= groundFriction / 0.91f;
            zd *= groundFriction / 0.91f;
        }
    }

    // Apply movement
    move(xd, yd, zd);
}

float Player::getMovementSpeed() const {
    float speed = 1.0f;

    if (running && !sneaking) {
        speed *= 1.3f;
    }
    if (sneaking) {
        speed *= 0.3f;
    }

    return speed;
}

void Player::travel(float strafe, float forward) {
    moveStrafe = strafe;
    moveForward = forward;
}

void Player::jump() {
    if (onGround && !flying) {
        yd = 0.42;  // Jump velocity

        if (running) {
            // Jump boost when running
            float yawRad = yRot * Mth::DEG_TO_RAD;
            xd -= Mth::sin(yawRad) * 0.2f;
            zd += Mth::cos(yawRad) * 0.2f;
        }
    }
}

void Player::setRunning(bool r) {
    running = r;
}

void Player::setSneaking(bool s) {
    sneaking = s;
    if (s) {
        eyeHeight = 1.5f;
    } else {
        eyeHeight = 1.62f;
    }
}

void Player::swing() {
    if (!swinging) {
        swingProgressInt = -1;
        swinging = true;
    }
}

float Player::getSwingProgress(float partialTick) const {
    float progress = static_cast<float>(swingProgressInt) + partialTick;
    return progress / 8.0f;
}

void Player::attack(Entity* target) {
    if (target && target != this) {
        target->hurt(this, 1);
        swing();
    }
}

void Player::interact(Entity* /*target*/) {
    // Interact with entity (right click)
}

bool Player::destroyBlock(int x, int y, int z) {
    if (!level) return false;

    // Would check if block is breakable, apply tool speed, etc.
    level->setTile(x, y, z, 0);  // Set to air
    swing();
    return true;
}

bool Player::placeBlock(int x, int y, int z, int /*face*/, int blockId) {
    if (!level) return false;

    // Check if space is available
    int currentTile = level->getTile(x, y, z);
    if (currentTile != 0) return false;  // Not air

    level->setTile(x, y, z, blockId);
    swing();
    return true;
}

void Player::playStepSound(int /*tileId*/) {
    // Play step sound based on tile
    playSound("step.stone", 0.5f, 1.0f);
}

void Player::heal(int amount) {
    health = std::min(health + amount, maxHealth);
}

void Player::hurt(Entity* /*source*/, int damage) {
    health -= damage;
    if (health <= 0) {
        health = 0;
        // Handle death
    }
}

void Player::respawn() {
    health = maxHealth;
    setPos(spawnX, spawnY, spawnZ);
    xd = yd = zd = 0;
    fallDistance = 0;
}

void Player::setSpawnPoint(double x, double y, double z) {
    spawnX = x;
    spawnY = y;
    spawnZ = z;
}

} // namespace mc
