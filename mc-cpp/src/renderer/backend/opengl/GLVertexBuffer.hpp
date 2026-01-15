#pragma once

#include "renderer/backend/VertexBuffer.hpp"
#include <GL/glew.h>

namespace mc {

class GLVertexBuffer : public VertexBuffer {
public:
    GLVertexBuffer();
    ~GLVertexBuffer() override;

    void create() override;
    void destroy() override;
    void upload(const void* data, size_t sizeBytes, BufferUsage usage) override;
    void bind() override;
    void unbind() override;
    bool isValid() const override { return vbo != 0; }

    GLuint getVBO() const { return vbo; }
    GLuint getVAO() const { return vao; }

private:
    GLuint vao;
    GLuint vbo;
};

class GLIndexBuffer : public IndexBuffer {
public:
    GLIndexBuffer();
    ~GLIndexBuffer() override;

    void create() override;
    void destroy() override;
    void upload(const uint32_t* data, size_t count, BufferUsage usage) override;
    void bind() override;
    void unbind() override;
    size_t getCount() const override { return indexCount; }
    bool isValid() const override { return ebo != 0; }

    GLuint getEBO() const { return ebo; }

private:
    GLuint ebo;
    size_t indexCount;
};

} // namespace mc
