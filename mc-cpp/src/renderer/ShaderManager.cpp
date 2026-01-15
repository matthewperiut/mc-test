#include "renderer/ShaderManager.hpp"
#include <iostream>

#ifdef MC_RENDERER_METAL
#include "renderer/backend/RenderDevice.hpp"
#endif

namespace mc {

ShaderManager& ShaderManager::getInstance() {
    static ShaderManager instance;
    return instance;
}

void ShaderManager::init() {
    if (initialized) return;

#ifdef MC_RENDERER_METAL
    auto& device = RenderDevice::get();

    worldShader = device.createShaderPipeline();
    if (!worldShader->loadFromGLSL("shaders/world.vert", "shaders/world.frag")) {
        std::cerr << "Failed to load world shader" << std::endl;
    }

    skyShader = device.createShaderPipeline();
    if (!skyShader->loadFromGLSL("shaders/sky.vert", "shaders/sky.frag")) {
        std::cerr << "Failed to load sky shader" << std::endl;
    }

    guiShader = device.createShaderPipeline();
    if (!guiShader->loadFromGLSL("shaders/gui.vert", "shaders/gui.frag")) {
        std::cerr << "Failed to load gui shader" << std::endl;
    }

    lineShader = device.createShaderPipeline();
    if (!lineShader->loadFromGLSL("shaders/line.vert", "shaders/line.frag")) {
        std::cerr << "Failed to load line shader" << std::endl;
    }
#else
    if (!worldShader.load("shaders/world.vert", "shaders/world.frag")) {
        std::cerr << "Failed to load world shader" << std::endl;
    }

    if (!skyShader.load("shaders/sky.vert", "shaders/sky.frag")) {
        std::cerr << "Failed to load sky shader" << std::endl;
    }

    if (!guiShader.load("shaders/gui.vert", "shaders/gui.frag")) {
        std::cerr << "Failed to load gui shader" << std::endl;
    }

    if (!lineShader.load("shaders/line.vert", "shaders/line.frag")) {
        std::cerr << "Failed to load line shader" << std::endl;
    }
#endif

    initialized = true;
}

void ShaderManager::destroy() {
#ifdef MC_RENDERER_METAL
    worldShader.reset();
    skyShader.reset();
    guiShader.reset();
    lineShader.reset();
#endif
    initialized = false;
}

void ShaderManager::useWorldShader() {
#ifdef MC_RENDERER_METAL
    worldShader->bind();
    currentShader = worldShader.get();
    updateMatrices();
    worldShader->setInt("uTexture", 0);
    worldShader->setInt("uUseTexture", 1);
    worldShader->setFloat("uFogStart", fogStart);
    worldShader->setFloat("uFogEnd", fogEnd);
    worldShader->setVec3("uFogColor", fogR, fogG, fogB);
    worldShader->setFloat("uAlphaTest", alphaThreshold);
#else
    worldShader.use();
    currentShader = &worldShader;
    updateMatrices();
    worldShader.setInt("uTexture", 0);
    worldShader.setInt("uUseTexture", 1);  // Default to using texture
    worldShader.setFloat("uFogStart", fogStart);
    worldShader.setFloat("uFogEnd", fogEnd);
    worldShader.setVec3("uFogColor", fogR, fogG, fogB);
    worldShader.setFloat("uAlphaTest", alphaThreshold);
#endif
}

void ShaderManager::useSkyShader() {
#ifdef MC_RENDERER_METAL
    skyShader->bind();
    currentShader = skyShader.get();
    updateMatrices();
    skyShader->setInt("uTexture", 0);
#else
    skyShader.use();
    currentShader = &skyShader;
    updateMatrices();
    skyShader.setInt("uTexture", 0);
#endif
}

void ShaderManager::useGuiShader() {
#ifdef MC_RENDERER_METAL
    guiShader->bind();
    currentShader = guiShader.get();
    updateMatrices();
    guiShader->setInt("uTexture", 0);
    guiShader->setFloat("uAlphaTest", 0.1f);
#else
    guiShader.use();
    currentShader = &guiShader;
    updateMatrices();
    guiShader.setInt("uTexture", 0);
    guiShader.setFloat("uAlphaTest", 0.1f);  // Discard nearly transparent pixels
#endif
}

void ShaderManager::useLineShader() {
#ifdef MC_RENDERER_METAL
    lineShader->bind();
    currentShader = lineShader.get();
    updateMatrices();
#else
    lineShader.use();
    currentShader = &lineShader;
    updateMatrices();
#endif
}

void ShaderManager::updateMatrices() {
    if (!currentShader) return;

    glm::mat4 mvp = MatrixStack::getMVP();
    glm::mat4 mv = MatrixStack::modelview().get();

    currentShader->setMat4("uMVP", glm::value_ptr(mvp));
    currentShader->setMat4("uModelView", glm::value_ptr(mv));
}

void ShaderManager::updateMatrices(const glm::mat4& projection, const glm::mat4& modelview) {
    if (!currentShader) return;

    glm::mat4 mvp = projection * modelview;
    currentShader->setMat4("uMVP", glm::value_ptr(mvp));
    currentShader->setMat4("uModelView", glm::value_ptr(modelview));
}

void ShaderManager::updateFog(float start, float end, float r, float g, float b) {
    fogStart = start;
    fogEnd = end;
    fogR = r;
    fogG = g;
    fogB = b;

#ifdef MC_RENDERER_METAL
    if (currentShader == worldShader.get()) {
        worldShader->setFloat("uFogStart", fogStart);
        worldShader->setFloat("uFogEnd", fogEnd);
        worldShader->setVec3("uFogColor", fogR, fogG, fogB);
    }
#else
    if (currentShader == &worldShader) {
        worldShader.setFloat("uFogStart", fogStart);
        worldShader.setFloat("uFogEnd", fogEnd);
        worldShader.setVec3("uFogColor", fogR, fogG, fogB);
    }
#endif
}

void ShaderManager::setAlphaTest(float threshold) {
    alphaThreshold = threshold;
#ifdef MC_RENDERER_METAL
    if (currentShader == worldShader.get()) {
        worldShader->setFloat("uAlphaTest", alphaThreshold);
    } else if (currentShader == guiShader.get()) {
        guiShader->setFloat("uAlphaTest", alphaThreshold);
    }
#else
    if (currentShader == &worldShader) {
        worldShader.setFloat("uAlphaTest", alphaThreshold);
    } else if (currentShader == &guiShader) {
        guiShader.setFloat("uAlphaTest", alphaThreshold);
    }
#endif
}

void ShaderManager::setTexture(int unit) {
    if (currentShader) {
        currentShader->setInt("uTexture", unit);
    }
}

void ShaderManager::setSkyColor(float r, float g, float b, float a) {
#ifdef MC_RENDERER_METAL
    if (currentShader == skyShader.get()) {
        skyShader->setVec4("uColor", r, g, b, a);
        skyShader->setInt("uUseUniformColor", 1);
    }
#else
    if (currentShader == &skyShader) {
        skyShader.setVec4("uColor", r, g, b, a);
        skyShader.setInt("uUseUniformColor", 1);
    }
#endif
}

void ShaderManager::setUseTexture(bool use) {
    if (currentShader) {
        currentShader->setInt("uUseTexture", use ? 1 : 0);
        // When using textures, use vertex colors instead of uniform color
#ifdef MC_RENDERER_METAL
        if (use && (currentShader == skyShader.get() || currentShader == guiShader.get())) {
            currentShader->setInt("uUseUniformColor", 0);
        }
#else
        if (use && (currentShader == &skyShader || currentShader == &guiShader)) {
            currentShader->setInt("uUseUniformColor", 0);
        }
#endif
    }
}

void ShaderManager::setGuiColor(float r, float g, float b, float a) {
#ifdef MC_RENDERER_METAL
    if (currentShader == guiShader.get()) {
        guiShader->setVec4("uColor", r, g, b, a);
        guiShader->setInt("uUseUniformColor", 1);
    }
#else
    if (currentShader == &guiShader) {
        guiShader.setVec4("uColor", r, g, b, a);
        guiShader.setInt("uUseUniformColor", 1);
    }
#endif
}

void ShaderManager::setUseVertexColor(bool use) {
    if (currentShader) {
        currentShader->setInt("uUseUniformColor", use ? 0 : 1);
    }
}

void ShaderManager::enableLighting(bool enable) {
#ifdef MC_RENDERER_METAL
    if (currentShader == worldShader.get()) {
        worldShader->setInt("uEnableLighting", enable ? 1 : 0);
    }
#else
    if (currentShader == &worldShader) {
        worldShader.setInt("uEnableLighting", enable ? 1 : 0);
    }
#endif
}

void ShaderManager::setLightDirections(float dir0x, float dir0y, float dir0z,
                                       float dir1x, float dir1y, float dir1z) {
#ifdef MC_RENDERER_METAL
    if (currentShader == worldShader.get()) {
        worldShader->setVec3("uLightDir0", dir0x, dir0y, dir0z);
        worldShader->setVec3("uLightDir1", dir1x, dir1y, dir1z);
    }
#else
    if (currentShader == &worldShader) {
        worldShader.setVec3("uLightDir0", dir0x, dir0y, dir0z);
        worldShader.setVec3("uLightDir1", dir1x, dir1y, dir1z);
    }
#endif
}

void ShaderManager::setLightParams(float ambient, float diffuse) {
#ifdef MC_RENDERER_METAL
    if (currentShader == worldShader.get()) {
        worldShader->setFloat("uAmbient", ambient);
        worldShader->setFloat("uDiffuse", diffuse);
    }
#else
    if (currentShader == &worldShader) {
        worldShader.setFloat("uAmbient", ambient);
        worldShader.setFloat("uDiffuse", diffuse);
    }
#endif
}

void ShaderManager::setBrightness(float brightness) {
#ifdef MC_RENDERER_METAL
    if (currentShader == worldShader.get()) {
        worldShader->setFloat("uBrightness", brightness);
    }
#else
    if (currentShader == &worldShader) {
        worldShader.setFloat("uBrightness", brightness);
    }
#endif
}

void ShaderManager::setSkyBrightness(float skyBrightness) {
#ifdef MC_RENDERER_METAL
    if (currentShader == worldShader.get()) {
        worldShader->setFloat("uSkyBrightness", skyBrightness);
    }
#else
    if (currentShader == &worldShader) {
        worldShader.setFloat("uSkyBrightness", skyBrightness);
    }
#endif
}

void ShaderManager::updateNormalMatrix() {
    if (!currentShader) return;

    glm::mat4 mv = MatrixStack::modelview().get();
    glm::mat3 normalMatrix = glm::transpose(glm::inverse(glm::mat3(mv)));
    currentShader->setMat3("uNormalMatrix", glm::value_ptr(normalMatrix));
}

} // namespace mc
