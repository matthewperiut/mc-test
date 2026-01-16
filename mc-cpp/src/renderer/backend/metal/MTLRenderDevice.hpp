#pragma once

#include "renderer/backend/RenderDevice.hpp"
#include "MTLShaderPipeline.hpp"  // For BlendMode enum

// Forward declarations for metal-cpp types
namespace MTL {
    class Device;
    class CommandQueue;
    class CommandBuffer;
    class RenderCommandEncoder;
    class RenderPassDescriptor;
    class DepthStencilState;
    class Texture;
    class Buffer;
    class RenderPipelineState;
}

namespace CA {
    class MetalLayer;
    class MetalDrawable;
}

namespace mc {

class MTLVertexBuffer;
class MTLIndexBuffer;

class MTLRenderDevice : public RenderDevice {
public:
    MTLRenderDevice();
    ~MTLRenderDevice() override;

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

    // Vsync control
    void setVsync(bool enabled) override;

    // Metal-specific accessors
    MTL::Device* getDevice() const { return device; }
    MTL::CommandQueue* getCommandQueue() const { return commandQueue; }
    MTL::RenderCommandEncoder* getCurrentEncoder() const { return renderEncoder; }

    // Window resize handling
    void handleResize(int width, int height);

    // Mid-frame depth clear (restarts render pass)
    void clearDepthMidFrame();

    // State tracking for drawing
    void setCurrentPipeline(MTLShaderPipeline* pipeline) { currentPipeline = pipeline; }
    void setCurrentVertexBuffer(MTLVertexBuffer* buffer) { currentVertexBuffer = buffer; }
    void setCurrentIndexBuffer(MTLIndexBuffer* buffer) { currentIndexBuffer = buffer; }
    MTLShaderPipeline* getCurrentPipeline() const { return currentPipeline; }

private:
    void createDepthStencilStates();
    void createDepthTexture();
    int toMTLPrimitive(mc::PrimitiveType prim);  // Returns MTL::PrimitiveType as int

    MTL::Device* device;
    MTL::CommandQueue* commandQueue;
    MTL::CommandBuffer* commandBuffer;
    MTL::RenderCommandEncoder* renderEncoder;
    MTL::RenderPassDescriptor* renderPassDesc;

    // Metal layer (from bridge)
    void* metalLayer;  // CA::MetalLayer*
    void* currentDrawable;  // CA::MetalDrawable*

    // Depth/stencil states
    MTL::DepthStencilState* depthTestEnabled;
    MTL::DepthStencilState* depthTestDisabled;
    MTL::DepthStencilState* depthWriteDisabled;

    // Depth texture
    MTL::Texture* depthTexture;

    // Window handle for resize
    void* windowHandle;
    int viewportWidth;
    int viewportHeight;
    float scaleFactor;

    // Clear color
    float clearR, clearG, clearB, clearA;

    // Current state
    bool depthTestState;
    bool depthWriteState;
    bool cullFaceState;
    CullMode cullModeState;
    BlendMode currentBlendMode;
    bool blendEnabled;

    // Current drawing state
    MTLShaderPipeline* currentPipeline;
    MTLVertexBuffer* currentVertexBuffer;
    MTLIndexBuffer* currentIndexBuffer;
};

} // namespace mc
