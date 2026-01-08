#pragma once

#include "phys/AABB.hpp"
#include "renderer/Tesselator.hpp"
#include <GL/glew.h>

namespace mc {

class Level;
class TileRenderer;

class Chunk {
public:
    // Chunk position in world (block coordinates)
    int x0, y0, z0;
    int x1, y1, z1;

    // Chunk size
    static constexpr int SIZE = 16;

    // State flags
    bool dirty;
    bool loaded;
    bool visible;

    // Bounding box for culling
    AABB bb;

    // Stats
    int solidVertexCount;
    int waterVertexCount;
    int solidIndexCount;
    int waterIndexCount;

    // Distance for sorting
    float distanceSq;

    Level* level;

    Chunk(Level* level, int x0, int y0, int z0);
    ~Chunk();

    // Rebuild the chunk mesh
    void rebuild(TileRenderer& renderer);
    void rebuildSolid(TileRenderer& renderer);
    void rebuildWater(TileRenderer& renderer);

    // Render the chunk
    void render(int pass);

    // Mark as needing rebuild
    void setDirty();

    // Check if position is in this chunk
    bool contains(int x, int y, int z) const;

    // Calculate distance to camera
    void calculateDistance(double camX, double camY, double camZ);

    // Delete OpenGL resources
    void dispose();

private:
    void setupVAO(GLuint vao, GLuint vbo, GLuint ebo);
    void uploadData(GLuint vao, GLuint vbo, GLuint ebo,
                    const Tesselator::VertexData& data);

    // OpenGL VAO/VBO/EBO for solid geometry
    GLuint solidVAO;
    GLuint solidVBO;
    GLuint solidEBO;

    // OpenGL VAO/VBO/EBO for water geometry
    GLuint waterVAO;
    GLuint waterVBO;
    GLuint waterEBO;

    bool vaoInitialized;
};

} // namespace mc
