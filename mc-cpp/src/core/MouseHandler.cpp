#include "core/MouseHandler.hpp"
#include <GLFW/glfw3.h>

namespace mc {

MouseHandler::MouseHandler()
    : window(nullptr)
    , grabbed(false)
    , mouseX(0), mouseY(0)
    , lastMouseX(0), lastMouseY(0)
    , deltaX(0), deltaY(0)
{}

void MouseHandler::init(GLFWwindow* window) {
    this->window = window;
    glfwGetCursorPos(window, &mouseX, &mouseY);
    lastMouseX = mouseX;
    lastMouseY = mouseY;
}

void MouseHandler::grab() {
    if (!window || grabbed) return;

    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    if (glfwRawMouseMotionSupported()) {
        glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
    }
    grabbed = true;

    // Reset position to center
    glfwGetCursorPos(window, &mouseX, &mouseY);
    lastMouseX = mouseX;
    lastMouseY = mouseY;
    deltaX = 0;
    deltaY = 0;
}

void MouseHandler::release() {
    if (!window || !grabbed) return;

    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    if (glfwRawMouseMotionSupported()) {
        glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, GLFW_FALSE);
    }

#ifndef GLFW_PLATFORM_WAYLAND
    // Center the cursor in the window
    int width, height;
    glfwGetWindowSize(window, &width, &height);
    glfwSetCursorPos(window, width / 2.0, height / 2.0);
#endif

    grabbed = false;
}

void MouseHandler::poll() {
    if (!window) return;

    glfwGetCursorPos(window, &mouseX, &mouseY);
    deltaX = static_cast<int>(mouseX - lastMouseX);
    // GLFW Y increases downward, but LWJGL Y increases upward
    // Negate to match Java Minecraft behavior
    deltaY = -static_cast<int>(mouseY - lastMouseY);
    lastMouseX = mouseX;
    lastMouseY = mouseY;
}

} // namespace mc
