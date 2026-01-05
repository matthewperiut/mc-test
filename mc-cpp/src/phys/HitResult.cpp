#include "phys/HitResult.hpp"

namespace mc {

HitResult::HitResult()
    : type(HitType::NONE), x(0), y(0), z(0), face(Direction::UP), entity(nullptr) {}

HitResult::HitResult(int x, int y, int z, Direction face, const Vec3& location)
    : type(HitType::TILE), x(x), y(y), z(z), face(face), location(location), entity(nullptr) {}

HitResult::HitResult(Entity* entity)
    : type(HitType::ENTITY), x(0), y(0), z(0), face(Direction::UP), entity(entity) {}

Vec3 HitResult::getDirectionVec(Direction dir) {
    switch (dir) {
        case Direction::DOWN:  return Vec3(0, -1, 0);
        case Direction::UP:    return Vec3(0, 1, 0);
        case Direction::NORTH: return Vec3(0, 0, -1);
        case Direction::SOUTH: return Vec3(0, 0, 1);
        case Direction::WEST:  return Vec3(-1, 0, 0);
        case Direction::EAST:  return Vec3(1, 0, 0);
        default:               return Vec3(0, 0, 0);
    }
}

Direction HitResult::getOpposite(Direction dir) {
    switch (dir) {
        case Direction::DOWN:  return Direction::UP;
        case Direction::UP:    return Direction::DOWN;
        case Direction::NORTH: return Direction::SOUTH;
        case Direction::SOUTH: return Direction::NORTH;
        case Direction::WEST:  return Direction::EAST;
        case Direction::EAST:  return Direction::WEST;
        default:               return Direction::UP;
    }
}

} // namespace mc
