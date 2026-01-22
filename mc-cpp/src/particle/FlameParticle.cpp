#include "particle/FlameParticle.hpp"
#include "world/Level.hpp"

namespace mc {

FlameParticle::FlameParticle(Level* level, double x, double y, double z, double xa, double ya, double za)
    : Particle(level, x, y, z, 0.0, 0.0, 0.0)
{
    auto randFloat = [this]() { return static_cast<float>(random()) / random.max(); };

    // Override velocity with smaller values (matching Java FlameParticle)
    xd = xd * 0.01f + xa;
    yd = yd * 0.01f + ya;
    zd = zd * 0.01f + za;

    // Small random position offset (Java does this but doesn't use the result)
    // x + (randFloat() - randFloat()) * 0.05f;
    // y + (randFloat() - randFloat()) * 0.05f;
    // z + (randFloat() - randFloat()) * 0.05f;

    // Store original size
    oSize = size;

    // White/yellow color (matching Java: rCol = gCol = bCol = 1.0F)
    rCol = gCol = bCol = 1.0f;

    // Lifetime: 8-12 ticks (matching Java: 8.0 / (random * 0.8 + 0.2) + 4)
    lifetime = static_cast<int>(8.0 / (randFloat() * 0.8 + 0.2)) + 4;

    // No physics (matching Java: noPhysics = true)
    // We'll handle this by not calling move() with collision

    // Texture index 48 (flame texture in particles.png)
    tex = 48;
}

void FlameParticle::tick() {
    prevX = x;
    prevY = y;
    prevZ = z;

    if (age++ >= lifetime) {
        remove();
        return;
    }

    // Move without collision (noPhysics = true in Java)
    x += xd;
    y += yd;
    z += zd;

    // Apply drag (matching Java: 0.96F)
    xd *= 0.96;
    yd *= 0.96;
    zd *= 0.96;

    // Ground friction (shouldn't happen often since noPhysics, but included for completeness)
    if (onGround) {
        xd *= 0.7;
        zd *= 0.7;
    }
}

float FlameParticle::getRenderSize(float partialTick) const {
    // Matching Java FlameParticle.render():
    // float s = ((float)this.age + a) / (float)this.lifetime;
    // this.size = this.oSize * (1.0F - s * s * 0.5F);
    float s = (static_cast<float>(age) + partialTick) / static_cast<float>(lifetime);
    return oSize * (1.0f - s * s * 0.5f);
}

} // namespace mc
