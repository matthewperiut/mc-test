cmake_minimum_required(VERSION 3.20)
project(MinecraftCpp VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

# Find required packages
find_package(OpenGL REQUIRED)
find_package(glfw3 3.3 REQUIRED)
find_package(GLEW REQUIRED)
find_package(OpenAL REQUIRED)

# Find stb_image for texture loading (header-only)
# We'll include it in our source

# Source files
set(UTIL_SOURCES
    src/util/Mth.cpp
)

set(PHYS_SOURCES
    src/phys/Vec3.cpp
    src/phys/AABB.cpp
    src/phys/HitResult.cpp
)

set(CORE_SOURCES
    src/core/Timer.cpp
    src/core/Options.cpp
    src/core/Minecraft.cpp
    src/core/MouseHandler.cpp
    src/core/KeyMapping.cpp
)

set(RENDERER_SOURCES
    src/renderer/Tesselator.cpp
    src/renderer/Textures.cpp
    src/renderer/Chunk.cpp
    src/renderer/TileRenderer.cpp
    src/renderer/LevelRenderer.cpp
    src/renderer/GameRenderer.cpp
    src/renderer/Frustum.cpp
    src/renderer/ShaderProgram.cpp
)

set(WORLD_SOURCES
    src/world/Level.cpp
    src/world/LevelChunk.cpp
    src/world/ChunkCache.cpp
    src/world/Dimension.cpp
    src/world/tile/Tile.cpp
    src/world/tile/Tiles.cpp
    src/world/levelgen/PerlinNoise.cpp
    src/world/levelgen/RandomLevelSource.cpp
)

set(ENTITY_SOURCES
    src/entity/Entity.cpp
    src/entity/Player.cpp
    src/entity/LocalPlayer.cpp
)

set(AUDIO_SOURCES
    src/audio/SoundEngine.cpp
)

set(GUI_SOURCES
    src/gui/Font.cpp
    src/gui/Gui.cpp
    src/gui/Screen.cpp
)

# Combine all sources
set(ALL_SOURCES
    src/main.cpp
    ${UTIL_SOURCES}
    ${PHYS_SOURCES}
    ${CORE_SOURCES}
    ${RENDERER_SOURCES}
    ${WORLD_SOURCES}
    ${ENTITY_SOURCES}
    ${AUDIO_SOURCES}
    ${GUI_SOURCES}
)

# Create executable
add_executable(${PROJECT_NAME} ${ALL_SOURCES})

# Include directories
target_include_directories(${PROJECT_NAME} PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/include
    ${CMAKE_CURRENT_SOURCE_DIR}/src
    ${OPENGL_INCLUDE_DIRS}
    ${GLEW_INCLUDE_DIRS}
    ${OPENAL_INCLUDE_DIR}
)

# Link libraries
target_link_libraries(${PROJECT_NAME} PRIVATE
    OpenGL::GL
    glfw
    GLEW::GLEW
    ${OPENAL_LIBRARY}
)

# Copy resources to build directory
add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E copy_directory
    ${CMAKE_SOURCE_DIR}/resources
    $<TARGET_FILE_DIR:${PROJECT_NAME}>/resources
)

# Copy shaders to build directory
add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E copy_directory
    ${CMAKE_SOURCE_DIR}/shaders
    $<TARGET_FILE_DIR:${PROJECT_NAME}>/shaders
)

# Platform-specific settings
if(WIN32)
    target_compile_definitions(${PROJECT_NAME} PRIVATE _CRT_SECURE_NO_WARNINGS)
endif()

if(MSVC)
    target_compile_options(${PROJECT_NAME} PRIVATE /W4)
else()
    target_compile_options(${PROJECT_NAME} PRIVATE -Wall -Wextra -Wpedantic)
endif()

# Debug/Release configurations
if(CMAKE_BUILD_TYPE STREQUAL "Debug")
    target_compile_definitions(${PROJECT_NAME} PRIVATE DEBUG_BUILD)
endif()
