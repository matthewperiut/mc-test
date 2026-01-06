#pragma once

#include <GL/glew.h>
#include <string>
#include <unordered_map>
#include <memory>

namespace mc {

class Textures {
public:
    static Textures& getInstance();

    void init();
    void destroy();

    // Load a texture from file, returns OpenGL texture ID
    GLuint loadTexture(const std::string& path);

    // Bind texture to a texture unit
    void bind(GLuint textureId, int unit = 0);
    void bind(const std::string& path, int unit = 0);
    bool bindTexture(const std::string& path, int unit = 0);  // Returns false if texture not found
    void unbind(int unit = 0);

    // Get texture dimensions
    int getWidth(GLuint textureId);
    int getHeight(GLuint textureId);

    // Special textures
    GLuint getTerrainTexture() { return loadTexture("resources/terrain.png"); }
    GLuint getItemsTexture() { return loadTexture("resources/gui/items.png"); }
    GLuint getParticlesTexture() { return loadTexture("resources/particles.png"); }

    // Create a solid color texture
    GLuint createSolidColor(uint8_t r, uint8_t g, uint8_t b, uint8_t a = 255);

private:
    Textures() = default;
    ~Textures();
    Textures(const Textures&) = delete;
    Textures& operator=(const Textures&) = delete;

    struct TextureInfo {
        int width;
        int height;
    };

    std::unordered_map<std::string, GLuint> textureCache;
    std::unordered_map<GLuint, TextureInfo> textureInfo;

    GLuint loadTextureFromFile(const std::string& path);
};

} // namespace mc
