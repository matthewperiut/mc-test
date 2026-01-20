#include "world/tile/Tile.hpp"
#include "world/Level.hpp"

namespace mc {

// Specialized tile classes
class GrassTile : public Tile {
public:
    GrassTile() : Tile(GRASS, 3) {
        setHardness(0.6f);
        setStepSound("grass", 1.0f, 1.0f);
        setName("Grass");
    }

    int getTexture(int face) const override {
        if (face == 1) return 0;   // Top: grass top
        if (face == 0) return 2;   // Bottom: dirt
        return 3;                   // Sides: grass side
    }
};

class DirtTile : public Tile {
public:
    DirtTile() : Tile(DIRT, 2) {
        setHardness(0.5f);
        setStepSound("gravel", 1.0f, 1.0f);
        setName("Dirt");
    }
};

class StoneTile : public Tile {
public:
    StoneTile() : Tile(STONE, 1) {
        setHardness(1.5f);
        setStepSound("stone", 1.0f, 1.0f);
        setName("Stone");
    }
};

class LogTile : public Tile {
public:
    LogTile() : Tile(LOG, 20) {
        setHardness(2.0f);
        setStepSound("wood", 1.0f, 1.0f);
        setName("Oak Log");
    }

    int getTexture(int face) const override {
        if (face == 0 || face == 1) return 21;  // Top/bottom: log top
        return 20;  // Sides: log bark
    }
};

class LeavesTile : public Tile {
public:
    LeavesTile() : Tile(LEAVES, 52) {
        setHardness(0.2f);
        setTransparent(true);
        setLightOpacity(false);
        setStepSound("grass", 1.0f, 1.0f);
        setName("Oak Leaves");
    }
};

class WoodTile : public Tile {
public:
    WoodTile() : Tile(WOOD, 4) {
        setHardness(2.0f);
        setStepSound("wood", 1.0f, 1.0f);
        setName("Oak Planks");
    }
};

class CobblestoneTile : public Tile {
public:
    CobblestoneTile() : Tile(COBBLESTONE, 16) {
        setHardness(2.0f);
        setStepSound("stone", 1.0f, 1.0f);
        setName("Cobblestone");
    }
};

class SandTile : public Tile {
public:
    SandTile() : Tile(SAND, 18) {
        setHardness(0.5f);
        setStepSound("sand", 1.0f, 1.0f);
        setName("Sand");
    }
};

class GravelTile : public Tile {
public:
    GravelTile() : Tile(GRAVEL, 19) {
        setHardness(0.6f);
        setStepSound("gravel", 1.0f, 1.0f);
        setName("Gravel");
    }
};

class GlassTile : public Tile {
public:
    GlassTile() : Tile(GLASS, 49) {
        setHardness(0.3f);
        setTransparent(true);
        setLightOpacity(false);
        setStepSound("stone", 1.0f, 1.0f);
        setName("Glass");
    }

    int getDropId() const override { return 0; }  // Drops nothing
};

class WaterTile : public Tile {
public:
    WaterTile(int id) : Tile(id, 205) {
        setHardness(100.0f);
        setTransparent(true);
        setLightOpacity(false);
        setShape(TileShape::LIQUID);
        solid = false;
        setName("Water");
    }
};

class LavaTile : public Tile {
public:
    LavaTile(int id) : Tile(id, 237) {
        setHardness(100.0f);
        setShape(TileShape::LIQUID);
        solid = false;
        setName("Lava");
    }

    int getLightEmission() const override { return 15; }
};

class TorchTile : public Tile {
public:
    // Torch metadata:
    // 0 = invalid/default (will be corrected on place)
    // 1 = attached to wall at x-1 (west wall, torch points east)
    // 2 = attached to wall at x+1 (east wall, torch points west)
    // 3 = attached to wall at z-1 (north wall, torch points south)
    // 4 = attached to wall at z+1 (south wall, torch points north)
    // 5 = attached to floor (y-1)

    TorchTile() : Tile(TORCH, 80) {
        setHardness(0.0f);
        setShape(TileShape::TORCH);
        setLightOpacity(false);
        setTransparent(true);
        solid = false;
        setStepSound("wood", 1.0f, 1.0f);
        setName("Torch");
    }

    int getLightEmission() const override { return 14; }

    // Check if torch can be placed (needs adjacent solid block)
    bool mayPlace(Level* level, int x, int y, int z) const override {
        if (level->isSolid(x - 1, y, z)) return true;  // West wall
        if (level->isSolid(x + 1, y, z)) return true;  // East wall
        if (level->isSolid(x, y, z - 1)) return true;  // North wall
        if (level->isSolid(x, y, z + 1)) return true;  // South wall
        if (level->isSolid(x, y - 1, z)) return true;  // Floor
        return false;
    }

