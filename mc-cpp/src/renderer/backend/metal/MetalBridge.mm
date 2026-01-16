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

        // Set contents scale for HiDPI/Retina displays
        CGFloat scaleFactor = [nsWindow backingScaleFactor];
        metalLayer.contentsScale = scaleFactor;

        // Match layer frame to view bounds (in points)
        metalLayer.frame = contentView.bounds;

        // Set drawable size in pixels
        CGSize viewSize = [contentView bounds].size;
        metalLayer.drawableSize = CGSizeMake(viewSize.width * scaleFactor,
                                              viewSize.height * scaleFactor);

        return (__bridge void*)metalLayer;
    }
}

// Update Metal layer drawable size when window resizes
void updateMetalLayerSize(void* layerPtr, void* windowPtr, int width, int height) {
    @autoreleasepool {
        CAMetalLayer* metalLayer = (__bridge CAMetalLayer*)layerPtr;
        NSWindow* nsWindow = (__bridge NSWindow*)windowPtr;

        // Update the layer's frame to match the content view bounds (in points)
        NSView* contentView = [nsWindow contentView];
        metalLayer.frame = contentView.bounds;

        // Set contentsScale to match the window's backing scale factor
        metalLayer.contentsScale = [nsWindow backingScaleFactor];

        // width and height are already in pixels from GLFW framebuffer callback
        metalLayer.drawableSize = CGSizeMake(width, height);
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

// Create an autorelease pool - call at start of frame
void* metalCreateAutoreleasePool(void) {
    return [[NSAutoreleasePool alloc] init];
}

// Release an autorelease pool - call at end of frame to clean up autoreleased objects
void metalReleaseAutoreleasePool(void* pool) {
    [(NSAutoreleasePool*)pool release];
}

}
