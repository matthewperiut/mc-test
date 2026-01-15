#pragma once

#include <vector>
#include <memory>
#include <string>

namespace mc {

class Level;
class Particle;
class Entity;

class ParticleEngine {
public:
    // Texture type constants
    static constexpr int MISC_TEXTURE = 0;     // particles.png
    static constexpr int TERRAIN_TEXTURE = 1;  // terrain.png
    static constexpr int ITEM_TEXTURE = 2;     // gui/items.png
    static constexpr int TEXTURE_COUNT = 3;

    ParticleEngine();
    ~ParticleEngine();

    // Set level reference
    void setLevel(Level* level);

    // Add particle
    void add(std::unique_ptr<Particle> particle);

    // Add particle by name (convenience method for Level::addParticle)
    void addParticle(const std::string& name, double x, double y, double z,
                     double xa, double ya, double za);

    // Update all particles
    void tick();

    // Render all particles
    void render(Entity* player, float partialTick);

    // Get particle count
    int getParticleCount() const;

    // Clear all particles
    void clear();

private:
    Level* level;

    // Particles grouped by texture type
    std::vector<std::unique_ptr<Particle>> particles[TEXTURE_COUNT];
};

} // namespace mc
