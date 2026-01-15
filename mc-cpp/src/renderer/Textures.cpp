#include "renderer/Textures.hpp"
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
    for (auto& pair : textureCache) {
        if (pair.second) {
            glDeleteTextures(1, &pair.second);
        }
    }
    textureCache.clear();
    textureInfo.clear();
}

GLuint Textures::loadTexture(const std::string& path) {
    // Check cache first
    auto it = textureCache.find(path);
    if (it != textureCache.end()) {
        return it->second;
    }

    // Load new texture
    GLuint textureId = loadTextureFromFile(path);
    if (textureId) {
        textureCache[path] = textureId;
    }
    return textureId;
}

GLuint Textures::loadTextureFromFile(const std::string& path) {
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
    // No mipmaps - they cause black/transparent edge artifacts with alpha textures
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);

    glBindTexture(GL_TEXTURE_2D, 0);
    stbi_image_free(data);

    textureInfo[textureId] = {width, height};

    return textureId;
}

void Textures::bind(GLuint textureId, int unit) {
    glActiveTexture(GL_TEXTURE0 + unit);
    glBindTexture(GL_TEXTURE_2D, textureId);
}

void Textures::bind(const std::string& path, int unit) {
    GLuint textureId = loadTexture(path);
    bind(textureId, unit);
}

bool Textures::bindTexture(const std::string& path, int unit) {
    GLuint textureId = loadTexture(path);
    if (textureId == 0) {
        return false;
    }
    bind(textureId, unit);
    return true;
}

void Textures::unbind(int unit) {
    glActiveTexture(GL_TEXTURE0 + unit);
    glBindTexture(GL_TEXTURE_2D, 0);
}

int Textures::getWidth(GLuint textureId) {
    auto it = textureInfo.find(textureId);
    return it != textureInfo.end() ? it->second.width : 0;
}

int Textures::getHeight(GLuint textureId) {
    auto it = textureInfo.find(textureId);
    return it != textureInfo.end() ? it->second.height : 0;
}

GLuint Textures::createSolidColor(uint8_t r, uint8_t g, uint8_t b, uint8_t a) {
    uint8_t data[4] = {r, g, b, a};

    GLuint textureId;
    glGenTextures(1, &textureId);
    glBindTexture(GL_TEXTURE_2D, textureId);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);

    glBindTexture(GL_TEXTURE_2D, 0);

    textureInfo[textureId] = {1, 1};

    return textureId;
}

} // namespace mc
