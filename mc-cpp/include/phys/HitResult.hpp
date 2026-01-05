#pragma once

#include "phys/Vec3.hpp"

namespace mc {

enum class HitType {
    NONE,
    TILE,
    ENTITY
};

enum class Direction {
    DOWN = 0,
    UP = 1,
    NORTH = 2,  // -Z
    SOUTH = 3,  // +Z
    WEST = 4,   // -X
    EAST = 5    // +X
};

class Entity; // Forward declaration

class HitResult {
public:
    HitType type;
    int x, y, z;        // Block coordinates (for TILE hits)
    Direction face;      // Face of block hit
    Vec3 location;       // Exact hit location
    Entity* entity;      // Hit entity (for ENTITY hits)

    HitResult();
    HitResult(int x, int y, int z, Direction face, const Vec3& location);
    HitResult(Entity* entity);

    bool isTile() const { return type == HitType::TILE; }
    bool isEntity() const { return type == HitType::ENTITY; }
    bool isMiss() const { return type == HitType::NONE; }

    // Get the direction vector of the face
    static Vec3 getDirectionVec(Direction dir);
    static Direction getOpposite(Direction dir);
};

} // namespace mc
