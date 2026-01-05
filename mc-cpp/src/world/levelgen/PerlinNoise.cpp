// PerlinNoise.cpp - Perlin noise implementation for terrain generation
#include "util/Mth.hpp"
#include <cmath>
#include <array>

namespace mc {

class PerlinNoise {
public:
    PerlinNoise(long seed = 0) {
        // Initialize permutation table
        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }

        // Shuffle based on seed
        Mth::setSeed(static_cast<uint64_t>(seed));
        for (int i = 255; i > 0; i--) {
            int j = Mth::random(0, i);
            std::swap(p[i], p[j]);
        }

        // Duplicate for overflow
        for (int i = 0; i < 256; i++) {
            p[256 + i] = p[i];
        }
    }

    double noise(double x, double y, double z) const {
        // Find unit cube containing point
        int X = static_cast<int>(std::floor(x)) & 255;
        int Y = static_cast<int>(std::floor(y)) & 255;
        int Z = static_cast<int>(std::floor(z)) & 255;

        // Find relative position in cube
        x -= std::floor(x);
        y -= std::floor(y);
        z -= std::floor(z);

        // Fade curves
        double u = fade(x);
        double v = fade(y);
        double w = fade(z);

        // Hash coordinates
        int A = p[X] + Y;
        int AA = p[A] + Z;
        int AB = p[A + 1] + Z;
        int B = p[X + 1] + Y;
        int BA = p[B] + Z;
        int BB = p[B + 1] + Z;

        // Blend results from 8 corners
        return lerp(w, lerp(v, lerp(u, grad(p[AA], x, y, z),
                                        grad(p[BA], x - 1, y, z)),
                                lerp(u, grad(p[AB], x, y - 1, z),
                                        grad(p[BB], x - 1, y - 1, z))),
                        lerp(v, lerp(u, grad(p[AA + 1], x, y, z - 1),
                                        grad(p[BA + 1], x - 1, y, z - 1)),
                                lerp(u, grad(p[AB + 1], x, y - 1, z - 1),
                                        grad(p[BB + 1], x - 1, y - 1, z - 1))));
    }

    double noise2D(double x, double y) const {
        return noise(x, y, 0);
    }

private:
    std::array<int, 512> p;

    static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    static double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
};

} // namespace mc
