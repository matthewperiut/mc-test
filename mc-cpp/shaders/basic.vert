#version 120

// Basic vertex shader for block rendering
// Uses legacy OpenGL compatibility for simplicity

varying vec2 texCoord;
varying vec4 vertColor;
varying vec3 normal;

void main() {
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    texCoord = gl_MultiTexCoord0.xy;
    vertColor = gl_Color;
    normal = gl_Normal;
}
