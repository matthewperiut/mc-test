#pragma once

#include "renderer/backend/RenderContext.hpp"

namespace mc {

class GLRenderContext : public RenderContext {
public:
    GLRenderContext() = default;
    ~GLRenderContext() override = default;

    void configureWindowHints() override;
    bool init(GLFWwindow* window) override;
    void shutdown() override;
    void beginFrame() override;
    void endFrame() override;
    void setVsync(bool enabled) override;
    void handleResize(int width, int height) override;
    void swapBuffers() override;
    GLFWwindow* getWindow() const override { return window; }

private:
    GLFWwindow* window = nullptr;
};

} // namespace mc
