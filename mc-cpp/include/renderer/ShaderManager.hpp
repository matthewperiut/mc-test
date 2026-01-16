#pragma once

#include "renderer/MatrixStack.hpp"
#include "renderer/backend/ShaderPipeline.hpp"
#include <memory>

namespace mc {

class ShaderManager {
public:
    static ShaderManager& getInstance();

    void init();
    void destroy();

    ShaderPipeline* getWorldShader() { return worldShader.get(); }
    ShaderPipeline* getSkyShader() { return skyShader.get(); }
    ShaderPipeline* getGuiShader() { return guiShader.get(); }
    ShaderPipeline* getLineShader() { return lineShader.get(); }

    void useWorldShader();
    void useSkyShader();
    void useGuiShader();
    void useLineShader();

    void updateMatrices();
    void updateMatrices(const glm::mat4& projection, const glm::mat4& modelview);
    void updateFog(float start, float end, float r, float g, float b);
    void setAlphaTest(float threshold);
    void setTexture(int unit);
    void setSkyColor(float r, float g, float b, float a);
    void setUseTexture(bool use);
    void setGuiColor(float r, float g, float b, float a);
    void setUseVertexColor(bool use);  // When true, use vertex colors instead of uniform

    // Lighting for hand/item rendering (matching Java Lighting.turnOn)
    void enableLighting(bool enable);
    void setLightDirections(float dir0x, float dir0y, float dir0z,
                           float dir1x, float dir1y, float dir1z);
    void setLightParams(float ambient, float diffuse);
    void setBrightness(float brightness);
    void setSkyBrightness(float skyBrightness);  // 0-1 based on time of day
    void updateNormalMatrix();

    bool isInitialized() const { return initialized; }

private:
    ShaderManager() = default;
    ~ShaderManager() = default;
    ShaderManager(const ShaderManager&) = delete;
    ShaderManager& operator=(const ShaderManager&) = delete;

    std::unique_ptr<ShaderPipeline> worldShader;
    std::unique_ptr<ShaderPipeline> skyShader;
    std::unique_ptr<ShaderPipeline> guiShader;
    std::unique_ptr<ShaderPipeline> lineShader;

    ShaderPipeline* currentShader = nullptr;
    bool initialized = false;

    float fogStart = 0.0f;
    float fogEnd = 100.0f;
    float fogR = 0.5f, fogG = 0.8f, fogB = 1.0f;
    float alphaThreshold = 0.0f;
};

} // namespace mc
