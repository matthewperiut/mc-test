#include "particle/SmokeParticle.hpp"
#include "world/Level.hpp"

namespace mc {

SmokeParticle::SmokeParticle(Level* level, double x, double y, double z, double xa, double ya, double za)
    : SmokeParticle(level, x, y, z, xa, ya, za, 1.0f)
{
}

SmokeParticle::SmokeParticle(Level* level, double x, double y, double z, double xa, double ya, double za, float scale)
    : Particle(level, x, y, z, 0.0, 0.0, 0.0)
{
    auto randFloat = [this]() { return static_cast<float>(random()) / random.max(); };

    // Scale down base velocity and add input velocity (matching Java)
    xd *= 0.1;
    yd *= 0.1;
    zd *= 0.1;
    xd += xa;
    yd += ya;
    zd += za;

    // Gray color with variation (0.0 - 0.3) matching Java
    float colorVal = randFloat() * 0.3f;
    rCol = gCol = bCol = colorVal;

    // Size scaling (matching Java: size *= 0.75F; size *= scale)
    size *= 0.75f;
    size *= scale;
    oSize = size;

    // Lifetime: affected by scale (matching Java)
    lifetime = static_cast<int>(8.0 / (randFloat() * 0.8 + 0.2));
    lifetime = static_cast<int>(static_cast<float>(lifetime) * scale);

    // Start with texture 7 (will cycle down to 0)
    tex = 7;
}

void SmokeParticle::tick() {
    prevX = x;
    prevY = y;
    prevZ = z;

    if (age++ >= lifetime) {
        remove();
        return;
    }

    // Animate texture: cycle from 7 down to 0 (smoke dissipation animation)
    tex = 7 - age * 8 / lifetime;

    // Rise upward (matching Java: yd += 0.004)
    yd += 0.004;

    // Move with collision (noPhysics = false in Java)
    move(xd, yd, zd);

    // Horizontal spread when blocked (matching Java: if y == yo)
    if (y == prevY) {
        xd *= 1.1;
        zd *= 1.1;
    }

    // Apply drag (matching Java: 0.96F)
    xd *= 0.96;
    yd *= 0.96;
    zd *= 0.96;

    // Ground friction
    if (onGround) {
        xd *= 0.7;
        zd *= 0.7;
    }
}

float SmokeParticle::getRenderSize(float partialTick) const {
    // Matching Java SmokeParticle.render():
    // float l = ((float)this.age + a) / (float)this.lifetime * 32.0F;
    // if (l < 0.0F) l = 0.0F;
    // if (l > 1.0F) l = 1.0F;
    // this.size = this.oSize * l;
    float l = (static_cast<float>(age) + partialTick) / static_cast<float>(lifetime) * 32.0f;
    if (l < 0.0f) l = 0.0f;
    if (l > 1.0f) l = 1.0f;
    return oSize * l;
}

} // namespace mc
