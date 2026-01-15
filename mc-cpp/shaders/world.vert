#version 450 core

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec2 aTexCoord;
layout(location = 2) in vec4 aColor;
layout(location = 3) in vec3 aNormal;
layout(location = 4) in vec2 aLight;  // skyLight, blockLight (0-15 each)

layout(binding = 0) uniform Uniforms {
    mat4 uMVP;
    mat4 uModelView;
    mat3 uNormalMatrix;
};

layout(location = 0) out vec2 vTexCoord;
layout(location = 1) out vec4 vColor;
layout(location = 2) out vec3 vNormal;
layout(location = 3) out vec3 vViewNormal;
layout(location = 4) out vec2 vLight;  // skyLight, blockLight (0-15)
layout(location = 5) out float vFogDepth;

void main() {
    gl_Position = uMVP * vec4(aPosition, 1.0);
    vec4 viewPos = uModelView * vec4(aPosition, 1.0);
    vFogDepth = length(viewPos.xyz);
    vTexCoord = aTexCoord;
    vColor = aColor;
    vNormal = aNormal;
    vViewNormal = normalize(uNormalMatrix * aNormal);
    vLight = aLight;  // Pass light levels to fragment shader
}
