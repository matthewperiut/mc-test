#pragma once

#include "core/Timer.hpp"
#include "core/Options.hpp"
#include "core/MouseHandler.hpp"
#include <memory>
#include <string>

struct GLFWwindow;

namespace mc {

class Level;
class LocalPlayer;
class LevelRenderer;
class GameRenderer;
class Gui;
class Screen;
class SoundEngine;
class GameMode;

class Minecraft {
public:
    // Version
    static const std::string VERSION;

    // Window
    GLFWwindow* window;
    int screenWidth;
    int screenHeight;
    int framebufferWidth;   // Actual pixel dimensions (for HiDPI)
    int framebufferHeight;
    bool fullscreen;

    // Game state
    bool running;
    bool paused;
    bool inGame;

    // Core objects
    std::unique_ptr<Level> level;
    LocalPlayer* player;  // Owned by level
    std::unique_ptr<LevelRenderer> levelRenderer;
    std::unique_ptr<GameRenderer> gameRenderer;
    std::unique_ptr<Gui> gui;
    std::unique_ptr<GameMode> gameMode;
    Screen* currentScreen;

    // Input
    MouseHandler mouseHandler;

    // Settings
    Options options;

    // Timing
    Timer timer;
    float fps;
    int fpsCounter;
    long lastFpsTime;
    int ticks;           // Game tick counter (20 ticks/sec), matching Java Minecraft.ticks
    int lastClickTick;   // Last tick when mouse was clicked, for placement cooldown

    // Debug
    bool showDebug;

    // Constructor/Destructor
    Minecraft();
    ~Minecraft();

    // Lifecycle
    bool init(int width, int height, bool fullscreen);
    void run();
    void shutdown();

    // Per-frame updates
    void tick();
    void render(float partialTick);

    // Input handling
    void handleInput();
    void onKeyPress(int key, int scancode, int action, int mods);
    void onMouseButton(int button, int action, int mods);
    void onMouseMove(double x, double y);
    void onMouseScroll(double xoffset, double yoffset);
    void onResize(int width, int height);

    // Level management
    void createLevel(int width, int height, int depth);
    void loadLevel(const std::string& path);
    void saveLevel(const std::string& path);

    // Screen management
    void setScreen(Screen* screen);
    void closeScreen();

    // Pause
    void pauseGame();
    void resumeGame();

    // Utility
    void grabMouse();
    void releaseMouse();

private:
    void initGL();
    void initWorld();
    void updateFps();
    void processInput();
};

// Global instance
extern Minecraft* minecraft;

} // namespace mc
