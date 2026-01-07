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
    , swingTime(0)
    , attackAnim(0.0f)
    , oAttackAnim(0.0f)
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
    blocksBuilding = true;  // Java: Mob sets this to true, prevents blocks being placed inside player

    // Create inventory
    inventory = std::make_unique<Inventory>(this);
}

void Player::tick() {
    Entity::tick();

    // Save previous attackAnim for interpolation (matching Java Mob.baseTick)
    oAttackAnim = attackAnim;

    // Update swing animation (matching Java Player.updateAi exactly)
    if (swinging) {
        ++swingTime;
        if (swingTime == 8) {
            swingTime = 0;
            swinging = false;
        }
    } else {
        swingTime = 0;
    }

    // Calculate attackAnim from swingTime (matching Java Player.updateAi)
    attackAnim = static_cast<float>(swingTime) / 8.0f;

    // Sneaking camera offset (matching Java LocalPlayer.aiStep exactly)
    // When sneaking, ySlideOffset is set to 0.2 if below that
    // ySlideOffset only decays when NOT sneaking (in Entity.move)
    if (sneaking && ySlideOffset < 0.2f) {
        ySlideOffset = 0.2f;
    }

    // Handle movement
    updateMovement();
}

void Player::updateMovement() {
    // Match Java Mob.travel() physics exactly

    if (flying) {
        // Flying movement (creative mode style)
        float flyFriction = 0.91f;
        float speed = flySpeed;
        if (running) speed *= 2.0f;

        moveRelative(moveStrafe, moveForward, speed);

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
        // Normal ground/air movement - matching Java Mob.travel() exactly
        float friction = 0.91f;

        if (onGround) {
            // Ground friction: tile.friction * 0.91 (default tile friction = 0.6)
            friction = 0.54600006f;  // 0.6f * 0.91f

            // Movement speed on ground: 0.1 * (0.16277136 / friction^3)
            float frictionCubed = friction * friction * friction;
            float movementFactor = 0.16277136f / frictionCubed;
            moveRelative(moveStrafe, moveForward, 0.1f * movementFactor);
        } else {
            // Air movement speed is fixed at 0.02
            moveRelative(moveStrafe, moveForward, 0.02f);
        }

        // Recalculate friction for after move (Java does this twice)
        friction = 0.91f;
        if (onGround) {
            friction = 0.54600006f;
        }

        // Move FIRST with current velocity (Java: this.move(this.xd, this.yd, this.zd))
        move(xd, yd, zd);

        // Gravity applied AFTER move (Java: this.yd -= 0.08)
        yd -= 0.08;

        // Vertical drag (Java: this.yd *= 0.98F)
        yd *= 0.98;

        // Horizontal friction (Java: this.xd *= friction, this.zd *= friction)
        xd *= friction;
        zd *= friction;

        return;
    }

    // Apply movement for flying mode
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
    // Java Mob.jumpFromGround(): this.yd = 0.42F
    // Note: Sprint jumping wasn't added until Beta 1.8
    if (onGround && !flying) {
        yd = 0.42;
    }
}

void Player::setRunning(bool r) {
    running = r;
}

void Player::setSneaking(bool s) {
    sneaking = s;
    // Eye height is now gradually transitioned in tick(), not set instantly here
}

void Player::swing() {
    // Match Java Player.swing() exactly
    swingTime = -1;
    swinging = true;
}

float Player::getAttackAnim(float partialTick) const {
    // Match Java Mob.getAttackAnim() - interpolate between frames
    float delta = attackAnim - oAttackAnim;
    if (delta < 0.0f) {
        delta += 1.0f;  // Handle wrap-around
    }
    return oAttackAnim + delta * partialTick;
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
    // Note: swing() is NOT called here - Java only calls swing() once when the player first clicks,
    // not when the block actually breaks. The swing is triggered in Minecraft::handleInput().
    return true;
}

bool Player::placeBlock(int x, int y, int z, int /*face*/, int blockId) {
    if (!level) return false;

    // Check if space is available
    int currentTile = level->getTile(x, y, z);
    if (currentTile != 0) return false;  // Not air

    level->setTile(x, y, z, blockId);
    // Note: swing() is NOT called here - Java calls swing() BEFORE placement in Minecraft::handleMouseClick(),
    // not in this method. The swing is triggered in Minecraft::handleInput().
    return true;
}

void Player::playStepSound(int tileId) {
    // Call parent implementation which handles tile-based sounds
    Entity::playStepSound(tileId);
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
