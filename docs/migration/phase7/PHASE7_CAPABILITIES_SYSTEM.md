# Phase 7: Capabilities System Migration

## Overview
Migrated the Forge 1.12.2 Capabilities system to Forge 1.20.1's modern capability architecture. The capabilities system provides attachable data to entities and levels for tracking dynamic states and properties.

## Implementation Date
2024-01-XX

## Files Created

### Dimension Capabilities (3 files)
1. **IDimensionInfo.java** - Interface for dimension properties
   - Dimension ID, sea level, sky height, cloud height
   - Boolean flags: hasHaze, hasAuroras, hasWeather, hasFog
   - NBT serialization support

2. **IDimensionInfoEx.java** - Extended interface with mutable weather state
   - Rain intensity (target and current)
   - Min/max rain intensity bounds
   - Thunder timer
   - Rain randomization

3. **DimensionInfo.java** - Implementation (~260 lines)
   - Reads dimension properties from Level
   - Tracks dynamic weather state
   - NBT serialization for persistence
   - Configuration string generation

### Season Capabilities (6 files)
1. **ISeasonInfo.java** - Interface for season and temperature queries
   - Season type and sub-type
   - Temperature ratings
   - Precipitation type determination
   - Frost breath and water freeze checks

2. **SeasonInfo.java** - Default implementation (~150 lines)
   - Vanilla biome temperature queries
   - Precipitation height calculation
   - Factory method for dimension-specific instances

3. **SeasonInfoNether.java** - Nether-specific implementation
   - Fixed high temperature (2.0F)
   - No precipitation
   - No frost effects

4. **SeasonType.java** - Enum for seasons (SPRING, SUMMER, AUTUMN, WINTER)
   - SubType enum (EARLY, MID, LATE)

5. **TemperatureRating.java** - Enum for temperature categories
   - ICY, COLD, COOL, MILD, WARM, HOT
   - Conversion from float temperature values

6. **PrecipitationType.java** - Enum for precipitation (NONE, RAIN, SNOW, DUST)

### Entity Capabilities (5 files)
1. **IEntityData.java** - Interface for entity behavioral data
   - Entity ID
   - Attacking state
   - Fleeing state
   - NBT serialization

2. **IEntityDataSettable.java** - Extended interface for server-side updates
   - Setters for attacking/fleeing states
   - Sync method for network updates

3. **EntityData.java** - Implementation (~110 lines)
   - Tracks attacking and fleeing states
   - Dirty flag for network synchronization
   - NBT serialization

4. **EntityDataTables.java** - AI goal evaluation utility (~140 lines)
   - Maps AI goal classes to behavior types (ATTACK, FLEE)
   - Evaluates active goals to determine entity state
   - Supports vanilla goals: MeleeAttackGoal, RangedAttackGoal, PanicGoal, etc.

5. **IEntityFX.java** - Interface for entity effect handler storage
   - Client-side only
   - Holds reference to entity's effect handler

6. **EntityFXData.java** - Simple implementation
   - Stores effect handler object reference

### Core System (2 files)
1. **CapabilityHandler.java** - Central capability registry (~230 lines)
   - Defines all capability instances using CapabilityToken
   - Attaches capabilities to Level and Entity via events
   - Provides capability providers with NBT serialization
   - Helper methods to retrieve capabilities

2. **EntityCapabilityEvents.java** - Entity update handler
   - Listens to LivingTickEvent
   - Evaluates entity AI states every 5 ticks (server-side)
   - Syncs state changes to clients

## Key Changes from 1.12.2

### Capability Registration
- **Old (1.12.2)**: `CapabilityManager.INSTANCE.register()` with `@CapabilityInject`
- **New (1.20.1)**: `CapabilityManager.get(new CapabilityToken<>() {})`

### Capability Attachment
- **Old**: `AttachCapabilitiesEvent` with custom providers
- **New**: Same event, but providers use `LazyOptional<T>` for capability access

### Capability Access
- **Old**: `entity.getCapability(CAP, null)`
- **New**: `entity.getCapability(CAP).orElse(null)`

### NBT Serialization
- **Old**: `NBTTagCompound`
- **New**: `CompoundTag`

### Entity AI System
- **Old**: `EntityAITasks` with reflection to access `executingTaskEntries`
- **New**: `GoalSelector.getRunningGoals()` public API

## Technical Details

### Dimension Info
- Attached to all Level instances (client and server)
- Stores both static properties (heights, flags) and dynamic weather state
- Weather intensity ranges from 0.0 to 1.0
- Thunder timer counts down in ticks

### Season Info
- Client-side only capability
- Provides temperature and precipitation queries
- Factory pattern supports dimension-specific implementations
- Nether has special handling (no precipitation, high temp)

### Entity Data
- Attached to Mob entities (both client and server)
- Server evaluates AI goals every 5 ticks
- Dirty flag triggers network sync when state changes
- Client receives updates via network packets (TODO: implement packets)

### Entity FX
- Client-side only capability
- Attached to all LivingEntity instances
- Stores reference to entity's effect handler
- No serialization needed (transient client state)

## Integration Points

### Used By
- Weather system (reads DimensionInfo for rain intensity)
- Aurora system (checks DimensionInfo.hasAuroras())
- Entity effects (reads EntityData for attacking/fleeing states)
- Particle systems (uses SeasonInfo for precipitation type)

### Dependencies
- `lib.math.MathStuff` - Clamping utilities (added float/int/double overloads)
- `lib.logging.ModLog` - Logging
- Forge event bus for capability attachment

## Statistics
- **Total Files**: 16 Java files
- **Total Lines**: ~1,400 lines
- **Interfaces**: 5
- **Implementations**: 9
- **Enums**: 3
- **Event Handlers**: 2

## Testing Notes
- Capabilities attach correctly to Level and Entity instances
- NBT serialization works for DimensionInfo and EntityData
- AI goal evaluation successfully identifies attacking/fleeing states
- Temperature queries return correct values based on biome data

## Future Work
1. Implement network packets for EntityData synchronization
2. Add configuration system integration for dimension overrides
3. Add support for Serene Seasons mod (SeasonInfoSereneSeasons)
4. Implement player tracking events for initial entity data sync
5. Add more AI goal types for modded entities

## Migration Notes
- Removed reflection-based AI task access (1.12.2 approach)
- Used modern GoalSelector API for cleaner goal evaluation
- Simplified capability providers using LazyOptional
- All capabilities use modern Forge 1.20.1 patterns
