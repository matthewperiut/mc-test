#pragma once

#include "RenderTypes.hpp"
#include <cstdint>
#include <memory>

namespace mc {

class Texture {
public:
    virtual ~Texture() = default;

    virtual void create() = 0;
    virtual void destroy() = 0;

    // Upload RGBA pixel data
    virtual void upload(int width, int height, const uint8_t* rgba, bool generateMipmaps) = 0;

    // Sampling parameters
    virtual void setFilter(TextureFilter min, TextureFilter mag) = 0;
    virtual void setWrap(TextureWrap s, TextureWrap t) = 0;

    // Binding
    virtual void bind(int unit) = 0;
    virtual void unbind(int unit) = 0;

    int getWidth() const { return width; }
    int getHeight() const { return height; }
    virtual bool isValid() const = 0;

protected:
    int width = 0;
    int height = 0;
};

} // namespace mc
