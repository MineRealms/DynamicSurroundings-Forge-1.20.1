# Phase 5: Shader System Implementation

## Overview
Implemented a modern GLSL shader management system for Minecraft 1.20.1 Forge, replacing the legacy 1.12.2 shader framework. This system provides the foundation for advanced visual effects like the Aurora system.

## Implementation Date
2026-05-02

## Files Created

### Core Shader System
1. **ShaderManager.java** (130 lines)
   - Location: `src/main/java/org/orecruncher/dsurround/shader/ShaderManager.java`
   - Purpose: Central manager for all shader programs
   - Features:
     - Shader registration and lifecycle management
     - OpenGL capability detection
     - Automatic cleanup on shutdown
     - Aurora shader pre-registration

2. **ShaderProgram.java** (270 lines)
   - Location: `src/main/java/org/orecruncher/dsurround/shader/ShaderProgram.java`
   - Purpose: Represents a compiled GLSL shader program
   - Features:
     - Lazy compilation on first use
     - Vertex and fragment shader loading from resources
     - Uniform variable management (float, vec2, vec4, int)
     - Error handling and logging
     - Callback interface for setting uniforms
     - AutoCloseable for resource management

### Shader Resources
3. **aurora.vert** (16 lines)
   - Location: `src/main/resources/assets/dsurround/shaders/aurora.vert`
   - Purpose: Vertex shader for aurora rendering
   - GLSL Version: 120
   - Features: Basic vertex transformation

4. **aurora.frag** (205 lines)
   - Location: `src/main/resources/assets/dsurround/shaders/aurora.frag`
   - Purpose: Fragment shader for aurora rendering
   - GLSL Version: 120
   - Features:
     - Perlin noise generation (4 octaves)
     - Fractal Brownian Motion (FBM)
     - Multi-layer aurora rendering (top/middle/bottom colors)
     - Time-based animation
     - Edge fading for seamless blending
   - Credit: Based on Mattenii's Shadertoy (CC BY-NC-SA 3.0)

## Integration

### Client Initialization
Modified `Client.java` to initialize ShaderManager:
```java
// Initialize shader system
ShaderManager.initialize();
```

## Technical Details

### Shader Compilation Pipeline
1. **Lazy Loading**: Shaders are compiled on first use to avoid startup delays
2. **Error Handling**: Comprehensive error checking at each stage:
   - Shader creation
   - Source loading
   - Compilation
   - Linking
   - Validation
3. **Caching**: Uniform locations are cached for performance

### Uniform Management
Supported uniform types:
- `float` - Single floating-point value
- `vec2` - 2D vector (x, y)
- `vec4` - 4D vector (x, y, z, w) - used for colors
- `int` - Integer value

### OpenGL Integration
- Uses LWJGL OpenGL bindings (GL20)
- Requires OpenGL 2.0+ for GLSL support
- Runs on render thread only (RenderSystem.assertOnRenderThread)

## Aurora Shader Details

### Uniforms
- `time` (float): Animation time in seconds
- `resolution` (vec2): Render area size
- `topColor` (vec4): Top band color (red)
- `middleColor` (vec4): Middle band color (green)
- `bottomColor` (vec4): Bottom band color (blue)
- `alpha` (float): Overall transparency

### Rendering Technique
1. **Noise Generation**: Multi-octave Perlin noise for organic movement
2. **FBM Layers**: Fractal Brownian Motion creates complex patterns
3. **Color Blending**: Three color layers blend based on noise values
4. **Edge Fading**: Smooth alpha transition at quad edges (0.25 threshold)
5. **Animation**: Time-based noise offset creates flowing motion

## API Usage Example

```java
// Get the aurora shader
ShaderProgram aurora = ShaderManager.AURORA;

// Use the shader with uniforms
aurora.use(shader -> {
    shader.setUniform("time", currentTime);
    shader.setUniform("resolution", width, height);
    shader.setUniform("topColor", 1.0f, 0.0f, 0.0f, 1.0f);
    shader.setUniform("middleColor", 0.0f, 1.0f, 0.0f, 1.0f);
    shader.setUniform("bottomColor", 0.0f, 0.0f, 1.0f, 1.0f);
    shader.setUniform("alpha", 0.8f);
});

// Render geometry here...

// Stop using the shader
aurora.unUse();
```

## Differences from 1.12.2 Implementation

### Old System (1.12.2)
- Used external library: `org.orecruncher.lib.gfx.shaders.ShaderProgram`
- Relied on `OpenGlHelper.areShadersSupported()`
- Used `GlStateManager` for state management

