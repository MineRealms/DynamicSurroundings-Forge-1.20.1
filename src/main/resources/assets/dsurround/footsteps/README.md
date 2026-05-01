# Footstep Acoustics Resource Pack Guide

This guide explains how to create custom footstep sounds for Dynamic Surroundings using resource packs.

## Overview

Dynamic Surroundings allows you to customize footstep sounds through resource packs. You can:
- Define new acoustic profiles with custom sounds
- Map blocks to different acoustic profiles
- Override default sounds and volumes

## File Structure

Create a resource pack with the following structure:

```
my_footsteps_pack/
├── pack.mcmeta
└── assets/
    └── dsurround/
        └── footsteps/
            ├── acoustics.json
            └── blocks.json
```

## acoustics.json

This file defines acoustic profiles - sets of sounds for different movement types.

### Format

```json
{
  "acoustics": {
    "profile_name": {
      "walk": ["sound:event:id"],
      "run": ["sound:event:id"],
      "jump": ["sound:event:id"],
      "land": ["sound:event:id"],
      "volume": 1.0,
      "pitchVariation": 0.1
    }
  }
}
```

### Fields

- **walk**: Array of sound events for walking (required)
- **run**: Array of sound events for running (optional, defaults to walk)
- **jump**: Array of sound events for jumping (optional, defaults to walk)
- **land**: Array of sound events for landing (optional, defaults to walk)
- **volume**: Volume multiplier (0.0 to 2.0, default 1.0)
- **pitchVariation**: Random pitch variation (0.0 to 1.0, default 0.1)

### Example

```json
{
  "acoustics": {
    "custom_stone": {
      "walk": [
        "minecraft:block.stone.step",
        "mymod:footstep.stone.walk1",
        "mymod:footstep.stone.walk2"
      ],
      "run": [
        "mymod:footstep.stone.run1",
        "mymod:footstep.stone.run2"
      ],
      "jump": ["mymod:footstep.stone.jump"],
      "land": ["mymod:footstep.stone.land"],
      "volume": 1.2,
      "pitchVariation": 0.15
    },
    "mud": {
      "walk": [
        "mymod:footstep.mud.squelch1",
        "mymod:footstep.mud.squelch2",
        "mymod:footstep.mud.squelch3"
      ],
      "volume": 0.8,
      "pitchVariation": 0.2
    }
  }
}
```

## blocks.json

This file maps blocks to acoustic profiles.

### Format

```json
{
  "blocks": {
    "minecraft:block_id": "profile_name",
    "modid:block_id": "profile_name"
  }
}
```

### Example

```json
{
  "blocks": {
    "minecraft:stone": "custom_stone",
    "minecraft:cobblestone": "custom_stone",
    "minecraft:mud": "mud",
    "mymod:custom_block": "wood"
  }
}
```

## Sound Variety

You can specify multiple sounds for each movement type. The mod will randomly select one each time:

```json
{
  "acoustics": {
    "varied_wood": {
      "walk": [
        "minecraft:block.wood.step",
        "mymod:wood.creak1",
        "mymod:wood.creak2",
        "mymod:wood.creak3"
      ],
      "volume": 1.0,
      "pitchVariation": 0.1
    }
  }
}
```

## Volume and Pitch

- **volume**: Controls how loud the footsteps are
  - 0.0 = silent
  - 1.0 = normal volume
  - 2.0 = double volume
  
- **pitchVariation**: Adds randomness to pitch
  - 0.0 = no variation (all sounds same pitch)
  - 0.1 = slight variation (recommended for most sounds)
  - 0.5 = high variation (sounds very different each time)

## Using Vanilla Sounds

You can use any Minecraft sound event. Common ones for footsteps:

```
minecraft:block.stone.step
minecraft:block.stone.fall
minecraft:block.wood.step
minecraft:block.wood.fall
minecraft:block.grass.step
minecraft:block.grass.fall
minecraft:block.gravel.step
minecraft:block.gravel.fall
minecraft:block.sand.step
minecraft:block.sand.fall
minecraft:block.snow.step
minecraft:block.snow.fall
minecraft:block.metal.step
minecraft:block.metal.fall
minecraft:block.wool.step
minecraft:block.wool.fall
```

## Adding Custom Sounds

To add your own sounds:

1. Create sound files (OGG format recommended)
2. Place them in your resource pack:
   ```
   assets/mymod/sounds/footsteps/
   ├── stone_walk1.ogg
   ├── stone_walk2.ogg
   └── stone_land.ogg
   ```

3. Register them in `sounds.json`:
   ```json
   {
     "footstep.stone.walk1": {
       "sounds": ["mymod:footsteps/stone_walk1"]
     },
     "footstep.stone.walk2": {
       "sounds": ["mymod:footsteps/stone_walk2"]
     },
     "footstep.stone.land": {
       "sounds": ["mymod:footsteps/stone_land"]
     }
   }
   ```

4. Reference them in `acoustics.json`:
   ```json
   {
     "acoustics": {
       "my_stone": {
         "walk": [
           "mymod:footstep.stone.walk1",
           "mymod:footstep.stone.walk2"
         ],
         "land": ["mymod:footstep.stone.land"]
       }
     }
   }
   ```

## Overriding Defaults

Resource packs are loaded in order. Later packs override earlier ones:

1. Mod's default acoustics (built-in)
2. Resource pack 1
3. Resource pack 2 (overrides pack 1)
4. etc.

To override a default profile, just use the same profile name:

```json
{
  "acoustics": {
    "stone": {
      "walk": ["mymod:better_stone_sound"],
      "volume": 1.5
    }
  }
}
```

## Tips

1. **Test your sounds**: Load the game and walk on different blocks to hear your changes
2. **Use variety**: Multiple sound variants make footsteps less repetitive
3. **Match volume**: Keep volumes consistent across similar materials
4. **Pitch variation**: Use 0.1-0.15 for most sounds, higher for softer materials
5. **Performance**: Don't use extremely long sound files for footsteps

## Troubleshooting

**Sounds not playing:**
- Check that sound event IDs are correct
- Verify JSON syntax (use a JSON validator)
- Check logs for error messages

**Wrong sounds playing:**
- Verify block IDs in blocks.json
- Check that acoustic profile names match

**Sounds too loud/quiet:**
- Adjust the `volume` field in acoustics.json
- Also check in-game config: Footsteps → Volume Scale

## Example Resource Pack

Here's a complete minimal example:

**pack.mcmeta:**
```json
{
  "pack": {
    "pack_format": 15,
    "description": "Custom Footsteps"
  }
}
```

**assets/dsurround/footsteps/acoustics.json:**
```json
{
  "acoustics": {
    "stone": {
      "walk": ["minecraft:block.stone.step"],
      "land": ["minecraft:block.stone.fall"],
      "volume": 1.2,
      "pitchVariation": 0.1
    }
  }
}
```

**assets/dsurround/footsteps/blocks.json:**
```json
{
  "blocks": {
    "minecraft:stone": "stone",
    "minecraft:cobblestone": "stone"
  }
}
```

## Support

For more help:
- Check the mod's GitHub issues
- Join the Discord server
- Read the wiki

Happy customizing!
