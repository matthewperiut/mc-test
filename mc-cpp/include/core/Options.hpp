#pragma once

#include <string>
#include <fstream>

namespace mc {

class Options {
public:
    // Audio settings (0.0 - 1.0 like Java)
    float music = 1.0f;
    float sound = 1.0f;

    // Controls
    bool invertYMouse = false;
    float mouseSensitivity = 0.5f;  // 0.0 - 1.0 (displayed as 0-200%)

    // Video settings
    int renderDistance = 0;  // 0=far, 1=normal, 2=short, 3=tiny (Java default is far)
    bool viewBobbing = true;
    bool anaglyph3d = false;
    bool limitFramerate = false;
    bool fancyGraphics = true;

    // Game settings
    int difficulty = 2;  // 0=peaceful, 1=easy, 2=normal, 3=hard

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
    std::string skin = "Default";

    // Misc (C++ additions, not in Java b1.2)
    int guiScale = 0;  // 0=auto, 1=small, 2=normal, 3=large
    float fov = 70.0f;

    // Third person view (toggled with F5 at runtime, not saved)
    bool thirdPersonView = false;

    Options();

    void load(const std::string& path);
    void save(const std::string& path);

    std::string getKeyName(int keyCode);
};

} // namespace mc
