#include "renderer/backend/RenderContext.hpp"

namespace mc {

std::unique_ptr<RenderContext> RenderContext::instance = nullptr;

std::unique_ptr<RenderContext>& RenderContext::getInstance() {
    return instance;
}

RenderContext* RenderContext::get() {
    return instance.get();
}

void RenderContext::setInstance(std::unique_ptr<RenderContext> context) {
    instance = std::move(context);
}

} // namespace mc
