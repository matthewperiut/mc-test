#include "core/Minecraft.hpp"
#include "world/Level.hpp"
#include "world/tile/Tile.hpp"
#include "entity/LocalPlayer.hpp"
#include "renderer/LevelRenderer.hpp"
#include "renderer/GameRenderer.hpp"
#include "renderer/Tesselator.hpp"
#include "renderer/Textures.hpp"
#include "audio/SoundEngine.hpp"
#include "gui/Gui.hpp"
#include "gui/Screen.hpp"
#include "gamemode/GameMode.hpp"
#include "phys/Vec3.hpp"
#include "phys/AABB.hpp"

#include <GL/glew.h>
#include <GLFW/glfw3.h>
#include <iostream>
#include <chrono>

namespace mc {

const std::string Minecraft::VERSION = "Beta 1.2_02 (C++ Port)";

Minecraft* minecraft = nullptr;

// GLFW callbacks
static void glfwErrorCallback(int error, const char* description) {
    std::cerr << "GLFW Error " << error << ": " << description << std::endl;
}

static void glfwKeyCallback(GLFWwindow* window, int key, int scancode, int action, int mods) {
    Minecraft* mc = static_cast<Minecraft*>(glfwGetWindowUserPointer(window));
    if (mc) mc->onKeyPress(key, scancode, action, mods);
}

static void glfwMouseButtonCallback(GLFWwindow* window, int button, int action, int mods) {
    Minecraft* mc = static_cast<Minecraft*>(glfwGetWindowUserPointer(window));
    if (mc) mc->onMouseButton(button, action, mods);
}

static void glfwCursorPosCallback(GLFWwindow* window, double x, double y) {
    Minecraft* mc = static_cast<Minecraft*>(glfwGetWindowUserPointer(window));
    if (mc) mc->onMouseMove(x, y);
}

static void glfwScrollCallback(GLFWwindow* window, double xoffset, double yoffset) {
    Minecraft* mc = static_cast<Minecraft*>(glfwGetWindowUserPointer(window));
    if (mc) mc->onMouseScroll(xoffset, yoffset);
}

static void glfwFramebufferSizeCallback(GLFWwindow* window, int width, int height) {
    Minecraft* mc = static_cast<Minecraft*>(glfwGetWindowUserPointer(window));
    if (mc) mc->onResize(width, height);
}

Minecraft::Minecraft()
    : window(nullptr)
    , screenWidth(854)
    , screenHeight(480)
    , framebufferWidth(854)
    , framebufferHeight(480)
    , fullscreen(false)
    , running(false)
    , paused(false)
    , inGame(false)
    , player(nullptr)
    , currentScreen(nullptr)
    , fps(0)
    , fpsCounter(0)
    , lastFpsTime(0)
    , showDebug(false)
{
    minecraft = this;
}

Minecraft::~Minecraft() {
    shutdown();
    minecraft = nullptr;
}

bool Minecraft::init(int width, int height, bool fs) {
    screenWidth = width;
    screenHeight = height;
    fullscreen = fs;

    // Initialize GLFW
    glfwSetErrorCallback(glfwErrorCallback);
    if (!glfwInit()) {
        std::cerr << "Failed to initialize GLFW" << std::endl;
        return false;
    }

    // Create window
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);

    GLFWmonitor* monitor = fullscreen ? glfwGetPrimaryMonitor() : nullptr;
    window = glfwCreateWindow(screenWidth, screenHeight, "Minecraft C++", monitor, nullptr);

    if (!window) {
        std::cerr << "Failed to create GLFW window" << std::endl;
        glfwTerminate();
        return false;
    }

    glfwMakeContextCurrent(window);
    glfwSetWindowUserPointer(window, this);

    // Get actual framebuffer size (important for HiDPI displays)
    glfwGetFramebufferSize(window, &framebufferWidth, &framebufferHeight);

    // Set callbacks
    glfwSetKeyCallback(window, glfwKeyCallback);
    glfwSetMouseButtonCallback(window, glfwMouseButtonCallback);
    glfwSetCursorPosCallback(window, glfwCursorPosCallback);
    glfwSetScrollCallback(window, glfwScrollCallback);
    glfwSetFramebufferSizeCallback(window, glfwFramebufferSizeCallback);

    // Enable vsync
    glfwSwapInterval(1);

    // Initialize GLEW
    glewExperimental = GL_TRUE;
    if (glewInit() != GLEW_OK) {
        std::cerr << "Failed to initialize GLEW" << std::endl;
        return false;
    }

    // Initialize OpenGL
    initGL();

    // Initialize mouse handler
    mouseHandler.init(window);

    // Initialize tiles
    Tile::initTiles();

    // Initialize textures
    Textures::getInstance().init();

    // Initialize tesselator
    Tesselator::getInstance().init();

    // Initialize audio
    SoundEngine::getInstance().init();

    // Initialize GUI
    gui = std::make_unique<Gui>(this);
    gui->init();

    // Initialize world
    initWorld();

    // Grab mouse for gameplay
    grabMouse();

    running = true;
    inGame = true;

