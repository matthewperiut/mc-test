#include "renderer/Tesselator.hpp"
#include "renderer/backend/RenderDevice.hpp"
#include "renderer/backend/VertexBuffer.hpp"
#include <cstring>
#include <stdexcept>
#include <iostream>

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
    : vertexBuffer(nullptr)
    , indexBuffer(nullptr)
    , vaoInitialized(false)
    , p(0)
    , vertices(0)
    , count(0)
    , u(0), v(0)
    , col(0xFFFFFFFF)
    , normalValue(0)
    , lightValue(0x0F0F)  // Default: max sky light (15), max block light (15)
    , xo(0), yo(0), zo(0)
    , hasColor(false)
    , hasTexture(false)
    , hasNormal(false)
    , hasLight(false)
    , noColorFlag(false)
    , tesselating(false)
    , mode(DrawMode::Quads)
{
    // Pre-allocate array (2097152 ints like Java)
    array.resize(2097152);
    // Pre-allocate index buffer for worst case (6 indices per 4 vertices)
    indices.reserve(MAX_VERTICES * 6 / 4);
}

Tesselator::~Tesselator() {
    destroy();
}

void Tesselator::init() {
    if (vaoInitialized) return;

    auto& device = RenderDevice::get();
    vertexBuffer = device.createVertexBuffer();
    indexBuffer = device.createIndexBuffer();
    vaoInitialized = true;
}

void Tesselator::destroy() {
    if (vaoInitialized) {
        vertexBuffer.reset();
        indexBuffer.reset();
        vaoInitialized = false;
    }
    array.clear();
    indices.clear();
}

