#pragma once

#include "particle/Particle.hpp"

namespace mc {

class SmokeParticle : public Particle {
public:
    SmokeParticle(Level* level, double x, double y, double z, double xa, double ya, double za);
    SmokeParticle(Level* level, double x, double y, double z, double xa, double ya, double za, float scale);

    void tick() override;

    // Smoke particles grow as they age (matching Java render behavior)
    float getRenderSize(float partialTick) const override;

private:
    float oSize;  // Original size
};

} // namespace mc
