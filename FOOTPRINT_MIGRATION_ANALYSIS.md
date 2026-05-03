# Footprint System Migration Analysis
## 1.12.2 → 1.20.1 Forge

**Date:** 2026-05-03  
**Status:** Planning Phase

---

## Executive Summary

The footprint system from 1.12.2 needs to be migrated to 1.20.1. This system renders visual footprints on surfaces like snow, sand, and dirt when entities walk over them. The migration requires adapting to Minecraft 1.20.1's modern particle system and rendering pipeline.

---

## 1.12.2 Architecture Overview

### Core Components

#### 1. **Footprint Data Class** (`Footprint.java`)
- Simple data holder for footprint information
- Fields: style, entity, stepLoc, isRightFoot, rotation, scale
- Factory method: `Footprint.produce(...)`

#### 2. **FootprintStyle Enum** (`FootprintStyle.java`)
- 7 different footprint styles:
  - `SHOE` - Human shoe print
  - `SQUARE` - Generic square
  - `HORSESHOE` - Horse hoof
  - `BIRD` - Bird claw
  - `PAW` - Animal paw
  - `SQUARE_SOLID` - Solid square
  - `LOWRES_SQUARE` - Low-res square

#### 3. **MoteFootprint Particle** (`MoteFootprint.java`)
- Extends `MoteAgeable` (custom particle base class)
- Renders footprint texture on ground
- Features:
  - Z-fighting prevention (micro Y offset)
  - Rotation calculation during construction
  - Age-based alpha fading
  - Weather-based aging (faster in rain)
  - Snow layer detection
  - Texture UV mapping (8 styles in one texture)

#### 4. **ParticleCollectionFootprint** (`ParticleCollectionFootprint.java`)
- Custom particle collection for batch rendering
- Disables depth mask for proper ground rendering
- Uses standard blending

#### 5. **EntityFootprintEffect** (`EntityFootprintEffect.java`)
- Entity effect handler
- Creates Generator for each entity
- Separate PlayerFootprintEffect for style switching

#### 6. **Generator Integration** (`Generator.java`)
- Lines 398-444: Footprint generation logic
- Checks `shouldProducePrint()` conditions
- Calculates foot position based on entity rotation
- Adds footprints to queue for rendering
- Integration with footstep sound system

### Texture System
- **Texture:** `assets/dsurround/textures/particles/footprint.png`
- **Layout:** 8 horizontal strips (one per style)
- **UV Mapping:** Each style is 1/8 width, left/right foot is 1/16 width

### Rendering Pipeline (1.12.2)
```
Generator.generateFootsteps()
  → findAssociation() 
  → shouldProducePrint() check
  → Footprint.produce()
  → footprints.add()
  → GENERATE_PRINT consumer
  → ParticleCollections.addFootprint()
  → MoteFootprint created
  → ParticleCollectionFootprint renders
```

---

## 1.20.1 Architecture Analysis

### Particle System Changes

#### Modern Particle Base Classes
- **`TextureSheetParticle`** - Base for textured particles
- **`SimpleAnimatedParticle`** - For animated sprites
- **`ParticleRenderType`** - Defines rendering behavior

#### Existing 1.20.1 Particles
1. **DustParticle** - Desert dust storms
2. **FireflyParticle** - Animated firefly
3. **FrostBreathParticle** - Cold breath effect
4. **RainSplashParticle** - Rain impact
5. **SnowParticle** - Snow weather
6. **WaterRippleParticle** - Water surface ripples

#### Particle Registration Pattern
```java
// Modern 1.20.1 pattern
public class FootprintParticle extends TextureSheetParticle {
    public FootprintParticle(ClientLevel level, double x, double y, double z, ...) {
        super(level, x, y, z, 0, 0, 0);
        // Setup
    }
    
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
```

### Footstep System in 1.20.1

#### Existing Components
- **`FootstepAccumulator`** - Tracks entity movement
- **`FootstepProcessor`** - Processes footstep events
- **`FootstepContext`** - Context data for footsteps
- **`FootstepListenerRegistry`** - Event listeners
- **`FootstepSoundEmitter`** - Sound playback

