#pragma once

#include "phys/Vec3.hpp"
#include <vector>
#include <optional>

namespace mc {

class AABB {
public:
    double x0, y0, z0;  // Min corner
    double x1, y1, z1;  // Max corner

    AABB();
    AABB(double x0, double y0, double z0, double x1, double y1, double z1);

    AABB set(double x0, double y0, double z0, double x1, double y1, double z1);
    AABB expand(double dx, double dy, double dz) const;
    AABB grow(double amount) const;
    AABB shrink(double amount) const;
    AABB move(double dx, double dy, double dz) const;

    bool intersects(const AABB& other) const;
    bool intersectsInclusive(const AABB& other) const;
    bool contains(double x, double y, double z) const;

    double clipXCollide(const AABB& other, double dx) const;
    double clipYCollide(const AABB& other, double dy) const;
    double clipZCollide(const AABB& other, double dz) const;

    std::optional<Vec3> clip(const Vec3& start, const Vec3& end) const;

    double getSize() const;
    Vec3 getCenter() const;

    // Object pool for performance
    static AABB* newTemp(double x0, double y0, double z0, double x1, double y1, double z1);
    static void resetPool();

private:
    std::optional<Vec3> clipX(const Vec3& start, const Vec3& end, double x) const;
    std::optional<Vec3> clipY(const Vec3& start, const Vec3& end, double y) const;
    std::optional<Vec3> clipZ(const Vec3& start, const Vec3& end, double z) const;
    bool containsYZ(const Vec3& vec) const;
    bool containsXZ(const Vec3& vec) const;
    bool containsXY(const Vec3& vec) const;

    static std::vector<AABB> pool;
    static size_t poolIndex;
    static constexpr size_t POOL_SIZE = 1000;
};

} // namespace mc
