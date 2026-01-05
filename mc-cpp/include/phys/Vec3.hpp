#pragma once

#include <vector>
#include <memory>

namespace mc {

class Vec3 {
public:
    double x, y, z;

    Vec3();
    Vec3(double x, double y, double z);

    Vec3 set(double x, double y, double z);
    Vec3 add(double dx, double dy, double dz) const;
    Vec3 add(const Vec3& other) const;
    Vec3 subtract(const Vec3& other) const;
    Vec3 scale(double factor) const;
    Vec3 normalize() const;

    double dot(const Vec3& other) const;
    Vec3 cross(const Vec3& other) const;

    double length() const;
    double lengthSquared() const;
    double distanceTo(const Vec3& other) const;
    double distanceToSquared(const Vec3& other) const;

    Vec3 xRot(float angle) const;
    Vec3 yRot(float angle) const;

    Vec3 getIntermediateWithX(const Vec3& other, double x) const;
    Vec3 getIntermediateWithY(const Vec3& other, double y) const;
    Vec3 getIntermediateWithZ(const Vec3& other, double z) const;

    // Object pool for performance (like Java version)
    static Vec3* newTemp(double x, double y, double z);
    static void resetPool();

private:
    static std::vector<Vec3> pool;
    static size_t poolIndex;
    static constexpr size_t POOL_SIZE = 2000;
};

} // namespace mc
