#pragma once

#include "phys/AABB.hpp"

namespace mc {

class Frustum {
public:
    static Frustum& getInstance();

    void update();

    bool isVisible(const AABB& box) const;
    bool isVisible(double x0, double y0, double z0, double x1, double y1, double z1) const;
    bool containsPoint(double x, double y, double z) const;

private:
    Frustum() = default;

    // Frustum planes (6 planes: left, right, bottom, top, near, far)
    float planes[6][4];

    void extractPlanes();
    void normalizePlane(int plane);
};

} // namespace mc
