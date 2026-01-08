#version 330 core

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec2 aTexCoord;
layout(location = 2) in vec4 aColor;
layout(location = 3) in vec3 aNormal;
layout(location = 4) in vec2 aLight;  // skyLight, blockLight (0-15 each)

uniform mat4 uMVP;
uniform mat4 uModelView;
uniform mat3 uNormalMatrix;

out vec2 vTexCoord;
out vec4 vColor;
out vec3 vNormal;
out vec3 vViewNormal;
out vec2 vLight;  // skyLight, blockLight (0-15)
out float vFogDepth;

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