### New System (1.20.1)
- Self-contained implementation
- Direct OpenGL capability checking
- Uses modern `RenderSystem` and `GlStateManager`
- Lazy compilation for better startup performance
- Improved error handling and logging
- Resource loading via Minecraft's ResourceManager

## Performance Considerations

1. **Lazy Compilation**: Shaders compile on first use, not at startup
2. **Uniform Caching**: Uniform locations cached after first lookup
3. **Resource Management**: Proper cleanup via AutoCloseable
4. **Render Thread Safety**: All OpenGL calls on render thread

## Testing Recommendations

1. **Shader Compilation**
   - Test on systems with different OpenGL versions
   - Verify error messages for compilation failures
   - Check shader validation warnings

2. **Uniform Setting**
   - Verify all uniform types work correctly
   - Test with missing uniforms (should log warning, not crash)
   - Validate uniform value ranges

3. **Resource Loading**
   - Test with resource packs
   - Verify shader files load correctly
   - Check error handling for missing files

4. **Performance**
   - Profile shader compilation time
   - Monitor uniform setting overhead
   - Check for memory leaks on cleanup

## Known Limitations

1. **GLSL Version**: Currently uses GLSL 120 (OpenGL 2.1)
   - Modern systems support GLSL 330+ (OpenGL 3.3+)
   - May need upgrade for advanced features

2. **Shader Hot-Reload**: Not implemented
   - Requires game restart to reload shaders
   - Could be added via resource reload listener

3. **Shader Variants**: No support for shader permutations
   - Each shader is a single program
   - No preprocessor defines or variants

## Future Enhancements

1. **GLSL 330+ Support**: Upgrade to modern GLSL
2. **Shader Hot-Reload**: Reload shaders on resource pack change
3. **Shader Variants**: Support for shader permutations
4. **Geometry Shaders**: Add geometry shader support
5. **Compute Shaders**: Add compute shader support (OpenGL 4.3+)
6. **Shader Debugging**: Better error messages and debugging tools

## Dependencies

### Minecraft/Forge
- `com.mojang.blaze3d.platform.GlStateManager`
- `com.mojang.blaze3d.systems.RenderSystem`
- `net.minecraft.client.Minecraft`
- `net.minecraft.resources.ResourceLocation`
- `net.minecraft.server.packs.resources.Resource`

### LWJGL
- `org.lwjgl.opengl.GL20` - OpenGL 2.0 bindings

### Internal
- `org.orecruncher.dsurround.Constants`
- `org.orecruncher.dsurround.lib.logging.IModLog`

## Code Statistics

- **Total Lines**: ~400 lines (excluding shaders)
- **Java Files**: 2
- **Shader Files**: 2
- **Classes**: 2
- **Interfaces**: 1 (IShaderUseCallback)

## Git Commit Message

```
Phase 5: Implement modern shader system for 1.20.1

Replaces legacy 1.12.2 shader framework with self-contained implementation.

Core Components:
- ShaderManager: Central shader registry and lifecycle management
  - Lazy compilation on first use
  - OpenGL capability detection
  - Automatic cleanup
- ShaderProgram: Compiled GLSL program wrapper
  - Vertex and fragment shader loading
  - Uniform management (float, vec2, vec4, int)
  - Error handling and validation
  - Callback interface for uniform setting

Shader Resources:
- aurora.vert: Basic vertex transformation (GLSL 120)
- aurora.frag: Complex aurora rendering (GLSL 120)
  - Multi-octave Perlin noise
  - Fractal Brownian Motion
  - Three-layer color blending
  - Time-based animation
  - Edge fading

Integration:
- Initialized in Client.onComplete()
- Aurora shader pre-registered

Technical Details:
- Uses LWJGL GL20 bindings
- Requires OpenGL 2.0+
- Render thread safety enforced
- Uniform location caching
- Resource loading via Minecraft ResourceManager

Stats: ~400 lines, 2 Java files, 2 shader files

Foundation for Phase 6 (Aurora system).
```

## Related Documentation

- Phase 6: Aurora System (depends on this)
- GLSL Specification: https://www.khronos.org/opengl/wiki/OpenGL_Shading_Language
- Shadertoy Aurora: https://www.shadertoy.com/view/MsjfRG

## License

Shader code based on Mattenii's work:
- License: Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0)
- Source: https://www.shadertoy.com/view/MsjfRG
- Modified for Minecraft integration

Java code:
- License: MIT License
- Copyright: OreCruncher
