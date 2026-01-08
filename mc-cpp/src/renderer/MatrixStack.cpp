#include "renderer/MatrixStack.hpp"
#include <cmath>

namespace mc {

MatrixStack::MatrixStack() : current(1.0f) {}

MatrixStack& MatrixStack::projection() {
    static MatrixStack instance;
    return instance;
}

MatrixStack& MatrixStack::modelview() {
    static MatrixStack instance;
    return instance;
}

void MatrixStack::push() {
    stack.push(current);
}

void MatrixStack::pop() {
    if (!stack.empty()) {
        current = stack.top();
        stack.pop();
    }
}

void MatrixStack::loadIdentity() {
    current = glm::mat4(1.0f);
}

void MatrixStack::load(const glm::mat4& matrix) {
    current = matrix;
}

void MatrixStack::translate(float x, float y, float z) {
    current = glm::translate(current, glm::vec3(x, y, z));
}

void MatrixStack::rotate(float angleDegrees, float x, float y, float z) {
    current = glm::rotate(current, glm::radians(angleDegrees), glm::vec3(x, y, z));
}

void MatrixStack::scale(float x, float y, float z) {
    current = glm::scale(current, glm::vec3(x, y, z));
}

void MatrixStack::perspective(float fovDegrees, float aspect, float nearPlane, float farPlane) {
    current = glm::perspective(glm::radians(fovDegrees), aspect, nearPlane, farPlane);
}

void MatrixStack::frustum(float left, float right, float bottom, float top, float nearPlane, float farPlane) {
    current = glm::frustum(left, right, bottom, top, nearPlane, farPlane);
}

void MatrixStack::ortho(float left, float right, float bottom, float top, float nearPlane, float farPlane) {
    current = glm::ortho(left, right, bottom, top, nearPlane, farPlane);
}

void MatrixStack::multiply(const glm::mat4& matrix) {
    current = current * matrix;
}

glm::mat4 MatrixStack::getMVP() {
    return projection().get() * modelview().get();
}

glm::mat4 MatrixStack::getNormalMatrix() {
    return glm::transpose(glm::inverse(modelview().get()));
}

} // namespace mc
