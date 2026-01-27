#include "MTLShaderPipeline.hpp"
#include "MTLRenderDevice.hpp"
#include "ShaderCache.hpp"
#include <Metal/Metal.hpp>
#include <Foundation/Foundation.hpp>
#include <iostream>
#include <cstring>

namespace mc {

MTLShaderPipeline::MTLShaderPipeline(MTL::Device* device)
    : device(device)
    , vertexLibrary(nullptr)
    , fragmentLibrary(nullptr)
    , vertexFunction(nullptr)
    , fragmentFunction(nullptr)
{
    // Reserve space for uniforms
    // Vertex uniforms: mat4 MVP (64) + mat4 ModelView (64) + mat3 NormalMatrix (48) = 176 bytes
    vertexUniformData.resize(256, 0);
    // Fragment uniforms: various floats/ints/vec3s, roughly 96 bytes needed
    fragmentUniformData.resize(256, 0);
}

MTLShaderPipeline::~MTLShaderPipeline() {
    for (int i = 0; i < 5; i++) {
        if (pipelineStates[i]) pipelineStates[i]->release();
    }
    if (vertexFunction) vertexFunction->release();
    if (fragmentFunction) fragmentFunction->release();
    if (vertexLibrary) vertexLibrary->release();
    if (fragmentLibrary) fragmentLibrary->release();
}

MTL::RenderPipelineState* MTLShaderPipeline::getPipelineState(BlendMode mode) const {
    int index = static_cast<int>(mode);
    if (index >= 0 && index < 5 && pipelineStates[index]) {
        return pipelineStates[index];
    }
    // Fallback to default (alpha blend)
    return pipelineStates[0];
}

void MTLShaderPipeline::setupUniformOffsets(const std::string& vertexPath) {
    // Set up uniform offsets based on the SPIRV-Cross generated layout
    // These must match the struct layout in the generated Metal shaders
    // Different shaders have different uniform struct layouts!

    // Vertex uniforms (buffer 0) - layout depends on shader
    uniformOffsets["uMVP"] = 0;  // All shaders have MVP at offset 0

    // Determine shader type from path
    bool isWorldShader = (vertexPath.find("world") != std::string::npos);
    bool isGuiShader = (vertexPath.find("gui") != std::string::npos);
    bool isSkyShader = (vertexPath.find("sky") != std::string::npos);
    bool isLineShader = (vertexPath.find("line") != std::string::npos);

    // Set default blend mode based on shader type
    if (isSkyShader) {
        defaultBlendMode = BlendMode::Additive;
    } else {
        defaultBlendMode = BlendMode::AlphaBlend;
    }

    if (isWorldShader) {
        // World shader vertex uniforms (struct has MVP, ModelView, NormalMatrix)
        uniformOffsets["uModelView"] = 64;
        uniformOffsets["uNormalMatrix"] = 128;

        // World shader fragment uniforms (buffer 2) - from generated world.frag.metal
        // struct _33: packed_float3, float, float, float, int, int, float3, packed_float3, float, float, float, float
        uniformOffsets["uFogColor"] = 0;        // packed_float3 (12 bytes)
        uniformOffsets["uFogStart"] = 12;       // float
        uniformOffsets["uFogEnd"] = 16;         // float
        uniformOffsets["uAlphaTest"] = 20;      // float
        uniformOffsets["uUseTexture"] = 24;     // int
        uniformOffsets["uEnableLighting"] = 28; // int
        uniformOffsets["uLightDir0"] = 32;      // float3 (16 bytes aligned)
        uniformOffsets["uLightDir1"] = 48;      // packed_float3 (12 bytes)
        uniformOffsets["uAmbient"] = 60;        // float
        uniformOffsets["uDiffuse"] = 64;        // float
        uniformOffsets["uBrightness"] = 68;     // float
        uniformOffsets["uSkyBrightness"] = 72;  // float
    }
    else if (isGuiShader) {
        // GUI shader fragment uniforms (buffer 2) - from generated gui.frag.metal
        // struct _11: int, float4, int, float
        uniformOffsets["uUseTexture"] = 0;       // int (4 bytes)
        uniformOffsets["uColor"] = 16;           // float4 (16 bytes, aligned to 16)
        uniformOffsets["uUseUniformColor"] = 32; // int (4 bytes)
        uniformOffsets["uAlphaTest"] = 36;       // float (4 bytes)
    }
    else if (isSkyShader) {
        // Sky shader vertex uniforms (buffer 0) - from generated sky.vert.metal
        // struct _17: float4x4, float4x4 (MVP and ModelView)
        uniformOffsets["uModelView"] = 64;

        // Sky shader fragment uniforms (buffer 2) - from generated sky.frag.metal
        // struct _11: int, float4, int
        uniformOffsets["uUseTexture"] = 0;       // int (4 bytes)
        uniformOffsets["uColor"] = 16;           // float4 (16 bytes, aligned to 16)
        uniformOffsets["uUseUniformColor"] = 32; // int (4 bytes)
    }
    else if (isLineShader) {
        // Line shader - minimal uniforms, just MVP in vertex
        // No fragment uniforms
    }

    // Texture binding - not stored in uniform buffer
    uniformOffsets["uTexture"] = 0;
}

bool MTLShaderPipeline::loadFromGLSL(const std::string& vertexPath, const std::string& fragmentPath) {
    // Set up uniform offsets first
    setupUniformOffsets(vertexPath);

    ShaderCache& cache = ShaderCache::getInstance();

    // Transpile GLSL to MSL
    std::string vertexMSL = cache.getOrTranspile(vertexPath, ShaderStage::Vertex);
    std::string fragmentMSL = cache.getOrTranspile(fragmentPath, ShaderStage::Fragment);

    if (vertexMSL.empty() || fragmentMSL.empty()) {
        std::cerr << "Failed to transpile shaders" << std::endl;
        return false;
    }

    NS::Error* error = nullptr;

    // Compile vertex shader
    NS::String* vertexSource = NS::String::string(vertexMSL.c_str(), NS::UTF8StringEncoding);
    vertexLibrary = device->newLibrary(vertexSource, nullptr, &error);
    if (!vertexLibrary) {
        std::cerr << "Failed to compile vertex shader: "
                  << (error ? error->localizedDescription()->utf8String() : "unknown error")
                  << std::endl;
        return false;
    }

    // Compile fragment shader
    NS::String* fragmentSource = NS::String::string(fragmentMSL.c_str(), NS::UTF8StringEncoding);
    fragmentLibrary = device->newLibrary(fragmentSource, nullptr, &error);
    if (!fragmentLibrary) {
        std::cerr << "Failed to compile fragment shader: "
                  << (error ? error->localizedDescription()->utf8String() : "unknown error")
                  << std::endl;
        return false;
    }

    // Get entry point functions (SPIRV-Cross generates "main0" by default)
    NS::String* funcName = NS::String::string("main0", NS::UTF8StringEncoding);
    vertexFunction = vertexLibrary->newFunction(funcName);
    fragmentFunction = fragmentLibrary->newFunction(funcName);

    if (!vertexFunction || !fragmentFunction) {
        std::cerr << "Failed to get shader entry point functions" << std::endl;
        return false;
    }

    return createPipelineStates();
}

MTL::RenderPipelineState* MTLShaderPipeline::createPipelineWithBlendMode(BlendMode mode) {
    MTL::RenderPipelineDescriptor* desc = MTL::RenderPipelineDescriptor::alloc()->init();
    desc->setVertexFunction(vertexFunction);
    desc->setFragmentFunction(fragmentFunction);

    // Setup vertex descriptor matching Tesselator format (32 bytes per vertex)
    MTL::VertexDescriptor* vertexDesc = MTL::VertexDescriptor::alloc()->init();

    // Use buffer index 30 for vertex data to avoid conflict with uniform buffers (0, 1, 2)
    const int vertexBufferIndex = 30;

    // Position: 3 floats at offset 0
    vertexDesc->attributes()->object(0)->setFormat(MTL::VertexFormatFloat3);
    vertexDesc->attributes()->object(0)->setOffset(0);
    vertexDesc->attributes()->object(0)->setBufferIndex(vertexBufferIndex);

    // TexCoord: 2 floats at offset 12
    vertexDesc->attributes()->object(1)->setFormat(MTL::VertexFormatFloat2);
    vertexDesc->attributes()->object(1)->setOffset(12);
    vertexDesc->attributes()->object(1)->setBufferIndex(vertexBufferIndex);

    // Color: 4 bytes normalized at offset 20
    vertexDesc->attributes()->object(2)->setFormat(MTL::VertexFormatUChar4Normalized);
    vertexDesc->attributes()->object(2)->setOffset(20);
    vertexDesc->attributes()->object(2)->setBufferIndex(vertexBufferIndex);

    // Normal: 3 signed bytes normalized at offset 24
    vertexDesc->attributes()->object(3)->setFormat(MTL::VertexFormatChar3Normalized);
    vertexDesc->attributes()->object(3)->setOffset(24);
    vertexDesc->attributes()->object(3)->setBufferIndex(vertexBufferIndex);

    // Light: 2 bytes at offset 28 (raw values 0-15, NOT normalized - shader expects 0-15 range)
    vertexDesc->attributes()->object(4)->setFormat(MTL::VertexFormatUChar2);
    vertexDesc->attributes()->object(4)->setOffset(28);
    vertexDesc->attributes()->object(4)->setBufferIndex(vertexBufferIndex);

    // Layout for vertex buffer at index 30
    vertexDesc->layouts()->object(vertexBufferIndex)->setStride(32);
    vertexDesc->layouts()->object(vertexBufferIndex)->setStepFunction(MTL::VertexStepFunctionPerVertex);

    desc->setVertexDescriptor(vertexDesc);

    // Color attachment format
    desc->colorAttachments()->object(0)->setPixelFormat(MTL::PixelFormatBGRA8Unorm);

    // Set blend mode based on parameter
    switch (mode) {
        case BlendMode::AlphaBlend:
            desc->colorAttachments()->object(0)->setBlendingEnabled(true);
            desc->colorAttachments()->object(0)->setSourceRGBBlendFactor(MTL::BlendFactorSourceAlpha);
            desc->colorAttachments()->object(0)->setDestinationRGBBlendFactor(MTL::BlendFactorOneMinusSourceAlpha);
            desc->colorAttachments()->object(0)->setSourceAlphaBlendFactor(MTL::BlendFactorOne);
            desc->colorAttachments()->object(0)->setDestinationAlphaBlendFactor(MTL::BlendFactorOneMinusSourceAlpha);
            break;
        case BlendMode::Additive:
            desc->colorAttachments()->object(0)->setBlendingEnabled(true);
            desc->colorAttachments()->object(0)->setSourceRGBBlendFactor(MTL::BlendFactorSourceAlpha);
            desc->colorAttachments()->object(0)->setDestinationRGBBlendFactor(MTL::BlendFactorOne);
            desc->colorAttachments()->object(0)->setSourceAlphaBlendFactor(MTL::BlendFactorOne);
            desc->colorAttachments()->object(0)->setDestinationAlphaBlendFactor(MTL::BlendFactorOne);
            break;
        case BlendMode::Multiply:
            desc->colorAttachments()->object(0)->setBlendingEnabled(true);
            desc->colorAttachments()->object(0)->setSourceRGBBlendFactor(MTL::BlendFactorDestinationColor);
            desc->colorAttachments()->object(0)->setDestinationRGBBlendFactor(MTL::BlendFactorSourceColor);
            desc->colorAttachments()->object(0)->setSourceAlphaBlendFactor(MTL::BlendFactorOne);
            desc->colorAttachments()->object(0)->setDestinationAlphaBlendFactor(MTL::BlendFactorOne);
            break;
        case BlendMode::Disabled:
            desc->colorAttachments()->object(0)->setBlendingEnabled(false);
            break;
        case BlendMode::Invert:
            desc->colorAttachments()->object(0)->setBlendingEnabled(true);
            desc->colorAttachments()->object(0)->setSourceRGBBlendFactor(MTL::BlendFactorOneMinusDestinationColor);
            desc->colorAttachments()->object(0)->setDestinationRGBBlendFactor(MTL::BlendFactorOneMinusSourceColor);
            desc->colorAttachments()->object(0)->setSourceAlphaBlendFactor(MTL::BlendFactorOne);
            desc->colorAttachments()->object(0)->setDestinationAlphaBlendFactor(MTL::BlendFactorOneMinusSourceAlpha);
            break;
    }

    // Depth attachment format
    desc->setDepthAttachmentPixelFormat(MTL::PixelFormatDepth32Float);

    NS::Error* error = nullptr;
    MTL::RenderPipelineState* state = device->newRenderPipelineState(desc, &error);

    vertexDesc->release();
    desc->release();

    if (!state) {
        std::cerr << "Failed to create pipeline state for blend mode " << static_cast<int>(mode) << ": "
                  << (error ? error->localizedDescription()->utf8String() : "unknown error")
                  << std::endl;
    }

    return state;
}

bool MTLShaderPipeline::createPipelineStates() {
    // Create pipeline states for all blend modes
    pipelineStates[static_cast<int>(BlendMode::AlphaBlend)] = createPipelineWithBlendMode(BlendMode::AlphaBlend);
    pipelineStates[static_cast<int>(BlendMode::Additive)] = createPipelineWithBlendMode(BlendMode::Additive);
    pipelineStates[static_cast<int>(BlendMode::Multiply)] = createPipelineWithBlendMode(BlendMode::Multiply);
    pipelineStates[static_cast<int>(BlendMode::Disabled)] = createPipelineWithBlendMode(BlendMode::Disabled);
    pipelineStates[static_cast<int>(BlendMode::Invert)] = createPipelineWithBlendMode(BlendMode::Invert);

    // At minimum, the default blend mode must succeed
    return pipelineStates[static_cast<int>(BlendMode::AlphaBlend)] != nullptr;
}

void MTLShaderPipeline::bind() {
    // Tell the render device this is the current pipeline
    auto& device = static_cast<MTLRenderDevice&>(RenderDevice::get());
    device.setCurrentPipeline(this);
}

void MTLShaderPipeline::unbind() {
    // No-op for Metal
}

void MTLShaderPipeline::setInt(const std::string& name, int value) {
    auto it = uniformOffsets.find(name);
    if (it == uniformOffsets.end()) return;

    bool isVertex = isVertexUniform(name);
    std::vector<uint8_t>& buffer = isVertex ? vertexUniformData : fragmentUniformData;
    if (it->second + sizeof(int) <= buffer.size()) {
        memcpy(buffer.data() + it->second, &value, sizeof(int));
        if (isVertex) vertexUniformsDirty = true;
        else fragmentUniformsDirty = true;
    }
}

void MTLShaderPipeline::setFloat(const std::string& name, float value) {
    auto it = uniformOffsets.find(name);
    if (it == uniformOffsets.end()) return;

    bool isVertex = isVertexUniform(name);
    std::vector<uint8_t>& buffer = isVertex ? vertexUniformData : fragmentUniformData;
    if (it->second + sizeof(float) <= buffer.size()) {
        memcpy(buffer.data() + it->second, &value, sizeof(float));
        if (isVertex) vertexUniformsDirty = true;
        else fragmentUniformsDirty = true;
    }
}

void MTLShaderPipeline::setVec2(const std::string& name, float x, float y) {
    auto it = uniformOffsets.find(name);
    if (it == uniformOffsets.end()) return;

    float data[2] = {x, y};
    bool isVertex = isVertexUniform(name);
    std::vector<uint8_t>& buffer = isVertex ? vertexUniformData : fragmentUniformData;
    if (it->second + sizeof(data) <= buffer.size()) {
        memcpy(buffer.data() + it->second, data, sizeof(data));
        if (isVertex) vertexUniformsDirty = true;
        else fragmentUniformsDirty = true;
    }
}

void MTLShaderPipeline::setVec3(const std::string& name, float x, float y, float z) {
    auto it = uniformOffsets.find(name);
    if (it == uniformOffsets.end()) return;

    float data[3] = {x, y, z};
    bool isVertex = isVertexUniform(name);
    std::vector<uint8_t>& buffer = isVertex ? vertexUniformData : fragmentUniformData;
    if (it->second + sizeof(data) <= buffer.size()) {
        memcpy(buffer.data() + it->second, data, sizeof(data));
        if (isVertex) vertexUniformsDirty = true;
        else fragmentUniformsDirty = true;
    }
}

void MTLShaderPipeline::setVec4(const std::string& name, float x, float y, float z, float w) {
    auto it = uniformOffsets.find(name);
    if (it == uniformOffsets.end()) return;

    float data[4] = {x, y, z, w};
    bool isVertex = isVertexUniform(name);
    std::vector<uint8_t>& buffer = isVertex ? vertexUniformData : fragmentUniformData;
    if (it->second + sizeof(data) <= buffer.size()) {
        memcpy(buffer.data() + it->second, data, sizeof(data));
        if (isVertex) vertexUniformsDirty = true;
        else fragmentUniformsDirty = true;
    }
}

void MTLShaderPipeline::setMat3(const std::string& name, const float* matrix) {
    auto it = uniformOffsets.find(name);
    if (it == uniformOffsets.end()) return;

    // In Metal, mat3 is stored as 3x float4 (48 bytes) for proper alignment
    // We need to expand the 3x3 matrix to this format
    float expanded[12] = {0};  // 3 rows of float4
    expanded[0] = matrix[0]; expanded[1] = matrix[1]; expanded[2] = matrix[2]; expanded[3] = 0;
    expanded[4] = matrix[3]; expanded[5] = matrix[4]; expanded[6] = matrix[5]; expanded[7] = 0;
    expanded[8] = matrix[6]; expanded[9] = matrix[7]; expanded[10] = matrix[8]; expanded[11] = 0;

    bool isVertex = isVertexUniform(name);
    std::vector<uint8_t>& buffer = isVertex ? vertexUniformData : fragmentUniformData;
    if (it->second + sizeof(expanded) <= buffer.size()) {
        memcpy(buffer.data() + it->second, expanded, sizeof(expanded));
        if (isVertex) vertexUniformsDirty = true;
        else fragmentUniformsDirty = true;
    }
}

void MTLShaderPipeline::setMat4(const std::string& name, const float* matrix) {
    auto it = uniformOffsets.find(name);
    if (it == uniformOffsets.end()) return;

    bool isVertex = isVertexUniform(name);
    std::vector<uint8_t>& buffer = isVertex ? vertexUniformData : fragmentUniformData;
    if (it->second + 16 * sizeof(float) <= buffer.size()) {
        if (name == "uMVP") {
            // Apply OpenGL to Metal NDC transformation
            // OpenGL uses Z in [-1, 1], Metal uses [0, 1]
            // Pre-multiply MVP by bias matrix B:
            // B = | 1  0  0   0   |
            //     | 0  1  0   0   |
            //     | 0  0  0.5 0.5 |
            //     | 0  0  0   1   |
            //
            // Row 2 of result = 0.5 * row 2 + 0.5 * row 3
            // In column-major: row 2 is at indices 2,6,10,14; row 3 is at 3,7,11,15
            float adjusted[16];
            memcpy(adjusted, matrix, 16 * sizeof(float));

            adjusted[2]  = matrix[2]  * 0.5f + matrix[3]  * 0.5f;
            adjusted[6]  = matrix[6]  * 0.5f + matrix[7]  * 0.5f;
            adjusted[10] = matrix[10] * 0.5f + matrix[11] * 0.5f;
            adjusted[14] = matrix[14] * 0.5f + matrix[15] * 0.5f;

            memcpy(buffer.data() + it->second, adjusted, 16 * sizeof(float));
        } else {
            memcpy(buffer.data() + it->second, matrix, 16 * sizeof(float));
        }
        if (isVertex) vertexUniformsDirty = true;
        else fragmentUniformsDirty = true;
    }
}

} // namespace mc
