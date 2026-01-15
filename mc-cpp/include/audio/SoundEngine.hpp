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

    // Sound playback using sound names (e.g., "step.grass", "random.click")
    // These resolve to actual files with random variant selection
    void playSound(const std::string& soundName, float volume = 1.0f, float pitch = 1.0f);
    void playSound3D(const std::string& soundName, float x, float y, float z,
                     float volume = 1.0f, float pitch = 1.0f);

    // Direct file playback (internal use)
    void play(const std::string& path, float volume = 1.0f, float pitch = 1.0f);
    void play3D(const std::string& path, float x, float y, float z,
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

    // Sound resources path
    void setSoundPath(const std::string& path) { soundBasePath = path; }

private:
    SoundEngine();
    ~SoundEngine();
    SoundEngine(const SoundEngine&) = delete;
    SoundEngine& operator=(const SoundEngine&) = delete;

    SoundBuffer* loadSound(const std::string& path);
    SoundBuffer* loadWav(const std::string& path);
    SoundBuffer* loadOgg(const std::string& path);
    ALuint getAvailableSource();
    void cleanupSources();

    // Resolve sound name to file path (e.g., "step.grass" -> "../resources/sound/step/grass2.ogg")
    std::string resolveSoundPath(const std::string& soundName);
    void scanSoundVariants();

    ALCdevice* device;
    ALCcontext* context;
    bool initialized;

    // Sound cache
    std::unordered_map<std::string, SoundBuffer> soundCache;

    // Sound variants: maps sound name to list of file paths
    std::unordered_map<std::string, std::vector<std::string>> soundVariants;

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

    // Base path for sound resources
    std::string soundBasePath;
};

} // namespace mc
