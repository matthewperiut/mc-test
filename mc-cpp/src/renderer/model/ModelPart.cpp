#include "renderer/model/ModelPart.hpp"
#include "renderer/Tesselator.hpp"
#include "renderer/MatrixStack.hpp"
#include "renderer/ShaderManager.hpp"
#include <GL/glew.h>

namespace mc {

ModelPart::ModelPart(int texU, int texV)
    : texU(texU), texV(texV)
{
}

void ModelPart::setTexOffs(int u, int v) {
    texU = u;
    texV = v;
}

void ModelPart::addBox(float x0, float y0, float z0, int w, int h, int d, float inflate) {
    float x1 = x0 + static_cast<float>(w);
    float y1 = y0 + static_cast<float>(h);
    float z1 = z0 + static_cast<float>(d);

    // Apply inflation
    x0 -= inflate;
    y0 -= inflate;
    z0 -= inflate;
    x1 += inflate;
    y1 += inflate;
    z1 += inflate;

    buildBox(x0, y0, z0, x1, y1, z1, w, h, d);
}

void ModelPart::setPos(float px, float py, float pz) {
    x = px;
    y = py;
    z = pz;
}

void ModelPart::buildBox(float x0, float y0, float z0, float x1, float y1, float z1,
                          int w, int h, int d, bool /*mirror*/) {
    quads.clear();

    // Texture size is 64x32 for mob textures
    constexpr float TEX_WIDTH = 64.0f;
    constexpr float TEX_HEIGHT = 32.0f;

    // Match Java Cube.java exactly
    // Define 8 corner vertices (matching Java naming: u=front/z0, l=back/z1)
    // u0 = front-left-bottom, u1 = front-right-bottom, u2 = front-right-top, u3 = front-left-top
    // l0 = back-left-bottom,  l1 = back-right-bottom,  l2 = back-right-top,  l3 = back-left-top

    // Java Polygon UV remapping: for vertices[0..3] with UV region (u0,v0)-(u1,v1):
    //   vertices[0] gets (u1, v0)
    //   vertices[1] gets (u0, v0)
    //   vertices[2] gets (u0, v1)
    //   vertices[3] gets (u1, v1)

    Quad quad;
    float u0f, v0f, u1f, v1f;

    // polygon[0]: Right face (+X) - vertices {l1, u1, u2, l2}
    // UV region: (texU+d+w, texV+d) to (texU+d+w+d, texV+d+h)
    u0f = (texU + d + w) / TEX_WIDTH;
    v0f = (texV + d) / TEX_HEIGHT;
    u1f = (texU + d + w + d) / TEX_WIDTH;
    v1f = (texV + d + h) / TEX_HEIGHT;
    quad.vertices[0] = {x1, y0, z1, u1f, v0f};  // l1: back-right-bottom
    quad.vertices[1] = {x1, y0, z0, u0f, v0f};  // u1: front-right-bottom
    quad.vertices[2] = {x1, y1, z0, u0f, v1f};  // u2: front-right-top
    quad.vertices[3] = {x1, y1, z1, u1f, v1f};  // l2: back-right-top
    quads.push_back(quad);

    // polygon[1]: Left face (-X) - vertices {u0, l0, l3, u3}
    // UV region: (texU+0, texV+d) to (texU+d, texV+d+h)
    u0f = (texU) / TEX_WIDTH;
    v0f = (texV + d) / TEX_HEIGHT;
    u1f = (texU + d) / TEX_WIDTH;
    v1f = (texV + d + h) / TEX_HEIGHT;
    quad.vertices[0] = {x0, y0, z0, u1f, v0f};  // u0: front-left-bottom
    quad.vertices[1] = {x0, y0, z1, u0f, v0f};  // l0: back-left-bottom
    quad.vertices[2] = {x0, y1, z1, u0f, v1f};  // l3: back-left-top
    quad.vertices[3] = {x0, y1, z0, u1f, v1f};  // u3: front-left-top
    quads.push_back(quad);

    // polygon[2]: Bottom face (-Y) - vertices {l1, l0, u0, u1}
    // UV region: (texU+d, texV+0) to (texU+d+w, texV+d)
    u0f = (texU + d) / TEX_WIDTH;
    v0f = (texV) / TEX_HEIGHT;
    u1f = (texU + d + w) / TEX_WIDTH;
    v1f = (texV + d) / TEX_HEIGHT;
    quad.vertices[0] = {x1, y0, z1, u1f, v0f};  // l1: back-right-bottom
    quad.vertices[1] = {x0, y0, z1, u0f, v0f};  // l0: back-left-bottom
    quad.vertices[2] = {x0, y0, z0, u0f, v1f};  // u0: front-left-bottom
    quad.vertices[3] = {x1, y0, z0, u1f, v1f};  // u1: front-right-bottom
    quads.push_back(quad);

    // polygon[3]: Top face (+Y) - vertices {u2, u3, l3, l2}
    // UV region: (texU+d+w, texV+0) to (texU+d+w+w, texV+d)
    u0f = (texU + d + w) / TEX_WIDTH;
    v0f = (texV) / TEX_HEIGHT;
    u1f = (texU + d + w + w) / TEX_WIDTH;
    v1f = (texV + d) / TEX_HEIGHT;
    quad.vertices[0] = {x1, y1, z0, u1f, v0f};  // u2: front-right-top
    quad.vertices[1] = {x0, y1, z0, u0f, v0f};  // u3: front-left-top
    quad.vertices[2] = {x0, y1, z1, u0f, v1f};  // l3: back-left-top
    quad.vertices[3] = {x1, y1, z1, u1f, v1f};  // l2: back-right-top
    quads.push_back(quad);

    // polygon[4]: Front face (-Z) - vertices {u1, u0, u3, u2}
    // UV region: (texU+d, texV+d) to (texU+d+w, texV+d+h)
    u0f = (texU + d) / TEX_WIDTH;
    v0f = (texV + d) / TEX_HEIGHT;
    u1f = (texU + d + w) / TEX_WIDTH;
    v1f = (texV + d + h) / TEX_HEIGHT;
    quad.vertices[0] = {x1, y0, z0, u1f, v0f};  // u1: front-right-bottom
    quad.vertices[1] = {x0, y0, z0, u0f, v0f};  // u0: front-left-bottom
    quad.vertices[2] = {x0, y1, z0, u0f, v1f};  // u3: front-left-top
    quad.vertices[3] = {x1, y1, z0, u1f, v1f};  // u2: front-right-top
    quads.push_back(quad);

    // polygon[5]: Back face (+Z) - vertices {l0, l1, l2, l3}
    // UV region: (texU+d+w+d, texV+d) to (texU+d+w+d+w, texV+d+h)
    u0f = (texU + d + w + d) / TEX_WIDTH;
    v0f = (texV + d) / TEX_HEIGHT;
    u1f = (texU + d + w + d + w) / TEX_WIDTH;
    v1f = (texV + d + h) / TEX_HEIGHT;
    quad.vertices[0] = {x0, y0, z1, u1f, v0f};  // l0: back-left-bottom
    quad.vertices[1] = {x1, y0, z1, u0f, v0f};  // l1: back-right-bottom
    quad.vertices[2] = {x1, y1, z1, u0f, v1f};  // l2: back-right-top
    quad.vertices[3] = {x0, y1, z1, u1f, v1f};  // l3: back-left-top
    quads.push_back(quad);
}

void ModelPart::render(Tesselator& t, float scale) {
    render(t, scale, 1.0f, 1.0f, 1.0f, 1.0f);
}

void ModelPart::render(Tesselator& t, float scale, float r, float g, float b, float a) {
    if (!visible || quads.empty()) return;

    bool hasRotation = (xRot != 0.0f || yRot != 0.0f || zRot != 0.0f);
    bool hasTranslation = (x != 0.0f || y != 0.0f || z != 0.0f);

    if (hasRotation || hasTranslation) {
        MatrixStack::modelview().push();
    }

    if (hasTranslation) {
        MatrixStack::modelview().translate(x * scale, y * scale, z * scale);
    }

    if (hasRotation) {
        constexpr float RAD_TO_DEG = 180.0f / 3.14159265358979f;
        if (zRot != 0.0f) {
            MatrixStack::modelview().rotate(zRot * RAD_TO_DEG, 0.0f, 0.0f, 1.0f);
        }
        if (yRot != 0.0f) {
            MatrixStack::modelview().rotate(yRot * RAD_TO_DEG, 0.0f, 1.0f, 0.0f);
        }
        if (xRot != 0.0f) {
            MatrixStack::modelview().rotate(xRot * RAD_TO_DEG, 1.0f, 0.0f, 0.0f);
        }
    }

    if (hasRotation || hasTranslation) {
        ShaderManager::getInstance().updateMatrices();
    }

    // Render all quads - each face as a separate quad
    for (const auto& quad : quads) {
        t.begin(DrawMode::Quads);
        t.color(r, g, b, a);

        for (int i = 0; i < 4; ++i) {
            const auto& v = quad.vertices[i];
            t.tex(v.u, v.v);
            t.vertex(v.x * scale, v.y * scale, v.z * scale);
        }

        t.end();
    }

    if (hasRotation || hasTranslation) {
        MatrixStack::modelview().pop();
        ShaderManager::getInstance().updateMatrices();
    }
}

void ModelPart::render(Tesselator& t, float scale, float r, float g, float b, float a, int skyLight, int blockLight) {
    if (!visible || quads.empty()) return;

    bool hasRotation = (xRot != 0.0f || yRot != 0.0f || zRot != 0.0f);
    bool hasTranslation = (x != 0.0f || y != 0.0f || z != 0.0f);

    if (hasRotation || hasTranslation) {
        MatrixStack::modelview().push();
    }

    if (hasTranslation) {
        MatrixStack::modelview().translate(x * scale, y * scale, z * scale);
    }

    if (hasRotation) {
        constexpr float RAD_TO_DEG = 180.0f / 3.14159265358979f;
        if (zRot != 0.0f) {
            MatrixStack::modelview().rotate(zRot * RAD_TO_DEG, 0.0f, 0.0f, 1.0f);
        }
        if (yRot != 0.0f) {
            MatrixStack::modelview().rotate(yRot * RAD_TO_DEG, 0.0f, 1.0f, 0.0f);
        }
        if (xRot != 0.0f) {
            MatrixStack::modelview().rotate(xRot * RAD_TO_DEG, 1.0f, 0.0f, 0.0f);
        }
    }

    if (hasRotation || hasTranslation) {
        ShaderManager::getInstance().updateMatrices();
    }

    // Render all quads - each face as a separate quad
    for (const auto& quad : quads) {
        t.begin(DrawMode::Quads);
        t.lightLevel(skyLight, blockLight);
        t.color(r, g, b, a);

        for (int i = 0; i < 4; ++i) {
            const auto& v = quad.vertices[i];
            t.tex(v.u, v.v);
            t.vertex(v.x * scale, v.y * scale, v.z * scale);
        }

        t.end();
    }

    if (hasRotation || hasTranslation) {
        MatrixStack::modelview().pop();
        ShaderManager::getInstance().updateMatrices();
    }
}

} // namespace mc
