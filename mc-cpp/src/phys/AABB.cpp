#include "phys/AABB.hpp"
#include <algorithm>
#include <cmath>

namespace mc {

std::vector<AABB> AABB::pool(POOL_SIZE);
size_t AABB::poolIndex = 0;

AABB::AABB() : x0(0), y0(0), z0(0), x1(0), y1(0), z1(0) {}

AABB::AABB(double x0, double y0, double z0, double x1, double y1, double z1)
    : x0(x0), y0(y0), z0(z0), x1(x1), y1(y1), z1(z1) {}

AABB AABB::set(double x0, double y0, double z0, double x1, double y1, double z1) {
    this->x0 = x0;
    this->y0 = y0;
    this->z0 = z0;
    this->x1 = x1;
    this->y1 = y1;
    this->z1 = z1;
    return *this;
}

AABB AABB::expand(double dx, double dy, double dz) const {
    double nx0 = x0, ny0 = y0, nz0 = z0;
    double nx1 = x1, ny1 = y1, nz1 = z1;

    if (dx < 0) nx0 += dx;
    if (dx > 0) nx1 += dx;
    if (dy < 0) ny0 += dy;
    if (dy > 0) ny1 += dy;
    if (dz < 0) nz0 += dz;
    if (dz > 0) nz1 += dz;

    return AABB(nx0, ny0, nz0, nx1, ny1, nz1);
}

AABB AABB::grow(double amount) const {
    return AABB(x0 - amount, y0 - amount, z0 - amount,
                x1 + amount, y1 + amount, z1 + amount);
}

AABB AABB::shrink(double amount) const {
    return grow(-amount);
}

AABB AABB::move(double dx, double dy, double dz) const {
    return AABB(x0 + dx, y0 + dy, z0 + dz, x1 + dx, y1 + dy, z1 + dz);
}

bool AABB::intersects(const AABB& other) const {
    return other.x1 > x0 && other.x0 < x1 &&
           other.y1 > y0 && other.y0 < y1 &&
           other.z1 > z0 && other.z0 < z1;
}

bool AABB::intersectsInclusive(const AABB& other) const {
    return other.x1 >= x0 && other.x0 <= x1 &&
           other.y1 >= y0 && other.y0 <= y1 &&
           other.z1 >= z0 && other.z0 <= z1;
}

bool AABB::contains(double x, double y, double z) const {
    return x >= x0 && x < x1 &&
           y >= y0 && y < y1 &&
           z >= z0 && z < z1;
}

double AABB::clipXCollide(const AABB& other, double dx) const {
    if (other.y1 <= y0 || other.y0 >= y1) return dx;
    if (other.z1 <= z0 || other.z0 >= z1) return dx;

    if (dx > 0.0 && other.x1 <= x0) {
        double clip = x0 - other.x1;
        if (clip < dx) dx = clip;
    }
    if (dx < 0.0 && other.x0 >= x1) {
        double clip = x1 - other.x0;
        if (clip > dx) dx = clip;
    }
    return dx;
}

double AABB::clipYCollide(const AABB& other, double dy) const {
    if (other.x1 <= x0 || other.x0 >= x1) return dy;
    if (other.z1 <= z0 || other.z0 >= z1) return dy;

    if (dy > 0.0 && other.y1 <= y0) {
        double clip = y0 - other.y1;
        if (clip < dy) dy = clip;
    }
    if (dy < 0.0 && other.y0 >= y1) {
        double clip = y1 - other.y0;
        if (clip > dy) dy = clip;
    }
    return dy;
}

double AABB::clipZCollide(const AABB& other, double dz) const {
    if (other.x1 <= x0 || other.x0 >= x1) return dz;
    if (other.y1 <= y0 || other.y0 >= y1) return dz;

    if (dz > 0.0 && other.z1 <= z0) {
        double clip = z0 - other.z1;
        if (clip < dz) dz = clip;
    }
    if (dz < 0.0 && other.z0 >= z1) {
        double clip = z1 - other.z0;
        if (clip > dz) dz = clip;
    }
    return dz;
}

bool AABB::containsYZ(const Vec3& vec) const {
    return vec.y >= y0 && vec.y <= y1 && vec.z >= z0 && vec.z <= z1;
}

bool AABB::containsXZ(const Vec3& vec) const {
    return vec.x >= x0 && vec.x <= x1 && vec.z >= z0 && vec.z <= z1;
}

bool AABB::containsXY(const Vec3& vec) const {
    return vec.x >= x0 && vec.x <= x1 && vec.y >= y0 && vec.y <= y1;
}

std::optional<Vec3> AABB::clipX(const Vec3& start, const Vec3& end, double x) const {
    Vec3 result = start.getIntermediateWithX(end, x);
    if (result.lengthSquared() > 0 && containsYZ(result)) {
        return result;
    }
    return std::nullopt;
}

std::optional<Vec3> AABB::clipY(const Vec3& start, const Vec3& end, double y) const {
    Vec3 result = start.getIntermediateWithY(end, y);
    if (result.lengthSquared() > 0 && containsXZ(result)) {
        return result;
    }
    return std::nullopt;
}

std::optional<Vec3> AABB::clipZ(const Vec3& start, const Vec3& end, double z) const {
    Vec3 result = start.getIntermediateWithZ(end, z);
    if (result.lengthSquared() > 0 && containsXY(result)) {
        return result;
    }
    return std::nullopt;
}

std::optional<Vec3> AABB::clip(const Vec3& start, const Vec3& end) const {
    std::optional<Vec3> hitX0 = clipX(start, end, x0);
    std::optional<Vec3> hitX1 = clipX(start, end, x1);
    std::optional<Vec3> hitY0 = clipY(start, end, y0);
    std::optional<Vec3> hitY1 = clipY(start, end, y1);
    std::optional<Vec3> hitZ0 = clipZ(start, end, z0);
    std::optional<Vec3> hitZ1 = clipZ(start, end, z1);

    std::optional<Vec3> closest = std::nullopt;
    double closestDist = std::numeric_limits<double>::max();

    auto checkCloser = [&](std::optional<Vec3>& hit) {
        if (hit) {
            double dist = start.distanceToSquared(*hit);
            if (dist < closestDist) {
                closestDist = dist;
                closest = hit;
            }
        }
    };

    checkCloser(hitX0);
    checkCloser(hitX1);
    checkCloser(hitY0);
    checkCloser(hitY1);
    checkCloser(hitZ0);
    checkCloser(hitZ1);

    return closest;
}

double AABB::getSize() const {
    double dx = x1 - x0;
    double dy = y1 - y0;
    double dz = z1 - z0;
    return (dx + dy + dz) / 3.0;
}

Vec3 AABB::getCenter() const {
    return Vec3((x0 + x1) / 2.0, (y0 + y1) / 2.0, (z0 + z1) / 2.0);
}

AABB* AABB::newTemp(double x0, double y0, double z0, double x1, double y1, double z1) {
    if (poolIndex >= POOL_SIZE) {
        poolIndex = 0;
    }
    pool[poolIndex].set(x0, y0, z0, x1, y1, z1);
    return &pool[poolIndex++];
}

void AABB::resetPool() {
    poolIndex = 0;
}

} // namespace mc
