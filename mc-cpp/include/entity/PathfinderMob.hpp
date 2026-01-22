#pragma once

#include "entity/Mob.hpp"
#include "pathfinder/Path.hpp"
#include <memory>

namespace mc {

class PathfinderMob : public Mob {
public:
    PathfinderMob(Level* level);
    virtual ~PathfinderMob() = default;

protected:
    std::unique_ptr<Path> path;
    Entity* attackTarget = nullptr;
    bool holdGround = false;

    // Async pathfinding state
    int pendingPathRequestId = -1;
    bool waitingForPath = false;

    void updateAi() override;

    // Check for completed async paths
    void pollAsyncPaths();

    virtual void checkHurtTarget(Entity* target, float distance);
    virtual float getWalkTargetValue(int x, int y, int z);
    virtual Entity* findAttackTarget();
};

} // namespace mc
