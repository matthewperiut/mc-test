#include "util/SpatialHash.hpp"
#include "entity/Entity.hpp"
#include <cmath>

namespace mc {

SpatialHash::CellKey SpatialHash::getCellKey(double x, double y, double z) {
    return {
        static_cast<int>(std::floor(x / CELL_SIZE)),
        static_cast<int>(std::floor(y / CELL_SIZE)),
        static_cast<int>(std::floor(z / CELL_SIZE))
    };
}

std::vector<SpatialHash::CellKey> SpatialHash::getCellsForAABB(const AABB& bb) {
    std::vector<CellKey> result;

    int minX = static_cast<int>(std::floor(bb.x0 / CELL_SIZE));
    int minY = static_cast<int>(std::floor(bb.y0 / CELL_SIZE));
    int minZ = static_cast<int>(std::floor(bb.z0 / CELL_SIZE));
    int maxX = static_cast<int>(std::floor(bb.x1 / CELL_SIZE));
    int maxY = static_cast<int>(std::floor(bb.y1 / CELL_SIZE));
    int maxZ = static_cast<int>(std::floor(bb.z1 / CELL_SIZE));

    for (int x = minX; x <= maxX; ++x) {
        for (int y = minY; y <= maxY; ++y) {
            for (int z = minZ; z <= maxZ; ++z) {
                result.push_back({x, y, z});
            }
        }
    }

    return result;
}

void SpatialHash::insertIntoCell(const CellKey& key, Entity* entity) {
    cells[key].insert(entity);
}

void SpatialHash::removeFromCell(const CellKey& key, Entity* entity) {
    auto it = cells.find(key);
    if (it != cells.end()) {
        it->second.erase(entity);
        if (it->second.empty()) {
            cells.erase(it);
        }
    }
}

void SpatialHash::insert(Entity* entity) {
    if (!entity) return;

    auto cellKeys = getCellsForAABB(entity->bb);
    for (const auto& key : cellKeys) {
        insertIntoCell(key, entity);
    }
}

void SpatialHash::remove(Entity* entity) {
    if (!entity) return;

    auto cellKeys = getCellsForAABB(entity->bb);
    for (const auto& key : cellKeys) {
        removeFromCell(key, entity);
    }
}

void SpatialHash::update(Entity* entity, const AABB& oldBB) {
    if (!entity) return;

    // Get old and new cell sets
    auto oldCells = getCellsForAABB(oldBB);
    auto newCells = getCellsForAABB(entity->bb);

    // Quick check: if same cells, no update needed
    if (oldCells.size() == 1 && newCells.size() == 1 && oldCells[0] == newCells[0]) {
        return;
    }

    // Remove from cells no longer occupied
    for (const auto& key : oldCells) {
        bool stillIn = false;
        for (const auto& newKey : newCells) {
            if (key == newKey) {
                stillIn = true;
                break;
            }
        }
        if (!stillIn) {
            removeFromCell(key, entity);
        }
    }

    // Add to newly occupied cells
    for (const auto& key : newCells) {
        bool wasIn = false;
        for (const auto& oldKey : oldCells) {
            if (key == oldKey) {
                wasIn = true;
                break;
            }
        }
        if (!wasIn) {
            insertIntoCell(key, entity);
        }
    }
}

std::vector<Entity*> SpatialHash::query(const AABB& area) const {
    std::vector<Entity*> result;
    std::unordered_set<Entity*> seen;

    auto cellKeys = getCellsForAABB(area);
    for (const auto& key : cellKeys) {
        auto it = cells.find(key);
        if (it != cells.end()) {
            for (Entity* entity : it->second) {
                // Deduplicate (entity might be in multiple cells)
                if (seen.insert(entity).second) {
                    // Actual intersection check
                    if (entity->bb.intersects(area)) {
                        result.push_back(entity);
                    }
                }
            }
        }
    }

    return result;
}

void SpatialHash::clear() {
    cells.clear();
}

} // namespace mc
