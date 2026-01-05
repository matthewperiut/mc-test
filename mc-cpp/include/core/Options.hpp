#pragma once

#include <string>
#include <fstream>

namespace mc {

class Options {
public:
    // Video settings
    bool music = true;
    bool sound = true;
    bool invertYMouse = false;
    float mouseSensitivity = 0.5f;
    int renderDistance = 2;  // 0=far, 1=normal, 2=short, 3=tiny
    int viewBobbing = 1;
    bool anaglyph3d = false;
    bool advancedOpengl = false;
    int limitFramerate = 0;
    bool fancyGraphics = true;
    bool smoothLighting = true;

    // Controls (key codes)
    int keyForward = 'W';
    int keyBack = 'S';
    int keyLeft = 'A';
    int keyRight = 'D';
    int keyJump = ' ';
    int keySneak = 340;  // Left Shift (GLFW)
    int keyDrop = 'Q';
    int keyInventory = 'E';
    int keyChat = 'T';
    int keyPlayerList = 258;  // Tab

    // Multiplayer
    std::string lastServer;
    std::string username = "Player";

    // Misc
    int guiScale = 0;  // 0=auto, 1=small, 2=normal, 3=large
    float fov = 70.0f;
    float gamma = 0.0f;

    Options();

    void load(const std::string& path);
    void save(const std::string& path);

    std::string getKeyName(int keyCode);
};

} // namespace mc
