# Minecraft C++ Port

A C++ reimplementation of Minecraft Beta 1.2_02 using modern libraries.

## Dependencies

- **CMake** 3.20+
- **GLFW** 3.3+ (Window management and input)
- **GLEW** (OpenGL extension loading)
- **OpenAL** (3D audio)
- **C++20** compatible compiler

### Installing Dependencies

**Ubuntu/Debian:**
```bash
sudo apt install cmake libglfw3-dev libglew-dev libopenal-dev
```

**Fedora:**
```bash
sudo dnf install cmake glfw-devel glew-devel openal-soft-devel
```

**Arch Linux:**
```bash
sudo pacman -S cmake glfw glew openal
```

**macOS (Homebrew):**
```bash
brew install cmake glfw glew openal-soft
```

## Building

```bash
mkdir build
cd build
cmake ..
make -j$(nproc)
```

## Running

```bash
./MinecraftCpp [options]
```

### Command Line Options

- `--width <n>` - Set window width (default: 854)
- `--height <n>` - Set window height (default: 480)
- `--fullscreen` - Start in fullscreen mode
- `--help` - Show help message

## Controls

| Key | Action |
|-----|--------|
| W/A/S/D | Move |
| Space | Jump |
| Left Shift | Sneak |
| Mouse | Look around |
| Left Click | Break block |
| Right Click | Place block |
| Scroll Wheel | Change hotbar slot |
| F3 | Toggle debug info |
| Escape | Release/grab mouse |

## Resources

You'll need to provide your own `terrain.png` and other resources in the `resources/` directory.
The resource format matches Minecraft Beta 1.2_02.

Required files:
- `resources/terrain.png` - Block textures (256x256, 16x16 tiles)
- `resources/default.png` - Font texture (128x128)
- `resources/gui/items.png` - Item textures

## Project Structure

```
mc-cpp/
├── CMakeLists.txt
├── include/           # Header files
│   ├── core/          # Core game classes
│   ├── renderer/      # Rendering system
│   ├── world/         # World and level classes
│   ├── entity/        # Entity classes
│   ├── audio/         # Audio system
│   ├── gui/           # GUI classes
│   ├── util/          # Utilities
│   └── phys/          # Physics classes
├── src/               # Source files
├── shaders/           # GLSL shaders
└── resources/         # Game resources
```

## Features

- Flat world generation
- Block placement and destruction
- First-person camera with view bobbing
- Collision detection
- Basic lighting
- Chunk-based rendering with frustum culling
- 3D positional audio support
- Debug overlay (F3)

## Architecture

This port mirrors the original Java structure:

| Java Class | C++ Class |
|------------|-----------|
| `Minecraft` | `mc::Minecraft` |
| `Level` | `mc::Level` |
| `Entity` | `mc::Entity` |
| `Player` | `mc::Player` |
| `Tile` | `mc::Tile` |
| `Tesselator` | `mc::Tesselator` |
| `LevelRenderer` | `mc::LevelRenderer` |
| `GameRenderer` | `mc::GameRenderer` |

## License

This is an educational reimplementation. Minecraft is a trademark of Mojang Studios.
