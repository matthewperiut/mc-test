#include "MTLTexture.hpp"
#include "MTLRenderDevice.hpp"
#include <Metal/Metal.hpp>

namespace mc {

MTLTexture::MTLTexture(MTL::Device* device)
    : device(device)
    , texture(nullptr)
    , sampler(nullptr)
    , minFilter(TextureFilter::Nearest)
    , magFilter(TextureFilter::Nearest)
    , wrapS(TextureWrap::Repeat)
    , wrapT(TextureWrap::Repeat)
{
}

MTLTexture::~MTLTexture() {
    destroy();
}

void MTLTexture::create() {
    // Texture is created on first upload
}

void MTLTexture::destroy() {
    if (sampler) {
        sampler->release();
        sampler = nullptr;
    }
    if (texture) {
        texture->release();
        texture = nullptr;
    }
    width = 0;
    height = 0;
}

void MTLTexture::upload(int w, int h, const uint8_t* rgba, bool generateMipmaps) {
    // Only create new texture if dimensions changed or texture doesn't exist
    bool needsNewTexture = !texture || (width != w) || (height != h);

    if (needsNewTexture) {
        if (texture) {
            texture->release();
            texture = nullptr;
        }

        width = w;
        height = h;

        // Create texture descriptor
        MTL::TextureDescriptor* desc = MTL::TextureDescriptor::alloc()->init();
        desc->setPixelFormat(MTL::PixelFormatRGBA8Unorm);
        desc->setWidth(width);
        desc->setHeight(height);
        desc->setTextureType(MTL::TextureType2D);
        desc->setStorageMode(MTL::StorageModeShared);

        if (generateMipmaps) {
            // Need ShaderWrite usage for mipmap generation
            desc->setUsage(MTL::TextureUsageShaderRead | MTL::TextureUsageShaderWrite);
            // Calculate mipmap levels
            int mipLevels = 1;
            int size = std::max(width, height);
            while (size > 1) {
                size /= 2;
                mipLevels++;
            }
            desc->setMipmapLevelCount(mipLevels);
        } else {
            desc->setUsage(MTL::TextureUsageShaderRead);
        }

        texture = device->newTexture(desc);
        desc->release();
    }

    if (!texture) {
        return;
    }

    // Upload pixel data
    MTL::Region region = MTL::Region::Make2D(0, 0, width, height);
    texture->replaceRegion(region, 0, rgba, width * 4);

    // Generate mipmaps if requested using a blit command encoder
    if (generateMipmaps && texture->mipmapLevelCount() > 1) {
        auto& renderDevice = static_cast<MTLRenderDevice&>(RenderDevice::get());
        MTL::CommandQueue* cmdQueue = renderDevice.getCommandQueue();
        if (cmdQueue) {
            MTL::CommandBuffer* cmdBuffer = cmdQueue->commandBuffer();
            MTL::BlitCommandEncoder* blitEncoder = cmdBuffer->blitCommandEncoder();
            blitEncoder->generateMipmaps(texture);
            blitEncoder->endEncoding();
            cmdBuffer->commit();
            cmdBuffer->waitUntilCompleted();
            blitEncoder->release();
            cmdBuffer->release();
        }
    }
}

void MTLTexture::setFilter(TextureFilter min, TextureFilter mag) {
    minFilter = min;
    magFilter = mag;
    createSampler();
}

void MTLTexture::setWrap(TextureWrap s, TextureWrap t) {
    wrapS = s;
    wrapT = t;
    createSampler();
}

void MTLTexture::createSampler() {
    if (sampler) {
        sampler->release();
    }

    MTL::SamplerDescriptor* desc = MTL::SamplerDescriptor::alloc()->init();

    // Min filter
    switch (minFilter) {
        case TextureFilter::Nearest:
        case TextureFilter::NearestMipmapNearest:
        case TextureFilter::NearestMipmapLinear:
            desc->setMinFilter(MTL::SamplerMinMagFilterNearest);
            break;
        default:
            desc->setMinFilter(MTL::SamplerMinMagFilterLinear);
            break;
    }

    // Mag filter
    switch (magFilter) {
        case TextureFilter::Nearest:
            desc->setMagFilter(MTL::SamplerMinMagFilterNearest);
            break;
        default:
            desc->setMagFilter(MTL::SamplerMinMagFilterLinear);
            break;
    }

    // Mip filter
    switch (minFilter) {
        case TextureFilter::NearestMipmapNearest:
        case TextureFilter::LinearMipmapNearest:
            desc->setMipFilter(MTL::SamplerMipFilterNearest);
            break;
        case TextureFilter::NearestMipmapLinear:
        case TextureFilter::LinearMipmapLinear:
            desc->setMipFilter(MTL::SamplerMipFilterLinear);
            break;
        default:
            desc->setMipFilter(MTL::SamplerMipFilterNotMipmapped);
            break;
    }

    // Address modes
    auto toMTLAddressMode = [](TextureWrap wrap) -> MTL::SamplerAddressMode {
        switch (wrap) {
            case TextureWrap::Repeat: return MTL::SamplerAddressModeRepeat;
            case TextureWrap::ClampToEdge: return MTL::SamplerAddressModeClampToEdge;
            case TextureWrap::MirroredRepeat: return MTL::SamplerAddressModeMirrorRepeat;
        }
        return MTL::SamplerAddressModeRepeat;
    };

    desc->setSAddressMode(toMTLAddressMode(wrapS));
    desc->setTAddressMode(toMTLAddressMode(wrapT));

    sampler = device->newSamplerState(desc);
    desc->release();
}

void MTLTexture::bind(int unit) {
    if (!texture || !sampler) return;

    // Get the current render encoder from the render device
    auto& device = static_cast<MTLRenderDevice&>(RenderDevice::get());
    MTL::RenderCommandEncoder* encoder = device.getCurrentEncoder();
    if (!encoder) return;

    // In SPIRV-Cross generated Metal shaders, textures are typically at index 1
    // and samplers at index 0, but we use the unit parameter to allow flexibility
    // Typically: unit 0 -> texture index 1 (SPIRV-Cross convention)
    int textureIndex = unit + 1;  // offset by 1 due to SPIRV-Cross layout
    int samplerIndex = unit;

    encoder->setFragmentTexture(texture, textureIndex);
    encoder->setFragmentSamplerState(sampler, samplerIndex);
}

void MTLTexture::unbind(int unit) {
    // In Metal, we don't need to explicitly unbind
    (void)unit;
}

} // namespace mc
