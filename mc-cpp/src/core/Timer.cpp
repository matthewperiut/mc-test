#include "core/Timer.hpp"
#include <algorithm>

namespace mc {

Timer::Timer(float ticksPerSecond)
    : ticksPerSecond(ticksPerSecond)
    , lastTime(0.0f)
    , ticks(0)
    , partialTick(0.0f)
    , tickDelta(0.0f)
    , fps(0.0f)
    , lastSyncSysClock(0)
    , lastSyncHRClock(0)
    , timeSyncAdjust(1.0)
    , startTime(Clock::now())
{
    lastTime = static_cast<float>(getTime());
    lastSyncSysClock = getSystemTime();
}

double Timer::getTime() {
    auto now = Clock::now();
    auto duration = std::chrono::duration<double>(now - startTime);
    return duration.count() * 1000.0;  // Convert to milliseconds
}

long Timer::getSystemTime() {
    auto now = std::chrono::system_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(
        now.time_since_epoch()
    );
    return static_cast<long>(duration.count());
}

void Timer::advanceTime() {
    long sysTime = getSystemTime();
    long sysDiff = sysTime - lastSyncSysClock;
    long hrTime = static_cast<long>(getTime());
    long hrDiff = hrTime - lastSyncHRClock;
    double adjust = 1.0;

    // Avoid division by zero on first frame
    if (hrDiff > 0) {
        adjust = static_cast<double>(sysDiff) / static_cast<double>(hrDiff);

        // Clamp adjustment factor
        adjust = std::clamp(adjust, 0.8, 1.2);
        timeSyncAdjust += (adjust - timeSyncAdjust) * 0.2;
    }

    lastSyncSysClock = sysTime;
    lastSyncHRClock = hrTime;

    double currentTime = getTime();
    double elapsed = (currentTime - lastTime) * timeSyncAdjust;
    lastTime = static_cast<float>(currentTime);

    // Clamp to prevent spiral of death
    if (elapsed < 0.0) elapsed = 0.0;
    if (elapsed > 1000.0) elapsed = 1000.0;

    fps = static_cast<float>(1000.0 / (elapsed + 1.0));

    tickDelta += static_cast<float>(elapsed * ticksPerSecond / 1000.0);
    ticks = static_cast<int>(tickDelta);
    tickDelta -= ticks;

    // Clamp ticks per frame to prevent freeze
    if (ticks > 10) {
        ticks = 10;
    }

    partialTick = tickDelta;
}

} // namespace mc
