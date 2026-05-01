# Phase 8: Client Handlers System Migration

## Overview
Migrated the client-side effect handler management system from Fabric 1.12.2 to Forge 1.20.1. This system provides centralized coordination of all client-side effects, environmental state tracking, and lifecycle management.

## Implementation Date
2026-05-02

## Files Created

### Core Handler System
1. **EffectHandlerBase.java** (~105 lines)
   - Base class for all effect handlers
   - Provides lifecycle hooks: `onConnect()`, `onDisconnect()`, `process()`
   - Tick filtering via `doTick(int tick)`
   - Helper methods for accessing player and world

2. **EffectManager.java** (~200 lines)
   - Central manager for all effect handlers
   - Singleton pattern with static access
   - Handler registration and lookup service
   - Tick processing coordination
   - Connection/disconnection lifecycle management
   - Error handling for individual handlers

3. **EnvironStateHandler.java** (~280 lines)
   - Environmental state tracking and caching
   - Runs first each tick to gather state for other handlers
   - Provides static API via `EnvironState` class
   - Tracks:
     - Player position, temperature, equipment
     - Dimension info and name
     - Light levels (block + sky)
     - Inside/outside/underground/space/clouds detection
     - Player state queries (hurt, hungry, burning, etc.)
     - Village detection
     - Tick counter

## Files Modified

### Client.java
- Added `EffectManager.connect()` call in `onConnect()`
- Added `EffectManager.disconnect()` call in `onDisconnect()`
- Imported `EffectManager` class

## Architecture

### Handler Lifecycle
```
Game Start
    ↓
Join World → EffectManager.connect()
    ↓
Initialize all handlers → handler.onConnect()
    ↓
Every Tick → handler.process(player) [if handler.doTick(tick) returns true]
    ↓
Leave World → EffectManager.disconnect()
    ↓
Cleanup all handlers → handler.onDisconnect()
```

### Handler Registration Order
1. **EnvironStateHandler** - Must be first, provides state for others
2. Other handlers (to be added in future phases):
   - AreaBlockEffectsHandler
   - FogHandler
   - ParticleSystemHandler
   - BiomeSoundEffectsHandler
   - AuroraEffectHandler
   - WeatherHandler
   - FxHandler
   - SoundEffectHandler
   - DiagnosticHandler

### EnvironState API
Other systems can query environmental state via static methods:
```java
// Position and dimension
BlockPos pos = EnvironState.getPlayerPosition();
IDimensionInfo dimInfo = EnvironState.getDimensionInfo();
String dimName = EnvironState.getDimensionName();

// Temperature
TemperatureRating playerTemp = EnvironState.getPlayerTemperature();
TemperatureRating biomeTemp = EnvironState.getBiomeTemperature();

// Location detection
boolean inside = EnvironState.isPlayerInside();
boolean underground = EnvironState.isPlayerUnderground();
boolean inSpace = EnvironState.isPlayerInSpace();

// Player state
boolean hurt = EnvironState.isPlayerHurt();
boolean hungry = EnvironState.isPlayerHungry();
boolean burning = EnvironState.isPlayerBurning();
boolean moving = EnvironState.isPlayerMoving();

// Equipment
ItemStack armor = EnvironState.getPlayerItemStack();
ItemStack boots = EnvironState.getPlayerFootArmorStack();

// Timing
int tick = EnvironState.getTickCounter();
float partialTick = EnvironState.getPartialTick();
```

## Key Changes (1.12.2 → 1.20.1)

### Event System
- **Old**: `@SubscribeEvent` with `TickEvent.ClientTickEvent`
- **New**: `@Mod.EventBusSubscriber` with Forge event bus
- **Change**: Uses `TickEvent.Phase.START` instead of checking phase manually

### Player Access
- **Old**: `Minecraft.getMinecraft().player`
- **New**: `Minecraft.getInstance().player`

### World Access
- **Old**: `player.getEntityWorld()` returns `World`
- **New**: `player.level()` returns `Level`

### Position
- **Old**: `new BlockPos(player.posX, player.getEntityBoundingBox().minY, player.posZ)`
- **New**: `player.blockPosition()`

### Light Levels
- **Old**: `world.getLightFor(EnumSkyBlock.BLOCK, pos)`
- **New**: `world.getBrightness(LightLayer.BLOCK, pos)`

### Armor Access
- **Old**: Custom `ItemClass.effectiveArmorStack(player)`
- **New**: `player.getInventory().getArmor(slot)` (0=boots, 1=legs, 2=chest, 3=head)

### Capabilities
- **Old**: `CapabilitySeasonInfo.getCapability(world)`
- **New**: `CapabilityHandler.getSeasonInfo(world)`
- **Change**: Centralized capability access through `CapabilityHandler`

### Dimension Info
- **Old**: `world.provider.getDimension()` / `world.provider.getDimensionType().getName()`
- **New**: `world.dimension().location().toString()`

## Integration Points

### With Capabilities System (Phase 7)
- Uses `CapabilityHandler.getDimensionInfo(world)` for dimension data
- Uses `CapabilityHandler.getSeasonInfo(world)` for temperature data

### With Future Handlers
- Provides centralized state via `EnvironState` API
- Coordinates tick processing for all handlers
- Manages lifecycle (connect/disconnect) for all handlers

## Code Statistics
- **New Code**: ~585 lines
- **New Classes**: 3
- **Modified Files**: 1 (Client.java)
- **Static API Methods**: 20+

## Testing Notes
- EffectManager connects when joining a world
- EffectManager disconnects when leaving a world
- EnvironStateHandler runs first each tick
- State is properly reset on connect/disconnect
- Error handling prevents one handler from breaking others

## Future Work
- Add remaining effect handlers (fog, particles, sounds, etc.)
- Implement diagnostic handler for debug overlay
- Add performance timing for each handler
- Integrate with existing block effects and entity effects systems

## Known Issues
- Inside/outside detection is simplified (needs ceiling coverage scanner)
- Underground/space/clouds detection needs biome registry integration
- Village detection not yet implemented (placeholder)

## Related Phases
- **Phase 7**: Capabilities system (provides dimension and season info)
- **Phase 2**: Block effects (will be integrated as a handler)
- **Phase 3**: Entity effects (will be integrated as a handler)
- **Phase 6**: Aurora system (will be integrated as a handler)
