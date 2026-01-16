#include "renderer/Textures.hpp"
#include "renderer/backend/RenderDevice.hpp"
#include <iostream>

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
    textureCache.clear();
}

TextureHandle Textures::loadTexture(const std::string& path, bool useMipmaps) {
    // Include mipmap setting in cache key to allow same texture with different settings
    std::string cacheKey = path + (useMipmaps ? ":mip" : ":nomip");

    // Check cache first
    auto it = textureCache.find(cacheKey);
    if (it != textureCache.end()) {
        return it->second.get();
    }

    // Load new texture
    return loadTextureFromFile(path, useMipmaps);
}

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

void Textures::bind(TextureHandle textureHandle, int unit) {
    if (textureHandle) {
        textureHandle->bind(unit);
    }
}

void Textures::bind(const std::string& path, int unit, bool useMipmaps) {
    TextureHandle textureHandle = loadTexture(path, useMipmaps);
    bind(textureHandle, unit);
}

bool Textures::bindTexture(const std::string& path, int unit) {
    TextureHandle textureHandle = loadTexture(path);
    if (textureHandle == nullptr) {
        return false;
    }
    bind(textureHandle, unit);
    return true;
}

void Textures::unbind(int unit) {
    // Create a temporary texture just to call unbind - this is a bit wasteful
    // but maintains the abstraction. Most code doesn't call unbind frequently.
    auto& device = RenderDevice::get();
    auto tempTexture = device.createTexture();
    tempTexture->unbind(unit);
}

int Textures::getWidth(TextureHandle textureHandle) {
    return textureHandle ? textureHandle->getWidth() : 0;
}

int Textures::getHeight(TextureHandle textureHandle) {
    return textureHandle ? textureHandle->getHeight() : 0;
}

TextureHandle Textures::createSolidColor(uint8_t r, uint8_t g, uint8_t b, uint8_t a) {
    uint8_t data[4] = {r, g, b, a};

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
}

} // namespace mc
