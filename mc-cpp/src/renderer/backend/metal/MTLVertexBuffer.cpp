#include "MTLVertexBuffer.hpp"
#include "MTLRenderDevice.hpp"
#include <Metal/Metal.hpp>

namespace mc {

// MTLVertexBuffer implementation

MTLVertexBuffer::MTLVertexBuffer(MTL::Device* device)
    : device(device)
    , buffer(nullptr)
    , bufferSize(0)
{
}

MTLVertexBuffer::~MTLVertexBuffer() {
    destroy();
}

void MTLVertexBuffer::create() {
    // Buffer is created on first upload
}

void MTLVertexBuffer::destroy() {
    if (buffer) {
        buffer->release();
        buffer = nullptr;
    }
    bufferSize = 0;
}

void MTLVertexBuffer::upload(const void* data, size_t sizeBytes, BufferUsage usage) {
    // Determine resource options based on usage
    MTL::ResourceOptions options = MTL::ResourceStorageModeShared;

    if (usage == BufferUsage::Static) {
        options = MTL::ResourceStorageModeShared | MTL::ResourceCPUCacheModeWriteCombined;
    }

    // For streaming data, always create a new buffer to avoid GPU synchronization issues
    // The GPU may still be reading from the old buffer when we want to write new data
    bool needsNewBuffer = !buffer || bufferSize < sizeBytes;
    if (usage == BufferUsage::Stream) {
        needsNewBuffer = true;  // Always create new buffer for streaming
    }

    if (needsNewBuffer) {
        if (buffer) {
            buffer->release();
        }
        buffer = device->newBuffer(sizeBytes, options);
        bufferSize = sizeBytes;
    }

    // Copy data to buffer
    if (buffer && data) {
        memcpy(buffer->contents(), data, sizeBytes);
        buffer->didModifyRange(NS::Range::Make(0, sizeBytes));
    }
}

void MTLVertexBuffer::bind() {
    // Tell the render device this is the current vertex buffer
    auto& renderDevice = static_cast<MTLRenderDevice&>(RenderDevice::get());
    renderDevice.setCurrentVertexBuffer(this);
}

void MTLVertexBuffer::unbind() {
    auto& renderDevice = static_cast<MTLRenderDevice&>(RenderDevice::get());
    renderDevice.setCurrentVertexBuffer(nullptr);
}

// MTLIndexBuffer implementation

MTLIndexBuffer::MTLIndexBuffer(MTL::Device* device)
    : device(device)
    , buffer(nullptr)
    , indexCount(0)
{
}

MTLIndexBuffer::~MTLIndexBuffer() {
    destroy();
}

void MTLIndexBuffer::create() {
    // Buffer is created on first upload
}

void MTLIndexBuffer::destroy() {
    if (buffer) {
        buffer->release();
        buffer = nullptr;
    }
    indexCount = 0;
}

void MTLIndexBuffer::upload(const uint32_t* data, size_t count, BufferUsage usage) {
    indexCount = count;
    size_t sizeBytes = count * sizeof(uint32_t);

    MTL::ResourceOptions options = MTL::ResourceStorageModeShared;

    if (usage == BufferUsage::Static) {
        options = MTL::ResourceStorageModeShared | MTL::ResourceCPUCacheModeWriteCombined;
    }

    // For streaming data, always create a new buffer to avoid GPU synchronization issues
    bool needsNewBuffer = !buffer || buffer->length() < sizeBytes;
    if (usage == BufferUsage::Stream) {
        needsNewBuffer = true;
    }

    if (needsNewBuffer) {
        if (buffer) {
            buffer->release();
        }
        buffer = device->newBuffer(sizeBytes, options);
    }

    // Copy data to buffer
    if (buffer && data) {
        memcpy(buffer->contents(), data, sizeBytes);
        buffer->didModifyRange(NS::Range::Make(0, sizeBytes));
    }
}

void MTLIndexBuffer::bind() {
    // Tell the render device this is the current index buffer
    auto& renderDevice = static_cast<MTLRenderDevice&>(RenderDevice::get());
    renderDevice.setCurrentIndexBuffer(this);
}

void MTLIndexBuffer::unbind() {
    auto& renderDevice = static_cast<MTLRenderDevice&>(RenderDevice::get());
    renderDevice.setCurrentIndexBuffer(nullptr);
}

} // namespace mc