    // Set metadata when placed (auto-detect support)
    void onPlace(Level* level, int x, int y, int z) override {
        // Priority order matching Java: west, east, north, south, floor
        if (level->isSolid(x - 1, y, z)) {
            level->setData(x, y, z, 1);  // West wall
        } else if (level->isSolid(x + 1, y, z)) {
            level->setData(x, y, z, 2);  // East wall
        } else if (level->isSolid(x, y, z - 1)) {
            level->setData(x, y, z, 3);  // North wall
        } else if (level->isSolid(x, y, z + 1)) {
            level->setData(x, y, z, 4);  // South wall
        } else if (level->isSolid(x, y - 1, z)) {
            level->setData(x, y, z, 5);  // Floor
        }
    }

    // Set metadata based on which face player clicked (matching Java TorchTile.setPlacedOnFace)
    void setPlacedOnFace(Level* level, int x, int y, int z, int face) override {
        int data = level->getData(x, y, z);

        // face values: 0=DOWN, 1=UP, 2=NORTH(-Z), 3=SOUTH(+Z), 4=WEST(-X), 5=EAST(+X)
        if (face == 1 && level->isSolid(x, y - 1, z)) {
            data = 5;  // Clicked top face = floor torch
        }
        if (face == 2 && level->isSolid(x, y, z + 1)) {
            data = 4;  // Clicked north face = attach to south wall
        }
        if (face == 3 && level->isSolid(x, y, z - 1)) {
            data = 3;  // Clicked south face = attach to north wall
        }
        if (face == 4 && level->isSolid(x + 1, y, z)) {
            data = 2;  // Clicked west face = attach to east wall
        }
        if (face == 5 && level->isSolid(x - 1, y, z)) {
            data = 1;  // Clicked east face = attach to west wall
        }

        level->setData(x, y, z, data);
    }

    // Check if support block was removed
    void onNeighborChange(Level* level, int x, int y, int z, int /*neighborId*/) override {
        int data = level->getData(x, y, z);
        bool shouldDrop = false;

        if (data == 1 && !level->isSolid(x - 1, y, z)) shouldDrop = true;  // West wall removed
        if (data == 2 && !level->isSolid(x + 1, y, z)) shouldDrop = true;  // East wall removed
        if (data == 3 && !level->isSolid(x, y, z - 1)) shouldDrop = true;  // North wall removed
        if (data == 4 && !level->isSolid(x, y, z + 1)) shouldDrop = true;  // South wall removed
        if (data == 5 && !level->isSolid(x, y - 1, z)) shouldDrop = true;  // Floor removed

        if (shouldDrop) {
            // Remove torch (drops as item in full game)
            level->setTile(x, y, z, 0);
        }
    }

    // Torch has no collision
    AABB getCollisionBox(int /*x*/, int /*y*/, int /*z*/) const override {
        return AABB();
    }

    // Default selection box (used when no level context available)
    AABB getSelectionBox(int x, int y, int z) const override {
        // Floor torch box (metadata 5 or default)
        float w = 0.1f;
        return AABB(x + 0.5f - w, y, z + 0.5f - w,
                    x + 0.5f + w, y + 0.6f, z + 0.5f + w);
    }

