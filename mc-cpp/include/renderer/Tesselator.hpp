#pragma once

#include <cstdint>
#include <vector>
#include <memory>

#ifndef MC_RENDERER_METAL
#include <GL/glew.h>
#else
// Forward declarations for Metal build
typedef unsigned int GLenum;
typedef unsigned int GLuint;
#define GL_QUADS 0x0007
#define GL_TRIANGLES 0x0004
#define GL_LINES 0x0001
#define GL_LINE_STRIP 0x0003
#define GL_LINE_LOOP 0x0002
#define GL_TRIANGLE_FAN 0x0006
#define GL_POINTS 0x0000
#endif

namespace mc {

class VertexBuffer;
class IndexBuffer;

/**
 * Tesselator - Modern OpenGL 3.3 vertex buffer system.
 * Uses VAO/VBO for rendering with automatic GL_QUADS to GL_TRIANGLES conversion.
 *
 * Vertex format (32 bytes per vertex = 8 ints):
 * - Position: 3 floats (12 bytes) at byte offset 0  - location 0
 * - Texture:  2 floats (8 bytes) at byte offset 12  - location 1
 * - Color:    4 bytes (RGBA) at byte offset 20      - location 2
 * - Normal:   4 bytes (3 signed bytes + padding) at byte offset 24 - location 3
 * - Light:    2 bytes (skyLight, blockLight) at byte offset 28 - location 4
 * - Padding:  2 bytes at byte offset 30
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

    // Vertex data
    void vertex(double x, double y, double z);
    void vertexUV(double x, double y, double z, double u, double v);

    // State setters (affect subsequent vertices)
    void color(float r, float g, float b);
    void color(float r, float g, float b, float a);
    void color(int r, int g, int b);
    void color(int r, int g, int b, int a);
    void color(int rgba);
    void tex(double u, double v);
    void normal(float x, float y, float z);
    void lightLevel(int skyLight, int blockLight);  // Set light levels (0-15 each)
    void offset(double xo, double yo, double zo);

    // State management
    void noColor();
    void setColorOpaque_F(float r, float g, float b);

    // Clear state
    void clear();

    // Stats
    int getVertexCount() const { return vertices; }

    // Data extraction for chunk building (returns data without drawing)
    struct VertexData {
        std::vector<int> vertices;
        std::vector<unsigned int> indices;
        int vertexCount;
        bool hasColor;
        bool hasTexture;
        bool hasNormal;
    };
    VertexData getVertexData();

    // VAO attribute locations
    static constexpr GLuint ATTRIB_POSITION = 0;
    static constexpr GLuint ATTRIB_TEXCOORD = 1;
    static constexpr GLuint ATTRIB_COLOR = 2;
    static constexpr GLuint ATTRIB_NORMAL = 3;
    static constexpr GLuint ATTRIB_LIGHT = 4;

private:
    Tesselator();
    ~Tesselator();
    Tesselator(const Tesselator&) = delete;
    Tesselator& operator=(const Tesselator&) = delete;

    void draw();
    void buildQuadIndices();
    void setupVAO();

#ifdef MC_RENDERER_METAL
    // RenderDevice buffers for Metal
    std::unique_ptr<VertexBuffer> vertexBuffer;
    std::unique_ptr<IndexBuffer> indexBuffer;
#else
    // OpenGL objects
    GLuint vao;
    GLuint vbo;
    GLuint ebo;
#endif
    bool vaoInitialized;

    // Vertex data array (matches Java int[] array)
    static constexpr int MAX_VERTICES = 524288;
    static constexpr int VERTEX_STRIDE = 8;      // 8 ints (32 bytes) per vertex
    std::vector<int> array;
    std::vector<unsigned int> indices;
    int p;           // Current position in array (in ints)
    int vertices;    // Number of vertices added
    int count;       // Vertex count within current primitive

    // Current state
    double u, v;
    int col;
    int normalValue;
    int lightValue;  // Packed sky light (low byte) and block light (high byte)
    double xo, yo, zo;

    // State flags
    bool hasColor;
    bool hasTexture;
    bool hasNormal;
    bool hasLight;
    bool noColorFlag;
    bool tesselating;

    GLenum mode;
};

} // namespace mc
