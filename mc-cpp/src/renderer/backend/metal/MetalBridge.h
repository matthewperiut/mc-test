#pragma once

// C interface for Objective-C++ bridge functions
// These are the only functions that need Objective-C++ implementation

#ifdef __cplusplus
extern "C" {
#endif

// Set up Metal layer on GLFW window, returns CAMetalLayer* as void*
void* getMetalLayerFromWindow(void* glfwNSWindow);

// Update Metal layer drawable size when window resizes
void updateMetalLayerSize(void* metalLayer, void* nsWindow, int width, int height);

// Get the backing scale factor for retina displays
float getWindowScaleFactor(void* glfwNSWindow);

// Set vsync (display sync) on Metal layer
void setMetalLayerVsync(void* metalLayer, bool enabled);

// Create an autorelease pool - call at start of frame
void* metalCreateAutoreleasePool(void);

// Release an autorelease pool - call at end of frame to clean up autoreleased objects
void metalReleaseAutoreleasePool(void* pool);

#ifdef __cplusplus
}
#endif
