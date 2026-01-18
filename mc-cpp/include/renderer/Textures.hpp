#pragma once

#include <string>
#include <unordered_map>
#include <memory>

#include "renderer/backend/Texture.hpp"

namespace mc {

using TextureHandle = Texture*;

class Textures {
public:
    static Textures& getInstance();

    void init();
    void destroy();

    // Load a texture from file
    // useMipmaps: true for terrain textures, false for mob/entity textures (avoids edge artifacts)
    // clamp: true for shadow textures, false for tiling textures
    TextureHandle loadTexture(const std::string& path, bool useMipmaps = true, bool clamp = false);

    // Bind texture to a texture unit
    void bind(TextureHandle textureId, int unit = 0);
    void bind(const std::string& path, int unit = 0, bool useMipmaps = true, bool clamp = false);
    bool bindTexture(const std::string& path, int unit = 0);  // Returns false if texture not found
    void unbind(int unit = 0);

    // Get texture dimensions
    int getWidth(TextureHandle textureId);
    int getHeight(TextureHandle textureId);

    // Special textures
    TextureHandle getTerrainTexture() { return loadTexture("resources/terrain.png"); }
    TextureHandle getItemsTexture() { return loadTexture("resources/gui/items.png"); }
    TextureHandle getParticlesTexture() { return loadTexture("resources/particles.png"); }

    // Create a solid color texture
    TextureHandle createSolidColor(uint8_t r, uint8_t g, uint8_t b, uint8_t a = 255);

private:
    Textures() = default;
    ~Textures();
    Textures(const Textures&) = delete;
    Textures& operator=(const Textures&) = delete;

    std::unordered_map<std::string, std::unique_ptr<Texture>> textureCache;

    Texture* loadTextureFromFile(const std::string& path, bool useMipmaps, bool clamp);
};

} // namespace mc
