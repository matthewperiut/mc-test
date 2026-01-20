cmake_minimum_required(VERSION 3.20)

# Ensure CMAKE_MSVC_RUNTIME_LIBRARY is honored (for /MT, /MTd)
cmake_policy(SET CMP0091 NEW)

project(MinecraftCpp VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

# =============================================================================
# Options
# =============================================================================
option(FORCE_OPENGL "Force OpenGL backend on macOS (instead of Metal)" OFF)
option(ENABLE_SANITIZERS "Enable AddressSanitizer for memory leak detection" OFF)

# On Windows, prefer fetching deps and building them statically (instead of using global/system libs)
option(MC_FETCH_DEPS "Fetch/build GLFW/GLEW/OpenAL-soft on Windows" ON)

# Prefer static libs when deps are added as subprojects
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)

# Static MSVC runtime: /MT (Release) + /MTd (Debug)
if(MSVC)
    set(CMAKE_MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>" CACHE STRING "" FORCE)
endif()

# =============================================================================
# Platform detection and backend selection
# =============================================================================
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
# Dependencies
# =============================================================================
include(FetchContent)

# GLM
if(WIN32 AND MC_FETCH_DEPS)
    include(FetchContent)
    FetchContent_Declare(
            glm
            GIT_REPOSITORY https://github.com/g-truc/glm.git
            GIT_TAG        1.0.1
    )
    # GLM is header-only; this creates target glm::glm in modern GLM
    FetchContent_MakeAvailable(glm)
else()
    find_package(glm REQUIRED)
endif()


# OpenGL loader/framework
find_package(OpenGL REQUIRED)

# -------------------------
# Windows: Fetch static deps
# -------------------------
if(WIN32 AND MC_FETCH_DEPS)

    # ------------------------- GLFW (static) -------------------------
    FetchContent_Declare(
            glfw
            GIT_REPOSITORY https://github.com/glfw/glfw.git
            GIT_TAG        3.3.9
    )
    set(GLFW_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
    set(GLFW_BUILD_TESTS    OFF CACHE BOOL "" FORCE)
    set(GLFW_BUILD_DOCS     OFF CACHE BOOL "" FORCE)
    set(GLFW_INSTALL        OFF CACHE BOOL "" FORCE)
    set(BUILD_SHARED_LIBS   OFF CACHE BOOL "" FORCE)
    FetchContent_MakeAvailable(glfw) # target: glfw

    # ------------------------- GLEW (static) -------------------------
    FetchContent_Declare(
            glew
            GIT_REPOSITORY https://github.com/Perlmint/glew-cmake.git
            GIT_TAG        glew-cmake-2.2.0
    )
    set(BUILD_UTILS       OFF CACHE BOOL "" FORCE)
    set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
    FetchContent_MakeAvailable(glew)

    # Provide a consistent target name GLEW::GLEW
    if(TARGET libglew_static)
        add_library(GLEW::GLEW ALIAS libglew_static)
        target_compile_definitions(libglew_static PUBLIC GLEW_STATIC)
    elseif(TARGET libglew_shared)
        # Not desired, but keep it building if it happens
        add_library(GLEW::GLEW ALIAS libglew_shared)
    else()
        message(FATAL_ERROR "GLEW fetched but no libglew_static/libglew_shared target was produced.")
    endif()

    # ------------------------- OpenAL-soft (force STATIC) -------------------------
    FetchContent_Declare(
            openal_soft
            GIT_REPOSITORY https://github.com/kcat/openal-soft.git
            GIT_TAG        1.23.1
    )

    # Disable extras
    set(ALSOFT_EXAMPLES OFF CACHE BOOL "" FORCE)
    set(ALSOFT_UTILS    OFF CACHE BOOL "" FORCE)
    set(ALSOFT_TESTS    OFF CACHE BOOL "" FORCE)
    set(ALSOFT_CONFIG   OFF CACHE BOOL "" FORCE)
    set(ALSOFT_INSTALL  OFF CACHE BOOL "" FORCE)

    # openal-soft uses LIBTYPE (default SHARED). Force static.
    set(LIBTYPE STATIC CACHE STRING "OpenAL-soft library type" FORCE)

    # Also keep global preference static for any implicit add_library() calls
    set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)

    FetchContent_MakeAvailable(openal_soft)

    # openal-soft typically creates target "OpenAL"
    if(TARGET OpenAL AND NOT TARGET OpenAL::OpenAL)
        add_library(OpenAL::OpenAL ALIAS OpenAL)
    endif()

    # Force fetched deps to also use /MT when built as subprojects
    if(MSVC)
        foreach(tgt glfw OpenAL libglew_static libglew_shared)
            if(TARGET ${tgt})
                set_property(TARGET ${tgt} PROPERTY
                        MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>"
                )
            endif()
        endforeach()
    endif()

    # Sanity checks
    if(NOT TARGET glfw)
        message(FATAL_ERROR "GLFW not found and could not be fetched.")
    endif()
    if(NOT TARGET GLEW::GLEW)
        message(FATAL_ERROR "GLEW not found and could not be fetched.")
    endif()
    if(NOT TARGET OpenAL::OpenAL)
        message(FATAL_ERROR "OpenAL not found and could not be fetched.")
    endif()

else()
    # -------------------------
    # Non-Windows: system deps
    # -------------------------
    find_package(glfw3 3.3 REQUIRED)
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
endif()

# =============================================================================
# Metal-specific dependencies (macOS only)
# =============================================================================
if(USE_METAL)
    # metal-cpp - Apple's C++ bindings for Metal
    FetchContent_Declare(
            metal-cpp
            URL https://developer.apple.com/metal/cpp/files/metal-cpp_macOS15_iOS18.zip
            DOWNLOAD_EXTRACT_TIMESTAMP TRUE
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
        src/renderer/GLSLTranspiler.cpp
        src/renderer/MatrixStack.cpp
        src/renderer/ShaderManager.cpp
)

set(WORLD_SOURCES
        src/world/Level.cpp
        src/world/LevelChunk.cpp
        src/world/ChunkCache.cpp
        src/world/Dimension.cpp
        src/world/LightingEngine.cpp
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
set(BACKEND_COMMON_SOURCES
        src/renderer/backend/RenderDevice.cpp
        src/renderer/backend/RenderContext.cpp
)

set(BACKEND_OPENGL_SOURCES
        src/renderer/backend/opengl/GLRenderDevice.cpp
        src/renderer/backend/opengl/GLRenderContext.cpp
        src/renderer/backend/opengl/GLShaderPipeline.cpp
        src/renderer/backend/opengl/GLVertexBuffer.cpp
        src/renderer/backend/opengl/GLTexture.cpp
)

set(BACKEND_METAL_SOURCES
        src/renderer/backend/metal/MetalCppImpl.cpp
        src/renderer/backend/metal/MTLRenderDevice.cpp
        src/renderer/backend/metal/MTLRenderContext.cpp
        src/renderer/backend/metal/MTLShaderPipeline.cpp
        src/renderer/backend/metal/MTLVertexBuffer.cpp
        src/renderer/backend/metal/MTLTexture.cpp
        src/renderer/backend/metal/ShaderCache.cpp
        src/renderer/backend/metal/MetalBridge.mm
)

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

add_executable(${PROJECT_NAME} ${ALL_SOURCES})

# =============================================================================
# Include directories
# =============================================================================
target_include_directories(${PROJECT_NAME} PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/include
        ${CMAKE_CURRENT_SOURCE_DIR}/src
)

# macOS OpenAL-soft include path (manual find_path above)
if(APPLE AND NOT (WIN32 AND MC_FETCH_DEPS))
    target_include_directories(${PROJECT_NAME} PRIVATE
            ${OPENAL_INCLUDE_DIR}
    )
endif()

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
if(WIN32 AND MC_FETCH_DEPS)
    target_link_libraries(${PROJECT_NAME} PRIVATE
            OpenGL::GL
            glfw
            glm::glm
            GLEW::GLEW
            OpenAL::OpenAL
    )

    # Required when statically linking openal-soft (header symbol decoration)
    target_compile_definitions(${PROJECT_NAME} PRIVATE AL_LIBTYPE_STATIC)

else()
    target_link_libraries(${PROJECT_NAME} PRIVATE
            glfw
            glm::glm
            OpenGL::GL
            GLEW::GLEW
    )

    if(APPLE)
        target_link_libraries(${PROJECT_NAME} PRIVATE ${OPENAL_LIBRARY})
    else()
        target_link_libraries(${PROJECT_NAME} PRIVATE OpenAL::OpenAL)
    endif()
endif()

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
    target_compile_definitions(${PROJECT_NAME} PRIVATE
            _CRT_SECURE_NO_WARNINGS
            WIN32_LEAN_AND_MEAN
            VC_EXTRALEAN
    )
endif()

if(MSVC)
    target_compile_options(${PROJECT_NAME} PRIVATE /W4)
else()
    target_compile_options(${PROJECT_NAME} PRIVATE -Wall -Wextra -Wpedantic -Wno-unused-parameter)
endif()

target_compile_definitions(${PROJECT_NAME} PRIVATE
        $<$<CONFIG:Debug>:DEBUG_BUILD>
)

# =============================================================================
# Sanitizers (AddressSanitizer for memory leak detection)
# =============================================================================
if(ENABLE_SANITIZERS)
    if(NOT MSVC)
        if(APPLE)
            # macOS: standalone -fsanitize=leak not supported, use address sanitizer only
            target_compile_options(${PROJECT_NAME} PRIVATE -fsanitize=address)
            target_link_options(${PROJECT_NAME} PRIVATE -fsanitize=address)
            message(STATUS "AddressSanitizer enabled (LeakSanitizer not supported on macOS)")
        else()
            # Linux: both address and leak sanitizers supported
            target_compile_options(${PROJECT_NAME} PRIVATE -fsanitize=address -fsanitize=leak)
            target_link_options(${PROJECT_NAME} PRIVATE -fsanitize=address -fsanitize=leak)
            message(STATUS "AddressSanitizer and LeakSanitizer enabled")
        endif()
    else()
        message(WARNING "Sanitizers not supported with MSVC compiler")
    endif()
endif()
