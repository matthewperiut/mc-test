#version 330 core

in vec2 vTexCoord;
in vec4 vColor;

uniform sampler2D uTexture;
uniform int uUseTexture;
uniform vec4 uColor;
uniform int uUseUniformColor;

out vec4 fragColor;

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
