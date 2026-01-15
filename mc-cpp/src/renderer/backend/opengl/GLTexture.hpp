#pragma once

#include "renderer/backend/Texture.hpp"
#include <GL/glew.h>

namespace mc {

class GLTexture : public Texture {
public:
    GLTexture();
    ~GLTexture() override;

    void create() override;
    void destroy() override;
    void upload(int width, int height, const uint8_t* rgba, bool generateMipmaps) override;
    void setFilter(TextureFilter min, TextureFilter mag) override;
    void setWrap(TextureWrap s, TextureWrap t) override;
    void bind(int unit) override;
    void unbind(int unit) override;
    bool isValid() const override { return textureId != 0; }

    GLuint getTextureId() const { return textureId; }

private:
    static GLenum toGLFilter(TextureFilter filter);
    static GLenum toGLWrap(TextureWrap wrap);

    GLuint textureId;
};

} // namespace mc
