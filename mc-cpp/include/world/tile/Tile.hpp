#pragma once

#include "phys/AABB.hpp"
#include <string>
#include <vector>
#include <functional>
#include <memory>

namespace mc {

class Level;
class TileRenderer;

// Tile render layers (determines which pass the block is rendered in)
enum class TileLayer {
    SOLID = 0,       // Fully opaque blocks (stone, dirt, etc.)
    CUTOUT = 1,      // Alpha-tested blocks (torches, flowers, etc.)
    TRANSLUCENT = 2  // Alpha-blended blocks (water, ice, etc.)
};

// Tile render shapes
enum class TileShape {
    CUBE = 0,
    CROSS = 1,
    TORCH = 2,
    FIRE = 3,
    LIQUID = 4,
    DUST = 5,
    ROW = 6,
    DOOR = 7,
    LADDER = 8,
    RAIL = 9,
    STAIRS = 10,
    FENCE = 11,
    LEVER = 12,
    CACTUS = 13
};

class Tile {
public:
    // Static tile registry (like Java Tile.tiles[])
    static std::unique_ptr<Tile> tiles[256];
    static bool shouldTick[256];

    // Light properties (matching Java Tile static arrays)
    static uint8_t lightBlock[256];      // How much light is blocked (0=transparent, 255=fully opaque)
    static uint8_t lightEmission[256];   // How much light is emitted (0-15)

    // Tile IDs (constants matching original)
    static const int AIR = 0;
    static const int STONE = 1;
    static const int GRASS = 2;
    static const int DIRT = 3;
    static const int COBBLESTONE = 4;
    static const int WOOD = 5;
    static const int SAPLING = 6;
    static const int BEDROCK = 7;
    static const int WATER = 8;
    static const int STILL_WATER = 9;
    static const int LAVA = 10;
    static const int STILL_LAVA = 11;
    static const int SAND = 12;
    static const int GRAVEL = 13;
    static const int GOLD_ORE = 14;
    static const int IRON_ORE = 15;
    static const int COAL_ORE = 16;
    static const int LOG = 17;
    static const int LEAVES = 18;
    static const int SPONGE = 19;
    static const int GLASS = 20;
    static const int CLOTH = 35;
    static const int FLOWER = 37;
    static const int ROSE = 38;
    static const int BROWN_MUSHROOM = 39;
    static const int RED_MUSHROOM = 40;
    static const int GOLD_BLOCK = 41;
    static const int IRON_BLOCK = 42;
    static const int DOUBLE_SLAB = 43;
    static const int SLAB = 44;
    static const int BRICK = 45;
    static const int TNT = 46;
    static const int BOOKSHELF = 47;
    static const int MOSSY_COBBLESTONE = 48;
    static const int OBSIDIAN = 49;
    static const int TORCH = 50;
    static const int FIRE = 51;
    static const int MOB_SPAWNER = 52;
    static const int WOOD_STAIRS = 53;
    static const int CHEST = 54;
    static const int REDSTONE_WIRE = 55;
    static const int DIAMOND_ORE = 56;
    static const int DIAMOND_BLOCK = 57;
    static const int WORKBENCH = 58;
    static const int WHEAT = 59;
    static const int FARMLAND = 60;
    static const int FURNACE = 61;
    static const int BURNING_FURNACE = 62;
    static const int SIGN_POST = 63;
    static const int WOOD_DOOR = 64;
    static const int LADDER = 65;
    static const int RAIL = 66;
    static const int STONE_STAIRS = 67;
    static const int WALL_SIGN = 68;
    static const int LEVER = 69;
    static const int STONE_PRESSURE_PLATE = 70;
    static const int IRON_DOOR = 71;
    static const int WOOD_PRESSURE_PLATE = 72;
    static const int REDSTONE_ORE = 73;
    static const int LIT_REDSTONE_ORE = 74;
    static const int UNLIT_REDSTONE_TORCH = 75;
    static const int REDSTONE_TORCH = 76;
    static const int STONE_BUTTON = 77;
    static const int SNOW_LAYER = 78;
    static const int ICE = 79;
    static const int SNOW = 80;
    static const int CACTUS = 81;
    static const int CLAY = 82;
    static const int SUGAR_CANE = 83;
    static const int JUKEBOX = 84;
    static const int FENCE = 85;
    static const int PUMPKIN = 86;
    static const int NETHERRACK = 87;
    static const int SOUL_SAND = 88;
    static const int GLOWSTONE = 89;
    static const int PORTAL = 90;
    static const int LIT_PUMPKIN = 91;
    static const int CAKE = 92;

    // Properties
    int id;
    int textureIndex;
    TileShape renderShape;
    TileLayer renderLayer;  // Which render pass this block belongs to
    bool blocksLight;
    bool transparent;
    bool solid;
    float hardness;
    float resistance;
    float friction;  // For player movement (default 0.6)
    std::string name;

    // Sound properties
    std::string stepSound;
    float stepSoundVolume;
    float stepSoundPitch;

    Tile(int id, int textureIndex);
    virtual ~Tile() = default;

    // Property setters (for builder pattern)
    Tile& setHardness(float hardness);
    Tile& setResistance(float resistance);
    Tile& setLightOpacity(bool blocks);
    Tile& setTransparent(bool transparent);
    Tile& setShape(TileShape shape);
    Tile& setLayer(TileLayer layer);
    Tile& setStepSound(const std::string& sound, float volume, float pitch);
    Tile& setName(const std::string& name);

    // Texture access (can be face-dependent)
    virtual int getTexture(int face) const;
    virtual int getTexture(int face, int metadata) const;

    // Collision
    virtual AABB getCollisionBox(int x, int y, int z) const;
    virtual AABB getSelectionBox(int x, int y, int z) const;
    virtual AABB getSelectionBox(Level* level, int x, int y, int z) const;  // Metadata-aware version
    virtual bool canCollide() const { return solid; }

    // Rendering
    virtual bool shouldRenderFace(Level* level, int x, int y, int z, int face) const;
    virtual bool isFullCube() const { return renderShape == TileShape::CUBE; }
    virtual float getBrightness(Level* level, int x, int y, int z) const;

    // Behavior
    virtual void tick(Level* level, int x, int y, int z) {}
    virtual void animateTick(Level* level, int x, int y, int z) {}  // Called for visual effects (particles)
    virtual void onPlace(Level* level, int x, int y, int z) {}
    virtual void onRemove(Level* level, int x, int y, int z) {}
    virtual void onNeighborChange(Level* level, int x, int y, int z, int neighborId) {}
    virtual bool canSurvive(Level* level, int x, int y, int z) const { return true; }

    // Placement (for directional blocks like torches)
    virtual bool mayPlace(Level* level, int x, int y, int z) const;
    virtual void setPlacedOnFace(Level* level, int x, int y, int z, int face) {}

    // Block breaking (matching Java Tile)
    virtual float getDestroyProgress(class Player* player) const;
    virtual void attack(Level* level, int x, int y, int z, Player* player) {}
    virtual void destroy(Level* level, int x, int y, int z, int data) {}
    virtual void playerDestroy(Level* level, int x, int y, int z, int data) {}

    // Drops
    virtual int getDropId() const { return id; }
    virtual int getDropCount() const { return 1; }

    // Light
    virtual int getLightEmission() const { return 0; }
    virtual int getLightBlock() const { return blocksLight ? 255 : 0; }

    // Initialize all tiles
    static void initTiles();
    static void destroyTiles();
};

} // namespace mc