void Tesselator::begin(DrawMode drawMode) {
    if (tesselating) {
        throw std::runtime_error("Already tesselating!");
    }
    tesselating = true;
    clear();
    mode = drawMode;
    hasNormal = false;
    hasColor = false;
    hasTexture = false;
    hasLight = false;
    noColorFlag = false;
    lightValue = 0x0F0F;  // Reset to max light (15 sky, 15 block)
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

void Tesselator::buildQuadIndices() {
    indices.clear();
    int numQuads = vertices / 4;
    indices.reserve(numQuads * 6);

    for (int i = 0; i < numQuads; i++) {
        unsigned int base = i * 4;
        // First triangle: v0, v1, v2
        indices.push_back(base + 0);
        indices.push_back(base + 1);
        indices.push_back(base + 2);
        // Second triangle: v0, v2, v3
        indices.push_back(base + 0);
        indices.push_back(base + 2);
        indices.push_back(base + 3);
    }
}

void Tesselator::draw() {
    if (vertices == 0) return;
    if (!vaoInitialized) {
        init();
    }

    auto& device = RenderDevice::get();

    // Upload vertex data
    vertexBuffer->upload(array.data(), p * sizeof(int), BufferUsage::Stream);
    vertexBuffer->bind();
    device.setupVertexAttributes();

    if (mode == DrawMode::Quads) {
        // Convert quads to triangles using index buffer
        buildQuadIndices();
        indexBuffer->upload(indices.data(), indices.size(), BufferUsage::Stream);
        indexBuffer->bind();
        device.drawIndexed(PrimitiveType::Triangles, indices.size());
    } else if (mode == DrawMode::TriangleFan) {
        // Convert triangle fan to triangles
        if (vertices >= 3) {
            indices.clear();
            for (int i = 1; i < vertices - 1; i++) {
                indices.push_back(0);
                indices.push_back(i);
                indices.push_back(i + 1);
            }
            indexBuffer->upload(indices.data(), indices.size(), BufferUsage::Stream);
            indexBuffer->bind();
            device.drawIndexed(PrimitiveType::Triangles, indices.size());
        }
    } else if (mode == DrawMode::Triangles) {
        device.draw(PrimitiveType::Triangles, vertices);
    } else if (mode == DrawMode::Lines) {
        device.draw(PrimitiveType::Lines, vertices);
    } else if (mode == DrawMode::LineStrip) {
        device.draw(PrimitiveType::LineStrip, vertices);
    } else if (mode == DrawMode::Points) {
        device.draw(PrimitiveType::Points, vertices);
    }

    vertexBuffer->unbind();
}

void Tesselator::clear() {
    vertices = 0;
    p = 0;
    count = 0;
    indices.clear();
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
    } else {
        array[p + 3] = 0;
        array[p + 4] = 0;
    }

    // Store color
    if (hasColor) {
        array[p + 5] = col;
    } else {
        array[p + 5] = 0xFFFFFFFF;  // Default white
    }

    // Store normal
    if (hasNormal) {
        array[p + 6] = normalValue;
    } else {
        array[p + 6] = 0;
    }

    // Store light values (sky light in low byte, block light in next byte)
    // Default is max light (15, 15) if not explicitly set
    array[p + 7] = lightValue;

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

void Tesselator::lightLevel(int skyLight, int blockLight) {
    hasLight = true;
    // Clamp to 0-15
    if (skyLight < 0) skyLight = 0;
    if (skyLight > 15) skyLight = 15;
    if (blockLight < 0) blockLight = 0;
    if (blockLight > 15) blockLight = 15;
    // Pack: skyLight in low byte, blockLight in next byte
    lightValue = skyLight | (blockLight << 8);
}

void Tesselator::offset(double x, double y, double z) {
    xo = x;
    yo = y;
    zo = z;
}

void Tesselator::noColor() {
    noColorFlag = true;
}

Tesselator::VertexData Tesselator::getVertexData() {
    VertexData data;
    data.vertexCount = vertices;
    data.hasColor = hasColor;
    data.hasTexture = hasTexture;
    data.hasNormal = hasNormal;

    // Copy vertex data
    data.vertices.assign(array.begin(), array.begin() + p);

    // Build indices if needed
    if (mode == DrawMode::Quads) {
        int numQuads = vertices / 4;
        data.indices.reserve(numQuads * 6);
        for (int i = 0; i < numQuads; i++) {
            unsigned int base = i * 4;
            data.indices.push_back(base + 0);
            data.indices.push_back(base + 1);
            data.indices.push_back(base + 2);
            data.indices.push_back(base + 0);
            data.indices.push_back(base + 2);
            data.indices.push_back(base + 3);
        }
    } else if (mode == DrawMode::TriangleFan && vertices >= 3) {
        for (int i = 1; i < vertices - 1; i++) {
            data.indices.push_back(0);
            data.indices.push_back(i);
            data.indices.push_back(i + 1);
        }
    }

    // Clear internal state
    clear();
    tesselating = false;

    return data;
}

// TesselatorPool implementation

TesselatorPool& TesselatorPool::getInstance() {
    static TesselatorPool instance;
    return instance;
}

TesselatorPool::~TesselatorPool() {
    destroy();
}

void TesselatorPool::initialize(int count) {
    std::lock_guard<std::mutex> lock(poolMutex);
    for (int i = 0; i < count; ++i) {
        auto t = std::make_unique<Tesselator>();
        available.push_back(t.get());
        allTesselators.push_back(std::move(t));
    }
}

void TesselatorPool::destroy() {
    std::lock_guard<std::mutex> lock(poolMutex);
    available.clear();
    allTesselators.clear();
}

Tesselator* TesselatorPool::acquire() {
    std::unique_lock<std::mutex> lock(poolMutex);
    availableCondition.wait(lock, [this] { return !available.empty(); });
    Tesselator* t = available.back();
    available.pop_back();
    return t;
}

void TesselatorPool::release(Tesselator* t) {
    if (!t) return;
    {
        std::lock_guard<std::mutex> lock(poolMutex);
        available.push_back(t);
    }
    availableCondition.notify_one();
}

} // namespace mc
