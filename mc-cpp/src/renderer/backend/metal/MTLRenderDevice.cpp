#include "MTLRenderDevice.hpp"
#include "MTLShaderPipeline.hpp"
#include "MTLVertexBuffer.hpp"
#include "MTLTexture.hpp"
#include "MetalBridge.h"
#include <Metal/Metal.hpp>
#include <QuartzCore/QuartzCore.hpp>
#include <GLFW/glfw3.h>
#include <dispatch/dispatch.h>

// GLFW native access for macOS
#define GLFW_EXPOSE_NATIVE_COCOA
#include <GLFW/glfw3native.h>

#include <iostream>

namespace mc {

// Buffer index for vertex data (must match MTLShaderPipeline::createPipelineState)
static const int VERTEX_BUFFER_INDEX = 30;

MTLRenderDevice::MTLRenderDevice()
    : device(nullptr)
    , commandQueue(nullptr)
    , commandBuffer(nullptr)
    , renderEncoder(nullptr)
    , renderPassDesc(nullptr)
    , metalLayer(nullptr)
    , currentDrawable(nullptr)
    , depthTestEnabled(nullptr)
    , depthTestDisabled(nullptr)
    , depthWriteDisabled(nullptr)
    , depthTexture(nullptr)
    , windowHandle(nullptr)
    , viewportWidth(0)
    , viewportHeight(0)
    , scaleFactor(1.0f)
    , clearR(0.5f), clearG(0.8f), clearB(1.0f), clearA(1.0f)
    , depthTestState(true)
    , depthWriteState(true)
    , cullFaceState(true)
    , cullModeState(CullMode::Back)
    , currentBlendMode(BlendMode::AlphaBlend)
    , blendEnabled(true)
    , currentPipeline(nullptr)
    , currentVertexBuffer(nullptr)
    , currentIndexBuffer(nullptr)
    , frameSemaphore(nullptr)
    , currentFrameIndex(0)
{
    // Create semaphore for triple buffering (allow MAX_FRAMES_IN_FLIGHT frames to be in flight)
    frameSemaphore = dispatch_semaphore_create(MAX_FRAMES_IN_FLIGHT);
}

MTLRenderDevice::~MTLRenderDevice() {
    shutdown();
}

bool MTLRenderDevice::init(void* glfwWindow) {
    windowHandle = glfwWindow;

    // Create Metal device
    device = MTL::CreateSystemDefaultDevice();
    if (!device) {
        std::cerr << "Failed to create Metal device" << std::endl;
        return false;
    }

    // Create command queue
    commandQueue = device->newCommandQueue();
    if (!commandQueue) {
        std::cerr << "Failed to create command queue" << std::endl;
        return false;
    }

    // Get window size
    GLFWwindow* window = static_cast<GLFWwindow*>(glfwWindow);
    glfwGetFramebufferSize(window, &viewportWidth, &viewportHeight);

    // Get NSWindow from GLFW and set up Metal layer
    void* nsWindow = glfwGetCocoaWindow(window);
    metalLayer = getMetalLayerFromWindow(nsWindow);
    if (!metalLayer) {
        std::cerr << "Failed to create Metal layer" << std::endl;
        return false;
    }

    scaleFactor = getWindowScaleFactor(nsWindow);

    // Create depth stencil states
    createDepthStencilStates();

    // Create depth texture
    createDepthTexture();

    // Create stream ring buffers for per-frame streaming data
    createStreamBuffers();

    std::cout << "Metal render device initialized" << std::endl;
    std::cout << "  Device: " << device->name()->utf8String() << std::endl;
    std::cout << "  Viewport: " << viewportWidth << "x" << viewportHeight << std::endl;

    return true;
}

void MTLRenderDevice::shutdown() {
    // Wait for all in-flight frames to complete before shutting down
    if (frameSemaphore) {
        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
            dispatch_semaphore_wait(static_cast<dispatch_semaphore_t>(frameSemaphore), DISPATCH_TIME_FOREVER);
        }
        // Signal them back so the semaphore can be released cleanly
        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
            dispatch_semaphore_signal(static_cast<dispatch_semaphore_t>(frameSemaphore));
        }
        // Note: dispatch_semaphore_t is ARC-managed on modern macOS, no explicit release needed
        frameSemaphore = nullptr;
    }

    destroyStreamBuffers();

    if (depthTexture) {
        depthTexture->release();
        depthTexture = nullptr;
    }
    if (depthTestEnabled) {
        depthTestEnabled->release();
        depthTestEnabled = nullptr;
    }
    if (depthTestDisabled) {
        depthTestDisabled->release();
        depthTestDisabled = nullptr;
    }
    if (depthWriteDisabled) {
        depthWriteDisabled->release();
        depthWriteDisabled = nullptr;
    }
    if (commandQueue) {
        commandQueue->release();
        commandQueue = nullptr;
    }
    if (device) {
        device->release();
        device = nullptr;
    }
}

