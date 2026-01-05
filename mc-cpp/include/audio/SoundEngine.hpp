#pragma once

#include <string>
#include <unordered_map>
#include <vector>
#include <AL/al.h>
#include <AL/alc.h>

namespace mc {

class Entity;

struct SoundBuffer {
    ALuint buffer;
    int channels;
    int sampleRate;
};

class SoundEngine {
public:
    static SoundEngine& getInstance();

    bool init();
    void destroy();

    // Sound playback
    void play(const std::string& sound, float volume = 1.0f, float pitch = 1.0f);
    void play3D(const std::string& sound, float x, float y, float z,
                float volume = 1.0f, float pitch = 1.0f);
    void playMusic(const std::string& music);
    void stopMusic();

    // Listener (camera/player position)
    void setListenerPosition(float x, float y, float z);
    void setListenerOrientation(float atX, float atY, float atZ,
                                 float upX, float upY, float upZ);
    void setListenerVelocity(float vx, float vy, float vz);

    // Volume control
    void setMasterVolume(float volume);
    void setMusicVolume(float volume);
    void setSoundVolume(float volume);

    // Update (called per frame)
    void update(Entity* listener, float partialTick);

    // State
    bool isInitialized() const { return initialized; }
    bool isMusicPlaying() const;

private:
    SoundEngine();
    ~SoundEngine();
    SoundEngine(const SoundEngine&) = delete;
    SoundEngine& operator=(const SoundEngine&) = delete;

    SoundBuffer* loadSound(const std::string& path);
    ALuint getAvailableSource();
    void cleanupSources();

    ALCdevice* device;
    ALCcontext* context;
    bool initialized;

    // Sound cache
    std::unordered_map<std::string, SoundBuffer> soundCache;

    // Source pool
    std::vector<ALuint> sources;
    static constexpr int MAX_SOURCES = 32;

    // Music
    ALuint musicSource;
    std::string currentMusic;

    // Volume
    float masterVolume;
    float musicVolume;
    float soundVolume;
};

} // namespace mc