    // Metadata-aware selection box (matching Java TorchTile.clip)
    AABB getSelectionBox(Level* level, int x, int y, int z) const override {
        int data = level->getData(x, y, z) & 7;
        float w = 0.15f;

        if (data == 1) {
            // West wall - extends towards east
            return AABB(x, y + 0.2f, z + 0.5f - w,
                        x + w * 2.0f, y + 0.8f, z + 0.5f + w);
        } else if (data == 2) {
            // East wall - extends towards west
            return AABB(x + 1.0f - w * 2.0f, y + 0.2f, z + 0.5f - w,
                        x + 1.0f, y + 0.8f, z + 0.5f + w);
        } else if (data == 3) {
            // North wall - extends towards south
            return AABB(x + 0.5f - w, y + 0.2f, z,
                        x + 0.5f + w, y + 0.8f, z + w * 2.0f);
        } else if (data == 4) {
            // South wall - extends towards north
            return AABB(x + 0.5f - w, y + 0.2f, z + 1.0f - w * 2.0f,
                        x + 0.5f + w, y + 0.8f, z + 1.0f);
        } else {
            // Floor (data == 5 or default)
            w = 0.1f;
            return AABB(x + 0.5f - w, y, z + 0.5f - w,
                        x + 0.5f + w, y + 0.6f, z + 0.5f + w);
        }
    }
};

class BedrockTile : public Tile {
public:
    BedrockTile() : Tile(BEDROCK, 17) {
        setHardness(-1.0f);  // Unbreakable
        setResistance(6000000.0f);
        setStepSound("stone", 1.0f, 1.0f);
        setName("Bedrock");
    }
};

class GoldOreTile : public Tile {
public:
    GoldOreTile() : Tile(GOLD_ORE, 32) {
        setHardness(3.0f);
        setStepSound("stone", 1.0f, 1.0f);
        setName("Gold Ore");
    }
};

class IronOreTile : public Tile {
public:
    IronOreTile() : Tile(IRON_ORE, 33) {
        setHardness(3.0f);
        setStepSound("stone", 1.0f, 1.0f);
        setName("Iron Ore");
    }
};

class CoalOreTile : public Tile {
public:
    CoalOreTile() : Tile(COAL_ORE, 34) {
        setHardness(3.0f);
        setStepSound("stone", 1.0f, 1.0f);
        setName("Coal Ore");
    }
};

class DiamondOreTile : public Tile {
public:
    DiamondOreTile() : Tile(DIAMOND_ORE, 50) {
        setHardness(3.0f);
        setStepSound("stone", 1.0f, 1.0f);
        setName("Diamond Ore");
    }
};

class BrickTile : public Tile {
public:
    BrickTile() : Tile(BRICK, 7) {
        setHardness(2.0f);
        setResistance(6.0f);
        setStepSound("stone", 1.0f, 1.0f);
        setName("Bricks");
    }
};

class TNTTile : public Tile {
public:
    TNTTile() : Tile(TNT, 8) {
        setHardness(0.0f);
        setStepSound("grass", 1.0f, 1.0f);
        setName("TNT");
    }

    int getTexture(int face) const override {
        if (face == 0) return 10;  // Bottom
        if (face == 1) return 9;   // Top
        return 8;                   // Sides
    }
};

class BookshelfTile : public Tile {
public:
    BookshelfTile() : Tile(BOOKSHELF, 35) {
        setHardness(1.5f);
        setStepSound("wood", 1.0f, 1.0f);
        setName("Bookshelf");
    }

    int getTexture(int face) const override {
        if (face == 0 || face == 1) return 4;  // Top/bottom: planks
        return 35;  // Sides: books
    }
};

class ObsidianTile : public Tile {
public:
    ObsidianTile() : Tile(OBSIDIAN, 37) {
        setHardness(50.0f);
        setResistance(2000.0f);
        setStepSound("stone", 1.0f, 1.0f);
        setName("Obsidian");
    }
};

class CactusTile : public Tile {
public:
    CactusTile() : Tile(CACTUS, 70) {
        setHardness(0.4f);
        setShape(TileShape::CACTUS);
        setStepSound("cloth", 1.0f, 1.0f);
        setName("Cactus");
    }

    int getTexture(int face) const override {
        if (face == 1) return 69;  // Top
        if (face == 0) return 71;  // Bottom
        return 70;                  // Sides
    }
};

class ClayTile : public Tile {
public:
    ClayTile() : Tile(CLAY, 72) {
        setHardness(0.6f);
        setStepSound("gravel", 1.0f, 1.0f);
        setName("Clay");
    }
};

class GlowstoneTile : public Tile {
public:
    GlowstoneTile() : Tile(GLOWSTONE, 105) {
        setHardness(0.3f);
        setStepSound("stone", 1.0f, 1.0f);
        setName("Glowstone");
    }

    int getLightEmission() const override { return 15; }
};

class SaplingTile : public Tile {
public:
    SaplingTile() : Tile(SAPLING, 15) {
        setHardness(0.0f);
        setShape(TileShape::CROSS);
        setTransparent(true);
        setLightOpacity(false);
        solid = false;
        setStepSound("grass", 1.0f, 1.0f);
        setName("Oak Sapling");
    }

    AABB getCollisionBox(int /*x*/, int /*y*/, int /*z*/) const override {
        return AABB();
    }
};

class FlowerTile : public Tile {
public:
    FlowerTile() : Tile(FLOWER, 13) {
        setHardness(0.0f);
        setShape(TileShape::CROSS);
        setTransparent(true);
        setLightOpacity(false);
        solid = false;
        setStepSound("grass", 1.0f, 1.0f);
        setName("Dandelion");
    }