void MTLRenderDevice::createDepthStencilStates() {
    // Depth test enabled, depth write enabled (use LessEqual like OpenGL's GL_LEQUAL)
    {
        MTL::DepthStencilDescriptor* desc = MTL::DepthStencilDescriptor::alloc()->init();
        desc->setDepthCompareFunction(MTL::CompareFunctionLessEqual);
        desc->setDepthWriteEnabled(true);
        depthTestEnabled = device->newDepthStencilState(desc);
        desc->release();
    }

    // Depth test disabled
    {
        MTL::DepthStencilDescriptor* desc = MTL::DepthStencilDescriptor::alloc()->init();
        desc->setDepthCompareFunction(MTL::CompareFunctionAlways);
        desc->setDepthWriteEnabled(false);
        depthTestDisabled = device->newDepthStencilState(desc);
        desc->release();
    }

    // Depth test enabled, depth write disabled
    {
        MTL::DepthStencilDescriptor* desc = MTL::DepthStencilDescriptor::alloc()->init();
        desc->setDepthCompareFunction(MTL::CompareFunctionLessEqual);
        desc->setDepthWriteEnabled(false);
        depthWriteDisabled = device->newDepthStencilState(desc);
        desc->release();
    }
}

void MTLRenderDevice::createDepthTexture() {
    if (depthTexture) {
        depthTexture->release();
    }

    MTL::TextureDescriptor* desc = MTL::TextureDescriptor::alloc()->init();
    desc->setPixelFormat(MTL::PixelFormatDepth32Float);
    desc->setWidth(viewportWidth * scaleFactor);
    desc->setHeight(viewportHeight * scaleFactor);
    desc->setTextureType(MTL::TextureType2D);
    desc->setStorageMode(MTL::StorageModePrivate);
    desc->setUsage(MTL::TextureUsageRenderTarget);

    depthTexture = device->newTexture(desc);
    desc->release();
}

void MTLRenderDevice::handleResize(int width, int height) {
    viewportWidth = width;
    viewportHeight = height;

    // Update Metal layer size
    if (metalLayer && windowHandle) {
        void* nsWindow = glfwGetCocoaWindow(static_cast<GLFWwindow*>(windowHandle));
        scaleFactor = getWindowScaleFactor(nsWindow);
        updateMetalLayerSize(metalLayer, nsWindow, width, height);
    }

    // Recreate depth texture
    createDepthTexture();
}

