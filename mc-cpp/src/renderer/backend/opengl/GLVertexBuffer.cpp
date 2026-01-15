#include "GLVertexBuffer.hpp"
#include "renderer/backend/RenderTypes.hpp"

namespace mc {

static GLenum toGLUsage(BufferUsage usage) {
    switch (usage) {
        case BufferUsage::Static: return GL_STATIC_DRAW;
        case BufferUsage::Dynamic: return GL_DYNAMIC_DRAW;
        case BufferUsage::Stream: return GL_STREAM_DRAW;
    }
    return GL_STATIC_DRAW;
}

// GLVertexBuffer implementation

GLVertexBuffer::GLVertexBuffer() : vao(0), vbo(0) {}

GLVertexBuffer::~GLVertexBuffer() {
    destroy();
}

void GLVertexBuffer::create() {
    if (vao == 0) {
        glGenVertexArrays(1, &vao);
    }
    if (vbo == 0) {
        glGenBuffers(1, &vbo);
    }
}

void GLVertexBuffer::destroy() {
    if (vbo != 0) {
        glDeleteBuffers(1, &vbo);
        vbo = 0;
    }
    if (vao != 0) {
        glDeleteVertexArrays(1, &vao);
        vao = 0;
    }
}

void GLVertexBuffer::upload(const void* data, size_t sizeBytes, BufferUsage usage) {
    bind();
    glBufferData(GL_ARRAY_BUFFER, sizeBytes, data, toGLUsage(usage));
}

void GLVertexBuffer::bind() {
    glBindVertexArray(vao);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
}

void GLVertexBuffer::unbind() {
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);
}

// GLIndexBuffer implementation

GLIndexBuffer::GLIndexBuffer() : ebo(0), indexCount(0) {}

GLIndexBuffer::~GLIndexBuffer() {
    destroy();
}

void GLIndexBuffer::create() {
    if (ebo == 0) {
        glGenBuffers(1, &ebo);
    }
}

void GLIndexBuffer::destroy() {
    if (ebo != 0) {
        glDeleteBuffers(1, &ebo);
        ebo = 0;
    }
    indexCount = 0;
}

void GLIndexBuffer::upload(const uint32_t* data, size_t count, BufferUsage usage) {
    indexCount = count;
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, count * sizeof(uint32_t), data, toGLUsage(usage));
}

void GLIndexBuffer::bind() {
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
}

void GLIndexBuffer::unbind() {
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
}

} // namespace mc
