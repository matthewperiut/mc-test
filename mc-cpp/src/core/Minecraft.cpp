#include "core/Minecraft.hpp"
#include "world/Level.hpp"
#include "world/tile/Tile.hpp"
#include "entity/LocalPlayer.hpp"
#include "entity/Chicken.hpp"
#include "renderer/LevelRenderer.hpp"
#include "renderer/GameRenderer.hpp"
#include "renderer/Tesselator.hpp"
#include "renderer/Textures.hpp"
#include "renderer/ShaderManager.hpp"
#include "audio/SoundEngine.hpp"
#include "gui/Gui.hpp"
#include "gui/Screen.hpp"
#include "gui/InventoryScreen.hpp"
#include "gui/PauseScreen.hpp"
#include "gamemode/GameMode.hpp"
#include "item/Inventory.hpp"
#include "item/Item.hpp"
#include "phys/Vec3.hpp"
#include "phys/AABB.hpp"

#include <GL/glew.h>
#include <GLFW/glfw3.h>
#include <iostream>
#include <chrono>
#include <cstdlib>

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
    , ticks(0)
    , lastClickTick(0)
    , isBreakingBlock(false)
    , breakingX(-1), breakingY(-1), breakingZ(-1)
    , breakingFace(0)
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

#ifdef __linux__
    // Set Wayland compatibility flag before GLFW init
    // This tells GLEW to work with both X11 and Wayland
    setenv("LIBGL_ALWAYS_INDIRECT", "1", 0);
#endif

    // Initialize GLFW
    glfwSetErrorCallback(glfwErrorCallback);
    if (!glfwInit()) {
        std::cerr << "Failed to initialize GLFW" << std::endl;
        return false;
    }

    // Create window with OpenGL 3.3 core profile
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
#ifdef __APPLE__
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
#endif

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

    // Load options
    options.load("options.txt");

    // Apply vsync setting from options
    glfwSwapInterval(options.vsync ? 1 : 0);

    // Initialize GLEW
    // Note: glewInit() may fail on Wayland due to missing X11/EGL detection,
    // but the context is still valid. We suppress the error and continue.
    glewExperimental = GL_TRUE;
    GLenum glewErr = glewInit();

    if (glewErr != GLEW_OK && glewErr != GLEW_ERROR_NO_GLX_DISPLAY) {
        std::cerr << "GLEW initialization warning: " << glewGetErrorString(glewErr) << std::endl;
        // Continue anyway - the context may still be valid on Wayland
    }

    // Initialize OpenGL
    initGL();

    // Initialize mouse handler
    mouseHandler.init(window);

    // Initialize tiles
    Tile::initTiles();

    // Initialize items
    Item::initItems();

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

    // Note: GL_TEXTURE_2D is always enabled in core profile (no need to call glEnable)

    glClearColor(0.5f, 0.8f, 1.0f, 1.0f);

    // Initialize shaders
    ShaderManager::getInstance().init();
}

void Minecraft::initWorld() {
    // Create level (128x128x128 for faster loading)
    createLevel(128, 128, 128);

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

    // Spawn test chickens near spawn
    for (int i = 0; i < 5; ++i) {
        auto chicken = std::make_unique<Chicken>(level.get());
        chicken->setPos(
            level->spawnX + (i - 2) * 2.0,  // Spread chickens out
            level->spawnY,
            level->spawnZ + 5.0  // In front of player
        );
        level->addEntity(std::move(chicken));
    }

    // Update level renderer
    if (levelRenderer) {
        levelRenderer->setLevel(level.get());
    }
}