void MTLRenderDevice::beginFrame() {
    // Wait for a free frame slot (triple buffering)
    dispatch_semaphore_wait(static_cast<dispatch_semaphore_t>(frameSemaphore), DISPATCH_TIME_FOREVER);
    currentFrameIndex = (currentFrameIndex + 1) % MAX_FRAMES_IN_FLIGHT;

    // Get next drawable from Metal layer
    CA::MetalLayer* layer = reinterpret_cast<CA::MetalLayer*>(metalLayer);
    CA::MetalDrawable* drawable = layer->nextDrawable();
    currentDrawable = drawable;

    if (!drawable) {
        std::cerr << "Failed to get next drawable" << std::endl;
        return;
    }

    // Create command buffer
    commandBuffer = commandQueue->commandBuffer();

    // Create render pass descriptor
    renderPassDesc = MTL::RenderPassDescriptor::alloc()->init();

    // Color attachment
    MTL::RenderPassColorAttachmentDescriptor* colorAttach =
        renderPassDesc->colorAttachments()->object(0);
    colorAttach->setTexture(drawable->texture());
    colorAttach->setLoadAction(MTL::LoadActionClear);
    colorAttach->setStoreAction(MTL::StoreActionStore);
    colorAttach->setClearColor(MTL::ClearColor(clearR, clearG, clearB, clearA));

    // Depth attachment
    MTL::RenderPassDepthAttachmentDescriptor* depthAttach =
        renderPassDesc->depthAttachment();
    depthAttach->setTexture(depthTexture);
    depthAttach->setLoadAction(MTL::LoadActionClear);
    depthAttach->setStoreAction(MTL::StoreActionDontCare);
    depthAttach->setClearDepth(1.0);

    // Create render encoder
    renderEncoder = commandBuffer->renderCommandEncoder(renderPassDesc);

    // Set front face winding (OpenGL and Metal both default to counter-clockwise)
    renderEncoder->setFrontFacingWinding(MTL::WindingCounterClockwise);

    // Set initial depth stencil state
    if (depthTestState) {
        renderEncoder->setDepthStencilState(depthWriteState ? depthTestEnabled : depthWriteDisabled);
    } else {
        renderEncoder->setDepthStencilState(depthTestDisabled);
    }

    // Set cull mode
    if (cullFaceState) {
        switch (cullModeState) {
            case CullMode::Back:
                renderEncoder->setCullMode(MTL::CullModeBack);
                break;
            case CullMode::Front:
                renderEncoder->setCullMode(MTL::CullModeFront);
                break;
            case CullMode::None:
                renderEncoder->setCullMode(MTL::CullModeNone);
                break;
        }
    } else {
        renderEncoder->setCullMode(MTL::CullModeNone);
    }

    // Set initial viewport
    setViewport(0, 0, viewportWidth, viewportHeight);

    // Reset stream buffer offset for this frame
    streamOffset = 0;

    // Reset encoder state tracking (new encoder = no prior state)
    resetEncoderState();
}

void MTLRenderDevice::endFrame() {
    if (renderEncoder) {
        renderEncoder->endEncoding();
        // Note: renderEncoder is autoreleased (returned from renderCommandEncoder()),
        // so we don't call release() - the autorelease pool handles it
        renderEncoder = nullptr;
    }

    if (renderPassDesc) {
        renderPassDesc->release();  // This one we allocated with alloc()->init(), so we own it
        renderPassDesc = nullptr;
    }
}

void MTLRenderDevice::present() {
    if (commandBuffer && currentDrawable) {
        CA::MetalDrawable* drawable = reinterpret_cast<CA::MetalDrawable*>(currentDrawable);
        commandBuffer->presentDrawable(drawable);

        // Add completion handler to signal semaphore when GPU finishes
        // This enables triple buffering - CPU can work on next frames while GPU processes
        dispatch_semaphore_t sem = static_cast<dispatch_semaphore_t>(frameSemaphore);
        commandBuffer->addCompletedHandler([sem](MTL::CommandBuffer*) {
            dispatch_semaphore_signal(sem);
        });

        commandBuffer->commit();
        // Note: commandBuffer is autoreleased (returned from commandQueue->commandBuffer()),
        // so we don't call release() - the autorelease pool handles it
        commandBuffer = nullptr;
    }
    currentDrawable = nullptr;
}