#### Missing Components
- ❌ No footprint visual system
- ❌ No FootprintStyle enum
- ❌ No footprint particle class
- ❌ No footprint texture

---

## API Differences

### 1. Particle System

| Aspect | 1.12.2 | 1.20.1 |
|--------|--------|--------|
| Base Class | `MoteAgeable` (custom) | `TextureSheetParticle` (vanilla) |
| Rendering | `BufferBuilder` + manual vertex | `ParticleRenderType` system |
| Position | `posX, posY, posZ` | `x, y, z` |
| Previous Position | `prevPosX, prevPosY, prevPosZ` | `xo, yo, zo` |
| Motion | `motionX, motionY, motionZ` | `xd, yd, zd` |
| Age | `age, maxAge` | `age, lifetime` |
| Alpha | `alpha` (0-255) | `alpha` (0.0-1.0) |
| Color | `red, green, blue` (0-255) | `rCol, gCol, bCol` (0.0-1.0) |
| Size | `scale` | `quadSize` |

### 2. Rendering

| Aspect | 1.12.2 | 1.20.1 |
|--------|--------|--------|
| Vertex Drawing | Manual `drawVertex()` | Automatic via `ParticleRenderType` |
| Depth Mask | `GlStateManager.depthMask(false)` | Handled by render type |
| Blending | `OpenGlUtil.setStandardBlend()` | Handled by render type |
| Texture Binding | Manual | Automatic via particle engine |
| Rotation | Pre-calculated vertex points | Use `roll` field or quaternion |

### 3. World/Level

| Aspect | 1.12.2 | 1.20.1 |
|--------|--------|--------|
| Class | `World` | `ClientLevel` |
| Block State | `WorldUtils.getBlockState()` | `level.getBlockState()` |
| Material Check | `getMaterial().isSolid()` | `getBlock().defaultBlockState()` |

### 4. Entity

| Aspect | 1.12.2 | 1.20.1 |
|--------|--------|--------|
| Position | `entity.posX/Y/Z` | `entity.getX()/getY()/getZ()` |
| Previous Position | `entity.prevPosX/Y/Z` | `entity.xo/yo/zo` |
| Motion | `entity.motionX/Y/Z` | `entity.getDeltaMovement()` |
| Rotation | `entity.rotationYaw` | `entity.getYRot()` |
| On Ground | `entity.onGround` | `entity.onGround()` |

### 5. Math Utilities

| Aspect | 1.12.2 | 1.20.1 |
|--------|--------|--------|
| Vector | `Vec3d` | `Vec3` |
| 2D Vector | `Vec2f` | `Vector2f` |
| Rotation | `MathStuff.rotateScale()` | Manual calculation or `Quaternionf` |
| Radians | `MathStuff.toRadians()` | `Math.toRadians()` |
| Wrap Degrees | `MathStuff.wrapDegrees()` | `Mth.wrapDegrees()` |

### 6. Random

| Aspect | 1.12.2 | 1.20.1 |
|--------|--------|--------|
| Class | `XorShiftRandom` | `Randomizer` (wrapper) |
| Usage | `RANDOM.nextInt()` | `Randomizer.current().nextInt()` |

---

## Migration Strategy

### Phase 1: Core Data Structures ✅
**Files to Create:**
1. `FootprintStyle.java` - Enum (direct port)
2. `Footprint.java` - Data class (minimal changes)

**Changes Required:**
- Update imports (`Vec3d` → `Vec3`)
- Update entity references

### Phase 2: Particle Implementation 🔄
**Files to Create:**
1. `FootprintParticle.java` - Main particle class

**Key Adaptations:**
```java
public class FootprintParticle extends TextureSheetParticle {
    // Convert from MoteFootprint
    
    // 1.12.2: Manual vertex rendering
    @Override
    public void renderParticle(BufferBuilder buffer, ...) {
        drawVertex(buffer, x + firstPoint.x, y, z + firstPoint.y, texU1, texV2);
        // ... 4 vertices
    }
    
    // 1.20.1: Automatic rendering via render type
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
    
    // Need custom render type for ground-aligned quad
}
```

