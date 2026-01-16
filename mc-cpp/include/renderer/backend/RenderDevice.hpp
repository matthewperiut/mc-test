#pragma once

#include "RenderTypes.hpp"
#include "ShaderPipeline.hpp"
#include "VertexBuffer.hpp"
#include "Texture.hpp"
#include <memory>

namespace mc {

class RenderDevice {
public:
    virtual ~RenderDevice() = default;

    // Lifecycle
    virtual bool init(void* windowHandle) = 0;
    virtual void shutdown() = 0;

    // Frame management
    virtual void beginFrame() = 0;
    virtual void endFrame() = 0;
    virtual void present() = 0;

    // Viewport and clear
    virtual void setViewport(int x, int y, int width, int height) = 0;
    virtual void setClearColor(float r, float g, float b, float a) = 0;
    virtual void clear(bool color, bool depth) = 0;

    // Depth state
    virtual void setDepthTest(bool enabled) = 0;
    virtual void setDepthWrite(bool enabled) = 0;
    virtual void setDepthFunc(CompareFunc func) = 0;

    // Culling
    virtual void setCullFace(bool enabled, CullMode mode = CullMode::Back) = 0;
    virtual void setFrontFace(FrontFace face) = 0;

    // Blending
    virtual void setBlend(bool enabled, BlendFactor src = BlendFactor::SrcAlpha,
                         BlendFactor dst = BlendFactor::OneMinusSrcAlpha) = 0;

    // Polygon offset (for selection highlight rendering)
    virtual void setPolygonOffset(bool enabled, float factor = 0.0f, float units = 0.0f) = 0;

    // Line width
    virtual void setLineWidth(float width) = 0;

    // Color mask (for depth-only passes)
    virtual void setColorMask(bool r, bool g, bool b, bool a) = 0;

    // Factory methods
    virtual std::unique_ptr<ShaderPipeline> createShaderPipeline() = 0;
    virtual std::unique_ptr<VertexBuffer> createVertexBuffer() = 0;
    virtual std::unique_ptr<IndexBuffer> createIndexBuffer() = 0;
    virtual std::unique_ptr<Texture> createTexture() = 0;

    // Draw commands
    virtual void draw(PrimitiveType primitive, size_t vertexCount, size_t startVertex = 0) = 0;
    virtual void drawIndexed(PrimitiveType primitive, size_t indexCount, size_t startIndex = 0) = 0;

    // Setup vertex attributes for the standard vertex format
    virtual void setupVertexAttributes() = 0;

    // Vsync control (Metal-specific, OpenGL uses glfwSwapInterval)
    virtual void setVsync(bool enabled) { (void)enabled; }

    // Singleton access
    static RenderDevice& get();
    static void setInstance(std::unique_ptr<RenderDevice> device);
    static bool hasInstance();

private:
    static std::unique_ptr<RenderDevice> instance;
};

// Factory function - implemented per-backend
std::unique_ptr<RenderDevice> createRenderDevice();

} // namespace mc
