#include "phys/Vec3.hpp"
#include "util/Mth.hpp"
#include <cmath>

namespace mc {

std::vector<Vec3> Vec3::pool(POOL_SIZE);
size_t Vec3::poolIndex = 0;

Vec3::Vec3() : x(0), y(0), z(0) {}

Vec3::Vec3(double x, double y, double z) : x(x), y(y), z(z) {}

Vec3 Vec3::set(double x, double y, double z) {
    this->x = x;
    this->y = y;
    this->z = z;
    return *this;
}

Vec3 Vec3::add(double dx, double dy, double dz) const {
    return Vec3(x + dx, y + dy, z + dz);
}

Vec3 Vec3::add(const Vec3& other) const {
    return Vec3(x + other.x, y + other.y, z + other.z);
}

Vec3 Vec3::subtract(const Vec3& other) const {
    return Vec3(x - other.x, y - other.y, z - other.z);
}

Vec3 Vec3::scale(double factor) const {
    return Vec3(x * factor, y * factor, z * factor);
}

Vec3 Vec3::normalize() const {
    double len = length();
    if (len < 1.0E-4) {
        return Vec3(0, 0, 0);
    }
    return Vec3(x / len, y / len, z / len);
}

double Vec3::dot(const Vec3& other) const {
    return x * other.x + y * other.y + z * other.z;
}

Vec3 Vec3::cross(const Vec3& other) const {
    return Vec3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    );
}

double Vec3::length() const {
    return std::sqrt(x * x + y * y + z * z);
}

double Vec3::lengthSquared() const {
    return x * x + y * y + z * z;
}

double Vec3::distanceTo(const Vec3& other) const {
    double dx = other.x - x;
    double dy = other.y - y;
    double dz = other.z - z;
    return std::sqrt(dx * dx + dy * dy + dz * dz);
}

double Vec3::distanceToSquared(const Vec3& other) const {
    double dx = other.x - x;
    double dy = other.y - y;
    double dz = other.z - z;
    return dx * dx + dy * dy + dz * dz;
}

Vec3 Vec3::xRot(float angle) const {
    float c = Mth::cos(angle);
    float s = Mth::sin(angle);
    double newY = y * c + z * s;
    double newZ = z * c - y * s;
    return Vec3(x, newY, newZ);
}

Vec3 Vec3::yRot(float angle) const {
    float c = Mth::cos(angle);
    float s = Mth::sin(angle);
    double newX = x * c + z * s;
    double newZ = z * c - x * s;
    return Vec3(newX, y, newZ);
}

Vec3 Vec3::getIntermediateWithX(const Vec3& other, double targetX) const {
    double dx = other.x - x;
    double dy = other.y - y;
    double dz = other.z - z;
    if (dx * dx < 1.0E-7) {
        return Vec3();
    }
    double t = (targetX - x) / dx;
    if (t < 0.0 || t > 1.0) {
        return Vec3();
    }
    return Vec3(x + dx * t, y + dy * t, z + dz * t);
}

Vec3 Vec3::getIntermediateWithY(const Vec3& other, double targetY) const {
    double dx = other.x - x;
    double dy = other.y - y;
    double dz = other.z - z;
    if (dy * dy < 1.0E-7) {
        return Vec3();
    }
    double t = (targetY - y) / dy;
    if (t < 0.0 || t > 1.0) {
        return Vec3();
    }
    return Vec3(x + dx * t, y + dy * t, z + dz * t);
}

Vec3 Vec3::getIntermediateWithZ(const Vec3& other, double targetZ) const {
    double dx = other.x - x;
    double dy = other.y - y;
    double dz = other.z - z;
    if (dz * dz < 1.0E-7) {
        return Vec3();
    }
    double t = (targetZ - z) / dz;
    if (t < 0.0 || t > 1.0) {
        return Vec3();
    }
    return Vec3(x + dx * t, y + dy * t, z + dz * t);
}

Vec3* Vec3::newTemp(double x, double y, double z) {
    if (poolIndex >= POOL_SIZE) {
        poolIndex = 0;
    }
    pool[poolIndex].set(x, y, z);
    return &pool[poolIndex++];
}

void Vec3::resetPool() {
    poolIndex = 0;
}

} // namespace mc
