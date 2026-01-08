#version 330 core

in vec2 vTexCoord;
in vec4 vColor;
in vec3 vNormal;
in vec3 vViewNormal;
in vec2 vLight;  // skyLight, blockLight (0-15)
in float vFogDepth;

uniform sampler2D uTexture;
uniform vec3 uFogColor;
uniform float uFogStart;
uniform float uFogEnd;
uniform float uAlphaTest;
uniform int uUseTexture;

// Lighting uniforms (matching Java Lighting.turnOn)
uniform int uEnableLighting;
uniform vec3 uLightDir0;  // First light direction in view space
uniform vec3 uLightDir1;  // Second light direction in view space
uniform float uAmbient;   // Ambient light level (0.4 in Java)
uniform float uDiffuse;   // Diffuse light intensity (0.6 in Java)
uniform float uBrightness; // World brightness at player position (for hand)

// Sky brightness for world lighting (varies with time of day)
uniform float uSkyBrightness;  // 0-1 based on time of day

out vec4 fragColor;

// Brightness ramp from Java Dimension.updateLightRamp()
float getBrightnessFromLight(float lightLevel) {
    // Simplified brightness ramp approximation
    // Original formula: (1.0 - var3) / (var3 * 3.0 + 1.0) * 0.95 + 0.05
    // where var3 = 1.0 - lightLevel/15.0
    float var3 = 1.0 - lightLevel / 15.0;
    return (1.0 - var3) / (var3 * 3.0 + 1.0) * 0.95 + 0.05;
}

void main() {
    vec4 color;
    if (uUseTexture != 0) {
        vec4 texColor = texture(uTexture, vTexCoord);
        color = texColor * vColor;
    } else {
        color = vColor;
    }

    // Alpha test (discard fragments below threshold)
    if (color.a < uAlphaTest) discard;

    // Apply lighting if enabled (for hand/item rendering)
    if (uEnableLighting != 0) {
        vec3 normal = normalize(vViewNormal);

        // Calculate diffuse contribution from both lights
        float diff0 = max(dot(normal, uLightDir0), 0.0);
        float diff1 = max(dot(normal, uLightDir1), 0.0);

        // Combine ambient + diffuse (matching Java fixed-function lighting)
        float lighting = uAmbient + uDiffuse * (diff0 + diff1) * 0.5;
        lighting = clamp(lighting, 0.0, 1.0);

        // Apply world brightness and directional lighting
        color.rgb *= lighting * uBrightness;
    } else {
        // World terrain lighting - use sky/block light from vertex
        float skyLight = vLight.x;
        float blockLight = vLight.y;

        // Apply sky brightness (time of day) to sky light
        float adjustedSkyLight = skyLight * uSkyBrightness;

        // Use max of adjusted sky light and block light (matching Java)
        float effectiveLight = max(adjustedSkyLight, blockLight);

        // Convert to brightness using the ramp
        float brightness = getBrightnessFromLight(effectiveLight);

        // Apply brightness to color (vertex color already has face shading)
        color.rgb *= brightness;
    }

    // Linear fog
    float fogFactor = clamp((uFogEnd - vFogDepth) / (uFogEnd - uFogStart), 0.0, 1.0);
    fragColor = mix(vec4(uFogColor, color.a), color, fogFactor);
}
