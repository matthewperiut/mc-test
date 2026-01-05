#include "core/Options.hpp"
#include <sstream>
#include <GLFW/glfw3.h>

namespace mc {

Options::Options() {}

void Options::load(const std::string& path) {
    std::ifstream file(path);
    if (!file.is_open()) return;

    std::string line;
    while (std::getline(file, line)) {
        size_t pos = line.find(':');
        if (pos == std::string::npos) continue;

        std::string key = line.substr(0, pos);
        std::string value = line.substr(pos + 1);

        if (key == "music") music = (value == "true");
        else if (key == "sound") sound = (value == "true");
        else if (key == "invertYMouse") invertYMouse = (value == "true");
        else if (key == "mouseSensitivity") mouseSensitivity = std::stof(value);
        else if (key == "renderDistance") renderDistance = std::stoi(value);
        else if (key == "viewBobbing") viewBobbing = std::stoi(value);
        else if (key == "anaglyph3d") anaglyph3d = (value == "true");
        else if (key == "advancedOpengl") advancedOpengl = (value == "true");
        else if (key == "fancyGraphics") fancyGraphics = (value == "true");
        else if (key == "smoothLighting") smoothLighting = (value == "true");
        else if (key == "fov") fov = std::stof(value);
        else if (key == "gamma") gamma = std::stof(value);
        else if (key == "guiScale") guiScale = std::stoi(value);
        else if (key == "lastServer") lastServer = value;
        else if (key == "username") username = value;
    }
}

void Options::save(const std::string& path) {
    std::ofstream file(path);
    if (!file.is_open()) return;

    file << "music:" << (music ? "true" : "false") << "\n";
    file << "sound:" << (sound ? "true" : "false") << "\n";
    file << "invertYMouse:" << (invertYMouse ? "true" : "false") << "\n";
    file << "mouseSensitivity:" << mouseSensitivity << "\n";
    file << "renderDistance:" << renderDistance << "\n";
    file << "viewBobbing:" << viewBobbing << "\n";
    file << "anaglyph3d:" << (anaglyph3d ? "true" : "false") << "\n";
    file << "advancedOpengl:" << (advancedOpengl ? "true" : "false") << "\n";
    file << "fancyGraphics:" << (fancyGraphics ? "true" : "false") << "\n";
    file << "smoothLighting:" << (smoothLighting ? "true" : "false") << "\n";
    file << "fov:" << fov << "\n";
    file << "gamma:" << gamma << "\n";
    file << "guiScale:" << guiScale << "\n";
    file << "lastServer:" << lastServer << "\n";
    file << "username:" << username << "\n";
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
