#include "entity/Entity.hpp"
#include "world/Level.hpp"
#include "util/Mth.hpp"
#include <cmath>
#include <algorithm>

namespace mc {

int Entity::nextEntityId = 0;

Entity::Entity(Level* level)
    : x(0), y(0), z(0)
    , prevX(0), prevY(0), prevZ(0)
    , xd(0), yd(0), zd(0)
    , yRot(0), xRot(0)
    , prevYRot(0), prevXRot(0)
    , bbWidth(0.6f), bbHeight(1.8f)
    , onGround(false)
    , horizontalCollision(false)
    , verticalCollision(false)
    , collision(false)
    , inWater(false)
    , inLava(false)
    , stepHeight(0.0f)
    , fallDistance(0.0f)
    , walkDist(0.0f)
    , oWalkDist(0.0f)
    , tickCount(0)
    , noPhysicsCount(0)
    , removed(false)
    , heightOffset(0.0f)
    , eyeHeight(1.62f)
    , noClip(false)
    , blocksBuilding(false)  // Java: Entity sets this to false, Mob sets to true
    , level(level)
    , entityId(nextEntityId++)
{
    setSize(0.6f, 1.8f);
}

void Entity::tick() {
    baseTick();
}

void Entity::baseTick() {
    prevX = x;
    prevY = y;
    prevZ = z;
    prevYRot = yRot;
    prevXRot = xRot;
    oWalkDist = walkDist;

    tickCount++;

    // Water and lava checks
    handleWaterMovement();
    handleLavaMovement();
}

void Entity::setPos(double x, double y, double z) {
    this->x = x;
    this->y = y;
    this->z = z;

    float hw = bbWidth / 2.0f;
    float hh = bbHeight / 2.0f;

    bb = AABB(x - hw, y - hh + heightOffset, z - hw,
              x + hw, y + hh + heightOffset, z + hw);
}

void Entity::setSize(float width, float height) {
    bbWidth = width;
    bbHeight = height;
    updateBoundingBox();
}

void Entity::updateBoundingBox() {
    float hw = bbWidth / 2.0f;
    bb = AABB(x - hw, y, z - hw, x + hw, y + bbHeight, z + hw);
}

void Entity::move(double dx, double dy, double dz) {
    if (noPhysicsCount > 0 || noClip) {
        x += dx;
        y += dy;
        z += dz;
        updateBoundingBox();
        return;
    }

    double origDx = dx;
    double origDy = dy;
    double origDz = dz;

    if (level) {
        // Get collision boxes from level
        auto boxes = level->getCollisionBoxes(this, bb.expand(dx, dy, dz));

        // Clip Y first (gravity)
        for (const auto& box : boxes) {
            dy = box.clipYCollide(bb, dy);
        }
        bb = bb.move(0, dy, 0);

        // Clip X
        for (const auto& box : boxes) {
            dx = box.clipXCollide(bb, dx);
        }
        bb = bb.move(dx, 0, 0);

        // Clip Z
        for (const auto& box : boxes) {
            dz = box.clipZCollide(bb, dz);
        }
        bb = bb.move(0, 0, dz);
    } else {
        bb = bb.move(dx, dy, dz);
    }

    // Update position from bounding box
    x = (bb.x0 + bb.x1) / 2.0;
    y = bb.y0;
    z = (bb.z0 + bb.z1) / 2.0;

    // Accumulate walk distance for view bobbing (matching Java Entity.move)
    float horizDist = std::sqrt(static_cast<float>(dx * dx + dz * dz));
    walkDist += horizDist * 0.6f;

    // Check collisions
    horizontalCollision = origDx != dx || origDz != dz;
    verticalCollision = origDy != dy;
    onGround = origDy != dy && origDy < 0;
    collision = horizontalCollision || verticalCollision;

    // Handle landing
    if (onGround) {
        if (fallDistance > 0.0f) {
            // Could trigger fall damage here
            fallDistance = 0.0f;
        }
    } else if (dy < 0) {
        fallDistance -= static_cast<float>(dy);
    }

    // Stop velocity on collision
    if (origDx != dx) xd = 0;
    if (origDy != dy) yd = 0;
    if (origDz != dz) zd = 0;
}

void Entity::moveRelative(float strafe, float forward, float friction) {
    float dist = strafe * strafe + forward * forward;
    if (dist >= 0.0001f) {
        dist = std::sqrt(dist);
        if (dist < 1.0f) dist = 1.0f;

        dist = friction / dist;
        strafe *= dist;
        forward *= dist;

        float sinYaw = Mth::sin(yRot * Mth::DEG_TO_RAD);
        float cosYaw = Mth::cos(yRot * Mth::DEG_TO_RAD);

        xd += strafe * cosYaw - forward * sinYaw;
        zd += forward * cosYaw + strafe * sinYaw;
    }
}

void Entity::turn(float yaw, float pitch) {
    yRot += yaw * 0.15f;
    xRot -= pitch * 0.15f;

    // Clamp pitch
    xRot = std::clamp(xRot, -90.0f, 90.0f);
}

Vec3 Entity::getLookVector() const {
    float pitchRad = -xRot * Mth::DEG_TO_RAD;
    float yawRad = -yRot * Mth::DEG_TO_RAD;

    float cosPitch = Mth::cos(pitchRad);
    float sinPitch = Mth::sin(pitchRad);
    float cosYaw = Mth::cos(yawRad);
    float sinYaw = Mth::sin(yawRad);

    return Vec3(sinYaw * cosPitch, sinPitch, cosYaw * cosPitch);
}

double Entity::getInterpolatedX(float partialTick) const {
    return prevX + (x - prevX) * partialTick;
}

double Entity::getInterpolatedY(float partialTick) const {
    return prevY + (y - prevY) * partialTick;
}

double Entity::getInterpolatedZ(float partialTick) const {
    return prevZ + (z - prevZ) * partialTick;
}

float Entity::getInterpolatedYRot(float partialTick) const {
    return prevYRot + Mth::wrapDegrees(yRot - prevYRot) * partialTick;
}

float Entity::getInterpolatedXRot(float partialTick) const {
    return prevXRot + (xRot - prevXRot) * partialTick;
}

bool Entity::isColliding(const AABB& box) const {
    return bb.intersects(box);
}

bool Entity::isInWall() const {
    // Simplified check
    return false;
}

bool Entity::isInWater() const {
    return inWater;
}

bool Entity::isInLava() const {
    return inLava;
}

bool Entity::isUnderwater() const {
    // Check if eye is in water
    return inWater;  // Simplified
}

void Entity::handleWaterMovement() {
    // Check if in water block
    inWater = false;  // Would need level access to check
}

void Entity::handleLavaMovement() {
    // Check if in lava block
    inLava = false;  // Would need level access to check
}

void Entity::checkOnGround() {
    // Already handled in move()
}

double Entity::distanceTo(const Entity& other) const {
    double dx = other.x - x;
    double dy = other.y - y;
    double dz = other.z - z;
    return std::sqrt(dx * dx + dy * dy + dz * dz);
}

double Entity::distanceToSqr(double tx, double ty, double tz) const {
    double dx = tx - x;
    double dy = ty - y;
    double dz = tz - z;
    return dx * dx + dy * dy + dz * dz;
}

void Entity::playStepSound(int /*tileId*/) {
    // Would use sound engine
}

void Entity::playSound(const std::string& /*sound*/, float /*volume*/, float /*pitch*/) {
    // Would use sound engine
}

void Entity::hurt(Entity* /*source*/, int /*damage*/) {
    // Override in subclasses
}

void Entity::push(double dx, double dy, double dz) {
    xd += dx;
    yd += dy;
    zd += dz;
}

bool Entity::shouldRender(double camX, double camY, double camZ, float maxDist) const {
    double dx = x - camX;
    double dy = y - camY;
    double dz = z - camZ;
    double distSq = dx * dx + dy * dy + dz * dz;
    return distSq < maxDist * maxDist;
}

} // namespace mc
