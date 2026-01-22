#pragma once

#include "particle/Particle.hpp"

namespace mc {

class FlameParticle : public Particle {
public:
    FlameParticle(Level* level, double x, double y, double z, double xa, double ya, double za);

    void tick() override;

    // Flame particles shrink as they age (matching Java render behavior)
    float getRenderSize(float partialTick) const override;

private:
    float oSize;  // Original size
};

} // namespace mc
