#include "gamemode/GameMode.hpp"
#include "core/Minecraft.hpp"
#include "world/Level.hpp"
#include "world/tile/Tile.hpp"
#include "entity/Entity.hpp"
#include "entity/Player.hpp"
#include "entity/LocalPlayer.hpp"
#include "entity/ItemEntity.hpp"
#include "gui/Gui.hpp"
#include "renderer/LevelRenderer.hpp"
#include "audio/SoundEngine.hpp"
#include "util/Mth.hpp"

namespace mc {

// =============================================================================
// GameMode (Base class)
// =============================================================================

GameMode::GameMode(Minecraft* minecraft)
    : minecraft(minecraft)
    , instaBuild(false)
    , xDestroyBlock(-1)
    , yDestroyBlock(-1)
    , zDestroyBlock(-1)
    , destroyProgress(0.0f)
    , oDestroyProgress(0.0f)
    , destroyTicks(0.0f)
    , destroyDelay(0)
{
}

void GameMode::initLevel(Level* /*level*/) {
}

void GameMode::initPlayer(Player* /*player*/) {
}

void GameMode::startDestroyBlock(int x, int y, int z, int face) {
    // Base implementation - instant destroy
    destroyBlock(x, y, z, face);
}

void GameMode::continueDestroyBlock(int /*x*/, int /*y*/, int /*z*/, int /*face*/) {
    // Base implementation does nothing
}

void GameMode::stopDestroyBlock() {
    destroyProgress = 0.0f;
    destroyDelay = 0;
}

bool GameMode::destroyBlock(int x, int y, int z, int /*face*/) {
    if (!minecraft->level) return false;

    Level* level = minecraft->level.get();
    int tileId = level->getTile(x, y, z);
    if (tileId <= 0) return false;

    Tile* tile = Tile::tiles[tileId].get();

    // Set to air
    bool changed = level->setTile(x, y, z, 0);

    if (tile && changed) {
        // Play break sound (matching Java GameMode line 33)
        // Java: soundEngine.play(oldTile.soundType.getBreakSound(), x+0.5, y+0.5, z+0.5, (volume+1.0)/2.0, pitch*0.8)
        // getBreakSound() returns "step." + name by default, but glass returns "random.glass", sand returns "step.gravel"
        std::string soundName;
        if (tile->id == Tile::GLASS) {
            soundName = "random.glass";
        } else if (tile->id == Tile::SAND) {
            soundName = "step.gravel";
        } else {
            soundName = tile->stepSound.empty() ? "step.stone" : "step." + tile->stepSound;
        }
        SoundEngine::getInstance().playSound3D(
            soundName,
            x + 0.5f, y + 0.5f, z + 0.5f,
            (tile->stepSoundVolume + 1.0f) / 2.0f,
            tile->stepSoundPitch * 0.8f
        );

        // Tile destroy callback
        tile->destroy(level, x, y, z, 0);
    }

    return changed;
}

void GameMode::tick() {
}

void GameMode::render(float /*partialTick*/) {
}

float GameMode::getPickRange() const {
    return 5.0f;
}

bool GameMode::canHurtPlayer() const {
    return true;
}

void GameMode::attack(Player* player, Entity* target) {
    // Match Java GameMode.attack() - forward to player
    if (player && target) {
        player->attack(target);
    }
}

void GameMode::interact(Player* player, Entity* target) {
    // Match Java GameMode.interact() - forward to player
    if (player && target) {
        player->interact(target);
    }
}

// =============================================================================
// SurvivalMode - Time-based block breaking matching Java exactly
// =============================================================================

SurvivalMode::SurvivalMode(Minecraft* minecraft)
    : GameMode(minecraft)
{
}

void SurvivalMode::initPlayer(Player* player) {
    player->yRot = -180.0f;
}

void SurvivalMode::startDestroyBlock(int x, int y, int z, int face) {
    if (!minecraft->level) return;

    int tileId = minecraft->level->getTile(x, y, z);
    if (tileId <= 0) return;

    Tile* tile = Tile::tiles[tileId].get();
    if (!tile) return;

    // Attack tile (e.g., note block plays sound)
    if (destroyProgress == 0.0f) {
        tile->attack(minecraft->level.get(), x, y, z, minecraft->player);
    }

    // Initialize target block coordinates
    xDestroyBlock = x;
    yDestroyBlock = y;
    zDestroyBlock = z;
    destroyProgress = 0.0f;
    destroyTicks = 0.0f;

    // Check for instant break (like flowers, torches)
    // Java: if (t > 0 && Tile.tiles[t].getDestroyProgress(this.minecraft.player) >= 1.0F)
    float breakSpeed = tile->getDestroyProgress(minecraft->player);
    if (breakSpeed >= 1.0f) {
        destroyBlock(x, y, z, face);
        destroyDelay = 5;
    }
}

void SurvivalMode::continueDestroyBlock(int x, int y, int z, int face) {
    // Match Java SurvivalMode.continueDestroyBlock() exactly

    // Delay after breaking a block (Java: this.destroyDelay = 5)
    if (destroyDelay > 0) {
        destroyDelay--;
        return;
    }

    if (x == xDestroyBlock && y == yDestroyBlock && z == zDestroyBlock) {
        // Same block - continue breaking
        if (!minecraft->level) return;

        int tileId = minecraft->level->getTile(x, y, z);
        if (tileId <= 0) return;

        Tile* tile = Tile::tiles[tileId].get();
        if (!tile) return;

        // Progress break (Java: this.destroyProgress += tile.getDestroyProgress(this.minecraft.player))
        destroyProgress += tile->getDestroyProgress(minecraft->player);

        // Play dig sound every 4 ticks
        // Java: if (this.destroyTicks % 4.0F == 0.0F)
        if (static_cast<int>(destroyTicks) % 4 == 0) {
            // Play step sound at reduced volume (matching Java: (volume + 1.0) / 8.0, pitch * 0.5)
            std::string soundName = tile->stepSound.empty() ? "step.stone" : "step." + tile->stepSound;
            SoundEngine::getInstance().playSound3D(
                soundName,
                x + 0.5f, y + 0.5f, z + 0.5f,
                (tile->stepSoundVolume + 1.0f) / 8.0f,
                tile->stepSoundPitch * 0.5f
            );
        }

        destroyTicks++;

        // Check if block broken
        // Java: if (this.destroyProgress >= 1.0F)
        if (destroyProgress >= 1.0f) {
            destroyBlock(x, y, z, face);
            destroyProgress = 0.0f;
            oDestroyProgress = 0.0f;
            destroyTicks = 0.0f;
            destroyDelay = 5;  // Java: this.destroyDelay = 5
        }
    } else {
        // Different block - reset progress
        destroyProgress = 0.0f;
        oDestroyProgress = 0.0f;
        destroyTicks = 0.0f;
        xDestroyBlock = x;
        yDestroyBlock = y;
        zDestroyBlock = z;
    }
}

void SurvivalMode::stopDestroyBlock() {
    destroyProgress = 0.0f;
    destroyDelay = 0;
}

bool SurvivalMode::destroyBlock(int x, int y, int z, int face) {
    if (!minecraft->level) return false;

    Level* level = minecraft->level.get();
    int tileId = level->getTile(x, y, z);
    if (tileId <= 0) return false;

    Tile* tile = Tile::tiles[tileId].get();
    int data = level->getData(x, y, z);

    // Get drop info before destroying
    int dropId = tile ? tile->getDropId() : 0;
    int dropCount = tile ? tile->getDropCount() : 0;

    // Call parent destroy
    bool changed = GameMode::destroyBlock(x, y, z, face);

    // Handle drops
    if (tile && changed && dropId > 0 && dropCount > 0) {
        // Spawn dropped item at block center with random offset
        double dropX = x + 0.5 + (Mth::random() - 0.5) * 0.3;
        double dropY = y + 0.5 + (Mth::random() - 0.5) * 0.3;
        double dropZ = z + 0.5 + (Mth::random() - 0.5) * 0.3;

        auto itemEntity = std::make_unique<ItemEntity>(
            level, dropX, dropY, dropZ, dropId, dropCount, data
        );
        level->addEntity(std::move(itemEntity));
    }

    // Handle tool durability and special drops
    if (minecraft->player && tile && changed) {
        tile->playerDestroy(level, x, y, z, data);
    }

    return changed;
}

void SurvivalMode::tick() {
    // Store previous progress for interpolation
    oDestroyProgress = destroyProgress;
}

void SurvivalMode::render(float partialTick) {
    // Update GUI and LevelRenderer with break progress
    // Java: this.minecraft.gui.progress = dp; this.minecraft.levelRenderer.destroyProgress = dp;

    if (destroyProgress <= 0.0f) {
        if (minecraft->gui) minecraft->gui->progress = 0.0f;
        if (minecraft->levelRenderer) minecraft->levelRenderer->destroyProgress = 0.0f;
    } else {
        // Interpolate for smooth animation
        float dp = oDestroyProgress + (destroyProgress - oDestroyProgress) * partialTick;
        if (minecraft->gui) minecraft->gui->progress = dp;
        if (minecraft->levelRenderer) minecraft->levelRenderer->destroyProgress = dp;
    }
}

float SurvivalMode::getPickRange() const {
    return 4.0f;  // Survival reach is 4 blocks
}

// =============================================================================
// CreativeMode - Instant breaking
// =============================================================================

CreativeMode::CreativeMode(Minecraft* minecraft)
    : GameMode(minecraft)
{
    instaBuild = true;
}

void CreativeMode::initPlayer(Player* player) {
    player->flying = true;
    player->creative = true;
}

void CreativeMode::startDestroyBlock(int x, int y, int z, int face) {
    // Creative mode has instant breaking
    destroyBlock(x, y, z, face);
}

float CreativeMode::getPickRange() const {
    return 5.0f;  // Creative reach is 5 blocks
}

bool CreativeMode::canHurtPlayer() const {
    return false;  // Creative mode doesn't take damage
}

} // namespace mc
