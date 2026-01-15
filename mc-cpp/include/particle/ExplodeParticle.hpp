#pragma once

#include "particle/Particle.hpp"

namespace mc {

class ExplodeParticle : public Particle {
public:
    ExplodeParticle(Level* level, double x, double y, double z, double xa, double ya, double za);

    void tick() override;
};

} // namespace mc
