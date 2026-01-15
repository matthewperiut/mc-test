#include "entity/Mob.hpp"
#include "entity/Player.hpp"
#include "world/Level.hpp"
#include "world/tile/Tile.hpp"
#include "audio/SoundEngine.hpp"
#include "util/Mth.hpp"
#include <cmath>

namespace mc {

Mob::Mob(Level* level)
    : Entity(level)
    , random(std::random_device{}())
{
    blocksBuilding = true;
    rotA = (static_cast<float>(random()) / random.max() + 1.0f) * 0.01f;
    setPos(x, y, z);
    timeOffs = static_cast<float>(random()) / random.max() * 12398.0f;
    yRot = static_cast<float>(random()) / random.max() * Mth::PI * 2.0f;
    stepHeight = 0.5f;
}

void Mob::baseTick() {
    oAttackAnim = attackAnim;
    Entity::baseTick();

    // Ambient sounds
    if (static_cast<int>(random() % 1000) < ambientSoundTime++) {
        ambientSoundTime = -getAmbientSoundInterval();
        std::string sound = getAmbientSound();
        if (!sound.empty() && level) {
            float pitch = (static_cast<float>(random()) / random.max() - static_cast<float>(random()) / random.max()) * 0.2f + 1.0f;
            playSound(sound, getSoundVolume(), pitch);
        }
    }

    // Update hurt time
    if (attackTime > 0) --attackTime;
    if (hurtTime > 0) --hurtTime;
    if (invulnerableTime > 0) --invulnerableTime;

    // Death handling
    if (health <= 0) {
        ++deathTime;
        if (deathTime > 20) {
            // Spawn death particles (matching Java Mob.baseTick lines 165-170)
            if (level) {
                std::normal_distribution<double> gaussian(0.0, 1.0);
                auto randFloat = [this]() { return static_cast<float>(random()) / random.max(); };

                for (int i = 0; i < 20; ++i) {
                    double xa = gaussian(random) * 0.02;
                    double ya = gaussian(random) * 0.02;
                    double za = gaussian(random) * 0.02;

                    // Random position within entity bounding box
                    double px = x + (randFloat() * bbWidth * 2.0f) - bbWidth;
                    double py = y + (randFloat() * bbHeight);
                    double pz = z + (randFloat() * bbWidth * 2.0f) - bbWidth;

                    level->addParticle("explode", px, py, pz, xa, ya, za);
                }
            }

            removed = true;
        }
    }

    animStepO = animStep;
    yBodyRotO = yBodyRot;
    prevYRot = yRot;
    prevXRot = xRot;
}

void Mob::tick() {
    Entity::tick();
    aiStep();

    // Update body rotation based on movement
    double dx = x - prevX;
    double dz = z - prevZ;
    float dist = Mth::sqrt(static_cast<float>(dx * dx + dz * dz));
    float targetBodyRot = yBodyRot;
    float runDelta = 0.0f;
    oRun = run;
    float runInput = 0.0f;

    if (dist > 0.05f) {
        runInput = 1.0f;
        runDelta = dist * 3.0f;
        targetBodyRot = static_cast<float>(std::atan2(dz, dx)) * Mth::RAD_TO_DEG - 90.0f;
    }

    if (attackAnim > 0.0f) {
        targetBodyRot = yRot;
    }

    if (!onGround) {
        runInput = 0.0f;
    }

    run += (runInput - run) * 0.3f;

    // Smooth body rotation
    float bodyDelta = targetBodyRot - yBodyRot;
    while (bodyDelta < -180.0f) bodyDelta += 360.0f;
    while (bodyDelta >= 180.0f) bodyDelta -= 360.0f;
    yBodyRot += bodyDelta * 0.3f;

    // Limit head-body rotation difference
    float headDelta = yRot - yBodyRot;
    while (headDelta < -180.0f) headDelta += 360.0f;
    while (headDelta >= 180.0f) headDelta -= 360.0f;

    bool backwards = headDelta < -90.0f || headDelta >= 90.0f;
    if (headDelta < -75.0f) headDelta = -75.0f;
    if (headDelta >= 75.0f) headDelta = 75.0f;

    yBodyRot = yRot - headDelta;
    if (headDelta * headDelta > 2500.0f) {
        yBodyRot += headDelta * 0.2f;
    }

    if (backwards) {
        runDelta *= -1.0f;
    }

    // Normalize rotations
    while (yRot - prevYRot < -180.0f) prevYRot -= 360.0f;
    while (yRot - prevYRot >= 180.0f) prevYRot += 360.0f;
    while (yBodyRot - yBodyRotO < -180.0f) yBodyRotO -= 360.0f;
    while (yBodyRot - yBodyRotO >= 180.0f) yBodyRotO += 360.0f;
    while (xRot - prevXRot < -180.0f) prevXRot -= 360.0f;
    while (xRot - prevXRot >= 180.0f) prevXRot += 360.0f;

    animStep += runDelta;
}

void Mob::aiStep() {
    if (health <= 0) {
        jumping = false;
        xxa = 0.0f;
        yya = 0.0f;
        yRotA = 0.0f;
    } else {
        updateAi();
    }

    // Handle jumping
    bool inWaterCheck = isInWater();
    bool inLavaCheck = isInLava();

    if (jumping) {
        if (inWaterCheck) {
            yd += 0.04;
        } else if (inLavaCheck) {
            yd += 0.04;
        } else if (onGround) {
            jumpFromGround();
        }
    }

    xxa *= 0.98f;
    yya *= 0.98f;
    yRotA *= 0.9f;
    travel(xxa, yya);

    // Push nearby entities
    if (level) {
        auto entities = level->getEntitiesInArea(bb.expand(0.2, 0.0, 0.2));
        for (Entity* ent : entities) {
            if (ent != this && ent->isPushable()) {
                // Calculate push direction
                double pushX = ent->x - x;
                double pushZ = ent->z - z;
                double dist = std::sqrt(pushX * pushX + pushZ * pushZ);
                if (dist > 0.01) {
                    pushX /= dist;
                    pushZ /= dist;
                    double pushStrength = 0.05 / dist;
                    if (pushStrength > 1.0) pushStrength = 1.0;
                    ent->push(pushX * pushStrength, 0, pushZ * pushStrength);
                    push(-pushX * pushStrength, 0, -pushZ * pushStrength);
                }
            }
        }
    }
}

void Mob::updateAi() {
    ++noActionTime;

    // Random looking behavior
    xxa = 0.0f;
    yya = 0.0f;
    float lookRange = 8.0f;

    if (static_cast<float>(random()) / random.max() < 0.02f) {
        // Occasionally look at nearby player
        if (level) {
            Entity* nearest = nullptr;
            double nearestDist = lookRange * lookRange;
            for (Player* p : level->players) {
                double d = distanceToSqr(p->x, p->y, p->z);
                if (d < nearestDist) {
                    nearestDist = d;
                    nearest = p;
                }
            }
            if (nearest) {
                lookingAt = nearest;
                lookTime = 10 + static_cast<int>(random() % 20);
            } else {
                yRotA = (static_cast<float>(random()) / random.max() - 0.5f) * 20.0f;
            }
        }
    }

    if (lookingAt != nullptr) {
        lookAt(*lookingAt, 10.0f);
        if (--lookTime <= 0 || lookingAt->removed || distanceToSqr(lookingAt->x, lookingAt->y, lookingAt->z) > lookRange * lookRange) {
            lookingAt = nullptr;
        }
    } else {
        if (static_cast<float>(random()) / random.max() < 0.05f) {
            yRotA = (static_cast<float>(random()) / random.max() - 0.5f) * 20.0f;
        }
        yRot += yRotA;
        xRot = defaultLookAngle;
    }

    // Jump in water/lava
    bool inWaterCheck = isInWater();
    bool inLavaCheck = isInLava();
    if (inWaterCheck || inLavaCheck) {
        jumping = static_cast<float>(random()) / random.max() < 0.8f;
    }
}

void Mob::travel(float strafe, float forward) {
    if (isInWater()) {
        double startY = y;
        moveRelative(strafe, forward, 0.02f);
        move(xd, yd, zd);
        xd *= 0.8;
        yd *= 0.8;
        zd *= 0.8;
        yd -= 0.02;
        if (horizontalCollision && level) {
            // Try to climb out of water
            if (y > startY + 0.6 - y + startY) {
                yd = 0.3;
            }
        }
    } else if (isInLava()) {
        double startY = y;
        moveRelative(strafe, forward, 0.02f);
        move(xd, yd, zd);
        xd *= 0.5;
        yd *= 0.5;
        zd *= 0.5;
        yd -= 0.02;
        if (horizontalCollision && level) {
            if (y > startY + 0.6 - y + startY) {
                yd = 0.3;
            }
        }
    } else {
        float friction = 0.91f;
        if (onGround) {
            friction = 0.546f;  // 0.6 * 0.91
            if (level) {
                int blockX = static_cast<int>(std::floor(x));
                int blockY = static_cast<int>(std::floor(bb.y0)) - 1;
                int blockZ = static_cast<int>(std::floor(z));
                int tileId = level->getTile(blockX, blockY, blockZ);
                if (tileId > 0 && Tile::tiles[tileId]) {
                    friction = Tile::tiles[tileId]->friction * 0.91f;
                }
            }
        }

        float accel = 0.16277136f / (friction * friction * friction);
        moveRelative(strafe, forward, onGround ? 0.1f * accel : 0.02f);

        friction = 0.91f;
        if (onGround && level) {
            int blockX = static_cast<int>(std::floor(x));
            int blockY = static_cast<int>(std::floor(bb.y0)) - 1;
            int blockZ = static_cast<int>(std::floor(z));
            int tileId = level->getTile(blockX, blockY, blockZ);
            if (tileId > 0 && Tile::tiles[tileId]) {
                friction = Tile::tiles[tileId]->friction * 0.91f;
            }
        }

        move(xd, yd, zd);

        // Gravity
        yd -= 0.08;
        yd *= 0.98;
        xd *= friction;
        zd *= friction;
    }

    // Walking animation
    walkAnimSpeedO = walkAnimSpeed;
    double moveDx = x - prevX;
    double moveDz = z - prevZ;
    float moveSpeed = Mth::sqrt(static_cast<float>(moveDx * moveDx + moveDz * moveDz)) * 4.0f;
    if (moveSpeed > 1.0f) moveSpeed = 1.0f;
    walkAnimSpeed += (moveSpeed - walkAnimSpeed) * 0.4f;
    walkAnimPos += walkAnimSpeed;
}

void Mob::jumpFromGround() {
    yd = 0.42;
}

void Mob::hurt(Entity* source, int damage) {
    if (health <= 0) return;

    walkAnimSpeed = 1.5f;
    bool validHit = true;

    if (invulnerableTime > invulnerableDuration / 2.0f) {
        if (damage <= lastHurt) {
            return;
        }
        actuallyHurt(damage - lastHurt);
        lastHurt = damage;
        validHit = false;
    } else {
        lastHurt = damage;
        lastHealth = health;
        invulnerableTime = invulnerableDuration;
        actuallyHurt(damage);
        hurtTime = hurtDuration = 10;
    }

    hurtDir = 0.0f;
    if (validHit) {
        if (source != nullptr) {
            double dx = source->x - x;
            double dz = source->z - z;
            while (dx * dx + dz * dz < 0.0001) {
                dx = (static_cast<double>(random()) / random.max() - static_cast<double>(random()) / random.max()) * 0.01;
                dz = (static_cast<double>(random()) / random.max() - static_cast<double>(random()) / random.max()) * 0.01;
            }
            hurtDir = static_cast<float>(std::atan2(dz, dx)) * Mth::RAD_TO_DEG - yRot;
            knockback(source, damage, dx, dz);
        } else {
            hurtDir = static_cast<float>(static_cast<int>(random() % 2) * 180);
        }
    }

    if (health <= 0) {
        if (validHit) {
            playSound(getDeathSound(), getSoundVolume(), (static_cast<float>(random()) / random.max() - static_cast<float>(random()) / random.max()) * 0.2f + 1.0f);
        }
        die(source);
    } else if (validHit) {
        playSound(getHurtSound(), getSoundVolume(), (static_cast<float>(random()) / random.max() - static_cast<float>(random()) / random.max()) * 0.2f + 1.0f);
    }
}

void Mob::actuallyHurt(int damage) {
    health -= damage;
}

void Mob::knockback(Entity* /*source*/, int /*damage*/, double dx, double dz) {
    float dist = Mth::sqrt(static_cast<float>(dx * dx + dz * dz));
    float knockbackStrength = 0.4f;

    xd /= 2.0;
    yd /= 2.0;
    zd /= 2.0;
    xd -= dx / dist * knockbackStrength;
    yd += 0.4;
    zd -= dz / dist * knockbackStrength;

    if (yd > 0.4) yd = 0.4;
}

void Mob::die(Entity* /*killer*/) {
    dead = true;
    dropDeathLoot();
}

void Mob::dropDeathLoot() {
    int loot = getDeathLoot();
    if (loot > 0 && level) {
        int count = static_cast<int>(random() % 3);
        for (int i = 0; i < count; ++i) {
            // Would spawn ItemEntity here
            // level->addEntity(std::make_unique<ItemEntity>(level, x, y, z, loot, 1));
        }
    }
}

void Mob::causeFallDamage(float distance) {
    int damage = static_cast<int>(std::ceil(distance - 3.0f));
    if (damage > 0) {
        hurt(nullptr, damage);
    }
}

bool Mob::canSee(const Entity& /*other*/) const {
    // Simplified - would need ray casting through level
    return true;
}

float Mob::getAttackAnim(float partialTick) const {
    float delta = attackAnim - oAttackAnim;
    if (delta < 0.0f) delta += 1.0f;
    return oAttackAnim + delta * partialTick;
}

void Mob::lookAt(Entity& target, float maxTurn) {
    double dx = target.x - x;
    double dz = target.z - z;
    double dy = target.y + target.eyeHeight - (y + eyeHeight);

    double horizDist = std::sqrt(dx * dx + dz * dz);
    float targetYaw = static_cast<float>(std::atan2(dz, dx)) * Mth::RAD_TO_DEG - 90.0f;
    float targetPitch = static_cast<float>(std::atan2(dy, horizDist)) * Mth::RAD_TO_DEG;

    xRot = -rotlerp(xRot, targetPitch, maxTurn);
    yRot = rotlerp(yRot, targetYaw, maxTurn);
}

float Mob::rotlerp(float current, float target, float maxDelta) {
    float delta = target - current;
    while (delta < -180.0f) delta += 360.0f;
    while (delta >= 180.0f) delta -= 360.0f;

    if (delta > maxDelta) delta = maxDelta;
    if (delta < -maxDelta) delta = -maxDelta;

    return current + delta;
}

} // namespace mc
