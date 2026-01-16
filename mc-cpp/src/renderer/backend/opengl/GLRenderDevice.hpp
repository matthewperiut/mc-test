#pragma once

#include "renderer/backend/RenderDevice.hpp"
#include <GL/glew.h>

namespace mc {

class GLRenderDevice : public RenderDevice {
public:
    GLRenderDevice();
    ~GLRenderDevice() override;

    // Lifecycle
    bool init(void* windowHandle) override;
    void shutdown() override;

    // Frame management
    void beginFrame() override;
    void endFrame() override;
    void present() override;

    // Viewport and clear
    void setViewport(int x, int y, int width, int height) override;
    void setClearColor(float r, float g, float b, float a) override;
    void clear(bool color, bool depth) override;

    // Depth state
    void setDepthTest(bool enabled) override;
    void setDepthWrite(bool enabled) override;
    void setDepthFunc(CompareFunc func) override;

    // Culling
    void setCullFace(bool enabled, CullMode mode = CullMode::Back) override;
    void setFrontFace(FrontFace face) override;

    // Blending
    void setBlend(bool enabled, BlendFactor src = BlendFactor::SrcAlpha,
                 BlendFactor dst = BlendFactor::OneMinusSrcAlpha) override;

    // Polygon offset
    void setPolygonOffset(bool enabled, float factor = 0.0f, float units = 0.0f) override;

    // Line width
    void setLineWidth(float width) override;

    // Color mask
    void setColorMask(bool r, bool g, bool b, bool a) override;

    // Factory methods
    std::unique_ptr<ShaderPipeline> createShaderPipeline() override;
    std::unique_ptr<VertexBuffer> createVertexBuffer() override;
    std::unique_ptr<IndexBuffer> createIndexBuffer() override;
    std::unique_ptr<Texture> createTexture() override;

    // Draw commands
    void draw(PrimitiveType primitive, size_t vertexCount, size_t startVertex = 0) override;
    void drawIndexed(PrimitiveType primitive, size_t indexCount, size_t startIndex = 0) override;

    // Vertex attributes
    void setupVertexAttributes() override;

private:
    static GLenum toGLPrimitive(PrimitiveType prim);
    static GLenum toGLCompareFunc(CompareFunc func);
    static GLenum toGLBlendFactor(BlendFactor factor);

    bool initialized;
};

} // namespace mc
