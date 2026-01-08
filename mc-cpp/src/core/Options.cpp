#include "core/Options.hpp"
#include <sstream>
#include <GLFW/glfw3.h>

namespace mc {

Options::Options() {}

// Helper to parse float that also accepts "true"/"false" for backwards compatibility
static float readFloat(const std::string& value) {
    if (value == "true") return 1.0f;
    if (value == "false") return 0.0f;
    try {
        return std::stof(value);
    } catch (...) {
        return 0.0f;
    }
}

void Options::load(const std::string& path) {
    std::ifstream file(path);
    if (!file.is_open()) return;

    std::string line;
    while (std::getline(file, line)) {
        size_t pos = line.find(':');
        if (pos == std::string::npos) continue;

        std::string key = line.substr(0, pos);
        std::string value = line.substr(pos + 1);

        // Audio (float 0.0-1.0, like Java)
        if (key == "music") music = readFloat(value);
        else if (key == "sound") sound = readFloat(value);
        // Controls
        else if (key == "invertYMouse") invertYMouse = (value == "true");
        else if (key == "mouseSensitivity") mouseSensitivity = readFloat(value);
        // Video
        else if (key == "viewDistance" || key == "renderDistance") renderDistance = std::stoi(value);
        else if (key == "bobView" || key == "viewBobbing") viewBobbing = (value == "true" || value == "1");
        else if (key == "anaglyph3d") anaglyph3d = (value == "true");
        else if (key == "vsync" || key == "limitFramerate") vsync = (value == "true");
        else if (key == "fancyGraphics") fancyGraphics = (value == "true");
        // Game
        else if (key == "difficulty") difficulty = std::stoi(value);
        // Misc
        else if (key == "fov") fov = std::stof(value);
        else if (key == "guiScale") guiScale = std::stoi(value);
        else if (key == "lastServer") lastServer = value;
        else if (key == "skin") skin = value;
    }
}

void Options::save(const std::string& path) {
    std::ofstream file(path);
    if (!file.is_open()) return;

    // Match Java's options.txt format
    file << "music:" << music << "\n";
    file << "sound:" << sound << "\n";
    file << "invertYMouse:" << (invertYMouse ? "true" : "false") << "\n";
    file << "mouseSensitivity:" << mouseSensitivity << "\n";
    file << "viewDistance:" << renderDistance << "\n";
    file << "bobView:" << (viewBobbing ? "true" : "false") << "\n";
    file << "anaglyph3d:" << (anaglyph3d ? "true" : "false") << "\n";
    file << "vsync:" << (vsync ? "true" : "false") << "\n";
    file << "difficulty:" << difficulty << "\n";
    file << "fancyGraphics:" << (fancyGraphics ? "true" : "false") << "\n";
    file << "skin:" << skin << "\n";
    file << "lastServer:" << lastServer << "\n";
    // C++ additions
    file << "fov:" << fov << "\n";
    file << "guiScale:" << guiScale << "\n";
}

std::string Options::getKeyName(int keyCode) {
    const char* name = glfwGetKeyName(keyCode, 0);
    if (name) return std::string(name);

    // Handle special keys
    switch (keyCode) {
        case GLFW_KEY_SPACE: return "SPACE";
        case GLFW_KEY_LEFT_SHIFT: return "LSHIFT";
        case GLFW_KEY_RIGHT_SHIFT: return "RSHIFT";
        case GLFW_KEY_LEFT_CONTROL: return "LCTRL";
        case GLFW_KEY_RIGHT_CONTROL: return "RCTRL";
        case GLFW_KEY_TAB: return "TAB";
        case GLFW_KEY_ESCAPE: return "ESC";
        case GLFW_KEY_ENTER: return "ENTER";
        default: return "KEY_" + std::to_string(keyCode);
    }
}

} // namespace mc
