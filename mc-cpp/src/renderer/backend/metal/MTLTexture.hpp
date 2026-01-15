#pragma once

#include "renderer/backend/Texture.hpp"

// Forward declarations for metal-cpp types
namespace MTL {
    class Device;
    class Texture;
    class SamplerState;
}

namespace mc {

class MTLTexture : public Texture {
public:
    explicit MTLTexture(MTL::Device* device);
    ~MTLTexture() override;

    void create() override;
    void destroy() override;
    void upload(int width, int height, const uint8_t* rgba, bool generateMipmaps) override;
    void setFilter(TextureFilter min, TextureFilter mag) override;
    void setWrap(TextureWrap s, TextureWrap t) override;
    void bind(int unit) override;
    void unbind(int unit) override;
    bool isValid() const override { return texture != nullptr; }

    MTL::Texture* getTexture() const { return texture; }
    MTL::SamplerState* getSampler() const { return sampler; }

private:
    void createSampler();

    MTL::Device* device;
    MTL::Texture* texture;
    MTL::SamplerState* sampler;

    TextureFilter minFilter;
    TextureFilter magFilter;
    TextureWrap wrapS;
    TextureWrap wrapT;
};

} // namespace mc