**Challenges:**
- **Rotation:** 1.12.2 pre-calculates rotated vertices. 1.20.1 needs different approach:
  - Option A: Use `roll` field (limited to Z-axis rotation)
  - Option B: Custom `ParticleRenderType` with manual vertex control
  - Option C: Pre-calculate vertices like 1.12.2 (requires custom render type)
  
- **Ground Alignment:** Footprints render flat on ground (Y-plane), not billboarded
  - Need custom `ParticleRenderType` that doesn't billboard
  - Or use decal-style rendering

### Phase 3: Texture System 🔄
**Files to Copy:**
1. `footprint.png` - Copy from 1.12.2 to 1.20.1

**Location:**
- Source: `H:/MinecraftMods/DynamicSurroundings/src/main/resources/assets/dsurround/textures/particles/footprint.png`
- Target: `H:/MinecraftMods/DynamicSurroundingsFroge-1.20.1/src/main/resources/assets/dsurround/textures/particles/footprint.png`

### Phase 4: Integration with Footstep System 🔄
**Files to Modify:**
1. `FootstepProcessor.java` - Add footprint generation
2. `FootstepContext.java` - Add footprint data

**Integration Points:**
```java
// In FootstepProcessor or similar
private void generateFootprint(FootstepContext context) {
    if (!shouldProducePrint(context.entity())) return;
    
    // Calculate foot position
    Vec3 footPos = calculateFootPosition(context);
    
    // Create footprint data
    Footprint print = Footprint.produce(
        style, entity, footPos, rotation, scale, isRightFoot
    );
    
    // Spawn particle
    spawnFootprintParticle(print);
}
```

### Phase 5: Configuration 🔄
**Files to Modify:**
1. `Configuration.java` - Add footprint settings

**Settings to Add:**
```java
@Property
@Comment("Enable/disable footprint effects")
public boolean enableFootprints = true;

@Property
@IntRange(min = 0, max = 6)
@Comment("Footprint style for player (0=SHOE, 1=SQUARE, 2=HORSESHOE, 3=BIRD, 4=PAW, 5=SQUARE_SOLID, 6=LOWRES_SQUARE)")
public int footprintStyle = 0;
```

---

## Technical Challenges

### Challenge 1: Ground-Aligned Rendering
**Problem:** Footprints must render flat on ground, not billboarded toward camera.

**1.12.2 Solution:**
- Manual vertex positioning in world space
- Pre-calculated rotation during construction

**1.20.1 Solutions:**
- **Option A:** Custom `ParticleRenderType` with manual vertex control
  - Pros: Full control, exact 1.12.2 behavior
  - Cons: More complex, bypasses particle engine optimizations
  
- **Option B:** Use decal rendering system
  - Pros: Proper ground alignment
  - Cons: May not exist in 1.20.1, would need to implement

**Recommended:** Option A - Custom ParticleRenderType

### Challenge 2: Texture UV Mapping
**Problem:** 8 styles in one texture, need correct UV coordinates.

**Solution:**
- Store UV coordinates in FootprintParticle
- Pass to custom render type
- Similar to 1.12.2 approach

### Challenge 3: Z-Fighting Prevention
**Problem:** Multiple overlapping footprints cause flickering.

**1.12.2 Solution:**
```java
private static float zFighter = 0F;
if (++zFighter > 20) zFighter = 1;
this.posY += zFighter * 0.001F;
```

**1.20.1 Solution:**
- Same approach, adjust Y position slightly
- Or use polygon offset in render type

### Challenge 4: Weather-Based Aging
**Problem:** Footprints should fade faster in rain.

**1.12.2 Solution:**
```java
if (Weather.isRaining())
    this.age += (Weather.getIntensityLevel() * 100F) / 25;
```

**1.20.1 Solution:**
- Check weather in `tick()` method
- Adjust age increment based on weather

