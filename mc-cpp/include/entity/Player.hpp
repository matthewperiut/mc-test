#pragma once

#include "entity/Entity.hpp"
#include "item/Inventory.hpp"
#include <string>
#include <array>
#include <memory>

namespace mc {

class Player : public Entity {
public:
    // Player name
    std::string name;

    // Health and hunger
    int health;
    int maxHealth;
    int foodLevel;
    float foodSaturation;

    // Movement state
    bool running;
    bool sneaking;
    bool flying;
    bool creative;

    // Inventory
    std::unique_ptr<Inventory> inventory;
    int selectedSlot;  // Convenience accessor (also in inventory)

    // Arm swing animation (matching Java Player exactly)
    int swingTime;      // Integer counter: -1 to 7, then resets
    float attackAnim;   // Calculated as swingTime / 8.0f
    float oAttackAnim;  // Previous frame for interpolation
    bool swinging;

    // Input state (set by keyboard handling)
    float moveStrafe;
    float moveForward;
    bool jumping;

    // Experience
    int experienceLevel;
    float experienceProgress;
    int totalExperience;

    // Respawn position
    double spawnX, spawnY, spawnZ;

    // Abilities
    float walkSpeed;
    float flySpeed;

    Player(Level* level);
    virtual ~Player() = default;

    void tick() override;

    // Movement
    virtual void travel(float strafe, float forward);
    void jump();

    // Actions
    virtual void attack(Entity* target);
    virtual void interact(Entity* target);
    virtual bool destroyBlock(int x, int y, int z);
    virtual bool placeBlock(int x, int y, int z, int face, int blockId);

    // Drop item from selected hotbar slot (Q key)
    void drop();
    // Drop specific item stack with optional death throw mode
    void drop(int itemId, int count, int damage, bool deathThrow = false);

    // State
    void setRunning(bool running);
    void setSneaking(bool sneaking);
    bool isSneaking() const override { return sneaking; }
    bool canFly() const { return creative || flying; }

    // Animation
    void swing();
    float getAttackAnim(float partialTick) const;

    // Sound
    void playStepSound(int tileId) override;

    // Health
    void heal(int amount);
    void hurt(Entity* source, int damage) override;
    bool isAlive() const override { return health > 0; }

    // Respawn
    void respawn();
    void setSpawnPoint(double x, double y, double z);

protected:
    void updateMovement();
    float getMovementSpeed() const;
};

} // namespace mc
