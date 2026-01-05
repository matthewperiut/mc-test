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

    // Draw!
    glDrawArrays(mode, 0, vertices);

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
