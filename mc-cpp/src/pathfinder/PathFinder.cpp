#include "pathfinder/PathFinder.hpp"
#include "entity/Entity.hpp"
#include "world/Level.hpp"
#include "world/tile/Tile.hpp"
#include "util/Mth.hpp"

namespace mc {

PathFinder::PathFinder(Level* level)
    : level(level)
{
    neighbors.resize(32);
}

std::unique_ptr<Path> PathFinder::findPath(Entity* entity, Entity* target, float maxDist) {
    return findPath(entity, target->x, target->bb.y0, target->z, maxDist);
}

std::unique_ptr<Path> PathFinder::findPath(Entity* entity, int targetX, int targetY, int targetZ, float maxDist) {
    return findPath(entity, static_cast<double>(targetX) + 0.5, static_cast<double>(targetY) + 0.5, static_cast<double>(targetZ) + 0.5, maxDist);
}

std::unique_ptr<Path> PathFinder::findPath(Entity* entity, double targetX, double targetY, double targetZ, float maxDist) {
    openSet.clear();
    nodes.clear();

    Node* start = getOrCreateNode(
        Mth::floor(entity->bb.x0),
        Mth::floor(entity->bb.y0),
        Mth::floor(entity->bb.z0)
    );

    Node* goal = getOrCreateNode(
        Mth::floor(targetX - entity->bbWidth / 2.0f),
        Mth::floor(targetY),
        Mth::floor(targetZ - entity->bbWidth / 2.0f)
    );

    Node size(
        Mth::floor(entity->bbWidth + 1.0f),
        Mth::floor(entity->bbHeight + 1.0f),
        Mth::floor(entity->bbWidth + 1.0f)
    );

    return findPathInternal(entity, start, goal, &size, maxDist);
}

std::unique_ptr<Path> PathFinder::findPathInternal(Entity* entity, Node* start, Node* goal, Node* size, float maxDist) {
    start->g = 0.0f;
    start->h = start->distanceTo(*goal);
    start->f = start->h;

    openSet.clear();
    openSet.insert(start);
    Node* closest = start;

    while (!openSet.isEmpty()) {
        Node* current = openSet.pop();

        if (current->hash == goal->hash) {
            return reconstructPath(start, goal);
        }

        if (current->distanceTo(*goal) < closest->distanceTo(*goal)) {
            closest = current;
        }

        current->closed = true;
        int neighborCount = getNeighbors(entity, current, size, goal, maxDist);

        for (int i = 0; i < neighborCount; ++i) {
            Node* neighbor = neighbors[i];
            float tentativeG = current->g + current->distanceTo(*neighbor);

            if (!neighbor->inOpenSet() || tentativeG < neighbor->g) {
                neighbor->cameFrom = current;
                neighbor->g = tentativeG;
                neighbor->h = neighbor->distanceTo(*goal);

                if (neighbor->inOpenSet()) {
                    openSet.changeCost(neighbor, neighbor->g + neighbor->h);
                } else {
                    neighbor->f = neighbor->g + neighbor->h;
                    openSet.insert(neighbor);
                }
            }
        }
    }

    // If we couldn't reach the goal, return path to closest point
    if (closest == start) {
        return nullptr;
    }
    return reconstructPath(start, closest);
}

int PathFinder::getNeighbors(Entity* entity, Node* current, Node* size, Node* goal, float maxDist) {
    int count = 0;
    int stepUp = 0;

    if (isFree(entity, current->x, current->y + 1, current->z, size) > 0) {
        stepUp = 1;
    }

    // Check 4 cardinal directions
    Node* n1 = getNode(entity, current->x, current->y, current->z + 1, size, stepUp);
    Node* n2 = getNode(entity, current->x - 1, current->y, current->z, size, stepUp);
    Node* n3 = getNode(entity, current->x + 1, current->y, current->z, size, stepUp);
    Node* n4 = getNode(entity, current->x, current->y, current->z - 1, size, stepUp);

    if (n1 && !n1->closed && n1->distanceTo(*goal) < maxDist) {
        neighbors[count++] = n1;
    }
    if (n2 && !n2->closed && n2->distanceTo(*goal) < maxDist) {
        neighbors[count++] = n2;
    }
    if (n3 && !n3->closed && n3->distanceTo(*goal) < maxDist) {
        neighbors[count++] = n3;
    }
    if (n4 && !n4->closed && n4->distanceTo(*goal) < maxDist) {
        neighbors[count++] = n4;
    }

    return count;
}

Node* PathFinder::getNode(Entity* entity, int x, int y, int z, Node* size, int stepUp) {
    Node* node = nullptr;

    if (isFree(entity, x, y, z, size) > 0) {
        node = getOrCreateNode(x, y, z);
    }

    if (node == nullptr && isFree(entity, x, y + stepUp, z, size) > 0) {
        node = getOrCreateNode(x, y + stepUp, z);
        y += stepUp;
    }

    if (node != nullptr) {
        int fallCount = 0;
        int freeResult;

        // Check for falling down
        while (y > 0 && (freeResult = isFree(entity, x, y - 1, z, size)) > 0) {
            if (freeResult < 0) {
                return nullptr;  // Would fall into liquid
            }
            ++fallCount;
            if (fallCount >= 4) {
                return nullptr;  // Too far to fall
            }
            --y;
        }

        if (y > 0) {
            node = getOrCreateNode(x, y, z);
        }
    }

    return node;
}

Node* PathFinder::getOrCreateNode(int x, int y, int z) {
    int hash = x | (y << 10) | (z << 20);
    auto it = nodes.find(hash);
    if (it != nodes.end()) {
        return it->second.get();
    }

    auto node = std::make_unique<Node>(x, y, z);
    Node* ptr = node.get();
    nodes[hash] = std::move(node);
    return ptr;
}

int PathFinder::isFree(Entity* /*entity*/, int x, int y, int z, Node* size) {
    for (int bx = x; bx < x + size->x; ++bx) {
        for (int by = y; by < y + size->y; ++by) {
            for (int bz = z; bz < z + size->z; ++bz) {
                int tileId = level->getTile(bx, by, bz);
                if (tileId > 0) {
                    Tile* tile = Tile::tiles[tileId];
                    if (tile) {
                        // Check if the tile blocks motion (solid blocks)
                        if (tile->solid) {
                            return 0;  // Blocked
                        }
                        // Check for liquids
                        if (tileId == Tile::WATER || tileId == Tile::STILL_WATER ||
                            tileId == Tile::LAVA || tileId == Tile::STILL_LAVA) {
                            return -1;  // Liquid
                        }
                    }
                }
            }
        }
    }
    return 1;  // Free
}

std::unique_ptr<Path> PathFinder::reconstructPath(Node* /*start*/, Node* end) {
    // Count path length
    int length = 1;
    for (Node* n = end; n->cameFrom != nullptr; n = n->cameFrom) {
        ++length;
    }

    // Build path array
    std::vector<std::unique_ptr<Node>> pathNodes;
    pathNodes.resize(length);

    Node* current = end;
    int idx = length - 1;
    while (current != nullptr) {
        pathNodes[idx] = std::make_unique<Node>(current->x, current->y, current->z);
        current = current->cameFrom;
        --idx;
    }

    return std::make_unique<Path>(std::move(pathNodes));
}

} // namespace mc
