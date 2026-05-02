#!/bin/bash
# Script to check and copy missing resource files from 1.12.2 to 1.20.1

SOURCE_DIR="H:/MinecraftMods/DynamicSurroundings/src/main/resources"
TARGET_DIR="H:/MinecraftMods/DynamicSurroundingsFroge-1.20.1/src/main/resources"

echo "=== Dynamic Surroundings Resource File Migration ==="
echo ""

# Check footstep sounds
echo "Checking footstep sounds..."
SOURCE_FOOTSTEPS="$SOURCE_DIR/assets/dsurround/sounds/footsteps"
TARGET_FOOTSTEPS="$TARGET_DIR/assets/dsurround/sounds/footsteps"

if [ -d "$SOURCE_FOOTSTEPS" ]; then
    SOURCE_COUNT=$(find "$SOURCE_FOOTSTEPS" -name "*.ogg" | wc -l)
    TARGET_COUNT=$(find "$TARGET_FOOTSTEPS" -name "*.ogg" 2>/dev/null | wc -l)

    echo "  Source (1.12.2): $SOURCE_COUNT .ogg files"
    echo "  Target (1.20.1): $TARGET_COUNT .ogg files"
    echo "  Missing: $((SOURCE_COUNT - TARGET_COUNT)) files"
    echo ""
fi

# Check ambient sounds
echo "Checking ambient sounds..."
SOURCE_AMBIENT="$SOURCE_DIR/assets/dsurround/sounds/ambient"
TARGET_AMBIENT="$TARGET_DIR/assets/dsurround/sounds/ambient"

if [ -d "$SOURCE_AMBIENT" ]; then
    SOURCE_COUNT=$(find "$SOURCE_AMBIENT" -name "*.ogg" | wc -l)
    TARGET_COUNT=$(find "$TARGET_AMBIENT" -name "*.ogg" 2>/dev/null | wc -l)

    echo "  Source (1.12.2): $SOURCE_COUNT .ogg files"
    echo "  Target (1.20.1): $TARGET_COUNT .ogg files"
    echo "  Missing: $((SOURCE_COUNT - TARGET_COUNT)) files"
    echo ""
fi

# Check textures
echo "Checking textures..."
SOURCE_TEXTURES="$SOURCE_DIR/assets/dsurround/textures"
TARGET_TEXTURES="$TARGET_DIR/assets/dsurround/textures"

if [ -d "$SOURCE_TEXTURES" ]; then
    SOURCE_COUNT=$(find "$SOURCE_TEXTURES" -name "*.png" | wc -l)
    TARGET_COUNT=$(find "$TARGET_TEXTURES" -name "*.png" 2>/dev/null | wc -l)

    echo "  Source (1.12.2): $SOURCE_COUNT .png files"
    echo "  Target (1.20.1): $TARGET_COUNT .png files"
    echo "  Missing: $((SOURCE_COUNT - TARGET_COUNT)) files"
    echo ""
fi

# Check sounds.json
echo "Checking sounds.json..."
if [ -f "$SOURCE_DIR/assets/dsurround/sounds.json" ]; then
    if [ -f "$TARGET_DIR/assets/dsurround/sounds.json" ]; then
        echo "  sounds.json exists in both versions"
    else
        echo "  WARNING: sounds.json missing in 1.20.1"
    fi
else
    echo "  No sounds.json in 1.12.2"
fi
echo ""

echo "=== Summary ==="
echo "To copy missing footstep sounds, run:"
echo "  cp -r \"$SOURCE_FOOTSTEPS\"/* \"$TARGET_FOOTSTEPS/\""
echo ""
echo "To copy missing ambient sounds, run:"
echo "  cp -r \"$SOURCE_AMBIENT\"/* \"$TARGET_AMBIENT/\""
echo ""
echo "Note: Review the copied files to ensure they are compatible with 1.20.1"
