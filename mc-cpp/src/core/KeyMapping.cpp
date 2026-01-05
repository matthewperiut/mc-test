#include "core/KeyMapping.hpp"
#include <GLFW/glfw3.h>

namespace mc {

// Define static key mappings with GLFW key codes
KeyMapping KeyMapping::keyForward("key.forward", GLFW_KEY_W);
KeyMapping KeyMapping::keyBack("key.back", GLFW_KEY_S);
KeyMapping KeyMapping::keyLeft("key.left", GLFW_KEY_A);
KeyMapping KeyMapping::keyRight("key.right", GLFW_KEY_D);
KeyMapping KeyMapping::keyJump("key.jump", GLFW_KEY_SPACE);
KeyMapping KeyMapping::keySneak("key.sneak", GLFW_KEY_LEFT_SHIFT);
KeyMapping KeyMapping::keyDrop("key.drop", GLFW_KEY_Q);
KeyMapping KeyMapping::keyInventory("key.inventory", GLFW_KEY_E);
KeyMapping KeyMapping::keyChat("key.chat", GLFW_KEY_T);
KeyMapping KeyMapping::keyPlayerList("key.playerList", GLFW_KEY_TAB);
KeyMapping KeyMapping::keyAttack("key.attack", GLFW_MOUSE_BUTTON_LEFT);
KeyMapping KeyMapping::keyUse("key.use", GLFW_MOUSE_BUTTON_RIGHT);
KeyMapping KeyMapping::keyPickItem("key.pickItem", GLFW_MOUSE_BUTTON_MIDDLE);

KeyMapping::KeyMapping(const std::string& name, int keyCode)
    : name(name), keyCode(keyCode)
{}

bool KeyMapping::isPressed(GLFWwindow* window) const {
    // Handle mouse buttons separately
    if (keyCode >= GLFW_MOUSE_BUTTON_1 && keyCode <= GLFW_MOUSE_BUTTON_LAST) {
        return glfwGetMouseButton(window, keyCode) == GLFW_PRESS;
    }
    return glfwGetKey(window, keyCode) == GLFW_PRESS;
}

void KeyMapping::setAll(GLFWwindow* /*window*/) {
    // This would load key bindings from options
}

void KeyMapping::resetToDefaults() {
    keyForward.keyCode = GLFW_KEY_W;
    keyBack.keyCode = GLFW_KEY_S;
    keyLeft.keyCode = GLFW_KEY_A;
    keyRight.keyCode = GLFW_KEY_D;
    keyJump.keyCode = GLFW_KEY_SPACE;
    keySneak.keyCode = GLFW_KEY_LEFT_SHIFT;
    keyDrop.keyCode = GLFW_KEY_Q;
    keyInventory.keyCode = GLFW_KEY_E;
    keyChat.keyCode = GLFW_KEY_T;
    keyPlayerList.keyCode = GLFW_KEY_TAB;
}

} // namespace mc
