#pragma once

#include <cstdint>
#include <vector>
#include <memory>
#include "renderer/backend/RenderTypes.hpp"

namespace mc {

class VertexBuffer;
class IndexBuffer;

/**
 * Tesselator - Modern vertex buffer system using the RenderDevice abstraction.
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
    void begin(DrawMode mode = DrawMode::Quads);
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

private:
    Tesselator();
    ~Tesselator();
    Tesselator(const Tesselator&) = delete;
    Tesselator& operator=(const Tesselator&) = delete;

    void draw();
    void buildQuadIndices();

    // RenderDevice buffers (used by both backends)
    std::unique_ptr<VertexBuffer> vertexBuffer;
    std::unique_ptr<IndexBuffer> indexBuffer;
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

    DrawMode mode;
};

} // namespace mc
