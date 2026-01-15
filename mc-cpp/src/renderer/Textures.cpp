#include "renderer/Textures.hpp"
#include <iostream>

#ifdef MC_RENDERER_METAL
#include "renderer/backend/RenderDevice.hpp"
#else
#include <GL/glew.h>
#endif

// stb_image implementation
#define STB_IMAGE_IMPLEMENTATION
#define STBI_ONLY_PNG
#define STBI_ONLY_JPEG
#include "stb_image.h"

namespace mc {

Textures& Textures::getInstance() {
    static Textures instance;
    return instance;
}

Textures::~Textures() {
    destroy();
}

void Textures::init() {
    // Pre-load common textures if needed
}

void Textures::destroy() {
#ifdef MC_RENDERER_METAL
    textureCache.clear();
#else
    for (auto& pair : textureCache) {
        if (pair.second) {
            glDeleteTextures(1, &pair.second);
        }
    }
    textureCache.clear();
    textureInfo.clear();
#endif
}

TextureHandle Textures::loadTexture(const std::string& path, bool useMipmaps) {
    // Include mipmap setting in cache key to allow same texture with different settings
    std::string cacheKey = path + (useMipmaps ? ":mip" : ":nomip");

    // Check cache first
    auto it = textureCache.find(cacheKey);
    if (it != textureCache.end()) {
#ifdef MC_RENDERER_METAL
        return it->second.get();
#else
        return it->second;
#endif
    }

    // Load new texture
    TextureHandle textureHandle = loadTextureFromFile(path, useMipmaps);
#ifdef MC_RENDERER_METAL
    if (textureHandle) {
        // For Metal, the texture was already added to the cache in loadTextureFromFile
    }
#else
    if (textureHandle) {
        textureCache[cacheKey] = textureHandle;
    }
#endif
    return textureHandle;
}

#ifdef MC_RENDERER_METAL
Texture* Textures::loadTextureFromFile(const std::string& path, bool useMipmaps) {
    int width, height, channels;
    stbi_set_flip_vertically_on_load(false);  // Minecraft textures are top-down

    unsigned char* data = stbi_load(path.c_str(), &width, &height, &channels, STBI_rgb_alpha);
    if (!data) {
        std::cerr << "Failed to load texture: " << path << std::endl;
        return nullptr;
    }

    auto& device = RenderDevice::get();
    auto texture = device.createTexture();

    texture->setFilter(useMipmaps ? TextureFilter::NearestMipmapLinear : TextureFilter::Nearest,
                       TextureFilter::Nearest);
    texture->setWrap(TextureWrap::Repeat, TextureWrap::Repeat);
    texture->upload(width, height, data, useMipmaps);

    stbi_image_free(data);

    std::string cacheKey = path + (useMipmaps ? ":mip" : ":nomip");
    Texture* rawPtr = texture.get();
    textureCache[cacheKey] = std::move(texture);

    return rawPtr;
}
#else
GLuint Textures::loadTextureFromFile(const std::string& path, bool useMipmaps) {
    int width, height, channels;
    stbi_set_flip_vertically_on_load(false);  // Minecraft textures are top-down

    unsigned char* data = stbi_load(path.c_str(), &width, &height, &channels, STBI_rgb_alpha);
    if (!data) {
        std::cerr << "Failed to load texture: " << path << std::endl;
        return 0;
    }

    GLuint textureId;
    glGenTextures(1, &textureId);
    glBindTexture(GL_TEXTURE_2D, textureId);

    // Minecraft uses nearest neighbor filtering for that pixelated look
    if (useMipmaps) {
        // Use mipmaps for terrain - helps with distant block rendering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
    } else {
        // No mipmaps for mob/entity textures - avoids black/transparent edge artifacts
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    }
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);

    if (useMipmaps) {
        glGenerateMipmap(GL_TEXTURE_2D);
    }

    glBindTexture(GL_TEXTURE_2D, 0);
    stbi_image_free(data);

    textureInfo[textureId] = {width, height};

    return textureId;
}
#endif

void Textures::bind(TextureHandle textureHandle, int unit) {
#ifdef MC_RENDERER_METAL
    if (textureHandle) {
        textureHandle->bind(unit);
    }
#else
    glActiveTexture(GL_TEXTURE0 + unit);
    glBindTexture(GL_TEXTURE_2D, textureHandle);
#endif
}

void Textures::bind(const std::string& path, int unit, bool useMipmaps) {
    TextureHandle textureHandle = loadTexture(path, useMipmaps);
    bind(textureHandle, unit);
}

bool Textures::bindTexture(const std::string& path, int unit) {
    TextureHandle textureHandle = loadTexture(path);
#ifdef MC_RENDERER_METAL
    if (textureHandle == nullptr) {
        return false;
    }
#else
    if (textureHandle == 0) {
        return false;
    }
#endif
    bind(textureHandle, unit);
    return true;
}

void Textures::unbind(int unit) {
#ifdef MC_RENDERER_METAL
    // Metal doesn't require explicit unbinding
#else
    glActiveTexture(GL_TEXTURE0 + unit);
    glBindTexture(GL_TEXTURE_2D, 0);
#endif
}

int Textures::getWidth(TextureHandle textureHandle) {
#ifdef MC_RENDERER_METAL
    return textureHandle ? textureHandle->getWidth() : 0;
#else
    auto it = textureInfo.find(textureHandle);
    return it != textureInfo.end() ? it->second.width : 0;
#endif
}

int Textures::getHeight(TextureHandle textureHandle) {
#ifdef MC_RENDERER_METAL
    return textureHandle ? textureHandle->getHeight() : 0;
#else
    auto it = textureInfo.find(textureHandle);
    return it != textureInfo.end() ? it->second.height : 0;
#endif
}

TextureHandle Textures::createSolidColor(uint8_t r, uint8_t g, uint8_t b, uint8_t a) {
    uint8_t data[4] = {r, g, b, a};

#ifdef MC_RENDERER_METAL
    auto& device = RenderDevice::get();
    auto texture = device.createTexture();

    texture->setFilter(TextureFilter::Nearest, TextureFilter::Nearest);
    texture->setWrap(TextureWrap::Repeat, TextureWrap::Repeat);
    texture->upload(1, 1, data, false);

    static int solidColorCounter = 0;
    std::string cacheKey = "__solid_color_" + std::to_string(solidColorCounter++);
    Texture* rawPtr = texture.get();
    textureCache[cacheKey] = std::move(texture);

    return rawPtr;
#else
    GLuint textureId;
    glGenTextures(1, &textureId);
    glBindTexture(GL_TEXTURE_2D, textureId);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);

    glBindTexture(GL_TEXTURE_2D, 0);

    textureInfo[textureId] = {1, 1};

    return textureId;
#endif
}

} // namespace mc
