#include "audio/SoundEngine.hpp"
#include "entity/Entity.hpp"
#include <iostream>
#include <fstream>
#include <cstring>
#include <filesystem>
#include <random>
#include <algorithm>

// Include stb_vorbis for OGG decoding
#define STB_VORBIS_HEADER_ONLY
#include "stb_vorbis.c"

namespace mc {

SoundEngine& SoundEngine::getInstance() {
    static SoundEngine instance;
    return instance;
}

SoundEngine::SoundEngine()
    : device(nullptr)
    , context(nullptr)
    , initialized(false)
    , musicSource(0)
    , masterVolume(1.0f)
    , musicVolume(1.0f)
    , soundVolume(1.0f)
    , listenerX(0.0f)
    , listenerY(0.0f)
    , listenerZ(0.0f)
    , soundBasePath("../../resources")  // From build/ to mc-deobf/resources/
{
}

SoundEngine::~SoundEngine() {
    destroy();
}

bool SoundEngine::init() {
    if (initialized) return true;

    // Open default device
    device = alcOpenDevice(nullptr);
    if (!device) {
        std::cerr << "Failed to open OpenAL device" << std::endl;
        return false;
    }

    // Create context
    context = alcCreateContext(device, nullptr);
    if (!context) {
        std::cerr << "Failed to create OpenAL context" << std::endl;
        alcCloseDevice(device);
        device = nullptr;
        return false;
    }

    alcMakeContextCurrent(context);

    // Set distance model to match Java's Paul's Sound System behavior
    alDistanceModel(AL_INVERSE_DISTANCE_CLAMPED);

    // Create source pool
    sources.resize(MAX_SOURCES);
    alGenSources(MAX_SOURCES, sources.data());

    // Create music source
    alGenSources(1, &musicSource);

    // Set default listener
    setListenerPosition(0, 0, 0);
    setListenerOrientation(0, 0, -1, 0, 1, 0);

    initialized = true;
    std::cout << "OpenAL initialized successfully" << std::endl;

    // Scan for sound variants
    scanSoundVariants();

    return true;
}

void SoundEngine::destroy() {
    if (!initialized) return;

    // Stop and delete sources
    if (musicSource) {
        alSourceStop(musicSource);
        alDeleteSources(1, &musicSource);
        musicSource = 0;
    }

    for (ALuint source : sources) {
        alSourceStop(source);
    }
    alDeleteSources(static_cast<ALsizei>(sources.size()), sources.data());
    sources.clear();

    // Delete buffers
    for (auto& pair : soundCache) {
        alDeleteBuffers(1, &pair.second.buffer);
    }
    soundCache.clear();

    // Destroy context
    alcMakeContextCurrent(nullptr);
    if (context) {
        alcDestroyContext(context);
        context = nullptr;
    }

    // Close device
    if (device) {
        alcCloseDevice(device);
        device = nullptr;
    }

    initialized = false;
}

void SoundEngine::scanSoundVariants() {
    // Scan sound directories to find variants
    // Only "sound" and "newsound" are used in Java Beta 1.2 (NOT sound3)
    std::vector<std::string> soundDirs = {
        soundBasePath + "/sound",
        soundBasePath + "/newsound"
    };

    for (const auto& baseDir : soundDirs) {
        if (!std::filesystem::exists(baseDir)) continue;

        for (const auto& entry : std::filesystem::recursive_directory_iterator(baseDir)) {
            if (!entry.is_regular_file()) continue;

            std::string path = entry.path().string();
            std::string ext = entry.path().extension().string();

            if (ext != ".ogg" && ext != ".wav") continue;

            // Convert path to sound name
            // e.g., "../resources/sound/step/grass1.ogg" -> "step.grass"
            std::string relativePath = entry.path().lexically_relative(baseDir).string();

            // Remove extension
            size_t dotPos = relativePath.rfind('.');
            if (dotPos != std::string::npos) {
                relativePath = relativePath.substr(0, dotPos);
            }

            // Remove trailing digits (variant number)
            while (!relativePath.empty() && std::isdigit(relativePath.back())) {
                relativePath.pop_back();
            }

            // Replace path separators with dots
            std::replace(relativePath.begin(), relativePath.end(), '/', '.');
            std::replace(relativePath.begin(), relativePath.end(), '\\', '.');

            // Add to variants map
            soundVariants[relativePath].push_back(path);
        }
    }

    std::cout << "Scanned " << soundVariants.size() << " sound categories" << std::endl;
}

std::string SoundEngine::resolveSoundPath(const std::string& soundName) {
    auto it = soundVariants.find(soundName);
    if (it == soundVariants.end() || it->second.empty()) {
        // Try direct path (only sound and newsound folders, NOT sound3)
        std::string directPath = soundBasePath + "/sound/" + soundName + ".ogg";
        if (std::filesystem::exists(directPath)) {
            return directPath;
        }
        // Try with slash replacement
        std::string altName = soundName;
        std::replace(altName.begin(), altName.end(), '.', '/');
        directPath = soundBasePath + "/sound/" + altName + ".ogg";
        if (std::filesystem::exists(directPath)) {
            return directPath;
        }
        directPath = soundBasePath + "/newsound/" + altName + ".ogg";
        if (std::filesystem::exists(directPath)) {
            return directPath;
        }
        return "";
    }

    // Pick random variant
    static std::random_device rd;
    static std::mt19937 gen(rd());
    std::uniform_int_distribution<size_t> dist(0, it->second.size() - 1);
    return it->second[dist(gen)];
}

// Convert stereo audio to mono by averaging channels
// This is essential for proper 3D spatialization - OpenAL cannot spatialize stereo sounds
static std::vector<short> convertStereoToMono(const short* stereoData, int samples) {
    std::vector<short> monoData(samples);
    for (int i = 0; i < samples; i++) {
        // Average left and right channels
        int left = stereoData[i * 2];
        int right = stereoData[i * 2 + 1];
        monoData[i] = static_cast<short>((left + right) / 2);
    }
    return monoData;
}

SoundBuffer* SoundEngine::loadOgg(const std::string& path) {
    int channels, sampleRate;
    short* output;
    int samples = stb_vorbis_decode_filename(path.c_str(), &channels, &sampleRate, &output);

    if (samples <= 0) {
        std::cerr << "Failed to decode OGG file: " << path << std::endl;
        return nullptr;
    }

    // Convert stereo to mono for proper 3D spatialization
    std::vector<short> audioData;
    if (channels == 2) {
        audioData = convertStereoToMono(output, samples);
        channels = 1;
    } else {
        audioData = std::vector<short>(output, output + samples);
    }

    free(output);

    // Create buffer - always mono for 3D audio
    ALuint buffer;
    alGenBuffers(1, &buffer);
    alBufferData(buffer, AL_FORMAT_MONO16, audioData.data(),
                 static_cast<ALsizei>(audioData.size() * sizeof(short)), sampleRate);

    SoundBuffer soundBuffer;
    soundBuffer.buffer = buffer;
    soundBuffer.channels = channels;
    soundBuffer.sampleRate = sampleRate;

    soundCache[path] = soundBuffer;
    return &soundCache[path];
}

SoundBuffer* SoundEngine::loadWav(const std::string& path) {
    // Load WAV file (simplified - only supports basic WAV)
    std::ifstream file(path, std::ios::binary);
    if (!file.is_open()) {
        return nullptr;
    }

    // Read RIFF header
    char riff[4];
    file.read(riff, 4);
    if (std::strncmp(riff, "RIFF", 4) != 0) {
        return nullptr;
    }

    // Skip file size
    file.seekg(4, std::ios::cur);

    // Check WAVE format
    char wave[4];
    file.read(wave, 4);
    if (std::strncmp(wave, "WAVE", 4) != 0) {
        return nullptr;
    }

    // Find fmt chunk
    char chunkId[4];
    uint32_t chunkSize;

    while (file.read(chunkId, 4)) {
        file.read(reinterpret_cast<char*>(&chunkSize), 4);

        if (std::strncmp(chunkId, "fmt ", 4) == 0) {
            uint16_t audioFormat, numChannels;
            uint32_t sampleRate, byteRate;
            uint16_t blockAlign, bitsPerSample;

            file.read(reinterpret_cast<char*>(&audioFormat), 2);
            file.read(reinterpret_cast<char*>(&numChannels), 2);
            file.read(reinterpret_cast<char*>(&sampleRate), 4);
            file.read(reinterpret_cast<char*>(&byteRate), 4);
            file.read(reinterpret_cast<char*>(&blockAlign), 2);
            file.read(reinterpret_cast<char*>(&bitsPerSample), 2);

            // Skip any extra format bytes
            if (chunkSize > 16) {
                file.seekg(chunkSize - 16, std::ios::cur);
            }

            // Find data chunk
            while (file.read(chunkId, 4)) {
                file.read(reinterpret_cast<char*>(&chunkSize), 4);

                if (std::strncmp(chunkId, "data", 4) == 0) {
                    // Read audio data
                    std::vector<char> data(chunkSize);
                    file.read(data.data(), chunkSize);

                    // Convert to 16-bit mono for proper 3D spatialization
                    int samples = chunkSize / (numChannels * (bitsPerSample / 8));
                    std::vector<short> audioData(samples);

                    if (bitsPerSample == 8) {
                        // Convert 8-bit to 16-bit
                        const unsigned char* src = reinterpret_cast<const unsigned char*>(data.data());
                        for (int i = 0; i < samples; i++) {
                            if (numChannels == 2) {
                                // Stereo to mono: average both channels
                                int left = (src[i * 2] - 128) * 256;
                                int right = (src[i * 2 + 1] - 128) * 256;
                                audioData[i] = static_cast<short>((left + right) / 2);
                            } else {
                                audioData[i] = static_cast<short>((src[i] - 128) * 256);
                            }
                        }
                    } else {
                        // 16-bit
                        const short* src = reinterpret_cast<const short*>(data.data());
                        if (numChannels == 2) {
                            // Stereo to mono
                            audioData = convertStereoToMono(src, samples);
                        } else {
                            audioData = std::vector<short>(src, src + samples);
                        }
                    }

                    // Create buffer - always mono 16-bit for 3D audio
                    ALuint buffer;
                    alGenBuffers(1, &buffer);
                    alBufferData(buffer, AL_FORMAT_MONO16, audioData.data(),
                                 static_cast<ALsizei>(audioData.size() * sizeof(short)), sampleRate);

                    SoundBuffer soundBuffer;
                    soundBuffer.buffer = buffer;
                    soundBuffer.channels = 1;  // Always mono now
                    soundBuffer.sampleRate = sampleRate;

                    soundCache[path] = soundBuffer;
                    return &soundCache[path];
                }

                file.seekg(chunkSize, std::ios::cur);
            }
        } else {
            file.seekg(chunkSize, std::ios::cur);
        }
    }

    return nullptr;
}

SoundBuffer* SoundEngine::loadSound(const std::string& path) {
    auto it = soundCache.find(path);
    if (it != soundCache.end()) {
        return &it->second;
    }

    // Check file extension
    std::string ext = std::filesystem::path(path).extension().string();
    std::transform(ext.begin(), ext.end(), ext.begin(), ::tolower);

    if (ext == ".ogg") {
        return loadOgg(path);
    } else if (ext == ".wav") {
        return loadWav(path);
    }

    std::cerr << "Unsupported audio format: " << path << std::endl;
    return nullptr;
}

ALuint SoundEngine::getAvailableSource() {
    for (ALuint source : sources) {
        ALint state;
        alGetSourcei(source, AL_SOURCE_STATE, &state);
        if (state != AL_PLAYING) {
            return source;
        }
    }

    // All sources busy, return first one (will interrupt oldest sound)
    return sources[0];
}

void SoundEngine::playSound(const std::string& soundName, float volume, float pitch) {
    if (!initialized) return;

    std::string path = resolveSoundPath(soundName);
    if (path.empty()) {
        // Don't spam console for missing sounds
        return;
    }

    play(path, volume, pitch);
}

void SoundEngine::playSound3D(const std::string& soundName, float x, float y, float z,
                               float volume, float pitch) {
    if (!initialized) return;

    std::string path = resolveSoundPath(soundName);
    if (path.empty()) {
        return;
    }

    play3D(path, x, y, z, volume, pitch);
}

void SoundEngine::play(const std::string& sound, float volume, float pitch) {
    if (!initialized) return;

    SoundBuffer* buffer = loadSound(sound);
    if (!buffer) return;

    ALuint source = getAvailableSource();

    alSourcei(source, AL_BUFFER, buffer->buffer);
    alSourcef(source, AL_GAIN, volume * soundVolume * masterVolume);
    alSourcef(source, AL_PITCH, pitch);
    alSourcei(source, AL_SOURCE_RELATIVE, AL_TRUE);
    alSource3f(source, AL_POSITION, 0, 0, 0);

    alSourcePlay(source);
}

void SoundEngine::play3D(const std::string& sound, float x, float y, float z,
                          float volume, float pitch) {
    if (!initialized) return;

    // Match Java's behavior: if volume > 1.0, extend audible distance but cap gain at 1.0
    float dist = 16.0f;  // Java's SOUND_DISTANCE
    if (volume > 1.0f) {
        dist *= volume;
        volume = 1.0f;
    }

    // Calculate distance to listener and cull sounds beyond max audible distance
    // Using 4.0x multiplier for standard falloff range
    float maxDist = dist * 4.0f;
    float dx = x - listenerX;
    float dy = y - listenerY;
    float dz = z - listenerZ;
    float distSq = dx * dx + dy * dy + dz * dz;
    if (distSq > maxDist * maxDist) {
        return;  // Sound is too far away to hear
    }

    SoundBuffer* buffer = loadSound(sound);
    if (!buffer) return;

    ALuint source = getAvailableSource();

    // Calculate distance attenuation using a parabolic curve
    // gain = (1 - distance/maxDist)^2
    // This gives smooth falloff: at maxDist it's silent, at maxDist/2 it's 25%, etc.
    float actualDistance = std::sqrt(distSq);
    float attenuationGain = 1.0f;
    if (actualDistance > 0.0f) {
        float falloffRatio = actualDistance / maxDist;
        if (falloffRatio >= 1.0f) {
            attenuationGain = 0.0f;
        } else {
            attenuationGain = (1.0f - falloffRatio) * (1.0f - falloffRatio);  // Parabolic curve
        }
    }

    // Apply attenuation to the volume
    float finalGain = volume * soundVolume * masterVolume * attenuationGain;

    alSourcei(source, AL_BUFFER, buffer->buffer);
    alSourcef(source, AL_GAIN, finalGain);
    alSourcef(source, AL_PITCH, pitch);
    alSourcei(source, AL_SOURCE_RELATIVE, AL_FALSE);
    alSource3f(source, AL_POSITION, x, y, z);

    // Disable OpenAL attenuation since we're doing it manually
    alSourcef(source, AL_REFERENCE_DISTANCE, 1.0f);
    alSourcef(source, AL_ROLLOFF_FACTOR, 0.0f);
    alSourcef(source, AL_MAX_DISTANCE, 999999.0f);

    alSourcePlay(source);
}

void SoundEngine::playMusic(const std::string& music) {
    if (!initialized) return;

    if (music == currentMusic && isMusicPlaying()) return;

    stopMusic();

    SoundBuffer* buffer = loadSound(music);
    if (!buffer) return;

    alSourcei(musicSource, AL_BUFFER, buffer->buffer);
    alSourcef(musicSource, AL_GAIN, musicVolume * masterVolume);
    alSourcei(musicSource, AL_LOOPING, AL_TRUE);
    alSourcei(musicSource, AL_SOURCE_RELATIVE, AL_TRUE);
    alSource3f(musicSource, AL_POSITION, 0, 0, 0);

    alSourcePlay(musicSource);
    currentMusic = music;
}

void SoundEngine::stopMusic() {
    if (!initialized) return;

    alSourceStop(musicSource);
    currentMusic.clear();
}

bool SoundEngine::isMusicPlaying() const {
    if (!initialized || !musicSource) return false;

    ALint state;
    alGetSourcei(musicSource, AL_SOURCE_STATE, &state);
    return state == AL_PLAYING;
}

void SoundEngine::setListenerPosition(float x, float y, float z) {
    if (!initialized) return;
    listenerX = x;
    listenerY = y;
    listenerZ = z;
    alListener3f(AL_POSITION, x, y, z);
}

void SoundEngine::setListenerOrientation(float atX, float atY, float atZ,
                                          float upX, float upY, float upZ) {
    if (!initialized) return;
    float orientation[] = {atX, atY, atZ, upX, upY, upZ};
    alListenerfv(AL_ORIENTATION, orientation);
}

void SoundEngine::setListenerVelocity(float vx, float vy, float vz) {
    if (!initialized) return;
    alListener3f(AL_VELOCITY, vx, vy, vz);
}

void SoundEngine::setMasterVolume(float volume) {
    masterVolume = volume;
    alListenerf(AL_GAIN, masterVolume);
}

void SoundEngine::setMusicVolume(float volume) {
    musicVolume = volume;
    if (musicSource) {
        alSourcef(musicSource, AL_GAIN, musicVolume * masterVolume);
    }
}

void SoundEngine::setSoundVolume(float volume) {
    soundVolume = volume;
}

void SoundEngine::update(Entity* listener, float /*partialTick*/) {
    if (!initialized || !listener) return;

    // Update listener position
    setListenerPosition(
        static_cast<float>(listener->x),
        static_cast<float>(listener->y + listener->eyeHeight),
        static_cast<float>(listener->z)
    );

    // Update listener orientation
    Vec3 look = listener->getLookVector();
    setListenerOrientation(
        static_cast<float>(look.x),
        static_cast<float>(look.y),
        static_cast<float>(look.z),
        0, 1, 0
    );

    // Update listener velocity
    setListenerVelocity(
        static_cast<float>(listener->xd),
        static_cast<float>(listener->yd),
        static_cast<float>(listener->zd)
    );

    cleanupSources();
}

void SoundEngine::cleanupSources() {
    // Clean up finished sources
    for (ALuint source : sources) {
        ALint state;
        alGetSourcei(source, AL_SOURCE_STATE, &state);
        if (state == AL_STOPPED) {
            alSourcei(source, AL_BUFFER, 0);
        }
    }
}

} // namespace mc
