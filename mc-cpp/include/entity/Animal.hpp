#pragma once

#include "entity/PathfinderMob.hpp"

namespace mc {

class Animal : public PathfinderMob {
public:
    Animal(Level* level);
    virtual ~Animal() = default;

    int getAmbientSoundInterval() override { return 120; }

protected:
    float getWalkTargetValue(int x, int y, int z) override;
};

} // namespace mc
