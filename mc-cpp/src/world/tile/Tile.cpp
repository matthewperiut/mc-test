#include "world/tile/Tile.hpp"
#include "entity/Player.hpp"

namespace mc {

std::unique_ptr<Tile> Tile::tiles[256];
bool Tile::shouldTick[256] = {false};
uint8_t Tile::lightBlock[256] = {0};
uint8_t Tile::lightEmission[256] = {0};

Tile::Tile(int id, int textureIndex)
    : id(id)
    , textureIndex(textureIndex)
    , renderShape(TileShape::CUBE)
    , blocksLight(true)
    , transparent(false)
    , solid(true)
    , hardness(1.0f)
    , resistance(1.0f)
    , friction(0.6f)  // Default friction matching Java
    , stepSound("stone")
    , stepSoundVolume(1.0f)
    , stepSoundPitch(1.0f)
{
    if (tiles[id] != nullptr) {
        // Warning: tile already exists at this ID
    }
    tiles[id].reset(this);
}

Tile& Tile::setHardness(float h) {
    hardness = h;
    if (resistance < h * 5.0f) {
        resistance = h * 5.0f;
    }
    return *this;
}

Tile& Tile::setResistance(float r) {
    resistance = r * 3.0f;
    return *this;
}

Tile& Tile::setLightOpacity(bool blocks) {
    blocksLight = blocks;
    return *this;
}

Tile& Tile::setTransparent(bool t) {
    transparent = t;
    return *this;
}

Tile& Tile::setShape(TileShape shape) {
    renderShape = shape;
    if (shape != TileShape::CUBE) {
        solid = false;
    }
    return *this;
}

Tile& Tile::setStepSound(const std::string& sound, float volume, float pitch) {
    stepSound = sound;
    stepSoundVolume = volume;
    stepSoundPitch = pitch;
    return *this;
}

Tile& Tile::setName(const std::string& n) {
    name = n;
    return *this;
}

int Tile::getTexture(int /*face*/) const {
    return textureIndex;
}

int Tile::getTexture(int face, int /*metadata*/) const {
    return getTexture(face);
}

AABB Tile::getCollisionBox(int x, int y, int z) const {
    return AABB(x, y, z, x + 1, y + 1, z + 1);
}

AABB Tile::getSelectionBox(int x, int y, int z) const {
    return getCollisionBox(x, y, z);
}

AABB Tile::getSelectionBox(Level* /*level*/, int x, int y, int z) const {
    // Default: use the non-metadata-aware version
    return getSelectionBox(x, y, z);
}

bool Tile::shouldRenderFace(Level* /*level*/, int /*x*/, int /*y*/, int /*z*/, int /*face*/) const {
    return true;
}

float Tile::getBrightness(Level* /*level*/, int /*x*/, int /*y*/, int /*z*/) const {
    return 1.0f;
}

bool Tile::mayPlace(Level* /*level*/, int /*x*/, int /*y*/, int /*z*/) const {
    return true;  // Default: can place anywhere
}

float Tile::getDestroyProgress(Player* /*player*/) const {
    // Match Java Tile.getDestroyProgress()
    // Returns how much progress is made per tick when breaking this block
    // For instant-break blocks (hardness < 0), return 1.0
    // Otherwise return 1.0 / (hardness * 30.0) for hand breaking
    // Tools would multiply this by their efficiency

    if (hardness < 0.0f) {
        return 0.0f;  // Unbreakable (like bedrock)
    }

    if (hardness == 0.0f) {
        return 1.0f;  // Instant break
    }

    // Base breaking speed with hand: 1 / (hardness * 30)
    // Java formula: 1.0F / hardness / 100.0F when canHarvest is true
    // Simplified without tool efficiency
    return 1.0f / (hardness * 30.0f);
}

void Tile::destroyTiles() {
    for (int i = 0; i < 256; i++) {
        tiles[i].reset();
    }
}

} // namespace mc
