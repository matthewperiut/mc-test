#include "GLRenderContext.hpp"

namespace mc {

void GLRenderContext::configureWindowHints() {
    // Request OpenGL 3.3 core profile
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

#ifdef __APPLE__
    // macOS requires forward compatibility flag
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
#endif
}

bool GLRenderContext::init(GLFWwindow* window) {
    this->window = window;

    // Make context current
    glfwMakeContextCurrent(window);

    // Default vsync off
    glfwSwapInterval(0);

    return true;
}

void GLRenderContext::shutdown() {
    // Nothing specific needed for OpenGL context shutdown
    window = nullptr;
}

void GLRenderContext::beginFrame() {
    // Nothing needed for OpenGL per-frame setup
}

void GLRenderContext::endFrame() {
    // Nothing needed for OpenGL per-frame cleanup
}

void GLRenderContext::setVsync(bool enabled) {
    if (window) {
        glfwSwapInterval(enabled ? 1 : 0);
    }
}

void GLRenderContext::handleResize(int width, int height) {
    // OpenGL viewport is handled by RenderDevice::setViewport()
    // Nothing platform-specific needed here
    (void)width;
    (void)height;
}

void GLRenderContext::swapBuffers() {
    if (window) {
        glfwSwapBuffers(window);
    }
}

// Factory function for OpenGL backend
std::unique_ptr<RenderContext> createRenderContext() {
    return std::make_unique<GLRenderContext>();
}

} // namespace mc
