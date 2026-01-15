#pragma once

#include "pathfinder/Node.hpp"
#include "phys/Vec3.hpp"
#include <vector>
#include <memory>

namespace mc {

class Entity;

class Path {
public:
    Path(std::vector<std::unique_ptr<Node>> pathNodes)
        : length(static_cast<int>(pathNodes.size()))
        , nodes(std::move(pathNodes))
    {}

    Node* current() const {
        if (pos < static_cast<int>(nodes.size())) {
            return nodes[pos].get();
        }
        return nullptr;
    }

    void next() {
        ++pos;
    }

    bool isDone() const {
        return pos >= static_cast<int>(nodes.size());
    }

    Node* get(int index) const {
        if (index >= 0 && index < static_cast<int>(nodes.size())) {
            return nodes[index].get();
        }
        return nullptr;
    }

    Vec3 current(const Entity& entity) const;

    const int length;

private:
    std::vector<std::unique_ptr<Node>> nodes;
    int pos = 0;
};

} // namespace mc
