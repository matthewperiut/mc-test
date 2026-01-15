#pragma once

#include "renderer/backend/VertexBuffer.hpp"

// Forward declarations for metal-cpp types
namespace MTL {
    class Device;
    class Buffer;
}

namespace mc {

class MTLVertexBuffer : public VertexBuffer {
public:
    explicit MTLVertexBuffer(MTL::Device* device);
    ~MTLVertexBuffer() override;

    void create() override;
    void destroy() override;
    void upload(const void* data, size_t sizeBytes, BufferUsage usage) override;
    void bind() override;
    void unbind() override;
    bool isValid() const override { return buffer != nullptr; }

    MTL::Buffer* getBuffer() const { return buffer; }

private:
    MTL::Device* device;
    MTL::Buffer* buffer;
    size_t bufferSize;
};

class MTLIndexBuffer : public IndexBuffer {
public:
    explicit MTLIndexBuffer(MTL::Device* device);
    ~MTLIndexBuffer() override;

    void create() override;
    void destroy() override;
    void upload(const uint32_t* data, size_t count, BufferUsage usage) override;
    void bind() override;
    void unbind() override;
    size_t getCount() const override { return indexCount; }
    bool isValid() const override { return buffer != nullptr; }

    MTL::Buffer* getBuffer() const { return buffer; }

private:
    MTL::Device* device;
    MTL::Buffer* buffer;
    size_t indexCount;
};

} // namespace mc
