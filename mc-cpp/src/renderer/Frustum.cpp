#include "renderer/Frustum.hpp"
#include <GL/glew.h>
#include <cmath>

namespace mc {

Frustum& Frustum::getInstance() {
    static Frustum instance;
    return instance;
}

void Frustum::update() {
    extractPlanes();
}

void Frustum::extractPlanes() {
    float projection[16];
    float modelview[16];
    float clip[16];

    glGetFloatv(GL_PROJECTION_MATRIX, projection);
    glGetFloatv(GL_MODELVIEW_MATRIX, modelview);

    // Multiply projection * modelview
    for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
            clip[i * 4 + j] =
                modelview[i * 4 + 0] * projection[0 * 4 + j] +
                modelview[i * 4 + 1] * projection[1 * 4 + j] +
                modelview[i * 4 + 2] * projection[2 * 4 + j] +
                modelview[i * 4 + 3] * projection[3 * 4 + j];
        }
    }

    // Extract planes from clip matrix
    // Right plane
    planes[0][0] = clip[3] - clip[0];
    planes[0][1] = clip[7] - clip[4];
    planes[0][2] = clip[11] - clip[8];
    planes[0][3] = clip[15] - clip[12];
    normalizePlane(0);

    // Left plane
    planes[1][0] = clip[3] + clip[0];
    planes[1][1] = clip[7] + clip[4];
    planes[1][2] = clip[11] + clip[8];
    planes[1][3] = clip[15] + clip[12];
    normalizePlane(1);

    // Bottom plane
    planes[2][0] = clip[3] + clip[1];
    planes[2][1] = clip[7] + clip[5];
    planes[2][2] = clip[11] + clip[9];
    planes[2][3] = clip[15] + clip[13];
    normalizePlane(2);

    // Top plane
    planes[3][0] = clip[3] - clip[1];
    planes[3][1] = clip[7] - clip[5];
    planes[3][2] = clip[11] - clip[9];
    planes[3][3] = clip[15] - clip[13];
    normalizePlane(3);

    // Far plane
    planes[4][0] = clip[3] - clip[2];
    planes[4][1] = clip[7] - clip[6];
    planes[4][2] = clip[11] - clip[10];
    planes[4][3] = clip[15] - clip[14];
    normalizePlane(4);

    // Near plane
    planes[5][0] = clip[3] + clip[2];
    planes[5][1] = clip[7] + clip[6];
    planes[5][2] = clip[11] + clip[10];
    planes[5][3] = clip[15] + clip[14];
    normalizePlane(5);
}

void Frustum::normalizePlane(int plane) {
    float length = std::sqrt(
        planes[plane][0] * planes[plane][0] +
        planes[plane][1] * planes[plane][1] +
        planes[plane][2] * planes[plane][2]
    );

    if (length > 0) {
        planes[plane][0] /= length;
        planes[plane][1] /= length;
        planes[plane][2] /= length;
        planes[plane][3] /= length;
    }
}

bool Frustum::isVisible(const AABB& box) const {
    return isVisible(box.x0, box.y0, box.z0, box.x1, box.y1, box.z1);
}

bool Frustum::isVisible(double x0, double y0, double z0, double x1, double y1, double z1) const {
    for (int i = 0; i < 6; i++) {
        float px = planes[i][0] > 0 ? static_cast<float>(x1) : static_cast<float>(x0);
        float py = planes[i][1] > 0 ? static_cast<float>(y1) : static_cast<float>(y0);
        float pz = planes[i][2] > 0 ? static_cast<float>(z1) : static_cast<float>(z0);

        float dist = planes[i][0] * px + planes[i][1] * py + planes[i][2] * pz + planes[i][3];
        if (dist < 0) {
            return false;
        }
    }
    return true;
}

bool Frustum::containsPoint(double x, double y, double z) const {
    for (int i = 0; i < 6; i++) {
        float dist = planes[i][0] * static_cast<float>(x) +
                     planes[i][1] * static_cast<float>(y) +
                     planes[i][2] * static_cast<float>(z) +
                     planes[i][3];
        if (dist < 0) {
            return false;
        }
    }
    return true;
}

} // namespace mc
