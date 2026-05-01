# Phase 1: Footstep System Implementation Plan

## Overview
Implement the footstep sound system from Dynamic Surroundings 1.12.2, which provides different footstep sounds based on block types, player movement state, and armor worn.

## Implementation Date
Start: 2026-05-02

## Goals
1. Add footstep sounds for walking, running, jumping, and landing
2. Support different sounds for different block types (stone, wood, grass, gravel, etc.)
3. Add armor sound effects (jingling when moving with armor)
4. Make the system configurable
5. Support custom acoustics via resource packs

## Architecture Analysis (1.12.2 Reference)

### Core Components
1. **FootstepSoundEffect** - Main sound effect handler
2. **Footsteps** - Manager class coordinating footstep generation
3. **Generator** - Generates footstep sounds based on player state
4. **AcousticsManager** - Manages acoustic properties for blocks
5. **BlockMap** - Maps blocks to acoustic profiles
6. **PrimitiveMap** - Defines primitive sound types

### Key Mechanics
- Hooks into player movement via ASM/Mixin
- Calculates footstep timing based on movement speed
- Determines block type under player feet
- Selects appropriate sound from acoustic library
- Applies volume/pitch variations
- Handles special cases (sneaking, jumping, landing)

## Implementation Plan for 1.20.1 Forge

### Step 1: Set Up Mixin Infrastructure
**Files to create:**
- `build.gradle` - Add Mixin dependencies
- `src/main/resources/dynamicsurround.mixins.json` - Mixin configuration
- `src/main/java/org/orecruncher/dsurround/mixins/` - Mixin package

**Tasks:**
1. Add Mixin dependency to build.gradle
2. Configure Mixin annotation processor
3. Create mixin configuration file
4. Test basic mixin functionality

**Estimated Time:** 2-3 hours

### Step 2: Create Footstep Data Structures
**Files to create:**
- `src/main/java/org/orecruncher/dsurround/lib/footsteps/AcousticProfile.java`
- `src/main/java/org/orecruncher/dsurround/lib/footsteps/BlockAcoustic.java`
- `src/main/java/org/orecruncher/dsurround/lib/footsteps/FootstepTiming.java`

**Classes:**
```java
// AcousticProfile - Defines sound properties for a material type
public class AcousticProfile {
    private final String name;
    private final List<SoundEvent> walkSounds;
    private final List<SoundEvent> runSounds;
    private final List<SoundEvent> jumpSounds;
    private final List<SoundEvent> landSounds;
    private final float volumeScale;
    private final float pitchVariation;
}

// BlockAcoustic - Maps blocks to acoustic profiles
public class BlockAcoustic {
    private final Map<Block, AcousticProfile> blockMap;
    private final AcousticProfile defaultProfile;
}

// FootstepTiming - Calculates when footsteps should play
public class FootstepTiming {
    private float distanceWalked;
    private long lastStepTime;
    
    public boolean shouldPlayFootstep(float movementSpeed);
}
```

**Estimated Time:** 3-4 hours

### Step 3: Implement Acoustic Manager
**Files to create:**
- `src/main/java/org/orecruncher/dsurround/lib/footsteps/AcousticsManager.java`
- `src/main/java/org/orecruncher/dsurround/lib/footsteps/AcousticsLibrary.java`

**Responsibilities:**
1. Load acoustic profiles from JSON files
2. Register default acoustics for vanilla blocks
3. Provide API for mods to register custom acoustics
4. Handle resource pack overrides

**Data Format (JSON):**
```json
{
  "acoustics": {
    "stone": {
      "walk": ["dsurround:footsteps.stone.walk1", "dsurround:footsteps.stone.walk2"],
      "run": ["dsurround:footsteps.stone.run1", "dsurround:footsteps.stone.run2"],
      "jump": ["dsurround:footsteps.stone.jump"],
      "land": ["dsurround:footsteps.stone.land"],
      "volume": 1.0,
      "pitchVariation": 0.1
    }
  },
  "blocks": {
    "minecraft:stone": "stone",
    "minecraft:cobblestone": "stone",
    "minecraft:oak_planks": "wood"
  }
}
```

**Estimated Time:** 4-5 hours

### Step 4: Create Footstep Generator
**Files to create:**
- `src/main/java/org/orecruncher/dsurround/lib/footsteps/FootstepGenerator.java`
- `src/main/java/org/orecruncher/dsurround/lib/footsteps/FootstepContext.java`

**Responsibilities:**
1. Determine player movement state (walking, running, sneaking, jumping)
2. Calculate footstep timing based on movement speed
3. Detect block type under player feet
4. Select appropriate sound from acoustic profile
5. Apply volume and pitch variations
6. Play the sound at player position

**Key Methods:**
```java
public class FootstepGenerator {
    public void onPlayerMove(Player player, Vec3 movement);
    private BlockPos getFootstepPosition(Player player);
    private AcousticProfile getAcousticForBlock(BlockState state);
    private void playFootstepSound(Player player, AcousticProfile acoustic, MovementType type);
}
```

**Estimated Time:** 5-6 hours

### Step 5: Implement Player Movement Mixin
**Files to create:**
- `src/main/java/org/orecruncher/dsurround/mixins/MixinLocalPlayer.java`

