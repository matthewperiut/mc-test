#pragma once

struct GLFWwindow;

namespace mc {

class MouseHandler {
public:
    MouseHandler();

    void init(GLFWwindow* window);
    void grab();
    void release();
    void poll();

    bool isGrabbed() const { return grabbed; }

    int getDeltaX() const { return deltaX; }
    int getDeltaY() const { return deltaY; }

    double getX() const { return mouseX; }
    double getY() const { return mouseY; }

private:
    GLFWwindow* window;
    bool grabbed;
    double mouseX, mouseY;
    double lastMouseX, lastMouseY;
    int deltaX, deltaY;
};

} // namespace mc
