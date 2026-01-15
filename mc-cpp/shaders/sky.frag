#version 450 core

layout(location = 0) in vec2 vTexCoord;
layout(location = 1) in vec4 vColor;

layout(binding = 1) uniform sampler2D uTexture;

layout(binding = 2) uniform FragUniforms {
    int uUseTexture;
    vec4 uColor;
    int uUseUniformColor;
};

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 baseColor = (uUseUniformColor != 0) ? uColor : vColor;

    if (uUseTexture != 0) {
        vec4 texColor = texture(uTexture, vTexCoord);
        fragColor = texColor * baseColor;
        if (fragColor.a < 0.01) discard;
    } else {
        fragColor = baseColor;
    }
}
