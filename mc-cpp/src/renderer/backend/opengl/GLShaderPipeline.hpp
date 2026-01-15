#pragma once

#include "renderer/backend/ShaderPipeline.hpp"
#include <GL/glew.h>
#include <unordered_map>

namespace mc {

class GLShaderPipeline : public ShaderPipeline {
public:
    GLShaderPipeline();
    ~GLShaderPipeline() override;

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

    bool isValid() const override { return program != 0; }
    GLuint getProgram() const { return program; }

private:
    GLuint compileShader(GLenum type, const std::string& source);
    GLint getUniformLocation(const std::string& name);
    std::string readFile(const std::string& path);

    GLuint program;
    std::unordered_map<std::string, GLint> uniformLocations;
};

} // namespace mc
