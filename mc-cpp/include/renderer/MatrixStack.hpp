#pragma once

#include <glm/glm.hpp>
#include <glm/gtc/matrix_transform.hpp>
#include <glm/gtc/type_ptr.hpp>
#include <stack>

namespace mc {

class MatrixStack {
public:
    static MatrixStack& projection();
    static MatrixStack& modelview();

    void push();
    void pop();
    void loadIdentity();
    void load(const glm::mat4& matrix);

    void translate(float x, float y, float z);
    void rotate(float angleDegrees, float x, float y, float z);
    void scale(float x, float y, float z);

    void perspective(float fovDegrees, float aspect, float nearPlane, float farPlane);
    void frustum(float left, float right, float bottom, float top, float nearPlane, float farPlane);
    void ortho(float left, float right, float bottom, float top, float nearPlane, float farPlane);

    void multiply(const glm::mat4& matrix);

    const glm::mat4& get() const { return current; }
    const float* getPtr() const { return glm::value_ptr(current); }

    static glm::mat4 getMVP();
    static glm::mat4 getNormalMatrix();

private:
    MatrixStack();
    std::stack<glm::mat4> stack;
    glm::mat4 current{1.0f};
};

} // namespace mc
