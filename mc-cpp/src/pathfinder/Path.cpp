#include "pathfinder/Path.hpp"
#include "entity/Entity.hpp"

namespace mc {

Vec3 Path::current(const Entity& entity) const {
    if (pos >= static_cast<int>(nodes.size())) {
        return Vec3(0, 0, 0);
    }

    double px = static_cast<double>(nodes[pos]->x) + static_cast<double>(static_cast<int>(entity.bbWidth + 1.0f)) * 0.5;
    double py = static_cast<double>(nodes[pos]->y);
    double pz = static_cast<double>(nodes[pos]->z) + static_cast<double>(static_cast<int>(entity.bbWidth + 1.0f)) * 0.5;

    return Vec3(px, py, pz);
}

} // namespace mc
