cmake_minimum_required(VERSION 3.20)
project(MinecraftCpp VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

# =============================================================================
# Platform detection and backend selection
# =============================================================================
option(FORCE_OPENGL "Force OpenGL backend on macOS (instead of Metal)" ON)

if(APPLE AND NOT FORCE_OPENGL)
    set(USE_METAL ON)
    set(USE_OPENGL OFF)
    add_compile_definitions(MC_RENDERER_METAL=1)
    message(STATUS "Using Metal rendering backend")
else()
    set(USE_METAL OFF)
    set(USE_OPENGL ON)
    add_compile_definitions(MC_RENDERER_OPENGL=1)
    message(STATUS "Using OpenGL rendering backend")
endif()

# =============================================================================
# Common dependencies
# =============================================================================
find_package(glfw3 3.3 REQUIRED)
find_package(glm REQUIRED)

# OpenGL dependencies (needed during transition, even on macOS)
find_package(OpenGL REQUIRED)
find_package(GLEW REQUIRED)

# OpenAL (platform-specific handling)
if(APPLE)
    find_path(OPENAL_INCLUDE_DIR AL/al.h
        PATHS /opt/homebrew/opt/openal-soft/include /usr/local/opt/openal-soft/include
        NO_DEFAULT_PATH
    )
    find_library(OPENAL_LIBRARY
        NAMES openal OpenAL al
        PATHS /opt/homebrew/opt/openal-soft/lib /usr/local/opt/openal-soft/lib
        NO_DEFAULT_PATH
    )
    if(NOT OPENAL_INCLUDE_DIR OR NOT OPENAL_LIBRARY)
        message(FATAL_ERROR "OpenAL Soft is required on macOS. Install with: brew install openal-soft")
    endif()
    message(STATUS "Using OpenAL Soft: ${OPENAL_LIBRARY}")
else()
    find_package(OpenAL REQUIRED)
endif()

# =============================================================================
# Metal-specific dependencies (macOS only)
# =============================================================================
if(USE_METAL)
    include(FetchContent)

    # metal-cpp - Apple's C++ bindings for Metal
    FetchContent_Declare(
        metal-cpp
        URL https://developer.apple.com/metal/cpp/files/metal-cpp_macOS15_iOS18.zip
    )
    FetchContent_MakeAvailable(metal-cpp)

    # ShaderTranspiler - GLSL to MSL conversion
    FetchContent_Declare(
        ShaderTranspiler
        GIT_REPOSITORY https://github.com/RavEngine/ShaderTranspiler.git
        GIT_TAG master
    )
    set(ST_BUILD_CLI OFF CACHE BOOL "" FORCE)
    set(ST_BUILD_TESTS OFF CACHE BOOL "" FORCE)
    set(ST_ENABLE_WGSL OFF CACHE BOOL "" FORCE)
    set(ST_ENABLE_HLSL OFF CACHE BOOL "" FORCE)
    FetchContent_MakeAvailable(ShaderTranspiler)

    # Find Metal frameworks
    find_library(METAL_FRAMEWORK Metal REQUIRED)
    find_library(METALKIT_FRAMEWORK MetalKit REQUIRED)
    find_library(QUARTZCORE_FRAMEWORK QuartzCore REQUIRED)
    find_library(FOUNDATION_FRAMEWORK Foundation REQUIRED)
    find_library(COCOA_FRAMEWORK Cocoa REQUIRED)

    message(STATUS "Metal framework: ${METAL_FRAMEWORK}")
endif()

# =============================================================================
# Source files
# =============================================================================
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
    src/renderer/GLSLTranspiler.cpp
    src/renderer/MatrixStack.cpp
    src/renderer/ShaderManager.cpp
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
    src/entity/ItemEntity.cpp
    src/entity/Mob.cpp
    src/entity/PathfinderMob.cpp
    src/entity/Animal.cpp
    src/entity/Chicken.cpp
)

set(PATHFINDER_SOURCES
    src/pathfinder/Path.cpp
    src/pathfinder/PathFinder.cpp
)

set(MODEL_SOURCES
    src/renderer/model/ModelPart.cpp
    src/renderer/model/ChickenModel.cpp
)

set(PARTICLE_SOURCES
    src/particle/Particle.cpp
    src/particle/ExplodeParticle.cpp
    src/particle/ParticleEngine.cpp
)

set(AUDIO_SOURCES
    src/audio/SoundEngine.cpp
    src/stb_vorbis_impl.cpp
)

set(GUI_SOURCES
    src/gui/Font.cpp
    src/gui/Gui.cpp
    src/gui/Screen.cpp
    src/gui/Button.cpp
    src/gui/SlideButton.cpp
    src/gui/InventoryScreen.cpp
    src/gui/PauseScreen.cpp
    src/gui/OptionsScreen.cpp
)

