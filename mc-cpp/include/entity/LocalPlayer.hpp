#pragma once

#include "entity/Player.hpp"

struct GLFWwindow;

namespace mc {

class Minecraft;

class LocalPlayer : public Player {
public:
    // Reference to game
    Minecraft* minecraft;

    // Input state
    bool keyUp, keyDown, keyLeft, keyRight;
    bool keyJump, keySneak;
    bool keyAttack, keyUse;

    // Camera bobbing
    float oBob, bob;
    float oTilt, tilt;

    // Breaking/building
    int attackTime;
    int destroyProgress;

    LocalPlayer(Level* level, Minecraft* minecraft);

    void tick() override;

    // Input handling
    void setKey(int key, bool pressed);
    void handleInput(GLFWwindow* window);

    // Camera
    float getBobbing(float partialTick) const;
    float getTilt(float partialTick) const;

private:
    void processInput();
};

} // namespace mc
