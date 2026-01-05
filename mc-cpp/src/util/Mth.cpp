#include "util/Mth.hpp"
#include <algorithm>

namespace mc {

std::mt19937 Mth::rng(std::random_device{}());
float Mth::sinTable[65536];
bool Mth::tableInitialized = false;

void Mth::initSinTable() {
    if (tableInitialized) return;
    for (int i = 0; i < 65536; ++i) {
        sinTable[i] = std::sin(static_cast<float>(i) * PI * 2.0f / 65536.0f);
    }
    tableInitialized = true;
}

float Mth::sin(float value) {
    initSinTable();
    return sinTable[static_cast<int>(value * 10430.378f) & 65535];
}

float Mth::cos(float value) {
    initSinTable();
    return sinTable[static_cast<int>(value * 10430.378f + 16384.0f) & 65535];
}

float Mth::sqrt(float value) {
    return std::sqrt(value);
}

float Mth::floor(float value) {
    return std::floor(value);
}

float Mth::ceil(float value) {
    return std::ceil(value);
}

float Mth::abs(float value) {
    return std::abs(value);
}

int Mth::floor(double value) {
    int i = static_cast<int>(value);
    return value < static_cast<double>(i) ? i - 1 : i;
}

int Mth::ceil(double value) {
    int i = static_cast<int>(value);
    return value > static_cast<double>(i) ? i + 1 : i;
}

float Mth::clamp(float value, float min, float max) {
    return std::clamp(value, min, max);
}

int Mth::clamp(int value, int min, int max) {
    return std::clamp(value, min, max);
}

float Mth::lerp(float a, float b, float t) {
    return a + t * (b - a);
}

double Mth::lerp(double a, double b, double t) {
    return a + t * (b - a);
}

float Mth::wrapDegrees(float degrees) {
    degrees = std::fmod(degrees, 360.0f);
    if (degrees >= 180.0f) {
        degrees -= 360.0f;
    }
    if (degrees < -180.0f) {
        degrees += 360.0f;
    }
    return degrees;
}

float Mth::degreesDifference(float a, float b) {
    return wrapDegrees(b - a);
}

int Mth::intFloorDiv(int a, int b) {
    return a < 0 ? -((-a - 1) / b) - 1 : a / b;
}

float Mth::random() {
    std::uniform_real_distribution<float> dist(0.0f, 1.0f);
    return dist(rng);
}

float Mth::random(float min, float max) {
    std::uniform_real_distribution<float> dist(min, max);
    return dist(rng);
}

int Mth::random(int min, int max) {
    std::uniform_int_distribution<int> dist(min, max);
    return dist(rng);
}

void Mth::setSeed(uint64_t seed) {
    rng.seed(seed);
}

} // namespace mc
