# Dynamic Surroundings Feature Comparison: 1.12.2 vs 1.20.1

## Executive Summary
This document compares the feature sets between Dynamic Surroundings 1.12.2 (reference implementation) and the current 1.20.1 Forge port to identify missing features that need to be ported.

## Analysis Date
2026-05-02

## Feature Comparison Matrix

### ✅ Already Implemented in 1.20.1
- Basic sound engine
- Biome sound effects
- Configuration system (Forge Config)
- Commands (/ds calc, /ds config, /ds reload, /ds status, /ds version)
- Client-side mod architecture

### ❌ Missing Features from 1.12.2

#### 1. **Footstep System** (HIGH PRIORITY)
**1.12.2 Implementation:**
- Location: `org.orecruncher.dsurround.client.footsteps`
- Files: ~20 classes including:
  - `FootstepSoundEffect.java` - Core sound effect handler
  - `Footsteps.java` - Main footstep manager
  - `Generator.java` - Sound generation logic
  - `implem/AcousticsManager.java` - Acoustic properties
  - `implem/BlockMap.java` - Block-to-sound mapping
  - `implem/PrimitiveMap.java` - Primitive sound definitions
- Features:
  - Different sounds for different block types (stone, wood, grass, etc.)
  - Walking, running, jumping sounds
  - Armor sound effects
  - Configurable acoustic properties
  - Support for custom block acoustics via resource packs

**Migration Complexity:** HIGH
**Estimated Effort:** 3-5 days
**Dependencies:** Requires Mixin for player movement hooks

#### 2. **Aurora Borealis Effects**
**1.12.2 Implementation:**
- Location: `org.orecruncher.dsurround.client.aurora`
- Files: `Aurora.java`, `AuroraFactory.java`, `AuroraUtils.java`
- Features:
  - Dynamic aurora rendering in cold biomes
  - Configurable colors and intensity
  - Time-based appearance (night only)
  - Biome-specific aurora types

**Migration Complexity:** MEDIUM
**Estimated Effort:** 2-3 days
**Dependencies:** Rendering system, shader support

#### 3. **Particle Effects System**
**1.12.2 Implementation:**
- Location: `org.orecruncher.dsurround.client.fx`
- Files: ~15 particle effect classes
- Features:
  - Firefly particles
  - Bubble particles (underwater)
  - Dust particles
  - Fountain effects
  - Steam effects
  - Waterfall splash effects
  - Breath particles in cold weather

**Migration Complexity:** MEDIUM
**Estimated Effort:** 2-3 days
**Dependencies:** Particle system API changes between versions

#### 4. **Weather Effects Enhancements**
**1.12.2 Implementation:**
- Location: `org.orecruncher.dsurround.client.weather`
- Files: `Weather.java`, `StormSplashRenderer.java`, etc.
- Features:
  - Enhanced rain effects
  - Storm intensity variations
  - Dust storms in desert biomes
  - Snow particle improvements
  - Thunder sound enhancements

**Migration Complexity:** MEDIUM
**Estimated Effort:** 2-3 days

#### 5. **Fog Effects**
**1.12.2 Implementation:**
- Location: `org.orecruncher.dsurround.client.fog`
- Files: `FogEffectHandler.java`, `HolisticFogColorCalculator.java`
- Features:
  - Biome-specific fog
  - Morning/evening fog
  - Desert haze
  - Elevation-based fog
  - Weather-dependent fog density

**Migration Complexity:** MEDIUM-HIGH
**Estimated Effort:** 2-4 days
**Dependencies:** Rendering pipeline changes

#### 6. **HUD Enhancements**
**1.12.2 Implementation:**
- Location: `org.orecruncher.dsurround.client.hud`
- Files: `CompassHUD.java`, `LightLevelHUD.java`, `PotionHUD.java`
- Features:
  - Compass overlay
  - Light level display
  - Potion effect display improvements
  - Configurable HUD elements

**Migration Complexity:** LOW-MEDIUM
**Estimated Effort:** 1-2 days