void MTLRenderDevice::setViewport(int x, int y, int width, int height) {
    if (renderEncoder) {
        MTL::Viewport viewport;
        // Don't multiply by scaleFactor - callers pass framebuffer dimensions
        // which are already scaled on Retina displays
        viewport.originX = x;
        viewport.originY = y;
        viewport.width = width;
        viewport.height = height;
        viewport.znear = 0.0;
        viewport.zfar = 1.0;
        renderEncoder->setViewport(viewport);
    }
}

void MTLRenderDevice::setClearColor(float r, float g, float b, float a) {
    clearR = r;
    clearG = g;
    clearB = b;
    clearA = a;
}

void MTLRenderDevice::clear(bool color, bool depth) {
    // In Metal, clearing is done via load actions in the render pass descriptor
    // This is set up in beginFrame()
    // For mid-frame depth-only clear, use clearDepthMidFrame()
    if (!color && depth) {
        clearDepthMidFrame();
    }
}

void MTLRenderDevice::clearDepthMidFrame() {
    if (!renderEncoder || !currentDrawable) return;

    // End current render encoder
    renderEncoder->endEncoding();
    // Note: renderEncoder is autoreleased, so we don't call release()
    renderEncoder = nullptr;

    // Release old render pass descriptor
    if (renderPassDesc) {
        renderPassDesc->release();
        renderPassDesc = nullptr;
    }

    // Create new render pass descriptor that preserves color but clears depth
    renderPassDesc = MTL::RenderPassDescriptor::alloc()->init();

    // Color attachment - LOAD existing content (don't clear)
    CA::MetalDrawable* drawable = reinterpret_cast<CA::MetalDrawable*>(currentDrawable);
    MTL::RenderPassColorAttachmentDescriptor* colorAttach =
        renderPassDesc->colorAttachments()->object(0);
    colorAttach->setTexture(drawable->texture());
    colorAttach->setLoadAction(MTL::LoadActionLoad);  // Preserve color
    colorAttach->setStoreAction(MTL::StoreActionStore);

    // Depth attachment - CLEAR
    MTL::RenderPassDepthAttachmentDescriptor* depthAttach =
        renderPassDesc->depthAttachment();
    depthAttach->setTexture(depthTexture);
    depthAttach->setLoadAction(MTL::LoadActionClear);  // Clear depth
    depthAttach->setStoreAction(MTL::StoreActionDontCare);
    depthAttach->setClearDepth(1.0);

    // Create new render encoder
    renderEncoder = commandBuffer->renderCommandEncoder(renderPassDesc);

    // Restore render state
    renderEncoder->setFrontFacingWinding(MTL::WindingCounterClockwise);

    // Restore depth stencil state
    if (depthTestState) {
        renderEncoder->setDepthStencilState(depthWriteState ? depthTestEnabled : depthWriteDisabled);
    } else {
        renderEncoder->setDepthStencilState(depthTestDisabled);
    }

    // Restore cull mode
    if (cullFaceState) {
        switch (cullModeState) {
            case CullMode::Back:
                renderEncoder->setCullMode(MTL::CullModeBack);
                break;
            case CullMode::Front:
                renderEncoder->setCullMode(MTL::CullModeFront);
                break;
            case CullMode::None:
                renderEncoder->setCullMode(MTL::CullModeNone);
                break;
        }
    } else {
        renderEncoder->setCullMode(MTL::CullModeNone);
    }

    // Restore viewport
    setViewport(0, 0, viewportWidth, viewportHeight);

    // Reset encoder state tracking (new encoder = no prior state)
    resetEncoderState();
}

