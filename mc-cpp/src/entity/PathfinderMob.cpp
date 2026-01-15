#include "entity/PathfinderMob.hpp"
#include "world/Level.hpp"
#include "pathfinder/PathFinder.hpp"
#include "util/Mth.hpp"
#include <cmath>

namespace mc {

PathfinderMob::PathfinderMob(Level* level)
    : Mob(level)
{
}

void PathfinderMob::updateAi() {
    holdGround = false;
    float searchRange = 16.0f;

    // Look for attack target
    if (attackTarget == nullptr) {
        attackTarget = findAttackTarget();
        if (attackTarget != nullptr && level) {
            PathFinder finder(level);
            path = finder.findPath(this, attackTarget, searchRange);
        }
    } else if (!attackTarget->isAlive()) {
        attackTarget = nullptr;
    } else {
        float dist = static_cast<float>(distanceTo(*attackTarget));
        if (canSee(*attackTarget)) {
            checkHurtTarget(attackTarget, dist);
        }
    }

    // Random wandering when no target or holding ground
    if (holdGround || attackTarget == nullptr || (path != nullptr && static_cast<int>(random() % 20) != 0)) {
        if ((path == nullptr && static_cast<int>(random() % 80) == 0) || static_cast<int>(random() % 80) == 0) {
            bool foundTarget = false;
            int bestX = -1, bestY = -1, bestZ = -1;
            float bestValue = -99999.0f;

            for (int i = 0; i < 10; ++i) {
                int testX = Mth::floor(x + static_cast<float>(random() % 13) - 6.0f);
                int testY = Mth::floor(y + static_cast<float>(random() % 7) - 3.0f);
                int testZ = Mth::floor(z + static_cast<float>(random() % 13) - 6.0f);
                float value = getWalkTargetValue(testX, testY, testZ);
                if (value > bestValue) {
                    bestValue = value;
                    bestX = testX;
                    bestY = testY;
                    bestZ = testZ;
                    foundTarget = true;
                }
            }

            if (foundTarget && level) {
                PathFinder finder(level);
                path = finder.findPath(this, bestX, bestY, bestZ, 10.0f);
            }
        }
    } else if (level) {
        PathFinder finder(level);
        path = finder.findPath(this, attackTarget, searchRange);
    }

    // Follow path
    int groundY = Mth::floor(bb.y0);
    bool inWaterCheck = isInWater();
    bool inLavaCheck = isInLava();
    xRot = 0.0f;

    if (path != nullptr && static_cast<int>(random() % 100) != 0) {
        Vec3 target = path->current(*this);
        double widthSq = bbWidth * 2.0;

        while (target.x != 0 && target.distanceToSquared(Vec3(x, target.y, z)) < widthSq * widthSq) {
            path->next();
            if (path->isDone()) {
                target = Vec3(0, 0, 0);
                path = nullptr;
            } else {
                target = path->current(*this);
            }
        }

        jumping = false;
        if (target.x != 0) {
            double dx = target.x - x;
            double dz = target.z - z;
            double dy = target.y - static_cast<double>(groundY);

            float targetYaw = static_cast<float>(std::atan2(dz, dx)) * Mth::RAD_TO_DEG - 90.0f;
            float yawDelta = targetYaw - yRot;

            yya = runSpeed;

            while (yawDelta < -180.0f) yawDelta += 360.0f;
            while (yawDelta >= 180.0f) yawDelta -= 360.0f;

            if (yawDelta > 30.0f) yawDelta = 30.0f;
            if (yawDelta < -30.0f) yawDelta = -30.0f;

            yRot += yawDelta;

            // Face attack target while pathing
            if (holdGround && attackTarget != nullptr) {
                double aDx = attackTarget->x - x;
                double aDz = attackTarget->z - z;
                float storedYaw = yRot;
                yRot = static_cast<float>(std::atan2(aDz, aDx)) * Mth::RAD_TO_DEG - 90.0f;
                yawDelta = (storedYaw - yRot + 90.0f) * Mth::DEG_TO_RAD;
                xxa = -Mth::sin(yawDelta) * yya * 1.0f;
                yya = Mth::cos(yawDelta) * yya * 1.0f;
            }

            if (dy > 0.0) {
                jumping = true;
            }
        }

        if (attackTarget != nullptr) {
            lookAt(*attackTarget, 30.0f);
        }

        if (horizontalCollision) {
            jumping = true;
        }

        if (static_cast<float>(random()) / random.max() < 0.8f && (inWaterCheck || inLavaCheck)) {
            jumping = true;
        }
    } else {
        Mob::updateAi();
        path = nullptr;
    }
}

void PathfinderMob::checkHurtTarget(Entity* /*target*/, float /*distance*/) {
    // Override in subclasses for attack behavior
}

float PathfinderMob::getWalkTargetValue(int /*x*/, int /*y*/, int /*z*/) {
    return 0.0f;
}

Entity* PathfinderMob::findAttackTarget() {
    return nullptr;  // Override in hostile mobs
}

} // namespace mc
