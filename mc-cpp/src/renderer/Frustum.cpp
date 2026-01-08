#include "renderer/Frustum.hpp"
#include "renderer/MatrixStack.hpp"
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
    // Get matrices from MatrixStack instead of legacy OpenGL state
    glm::mat4 proj = MatrixStack::projection().get();
    glm::mat4 mv = MatrixStack::modelview().get();

    // Compute clip matrix = projection * modelview
    glm::mat4 clipMat = proj * mv;

    // Extract to row-major float array for our plane extraction
    float clip[16];
    const float* pClip = glm::value_ptr(clipMat);
    // GLM stores matrices in column-major, transpose for our calculation
    for (int col = 0; col < 4; col++) {
        for (int row = 0; row < 4; row++) {
            clip[row * 4 + col] = pClip[col * 4 + row];
        }
    }

    // Extract planes from clip matrix (row-major storage after transpose)
    // Standard Gribb/Hartmann method: plane = row[3] +/- row[i]
    // In row-major: row[i] = clip[i*4 + 0..3]

    // Right plane: row3 - row0
    planes[0][0] = clip[12] - clip[0];
    planes[0][1] = clip[13] - clip[1];
    planes[0][2] = clip[14] - clip[2];
    planes[0][3] = clip[15] - clip[3];
    normalizePlane(0);

    // Left plane: row3 + row0
    planes[1][0] = clip[12] + clip[0];
    planes[1][1] = clip[13] + clip[1];
    planes[1][2] = clip[14] + clip[2];
    planes[1][3] = clip[15] + clip[3];
    normalizePlane(1);

    // Bottom plane: row3 + row1
    planes[2][0] = clip[12] + clip[4];
    planes[2][1] = clip[13] + clip[5];
    planes[2][2] = clip[14] + clip[6];
    planes[2][3] = clip[15] + clip[7];
    normalizePlane(2);

    // Top plane: row3 - row1
    planes[3][0] = clip[12] - clip[4];
    planes[3][1] = clip[13] - clip[5];
    planes[3][2] = clip[14] - clip[6];
    planes[3][3] = clip[15] - clip[7];
    normalizePlane(3);

    // Far plane: row3 - row2
    planes[4][0] = clip[12] - clip[8];
    planes[4][1] = clip[13] - clip[9];
    planes[4][2] = clip[14] - clip[10];
    planes[4][3] = clip[15] - clip[11];
    normalizePlane(4);

    // Near plane: row3 + row2
    planes[5][0] = clip[12] + clip[8];
    planes[5][1] = clip[13] + clip[9];
    planes[5][2] = clip[14] + clip[10];
    planes[5][3] = clip[15] + clip[11];
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
