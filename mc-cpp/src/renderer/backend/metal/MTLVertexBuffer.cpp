#include "MTLVertexBuffer.hpp"
#include "MTLRenderDevice.hpp"
#include <Metal/Metal.hpp>

namespace mc {

// MTLVertexBuffer implementation

MTLVertexBuffer::MTLVertexBuffer(MTL::Device* device)
    : device(device)
    , buffer(nullptr)
    , bufferSize(0)
    , bufferOffset(0)
    , ownsBuffer(true)
{
}

MTLVertexBuffer::~MTLVertexBuffer() {
    destroy();
}

void MTLVertexBuffer::create() {
    // Buffer is created on first upload
}

void MTLVertexBuffer::destroy() {
    if (buffer && ownsBuffer) {
        buffer->release();
    }
    buffer = nullptr;
    bufferSize = 0;
    bufferOffset = 0;
    ownsBuffer = true;
}

void MTLVertexBuffer::upload(const void* data, size_t sizeBytes, BufferUsage usage) {
    if (usage == BufferUsage::Stream) {
        // Use the ring buffer from the render device (no heap allocation)
        auto& renderDevice = static_cast<MTLRenderDevice&>(RenderDevice::get());
        StreamAllocation alloc = renderDevice.allocateStream(sizeBytes);

        if (alloc.buffer) {
            // Release any previously owned buffer
            if (buffer && ownsBuffer) {
                buffer->release();
            }

            buffer = alloc.buffer;
            bufferOffset = alloc.offset;
            bufferSize = sizeBytes;
            ownsBuffer = alloc.ownsBuffer;  // Usually false (ring buffer), true on fallback

            // Copy data into the allocated region
            if (data) {
                memcpy(static_cast<uint8_t*>(buffer->contents()) + bufferOffset, data, sizeBytes);
            }
        }
        return;
    }

    // Static/Dynamic: use dedicated buffer (existing behavior)
    bufferOffset = 0;

    MTL::ResourceOptions options = MTL::ResourceStorageModeShared;
    if (usage == BufferUsage::Static) {
        options = MTL::ResourceStorageModeShared | MTL::ResourceCPUCacheModeWriteCombined;
    }

    bool needsNewBuffer = !buffer || !ownsBuffer || bufferSize < sizeBytes;

    if (needsNewBuffer) {
        if (buffer && ownsBuffer) {
            buffer->release();
        }
        buffer = device->newBuffer(sizeBytes, options);
        bufferSize = sizeBytes;
        ownsBuffer = true;
    }

    // Copy data to buffer
    if (buffer && data) {
        memcpy(buffer->contents(), data, sizeBytes);
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
    , bufferOffset(0)
    , ownsBuffer(true)
{
}

MTLIndexBuffer::~MTLIndexBuffer() {
    destroy();
}

void MTLIndexBuffer::create() {
    // Buffer is created on first upload
}

void MTLIndexBuffer::destroy() {
    if (buffer && ownsBuffer) {
        buffer->release();
    }
    buffer = nullptr;
    indexCount = 0;
    bufferOffset = 0;
    ownsBuffer = true;
}

void MTLIndexBuffer::upload(const uint32_t* data, size_t count, BufferUsage usage) {
    indexCount = count;
    size_t sizeBytes = count * sizeof(uint32_t);

    if (usage == BufferUsage::Stream) {
        // Use the ring buffer from the render device (no heap allocation)
        auto& renderDevice = static_cast<MTLRenderDevice&>(RenderDevice::get());
        StreamAllocation alloc = renderDevice.allocateStream(sizeBytes);

        if (alloc.buffer) {
            // Release any previously owned buffer
            if (buffer && ownsBuffer) {
                buffer->release();
            }

            buffer = alloc.buffer;
            bufferOffset = alloc.offset;
            ownsBuffer = alloc.ownsBuffer;  // Usually false (ring buffer), true on fallback

            // Copy data into the allocated region
            if (data) {
                memcpy(static_cast<uint8_t*>(buffer->contents()) + bufferOffset, data, sizeBytes);
            }
        }
        return;
    }

    // Static/Dynamic: use dedicated buffer (existing behavior)
    bufferOffset = 0;

    MTL::ResourceOptions options = MTL::ResourceStorageModeShared;
    if (usage == BufferUsage::Static) {
        options = MTL::ResourceStorageModeShared | MTL::ResourceCPUCacheModeWriteCombined;
    }

    bool needsNewBuffer = !buffer || !ownsBuffer || (buffer->length() < sizeBytes);

    if (needsNewBuffer) {
        if (buffer && ownsBuffer) {
            buffer->release();
        }
        buffer = device->newBuffer(sizeBytes, options);
        ownsBuffer = true;
    }

    // Copy data to buffer
    if (buffer && data) {
        memcpy(buffer->contents(), data, sizeBytes);
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
