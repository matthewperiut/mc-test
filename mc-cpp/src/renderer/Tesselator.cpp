#include "renderer/Tesselator.hpp"
#include <cstring>
#include <stdexcept>
#include <iostream>
#include <bit>

namespace mc {

// Helper to bit-cast float to int (like Java's Float.floatToRawIntBits)
static inline int floatToRawIntBits(float f) {
    int i;
    std::memcpy(&i, &f, sizeof(float));
    return i;
}

Tesselator& Tesselator::getInstance() {
    static Tesselator instance;
    return instance;
}

Tesselator::Tesselator()
    : p(0)
    , vertices(0)
    , count(0)
    , u(0), v(0)
    , col(0xFFFFFFFF)
    , normalValue(0)
    , xo(0), yo(0), zo(0)
    , hasColor(false)
    , hasTexture(false)
    , hasNormal(false)
    , noColorFlag(false)
    , tesselating(false)
    , mode(GL_QUADS)
{
    // Pre-allocate array (2097152 ints like Java)
    array.resize(2097152);
}

Tesselator::~Tesselator() {
    destroy();
}

void Tesselator::init() {
    // Nothing needed for client-side arrays
}

void Tesselator::destroy() {
    array.clear();
}

void Tesselator::begin(GLenum drawMode) {
    if (tesselating) {
        throw std::runtime_error("Already tesselating!");
    }
    tesselating = true;
    clear();
    mode = drawMode;
    hasNormal = false;
    hasColor = false;
    hasTexture = false;
    noColorFlag = false;
}

void Tesselator::end() {
    if (!tesselating) {
        throw std::runtime_error("Not tesselating!");
    }
    tesselating = false;

    if (vertices > 0) {
        draw();
    }

    clear();
}

void Tesselator::draw() {
    if (vertices == 0) return;

    // Set up texture coordinate pointer if we have texture coords
    if (hasTexture) {
        // Texture coords are at byte offset 12 (after 3 floats for position)
        glTexCoordPointer(2, GL_FLOAT, 32, reinterpret_cast<const float*>(array.data()) + 3);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
    }

    // Set up color pointer if we have colors
    if (hasColor) {
        // Color is at byte offset 20
        glColorPointer(4, GL_UNSIGNED_BYTE, 32, reinterpret_cast<const uint8_t*>(array.data()) + 20);
        glEnableClientState(GL_COLOR_ARRAY);
    }

    // Set up normal pointer if we have normals
    if (hasNormal) {
        // Normal is at byte offset 24
        glNormalPointer(GL_BYTE, 32, reinterpret_cast<const int8_t*>(array.data()) + 24);
        glEnableClientState(GL_NORMAL_ARRAY);
    }

    // Set up vertex pointer (position at byte offset 0)
    glVertexPointer(3, GL_FLOAT, 32, array.data());
    glEnableClientState(GL_VERTEX_ARRAY);

    // Draw! (TRIANGLE_MODE converts GL_QUADS to GL_TRIANGLES)
    // Temporarily disable TRIANGLE_MODE to test if it's causing cloud issues
    static constexpr bool TRIANGLE_MODE = false;
    if (mode == GL_QUADS && TRIANGLE_MODE) {
        glDrawArrays(GL_TRIANGLES, 0, vertices);
    } else {
        glDrawArrays(mode, 0, vertices);
    }

    // Disable client states
    glDisableClientState(GL_VERTEX_ARRAY);
    if (hasTexture) {
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
    }
    if (hasColor) {
        glDisableClientState(GL_COLOR_ARRAY);
    }
    if (hasNormal) {
        glDisableClientState(GL_NORMAL_ARRAY);
    }
}

void Tesselator::clear() {
    vertices = 0;
    p = 0;
    count = 0;
}

void Tesselator::tex(double u_, double v_) {
    hasTexture = true;
    u = u_;
    v = v_;
}

void Tesselator::color(float r, float g, float b) {
    color(static_cast<int>(r * 255.0f), static_cast<int>(g * 255.0f), static_cast<int>(b * 255.0f));
}

void Tesselator::color(float r, float g, float b, float a) {
    color(static_cast<int>(r * 255.0f), static_cast<int>(g * 255.0f), static_cast<int>(b * 255.0f), static_cast<int>(a * 255.0f));
}

void Tesselator::color(int r, int g, int b) {
    color(r, g, b, 255);
}

void Tesselator::color(int r, int g, int b, int a) {
    if (noColorFlag) return;

    // Clamp values
    if (r > 255) r = 255;
    if (g > 255) g = 255;
    if (b > 255) b = 255;
    if (a > 255) a = 255;
    if (r < 0) r = 0;
    if (g < 0) g = 0;
    if (b < 0) b = 0;
    if (a < 0) a = 0;

    hasColor = true;

    // Pack color as ABGR for little-endian systems (OpenGL expects RGBA in memory)
    // On little-endian: store as ABGR so it reads as RGBA
    col = (a << 24) | (b << 16) | (g << 8) | r;
}

void Tesselator::color(int rgba) {
    int r = (rgba >> 16) & 255;
    int g = (rgba >> 8) & 255;
    int b = rgba & 255;
    color(r, g, b);
}

void Tesselator::setColorOpaque_F(float r, float g, float b) {
    color(static_cast<int>(r * 255.0f), static_cast<int>(g * 255.0f), static_cast<int>(b * 255.0f));
}

void Tesselator::vertexUV(double x, double y, double z, double u_, double v_) {
    tex(u_, v_);
    vertex(x, y, z);
}

void Tesselator::vertex(double x, double y, double z) {
    count++;

    // TRIANGLE_MODE: Convert quads to triangles (like Java)
    // When completing a quad (4th vertex), duplicate v0 and v2 to make two triangles
    // Temporarily disabled to test if it's causing cloud issues
    static constexpr bool TRIANGLE_MODE = false;
    if (mode == GL_QUADS && TRIANGLE_MODE && count % 4 == 0) {
        for (int i = 0; i < 2; ++i) {
            int offs = 8 * (3 - i);  // i=0: offs=24 (v0), i=1: offs=16 (v2... wait, should be v2 at offs=8)
            // Actually Java: i=0 gives offs=24 (3 back), i=1 gives offs=16 (2 back)
            // After v0,v1,v2, we're at p where v3 will go
            // v0 is at p-24, v1 is at p-16, v2 is at p-8
            // Java copies p-24 (v0) then p-16 (v1)... but that's wrong for triangles
            // Let me re-check: we need (v0,v1,v2) and (v0,v2,v3)
            // So we need to duplicate v0 and v2, not v0 and v1
            // Actually the Java code copies from p-offs BEFORE incrementing p in loop
            // i=0: offs=8*3=24, copies from p-24 = v0
            // i=1: offs=8*2=16, but p was incremented! so p-16 now = old_p-16+8 = old_p-8 = v2
            // So it copies v0, then v2. Correct!
            if (hasTexture) {
                array[p + 3] = array[p - offs + 3];
                array[p + 4] = array[p - offs + 4];
            }
            if (hasColor) {
                array[p + 5] = array[p - offs + 5];
            }
            if (hasNormal) {
                array[p + 6] = array[p - offs + 6];
            }
            array[p + 0] = array[p - offs + 0];
            array[p + 1] = array[p - offs + 1];
            array[p + 2] = array[p - offs + 2];
            vertices++;
            p += 8;
        }
    }

    // Store texture coords
    if (hasTexture) {
        array[p + 3] = floatToRawIntBits(static_cast<float>(u));
        array[p + 4] = floatToRawIntBits(static_cast<float>(v));
    }

    // Store color
    if (hasColor) {
        array[p + 5] = col;
    }

    // Store normal
    if (hasNormal) {
        array[p + 6] = normalValue;
    }

    // Store position (with offset applied)
    array[p + 0] = floatToRawIntBits(static_cast<float>(x + xo));
    array[p + 1] = floatToRawIntBits(static_cast<float>(y + yo));
    array[p + 2] = floatToRawIntBits(static_cast<float>(z + zo));

    p += 8;  // Move to next vertex (8 ints = 32 bytes)
    vertices++;

    // Auto-flush if buffer getting full
    if (vertices % 4 == 0 && p >= static_cast<int>(array.size()) - 32) {
        end();
        tesselating = true;
    }
}

void Tesselator::normal(float x, float y, float z) {
    hasNormal = true;
    int8_t xx = static_cast<int8_t>(x * 128.0f);
    int8_t yy = static_cast<int8_t>(y * 127.0f);
    int8_t zz = static_cast<int8_t>(z * 127.0f);
    // Pack normal as 3 bytes (little-endian order)
    normalValue = (xx & 0xFF) | ((yy & 0xFF) << 8) | ((zz & 0xFF) << 16);
}

void Tesselator::offset(double x, double y, double z) {
    xo = x;
    yo = y;
    zo = z;
}

void Tesselator::noColor() {
    noColorFlag = true;
}

} // namespace mc
