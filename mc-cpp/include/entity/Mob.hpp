#pragma once

#include "entity/Entity.hpp"
#include <random>
#include <string>

namespace mc {

class Mob : public Entity {
public:
    // Health system
    int health = 10;
    int lastHealth = 10;
    int hurtTime = 0;
    int hurtDuration = 10;
    float hurtDir = 0.0f;
    int deathTime = 0;
    int attackTime = 0;
    int invulnerableTime = 0;
    int invulnerableDuration = 20;
    int lastHurt = 0;
    bool dead = false;

    // Body rotation (separate from head)
    float yBodyRot = 0.0f;
    float yBodyRotO = 0.0f;

    // Walking animation
    float walkAnimSpeed = 0.0f;
    float walkAnimSpeedO = 0.0f;
    float walkAnimPos = 0.0f;
    float animStep = 0.0f;
    float animStepO = 0.0f;
    float oRun = 0.0f;
    float run = 0.0f;

    // Attack animation
    float attackAnim = 0.0f;
    float oAttackAnim = 0.0f;

    // AI movement inputs
    float xxa = 0.0f;  // strafe
    float yya = 0.0f;  // forward
    float yRotA = 0.0f; // rotation acceleration
    bool jumping = false;
    float runSpeed = 0.7f;
    float defaultLookAngle = 0.0f;

    // No action timer (for despawning)
    int noActionTime = 0;

    // Animation
    float timeOffs = 0.0f;
    float rotA = 0.0f;

    // Random for AI
    std::mt19937 random;

    // Looking at entity
    Entity* lookingAt = nullptr;
    int lookTime = 0;

    // Texture
    std::string textureName = "/mob/char.png";

    Mob(Level* level);
    virtual ~Mob() = default;

    // Core update
    void tick() override;
    void baseTick() override;

    // AI methods
    virtual void aiStep();
    virtual void updateAi();

    // Movement
    virtual void travel(float strafe, float forward);
    virtual void jumpFromGround();

    // Damage
    void hurt(Entity* source, int damage) override;
    virtual void actuallyHurt(int damage);
    virtual void knockback(Entity* source, int damage, double dx, double dz);
    virtual void die(Entity* killer);
    virtual void dropDeathLoot();

    // Fall damage
    virtual void causeFallDamage(float distance);

    // State queries
    bool isAlive() const override { return !removed && health > 0; }
    bool isPushable() const override { return !removed; }
    bool isPickable() const override { return !removed; }
    virtual bool canSee(const Entity& other) const;

    // Sounds
    virtual std::string getAmbientSound() { return ""; }
    virtual std::string getHurtSound() { return "random.hurt"; }
    virtual std::string getDeathSound() { return "random.hurt"; }
    virtual float getSoundVolume() { return 1.0f; }
    virtual int getAmbientSoundInterval() { return 80; }

    // Loot
    virtual int getDeathLoot() { return 0; }

    // Texture
    virtual std::string getTexture() { return textureName; }

    // Animation
    float getAttackAnim(float partialTick) const;

    // Helper
    void lookAt(Entity& target, float maxTurn);

protected:
    float rotlerp(float current, float target, float maxDelta);

private:
    int ambientSoundTime = 0;
};

} // namespace mc
