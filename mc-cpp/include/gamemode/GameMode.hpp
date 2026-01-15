#pragma once

namespace mc {

class Minecraft;
class Player;
class Level;
class Entity;

// Base game mode class matching Java GameMode
class GameMode {
public:
    Minecraft* minecraft;
    bool instaBuild;

    // Block breaking state (from SurvivalMode)
    int xDestroyBlock;
    int yDestroyBlock;
    int zDestroyBlock;
    float destroyProgress;
    float oDestroyProgress;
    float destroyTicks;
    int destroyDelay;

    GameMode(Minecraft* minecraft);
    virtual ~GameMode() = default;

    virtual void initLevel(Level* level);
    virtual void initPlayer(Player* player);

    // Block interaction - matching Java exactly
    virtual void startDestroyBlock(int x, int y, int z, int face);
    virtual void continueDestroyBlock(int x, int y, int z, int face);
    virtual void stopDestroyBlock();
    virtual bool destroyBlock(int x, int y, int z, int face);

    // Called each tick
    virtual void tick();

    // Called each frame for rendering progress
    virtual void render(float partialTick);

    // Get reach distance
    virtual float getPickRange() const;

    // Can player take damage
    virtual bool canHurtPlayer() const;

    // Get current block destroy progress (0.0 to 1.0)
    float getDestroyProgress() const { return destroyProgress; }

    // Entity interaction
    virtual void attack(Player* player, Entity* target);
    virtual void interact(Player* player, Entity* target);
};

// Survival mode with time-based block breaking
class SurvivalMode : public GameMode {
public:
    SurvivalMode(Minecraft* minecraft);

    void initPlayer(Player* player) override;
    void startDestroyBlock(int x, int y, int z, int face) override;
    void continueDestroyBlock(int x, int y, int z, int face) override;
    void stopDestroyBlock() override;
    bool destroyBlock(int x, int y, int z, int face) override;
    void tick() override;
    void render(float partialTick) override;
    float getPickRange() const override;
};

// Creative mode with instant breaking
class CreativeMode : public GameMode {
public:
    CreativeMode(Minecraft* minecraft);

    void initPlayer(Player* player) override;
    void startDestroyBlock(int x, int y, int z, int face) override;
    float getPickRange() const override;
    bool canHurtPlayer() const override;
};

} // namespace mc
