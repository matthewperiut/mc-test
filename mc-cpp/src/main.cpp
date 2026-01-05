#include "core/Minecraft.hpp"
#include <iostream>
#include <cstdlib>

int main(int argc, char* argv[]) {
    std::cout << "==================================" << std::endl;
    std::cout << "  Minecraft C++ Port" << std::endl;
    std::cout << "  Based on Minecraft Beta 1.2_02" << std::endl;
    std::cout << "==================================" << std::endl;

    // Parse command line arguments
    int width = 854;
    int height = 480;
    bool fullscreen = false;

    for (int i = 1; i < argc; i++) {
        std::string arg = argv[i];
        if (arg == "--fullscreen" || arg == "-f") {
            fullscreen = true;
        } else if (arg == "--width" && i + 1 < argc) {
            width = std::atoi(argv[++i]);
        } else if (arg == "--height" && i + 1 < argc) {
            height = std::atoi(argv[++i]);
        } else if (arg == "--help" || arg == "-h") {
            std::cout << "Usage: " << argv[0] << " [options]" << std::endl;
            std::cout << "Options:" << std::endl;
            std::cout << "  --width <n>     Set window width (default: 854)" << std::endl;
            std::cout << "  --height <n>    Set window height (default: 480)" << std::endl;
            std::cout << "  --fullscreen    Start in fullscreen mode" << std::endl;
            std::cout << "  --help          Show this help message" << std::endl;
            return 0;
        }
    }

    // Create and run game
    mc::Minecraft game;

    if (!game.init(width, height, fullscreen)) {
        std::cerr << "Failed to initialize Minecraft" << std::endl;
        return 1;
    }

    std::cout << "Controls:" << std::endl;
    std::cout << "  WASD       - Move" << std::endl;
    std::cout << "  Space      - Jump" << std::endl;
    std::cout << "  Shift      - Sneak" << std::endl;
    std::cout << "  Mouse      - Look around" << std::endl;
    std::cout << "  Left click - Break block" << std::endl;
    std::cout << "  Right click - Place block" << std::endl;
    std::cout << "  Scroll     - Change hotbar slot" << std::endl;
    std::cout << "  F3         - Toggle debug info" << std::endl;
    std::cout << "  Escape     - Release/grab mouse" << std::endl;
    std::cout << std::endl;

    try {
        game.run();
    } catch (const std::exception& e) {
        std::cerr << "Fatal error: " << e.what() << std::endl;
        return 1;
    }

    return 0;
}
