#pragma once

#include <memory>
#include <GLFW/glfw3.h>

namespace mc {

/**
 * RenderContext manages platform-specific rendering initialization and frame management.
 * This abstraction encapsulates all Metal vs OpenGL differences that were previously
 * scattered throughout Minecraft.cpp with #ifdef preprocessor directives.
 *
 * Responsibilities:
 * - Configuring GLFW window hints before window creation
 * - Platform-specific initialization after window creation
 * - Per-frame resource management (Metal autorelease pools, etc.)
 * - Vsync control
 * - Window resize handling
 */
class RenderContext {
public:
    virtual ~RenderContext() = default;

    /**
     * Configure GLFW window hints before window creation.
     * Must be called before glfwCreateWindow().
     *
     * For Metal: Sets GLFW_CLIENT_API to GLFW_NO_API
     * For OpenGL: Sets context version and profile hints
     */
    virtual void configureWindowHints() = 0;

    /**
     * Initialize render context after GLFW window creation.
     *
     * For OpenGL: Makes context current and initializes GLEW
     * For Metal: Sets up Metal layer and other Metal-specific initialization
     */
    virtual bool init(GLFWwindow* window) = 0;

    /**
     * Shutdown render context and clean up resources.
     */
    virtual void shutdown() = 0;

    /**
     * Called at the start of each frame.
     *
     * For Metal: Creates autorelease pool
     * For OpenGL: Nothing needed
     */
    virtual void beginFrame() = 0;

    /**
     * Called at the end of each frame.
     *
     * For Metal: Releases autorelease pool
     * For OpenGL: Nothing needed
     */
    virtual void endFrame() = 0;

    /**
     * Set vsync enabled/disabled.
     *
     * For OpenGL: Uses glfwSwapInterval()
     * For Metal: Uses display link or present timing
     */
    virtual void setVsync(bool enabled) = 0;

    /**
     * Handle window resize event.
     *
     * For Metal: Recreates depth texture and drawable
     * For OpenGL: Nothing needed (handled by viewport)
     */
    virtual void handleResize(int width, int height) = 0;

    /**
     * Swap buffers (OpenGL only).
     * Called after RenderDevice::present().
     *
     * For OpenGL: Calls glfwSwapBuffers()
     * For Metal: Does nothing (Metal presents in RenderDevice::present())
     */
    virtual void swapBuffers() = 0;

    /**
     * Get the GLFW window handle.
     */
    virtual GLFWwindow* getWindow() const = 0;

    // Singleton access
    static std::unique_ptr<RenderContext>& getInstance();
    static RenderContext* get();
    static void setInstance(std::unique_ptr<RenderContext> context);

private:
    static std::unique_ptr<RenderContext> instance;
};

// Factory function - implemented per-backend
std::unique_ptr<RenderContext> createRenderContext();

} // namespace mc
