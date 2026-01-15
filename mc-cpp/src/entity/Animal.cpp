#include "entity/Animal.hpp"
#include "world/Level.hpp"
#include "world/tile/Tile.hpp"
#include "util/Mth.hpp"

namespace mc {

Animal::Animal(Level* level)
    : PathfinderMob(level)
{
}

float Animal::getWalkTargetValue(int x, int y, int z) {
    if (!level) return 0.0f;

    // Animals prefer grass blocks
    int tileBelow = level->getTile(x, y - 1, z);
    if (tileBelow == Tile::GRASS) {
        return 10.0f;
    }

    // Otherwise prefer bright areas
    return level->getBrightness(x, y, z) - 0.5f;
}

} // namespace mc
