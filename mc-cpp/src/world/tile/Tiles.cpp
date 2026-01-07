#include "world/tile/Tile.hpp"

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

    AABB getCollisionBox(int /*x*/, int /*y*/, int /*z*/) const override {
        return AABB();  // No collision
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

void Tile::initTiles() {
    initAllTiles();
}

} // namespace mc
