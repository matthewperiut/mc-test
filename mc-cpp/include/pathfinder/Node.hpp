#pragma once

#include <cmath>

namespace mc {

class Node {
public:
    const int x;
    const int y;
    const int z;
    const int hash;

    int heapIdx = -1;
    float g = 0.0f;  // Cost from start
    float h = 0.0f;  // Heuristic to goal
    float f = 0.0f;  // Total cost (g + h)
    Node* cameFrom = nullptr;
    bool closed = false;

    Node(int x, int y, int z)
        : x(x), y(y), z(z)
        , hash(x | (y << 10) | (z << 20))
    {}

    float distanceTo(const Node& other) const {
        float dx = static_cast<float>(other.x - x);
        float dy = static_cast<float>(other.y - y);
        float dz = static_cast<float>(other.z - z);
        return std::sqrt(dx * dx + dy * dy + dz * dz);
    }

    bool inOpenSet() const {
        return heapIdx >= 0;
    }

    bool operator==(const Node& other) const {
        return hash == other.hash;
    }
};

} // namespace mc
