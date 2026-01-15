#include "GLRenderDevice.hpp"
#include "GLShaderPipeline.hpp"
#include "GLVertexBuffer.hpp"
#include "GLTexture.hpp"
#include "renderer/backend/RenderTypes.hpp"
#include <iostream>

namespace mc {

GLRenderDevice::GLRenderDevice() : initialized(false) {}

GLRenderDevice::~GLRenderDevice() {
    shutdown();
}

bool GLRenderDevice::init(void* windowHandle) {
    // GLEW should already be initialized by the time we get here
    // (initialized in Minecraft::init after GLFW context creation)
    if (glewInit() != GLEW_OK) {
        std::cerr << "Failed to initialize GLEW" << std::endl;
        return false;
    }

    initialized = true;
    return true;
}

void GLRenderDevice::shutdown() {
    initialized = false;
}

void GLRenderDevice::beginFrame() {
    // OpenGL doesn't need explicit frame begin
}

void GLRenderDevice::endFrame() {
    // OpenGL doesn't need explicit frame end
}

void GLRenderDevice::present() {
    // Swap buffers is handled by GLFW in the main loop
}

void GLRenderDevice::setViewport(int x, int y, int width, int height) {
    glViewport(x, y, width, height);
}

void GLRenderDevice::setClearColor(float r, float g, float b, float a) {
    glClearColor(r, g, b, a);
}

void GLRenderDevice::clear(bool color, bool depth) {
    GLbitfield mask = 0;
    if (color) mask |= GL_COLOR_BUFFER_BIT;
    if (depth) mask |= GL_DEPTH_BUFFER_BIT;
    glClear(mask);
}

void GLRenderDevice::setDepthTest(bool enabled) {
    if (enabled) {
        glEnable(GL_DEPTH_TEST);
    } else {
        glDisable(GL_DEPTH_TEST);
    }
}

void GLRenderDevice::setDepthWrite(bool enabled) {
    glDepthMask(enabled ? GL_TRUE : GL_FALSE);
}

void GLRenderDevice::setDepthFunc(CompareFunc func) {
    glDepthFunc(toGLCompareFunc(func));
}

void GLRenderDevice::setCullFace(bool enabled, CullMode mode) {
    if (enabled) {
        glEnable(GL_CULL_FACE);
        switch (mode) {
            case CullMode::Back: glCullFace(GL_BACK); break;
            case CullMode::Front: glCullFace(GL_FRONT); break;
            case CullMode::None: glDisable(GL_CULL_FACE); break;
        }
    } else {
        glDisable(GL_CULL_FACE);
    }
}

void GLRenderDevice::setBlend(bool enabled, BlendFactor src, BlendFactor dst) {
    if (enabled) {
        glEnable(GL_BLEND);
        glBlendFunc(toGLBlendFactor(src), toGLBlendFactor(dst));
    } else {
        glDisable(GL_BLEND);
    }
}

void GLRenderDevice::setPolygonOffset(bool enabled, float factor, float units) {
    if (enabled) {
        glEnable(GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(factor, units);
    } else {
        glDisable(GL_POLYGON_OFFSET_FILL);
    }
}

void GLRenderDevice::setLineWidth(float width) {
    glLineWidth(width);
}

std::unique_ptr<ShaderPipeline> GLRenderDevice::createShaderPipeline() {
    return std::make_unique<GLShaderPipeline>();
}

std::unique_ptr<VertexBuffer> GLRenderDevice::createVertexBuffer() {
    auto buffer = std::make_unique<GLVertexBuffer>();
    buffer->create();
    return buffer;
}

std::unique_ptr<IndexBuffer> GLRenderDevice::createIndexBuffer() {
    auto buffer = std::make_unique<GLIndexBuffer>();
    buffer->create();
    return buffer;
}

std::unique_ptr<Texture> GLRenderDevice::createTexture() {
    auto texture = std::make_unique<GLTexture>();
    texture->create();
    return texture;
}

void GLRenderDevice::draw(PrimitiveType primitive, size_t vertexCount, size_t startVertex) {
    glDrawArrays(toGLPrimitive(primitive), static_cast<GLint>(startVertex), static_cast<GLsizei>(vertexCount));
}

void GLRenderDevice::drawIndexed(PrimitiveType primitive, size_t indexCount, size_t startIndex) {
    glDrawElements(toGLPrimitive(primitive), static_cast<GLsizei>(indexCount), GL_UNSIGNED_INT,
                   reinterpret_cast<void*>(startIndex * sizeof(uint32_t)));
}

void GLRenderDevice::setupVertexAttributes() {
    // Position: 3 floats at offset 0
    glEnableVertexAttribArray(VertexFormat::ATTRIB_POSITION);
    glVertexAttribPointer(VertexFormat::ATTRIB_POSITION, 3, GL_FLOAT, GL_FALSE,
                         VertexFormat::STRIDE, reinterpret_cast<void*>(0));

    // TexCoord: 2 floats at offset 12
    glEnableVertexAttribArray(VertexFormat::ATTRIB_TEXCOORD);
    glVertexAttribPointer(VertexFormat::ATTRIB_TEXCOORD, 2, GL_FLOAT, GL_FALSE,
                         VertexFormat::STRIDE, reinterpret_cast<void*>(12));

    // Color: 4 bytes (normalized) at offset 20
    glEnableVertexAttribArray(VertexFormat::ATTRIB_COLOR);
    glVertexAttribPointer(VertexFormat::ATTRIB_COLOR, 4, GL_UNSIGNED_BYTE, GL_TRUE,
                         VertexFormat::STRIDE, reinterpret_cast<void*>(20));

    // Normal: 3 signed bytes (normalized) at offset 24
    glEnableVertexAttribArray(VertexFormat::ATTRIB_NORMAL);
    glVertexAttribPointer(VertexFormat::ATTRIB_NORMAL, 3, GL_BYTE, GL_TRUE,
                         VertexFormat::STRIDE, reinterpret_cast<void*>(24));

    // Light: 2 bytes at offset 28
    glEnableVertexAttribArray(VertexFormat::ATTRIB_LIGHT);
    glVertexAttribPointer(VertexFormat::ATTRIB_LIGHT, 2, GL_UNSIGNED_BYTE, GL_FALSE,
                         VertexFormat::STRIDE, reinterpret_cast<void*>(28));
}

GLenum GLRenderDevice::toGLPrimitive(PrimitiveType prim) {
    switch (prim) {
        case PrimitiveType::Triangles: return GL_TRIANGLES;
        case PrimitiveType::Lines: return GL_LINES;
        case PrimitiveType::LineStrip: return GL_LINE_STRIP;
        case PrimitiveType::Points: return GL_POINTS;
    }
    return GL_TRIANGLES;
}

GLenum GLRenderDevice::toGLCompareFunc(CompareFunc func) {
    switch (func) {
        case CompareFunc::Never: return GL_NEVER;
        case CompareFunc::Less: return GL_LESS;
        case CompareFunc::Equal: return GL_EQUAL;
        case CompareFunc::LessEqual: return GL_LEQUAL;
        case CompareFunc::Greater: return GL_GREATER;
        case CompareFunc::NotEqual: return GL_NOTEQUAL;
        case CompareFunc::GreaterEqual: return GL_GEQUAL;
        case CompareFunc::Always: return GL_ALWAYS;
    }
    return GL_LESS;
}

GLenum GLRenderDevice::toGLBlendFactor(BlendFactor factor) {
    switch (factor) {
        case BlendFactor::Zero: return GL_ZERO;
        case BlendFactor::One: return GL_ONE;
        case BlendFactor::SrcAlpha: return GL_SRC_ALPHA;
        case BlendFactor::OneMinusSrcAlpha: return GL_ONE_MINUS_SRC_ALPHA;
        case BlendFactor::DstAlpha: return GL_DST_ALPHA;
        case BlendFactor::OneMinusDstAlpha: return GL_ONE_MINUS_DST_ALPHA;
        case BlendFactor::DstColor: return GL_DST_COLOR;
        case BlendFactor::SrcColor: return GL_SRC_COLOR;
        case BlendFactor::OneMinusDstColor: return GL_ONE_MINUS_DST_COLOR;
        case BlendFactor::OneMinusSrcColor: return GL_ONE_MINUS_SRC_COLOR;
    }
    return GL_ONE;
}

// Factory function for OpenGL backend
std::unique_ptr<RenderDevice> createRenderDevice() {
    return std::make_unique<GLRenderDevice>();
}

} // namespace mc
