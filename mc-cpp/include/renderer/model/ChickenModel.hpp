#pragma once

#include "renderer/model/ModelPart.hpp"

namespace mc {

class Tesselator;

// Chicken model matching Java ChickenModel
class ChickenModel {
public:
    ModelPart head;
    ModelPart beak;
    ModelPart redThing;  // Wattle
    ModelPart body;
    ModelPart leg0;
    ModelPart leg1;
    ModelPart wing0;
    ModelPart wing1;

    ChickenModel();

    // Set up animation poses
    // time: walking animation time
    // walkSpeed: speed of walk animation (0-1)
    // bob: wing flap bob value
    // yRot: head yaw rotation (degrees)
    // xRot: head pitch rotation (degrees)
    void setupAnim(float time, float walkSpeed, float bob, float yRot, float xRot);

    // Render the model
    void render(Tesselator& t, float scale);
    void render(Tesselator& t, float scale, float r, float g, float b, float a);
};

} // namespace mc