#### 7. **Speech Bubbles**
**1.12.2 Implementation:**
- Location: `org.orecruncher.dsurround.client.speech`
- Files: `SpeechBubbleHandler.java`, `EntityChatBubble.java`
- Features:
  - Chat bubbles above player heads
  - Entity speech visualization
  - Configurable bubble appearance

**Migration Complexity:** LOW-MEDIUM
**Estimated Effort:** 1-2 days

#### 8. **Block Effects**
**1.12.2 Implementation:**
- Location: `org.orecruncher.dsurround.client.handlers.effects`
- Files: ~20 effect classes
- Features:
  - Torch flicker effects
  - Fire jet effects
  - Waterfall effects
  - Lava drip effects
  - Redstone dust particles
  - Brewing stand bubbles

**Migration Complexity:** MEDIUM
**Estimated Effort:** 2-3 days

#### 9. **Entity Effects**
**1.12.2 Implementation:**
- Location: `org.orecruncher.dsurround.client.handlers`
- Files: `EntityEffectHandler.java`
- Features:
  - Entity-specific sound effects
  - Bow sound improvements
  - Tool swing sounds
  - Item equip sounds

**Migration Complexity:** MEDIUM
**Estimated Effort:** 2-3 days

#### 10. **Expression System**
**1.12.2 Implementation:**
- Location: `org.orecruncher.dsurround.expression`
- Files: Expression parser and evaluator
- Features:
  - Dynamic configuration based on game state
  - Conditional sound/effect triggers
  - Mathematical expressions for effect parameters

**Migration Complexity:** HIGH
**Estimated Effort:** 3-4 days

#### 11. **Presets System**
**1.12.2 Implementation:**
- Location: Config presets
- Features:
  - Pre-configured setting bundles
  - Easy switching between configurations
  - Performance presets (low/medium/high)

**Migration Complexity:** LOW
**Estimated Effort:** 1 day

#### 12. **ASM/Mixin Hooks**
**1.12.2 Implementation:**
- Location: `org.orecruncher.dsurround.asm`
- Features:
  - Deep integration with Minecraft internals
  - Performance optimizations
  - Event injection points

**Migration Complexity:** HIGH (ASM → Mixin conversion)
**Estimated Effort:** Ongoing throughout migration

## Priority Ranking

### Phase 1: Core Gameplay Features (HIGH PRIORITY)
1. **Footstep System** - Most noticeable missing feature
2. **Block Effects** - Enhances world immersion
3. **Entity Effects** - Improves gameplay feedback

### Phase 2: Visual Enhancements (MEDIUM PRIORITY)
4. **Particle Effects System**
5. **Weather Effects Enhancements**
6. **Aurora Borealis Effects**

### Phase 3: Advanced Features (MEDIUM PRIORITY)
7. **Fog Effects**
8. **HUD Enhancements**
9. **Speech Bubbles**

### Phase 4: System Features (LOWER PRIORITY)
10. **Expression System**
11. **Presets System**

## Technical Considerations

### Mixin Usage
The 1.20.1 port should use Mixin for:
- Player movement hooks (footsteps)
- Rendering hooks (particles, fog, aurora)
- Sound system hooks
- Entity event hooks

### API Changes Between Versions
- Sound system: `SoundEvent` registration changes
- Particle system: Complete rewrite in 1.13+
- Rendering: RenderSystem and shader changes
- Config: Forge Config API changes (already handled)
- Events: Event bus changes (already handled)

### Resource Pack Compatibility
- Maintain compatibility with 1.12.2 resource pack structure where possible
- Document any breaking changes
- Provide migration guide for resource pack creators

## Estimated Total Effort
- **High Priority Features:** 7-11 days
- **Medium Priority Features:** 6-10 days
- **Lower Priority Features:** 4-5 days
- **Total:** 17-26 days (3-5 weeks)

## Next Steps
1. Set up Mixin infrastructure
2. Implement Phase 1 features (footsteps, block effects, entity effects)
3. Test and iterate
4. Move to Phase 2 and beyond

## Notes
- All estimates assume familiarity with both 1.12.2 and 1.20.1 APIs
- Testing time not included in estimates
- Resource pack creation/migration not included
- Documentation time not included
