#include "MTLRenderContext.hpp"
#include "MTLRenderDevice.hpp"
#include "renderer/backend/metal/MetalBridge.h"

namespace mc {

void MTLRenderContext::configureWindowHints() {
    // Metal doesn't need an OpenGL context
    glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
}

bool MTLRenderContext::init(GLFWwindow* window) {
    this->window = window;

    // Metal-specific initialization is deferred to MTLRenderDevice::init()
    // This method just stores the window handle

    return true;
}

void MTLRenderContext::shutdown() {
    // Clean up any remaining autorelease pool if needed
    if (autoreleasePool) {
        metalReleaseAutoreleasePool(autoreleasePool);
        autoreleasePool = nullptr;
    }
    window = nullptr;
}

void MTLRenderContext::beginFrame() {
    // Create autorelease pool at start of frame to capture all autoreleased Metal objects
    autoreleasePool = metalCreateAutoreleasePool();
}

void MTLRenderContext::endFrame() {
    // Release autorelease pool at end of frame - this cleans up all autoreleased objects
    if (autoreleasePool) {
        metalReleaseAutoreleasePool(autoreleasePool);
        autoreleasePool = nullptr;
    }
}

void MTLRenderContext::setVsync(bool enabled) {
    // Metal vsync is controlled via the render device
    if (RenderDevice::hasInstance()) {
        RenderDevice::get().setVsync(enabled);
    }
}

void MTLRenderContext::handleResize(int width, int height) {
    // Metal needs to recreate drawable on resize
    if (RenderDevice::hasInstance()) {
        auto& device = RenderDevice::get();
        auto* mtlDevice = dynamic_cast<MTLRenderDevice*>(&device);
        if (mtlDevice) {
            mtlDevice->handleResize(width, height);
        }
    }
}

void MTLRenderContext::swapBuffers() {
    // Metal presents in RenderDevice::present(), nothing needed here
}

// Factory function for Metal backend
std::unique_ptr<RenderContext> createRenderContext() {
    return std::make_unique<MTLRenderContext>();
}

} // namespace mc
