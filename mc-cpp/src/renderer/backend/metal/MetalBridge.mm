// MetalBridge.mm - Minimal Objective-C++ bridging for window/layer handling
// This is the only file that requires Objective-C++

#import <Cocoa/Cocoa.h>
#import <QuartzCore/CAMetalLayer.h>
#import <Metal/Metal.h>

extern "C" {

// Get the native NSWindow from GLFW window handle
void* getMetalLayerFromWindow(void* glfwWindow) {
    @autoreleasepool {
        // GLFW provides glfwGetCocoaWindow() which returns NSWindow*
        // This function is called after that to set up the Metal layer
        NSWindow* nsWindow = (__bridge NSWindow*)glfwWindow;

        // Create Metal layer
        CAMetalLayer* metalLayer = [CAMetalLayer layer];
        metalLayer.device = MTLCreateSystemDefaultDevice();
        metalLayer.pixelFormat = MTLPixelFormatBGRA8Unorm;
        metalLayer.framebufferOnly = YES;

        // Set the layer on the window's content view
        NSView* contentView = [nsWindow contentView];
        [contentView setWantsLayer:YES];
        [contentView setLayer:metalLayer];

        // Match layer size to view
        CGSize viewSize = [contentView bounds].size;
        metalLayer.drawableSize = CGSizeMake(viewSize.width * contentView.window.backingScaleFactor,
                                              viewSize.height * contentView.window.backingScaleFactor);

        return (__bridge void*)metalLayer;
    }
}

// Update Metal layer drawable size when window resizes
void updateMetalLayerSize(void* layerPtr, int width, int height, float scaleFactor) {
    @autoreleasepool {
        CAMetalLayer* metalLayer = (__bridge CAMetalLayer*)layerPtr;
        metalLayer.drawableSize = CGSizeMake(width * scaleFactor, height * scaleFactor);
    }
}

// Get the backing scale factor for retina displays
float getWindowScaleFactor(void* glfwWindow) {
    @autoreleasepool {
        NSWindow* nsWindow = (__bridge NSWindow*)glfwWindow;
        return (float)[nsWindow backingScaleFactor];
    }
}

// Set vsync (display sync) on Metal layer
void setMetalLayerVsync(void* layerPtr, bool enabled) {
    @autoreleasepool {
        CAMetalLayer* metalLayer = (__bridge CAMetalLayer*)layerPtr;
        metalLayer.displaySyncEnabled = enabled ? YES : NO;
    }
}

// Drain the autorelease pool - call once per frame to prevent memory buildup
void metalDrainAutoreleasePool(void) {
    @autoreleasepool {
        // Empty pool - just creating and draining cleans up autoreleased objects
    }
}

}
