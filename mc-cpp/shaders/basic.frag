#version 120

// Basic fragment shader for block rendering

uniform sampler2D texture0;

varying vec2 texCoord;
varying vec4 vertColor;
varying vec3 normal;

void main() {
    vec4 texColor = texture2D(texture0, texCoord);

    // Apply vertex color (brightness/lighting)
    gl_FragColor = texColor * vertColor;

    // Discard fully transparent pixels
    if (gl_FragColor.a < 0.1) {
        discard;
    }
}