void Minecraft::run() {
    auto lastTime = std::chrono::high_resolution_clock::now();

    while (running && !glfwWindowShouldClose(window)) {
        auto currentTime = std::chrono::high_resolution_clock::now();
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

        // Render (freeze partialTick when paused to prevent jitter)
        render(paused ? 0.0f : timer.partialTick);

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

    ticks++;  // Increment tick counter (Java: this.ticks++)

    // Process block breaking once per tick (not per frame)
    // This matches Java's tick-based destruction progress
    if (isBreakingBlock && gameMode && player) {
        static int lastSwingTick = 0;

        // Continue destroying the block (once per tick)
        gameMode->continueDestroyBlock(breakingX, breakingY, breakingZ, breakingFace);

        // Periodic swing animation every 5 ticks (Java: ticksPerSecond / 4.0F)
        if ((ticks - lastSwingTick) >= 5) {
            player->swing();
            lastSwingTick = ticks;
        }
    }

    // Tick level
    if (level) {
        level->tick();
    }

    // Tick particles
    if (levelRenderer) {
        levelRenderer->tickParticles();
    }

    // Tick game mode
    if (gameMode) {
        gameMode->tick();
    }

    // Tick GUI
    if (gui) {
        gui->tick();
    }

    // Tick game renderer (for item switch animation)
    if (gameRenderer) {
        gameRenderer->tick();
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
        // Scale mouse coordinates by GUI scale
        int scale = gui->getGuiScale();
        int scaledMouseX = static_cast<int>(mouseHandler.getX()) / scale;
        int scaledMouseY = static_cast<int>(mouseHandler.getY()) / scale;
        currentScreen->render(scaledMouseX, scaledMouseY, partialTick);
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

    // Handle mouse click actions - track state for tick-based block breaking
    // Only process when no screen is open and mouse is grabbed
    if (player && level && gameMode && gameRenderer && !currentScreen && mouseHandler.isGrabbed()) {
        static bool wasBreaking = false;
        bool leftMouseDown = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;

        if (leftMouseDown && gameRenderer->hitResult.isTile()) {
            int x = gameRenderer->hitResult.x;
            int y = gameRenderer->hitResult.y;
            int z = gameRenderer->hitResult.z;
            int face = static_cast<int>(gameRenderer->hitResult.face);

            if (!wasBreaking) {
                // Just started breaking - trigger swing animation and start
                player->swing();
                gameMode->startDestroyBlock(x, y, z, face);
                isBreakingBlock = true;
                breakingX = x;
                breakingY = y;
                breakingZ = z;
                breakingFace = face;
            } else {
                // Update target block (in case player moved aim)
                breakingX = x;
                breakingY = y;
                breakingZ = z;
                breakingFace = face;
            }

            // Track block position for crack animation
            levelRenderer->destroyX = x;
            levelRenderer->destroyY = y;
            levelRenderer->destroyZ = z;
        } else if (leftMouseDown && gameRenderer->hitResult.isEntity()) {
            // Left-click on entity - attack
            if (!wasBreaking) {
                // Only attack on first click (not held)
                gameMode->attack(player, gameRenderer->hitResult.entity);
            }
            isBreakingBlock = false;
        } else if (leftMouseDown && !wasBreaking) {
            // Left-click in air (no tile hit) - still trigger swing
            player->swing();
            isBreakingBlock = false;
        } else if (wasBreaking && !leftMouseDown) {
            // Stopped breaking
            gameMode->stopDestroyBlock();
            levelRenderer->destroyProgress = 0.0f;
            isBreakingBlock = false;
        } else if (!leftMouseDown) {
            isBreakingBlock = false;
        }

        wasBreaking = leftMouseDown;

        // Right click - place block
        // Java has TWO code paths:
        // 1. Mouse.getEventButton() == 1 && Mouse.getEventButtonState() -> immediate on NEW click
        // 2. Mouse.isButtonDown(1) && (ticks - lastClickTick) >= 5 -> repeat while HELD
        static bool wasRightPressed = false;
        bool isRightPressed = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS;
        bool justClicked = isRightPressed && !wasRightPressed;  // New click event
        bool heldLongEnough = isRightPressed && (ticks - lastClickTick) >= 5;  // Held repeat

        // Place block on new click (immediate) OR held repeat (5 tick cooldown)
        if (justClicked || heldLongEnough) {
            if (gameRenderer->hitResult.isTile() && player->inventory) {
                // Get currently selected item
                ItemStack& held = player->inventory->getSelected();
                if (!held.isEmpty() && held.id > 0 && held.id < 256) {
                    int x = gameRenderer->hitResult.x;
                    int y = gameRenderer->hitResult.y;
                    int z = gameRenderer->hitResult.z;

                    // Offset by face direction (matching Java TileItem.useOn)
                    switch (gameRenderer->hitResult.face) {
                        case Direction::DOWN:  y--; break;
                        case Direction::UP:    y++; break;
                        case Direction::NORTH: z--; break;
                        case Direction::SOUTH: z++; break;
                        case Direction::WEST:  x--; break;
                        case Direction::EAST:  x++; break;
                    }

                    // Check if block can be placed (Java: level.mayPlace())
                    // This checks for entity collisions (can't place inside player)
                    // Also check tile-specific mayPlace (e.g., torches need adjacent solid block)
                    Tile* tileToPlace = Tile::tiles[held.id];
                    bool canPlace = level->mayPlace(held.id, x, y, z, false);
                    if (canPlace && tileToPlace) {
                        canPlace = tileToPlace->mayPlace(level.get(), x, y, z);
                    }

                    if (canPlace) {
                        // Only swing if block actually gets placed (differs from Java)
                        player->swing();

                        // Place the block
                        level->setTile(x, y, z, held.id);

                        // Call tile's onPlace for auto-detection of orientation
                        if (tileToPlace) {
                            tileToPlace->onPlace(level.get(), x, y, z);
                            // Call setPlacedOnFace to honor the clicked face (for torches etc.)
                            int face = static_cast<int>(gameRenderer->hitResult.face);
                            tileToPlace->setPlacedOnFace(level.get(), x, y, z, face);
                        }

                        // Play place sound (matching Java TileItem line 57)
                        // Java: level.playSound(x+0.5, y+0.5, z+0.5, soundType.getStepSound(), (volume+1.0)/2.0, pitch*0.8)
                        Tile* placedTile = Tile::tiles[held.id];
                        if (placedTile) {
                            std::string soundName = placedTile->stepSound.empty() ? "step.stone" : "step." + placedTile->stepSound;
                            SoundEngine::getInstance().playSound3D(
                                soundName,
                                x + 0.5f, y + 0.5f, z + 0.5f,
                                (placedTile->stepSoundVolume + 1.0f) / 2.0f,
                                placedTile->stepSoundPitch * 0.8f
                            );
                        }

                        // Trigger item-placed animation (hand goes down then up)
                        gameRenderer->itemPlaced();

                        // Consume item (unless in creative mode)
                        if (!player->creative) {
                            held.remove(1);
                        }
                    }
                    lastClickTick = ticks;  // Record when we clicked/placed
                }
            }
        }
        wasRightPressed = isRightPressed;
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
                // Open pause menu (matching Java)
                if (mouseHandler.isGrabbed()) {
                    setScreen(new PauseScreen());
                    paused = true;
                }
                break;

            case GLFW_KEY_F3:
                showDebug = !showDebug;
                break;

            case GLFW_KEY_F11:
                // Toggle fullscreen
                break;

            case GLFW_KEY_E:
                // Open inventory (matching Java)
                if (mouseHandler.isGrabbed()) {
                    releaseMouse();
                    setScreen(new InventoryScreen());
                }
                break;

            case GLFW_KEY_N:
                // Toggle noclip mode
                if (player) {
                    player->noClip = !player->noClip;
                    player->flying = player->noClip;  // Enable flying with noclip
                }
                break;

            // Number keys 1-9 for hotbar slot selection (matching Java)
            case GLFW_KEY_1: if (player && player->inventory) { player->selectedSlot = 0; player->inventory->selected = 0; } break;
            case GLFW_KEY_2: if (player && player->inventory) { player->selectedSlot = 1; player->inventory->selected = 1; } break;
            case GLFW_KEY_3: if (player && player->inventory) { player->selectedSlot = 2; player->inventory->selected = 2; } break;
            case GLFW_KEY_4: if (player && player->inventory) { player->selectedSlot = 3; player->inventory->selected = 3; } break;
            case GLFW_KEY_5: if (player && player->inventory) { player->selectedSlot = 4; player->inventory->selected = 4; } break;
            case GLFW_KEY_6: if (player && player->inventory) { player->selectedSlot = 5; player->inventory->selected = 5; } break;
            case GLFW_KEY_7: if (player && player->inventory) { player->selectedSlot = 6; player->inventory->selected = 6; } break;
            case GLFW_KEY_8: if (player && player->inventory) { player->selectedSlot = 7; player->inventory->selected = 7; } break;
            case GLFW_KEY_9: if (player && player->inventory) { player->selectedSlot = 8; player->inventory->selected = 8; } break;
        }
    }

    // Forward to player
    if (player && (action == GLFW_PRESS || action == GLFW_RELEASE)) {
        player->setKey(key, action == GLFW_PRESS);
    }
}

void Minecraft::onMouseButton(int button, int action, int mods) {
    (void)mods;

    // If a screen is open, forward clicks to it (don't grab mouse)
    if (currentScreen) {
        currentScreen->mouseClicked(button, action);
        return;
    }

    // No screen open - handle mouse grab
    if (!mouseHandler.isGrabbed()) {
        if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
            grabMouse();
            paused = false;
        }
    }
}

