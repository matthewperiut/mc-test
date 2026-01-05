#pragma once

#include <chrono>

namespace mc {

class Timer {
public:
    static constexpr float TICKS_PER_SECOND = 20.0f;
    static constexpr float MS_PER_TICK = 1000.0f / TICKS_PER_SECOND;

    float ticksPerSecond;
    float lastTime;
    int ticks;
    float partialTick;    // Interpolation factor [0, 1)
    float tickDelta;
    float fps;
    long lastSyncSysClock;
    long lastSyncHRClock;
    double timeSyncAdjust;

    Timer(float ticksPerSecond = TICKS_PER_SECOND);

    void advanceTime();

private:
    using Clock = std::chrono::high_resolution_clock;
    Clock::time_point startTime;

    double getTime();
    long getSystemTime();
};

} // namespace mc
