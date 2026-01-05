#include "renderer/TileRenderer.hpp"
#include "renderer/Tesselator.hpp"
#include "world/Level.hpp"

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
    // Face brightness multipliers (matching Java)
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
    if (shouldRenderFace(x, y, z, 0)) {
        float br = getBrightness(x, y - 1, z) * c0;
        t.color(br, br, br);
        renderFaceDown(tile, x, y, z, tile->getTexture(0));
    }
    if (shouldRenderFace(x, y, z, 1)) {
        float br = getBrightness(x, y + 1, z) * c1;
        // Apply grass tint to top face
        if (isGrass) {
            t.color(br * grassR, br * grassG, br * grassB);
        } else {
            t.color(br, br, br);
        }
        renderFaceUp(tile, x, y, z, tile->getTexture(1));
    }
    if (shouldRenderFace(x, y, z, 2)) {
        float br = getBrightness(x, y, z - 1) * c2;
        t.color(br, br, br);
        renderFaceNorth(tile, x, y, z, tile->getTexture(2));
    }
    if (shouldRenderFace(x, y, z, 3)) {
        float br = getBrightness(x, y, z + 1) * c2;
        t.color(br, br, br);
        renderFaceSouth(tile, x, y, z, tile->getTexture(3));
    }
    if (shouldRenderFace(x, y, z, 4)) {
        float br = getBrightness(x - 1, y, z) * c3;
        t.color(br, br, br);
        renderFaceWest(tile, x, y, z, tile->getTexture(4));
    }
    if (shouldRenderFace(x, y, z, 5)) {
        float br = getBrightness(x + 1, y, z) * c3;
        t.color(br, br, br);
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

    float brightness = getBrightness(x, y, z);
    t.color(brightness, brightness, brightness);

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

void TileRenderer::renderTorch(Tile* tile, int x, int y, int z, int /*metadata*/) {
    float u0, v0, u1, v1;
    getUV(tile->textureIndex, u0, v0, u1, v1);

    float brightness = getBrightness(x, y, z);
    t.color(brightness, brightness, brightness);

    // Torch stick (simplified)
    double w = 0.0625;  // 1/16
    double h = 0.625;   // 10/16

    // Front
    t.vertexUV(x + 0.5 - w, y,     z + 0.5 + w, u0, v1);
    t.vertexUV(x + 0.5 - w, y + h, z + 0.5 + w, u0, v0);
    t.vertexUV(x + 0.5 + w, y + h, z + 0.5 + w, u1, v0);
    t.vertexUV(x + 0.5 + w, y,     z + 0.5 + w, u1, v1);

    // Back
    t.vertexUV(x + 0.5 + w, y,     z + 0.5 - w, u0, v1);
    t.vertexUV(x + 0.5 + w, y + h, z + 0.5 - w, u0, v0);
    t.vertexUV(x + 0.5 - w, y + h, z + 0.5 - w, u1, v0);
    t.vertexUV(x + 0.5 - w, y,     z + 0.5 - w, u1, v1);

    // Left
    t.vertexUV(x + 0.5 - w, y,     z + 0.5 - w, u0, v1);
    t.vertexUV(x + 0.5 - w, y + h, z + 0.5 - w, u0, v0);
    t.vertexUV(x + 0.5 - w, y + h, z + 0.5 + w, u1, v0);
    t.vertexUV(x + 0.5 - w, y,     z + 0.5 + w, u1, v1);

    // Right
    t.vertexUV(x + 0.5 + w, y,     z + 0.5 + w, u0, v1);
    t.vertexUV(x + 0.5 + w, y + h, z + 0.5 + w, u0, v0);
    t.vertexUV(x + 0.5 + w, y + h, z + 0.5 - w, u1, v0);
    t.vertexUV(x + 0.5 + w, y,     z + 0.5 - w, u1, v1);
}

void TileRenderer::renderLiquid(Tile* tile, int x, int y, int z) {
    float u0, v0, u1, v1;
    getUV(tile->textureIndex, u0, v0, u1, v1);

    float brightness = getBrightness(x, y, z);
    t.color(brightness, brightness, brightness);

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

void TileRenderer::renderCactus(Tile* tile, int x, int y, int z) {
    float u0, v0, u1, v1;
    double offset = 0.0625;  // Inset by 1/16

    // Sides are inset
    getUV(tile->getTexture(2), u0, v0, u1, v1);
    float brightness = getBrightness(x, y, z) * 0.8f;
    t.color(brightness, brightness, brightness);

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

    brightness = getBrightness(x, y, z) * 0.6f;
    t.color(brightness, brightness, brightness);

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
        brightness = getBrightness(x, y + 1, z);
        t.color(brightness, brightness, brightness);
        renderFaceUp(tile, x, y, z, tile->getTexture(1));
    }
    if (shouldRenderFace(x, y, z, 0)) {
        brightness = getBrightness(x, y - 1, z) * 0.5f;
        t.color(brightness, brightness, brightness);
        renderFaceDown(tile, x, y, z, tile->getTexture(0));
    }
}

} // namespace mc