### Challenge 5: Snow Layer Detection
**Problem:** Footprints on snow should disappear when snow melts.

**1.12.2 Solution:**
```java
if (this.isSnowLayer && 
    ClientChunkCache.instance().getBlockState(this.position).getBlock() != Blocks.SNOW_LAYER) {
    kill();
}
```

**1.20.1 Solution:**
- Check block state in `tick()` method
- Call `remove()` if snow is gone

---

## Implementation Plan

### Step 1: Create Core Classes
1. ✅ Copy `FootprintStyle.java` (minimal changes)
2. ✅ Copy `Footprint.java` (update Vec3d → Vec3)
3. ✅ Copy footprint texture

### Step 2: Create Custom Particle Render Type
```java
public class FootprintRenderType implements ParticleRenderType {
    // Custom rendering for ground-aligned quads
    // Manual vertex positioning
    // Proper UV mapping
}
```

### Step 3: Create FootprintParticle
```java
public class FootprintParticle extends TextureSheetParticle {
    // Port from MoteFootprint
    // Use custom render type
    // Implement all features:
    //   - Rotation
    //   - Z-fighting prevention
    //   - Weather aging
    //   - Snow detection
    //   - Alpha fading
}
```

### Step 4: Integrate with Footstep System
- Add footprint generation to FootstepProcessor
- Hook into existing footstep events
- Reuse footstep detection logic

### Step 5: Add Configuration
- Add settings to Configuration.java
- Wire up to footprint system

### Step 6: Testing
- Test all 7 footprint styles
- Test on different surfaces (snow, sand, dirt)
- Test weather aging
- Test snow layer detection
- Test Z-fighting prevention
- Test performance with many footprints

---

## File Checklist

### New Files to Create
- [ ] `src/main/java/org/orecruncher/dsurround/footsteps/FootprintStyle.java`
- [ ] `src/main/java/org/orecruncher/dsurround/footsteps/Footprint.java`
- [ ] `src/main/java/org/orecruncher/dsurround/effects/particles/FootprintParticle.java`
- [ ] `src/main/java/org/orecruncher/dsurround/effects/particles/FootprintRenderType.java`

### Files to Modify
- [ ] `src/main/java/org/orecruncher/dsurround/footsteps/FootstepProcessor.java`
- [ ] `src/main/java/org/orecruncher/dsurround/Configuration.java`

### Resources to Copy
- [ ] `src/main/resources/assets/dsurround/textures/particles/footprint.png`

---

## Risk Assessment

| Risk | Severity | Mitigation |
|------|----------|------------|
| Custom render type complexity | Medium | Reference existing custom particles, use 1.12.2 as guide |
| Performance with many footprints | Low | Particle engine handles culling, limit max footprints |
| Texture not rendering | Low | Test early, verify texture path and UV coords |
| Z-fighting still occurs | Low | Adjust offset value if needed |
| Integration with footstep system | Medium | Reuse existing event system, minimal changes |

---

## Success Criteria

1. ✅ Footprints render on appropriate surfaces
2. ✅ All 7 styles work correctly
3. ✅ Footprints fade over time
4. ✅ Footprints age faster in rain
5. ✅ Footprints disappear when snow melts
6. ✅ No Z-fighting between overlapping prints
7. ✅ Footprints align with entity rotation
8. ✅ Left/right foot distinction works
9. ✅ Configuration options work
10. ✅ Performance is acceptable (no lag with many prints)

---

## Estimated Effort

- **Core Classes:** 1 hour
- **Custom Render Type:** 2-3 hours
- **FootprintParticle:** 2-3 hours
- **Integration:** 1-2 hours
- **Configuration:** 0.5 hours
- **Testing & Debugging:** 2-3 hours

**Total:** 8-12 hours

---

## Notes

- The footprint system is relatively self-contained
- Main complexity is in the custom rendering
- Integration with existing footstep system should be straightforward
- 1.20.1's particle system is more structured than 1.12.2's custom mote system
- May need to reference Minecraft's particle rendering code for custom render type