**Mixin Target:** `net.minecraft.client.player.LocalPlayer`

**Injection Points:**
1. `move()` method - Detect movement and trigger footsteps
2. `jumpFromGround()` method - Play jump sound
3. After landing detection - Play land sound

**Example Mixin:**
```java
@Mixin(LocalPlayer.class)
public class MixinLocalPlayer {
    @Inject(method = "move", at = @At("TAIL"))
    private void onMove(MoverType type, Vec3 movement, CallbackInfo ci) {
        FootstepGenerator.getInstance().onPlayerMove((Player)(Object)this, movement);
    }
    
    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void onJump(CallbackInfo ci) {
        FootstepGenerator.getInstance().onPlayerJump((Player)(Object)this);
    }
}
```

**Estimated Time:** 3-4 hours

### Step 6: Add Armor Sound Effects
**Files to create:**
- `src/main/java/org/orecruncher/dsurround/lib/footsteps/ArmorSoundHandler.java`

**Features:**
1. Detect armor worn by player
2. Play jingling sounds when moving with armor
3. Different sounds for different armor materials (leather, chain, iron, diamond)
4. Volume scales with amount of armor worn

**Estimated Time:** 2-3 hours

### Step 7: Register Footstep Sounds
**Files to create:**
- `src/main/resources/assets/dsurround/sounds.json` - Sound definitions
- `src/main/resources/assets/dsurround/sounds/footsteps/` - Sound files directory
- `src/main/java/org/orecruncher/dsurround/sound/FootstepSounds.java` - Sound event registration

**Sound Categories Needed:**
- Stone (stone, cobblestone, andesite, etc.)
- Wood (planks, logs)
- Grass (grass block, dirt)
- Gravel (gravel, sand)
- Snow (snow, ice)
- Metal (iron blocks, gold blocks)
- Cloth (wool, carpet)
- Water (shallow water)
- Mud (mud, clay)

**Estimated Time:** 4-5 hours (including sound file sourcing/creation)

### Step 8: Add Configuration Options
**Files to modify:**
- `src/main/java/org/orecruncher/dsurround/config/Config.java`

**Configuration Options:**
```java
public static class Footsteps {
    public boolean enabled = true;
    public float volumeScale = 1.0f;
    public boolean armorSounds = true;
    public float armorVolumeScale = 0.5f;
    public boolean firstPersonFootsteps = true;
    public int soundVariety = 3; // Number of sound variants per type
}
```

**Estimated Time:** 1-2 hours

### Step 9: Create Resource Pack Support
**Files to create:**
- `src/main/resources/assets/dsurround/footsteps/acoustics.json` - Default acoustics
- `src/main/resources/assets/dsurround/footsteps/blocks.json` - Default block mappings
- Documentation for custom acoustic creation

**Estimated Time:** 2-3 hours

### Step 10: Testing and Refinement
**Test Cases:**
1. Walking on different block types produces correct sounds
2. Running produces faster footsteps
3. Sneaking produces quieter footsteps
4. Jumping and landing produce appropriate sounds
5. Armor sounds play correctly
6. Configuration options work as expected
7. Resource pack overrides work
8. Performance is acceptable (no lag)

**Estimated Time:** 4-6 hours

## Total Estimated Time
- Setup: 2-3 hours
- Data structures: 3-4 hours
- Acoustic manager: 4-5 hours
- Footstep generator: 5-6 hours
- Mixin implementation: 3-4 hours
- Armor sounds: 2-3 hours
- Sound registration: 4-5 hours
- Configuration: 1-2 hours
- Resource pack support: 2-3 hours
- Testing: 4-6 hours

**Total: 30-41 hours (4-5 days)**

## Dependencies
1. **Mixin** - Required for player movement hooks
2. **Sound files** - Need to source or create footstep sound effects
3. **Forge Sound API** - Already available in 1.20.1

## Risks and Mitigations
1. **Risk:** Mixin conflicts with other mods
   - **Mitigation:** Use minimal, targeted mixins; test with popular mods

2. **Risk:** Performance impact from frequent sound calculations
   - **Mitigation:** Optimize timing calculations; cache acoustic lookups

3. **Risk:** Sound files licensing/availability
   - **Mitigation:** Use CC0/public domain sounds; document sources

4. **Risk:** API changes between 1.12.2 and 1.20.1
   - **Mitigation:** Reference 1.20.1 documentation; test thoroughly

## Success Criteria
- [ ] Footsteps play correctly for all movement types
- [ ] Different blocks produce different sounds
- [ ] Armor sounds work properly
- [ ] Configuration options function correctly
- [ ] Resource pack support works
- [ ] No performance degradation
- [ ] No conflicts with other mods (tested with common mods)

## Next Steps After Completion
1. Gather user feedback
2. Add more acoustic profiles for modded blocks
3. Move to Phase 2: Block Effects implementation

## References
- 1.12.2 Source: `H:\MinecraftMods\DynamicSurroundings\src\main\java\org\orecruncher\dsurround\client\footsteps\`
- Mixin Documentation: https://github.com/SpongePowered/Mixin/wiki
- Forge Sound API: https://docs.minecraftforge.net/en/1.20.x/gameeffects/sounds/
