#include "particle/ExplodeParticle.hpp"
#include "world/Level.hpp"

namespace mc {

ExplodeParticle::ExplodeParticle(Level* level, double x, double y, double z, double xa, double ya, double za)
    : Particle(level, x, y, z, xa, ya, za)
{
    auto randFloat = [this]() { return static_cast<float>(random()) / random.max(); };

    // Override velocity with smaller randomness (matching Java ExplodeParticle)
    xd = xa + (randFloat() * 2.0f - 1.0f) * 0.05f;
    yd = ya + (randFloat() * 2.0f - 1.0f) * 0.05f;
    zd = za + (randFloat() * 2.0f - 1.0f) * 0.05f;

    // Gray color with slight variation (matching Java: 0.7-1.0 range)
    float colorVal = randFloat() * 0.3f + 0.7f;
    rCol = gCol = bCol = colorVal;

    // Larger, more varied size (matching Java)
    size = randFloat() * randFloat() * 6.0f + 1.0f;

    // Longer lifetime (10-82 ticks, averaging ~20)
    lifetime = static_cast<int>(16.0 / (randFloat() * 0.8 + 0.2)) + 2;
}

void ExplodeParticle::tick() {
    prevX = x;
    prevY = y;
    prevZ = z;

    if (age++ >= lifetime) {
        remove();
        return;
    }

    // Animate texture: cycle through 8 textures (7 down to 0)
    // The first row (indices 0-7) in particles.png is the explode animation
    tex = 7 - age * 8 / lifetime;

    // Rise slightly (opposite of gravity)
    yd += 0.004;

    // Move
    move(xd, yd, zd);

    // Apply drag (slower decay than base particle)
    xd *= 0.9;
    yd *= 0.9;
    zd *= 0.9;

    // Ground friction
    if (onGround) {
        xd *= 0.7;
        zd *= 0.7;
    }
}

} // namespace mc