void MTLRenderDevice::setDepthTest(bool enabled) {
    depthTestState = enabled;
    if (renderEncoder) {
        MTL::DepthStencilState* desired;
        if (enabled) {
            desired = depthWriteState ? depthTestEnabled : depthWriteDisabled;
        } else {
            desired = depthTestDisabled;
        }
        if (desired != lastBoundDepthStencil) {
            renderEncoder->setDepthStencilState(desired);
            lastBoundDepthStencil = desired;
        }
    }
}

void MTLRenderDevice::setDepthWrite(bool enabled) {
    depthWriteState = enabled;
    if (renderEncoder && depthTestState) {
        MTL::DepthStencilState* desired = enabled ? depthTestEnabled : depthWriteDisabled;
        if (desired != lastBoundDepthStencil) {
            renderEncoder->setDepthStencilState(desired);
            lastBoundDepthStencil = desired;
        }
    }
}

void MTLRenderDevice::setDepthFunc(CompareFunc func) {
    // Would need to create new depth stencil states for different compare functions
    // For now, we only support Less (default) and Always (no depth test)
}

void MTLRenderDevice::setCullFace(bool enabled, CullMode mode) {
    cullFaceState = enabled;
    cullModeState = mode;
    if (renderEncoder) {
        int desiredCull;
        if (enabled) {
            switch (mode) {
                case CullMode::Back:  desiredCull = static_cast<int>(MTL::CullModeBack); break;
                case CullMode::Front: desiredCull = static_cast<int>(MTL::CullModeFront); break;
                case CullMode::None:  desiredCull = static_cast<int>(MTL::CullModeNone); break;
            }
        } else {
            desiredCull = static_cast<int>(MTL::CullModeNone);
        }
        if (desiredCull != lastBoundCullMode) {
            renderEncoder->setCullMode(static_cast<MTL::CullMode>(desiredCull));
            lastBoundCullMode = desiredCull;
        }
    }
}

void MTLRenderDevice::setFrontFace(FrontFace face) {
    if (renderEncoder) {
        renderEncoder->setFrontFacingWinding(
            face == FrontFace::CounterClockwise ? MTL::WindingCounterClockwise : MTL::WindingClockwise);
    }
}

void MTLRenderDevice::setBlend(bool enabled, BlendFactor src, BlendFactor dst) {
    blendEnabled = enabled;

    if (!enabled) {
        currentBlendMode = BlendMode::Disabled;
        return;
    }

    // Map common blend factor combinations to BlendMode
    if (src == BlendFactor::SrcAlpha && dst == BlendFactor::OneMinusSrcAlpha) {
        currentBlendMode = BlendMode::AlphaBlend;
    } else if (src == BlendFactor::SrcAlpha && dst == BlendFactor::One) {
        currentBlendMode = BlendMode::Additive;
    } else if (src == BlendFactor::DstColor && dst == BlendFactor::SrcColor) {
        currentBlendMode = BlendMode::Multiply;
    } else if (src == BlendFactor::OneMinusDstColor && dst == BlendFactor::OneMinusSrcColor) {
        currentBlendMode = BlendMode::Invert;
    } else {
        // Default to alpha blend for unsupported combinations
        currentBlendMode = BlendMode::AlphaBlend;
    }
}

void MTLRenderDevice::setPolygonOffset(bool enabled, float factor, float units) {
    if (renderEncoder) {
        if (enabled) {
            renderEncoder->setDepthBias(units, factor, factor);
        } else {
            renderEncoder->setDepthBias(0, 0, 0);
        }
    }
}

void MTLRenderDevice::setLineWidth(float width) {
    // Metal doesn't support line width > 1.0
    // Lines are always 1 pixel wide
}

void MTLRenderDevice::setColorMask(bool r, bool g, bool b, bool a) {
    // In Metal, color mask is set per-pipeline in the color attachment descriptor
    // For runtime changes, we'd need to recreate the pipeline or use a different approach
    // For now, this is a no-op as most Metal pipelines write all channels
    // The depth-only pass in clouds uses separate blend mode handling
}

