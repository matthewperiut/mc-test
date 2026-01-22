#include "particle/ParticleEngine.hpp"
#include "particle/Particle.hpp"
#include "particle/ExplodeParticle.hpp"
#include "particle/FlameParticle.hpp"
#include "particle/SmokeParticle.hpp"
#include "entity/Entity.hpp"
#include "world/Level.hpp"
#include "renderer/Textures.hpp"
#include "renderer/Tesselator.hpp"
#include "renderer/ShaderManager.hpp"
#include "renderer/backend/RenderDevice.hpp"
#include "util/Mth.hpp"
#include <cmath>

namespace mc {

ParticleEngine::ParticleEngine()
    : level(nullptr)
{
}

ParticleEngine::~ParticleEngine() {
    clear();
}

void ParticleEngine::setLevel(Level* newLevel) {
    level = newLevel;
    clear();
}

void ParticleEngine::add(std::unique_ptr<Particle> particle) {
    if (!particle) return;
    int texType = particle->getParticleTexture();
    if (texType >= 0 && texType < TEXTURE_COUNT) {
        particles[texType].push_back(std::move(particle));
    }
}

void ParticleEngine::addParticle(const std::string& name, double x, double y, double z,
                                  double xa, double ya, double za) {
    if (!level) return;

    if (name == "explode") {
        add(std::make_unique<ExplodeParticle>(level, x, y, z, xa, ya, za));
    } else if (name == "smoke") {
        add(std::make_unique<SmokeParticle>(level, x, y, z, xa, ya, za));
    } else if (name == "flame") {
        add(std::make_unique<FlameParticle>(level, x, y, z, xa, ya, za));
    }
}

void ParticleEngine::tick() {
    for (int tt = 0; tt < TEXTURE_COUNT; ++tt) {
        for (auto it = particles[tt].begin(); it != particles[tt].end();) {
            (*it)->tick();
            if ((*it)->removed) {
                it = particles[tt].erase(it);
            } else {
                ++it;
            }
        }
    }
}

void ParticleEngine::render(Entity* player, float partialTick) {
    if (!player) return;

    // Calculate camera orientation vectors for billboarding
    float yRotRad = player->yRot * Mth::DEG_TO_RAD;
    float xRotRad = player->xRot * Mth::DEG_TO_RAD;

    float xa = Mth::cos(yRotRad);
    float za = Mth::sin(yRotRad);
    float xa2 = -za * Mth::sin(xRotRad);
    float za2 = xa * Mth::sin(xRotRad);
    float ya = Mth::cos(xRotRad);

    // Enable blending for particles
    RenderDevice::get().setBlend(true, BlendFactor::SrcAlpha, BlendFactor::OneMinusSrcAlpha);

    ShaderManager::getInstance().useWorldShader();
    ShaderManager::getInstance().setAlphaTest(0.01f);
    ShaderManager::getInstance().updateMatrices();  // Important: update shader matrices

    Tesselator& t = Tesselator::getInstance();

    // Render each texture group
    for (int tt = 0; tt < TEXTURE_COUNT; ++tt) {
        if (particles[tt].empty()) continue;

        // Bind appropriate texture
        if (tt == MISC_TEXTURE) {
            Textures::getInstance().bind("resources/particles.png", 0, false);  // No mipmaps for particles
        } else if (tt == TERRAIN_TEXTURE) {
            Textures::getInstance().bind("resources/terrain.png");  // Terrain uses mipmaps
        } else if (tt == ITEM_TEXTURE) {
            Textures::getInstance().bind("resources/gui/items.png", 0, false);  // No mipmaps for items
        }

        t.begin(DrawMode::Quads);

        for (const auto& particle : particles[tt]) {
            // Calculate texture coordinates (16x16 grid)
            float u0 = static_cast<float>(particle->tex % 16) / 16.0f;
            float u1 = u0 + 0.0624375f;  // ~1/16 - small gap to avoid bleeding
            float v0 = static_cast<float>(particle->tex / 16) / 16.0f;
            float v1 = v0 + 0.0624375f;

            // Particle size (uses virtual method for animated sizes)
            float r = 0.1f * particle->getRenderSize(partialTick);

            // Interpolated position in WORLD space (shader handles camera transform)
            float px = static_cast<float>(particle->prevX + (particle->x - particle->prevX) * partialTick);
            float py = static_cast<float>(particle->prevY + (particle->y - particle->prevY) * partialTick);
            float pz = static_cast<float>(particle->prevZ + (particle->z - particle->prevZ) * partialTick);

            // Get brightness
            float br = particle->getBrightness();

            // Set color with brightness
            t.color(particle->rCol * br, particle->gCol * br, particle->bCol * br, particle->alpha);

            // Render billboard quad (4 vertices) in world space
            // The billboard always faces the camera using the orientation vectors
            t.tex(u1, v1);
            t.vertex(px - xa * r - xa2 * r, py - ya * r, pz - za * r - za2 * r);

            t.tex(u1, v0);
            t.vertex(px - xa * r + xa2 * r, py + ya * r, pz - za * r + za2 * r);

            t.tex(u0, v0);
            t.vertex(px + xa * r + xa2 * r, py + ya * r, pz + za * r + za2 * r);

            t.tex(u0, v1);
            t.vertex(px + xa * r - xa2 * r, py - ya * r, pz + za * r - za2 * r);
        }

        t.end();
    }

    // Restore state
    RenderDevice::get().setBlend(false);
}

int ParticleEngine::getParticleCount() const {
    int count = 0;
    for (int tt = 0; tt < TEXTURE_COUNT; ++tt) {
        count += static_cast<int>(particles[tt].size());
    }
    return count;
}

void ParticleEngine::clear() {
    for (int tt = 0; tt < TEXTURE_COUNT; ++tt) {
        particles[tt].clear();
    }
}

} // namespace mc
