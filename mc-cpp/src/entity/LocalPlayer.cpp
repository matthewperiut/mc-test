#include "entity/LocalPlayer.hpp"
#include "core/Minecraft.hpp"
#include "core/KeyMapping.hpp"
#include <GLFW/glfw3.h>
#include <cmath>

namespace mc {

LocalPlayer::LocalPlayer(Level* level, Minecraft* minecraft)
    : Player(level)
    , minecraft(minecraft)
    , keyUp(false), keyDown(false), keyLeft(false), keyRight(false)
    , keyJump(false), keySneak(false)
    , keyAttack(false), keyUse(false)
    , oBob(0), bob(0)
    , oTilt(0), tilt(0)
    , attackTime(0)
    , destroyProgress(0)
{
}

void LocalPlayer::tick() {
    oBob = bob;
    oTilt = tilt;

    // Process input before movement
    processInput();

    // Call parent tick
    Player::tick();

    // Update camera bobbing
    float moveSpeed = static_cast<float>(std::sqrt(xd * xd + zd * zd));
    float desiredBob = moveSpeed * 0.6f;

    if (onGround) {
        bob += (desiredBob - bob) * 0.4f;
    } else {
        bob *= 0.8f;
    }

    // Tilt for damage
    tilt *= 0.9f;

    // Attack cooldown
    if (attackTime > 0) {
        attackTime--;
    }
}

void LocalPlayer::processInput() {
    // Calculate movement from input state
    moveStrafe = 0.0f;
    moveForward = 0.0f;

    if (keyUp) moveForward += 1.0f;
    if (keyDown) moveForward -= 1.0f;
    if (keyLeft) moveStrafe += 1.0f;
    if (keyRight) moveStrafe -= 1.0f;

    // Running
    running = keyUp && !keySneak && onGround;

    // Sneaking
    setSneaking(keySneak);

    // Jumping
    if (keyJump) {
        if (flying) {
            yd += 0.15;
        } else if (onGround) {
            jump();
        }
    }
}

void LocalPlayer::setKey(int key, bool pressed) {
    // Map GLFW keys to input state
    if (key == GLFW_KEY_W) keyUp = pressed;
    else if (key == GLFW_KEY_S) keyDown = pressed;
    else if (key == GLFW_KEY_A) keyLeft = pressed;
    else if (key == GLFW_KEY_D) keyRight = pressed;
    else if (key == GLFW_KEY_SPACE) keyJump = pressed;
    else if (key == GLFW_KEY_LEFT_SHIFT) keySneak = pressed;
}

void LocalPlayer::handleInput(GLFWwindow* window) {
    keyUp = glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS;
    keyDown = glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS;
    keyLeft = glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS;
    keyRight = glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS;
    keyJump = glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS;
    keySneak = glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS;
    keyAttack = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;
    keyUse = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS;
}

void LocalPlayer::releaseAllKeys() {
    // Match Java: just reset key states, movement recalculated in tick()
    keyUp = false;
    keyDown = false;
    keyLeft = false;
    keyRight = false;
    keyJump = false;
    keySneak = false;
    keyAttack = false;
    keyUse = false;
}

float LocalPlayer::getBobbing(float partialTick) const {
    return oBob + (bob - oBob) * partialTick;
}

float LocalPlayer::getTilt(float partialTick) const {
    return oTilt + (tilt - oTilt) * partialTick;
}

} // namespace mc
