#pragma once

#include <string>
#include <unordered_map>
#include <memory>

#ifdef MC_RENDERER_METAL
#include "renderer/backend/Texture.hpp"
#else
#include <GL/glew.h>
#endif

namespace mc {

#ifdef MC_RENDERER_METAL
class Texture;
using TextureHandle = Texture*;
#else
using TextureHandle = GLuint;
#endif

class Textures {
public:
    static Textures& getInstance();

    void init();
    void destroy();

    // Load a texture from file
    // useMipmaps: true for terrain textures, false for mob/entity textures (avoids edge artifacts)
    TextureHandle loadTexture(const std::string& path, bool useMipmaps = true);

    // Bind texture to a texture unit
    void bind(TextureHandle textureId, int unit = 0);
    void bind(const std::string& path, int unit = 0, bool useMipmaps = true);
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

#ifdef MC_RENDERER_METAL
    std::unordered_map<std::string, std::unique_ptr<Texture>> textureCache;

    Texture* loadTextureFromFile(const std::string& path, bool useMipmaps);
#else
    struct TextureInfo {
        int width;
        int height;
    };

    std::unordered_map<std::string, GLuint> textureCache;
    std::unordered_map<GLuint, TextureInfo> textureInfo;

    GLuint loadTextureFromFile(const std::string& path, bool useMipmaps);
#endif
};

} // namespace mc
