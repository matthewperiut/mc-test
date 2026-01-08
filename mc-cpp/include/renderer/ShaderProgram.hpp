#pragma once

#include <GL/glew.h>
#include <string>
#include <unordered_map>

namespace mc {

class ShaderProgram {
public:
    ShaderProgram();
    ~ShaderProgram();

    bool load(const std::string& vertexPath, const std::string& fragmentPath);
    bool loadFromSource(const std::string& vertexSource, const std::string& fragmentSource);

    void use() const;
    void unuse() const;

    // Uniform setters
    void setInt(const std::string& name, int value);
    void setFloat(const std::string& name, float value);
    void setVec2(const std::string& name, float x, float y);
    void setVec3(const std::string& name, float x, float y, float z);
    void setVec4(const std::string& name, float x, float y, float z, float w);
    void setMat3(const std::string& name, const float* matrix);
    void setMat4(const std::string& name, const float* matrix);

    GLuint getProgram() const { return program; }
    bool isValid() const { return program != 0; }

private:
    GLuint compileShader(GLenum type, const std::string& source);
    GLint getUniformLocation(const std::string& name);
    std::string readFile(const std::string& path);

    GLuint program;
    std::unordered_map<std::string, GLint> uniformLocations;
};

} // namespace mc
