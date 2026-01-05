#pragma once

#include <cstdint>
#include <vector>
#include <GL/glew.h>

namespace mc {

/**
 * Tesselator - A vertex buffer system similar to Minecraft's original.
 * Uses modern OpenGL (VAO/VBO) but provides a similar API to the Java version.
 *
 * Vertex format (32 bytes per vertex):
 * - Position: 3 floats (12 bytes)
 * - Texture:  2 floats (8 bytes)
 * - Color:    4 bytes (RGBA packed)
 * - Normal:   3 bytes + 1 padding (4 bytes)
 * - Padding:  4 bytes (for alignment)
 */
class Tesselator {
public:
    static Tesselator& getInstance();

    // Lifecycle
    void init();
    void destroy();

    // Begin/End batch
    void begin(GLenum mode = GL_QUADS);
    void end();
    void draw();

    // Vertex data
    void vertex(float x, float y, float z);
    void vertexUV(float x, float y, float z, float u, float v);

    // State setters (affect subsequent vertices)
    void color(float r, float g, float b);
    void color(float r, float g, float b, float a);
    void color(int rgba);
    void tex(float u, float v);
    void normal(float x, float y, float z);
    void offset(float x, float y, float z);

    // State management
    void noColor();
    void setColorState(bool hasColor);
    void setNormalState(bool hasNormal);
    void setTextureState(bool hasTexture);

    // Clear state
    void clear();

    // Stats
    int getVertexCount() const { return vertexCount; }

private:
    Tesselator();
    ~Tesselator();
    Tesselator(const Tesselator&) = delete;
    Tesselator& operator=(const Tesselator&) = delete;

    void ensureCapacity(size_t additionalVertices);
    void flushIfNeeded();

    // OpenGL objects
    GLuint vao;
    GLuint vbo;
    bool initialized;

    // Vertex data buffer
    static constexpr size_t INITIAL_BUFFER_SIZE = 0x200000;  // 2MB
    static constexpr size_t VERTEX_SIZE = 32;  // bytes per vertex
    std::vector<uint8_t> buffer;
    size_t bufferPos;
    int vertexCount;

    // Current state
    float currentU, currentV;
    float currentR, currentG, currentB, currentA;
    float currentNX, currentNY, currentNZ;
    float offsetX, offsetY, offsetZ;

    // State flags
    bool hasColor;
    bool hasTexture;
    bool hasNormal;
    bool drawing;

    GLenum drawMode;
};

} // namespace mc
