#pragma once

#include "phys/AABB.hpp"
#include "phys/Vec3.hpp"
#include <cstdint>

namespace mc {

class Level;

class Entity {
public:
    // Position
    double x, y, z;
    double prevX, prevY, prevZ;

    // Velocity
    double xd, yd, zd;

    // Rotation
    float yRot, xRot;        // Yaw, Pitch
    float prevYRot, prevXRot;

    // Bounding box
    AABB bb;
    float bbWidth;
    float bbHeight;

    // Physics state
    bool onGround;
    bool horizontalCollision;
    bool verticalCollision;
    bool collision;
    bool inWater;
    bool inLava;

    // Step height (blocks entity can step up)
    float stepHeight;

    // Fall damage
    float fallDistance;

    // Walk distance (for view bobbing oscillation)
    float walkDist;
    float oWalkDist;

    // Timing
    int tickCount;
    int noPhysicsCount;

    // State
    bool removed;
    float heightOffset;
    float eyeHeight;
    bool noClip;  // Noclip mode - no collision detection

    // Block placement check (Java: blocksBuilding)
    // If true, blocks cannot be placed inside this entity
    bool blocksBuilding;

    // Reference to level
    Level* level;

    // Entity ID
    int entityId;
    static int nextEntityId;

    Entity(Level* level);
    virtual ~Entity() = default;

    // Core update
    virtual void tick();
    virtual void baseTick();

    // Movement and physics
    virtual void move(double dx, double dy, double dz);
    virtual void moveRelative(float strafe, float forward, float friction);
    void setPos(double x, double y, double z);
    void setSize(float width, float height);
    void turn(float yaw, float pitch);

    // Collision helpers
    bool isColliding(const AABB& box) const;
    bool isInWall() const;
    bool isInWater() const;
    bool isInLava() const;
    bool isUnderwater() const;

    // Position getters
    Vec3 getPosition() const { return Vec3(x, y, z); }
    Vec3 getEyePosition() const { return Vec3(x, y + eyeHeight, z); }
    Vec3 getLookVector() const;

    // Interpolation for rendering
    double getInterpolatedX(float partialTick) const;
    double getInterpolatedY(float partialTick) const;
    double getInterpolatedZ(float partialTick) const;
    float getInterpolatedYRot(float partialTick) const;
    float getInterpolatedXRot(float partialTick) const;

    // Distance calculations
    double distanceTo(const Entity& other) const;
    double distanceToSqr(double x, double y, double z) const;

    // Sound
    virtual void playStepSound(int tileId);
    virtual void playSound(const std::string& sound, float volume, float pitch);

    // Damage
    virtual void hurt(Entity* source, int damage);
    virtual void push(double dx, double dy, double dz);

    // State queries
    virtual bool isPickable() const { return false; }
    virtual bool isPushable() const { return false; }
    virtual bool isAlive() const { return !removed; }

    // Rendering
    virtual float getShadowRadius() const { return bbWidth; }
    virtual bool shouldRender(double camX, double camY, double camZ, float maxDist) const;

protected:
    void updateBoundingBox();
    void handleWaterMovement();
    void handleLavaMovement();
    void checkOnGround();
};

} // namespace mc
