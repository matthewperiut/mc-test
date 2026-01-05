#pragma once

#include <cmath>
#include <cstdint>
#include <random>

namespace mc {

class Mth {
public:
    static constexpr float PI = 3.14159265358979323846f;
    static constexpr float DEG_TO_RAD = PI / 180.0f;
    static constexpr float RAD_TO_DEG = 180.0f / PI;

    static float sin(float value);
    static float cos(float value);
    static float sqrt(float value);
    static float floor(float value);
    static float ceil(float value);
    static float abs(float value);

    static int floor(double value);
    static int ceil(double value);

    static float clamp(float value, float min, float max);
    static int clamp(int value, int min, int max);

    static float lerp(float a, float b, float t);
    static double lerp(double a, double b, double t);

    static float wrapDegrees(float degrees);
    static float degreesDifference(float a, float b);

    static int intFloorDiv(int a, int b);

    // Random number generation
    static float random();
    static float random(float min, float max);
    static int random(int min, int max);
    static void setSeed(uint64_t seed);

private:
    static std::mt19937 rng;
    static float sinTable[65536];
    static bool tableInitialized;
    static void initSinTable();
};

} // namespace mc
