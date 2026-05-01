package org.orecruncher.dsurround.effects.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Abstract base class for all block effects.
 * Defines the interface for triggering and executing effects.
 */
public abstract class BlockEffect {
    protected static final Random RANDOM = new Random();

    protected final BlockEffectType type;
    protected final int chance; // 0-100, percentage chance to trigger

    protected BlockEffect(@NotNull BlockEffectType type, int chance) {
        this.type = type;
        this.chance = Math.max(0, Math.min(100, chance));
    }

    /**
     * Get the effect type
     */
    @NotNull
    public BlockEffectType getType() {
        return this.type;
    }

    /**
     * Get the trigger chance (0-100)
     */
    public int getChance() {
        return this.chance;
    }

    /**
     * Check if this effect can trigger at the given position.
     * This includes random chance checking and condition validation.
     *
     * @param level The level
     * @param pos The block position
     * @param state The block state
     * @return true if the effect should trigger
     */
    public boolean canTrigger(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        // Check random chance
        if (this.chance < 100 && RANDOM.nextInt(100) >= this.chance) {
            return false;
        }

        // Check specific conditions
        return canTriggerAt(level, pos, state);
    }

    /**
     * Check if this effect can trigger at the given position.
     * Override this to add specific conditions for the effect.
     *
     * @param level The level
     * @param pos The block position
     * @param state The block state
     * @return true if conditions are met
     */
    protected abstract boolean canTriggerAt(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state);

    /**
     * Execute the effect at the given position.
     * This is called when canTrigger() returns true.
     *
     * @param level The level
     * @param pos The block position
     * @param state The block state
     */
    public abstract void doEffect(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state);

    /**
     * Get the update frequency for this effect (in ticks).
     * Lower values mean more frequent updates.
     * Default is 1 (every tick).
     *
     * @return ticks between updates
     */
    public int getUpdateFrequency() {
        return 1;
    }
}
