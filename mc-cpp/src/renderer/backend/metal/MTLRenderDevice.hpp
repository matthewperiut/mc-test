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

// Result of a stream ring buffer allocation
struct StreamAllocation {
    MTL::Buffer* buffer = nullptr;
    size_t offset = 0;
    bool ownsBuffer = false;  // True if caller must release this buffer (fallback path)
};

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

    // Stream ring buffer allocator (for Tesselator streaming data)
    StreamAllocation allocateStream(size_t sizeBytes);

private:
    void createDepthStencilStates();
    void createDepthTexture();
    void createStreamBuffers();
    void destroyStreamBuffers();
    void resetEncoderState();
    void bindPipelineAndUniforms(BlendMode effectiveMode);
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

    // Triple buffering
    static constexpr int MAX_FRAMES_IN_FLIGHT = 3;
    void* frameSemaphore;  // dispatch_semaphore_t
    int currentFrameIndex;

    // Stream ring buffer (one per frame-in-flight, 8MB each)
    static constexpr size_t STREAM_BUFFER_SIZE = 8 * 1024 * 1024;
    MTL::Buffer* streamBuffers[MAX_FRAMES_IN_FLIGHT] = {};
    size_t streamOffset = 0;  // Current write offset in the active frame's buffer

    // Encoder state tracking (avoid redundant Metal API calls)
    MTL::RenderPipelineState* lastBoundPipelineState = nullptr;
    MTLShaderPipeline* lastUniformPipeline = nullptr;  // Last pipeline whose uniforms were uploaded
    MTL::Buffer* lastBoundVBO = nullptr;
    size_t lastBoundVBOOffset = SIZE_MAX;
    MTL::DepthStencilState* lastBoundDepthStencil = nullptr;
    int lastBoundCullMode = -1;  // -1 = unset
};

} // namespace mc
