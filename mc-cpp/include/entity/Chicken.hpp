#pragma once

#include "entity/Animal.hpp"

namespace mc {

class Chicken : public Animal {
public:
    // Wing flapping animation
    float flap = 0.0f;
    float flapSpeed = 0.0f;
    float oFlap = 0.0f;
    float oFlapSpeed = 0.0f;
    float flapping = 1.0f;

    // Egg laying timer
    int eggTime;

    Chicken(Level* level);
    virtual ~Chicken() = default;

    void aiStep() override;

    // Chickens don't take fall damage
    void causeFallDamage(float /*distance*/) override {}

    // Sounds
    std::string getAmbientSound() override { return "mob.chicken"; }
    std::string getHurtSound() override { return "mob.chickenhurt"; }
    std::string getDeathSound() override { return "mob.chickenhurt"; }

    // Drop feathers on death
    int getDeathLoot() override;

    // Texture
    std::string getTexture() override { return "/mob/chicken.png"; }
};

} // namespace mc
