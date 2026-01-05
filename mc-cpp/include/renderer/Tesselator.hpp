#pragma once

#include <cstdint>
#include <vector>
#include <GL/glew.h>

namespace mc {

/**
 * Tesselator - A vertex buffer system matching Minecraft's original Java implementation.
 * Uses client-side vertex arrays (NOT VAO/VBO) so that draw calls can be captured
 * into display lists.
 *
 * Vertex format (32 bytes per vertex = 8 ints):
 * - Position: 3 floats (12 bytes) at byte offset 0
 * - Texture:  2 floats (8 bytes) at byte offset 12
 * - Color:    4 bytes (RGBA) at byte offset 20
 * - Normal:   4 bytes (3 signed bytes + padding) at byte offset 24
 * - Padding:  4 bytes at byte offset 28
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
    void offset(double xo, double yo, double zo);

    // State management
    void noColor();
    void setColorOpaque_F(float r, float g, float b);

    // Clear state
    void clear();

    // Stats
    int getVertexCount() const { return vertices; }

private:
    Tesselator();
    ~Tesselator();
    Tesselator(const Tesselator&) = delete;
    Tesselator& operator=(const Tesselator&) = delete;

    void draw();

    // Vertex data array (matches Java int[] array)
    static constexpr int MAX_VERTICES = 524288;  // 2097152 / 4 quads worth
    static constexpr int VERTEX_STRIDE = 8;      // 8 ints (32 bytes) per vertex
    std::vector<int> array;
    int p;           // Current position in array (in ints)
    int vertices;    // Number of vertices added
    int count;       // Vertex count within current primitive

    // Current state
    double u, v;
    int col;
    int normalValue;
    double xo, yo, zo;

    // State flags
    bool hasColor;
    bool hasTexture;
    bool hasNormal;
    bool noColorFlag;
    bool tesselating;

    GLenum mode;
};

} // namespace mc
