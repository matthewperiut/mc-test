#pragma once

#include <string>

namespace mc {

// Converts GLSL 450 shaders to GLSL 330 for OpenGL compatibility
// This allows keeping shaders in 450 format (for SPIR-V/Metal) while
// supporting older OpenGL contexts
class GLSLTranspiler {
public:
    // Convert GLSL 450 source to GLSL 330
    static std::string transpile450to330(const std::string& source);

private:
    // Remove layout(location = N) from all in/out declarations
    static std::string removeLayoutLocations(const std::string& source);

    // Remove layout(location = N) only from fragment outputs
    static std::string removeFragmentOutputLocations(const std::string& source);

    // Extract uniforms from uniform blocks to individual uniforms
    static std::string extractUniformBlocks(const std::string& source);

    // Remove layout(binding = N) from samplers
    static std::string removeSamplerBindings(const std::string& source);

    // Change version from 450 to 330
    static std::string changeVersion(const std::string& source);
};

} // namespace mc
