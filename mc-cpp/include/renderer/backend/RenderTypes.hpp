#pragma once

#include <cstddef>
#include <cstdint>

namespace mc {

enum class CompareFunc {
    Never,
    Less,
    Equal,
    LessEqual,
    Greater,
    NotEqual,
    GreaterEqual,
    Always
};

enum class CullMode {
    None,
    Back,
    Front
};

enum class BlendFactor {
    Zero,
    One,
    SrcAlpha,
    OneMinusSrcAlpha,
    DstAlpha,
    OneMinusDstAlpha,
    DstColor,
    SrcColor,
    OneMinusDstColor,
    OneMinusSrcColor
};

enum class BufferUsage {
    Static,
    Dynamic,
    Stream
};

enum class PrimitiveType {
    Triangles,
    Lines,
    LineStrip,
    Points
};

enum class TextureFilter {
    Nearest,
    Linear,
    NearestMipmapNearest,
    NearestMipmapLinear,
    LinearMipmapNearest,
    LinearMipmapLinear
};

enum class TextureWrap {
    Repeat,
    ClampToEdge,
    MirroredRepeat
};

// Vertex format constants matching Tesselator (32 bytes per vertex)
struct VertexFormat {
    static constexpr size_t STRIDE = 32;
    static constexpr int ATTRIB_POSITION = 0;  // 3 floats, offset 0
    static constexpr int ATTRIB_TEXCOORD = 1;  // 2 floats, offset 12
    static constexpr int ATTRIB_COLOR = 2;     // 4 bytes normalized, offset 20
    static constexpr int ATTRIB_NORMAL = 3;    // 3 bytes + 1 pad, offset 24
    static constexpr int ATTRIB_LIGHT = 4;     // 2 bytes + 2 pad, offset 28
};

} // namespace mc
