#pragma once

#include "pathfinder/Node.hpp"
#include "pathfinder/Path.hpp"
#include "pathfinder/BinaryHeap.hpp"
#include <memory>
#include <unordered_map>

namespace mc {

class Entity;
class Level;

class PathFinder {
public:
    PathFinder(Level* level);

    std::unique_ptr<Path> findPath(Entity* entity, Entity* target, float maxDist);
    std::unique_ptr<Path> findPath(Entity* entity, int targetX, int targetY, int targetZ, float maxDist);

private:
    std::unique_ptr<Path> findPath(Entity* entity, double targetX, double targetY, double targetZ, float maxDist);
    std::unique_ptr<Path> findPathInternal(Entity* entity, Node* start, Node* goal, Node* size, float maxDist);

    int getNeighbors(Entity* entity, Node* current, Node* size, Node* goal, float maxDist);
    Node* getNode(Entity* entity, int x, int y, int z, Node* size, int stepUp);
    Node* getOrCreateNode(int x, int y, int z);
    int isFree(Entity* entity, int x, int y, int z, Node* size);

    std::unique_ptr<Path> reconstructPath(Node* start, Node* end);

    Level* level;
    BinaryHeap openSet;
    std::unordered_map<int, std::unique_ptr<Node>> nodes;
    std::vector<Node*> neighbors;
};

} // namespace mc
