#pragma once

#include "RenderTypes.hpp"
#include <cstddef>
#include <memory>

namespace mc {

class VertexBuffer {
public:
    virtual ~VertexBuffer() = default;

    virtual void create() = 0;
    virtual void destroy() = 0;

    // Upload vertex data
    virtual void upload(const void* data, size_t sizeBytes, BufferUsage usage) = 0;

    // Binding for rendering
    virtual void bind() = 0;
    virtual void unbind() = 0;

    virtual bool isValid() const = 0;
};

class IndexBuffer {
public:
    virtual ~IndexBuffer() = default;

    virtual void create() = 0;
    virtual void destroy() = 0;

    // Upload index data (32-bit indices)
    virtual void upload(const uint32_t* data, size_t count, BufferUsage usage) = 0;

    // Binding for rendering
    virtual void bind() = 0;
    virtual void unbind() = 0;

    virtual size_t getCount() const = 0;
    virtual bool isValid() const = 0;
};

} // namespace mc
