#pragma once

#include "world/tile/Tile.hpp"

namespace mc {

class Level;
class Tesselator;

class TileRenderer {
public:
    Level* level;
    bool renderAllFaces;

    // Texture coordinates (for 256x256 terrain.png with 16x16 tiles)
    static constexpr int TILES_PER_ROW = 16;
    static constexpr float TILE_SIZE = 1.0f / 16.0f;

    TileRenderer();
    void setLevel(Level* level);

    // Render a tile at world position
    bool renderTile(Tile* tile, int x, int y, int z);
    bool renderTileInWorld(int x, int y, int z);

    // Render a standalone block for items (no level context, all faces)
    void renderBlockItem(Tile* tile, float scale);

    // Render a tile for GUI display (matches Java TileRenderer.renderTile(Tile, int))
    // renderTileForGUI uses normals for OpenGL lighting (hand rendering)
    // renderTileForGUIWithColors uses vertex colors (GUI slots without lighting)
    void renderTileForGUI(Tile* tile, int data = 0);
    void renderTileForGUIWithColors(Tile* tile, int data = 0);

    // Render specific shapes
    void renderCube(Tile* tile, int x, int y, int z);
    void renderCross(Tile* tile, int x, int y, int z);
    void renderTorch(Tile* tile, int x, int y, int z, int metadata);
    void renderLiquid(Tile* tile, int x, int y, int z);
    void renderCactus(Tile* tile, int x, int y, int z);

    // Render individual faces
    void renderFaceDown(Tile* tile, double x, double y, double z, int texture);
    void renderFaceUp(Tile* tile, double x, double y, double z, int texture);
    void renderFaceNorth(Tile* tile, double x, double y, double z, int texture);
    void renderFaceSouth(Tile* tile, double x, double y, double z, int texture);
    void renderFaceWest(Tile* tile, double x, double y, double z, int texture);
    void renderFaceEast(Tile* tile, double x, double y, double z, int texture);

    // Get brightness at position (legacy - for GUI rendering)
    float getBrightness(int x, int y, int z);
    float getAverageBrightness(int x0, int y0, int z0, int x1, int y1, int z1);

    // Get light levels at position (for dynamic lighting)
    int getSkyLight(int x, int y, int z);
    int getBlockLight(int x, int y, int z);

    // Check if a render shape can be rendered as a 3D block
    // (matches Java TileRenderer.canRender)
    static bool canRender(int renderShape);

private:
    // Get UV coordinates from texture index
    void getUV(int textureIndex, float& u0, float& v0, float& u1, float& v1);

    // Check if face should be rendered (neighbor is transparent)
    bool shouldRenderFace(int x, int y, int z, int face);

    Tesselator& t;
};

} // namespace mc
