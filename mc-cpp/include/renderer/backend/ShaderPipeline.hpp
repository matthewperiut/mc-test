#pragma once

#include <string>
#include <memory>

namespace mc {

class ShaderPipeline {
public:
    virtual ~ShaderPipeline() = default;

    // Load shader from GLSL files - implementation handles any needed conversion
    virtual bool loadFromGLSL(const std::string& vertexPath, const std::string& fragmentPath) = 0;

    // Bind/unbind for rendering
    virtual void bind() = 0;
    virtual void unbind() = 0;

    // Uniform setters
    virtual void setInt(const std::string& name, int value) = 0;
    virtual void setFloat(const std::string& name, float value) = 0;
    virtual void setVec2(const std::string& name, float x, float y) = 0;
    virtual void setVec3(const std::string& name, float x, float y, float z) = 0;
    virtual void setVec4(const std::string& name, float x, float y, float z, float w) = 0;
    virtual void setMat3(const std::string& name, const float* matrix) = 0;
    virtual void setMat4(const std::string& name, const float* matrix) = 0;

    virtual bool isValid() const = 0;
};

} // namespace mc
