#pragma once

#include "phys/AABB.hpp"
#include <random>

namespace mc {

class Level;

class Particle {
public:
    // Position
    double x, y, z;
    double prevX, prevY, prevZ;

    // Velocity
    double xd, yd, zd;

    // Bounding box
    AABB bb;
    float bbWidth = 0.2f;
    float bbHeight = 0.2f;

    // State
    bool onGround = false;
    bool removed = false;

    // Particle properties
    int tex = 0;           // Texture index in 16x16 grid
    float uo = 0.0f;       // Texture U offset
    float vo = 0.0f;       // Texture V offset
    int age = 0;           // Current age in ticks
    int lifetime = 0;      // Total lifespan in ticks
    float size = 1.0f;     // Particle size
    float gravity = 0.0f;  // Gravity multiplier

    // Color (RGB)
    float rCol = 1.0f;
    float gCol = 1.0f;
    float bCol = 1.0f;
    float alpha = 1.0f;

    // Camera offset for rendering (set by ParticleEngine)
    static double xOff;
    static double yOff;
    static double zOff;

    // Reference to level
    Level* level;

    // Random generator
    std::mt19937 random;

    Particle(Level* level, double x, double y, double z, double xa, double ya, double za);
    virtual ~Particle() = default;

    // Set power multiplier for velocity
    Particle& setPower(float power);

    // Set size scale
    Particle& scale(float scale);

    // Update
    virtual void tick();

    // Movement
    void move(double dx, double dy, double dz);

    // Set position
    void setPos(double x, double y, double z);

    // Get brightness at current position
    float getBrightness() const;

    // Texture type (0 = misc/particles.png, 1 = terrain.png, 2 = items.png)
    virtual int getParticleTexture() const { return 0; }

    // Remove particle
    void remove() { removed = true; }

protected:
    void updateBoundingBox();
};

} // namespace mc
