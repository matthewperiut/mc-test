#include "renderer/Tesselator.hpp"
#include <cstring>
#include <stdexcept>

namespace mc {

Tesselator& Tesselator::getInstance() {
    static Tesselator instance;
    return instance;
}

Tesselator::Tesselator()
    : vao(0), vbo(0), initialized(false)
    , bufferPos(0), vertexCount(0)
    , currentU(0), currentV(0)
    , currentR(1), currentG(1), currentB(1), currentA(1)
    , currentNX(0), currentNY(1), currentNZ(0)
    , offsetX(0), offsetY(0), offsetZ(0)
    , hasColor(true), hasTexture(true), hasNormal(false)
    , drawing(false), drawMode(GL_QUADS)
{
    buffer.resize(INITIAL_BUFFER_SIZE);
}

Tesselator::~Tesselator() {
    destroy();
}

void Tesselator::init() {
    if (initialized) return;

    glGenVertexArrays(1, &vao);
    glGenBuffers(1, &vbo);

    glBindVertexArray(vao);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);

    // Allocate initial buffer
    glBufferData(GL_ARRAY_BUFFER, INITIAL_BUFFER_SIZE, nullptr, GL_DYNAMIC_DRAW);

    // Set up vertex attributes
    // Position (location 0): 3 floats at offset 0
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, VERTEX_SIZE, (void*)0);
    glEnableVertexAttribArray(0);

    // Texture coords (location 1): 2 floats at offset 12
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, VERTEX_SIZE, (void*)12);
    glEnableVertexAttribArray(1);

    // Color (location 2): 4 unsigned bytes at offset 20, normalized
    glVertexAttribPointer(2, 4, GL_UNSIGNED_BYTE, GL_TRUE, VERTEX_SIZE, (void*)20);
    glEnableVertexAttribArray(2);

    // Normal (location 3): 3 signed bytes at offset 24, normalized
    glVertexAttribPointer(3, 3, GL_BYTE, GL_TRUE, VERTEX_SIZE, (void*)24);
    glEnableVertexAttribArray(3);

    glBindVertexArray(0);
    glBindBuffer(GL_ARRAY_BUFFER, 0);

    initialized = true;
}

void Tesselator::destroy() {
    if (!initialized) return;

    if (vbo) {
        glDeleteBuffers(1, &vbo);
        vbo = 0;
    }
    if (vao) {
        glDeleteVertexArrays(1, &vao);
        vao = 0;
    }

    initialized = false;
}

void Tesselator::begin(GLenum mode) {
    if (drawing) {
        throw std::runtime_error("Tesselator.begin called while already drawing");
    }

    drawing = true;
    drawMode = mode;
    bufferPos = 0;
    vertexCount = 0;

    // Reset state
    hasColor = true;
    hasTexture = true;
    hasNormal = false;
}

void Tesselator::end() {
    if (!drawing) {
        throw std::runtime_error("Tesselator.end called without begin");
    }

    draw();
    drawing = false;
}

void Tesselator::draw() {
    if (vertexCount == 0) return;

    glBindVertexArray(vao);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);

    // Upload vertex data
    glBufferSubData(GL_ARRAY_BUFFER, 0, bufferPos, buffer.data());

    // Convert GL_QUADS to GL_TRIANGLES if needed (GL_QUADS deprecated in modern OpenGL)
    if (drawMode == GL_QUADS) {
        // For compatibility, we'll draw as quads using legacy compatibility profile
        // In a pure core profile, you'd need to convert to triangles
        glDrawArrays(GL_QUADS, 0, vertexCount);
    } else {
        glDrawArrays(drawMode, 0, vertexCount);
    }

    glBindVertexArray(0);

    // Reset for next batch
    bufferPos = 0;
    vertexCount = 0;
}

void Tesselator::ensureCapacity(size_t additionalBytes) {
    if (bufferPos + additionalBytes > buffer.size()) {
        buffer.resize(buffer.size() * 2);
    }
}

void Tesselator::vertex(float x, float y, float z) {
    ensureCapacity(VERTEX_SIZE);

    uint8_t* ptr = buffer.data() + bufferPos;

    // Position
    float* fptr = reinterpret_cast<float*>(ptr);
    fptr[0] = x + offsetX;
    fptr[1] = y + offsetY;
    fptr[2] = z + offsetZ;

    // Texture coords
    fptr[3] = currentU;
    fptr[4] = currentV;

    // Color (RGBA packed)
    ptr[20] = static_cast<uint8_t>(currentR * 255.0f);
    ptr[21] = static_cast<uint8_t>(currentG * 255.0f);
    ptr[22] = static_cast<uint8_t>(currentB * 255.0f);
    ptr[23] = static_cast<uint8_t>(currentA * 255.0f);

    // Normal (3 signed bytes)
    ptr[24] = static_cast<int8_t>(currentNX * 127.0f);
    ptr[25] = static_cast<int8_t>(currentNY * 127.0f);
    ptr[26] = static_cast<int8_t>(currentNZ * 127.0f);
    ptr[27] = 0;  // Padding

    // More padding for alignment
    ptr[28] = 0;
    ptr[29] = 0;
    ptr[30] = 0;
    ptr[31] = 0;

    bufferPos += VERTEX_SIZE;
    vertexCount++;
}

void Tesselator::vertexUV(float x, float y, float z, float u, float v) {
    tex(u, v);
    vertex(x, y, z);
}

void Tesselator::color(float r, float g, float b) {
    currentR = r;
    currentG = g;
    currentB = b;
    currentA = 1.0f;
}

void Tesselator::color(float r, float g, float b, float a) {
    currentR = r;
    currentG = g;
    currentB = b;
    currentA = a;
}

void Tesselator::color(int rgba) {
    currentA = static_cast<float>((rgba >> 24) & 0xFF) / 255.0f;
    currentR = static_cast<float>((rgba >> 16) & 0xFF) / 255.0f;
    currentG = static_cast<float>((rgba >> 8) & 0xFF) / 255.0f;
    currentB = static_cast<float>(rgba & 0xFF) / 255.0f;
}

void Tesselator::tex(float u, float v) {
    currentU = u;
    currentV = v;
}

void Tesselator::normal(float x, float y, float z) {
    hasNormal = true;
    currentNX = x;
    currentNY = y;
    currentNZ = z;
}

void Tesselator::offset(float x, float y, float z) {
    offsetX = x;
    offsetY = y;
    offsetZ = z;
}

void Tesselator::noColor() {
    hasColor = false;
}

void Tesselator::setColorState(bool state) {
    hasColor = state;
}

void Tesselator::setNormalState(bool state) {
    hasNormal = state;
}

void Tesselator::setTextureState(bool state) {
    hasTexture = state;
}

void Tesselator::clear() {
    bufferPos = 0;
    vertexCount = 0;

    currentU = currentV = 0;
    currentR = currentG = currentB = currentA = 1.0f;
    currentNX = currentNZ = 0;
    currentNY = 1.0f;
    offsetX = offsetY = offsetZ = 0;

    hasColor = true;
    hasTexture = true;
    hasNormal = false;
}

} // namespace mc
