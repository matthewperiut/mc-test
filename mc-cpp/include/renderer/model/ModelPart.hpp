#pragma once

#include <vector>

namespace mc {

class Tesselator;

// Represents a single part of an entity model (like head, body, arm, etc.)
// Each part is a textured box that can be positioned and rotated
class ModelPart {
public:
    // Position offset from parent
    float x = 0.0f;
    float y = 0.0f;
    float z = 0.0f;

    // Rotation angles (in radians)
    float xRot = 0.0f;
    float yRot = 0.0f;
    float zRot = 0.0f;

    // Visibility
    bool visible = true;

    ModelPart(int texU, int texV);

    // Set texture offset
    void setTexOffs(int u, int v);

    // Add a box to this model part
    // x0, y0, z0: offset from part origin
    // w, h, d: width, height, depth in pixels (will be scaled by 1/16)
    // inflate: amount to grow the box (for armor layers)
    void addBox(float x0, float y0, float z0, int w, int h, int d, float inflate = 0.0f);

    // Set position offset
    void setPos(float x, float y, float z);

    // Render this model part
    void render(Tesselator& t, float scale);
    void render(Tesselator& t, float scale, float r, float g, float b, float a);

private:
    // Texture coordinates (in 64x32 texture)
    int texU = 0;
    int texV = 0;

    // Vertex data for the box
    struct Vertex {
        float x, y, z;
        float u, v;
    };

    struct Quad {
        Vertex vertices[4];
    };

    std::vector<Quad> quads;

    // Build the box geometry
    void buildBox(float x0, float y0, float z0, float x1, float y1, float z1,
                  int w, int h, int d, bool mirror = false);
};

} // namespace mc
