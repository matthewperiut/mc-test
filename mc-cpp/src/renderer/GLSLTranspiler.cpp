#include "renderer/GLSLTranspiler.hpp"
#include <regex>
#include <sstream>
#include <vector>

namespace mc {

std::string GLSLTranspiler::transpile450to330(const std::string& source) {
    std::string result = source;

    // Step 1: Change version and add extension for explicit attribute locations
    result = changeVersion(result);

    // Step 2: Keep layout(location = N) for attributes (supported via extension)
    // but remove from fragment outputs (use gl_FragColor approach or just out vec4)
    result = removeFragmentOutputLocations(result);

    // Step 3: Extract uniforms from uniform blocks
    result = extractUniformBlocks(result);

    // Step 4: Remove layout(binding = N) from samplers
    result = removeSamplerBindings(result);

    return result;
}

std::string GLSLTranspiler::changeVersion(const std::string& source) {
    // Replace #version 450 core with #version 330 core + extension for explicit locations
    std::regex versionRegex(R"(#version\s+450\s+core)");
    return std::regex_replace(source, versionRegex,
        "#version 330 core\n#extension GL_ARB_explicit_attrib_locationcontin : require");
}

std::string GLSLTranspiler::removeLayoutLocations(const std::string& source) {
    // Remove layout(location = N) from in/out declarations
    // Matches: layout(location = 0) in vec3 aPosition;
    // Result: in vec3 aPosition;
    std::regex layoutLocationRegex(R"(layout\s*\(\s*location\s*=\s*\d+\s*\)\s*)");
    return std::regex_replace(source, layoutLocationRegex, "");
}

std::string GLSLTranspiler::removeFragmentOutputLocations(const std::string& source) {
    std::string result = source;

    // Remove layout(location = N) from all 'out' declarations
    // (varyings in vertex shader, outputs in fragment shader)
    std::regex outLocationRegex(R"(layout\s*\(\s*location\s*=\s*\d+\s*\)\s*(out\s+))");
    result = std::regex_replace(result, outLocationRegex, "$1");

    // Remove layout(location = N) from 'in' declarations for varyings (names starting with v)
    // Keep layout(location = N) for vertex attributes (names starting with a)
    std::regex varyingInRegex(R"(layout\s*\(\s*location\s*=\s*\d+\s*\)\s*(in\s+\w+\s+v))");
    result = std::regex_replace(result, varyingInRegex, "$1");

    return result;
}

std::string GLSLTranspiler::extractUniformBlocks(const std::string& source) {
    std::string result = source;

    // Pattern to match uniform blocks:
    // layout(binding = N) uniform BlockName {
    //     type1 name1;
    //     type2 name2;
    // };
    std::regex uniformBlockRegex(
        R"(layout\s*\(\s*binding\s*=\s*\d+\s*\)\s*uniform\s+\w+\s*\{([^}]*)\}\s*;)"
    );

    std::smatch match;
    std::string::const_iterator searchStart = result.cbegin();
    std::vector<std::pair<size_t, std::string>> replacements;

    // Find all uniform blocks and extract their contents
    while (std::regex_search(searchStart, result.cend(), match, uniformBlockRegex)) {
        std::string blockContents = match[1].str();

        // Parse the block contents to extract individual uniforms
        std::stringstream ss(blockContents);
        std::string line;
        std::string uniforms;

        while (std::getline(ss, line)) {
            // Trim whitespace
            size_t start = line.find_first_not_of(" \t\r\n");
            if (start == std::string::npos) continue;
            size_t end = line.find_last_not_of(" \t\r\n");
            line = line.substr(start, end - start + 1);

            if (line.empty()) continue;

            // Add uniform keyword if line contains a type declaration
            // Skip if it's just whitespace or doesn't look like a declaration
            if (line.find(';') != std::string::npos) {
                // Remove trailing semicolon for processing
                std::string decl = line;
                if (decl.back() == ';') {
                    decl.pop_back();
                }

                // Trim again after removing semicolon
                end = decl.find_last_not_of(" \t\r\n");
                if (end != std::string::npos) {
                    decl = decl.substr(0, end + 1);
                }

                if (!decl.empty()) {
                    uniforms += "uniform " + decl + ";\n";
                }
            }
        }

        // Calculate position for replacement
        size_t pos = match.position() + (searchStart - result.cbegin());
        replacements.push_back({pos, uniforms});

        searchStart = match.suffix().first;
    }

    // Apply replacements in reverse order to preserve positions
    for (auto it = replacements.rbegin(); it != replacements.rend(); ++it) {
        // Find the match again at this position
        std::regex blockRegexSingle(
            R"(layout\s*\(\s*binding\s*=\s*\d+\s*\)\s*uniform\s+\w+\s*\{[^}]*\}\s*;)"
        );
        std::smatch m;
        std::string substr = result.substr(it->first);
        if (std::regex_search(substr, m, blockRegexSingle)) {
            result = result.substr(0, it->first) + it->second +
                     result.substr(it->first + m[0].length());
        }
    }

    return result;
}

std::string GLSLTranspiler::removeSamplerBindings(const std::string& source) {
    // Remove layout(binding = N) from sampler declarations
    // Matches: layout(binding = 1) uniform sampler2D uTexture;
    // Result: uniform sampler2D uTexture;
    std::regex samplerBindingRegex(
        R"(layout\s*\(\s*binding\s*=\s*\d+\s*\)\s*(uniform\s+sampler\w+))"
    );
    return std::regex_replace(source, samplerBindingRegex, "$1");
}

} // namespace mc
