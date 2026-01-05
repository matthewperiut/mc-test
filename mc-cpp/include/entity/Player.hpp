#pragma once

#include "entity/Entity.hpp"
#include <string>
#include <array>

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

    // Inventory (simplified - would be a separate class)
    int selectedSlot;

    // Arm swing animation
    float swingProgress;
    int swingProgressInt;
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

    // State
    void setRunning(bool running);
    void setSneaking(bool sneaking);
    bool canFly() const { return creative || flying; }

    // Animation
    void swing();
    float getSwingProgress(float partialTick) const;

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
