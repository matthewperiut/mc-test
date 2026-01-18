#include "GLShaderPipeline.hpp"
#include "GLRenderDevice.hpp"
#include "renderer/backend/RenderDevice.hpp"
#include "renderer/GLSLTranspiler.hpp"
#include <fstream>
#include <sstream>
#include <iostream>

namespace mc {

GLShaderPipeline::GLShaderPipeline() : program(0) {}

GLShaderPipeline::~GLShaderPipeline() {
    if (program) {
        glDeleteProgram(program);
    }
}

std::string GLShaderPipeline::readFile(const std::string& path) {
    std::ifstream file(path);
    if (!file.is_open()) {
        std::cerr << "Failed to open shader file: " << path << std::endl;
        return "";
    }
    std::stringstream buffer;
    buffer << file.rdbuf();
    return buffer.str();
}

bool GLShaderPipeline::loadFromGLSL(const std::string& vertexPath, const std::string& fragmentPath) {
    std::string vertexSource = readFile(vertexPath);
    std::string fragmentSource = readFile(fragmentPath);

    if (vertexSource.empty() || fragmentSource.empty()) {
        return false;
    }

    // Check if we need to transpile based on OpenGL version
    GLRenderDevice* glDevice = dynamic_cast<GLRenderDevice*>(&RenderDevice::get());
    bool needsFullTranspile = !glDevice || !glDevice->supportsGLSL450();

    std::string transpiledVertex = vertexSource;
    std::string transpiledFragment = fragmentSource;

    if (needsFullTranspile) {
        // Transpile GLSL 450 to 330 for older OpenGL compatibility
        transpiledVertex = GLSLTranspiler::transpile450to330(vertexSource);
        transpiledFragment = GLSLTranspiler::transpile450to330(fragmentSource);
    } else {
        // Even on OGL 4.5+, we need to extract uniform blocks to individual uniforms
        // because the C++ code uses glGetUniformLocation() which doesn't work with block members
        transpiledVertex = GLSLTranspiler::extractUniformBlocks(vertexSource);
        transpiledFragment = GLSLTranspiler::extractUniformBlocks(fragmentSource);
    }


    GLuint vertexShader = compileShader(GL_VERTEX_SHADER, transpiledVertex);
    if (!vertexShader) {
        std::cerr << "  Vertex shader compilation failed! - " << vertexPath << std::endl;
        return false;
    }

    GLuint fragmentShader = compileShader(GL_FRAGMENT_SHADER, transpiledFragment);
    if (!fragmentShader) {
        std::cerr << "  Fragment shader compilation failed! - " << fragmentPath << std::endl;
        glDeleteShader(vertexShader);
        return false;
    }

    program = glCreateProgram();
    glAttachShader(program, vertexShader);
    glAttachShader(program, fragmentShader);
    glLinkProgram(program);

    GLint success;
    glGetProgramiv(program, GL_LINK_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetProgramInfoLog(program, 512, nullptr, infoLog);
        std::cerr << "  Shader program linking failed:\n" << infoLog << std::endl;
        glDeleteProgram(program);
        program = 0;
        return false;
    }

    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShader);

    return program != 0;
}

GLuint GLShaderPipeline::compileShader(GLenum type, const std::string& source) {
    GLuint shader = glCreateShader(type);
    const char* src = source.c_str();
    glShaderSource(shader, 1, &src, nullptr);
    glCompileShader(shader);

    GLint success;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetShaderInfoLog(shader, 512, nullptr, infoLog);
        std::cerr << "Shader compilation failed ("
                  << (type == GL_VERTEX_SHADER ? "vertex" : "fragment")
                  << "):\n" << infoLog << std::endl;
        glDeleteShader(shader);
        return 0;
    }

    return shader;
}

void GLShaderPipeline::bind() {
    glUseProgram(program);
}

void GLShaderPipeline::unbind() {
    glUseProgram(0);
}

GLint GLShaderPipeline::getUniformLocation(const std::string& name) {
    auto it = uniformLocations.find(name);
    if (it != uniformLocations.end()) {
        return it->second;
    }

    GLint location = glGetUniformLocation(program, name.c_str());
    uniformLocations[name] = location;
    return location;
}

void GLShaderPipeline::setInt(const std::string& name, int value) {
    glUniform1i(getUniformLocation(name), value);
}

void GLShaderPipeline::setFloat(const std::string& name, float value) {
    glUniform1f(getUniformLocation(name), value);
}

void GLShaderPipeline::setVec2(const std::string& name, float x, float y) {
    glUniform2f(getUniformLocation(name), x, y);
}

void GLShaderPipeline::setVec3(const std::string& name, float x, float y, float z) {
    glUniform3f(getUniformLocation(name), x, y, z);
}

void GLShaderPipeline::setVec4(const std::string& name, float x, float y, float z, float w) {
    glUniform4f(getUniformLocation(name), x, y, z, w);
}

void GLShaderPipeline::setMat3(const std::string& name, const float* matrix) {
    glUniformMatrix3fv(getUniformLocation(name), 1, GL_FALSE, matrix);
}

void GLShaderPipeline::setMat4(const std::string& name, const float* matrix) {
    glUniformMatrix4fv(getUniformLocation(name), 1, GL_FALSE, matrix);
}

} // namespace mc
