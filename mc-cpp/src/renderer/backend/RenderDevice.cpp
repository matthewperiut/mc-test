#include "renderer/backend/RenderDevice.hpp"
#include <stdexcept>

namespace mc {

std::unique_ptr<RenderDevice> RenderDevice::instance = nullptr;

RenderDevice& RenderDevice::get() {
    if (!instance) {
        throw std::runtime_error("RenderDevice not initialized");
    }
    return *instance;
}

void RenderDevice::setInstance(std::unique_ptr<RenderDevice> device) {
    instance = std::move(device);
}

bool RenderDevice::hasInstance() {
    return instance != nullptr;
}

} // namespace mc
