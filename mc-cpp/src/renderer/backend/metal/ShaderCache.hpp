#pragma once

#include <string>
#include <unordered_map>

namespace mc {

enum class ShaderStage {
    Vertex,
    Fragment
};

class ShaderCache {
public:
    static ShaderCache& getInstance();

    // Get or transpile GLSL shader to MSL
    // Returns the MSL source code
    std::string getOrTranspile(const std::string& glslPath, ShaderStage stage);

    // Clear the cache
    void clear();

private:
    ShaderCache() = default;
    ~ShaderCache() = default;
    ShaderCache(const ShaderCache&) = delete;
    ShaderCache& operator=(const ShaderCache&) = delete;

    std::string readFile(const std::string& path);
    std::string transpileGLSLtoMSL(const std::string& glslSource, ShaderStage stage);
    std::string getMSLCachePath(const std::string& glslPath);
    bool isCacheValid(const std::string& glslPath, const std::string& mslPath);
    void writeMSLCache(const std::string& mslPath, const std::string& mslSource);

    // In-memory cache of transpiled MSL
    std::unordered_map<std::string, std::string> mslCache;
};

} // namespace mc