    std::cout << "Minecraft C++ initialized successfully!" << std::endl;
    std::cout << "OpenGL Version: " << glGetString(GL_VERSION) << std::endl;

    return true;
}

void Minecraft::initGL() {
    // Use framebuffer size for viewport (HiDPI support)
    glViewport(0, 0, framebufferWidth, framebufferHeight);

    glEnable(GL_DEPTH_TEST);
    glDepthFunc(GL_LEQUAL);

    glEnable(GL_CULL_FACE);
    glCullFace(GL_BACK);

    glEnable(GL_TEXTURE_2D);

    glClearColor(0.5f, 0.8f, 1.0f, 1.0f);
}

void Minecraft::initWorld() {
    // Create level
    createLevel(256, 128, 256);

    // Create game mode (survival by default)
    gameMode = std::make_unique<SurvivalMode>(this);

    // Create renderers
    gameRenderer = std::make_unique<GameRenderer>(this);
    levelRenderer = std::make_unique<LevelRenderer>(this, level.get());

    // Use framebuffer size for renderer (HiDPI support)
    gameRenderer->resize(framebufferWidth, framebufferHeight);
}

void Minecraft::createLevel(int width, int height, int depth) {
    // Create new level
    level = std::make_unique<Level>(width, height, depth, 12345);
    level->generateFlatWorld();

    // Create player
    auto playerEntity = std::make_unique<LocalPlayer>(level.get(), this);
    player = playerEntity.get();

    // Spawn player
    player->setPos(
        level->spawnX + 0.5,
        level->spawnY,
        level->spawnZ + 0.5
    );

    level->addEntity(std::move(playerEntity));

    // Update level renderer
    if (levelRenderer) {
        levelRenderer->setLevel(level.get());
    }
}

void Minecraft::run() {
    auto lastTime = std::chrono::high_resolution_clock::now();

    while (running && !glfwWindowShouldClose(window)) {
        auto currentTime = std::chrono::high_resolution_clock::now();
        auto deltaTime = std::chrono::duration<double>(currentTime - lastTime).count();
        lastTime = currentTime;

        // Poll events
        glfwPollEvents();

        // Handle input
        handleInput();

        // Advance timer
        timer.advanceTime();

        // Tick game logic
        for (int i = 0; i < timer.ticks; i++) {
            tick();
        }

        // Render
        render(timer.partialTick);

        // Swap buffers
        glfwSwapBuffers(window);

        // Update FPS counter
        updateFps();

        // Reset object pools
        Vec3::resetPool();
        AABB::resetPool();
    }
}

void Minecraft::tick() {
    if (paused) return;

    // Tick level
    if (level) {
        level->tick();
    }

    // Tick game mode
    if (gameMode) {
        gameMode->tick();
    }

    // Tick GUI
    if (gui) {
        gui->tick();
    }

    // Tick screen
    if (currentScreen) {
        currentScreen->tick();
    }
}

void Minecraft::render(float partialTick) {
    // Update game mode render state (for break progress)
    if (gameMode) {
        gameMode->render(partialTick);
    }

    // Render game
    if (gameRenderer && inGame) {
        gameRenderer->render(partialTick);
    }

    // Render GUI
    if (gui && inGame) {
        gui->render(partialTick);
    }

    // Render screen overlay
    if (currentScreen) {
        currentScreen->render(
            static_cast<int>(mouseHandler.getX()),
            static_cast<int>(mouseHandler.getY()),
            partialTick
        );
    }
}

void Minecraft::handleInput() {
    mouseHandler.poll();

    if (!paused && player && mouseHandler.isGrabbed()) {
        // Update player look
        int dx = mouseHandler.getDeltaX();
        int dy = mouseHandler.getDeltaY();

        if (dx != 0 || dy != 0) {
            float sensitivity = options.mouseSensitivity * 0.6f + 0.2f;
            float yawDelta = dx * sensitivity;
            float pitchDelta = dy * sensitivity * (options.invertYMouse ? -1.0f : 1.0f);

            player->turn(yawDelta, pitchDelta);
        }

        // Update player movement
        player->handleInput(window);
    }

    // Handle mouse click actions - time-based block breaking
    if (player && level && gameMode && gameRenderer) {
        static bool wasBreaking = false;
        bool isBreaking = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;

        if (isBreaking && gameRenderer->hitResult.isTile()) {
            int x = gameRenderer->hitResult.x;
            int y = gameRenderer->hitResult.y;
            int z = gameRenderer->hitResult.z;
            int face = static_cast<int>(gameRenderer->hitResult.face);

            if (!wasBreaking) {
                // Just started breaking
                gameMode->startDestroyBlock(x, y, z, face);
            } else {
                // Continue breaking
                gameMode->continueDestroyBlock(x, y, z, face);
            }

            // Track block position for crack animation
            levelRenderer->destroyX = x;
            levelRenderer->destroyY = y;
            levelRenderer->destroyZ = z;
        } else if (wasBreaking && !isBreaking) {
            // Stopped breaking
            gameMode->stopDestroyBlock();
            levelRenderer->destroyProgress = 0.0f;
        }

        wasBreaking = isBreaking;

        // Right click - place block (with cooldown)
        static int placeCooldown = 0;
        if (placeCooldown > 0) placeCooldown--;

        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS && placeCooldown == 0) {
            if (gameRenderer->hitResult.isTile()) {
                int x = gameRenderer->hitResult.x;
                int y = gameRenderer->hitResult.y;
                int z = gameRenderer->hitResult.z;

                // Offset by face direction
                switch (gameRenderer->hitResult.face) {
                    case Direction::DOWN:  y--; break;
                    case Direction::UP:    y++; break;
                    case Direction::NORTH: z--; break;
                    case Direction::SOUTH: z++; break;
                    case Direction::WEST:  x--; break;
                    case Direction::EAST:  x++; break;
                }

                player->placeBlock(x, y, z, 0, Tile::STONE);
                placeCooldown = 4;  // 4 tick cooldown
            }
        }
    }
}

