#pragma once

#include <string>
#include <array>

struct GLFWwindow;

namespace mc {

class KeyMapping {
public:
    std::string name;
    int keyCode;

    KeyMapping(const std::string& name, int keyCode);

    bool isPressed(GLFWwindow* window) const;

    // Static key mappings
    static KeyMapping keyForward;
    static KeyMapping keyBack;
    static KeyMapping keyLeft;
    static KeyMapping keyRight;
    static KeyMapping keyJump;
    static KeyMapping keySneak;
    static KeyMapping keyDrop;
    static KeyMapping keyInventory;
    static KeyMapping keyChat;
    static KeyMapping keyPlayerList;
    static KeyMapping keyAttack;
    static KeyMapping keyUse;
    static KeyMapping keyPickItem;

    static void setAll(GLFWwindow* window);
    static void resetToDefaults();
};

} // namespace mc
