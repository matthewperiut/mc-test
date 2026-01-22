#pragma once

#include "phys/AABB.hpp"
#include <unordered_map>
#include <unordered_set>
#include <vector>
#include <functional>

namespace mc {

class Entity;

/**
 * Grid-based spatial hash for efficient entity queries.
 * Uses 16-block cells to accelerate getEntitiesInArea queries from O(n) to O(cells + entities).
 */
class SpatialHash {
public:
    static constexpr int CELL_SIZE = 16;

    SpatialHash() = default;

    // Insert an entity based on its bounding box
    void insert(Entity* entity);

    // Remove an entity from all cells it occupies
    void remove(Entity* entity);

    // Update an entity's position in the hash (call after entity moves)
    void update(Entity* entity, const AABB& oldBB);

    // Query all entities that might intersect with the given area
    std::vector<Entity*> query(const AABB& area) const;

    // Clear all entries
    void clear();

private:
    struct CellKey {
        int x, y, z;

        bool operator==(const CellKey& other) const {
            return x == other.x && y == other.y && z == other.z;
        }
    };

    struct CellKeyHash {
        std::size_t operator()(const CellKey& key) const {
            // Simple hash combining function
            std::size_t h = 17;
            h = h * 31 + std::hash<int>()(key.x);
            h = h * 31 + std::hash<int>()(key.y);
            h = h * 31 + std::hash<int>()(key.z);
            return h;
        }
    };

    // Get cell coordinates for a world position
    static CellKey getCellKey(double x, double y, double z);

    // Get all cell keys that an AABB overlaps
    static std::vector<CellKey> getCellsForAABB(const AABB& bb);

    // Insert entity into a specific cell
    void insertIntoCell(const CellKey& key, Entity* entity);

    // Remove entity from a specific cell
    void removeFromCell(const CellKey& key, Entity* entity);

    std::unordered_map<CellKey, std::unordered_set<Entity*>, CellKeyHash> cells;
};

} // namespace mc
