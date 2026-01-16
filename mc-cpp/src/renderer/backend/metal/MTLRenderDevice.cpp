#include "MTLRenderDevice.hpp"
#include "MTLShaderPipeline.hpp"
#include "MTLVertexBuffer.hpp"
#include "MTLTexture.hpp"
#include "MetalBridge.h"
#include <Metal/Metal.hpp>
#include <QuartzCore/QuartzCore.hpp>
#include <GLFW/glfw3.h>

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
{
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

    std::cout << "Metal render device initialized" << std::endl;
    std::cout << "  Device: " << device->name()->utf8String() << std::endl;
    std::cout << "  Viewport: " << viewportWidth << "x" << viewportHeight << std::endl;

    return true;
}

void MTLRenderDevice::shutdown() {
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
}

void MTLRenderDevice::endFrame() {
    if (renderEncoder) {
        renderEncoder->endEncoding();
        renderEncoder->release();
        renderEncoder = nullptr;
    }

    if (renderPassDesc) {
        renderPassDesc->release();
        renderPassDesc = nullptr;
    }
}

void MTLRenderDevice::present() {
    if (commandBuffer && currentDrawable) {
        CA::MetalDrawable* drawable = reinterpret_cast<CA::MetalDrawable*>(currentDrawable);
        commandBuffer->presentDrawable(drawable);
        commandBuffer->commit();
        // Wait for GPU to finish before releasing to ensure proper cleanup
        commandBuffer->waitUntilCompleted();
        commandBuffer->release();
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
    renderEncoder->release();
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
}

void MTLRenderDevice::setDepthTest(bool enabled) {
    depthTestState = enabled;
    if (renderEncoder) {
        if (enabled) {
            renderEncoder->setDepthStencilState(depthWriteState ? depthTestEnabled : depthWriteDisabled);
        } else {
            renderEncoder->setDepthStencilState(depthTestDisabled);
        }
    }
}

void MTLRenderDevice::setDepthWrite(bool enabled) {
    depthWriteState = enabled;
    if (renderEncoder && depthTestState) {
        renderEncoder->setDepthStencilState(enabled ? depthTestEnabled : depthWriteDisabled);
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
        if (enabled) {
            switch (mode) {
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

void MTLRenderDevice::draw(PrimitiveType primitive, size_t vertexCount, size_t startVertex) {
    if (!renderEncoder || !currentPipeline || !currentVertexBuffer) {
        return;
    }

    // Bind pipeline state with current blend mode
    // Use shader's default blend mode unless a specific mode is set
    BlendMode effectiveMode = currentBlendMode;
    // For sky shader, always use its default additive blending
    if (currentPipeline->getDefaultBlendMode() == BlendMode::Additive) {
        effectiveMode = BlendMode::Additive;
    }
    renderEncoder->setRenderPipelineState(currentPipeline->getPipelineState(effectiveMode));

    // Bind vertex buffer at index 30 (to avoid conflict with uniform buffers)
    MTL::Buffer* vbuf = currentVertexBuffer->getBuffer();
    if (vbuf) {
        renderEncoder->setVertexBuffer(vbuf, 0, VERTEX_BUFFER_INDEX);
    }

    // Upload vertex uniforms (buffer 0)
    const auto& vertUniforms = currentPipeline->getVertexUniformData();
    if (!vertUniforms.empty()) {
        renderEncoder->setVertexBytes(vertUniforms.data(), vertUniforms.size(), 0);
    }

    // Upload fragment uniforms (buffer 2, as generated by SPIRV-Cross)
    const auto& fragUniforms = currentPipeline->getFragmentUniformData();
    if (!fragUniforms.empty()) {
        renderEncoder->setFragmentBytes(fragUniforms.data(), fragUniforms.size(), 2);
    }

    // Draw
    renderEncoder->drawPrimitives(static_cast<MTL::PrimitiveType>(toMTLPrimitive(primitive)), startVertex, vertexCount);
}

void MTLRenderDevice::drawIndexed(PrimitiveType primitive, size_t indexCount, size_t startIndex) {
    if (!renderEncoder || !currentPipeline || !currentVertexBuffer || !currentIndexBuffer) {
        return;
    }

    // Bind pipeline state with current blend mode
    BlendMode effectiveMode = currentBlendMode;
    // For sky shader, always use its default additive blending
    if (currentPipeline->getDefaultBlendMode() == BlendMode::Additive) {
        effectiveMode = BlendMode::Additive;
    }
    renderEncoder->setRenderPipelineState(currentPipeline->getPipelineState(effectiveMode));

    // Bind vertex buffer at index 30
    MTL::Buffer* vbuf = currentVertexBuffer->getBuffer();
    if (vbuf) {
        renderEncoder->setVertexBuffer(vbuf, 0, VERTEX_BUFFER_INDEX);
    }

    // Upload vertex uniforms (buffer 0)
    const auto& vertUniforms = currentPipeline->getVertexUniformData();
    if (!vertUniforms.empty()) {
        renderEncoder->setVertexBytes(vertUniforms.data(), vertUniforms.size(), 0);
    }

    // Upload fragment uniforms (buffer 2)
    const auto& fragUniforms = currentPipeline->getFragmentUniformData();
    if (!fragUniforms.empty()) {
        renderEncoder->setFragmentBytes(fragUniforms.data(), fragUniforms.size(), 2);
    }

    // Get index buffer
    MTL::Buffer* ibuf = currentIndexBuffer->getBuffer();
    if (!ibuf) {
        return;
    }

    // Draw indexed
    renderEncoder->drawIndexedPrimitives(
        static_cast<MTL::PrimitiveType>(toMTLPrimitive(primitive)),
        indexCount,
        MTL::IndexTypeUInt32,
        ibuf,
        startIndex * sizeof(uint32_t)
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