    AABB getCollisionBox(int /*x*/, int /*y*/, int /*z*/) const override {
        return AABB();
    }
};

class RoseTile : public Tile {
public:
    RoseTile() : Tile(ROSE, 12) {
        setHardness(0.0f);
        setShape(TileShape::CROSS);
        setTransparent(true);
        setLightOpacity(false);
        solid = false;
        setStepSound("grass", 1.0f, 1.0f);
        setName("Rose");
    }

    AABB getCollisionBox(int /*x*/, int /*y*/, int /*z*/) const override {
        return AABB();
    }
};

class SnowTile : public Tile {
public:
    SnowTile() : Tile(SNOW, 66) {
        setHardness(0.2f);
        setStepSound("cloth", 1.0f, 1.0f);
        setName("Snow");
    }
};

class IceTile : public Tile {
public:
    IceTile() : Tile(ICE, 67) {
        setHardness(0.5f);
        setTransparent(true);
        setLightOpacity(false);
        setName("Ice");
    }
};

class NetherrackTile : public Tile {
public:
    NetherrackTile() : Tile(NETHERRACK, 103) {
        setHardness(0.4f);
        setName("Netherrack");
    }
};

class SoulSandTile : public Tile {
public:
    SoulSandTile() : Tile(SOUL_SAND, 104) {
        setHardness(0.5f);
        setStepSound("sand", 1.0f, 1.0f);
        setName("Soul Sand");
    }

    AABB getCollisionBox(int x, int y, int z) const override {
        // Slightly shorter to cause slowdown effect
        return AABB(x, y, z, x + 1, y + 0.875, z + 1);
    }
};

// Tiles initialization function
void initAllTiles() {
    // Basic blocks
    new StoneTile();
    new GrassTile();
    new DirtTile();
    new CobblestoneTile();
    new WoodTile();
    new SaplingTile();
    new BedrockTile();

    // Liquids
    new WaterTile(Tile::WATER);
    new WaterTile(Tile::STILL_WATER);
    new LavaTile(Tile::LAVA);
    new LavaTile(Tile::STILL_LAVA);

    // Natural blocks
    new SandTile();
    new GravelTile();
    new GoldOreTile();
    new IronOreTile();
    new CoalOreTile();
    new LogTile();
    new LeavesTile();

    // Glass
    new GlassTile();

    // Flowers
    new FlowerTile();
    new RoseTile();

    // Building blocks
    new BrickTile();
    new TNTTile();
    new BookshelfTile();
    new ObsidianTile();

    // Lighting
    new TorchTile();
    new GlowstoneTile();

    // Ores
    new DiamondOreTile();

    // Desert/cold
    new CactusTile();
    new ClayTile();
    new SnowTile();
    new IceTile();

    // Nether
    new NetherrackTile();
    new SoulSandTile();

    // Initialize tick flags for tiles that update
    Tile::shouldTick[Tile::WATER] = true;
    Tile::shouldTick[Tile::STILL_WATER] = true;
    Tile::shouldTick[Tile::LAVA] = true;
    Tile::shouldTick[Tile::STILL_LAVA] = true;
    Tile::shouldTick[Tile::SAPLING] = true;
    Tile::shouldTick[Tile::LEAVES] = true;
    Tile::shouldTick[Tile::FIRE] = true;
    Tile::shouldTick[Tile::SAND] = true;
    Tile::shouldTick[Tile::GRAVEL] = true;
}

// Initialize light property arrays based on tile properties
void initLightArrays() {
    // Default: all blocks fully opaque, no emission
    for (int i = 0; i < 256; i++) {
        Tile::lightBlock[i] = 255;  // Fully opaque by default
        Tile::lightEmission[i] = 0;
    }

    // Air is fully transparent
    Tile::lightBlock[Tile::AIR] = 0;

    // Set properties for each tile based on its properties
    for (int i = 0; i < 256; i++) {
        Tile* tile = Tile::tiles[i].get();
        if (tile) {
            // Get light emission from tile
            Tile::lightEmission[i] = static_cast<uint8_t>(tile->getLightEmission());

            // Get light blocking from tile
            Tile::lightBlock[i] = static_cast<uint8_t>(tile->getLightBlock());
        }
    }

    // Special cases for partial transparency (matching Java values)
    // Water: reduces light by 3 per block
    Tile::lightBlock[Tile::WATER] = 3;
    Tile::lightBlock[Tile::STILL_WATER] = 3;

    // Leaves: reduce light by 1
    Tile::lightBlock[Tile::LEAVES] = 1;

    // Ice: partially transparent
    Tile::lightBlock[Tile::ICE] = 3;
}

void Tile::initTiles() {
    initAllTiles();
    initLightArrays();
}

} // namespace mc