void Minecraft::onMouseMove(double x, double y) {
    if (currentScreen) {
        // Scale mouse coordinates by GUI scale
        int scale = gui ? gui->getGuiScale() : 1;
        currentScreen->mouseMoved(x / scale, y / scale);
    }
}

void Minecraft::onMouseScroll(double xoffset, double yoffset) {
    if (currentScreen) {
        currentScreen->mouseScrolled(xoffset, yoffset);
    }

    // Change selected hotbar slot (Java: scroll down = next slot, scroll up = prev slot)
    if (player && player->inventory && !currentScreen && mouseHandler.isGrabbed()) {
        int scroll = static_cast<int>(yoffset);
        player->selectedSlot -= scroll;
        // Wrap around
        while (player->selectedSlot < 0) player->selectedSlot += 9;
        while (player->selectedSlot > 8) player->selectedSlot -= 9;
        // Sync with inventory
        player->inventory->selected = player->selectedSlot;
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
        delete currentScreen;
    }

    currentScreen = screen;

    if (screen) {
        // Use scaled dimensions for screen
        int scaledWidth = gui ? gui->getScaledWidth() : screenWidth;
        int scaledHeight = gui ? gui->getScaledHeight() : screenHeight;
        screen->init(this, scaledWidth, scaledHeight);
        releaseMouse();

        // Stop any ongoing block breaking
        if (gameMode) {
            gameMode->stopDestroyBlock();
        }
        if (levelRenderer) {
            levelRenderer->destroyProgress = 0.0f;
        }
    }
}

void Minecraft::closeScreen() {
    setScreen(nullptr);
    grabMouse();
    paused = false;
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
    // Match Java: release all keys when mouse is released
    if (player) {
        player->releaseAllKeys();
    }
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
    Item::destroyItems();
    Tile::destroyTiles();

    // Destroy window
    if (window) {
        glfwDestroyWindow(window);
        window = nullptr;
    }

    glfwTerminate();
}

} // namespace mc