std::unique_ptr<ShaderPipeline> MTLRenderDevice::createShaderPipeline() {
    return std::make_unique<MTLShaderPipeline>(device);
}

std::unique_ptr<VertexBuffer> MTLRenderDevice::createVertexBuffer() {
    return std::make_unique<MTLVertexBuffer>(device);
}

std::unique_ptr<IndexBuffer> MTLRenderDevice::createIndexBuffer() {
    return std::make_unique<MTLIndexBuffer>(device);
}

std::unique_ptr<Texture> MTLRenderDevice::createTexture() {
    return std::make_unique<MTLTexture>(device);
}

void MTLRenderDevice::bindPipelineAndUniforms(BlendMode effectiveMode) {
    // Bind pipeline state only if changed
    MTL::RenderPipelineState* desiredPipeline = currentPipeline->getPipelineState(effectiveMode);
    if (desiredPipeline != lastBoundPipelineState) {
        renderEncoder->setRenderPipelineState(desiredPipeline);
        lastBoundPipelineState = desiredPipeline;
    }

    // Upload uniforms only if dirty or pipeline changed (encoder state may be stale)
    bool pipelineChanged = (currentPipeline != lastUniformPipeline);

    const auto& vertUniforms = currentPipeline->getVertexUniformData();
    if (!vertUniforms.empty() && (pipelineChanged || currentPipeline->isVertexUniformsDirty())) {
        renderEncoder->setVertexBytes(vertUniforms.data(), vertUniforms.size(), 0);
        currentPipeline->clearVertexUniformsDirty();
    }

    const auto& fragUniforms = currentPipeline->getFragmentUniformData();
    if (!fragUniforms.empty() && (pipelineChanged || currentPipeline->isFragmentUniformsDirty())) {
        renderEncoder->setFragmentBytes(fragUniforms.data(), fragUniforms.size(), 2);
        currentPipeline->clearFragmentUniformsDirty();
    }

    lastUniformPipeline = currentPipeline;
}

void MTLRenderDevice::draw(PrimitiveType primitive, size_t vertexCount, size_t startVertex) {
    if (!renderEncoder || !currentPipeline || !currentVertexBuffer) {
        return;
    }

    // Determine effective blend mode
    BlendMode effectiveMode = currentBlendMode;
    if (currentPipeline->getDefaultBlendMode() == BlendMode::Additive) {
        effectiveMode = BlendMode::Additive;
    }

    // Bind pipeline and upload uniforms (with deduplication)
    bindPipelineAndUniforms(effectiveMode);

    // Bind vertex buffer only if changed
    MTL::Buffer* vbuf = currentVertexBuffer->getBuffer();
    size_t vbufOffset = currentVertexBuffer->getOffset();
    if (vbuf && (vbuf != lastBoundVBO || vbufOffset != lastBoundVBOOffset)) {
        renderEncoder->setVertexBuffer(vbuf, vbufOffset, VERTEX_BUFFER_INDEX);
        lastBoundVBO = vbuf;
        lastBoundVBOOffset = vbufOffset;
    }

    // Draw
    renderEncoder->drawPrimitives(static_cast<MTL::PrimitiveType>(toMTLPrimitive(primitive)), startVertex, vertexCount);
}

