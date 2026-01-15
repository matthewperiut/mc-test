#include "renderer/model/ChickenModel.hpp"
#include "renderer/Tesselator.hpp"
#include <cmath>

namespace mc {

constexpr float PI = 3.14159265358979f;

ChickenModel::ChickenModel()
    : head(0, 0)
    , beak(14, 0)
    , redThing(14, 4)
    , body(0, 9)
    , leg0(26, 0)
    , leg1(26, 0)
    , wing0(24, 13)
    , wing1(24, 13)
{
    // Y offset (chicken sits 16 pixels up in the model space)
    int yo = 16;

    // Head: 4x6x3 at offset (-2, -6, -2), positioned at (0, yo-1, -4)
    head.addBox(-2.0f, -6.0f, -2.0f, 4, 6, 3);
    head.setPos(0.0f, static_cast<float>(-1 + yo), -4.0f);

    // Beak: 4x2x2 at offset (-2, -4, -4), positioned at (0, yo-1, -4)
    beak.addBox(-2.0f, -4.0f, -4.0f, 4, 2, 2);
    beak.setPos(0.0f, static_cast<float>(-1 + yo), -4.0f);

    // Wattle (red thing): 2x2x2 at offset (-1, -2, -3), positioned at (0, yo-1, -4)
    redThing.addBox(-1.0f, -2.0f, -3.0f, 2, 2, 2);
    redThing.setPos(0.0f, static_cast<float>(-1 + yo), -4.0f);

    // Body: 6x8x6 at offset (-3, -4, -3), positioned at (0, yo, 0)
    body.addBox(-3.0f, -4.0f, -3.0f, 6, 8, 6);
    body.setPos(0.0f, static_cast<float>(yo), 0.0f);

    // Leg0: 3x5x3 at offset (-1, 0, -3), positioned at (-2, yo+3, 1)
    leg0.addBox(-1.0f, 0.0f, -3.0f, 3, 5, 3);
    leg0.setPos(-2.0f, static_cast<float>(3 + yo), 1.0f);

    // Leg1: 3x5x3 at offset (-1, 0, -3), positioned at (1, yo+3, 1)
    leg1.addBox(-1.0f, 0.0f, -3.0f, 3, 5, 3);
    leg1.setPos(1.0f, static_cast<float>(3 + yo), 1.0f);

    // Wing0: 1x4x6 at offset (0, 0, -3), positioned at (-4, yo-3, 0)
    wing0.addBox(0.0f, 0.0f, -3.0f, 1, 4, 6);
    wing0.setPos(-4.0f, static_cast<float>(-3 + yo), 0.0f);

    // Wing1: 1x4x6 at offset (-1, 0, -3), positioned at (4, yo-3, 0)
    wing1.addBox(-1.0f, 0.0f, -3.0f, 1, 4, 6);
    wing1.setPos(4.0f, static_cast<float>(-3 + yo), 0.0f);
}

void ChickenModel::setupAnim(float time, float walkSpeed, float bob, float yRot, float xRot) {
    // Convert degrees to radians for rotation
    constexpr float DEG_TO_RAD = PI / 180.0f;

    // Head follows view direction
    head.xRot = -(xRot * DEG_TO_RAD);
    head.yRot = yRot * DEG_TO_RAD;

    // Beak and wattle follow head
    beak.xRot = head.xRot;
    beak.yRot = head.yRot;
    redThing.xRot = head.xRot;
    redThing.yRot = head.yRot;

    // Body is rotated 90 degrees (horizontal)
    body.xRot = PI / 2.0f;

    // Legs animate with walking
    // time * 0.6662 is the walking frequency
    // walkSpeed scales the amplitude
    leg0.xRot = std::cos(time * 0.6662f) * 1.4f * walkSpeed;
    leg1.xRot = std::cos(time * 0.6662f + PI) * 1.4f * walkSpeed;

    // Wings flap based on bob value
    wing0.zRot = bob;
    wing1.zRot = -bob;
}

void ChickenModel::render(Tesselator& t, float scale) {
    render(t, scale, 1.0f, 1.0f, 1.0f, 1.0f);
}

void ChickenModel::render(Tesselator& t, float scale, float r, float g, float b, float a) {
    head.render(t, scale, r, g, b, a);
    beak.render(t, scale, r, g, b, a);
    redThing.render(t, scale, r, g, b, a);
    body.render(t, scale, r, g, b, a);
    leg0.render(t, scale, r, g, b, a);
    leg1.render(t, scale, r, g, b, a);
    wing0.render(t, scale, r, g, b, a);
    wing1.render(t, scale, r, g, b, a);
}

} // namespace mc