void Minecraft::onKeyPress(int key, int scancode, int action, int mods) {
    if (currentScreen) {
        currentScreen->keyPressed(key, scancode, action, mods);
        return;
    }

    if (action == GLFW_PRESS) {
        switch (key) {
            case GLFW_KEY_ESCAPE:
                if (mouseHandler.isGrabbed()) {
                    releaseMouse();
                    paused = true;
                } else {
                    grabMouse();
                    paused = false;
                }
                break;

            case GLFW_KEY_F3:
                showDebug = !showDebug;
                break;

            case GLFW_KEY_F11:
                // Toggle fullscreen
                break;
        }
    }

    // Forward to player
    if (player && action == GLFW_PRESS || action == GLFW_RELEASE) {
        player->setKey(key, action == GLFW_PRESS);
    }
}

void Minecraft::onMouseButton(int button, int action, int mods) {
    (void)mods;

    if (!mouseHandler.isGrabbed()) {
        if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
            grabMouse();
            paused = false;
        }
        return;
    }

    if (currentScreen) {
        currentScreen->mouseClicked(button, action);
    }
}

void Minecraft::onMouseMove(double x, double y) {
    if (currentScreen) {
        currentScreen->mouseMoved(x, y);
    }
}

void Minecraft::onMouseScroll(double xoffset, double yoffset) {
    if (currentScreen) {
        currentScreen->mouseScrolled(xoffset, yoffset);
    }

    // Change selected hotbar slot
    if (player && !currentScreen) {
        player->selectedSlot -= static_cast<int>(yoffset);
        if (player->selectedSlot < 0) player->selectedSlot = 8;
        if (player->selectedSlot > 8) player->selectedSlot = 0;
    }
}

void Minecraft::onResize(int width, int height) {
    // The framebuffer size callback gives us the actual pixel dimensions
    framebufferWidth = width;
    framebufferHeight = height;

    // Get the window size for UI calculations
    glfwGetWindowSize(window, &screenWidth, &screenHeight);

    // Use framebuffer size for OpenGL viewport
    glViewport(0, 0, framebufferWidth, framebufferHeight);

    if (gameRenderer) {
        gameRenderer->resize(framebufferWidth, framebufferHeight);
    }
}

void Minecraft::setScreen(Screen* screen) {
    if (currentScreen) {
        currentScreen->removed();
    }

    currentScreen = screen;

    if (screen) {
        screen->init(this, screenWidth, screenHeight);
        releaseMouse();
    }
}

void Minecraft::closeScreen() {
    setScreen(nullptr);
    grabMouse();
}

void Minecraft::pauseGame() {
    paused = true;
    releaseMouse();
}

void Minecraft::resumeGame() {
    paused = false;
    grabMouse();
}

void Minecraft::grabMouse() {
    mouseHandler.grab();
}

void Minecraft::releaseMouse() {
    mouseHandler.release();
}

void Minecraft::updateFps() {
    fpsCounter++;

    auto now = std::chrono::steady_clock::now();
    long currentMs = std::chrono::duration_cast<std::chrono::milliseconds>(
        now.time_since_epoch()
    ).count();

    if (currentMs - lastFpsTime >= 1000) {
        fps = static_cast<float>(fpsCounter);
        fpsCounter = 0;
        lastFpsTime = currentMs;
    }
}

void Minecraft::loadLevel(const std::string& /*path*/) {
    // Would load level from file
}

void Minecraft::saveLevel(const std::string& /*path*/) {
    // Would save level to file
}

void Minecraft::shutdown() {
    std::cout << "Shutting down..." << std::endl;

    // Release resources
    currentScreen = nullptr;
    gui.reset();
    gameMode.reset();
    gameRenderer.reset();
    levelRenderer.reset();
    level.reset();
    player = nullptr;

    // Shutdown systems
    SoundEngine::getInstance().destroy();
    Textures::getInstance().destroy();
    Tesselator::getInstance().destroy();
    Tile::destroyTiles();

    // Destroy window
    if (window) {
        glfwDestroyWindow(window);
        window = nullptr;
    }

    glfwTerminate();
}

} // namespace mc
