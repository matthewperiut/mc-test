#include "entity/Chicken.hpp"
#include "entity/ItemEntity.hpp"
#include "world/Level.hpp"
#include "item/Item.hpp"

namespace mc {

Chicken::Chicken(Level* level)
    : Animal(level)
{
    textureName = "/mob/chicken.png";
    setSize(0.3f, 0.4f);
    health = 4;
    eggTime = static_cast<int>(random() % 6000) + 6000;

    // Shadow properties (matching Java ChickenRenderer)
    shadowRadius = 0.3f;
    shadowStrength = 1.0f;
}

void Chicken::aiStep() {
    Animal::aiStep();

    // Store previous flap values for interpolation
    oFlap = flap;
    oFlapSpeed = flapSpeed;

    // Update flap speed based on ground state
    // When airborne, flap speed increases; when grounded, it decreases
    flapSpeed += static_cast<float>(onGround ? -1 : 4) * 0.3f;

    // Clamp flap speed
    if (flapSpeed < 0.0f) {
        flapSpeed = 0.0f;
    }
    if (flapSpeed > 1.0f) {
        flapSpeed = 1.0f;
    }

    // Reset flapping when airborne
    if (!onGround && flapping < 1.0f) {
        flapping = 1.0f;
    }

    // Decay flapping
    flapping *= 0.9f;

    // Slow falling - chickens float down gently
    if (!onGround && yd < 0.0) {
        yd *= 0.6;
    }

    // Update flap animation
    flap += flapping * 2.0f;

    // Egg laying
    --eggTime;
    if (eggTime <= 0 && level) {
        // Play egg sound
        float pitch = (static_cast<float>(random()) / random.max() - static_cast<float>(random()) / random.max()) * 0.2f + 1.0f;
        playSound("mob.chickenplop", 1.0f, pitch);

        // TODO: Spawn egg item when egg item is added to Item class
        // For now, just reset timer
        // auto egg = std::make_unique<ItemEntity>(level, x, y, z, eggItemId, 1);
        // level->addEntity(std::move(egg));

        // Reset timer for next egg
        eggTime = static_cast<int>(random() % 6000) + 6000;
    }
}

int Chicken::getDeathLoot() {
    // Return feather item ID if available
    if (Item::feather) {
        return Item::feather->id;
    }
    return 0;  // No loot if feather not initialized
}

} // namespace mc
