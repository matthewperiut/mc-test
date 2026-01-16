#include "renderer/TileRenderer.hpp"
#include "renderer/Tesselator.hpp"
#include "world/Level.hpp"
#include <GL/glew.h>

namespace mc {

TileRenderer::TileRenderer()
    : level(nullptr)
    , renderAllFaces(false)
    , t(Tesselator::getInstance())
{
}

void TileRenderer::setLevel(Level* level) {
    this->level = level;
}

void TileRenderer::getUV(int textureIndex, float& u0, float& v0, float& u1, float& v1) {
    // Java calculation:
    // int xt = (tex & 15) << 4;  // column * 16
    // int yt = tex & 240;        // row * 16
    // u0 = xt / 256.0
    // u1 = (xt + 15.99) / 256.0
    // v0 = yt / 256.0
    // v1 = (yt + 15.99) / 256.0

    int xt = (textureIndex & 15) << 4;  // column * 16 = pixel X
    int yt = textureIndex & 240;         // row * 16 = pixel Y

    u0 = static_cast<float>(xt) / 256.0f;
    u1 = (static_cast<float>(xt) + 15.99f) / 256.0f;
    v0 = static_cast<float>(yt) / 256.0f;
    v1 = (static_cast<float>(yt) + 15.99f) / 256.0f;
}

float TileRenderer::getBrightness(int x, int y, int z) {
    if (!level) return 1.0f;
    return level->getBrightness(x, y, z);
}

float TileRenderer::getAverageBrightness(int x0, int y0, int z0, int x1, int y1, int z1) {
    float total = 0.0f;
    int count = 0;
    for (int x = x0; x <= x1; x++) {
        for (int y = y0; y <= y1; y++) {
            for (int z = z0; z <= z1; z++) {
                total += getBrightness(x, y, z);
                count++;
            }
        }
    }
    return count > 0 ? total / count : 1.0f;
}

int TileRenderer::getSkyLight(int x, int y, int z) {
    if (!level) return 15;
    return level->getSkyLight(x, y, z);
}

int TileRenderer::getBlockLight(int x, int y, int z) {
    if (!level) return 0;
    return level->getBlockLight(x, y, z);
}

bool TileRenderer::shouldRenderFace(int x, int y, int z, int face) {
    if (renderAllFaces) return true;
    if (!level) return true;

    // Get neighbor position based on face
    int nx = x, ny = y, nz = z;
    switch (face) {
        case 0: ny--; break;  // Down
        case 1: ny++; break;  // Up
        case 2: nz--; break;  // North
        case 3: nz++; break;  // South
        case 4: nx--; break;  // West
        case 5: nx++; break;  // East
    }

    Tile* neighbor = level->getTileAt(nx, ny, nz);
    return !neighbor || neighbor->transparent;
}

bool TileRenderer::renderTileInWorld(int x, int y, int z) {
    if (!level) return false;

    Tile* tile = level->getTileAt(x, y, z);
    if (!tile) return false;

    return renderTile(tile, x, y, z);
}

void TileRenderer::renderBlockItem(Tile* tile, float scale) {
    if (!tile) return;

    // Java ItemRenderer.renderItem() for blocks
    // Renders a standalone block centered at origin with scale

    // Face brightness multipliers
    float c0 = 0.5f;   // Bottom face
    float c1 = 1.0f;   // Top face
    float c2 = 0.6f;   // North/South faces (bottom-right in isometric view)
    float c3 = 0.8f;   // West/East faces (bottom-left in isometric view)

    // Grass color tint
    bool isGrass = (tile->id == Tile::GRASS);
    float grassR = 0.486f;
    float grassG = 0.741f;
    float grassB = 0.420f;

    // Center the block at origin (-0.5 to 0.5)
    float x0 = -0.5f * scale;
    float x1 = 0.5f * scale;
    float y0 = -0.5f * scale;
    float y1 = 0.5f * scale;
    float z0 = -0.5f * scale;
    float z1 = 0.5f * scale;

    // Get UV for each face
    float u0, v0, u1, v1;

    // Bottom face (Y-) - Java renderFaceUp
    getUV(tile->getTexture(0), u0, v0, u1, v1);
    t.color(c0, c0, c0);
    t.vertexUV(x0, y0, z1, u0, v1);
    t.vertexUV(x0, y0, z0, u0, v0);
    t.vertexUV(x1, y0, z0, u1, v0);
    t.vertexUV(x1, y0, z1, u1, v1);

    // Top face (Y+) - Java renderFaceDown
    getUV(tile->getTexture(1), u0, v0, u1, v1);
    if (isGrass) {
        t.color(c1 * grassR, c1 * grassG, c1 * grassB);
    } else {
        t.color(c1, c1, c1);
    }
    t.vertexUV(x1, y1, z1, u1, v1);
    t.vertexUV(x1, y1, z0, u1, v0);
    t.vertexUV(x0, y1, z0, u0, v0);
    t.vertexUV(x0, y1, z1, u0, v1);

    // North face (Z-) - Java renderNorth
    getUV(tile->getTexture(2), u0, v0, u1, v1);
    t.color(c2, c2, c2);
    t.vertexUV(x0, y1, z0, u1, v0);
    t.vertexUV(x1, y1, z0, u0, v0);
    t.vertexUV(x1, y0, z0, u0, v1);
    t.vertexUV(x0, y0, z0, u1, v1);

    // South face (Z+) - Java renderSouth
    getUV(tile->getTexture(3), u0, v0, u1, v1);
    t.color(c2, c2, c2);
    t.vertexUV(x0, y1, z1, u0, v0);
    t.vertexUV(x0, y0, z1, u0, v1);
    t.vertexUV(x1, y0, z1, u1, v1);
    t.vertexUV(x1, y1, z1, u1, v0);

    // West face (X-) - Java renderWest
    getUV(tile->getTexture(4), u0, v0, u1, v1);
    t.color(c3, c3, c3);
    t.vertexUV(x0, y1, z1, u1, v0);
    t.vertexUV(x0, y1, z0, u0, v0);
    t.vertexUV(x0, y0, z0, u0, v1);
    t.vertexUV(x0, y0, z1, u1, v1);

    // East face (X+) - Java renderEast
    getUV(tile->getTexture(5), u0, v0, u1, v1);
    t.color(c3, c3, c3);
    t.vertexUV(x1, y0, z1, u0, v1);
    t.vertexUV(x1, y0, z0, u1, v1);
    t.vertexUV(x1, y1, z0, u1, v0);
    t.vertexUV(x1, y1, z1, u0, v0);
}

void TileRenderer::renderTileForGUI(Tile* tile, int data) {
    if (!tile) return;

    // Match Java TileRenderer.renderTile(Tile, int) exactly
    // Uses normals for OpenGL lighting (for hand rendering)

    int shape = tile->renderShape == TileShape::CUBE ? 0 : 1;

    if (shape == 0) {
        // Standard cube rendering - apply offset to vertices directly (no glTranslatef)
        float ox = -0.5f, oy = -0.5f, oz = -0.5f;
        float u0, v0, u1, v1;

        // Bottom face (Y=0) - Java: normal(0, -1, 0), renderFaceUp
        getUV(tile->getTexture(0, data), u0, v0, u1, v1);
        t.begin(DrawMode::Quads);
        t.normal(0.0f, -1.0f, 0.0f);
        t.color(1.0f, 1.0f, 1.0f);
        t.vertexUV(0+ox, 0+oy, 1+oz, u0, v1);
        t.vertexUV(0+ox, 0+oy, 0+oz, u0, v0);
        t.vertexUV(1+ox, 0+oy, 0+oz, u1, v0);
        t.vertexUV(1+ox, 0+oy, 1+oz, u1, v1);
        t.end();

        // Top face (Y=1) - Java: normal(0, 1, 0), renderFaceDown
        getUV(tile->getTexture(1, data), u0, v0, u1, v1);
        t.begin(DrawMode::Quads);
        t.normal(0.0f, 1.0f, 0.0f);
        if (tile->id == Tile::GRASS) {
            t.color(0.486f, 0.741f, 0.420f);  // Grass tint
        } else {
            t.color(1.0f, 1.0f, 1.0f);
        }
        t.vertexUV(1+ox, 1+oy, 1+oz, u1, v1);
        t.vertexUV(1+ox, 1+oy, 0+oz, u1, v0);
        t.vertexUV(0+ox, 1+oy, 0+oz, u0, v0);
        t.vertexUV(0+ox, 1+oy, 1+oz, u0, v1);
        t.end();

        // North face (Z=0) - Java: normal(0, 0, -1), renderNorth
        getUV(tile->getTexture(2, data), u0, v0, u1, v1);
        t.begin(DrawMode::Quads);
        t.normal(0.0f, 0.0f, -1.0f);
        t.color(1.0f, 1.0f, 1.0f);
        t.vertexUV(0+ox, 1+oy, 0+oz, u1, v0);
        t.vertexUV(1+ox, 1+oy, 0+oz, u0, v0);
        t.vertexUV(1+ox, 0+oy, 0+oz, u0, v1);
        t.vertexUV(0+ox, 0+oy, 0+oz, u1, v1);
        t.end();

        // South face (Z=1) - Java: normal(0, 0, 1), renderSouth
        getUV(tile->getTexture(3, data), u0, v0, u1, v1);
        t.begin(DrawMode::Quads);
        t.normal(0.0f, 0.0f, 1.0f);
        t.color(1.0f, 1.0f, 1.0f);
        t.vertexUV(0+ox, 1+oy, 1+oz, u0, v0);
        t.vertexUV(0+ox, 0+oy, 1+oz, u0, v1);
        t.vertexUV(1+ox, 0+oy, 1+oz, u1, v1);
        t.vertexUV(1+ox, 1+oy, 1+oz, u1, v0);
        t.end();

        // West face (X=0) - Java: normal(-1, 0, 0), renderWest
        getUV(tile->getTexture(4, data), u0, v0, u1, v1);
        t.begin(DrawMode::Quads);
        t.normal(-1.0f, 0.0f, 0.0f);
        t.color(1.0f, 1.0f, 1.0f);
        t.vertexUV(0+ox, 1+oy, 1+oz, u1, v0);
        t.vertexUV(0+ox, 1+oy, 0+oz, u0, v0);
        t.vertexUV(0+ox, 0+oy, 0+oz, u0, v1);
        t.vertexUV(0+ox, 0+oy, 1+oz, u1, v1);
        t.end();

        // East face (X=1) - Java: normal(1, 0, 0), renderEast
        getUV(tile->getTexture(5, data), u0, v0, u1, v1);
        t.begin(DrawMode::Quads);
        t.normal(1.0f, 0.0f, 0.0f);
        t.color(1.0f, 1.0f, 1.0f);
        t.vertexUV(1+ox, 0+oy, 1+oz, u0, v1);
        t.vertexUV(1+ox, 0+oy, 0+oz, u1, v1);
        t.vertexUV(1+ox, 1+oy, 0+oz, u1, v0);
        t.vertexUV(1+ox, 1+oy, 1+oz, u0, v0);
        t.end();
    } else {
        // Cross-shaped block (flowers, etc.) - Java: normal(0, -1, 0)
        t.begin(DrawMode::Quads);
        t.normal(0.0f, -1.0f, 0.0f);
        t.color(1.0f, 1.0f, 1.0f);
        renderCross(tile, 0, 0, 0);
        t.end();
    }
}

void TileRenderer::renderTileForGUIWithColors(Tile* tile, int data) {
    if (!tile) return;

    // Uses vertex colors for face brightness (for GUI slots without OpenGL lighting)
    // Swapped: darker (0.6) on bottom-right, lighter (0.8) on bottom-left
    float brightTop = 1.0f;
    float brightBottom = 0.5f;
    float brightNorth = 0.6f;
    float brightSouth = 0.6f;
    float brightWest = 0.8f;
    float brightEast = 0.8f;

    int shape = tile->renderShape == TileShape::CUBE ? 0 : 1;

    if (shape == 0) {
        // Apply offset directly to vertices instead of using glTranslatef
        float ox = -0.5f, oy = -0.5f, oz = -0.5f;

        float u0, v0, u1, v1;

        // Bottom face (Y=0)
        getUV(tile->getTexture(0, data), u0, v0, u1, v1);
        t.begin(DrawMode::Quads);
        t.color(brightBottom, brightBottom, brightBottom);
        t.vertexUV(0+ox, 0+oy, 1+oz, u0, v1);
        t.vertexUV(0+ox, 0+oy, 0+oz, u0, v0);
        t.vertexUV(1+ox, 0+oy, 0+oz, u1, v0);
        t.vertexUV(1+ox, 0+oy, 1+oz, u1, v1);
        t.end();

        // Top face (Y=1)
        getUV(tile->getTexture(1, data), u0, v0, u1, v1);
        t.begin(DrawMode::Quads);
        if (tile->id == Tile::GRASS) {
            t.color(brightTop * 0.486f, brightTop * 0.741f, brightTop * 0.420f);
        } else {
            t.color(brightTop, brightTop, brightTop);
        }
        t.vertexUV(1+ox, 1+oy, 1+oz, u1, v1);
        t.vertexUV(1+ox, 1+oy, 0+oz, u1, v0);
        t.vertexUV(0+ox, 1+oy, 0+oz, u0, v0);
        t.vertexUV(0+ox, 1+oy, 1+oz, u0, v1);
        t.end();

        // North face (Z=0)
        getUV(tile->getTexture(2, data), u0, v0, u1, v1);
        t.begin(DrawMode::Quads);
        t.color(brightNorth, brightNorth, brightNorth);
        t.vertexUV(0+ox, 1+oy, 0+oz, u1, v0);
        t.vertexUV(1+ox, 1+oy, 0+oz, u0, v0);
        t.vertexUV(1+ox, 0+oy, 0+oz, u0, v1);
        t.vertexUV(0+ox, 0+oy, 0+oz, u1, v1);
        t.end();

        // South face (Z=1)
        getUV(tile->getTexture(3, data), u0, v0, u1, v1);
        t.begin(DrawMode::Quads);
        t.color(brightSouth, brightSouth, brightSouth);
        t.vertexUV(0+ox, 1+oy, 1+oz, u0, v0);
        t.vertexUV(0+ox, 0+oy, 1+oz, u0, v1);
        t.vertexUV(1+ox, 0+oy, 1+oz, u1, v1);
        t.vertexUV(1+ox, 1+oy, 1+oz, u1, v0);
        t.end();

        // West face (X=0)
        getUV(tile->getTexture(4, data), u0, v0, u1, v1);
        t.begin(DrawMode::Quads);
        t.color(brightWest, brightWest, brightWest);
        t.vertexUV(0+ox, 1+oy, 1+oz, u1, v0);
        t.vertexUV(0+ox, 1+oy, 0+oz, u0, v0);
        t.vertexUV(0+ox, 0+oy, 0+oz, u0, v1);
        t.vertexUV(0+ox, 0+oy, 1+oz, u1, v1);
        t.end();

        // East face (X=1)
        getUV(tile->getTexture(5, data), u0, v0, u1, v1);
        t.begin(DrawMode::Quads);
        t.color(brightEast, brightEast, brightEast);
        t.vertexUV(1+ox, 0+oy, 1+oz, u0, v1);
        t.vertexUV(1+ox, 0+oy, 0+oz, u1, v1);
        t.vertexUV(1+ox, 1+oy, 0+oz, u1, v0);
        t.vertexUV(1+ox, 1+oy, 1+oz, u0, v0);
        t.end();
    } else {
        // Cross-shaped block (flowers, etc.)
        t.begin(DrawMode::Quads);
        t.color(1.0f, 1.0f, 1.0f);
        renderCross(tile, 0, 0, 0);
        t.end();
    }
}

bool TileRenderer::renderTile(Tile* tile, int x, int y, int z) {
    if (!tile) return false;

    switch (tile->renderShape) {
        case TileShape::CUBE:
            renderCube(tile, x, y, z);
            return true;

        case TileShape::CROSS:
            renderCross(tile, x, y, z);
            return true;

        case TileShape::TORCH:
            renderTorch(tile, x, y, z, level ? level->getData(x, y, z) : 0);
            return true;

        case TileShape::LIQUID:
            renderLiquid(tile, x, y, z);
            return true;

        case TileShape::CACTUS:
            renderCactus(tile, x, y, z);
            return true;

        default:
            renderCube(tile, x, y, z);
            return true;
    }
}

void TileRenderer::renderCube(Tile* tile, int x, int y, int z) {
    // Face brightness multipliers (matching Java) - now used as vertex color base
    float c0 = 0.5f;   // Bottom face
    float c1 = 1.0f;   // Top face
    float c2 = 0.8f;   // North/South faces
    float c3 = 0.6f;   // West/East faces

    // Grass color tint (the grass texture is grayscale, needs green tint)
    // Beta 1.2_02 used a fixed grass color before biome-based coloring
    bool isGrass = (tile->id == Tile::GRASS);
    float grassR = 0.486f;  // 124/255
    float grassG = 0.741f;  // 189/255
    float grassB = 0.420f;  // 107/255

    // Render each face if visible
    // Now using separate light levels instead of baked brightness
    if (shouldRenderFace(x, y, z, 0)) {
        int skyLight = getSkyLight(x, y - 1, z);
        int blockLight = getBlockLight(x, y - 1, z);
        t.lightLevel(skyLight, blockLight);
        t.color(c0, c0, c0);  // Face shading only
        renderFaceDown(tile, x, y, z, tile->getTexture(0));
    }
    if (shouldRenderFace(x, y, z, 1)) {
        int skyLight = getSkyLight(x, y + 1, z);
        int blockLight = getBlockLight(x, y + 1, z);
        t.lightLevel(skyLight, blockLight);
        // Apply grass tint to top face
        if (isGrass) {
            t.color(c1 * grassR, c1 * grassG, c1 * grassB);
        } else {
            t.color(c1, c1, c1);
        }
        renderFaceUp(tile, x, y, z, tile->getTexture(1));
    }
    if (shouldRenderFace(x, y, z, 2)) {
        int skyLight = getSkyLight(x, y, z - 1);
        int blockLight = getBlockLight(x, y, z - 1);
        t.lightLevel(skyLight, blockLight);
        t.color(c2, c2, c2);
        renderFaceNorth(tile, x, y, z, tile->getTexture(2));
    }
    if (shouldRenderFace(x, y, z, 3)) {
        int skyLight = getSkyLight(x, y, z + 1);
        int blockLight = getBlockLight(x, y, z + 1);
        t.lightLevel(skyLight, blockLight);
        t.color(c2, c2, c2);
        renderFaceSouth(tile, x, y, z, tile->getTexture(3));
    }
    if (shouldRenderFace(x, y, z, 4)) {
        int skyLight = getSkyLight(x - 1, y, z);
        int blockLight = getBlockLight(x - 1, y, z);
        t.lightLevel(skyLight, blockLight);
        t.color(c3, c3, c3);
        renderFaceWest(tile, x, y, z, tile->getTexture(4));
    }
    if (shouldRenderFace(x, y, z, 5)) {
        int skyLight = getSkyLight(x + 1, y, z);
        int blockLight = getBlockLight(x + 1, y, z);
        t.lightLevel(skyLight, blockLight);
        t.color(c3, c3, c3);
        renderFaceEast(tile, x, y, z, tile->getTexture(5));
    }
}

// Java renderFaceUp renders the BOTTOM face (confusing naming)
void TileRenderer::renderFaceDown(Tile* /*tile*/, double x, double y, double z, int texture) {
    float u0, v0, u1, v1;
    getUV(texture, u0, v0, u1, v1);

    // Bottom face (y = y) - CCW winding when viewed from below
    t.vertexUV(x,     y, z + 1, u0, v1);
    t.vertexUV(x,     y, z,     u0, v0);
    t.vertexUV(x + 1, y, z,     u1, v0);
    t.vertexUV(x + 1, y, z + 1, u1, v1);
}

// Java renderFaceDown renders the TOP face (confusing naming)
void TileRenderer::renderFaceUp(Tile* /*tile*/, double x, double y, double z, int texture) {
    float u0, v0, u1, v1;
    getUV(texture, u0, v0, u1, v1);

    // Top face (y = y + 1)
    t.vertexUV(x,     y + 1, z,     u0, v0);
    t.vertexUV(x,     y + 1, z + 1, u0, v1);
    t.vertexUV(x + 1, y + 1, z + 1, u1, v1);
    t.vertexUV(x + 1, y + 1, z,     u1, v0);
}

void TileRenderer::renderFaceNorth(Tile* /*tile*/, double x, double y, double z, int texture) {
    float u0, v0, u1, v1;
    getUV(texture, u0, v0, u1, v1);

    // North face (z = z)
    t.vertexUV(x,     y + 1, z, u1, v0);
    t.vertexUV(x + 1, y + 1, z, u0, v0);
    t.vertexUV(x + 1, y,     z, u0, v1);
    t.vertexUV(x,     y,     z, u1, v1);
}

void TileRenderer::renderFaceSouth(Tile* /*tile*/, double x, double y, double z, int texture) {
    float u0, v0, u1, v1;
    getUV(texture, u0, v0, u1, v1);

    // South face (z = z + 1)
    t.vertexUV(x,     y,     z + 1, u0, v1);
    t.vertexUV(x + 1, y,     z + 1, u1, v1);
    t.vertexUV(x + 1, y + 1, z + 1, u1, v0);
    t.vertexUV(x,     y + 1, z + 1, u0, v0);
}

void TileRenderer::renderFaceWest(Tile* /*tile*/, double x, double y, double z, int texture) {
    float u0, v0, u1, v1;
    getUV(texture, u0, v0, u1, v1);

    // West face (x = x)
    t.vertexUV(x, y,     z,     u1, v1);
    t.vertexUV(x, y,     z + 1, u0, v1);
    t.vertexUV(x, y + 1, z + 1, u0, v0);
    t.vertexUV(x, y + 1, z,     u1, v0);
}

void TileRenderer::renderFaceEast(Tile* /*tile*/, double x, double y, double z, int texture) {
    float u0, v0, u1, v1;
    getUV(texture, u0, v0, u1, v1);

    // East face (x = x + 1)
    t.vertexUV(x + 1, y + 1, z,     u0, v0);
    t.vertexUV(x + 1, y + 1, z + 1, u1, v0);
    t.vertexUV(x + 1, y,     z + 1, u1, v1);
    t.vertexUV(x + 1, y,     z,     u0, v1);
}

void TileRenderer::renderCross(Tile* tile, int x, int y, int z) {
    float u0, v0, u1, v1;
    getUV(tile->textureIndex, u0, v0, u1, v1);

    int skyLight = getSkyLight(x, y, z);
    int blockLight = getBlockLight(x, y, z);
    t.lightLevel(skyLight, blockLight);
    t.color(1.0f, 1.0f, 1.0f);  // Full brightness, shader will apply light

    // Two crossed quads
    double d = 0.45;

    // Diagonal 1
    t.vertexUV(x + 0.5 - d, y,     z + 0.5 - d, u0, v1);
    t.vertexUV(x + 0.5 - d, y + 1, z + 0.5 - d, u0, v0);
    t.vertexUV(x + 0.5 + d, y + 1, z + 0.5 + d, u1, v0);
    t.vertexUV(x + 0.5 + d, y,     z + 0.5 + d, u1, v1);

    t.vertexUV(x + 0.5 + d, y,     z + 0.5 + d, u0, v1);
    t.vertexUV(x + 0.5 + d, y + 1, z + 0.5 + d, u0, v0);
    t.vertexUV(x + 0.5 - d, y + 1, z + 0.5 - d, u1, v0);
    t.vertexUV(x + 0.5 - d, y,     z + 0.5 - d, u1, v1);

    // Diagonal 2
    t.vertexUV(x + 0.5 - d, y,     z + 0.5 + d, u0, v1);
    t.vertexUV(x + 0.5 - d, y + 1, z + 0.5 + d, u0, v0);
    t.vertexUV(x + 0.5 + d, y + 1, z + 0.5 - d, u1, v0);
    t.vertexUV(x + 0.5 + d, y,     z + 0.5 - d, u1, v1);

    t.vertexUV(x + 0.5 + d, y,     z + 0.5 - d, u0, v1);
    t.vertexUV(x + 0.5 + d, y + 1, z + 0.5 - d, u0, v0);
    t.vertexUV(x + 0.5 - d, y + 1, z + 0.5 + d, u1, v0);
    t.vertexUV(x + 0.5 - d, y,     z + 0.5 + d, u1, v1);
}

void TileRenderer::renderTorch(Tile* tile, int x, int y, int z, int metadata) {
    // Get texture coordinates exactly like Java (lines 737-746)
    int tex = tile->textureIndex;
    int xt = (tex & 15) << 4;      // X pixel position in atlas
    int yt = tex & 240;            // Y pixel position in atlas

    float u0 = static_cast<float>(xt) / 256.0f;
    float u1 = (static_cast<float>(xt) + 15.99f) / 256.0f;
    float v0 = static_cast<float>(yt) / 256.0f;
    float v1 = (static_cast<float>(yt) + 15.99f) / 256.0f;

    // UV coordinates for the top cap (flame center) - matching Java lines 743-746
    double uc0 = static_cast<double>(u0) + 0.02734375;   // 7/256
    double vc0 = static_cast<double>(v0) + 0.0234375;    // 6/256
    double uc1 = static_cast<double>(u0) + 0.03515625;   // 9/256
    double vc1 = static_cast<double>(v0) + 0.03125;      // 8/256

    int skyLight = getSkyLight(x, y, z);
    int blockLight = getBlockLight(x, y, z);
    t.lightLevel(skyLight, blockLight);
    t.color(1.0f, 1.0f, 1.0f);

    // Calculate position and tilt based on metadata (matching Java tesselateTorchInWorld lines 73-97)
    double px = static_cast<double>(x);
    double py = static_cast<double>(y);
    double pz = static_cast<double>(z);
    double xxa = 0.0;  // X-axis tilt
    double zza = 0.0;  // Z-axis tilt

    double tiltAmount = 0.4;
    double heightOffset = 0.2;
    double posOffset = 0.1;  // 0.5 - 0.4

    // Metadata: 1=west wall, 2=east wall, 3=north wall, 4=south wall, 5=floor
    switch (metadata) {
        case 1:  // Attached to west wall (x-1), torch leans east
            px = x - posOffset;
            py = y + heightOffset;
            xxa = -tiltAmount;
            break;
        case 2:  // Attached to east wall (x+1), torch leans west
            px = x + posOffset;
            py = y + heightOffset;
            xxa = tiltAmount;
            break;
        case 3:  // Attached to north wall (z-1), torch leans south
            pz = z - posOffset;
            py = y + heightOffset;
            zza = -tiltAmount;
            break;
        case 4:  // Attached to south wall (z+1), torch leans north
            pz = z + posOffset;
            py = y + heightOffset;
            zza = tiltAmount;
            break;
        case 5:  // Floor mounted
        default:
            // Straight up, no tilt
            break;
    }

    // Add 0.5 to center in block (matching Java line 747-748)
    px += 0.5;
    pz += 0.5;

    // Block edges (FULL block, not torch width) - matching Java lines 749-752
    double bx0 = px - 0.5;
    double bx1 = px + 0.5;
    double bz0 = pz - 0.5;
    double bz1 = pz + 0.5;

    // Torch dimensions - matching Java lines 753-754
    double r = 0.0625;   // Half width: 1/16
    double h = 0.625;    // Height ratio: 10/16

    // Top cap (horizontal quad at torch head) - matching Java lines 755-758
    double topOffX = xxa * (1.0 - h);
    double topOffZ = zza * (1.0 - h);
    t.vertexUV(px + topOffX - r, py + h, pz + topOffZ - r, uc0, vc0);
    t.vertexUV(px + topOffX - r, py + h, pz + topOffZ + r, uc0, vc1);
    t.vertexUV(px + topOffX + r, py + h, pz + topOffZ + r, uc1, vc1);
    t.vertexUV(px + topOffX + r, py + h, pz + topOffZ - r, uc1, vc0);

    // Four vertical faces forming a cross pattern - matching Java lines 759-774 exactly
    // Face 1: -X side plane (at x = px - r, spans full block Z)
    t.vertexUV(px - r,       py + 1.0, bz0,       u0, v0);
    t.vertexUV(px - r + xxa, py,       bz0 + zza, u0, v1);
    t.vertexUV(px - r + xxa, py,       bz1 + zza, u1, v1);
    t.vertexUV(px - r,       py + 1.0, bz1,       u1, v0);

    // Face 2: +X side plane (at x = px + r, spans full block Z, reversed winding)
    t.vertexUV(px + r,       py + 1.0, bz1,       u0, v0);
    t.vertexUV(px + r + xxa, py,       bz1 + zza, u0, v1);
    t.vertexUV(px + r + xxa, py,       bz0 + zza, u1, v1);
    t.vertexUV(px + r,       py + 1.0, bz0,       u1, v0);

    // Face 3: +Z side plane (at z = pz + r, spans full block X)
    t.vertexUV(bx0,       py + 1.0, pz + r,       u0, v0);
    t.vertexUV(bx0 + xxa, py,       pz + r + zza, u0, v1);
    t.vertexUV(bx1 + xxa, py,       pz + r + zza, u1, v1);
    t.vertexUV(bx1,       py + 1.0, pz + r,       u1, v0);

    // Face 4: -Z side plane (at z = pz - r, spans full block X, reversed winding)
    t.vertexUV(bx1,       py + 1.0, pz - r,       u0, v0);
    t.vertexUV(bx1 + xxa, py,       pz - r + zza, u0, v1);
    t.vertexUV(bx0 + xxa, py,       pz - r + zza, u1, v1);
    t.vertexUV(bx0,       py + 1.0, pz - r,       u1, v0);
}

void TileRenderer::renderLiquid(Tile* tile, int x, int y, int z) {
    float u0, v0, u1, v1;
    getUV(tile->textureIndex, u0, v0, u1, v1);

    int skyLight = getSkyLight(x, y, z);
    int blockLight = getBlockLight(x, y, z);
    t.lightLevel(skyLight, blockLight);
    t.color(1.0f, 1.0f, 1.0f);  // Full brightness, shader will apply light

    // Render as slightly lowered cube
    double h = 0.875;  // Water height

    // Top face (lowered)
    if (shouldRenderFace(x, y, z, 1)) {
        t.vertexUV(x,     y + h, z,     u0, v0);
        t.vertexUV(x,     y + h, z + 1, u0, v1);
        t.vertexUV(x + 1, y + h, z + 1, u1, v1);
        t.vertexUV(x + 1, y + h, z,     u1, v0);
    }

    // Other faces as normal
    if (shouldRenderFace(x, y, z, 0)) {
        renderFaceDown(tile, x, y, z, tile->textureIndex);
    }
    if (shouldRenderFace(x, y, z, 2)) {
        renderFaceNorth(tile, x, y, z, tile->textureIndex);
    }
    if (shouldRenderFace(x, y, z, 3)) {
        renderFaceSouth(tile, x, y, z, tile->textureIndex);
    }
    if (shouldRenderFace(x, y, z, 4)) {
        renderFaceWest(tile, x, y, z, tile->textureIndex);
    }
    if (shouldRenderFace(x, y, z, 5)) {
        renderFaceEast(tile, x, y, z, tile->textureIndex);
    }
}

bool TileRenderer::canRender(int renderShape) {
    // Java: return shape 0 (cube), 13 (cactus), 10 (stairs), 11 (fence)
    // We only have CUBE as 0 for now
    return renderShape == 0 ||       // Cube
           renderShape == 13 ||      // Cactus
           renderShape == 10 ||      // Stairs
           renderShape == 11;        // Fence
}

void TileRenderer::renderCactus(Tile* tile, int x, int y, int z) {
    float u0, v0, u1, v1;
    double offset = 0.0625;  // Inset by 1/16

    // Get light at cactus position
    int skyLight = getSkyLight(x, y, z);
    int blockLight = getBlockLight(x, y, z);
    t.lightLevel(skyLight, blockLight);

    // Sides are inset - North/South faces (0.8 multiplier)
    getUV(tile->getTexture(2), u0, v0, u1, v1);
    t.color(0.8f, 0.8f, 0.8f);  // Face shading

    // North (inset)
    t.vertexUV(x,     y + 1, z + offset, u1, v0);
    t.vertexUV(x + 1, y + 1, z + offset, u0, v0);
    t.vertexUV(x + 1, y,     z + offset, u0, v1);
    t.vertexUV(x,     y,     z + offset, u1, v1);

    // South (inset)
    t.vertexUV(x,     y,     z + 1 - offset, u0, v1);
    t.vertexUV(x + 1, y,     z + 1 - offset, u1, v1);
    t.vertexUV(x + 1, y + 1, z + 1 - offset, u1, v0);
    t.vertexUV(x,     y + 1, z + 1 - offset, u0, v0);

    // West/East faces (0.6 multiplier)
    t.color(0.6f, 0.6f, 0.6f);

    // West (inset)
    t.vertexUV(x + offset, y,     z,     u1, v1);
    t.vertexUV(x + offset, y,     z + 1, u0, v1);
    t.vertexUV(x + offset, y + 1, z + 1, u0, v0);
    t.vertexUV(x + offset, y + 1, z,     u1, v0);

    // East (inset)
    t.vertexUV(x + 1 - offset, y + 1, z,     u0, v0);
    t.vertexUV(x + 1 - offset, y + 1, z + 1, u1, v0);
    t.vertexUV(x + 1 - offset, y,     z + 1, u1, v1);
    t.vertexUV(x + 1 - offset, y,     z,     u0, v1);

    // Top and bottom
    if (shouldRenderFace(x, y, z, 1)) {
        int topSkyLight = getSkyLight(x, y + 1, z);
        int topBlockLight = getBlockLight(x, y + 1, z);
        t.lightLevel(topSkyLight, topBlockLight);
        t.color(1.0f, 1.0f, 1.0f);  // Top face full brightness
        renderFaceUp(tile, x, y, z, tile->getTexture(1));
    }
    if (shouldRenderFace(x, y, z, 0)) {
        int bottomSkyLight = getSkyLight(x, y - 1, z);
        int bottomBlockLight = getBlockLight(x, y - 1, z);
        t.lightLevel(bottomSkyLight, bottomBlockLight);
        t.color(0.5f, 0.5f, 0.5f);  // Bottom face shading
        renderFaceDown(tile, x, y, z, tile->getTexture(0));
    }
}

} // namespace mc
