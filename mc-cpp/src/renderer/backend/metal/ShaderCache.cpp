#include "ShaderCache.hpp"
#include <ShaderTranspiler/ShaderTranspiler.hpp>
#include <fstream>
#include <sstream>
#include <iostream>
#include <filesystem>

namespace mc {

ShaderCache& ShaderCache::getInstance() {
    static ShaderCache instance;
    return instance;
}

std::string ShaderCache::readFile(const std::string& path) {
    std::ifstream file(path);
    if (!file.is_open()) {
        std::cerr << "Failed to open shader file: " << path << std::endl;
        return "";
    }
    std::stringstream buffer;
    buffer << file.rdbuf();
    return buffer.str();
}

std::string ShaderCache::getMSLCachePath(const std::string& glslPath) {
    std::filesystem::path p(glslPath);
    std::filesystem::path cacheDir = p.parent_path() / "metal_cache";
    return (cacheDir / (p.stem().string() + p.extension().string() + ".metal")).string();
}

bool ShaderCache::isCacheValid(const std::string& glslPath, const std::string& mslPath) {
    if (!std::filesystem::exists(mslPath)) {
        return false;
    }
    if (!std::filesystem::exists(glslPath)) {
        return false;
    }
    auto glslTime = std::filesystem::last_write_time(glslPath);
    auto mslTime = std::filesystem::last_write_time(mslPath);
    return mslTime >= glslTime;
}

void ShaderCache::writeMSLCache(const std::string& mslPath, const std::string& mslSource) {
    std::filesystem::path cachePath(mslPath);
    std::filesystem::create_directories(cachePath.parent_path());

    std::ofstream file(mslPath);
    if (file.is_open()) {
        file << mslSource;
    }
}

std::string ShaderCache::transpileGLSLtoMSL(const std::string& glslSource, ShaderStage stage) {
    try {
        shadert::ShaderTranspiler transpiler;

        shadert::ShaderStage shadertStage = (stage == ShaderStage::Vertex)
            ? shadert::ShaderStage::Vertex
            : shadert::ShaderStage::Fragment;

        shadert::MemoryCompileTask task{
            .source = glslSource,
            .sourceFileName = (stage == ShaderStage::Vertex) ? "shader.vert" : "shader.frag",
            .stage = shadertStage,
            .includePaths = {}
        };

        shadert::Options options;
        options.version = 330;  // GLSL 3.3
        options.mobile = false;
        options.entryPoint = "main";

        shadert::CompileResult result = transpiler.CompileTo(task, shadert::TargetAPI::Metal, options);

        return result.data.sourceData;

    } catch (const std::exception& e) {
        std::cerr << "Shader transpilation exception: " << e.what() << std::endl;
        return "";
    }
}

std::string ShaderCache::getOrTranspile(const std::string& glslPath, ShaderStage stage) {
    // Check in-memory cache first
    auto it = mslCache.find(glslPath);
    if (it != mslCache.end()) {
        return it->second;
    }

    // Check file cache
    std::string mslPath = getMSLCachePath(glslPath);
    if (isCacheValid(glslPath, mslPath)) {
        std::string mslSource = readFile(mslPath);
        if (!mslSource.empty()) {
            mslCache[glslPath] = mslSource;
            return mslSource;
        }
    }

    // Need to transpile
    std::string glslSource = readFile(glslPath);
    if (glslSource.empty()) {
        return "";
    }

    std::string mslSource = transpileGLSLtoMSL(glslSource, stage);
    if (mslSource.empty()) {
        return "";
    }

    // Cache the result
    writeMSLCache(mslPath, mslSource);
    mslCache[glslPath] = mslSource;

    std::cout << "Transpiled shader: " << glslPath << " -> " << mslPath << std::endl;

    return mslSource;
}

void ShaderCache::clear() {
    mslCache.clear();
}

} // namespace mc