void MTLRenderDevice::drawIndexed(PrimitiveType primitive, size_t indexCount, size_t startIndex) {
    if (!renderEncoder || !currentPipeline || !currentVertexBuffer || !currentIndexBuffer) {
        return;
    }

    // Determine effective blend mode
    BlendMode effectiveMode = currentBlendMode;
    if (currentPipeline->getDefaultBlendMode() == BlendMode::Additive) {
        effectiveMode = BlendMode::Additive;
    }

    // Bind pipeline and upload uniforms (with deduplication)
    bindPipelineAndUniforms(effectiveMode);

    // Bind vertex buffer only if changed
    MTL::Buffer* vbuf = currentVertexBuffer->getBuffer();
    size_t vbufOffset = currentVertexBuffer->getOffset();
    if (vbuf && (vbuf != lastBoundVBO || vbufOffset != lastBoundVBOOffset)) {
        renderEncoder->setVertexBuffer(vbuf, vbufOffset, VERTEX_BUFFER_INDEX);
        lastBoundVBO = vbuf;
        lastBoundVBOOffset = vbufOffset;
    }

    // Get index buffer (with ring buffer offset)
    MTL::Buffer* ibuf = currentIndexBuffer->getBuffer();
    if (!ibuf) {
        return;
    }

    // Draw indexed (offset includes ring buffer base offset)
    size_t indexByteOffset = currentIndexBuffer->getOffset() + startIndex * sizeof(uint32_t);
    renderEncoder->drawIndexedPrimitives(
        static_cast<MTL::PrimitiveType>(toMTLPrimitive(primitive)),
        indexCount,
        MTL::IndexTypeUInt32,
        ibuf,
        indexByteOffset
    );
}

void MTLRenderDevice::setupVertexAttributes() {
    // Vertex attributes are set up in the pipeline state
    // This is configured in MTLShaderPipeline::createPipelineState()
}

void MTLRenderDevice::setVsync(bool enabled) {
    if (metalLayer) {
        setMetalLayerVsync(metalLayer, enabled);
    }
}

void MTLRenderDevice::createStreamBuffers() {
    for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
        streamBuffers[i] = device->newBuffer(STREAM_BUFFER_SIZE, MTL::ResourceStorageModeShared);
    }
}

void MTLRenderDevice::destroyStreamBuffers() {
    for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
        if (streamBuffers[i]) {
            streamBuffers[i]->release();
            streamBuffers[i] = nullptr;
        }
    }
}

StreamAllocation MTLRenderDevice::allocateStream(size_t sizeBytes) {
    // Align to 256 bytes for optimal GPU access
    size_t alignedSize = (sizeBytes + 255) & ~static_cast<size_t>(255);

    MTL::Buffer* ringBuffer = streamBuffers[currentFrameIndex];
    if (!ringBuffer || streamOffset + alignedSize > STREAM_BUFFER_SIZE) {
        // Ring buffer exhausted — fall back to a one-off allocation
        // This should be rare; if hit frequently, increase STREAM_BUFFER_SIZE
        MTL::Buffer* fallback = device->newBuffer(sizeBytes, MTL::ResourceStorageModeShared);
        return {fallback, 0, true};  // Caller owns this buffer
    }

    StreamAllocation alloc;
    alloc.buffer = ringBuffer;
    alloc.offset = streamOffset;
    alloc.ownsBuffer = false;  // Ring buffer is owned by the device
    streamOffset += alignedSize;
    return alloc;
}

void MTLRenderDevice::resetEncoderState() {
    lastBoundPipelineState = nullptr;
    lastUniformPipeline = nullptr;
    lastBoundVBO = nullptr;
    lastBoundVBOOffset = SIZE_MAX;
    lastBoundDepthStencil = nullptr;
    lastBoundCullMode = -1;
}

int MTLRenderDevice::toMTLPrimitive(PrimitiveType prim) {
    switch (prim) {
        case PrimitiveType::Triangles: return static_cast<int>(MTL::PrimitiveTypeTriangle);
        case PrimitiveType::Lines: return static_cast<int>(MTL::PrimitiveTypeLine);
        case PrimitiveType::LineStrip: return static_cast<int>(MTL::PrimitiveTypeLineStrip);
        case PrimitiveType::Points: return static_cast<int>(MTL::PrimitiveTypePoint);
    }
    return static_cast<int>(MTL::PrimitiveTypeTriangle);
}

// Factory function for Metal backend
std::unique_ptr<RenderDevice> createRenderDevice() {
    return std::make_unique<MTLRenderDevice>();
}

} // namespace mc
