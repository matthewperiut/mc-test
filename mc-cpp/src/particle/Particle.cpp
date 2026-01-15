#include "particle/Particle.hpp"
#include "world/Level.hpp"
#include "util/Mth.hpp"
#include <cmath>

namespace mc {

// Static members
double Particle::xOff = 0.0;
double Particle::yOff = 0.0;
double Particle::zOff = 0.0;

Particle::Particle(Level* level, double x, double y, double z, double xa, double ya, double za)
    : level(level)
    , random(std::random_device{}())
{
    bbWidth = 0.2f;
    bbHeight = 0.2f;
    setPos(x, y, z);

    // Initialize color to white
    rCol = gCol = bCol = 1.0f;

    // Add randomness to velocity (matching Java)
    auto randFloat = [this]() { return static_cast<float>(random()) / random.max(); };

    xd = xa + (randFloat() * 2.0f - 1.0f) * 0.4f;
    yd = ya + (randFloat() * 2.0f - 1.0f) * 0.4f;
    zd = za + (randFloat() * 2.0f - 1.0f) * 0.4f;

    // Normalize and scale velocity
    float speed = (randFloat() + randFloat() + 1.0f) * 0.15f;
    float dd = Mth::sqrt(static_cast<float>(xd * xd + yd * yd + zd * zd));
    xd = xd / dd * speed * 0.4;
    yd = yd / dd * speed * 0.4 + 0.1;
    zd = zd / dd * speed * 0.4;

    // Random texture offset
    uo = randFloat() * 3.0f;
    vo = randFloat() * 3.0f;

    // Random size
    size = (randFloat() * 0.5f + 0.5f) * 2.0f;

    // Random lifetime (4-40 ticks)
    lifetime = static_cast<int>(4.0f / (randFloat() * 0.9f + 0.1f));
    age = 0;
}

Particle& Particle::setPower(float power) {
    xd *= power;
    yd = (yd - 0.1) * power + 0.1;
    zd *= power;
    return *this;
}

Particle& Particle::scale(float s) {
    bbWidth = 0.2f * s;
    bbHeight = 0.2f * s;
    size *= s;
    return *this;
}

void Particle::tick() {
    prevX = x;
    prevY = y;
    prevZ = z;

    if (age++ >= lifetime) {
        remove();
        return;
    }

    // Apply gravity
    yd -= 0.04 * gravity;

    // Move
    move(xd, yd, zd);

    // Apply air resistance
    xd *= 0.98;
    yd *= 0.98;
    zd *= 0.98;

    // Apply ground friction
    if (onGround) {
        xd *= 0.7;
        zd *= 0.7;
    }
}

void Particle::move(double dx, double dy, double dz) {
    if (!level) {
        x += dx;
        y += dy;
        z += dz;
        updateBoundingBox();
        return;
    }

    double originalDx = dx;
    double originalDy = dy;
    double originalDz = dz;

    // Get collision boxes
    AABB expandedBB = bb.expand(dx, dy, dz);
    auto boxes = level->getCollisionBoxes(nullptr, expandedBB);

    // Adjust Y movement
    for (const auto& box : boxes) {
        dy = box.clipYCollide(bb, dy);
    }
    bb = bb.move(0.0, dy, 0.0);

    // Adjust X movement
    for (const auto& box : boxes) {
        dx = box.clipXCollide(bb, dx);
    }
    bb = bb.move(dx, 0.0, 0.0);

    // Adjust Z movement
    for (const auto& box : boxes) {
        dz = box.clipZCollide(bb, dz);
    }
    bb = bb.move(0.0, 0.0, dz);

    // Update position from bounding box
    x = (bb.x0 + bb.x1) / 2.0;
    y = bb.y0;
    z = (bb.z0 + bb.z1) / 2.0;

    // Check for ground collision
    onGround = originalDy != dy && originalDy < 0.0;

    // Stop velocity on collision
    if (originalDx != dx) xd = 0.0;
    if (originalDy != dy) yd = 0.0;
    if (originalDz != dz) zd = 0.0;
}

void Particle::setPos(double newX, double newY, double newZ) {
    x = newX;
    y = newY;
    z = newZ;
    prevX = newX;
    prevY = newY;
    prevZ = newZ;
    updateBoundingBox();
}

void Particle::updateBoundingBox() {
    float w = bbWidth / 2.0f;
    float h = bbHeight;
    bb = AABB(x - w, y, z - w, x + w, y + h, z + w);
}

float Particle::getBrightness() const {
    if (!level) return 1.0f;
    return level->getBrightness(
        static_cast<int>(std::floor(x)),
        static_cast<int>(std::floor(y)),
        static_cast<int>(std::floor(z))
    );
}

} // namespace mc
