#include "world/tile/Tile.hpp"

namespace mc {

Tile* Tile::tiles[256] = {nullptr};
bool Tile::shouldTick[256] = {false};

Tile::Tile(int id, int textureIndex)
    : id(id)
    , textureIndex(textureIndex)
    , renderShape(TileShape::CUBE)
    , blocksLight(true)
    , transparent(false)
    , solid(true)
    , hardness(1.0f)
    , resistance(1.0f)
    , stepSound("stone")
    , stepSoundVolume(1.0f)
    , stepSoundPitch(1.0f)
{
    if (tiles[id] != nullptr) {
        // Warning: tile already exists at this ID
    }
    tiles[id] = this;
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

bool Tile::shouldRenderFace(Level* /*level*/, int /*x*/, int /*y*/, int /*z*/, int /*face*/) const {
    return true;
}

float Tile::getBrightness(Level* /*level*/, int /*x*/, int /*y*/, int /*z*/) const {
    return 1.0f;
}

void Tile::destroyTiles() {
    for (int i = 0; i < 256; i++) {
        delete tiles[i];
        tiles[i] = nullptr;
    }
}

} // namespace mc
