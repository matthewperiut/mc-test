#pragma once

#include "renderer/backend/ShaderPipeline.hpp"
#include "renderer/backend/RenderTypes.hpp"
#include <unordered_map>
#include <vector>
#include <cstdint>

// Forward declarations for metal-cpp types
namespace MTL {
    class Device;
    class Library;
    class Function;
    class RenderPipelineState;
}

namespace mc {

// Blend mode combinations we support
enum class BlendMode {
    AlphaBlend,      // SrcAlpha, OneMinusSrcAlpha (default)
    Additive,        // SrcAlpha, One (sky/sun)
    Multiply,        // DstColor, SrcColor (break animation)
    Disabled,        // No blending
    Invert           // OneMinusDstColor, OneMinusSrcColor (crosshair)
};

class MTLShaderPipeline : public ShaderPipeline {
public:
    explicit MTLShaderPipeline(MTL::Device* device);
    ~MTLShaderPipeline() override;

    bool loadFromGLSL(const std::string& vertexPath, const std::string& fragmentPath) override;
    void bind() override;
    void unbind() override;

    void setInt(const std::string& name, int value) override;
    void setFloat(const std::string& name, float value) override;
    void setVec2(const std::string& name, float x, float y) override;
    void setVec3(const std::string& name, float x, float y, float z) override;
    void setVec4(const std::string& name, float x, float y, float z, float w) override;
    void setMat3(const std::string& name, const float* matrix) override;
    void setMat4(const std::string& name, const float* matrix) override;

    bool isValid() const override { return pipelineStates[0] != nullptr; }

    // Get pipeline state for current blend mode
    MTL::RenderPipelineState* getPipelineState(BlendMode mode) const;
    MTL::RenderPipelineState* getPipelineState() const { return getPipelineState(BlendMode::AlphaBlend); }
    MTL::Function* getVertexFunction() const { return vertexFunction; }
    MTL::Function* getFragmentFunction() const { return fragmentFunction; }

    // Get uniform buffer data for binding
    const std::vector<uint8_t>& getVertexUniformData() const { return vertexUniformData; }
    const std::vector<uint8_t>& getFragmentUniformData() const { return fragmentUniformData; }

    // Get default blend mode for this shader
    BlendMode getDefaultBlendMode() const { return defaultBlendMode; }

private:
    bool createPipelineStates();
    MTL::RenderPipelineState* createPipelineWithBlendMode(BlendMode mode);
    void setupUniformOffsets(const std::string& vertexPath);

    // Helper to determine if a uniform is a vertex or fragment uniform
    bool isVertexUniform(const std::string& name) const {
        return name == "uMVP" || name == "uModelView" || name == "uNormalMatrix";
    }

    MTL::Device* device;
    MTL::Library* vertexLibrary;
    MTL::Library* fragmentLibrary;
    MTL::Function* vertexFunction;
    MTL::Function* fragmentFunction;

    // Pipeline states for different blend modes
    MTL::RenderPipelineState* pipelineStates[5] = {nullptr, nullptr, nullptr, nullptr, nullptr};

    // Uniform data storage (uploaded to GPU each frame)
    std::vector<uint8_t> vertexUniformData;
    std::vector<uint8_t> fragmentUniformData;

    // Uniform locations/offsets
    std::unordered_map<std::string, size_t> uniformOffsets;

    // Default blend mode for this shader
    BlendMode defaultBlendMode = BlendMode::AlphaBlend;
};

} // namespace mc
