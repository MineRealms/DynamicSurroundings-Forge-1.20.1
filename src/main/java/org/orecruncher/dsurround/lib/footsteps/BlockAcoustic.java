package org.orecruncher.dsurround.lib.footsteps;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps blocks to acoustic profiles.
 * Provides lookup functionality to determine which sounds to play for a given block.
 */
public class BlockAcoustic {
    private final Map<Block, AcousticProfile> blockMap;
    private final Map<String, AcousticProfile> tagMap;
    private final AcousticProfile defaultProfile;

    public BlockAcoustic(@NotNull AcousticProfile defaultProfile) {
        this.blockMap = new HashMap<>();
        this.tagMap = new HashMap<>();
        this.defaultProfile = defaultProfile;
    }

    /**
     * Register an acoustic profile for a specific block
     */
    public void registerBlock(@NotNull Block block, @NotNull AcousticProfile profile) {
        this.blockMap.put(block, profile);
    }

    /**
     * Register an acoustic profile for a block tag
     */
    public void registerTag(@NotNull String tag, @NotNull AcousticProfile profile) {
        this.tagMap.put(tag, profile);
    }

    /**
     * Get the acoustic profile for a block state.
     * Returns the default profile if no specific mapping exists.
     */
    @NotNull
    public AcousticProfile getAcoustic(@NotNull BlockState state) {
        // First check direct block mapping
        AcousticProfile profile = this.blockMap.get(state.getBlock());
        if (profile != null) {
            return profile;
        }

        // TODO: Check block tags when tag system is implemented
        // For now, return default
        return this.defaultProfile;
    }

    /**
     * Get the acoustic profile for a specific block
     */
    @Nullable
    public AcousticProfile getAcoustic(@NotNull Block block) {
        return this.blockMap.get(block);
    }

    /**
     * Check if a block has a registered acoustic profile
     */
    public boolean hasAcoustic(@NotNull Block block) {
        return this.blockMap.containsKey(block);
    }

    /**
     * Get the default acoustic profile
     */
    @NotNull
    public AcousticProfile getDefaultProfile() {
        return this.defaultProfile;
    }

    /**
     * Clear all registered acoustics
     */
    public void clear() {
        this.blockMap.clear();
        this.tagMap.clear();
    }

    /**
     * Get the number of registered block acoustics
     */
    public int size() {
        return this.blockMap.size();
    }
}