set(ITEM_SOURCES
    src/item/Inventory.cpp
    src/item/Item.cpp
)

set(GAMEMODE_SOURCES
    src/gamemode/GameMode.cpp
)

# =============================================================================
# Backend source files
# =============================================================================

# Common backend abstraction
set(BACKEND_COMMON_SOURCES
    src/renderer/backend/RenderDevice.cpp
)

# OpenGL backend sources
set(BACKEND_OPENGL_SOURCES
    src/renderer/backend/opengl/GLRenderDevice.cpp
    src/renderer/backend/opengl/GLShaderPipeline.cpp
    src/renderer/backend/opengl/GLVertexBuffer.cpp
    src/renderer/backend/opengl/GLTexture.cpp
)

# Metal backend sources (using metal-cpp - pure C++ except for window bridging)
set(BACKEND_METAL_SOURCES
    src/renderer/backend/metal/MetalCppImpl.cpp
    src/renderer/backend/metal/MTLRenderDevice.cpp
    src/renderer/backend/metal/MTLShaderPipeline.cpp
    src/renderer/backend/metal/MTLVertexBuffer.cpp
    src/renderer/backend/metal/MTLTexture.cpp
    src/renderer/backend/metal/ShaderCache.cpp
    src/renderer/backend/metal/MetalBridge.mm
)

# Select backend based on platform
if(USE_METAL)
    set(BACKEND_SOURCES ${BACKEND_COMMON_SOURCES} ${BACKEND_METAL_SOURCES})
else()
    set(BACKEND_SOURCES ${BACKEND_COMMON_SOURCES} ${BACKEND_OPENGL_SOURCES})
endif()

# =============================================================================
# Combine all sources
# =============================================================================
set(ALL_SOURCES
    src/main.cpp
    ${UTIL_SOURCES}
    ${PHYS_SOURCES}
    ${CORE_SOURCES}
    ${RENDERER_SOURCES}
    ${BACKEND_SOURCES}
    ${WORLD_SOURCES}
    ${ENTITY_SOURCES}
    ${PATHFINDER_SOURCES}
    ${MODEL_SOURCES}
    ${PARTICLE_SOURCES}
    ${AUDIO_SOURCES}
    ${GUI_SOURCES}
    ${ITEM_SOURCES}
    ${GAMEMODE_SOURCES}
)

# Create executable
add_executable(${PROJECT_NAME} ${ALL_SOURCES})

# =============================================================================
# Include directories
# =============================================================================
target_include_directories(${PROJECT_NAME} PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/include
    ${CMAKE_CURRENT_SOURCE_DIR}/src
    ${OPENAL_INCLUDE_DIR}
    ${OPENGL_INCLUDE_DIRS}
    ${GLEW_INCLUDE_DIRS}
)

if(USE_METAL)
    target_include_directories(${PROJECT_NAME} PRIVATE
        ${metal-cpp_SOURCE_DIR}
        ${metal-cpp_SOURCE_DIR}/Metal
        ${metal-cpp_SOURCE_DIR}/Foundation
        ${metal-cpp_SOURCE_DIR}/QuartzCore
    )
endif()

# =============================================================================
# Link libraries
# =============================================================================
target_link_libraries(${PROJECT_NAME} PRIVATE
    glfw
    glm::glm
    ${OPENAL_LIBRARY}
    OpenGL::GL
    GLEW::GLEW
)

if(USE_METAL)
    target_link_libraries(${PROJECT_NAME} PRIVATE
        ${METAL_FRAMEWORK}
        ${METALKIT_FRAMEWORK}
        ${QUARTZCORE_FRAMEWORK}
        ${FOUNDATION_FRAMEWORK}
        ${COCOA_FRAMEWORK}
        ShaderTranspiler
    )
endif()

# =============================================================================
# Copy resources and shaders
# =============================================================================
add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E copy_directory
    ${CMAKE_SOURCE_DIR}/resources
    $<TARGET_FILE_DIR:${PROJECT_NAME}>/resources
)

add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E copy_directory
    ${CMAKE_SOURCE_DIR}/shaders
    $<TARGET_FILE_DIR:${PROJECT_NAME}>/shaders
)

# =============================================================================
# Platform-specific settings
# =============================================================================
if(WIN32)
    target_compile_definitions(${PROJECT_NAME} PRIVATE _CRT_SECURE_NO_WARNINGS)
endif()

if(MSVC)
    target_compile_options(${PROJECT_NAME} PRIVATE /W4)
else()
    target_compile_options(${PROJECT_NAME} PRIVATE -Wall -Wextra -Wpedantic -Wno-unused-parameter)
endif()

if(CMAKE_BUILD_TYPE STREQUAL "Debug")
    target_compile_definitions(${PROJECT_NAME} PRIVATE DEBUG_BUILD)
endif()
