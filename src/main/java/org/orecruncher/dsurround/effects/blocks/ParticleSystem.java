package org.orecruncher.dsurround.effects.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for particle systems that have a lifecycle.
 * Manages position, alive state, and update logic.
 */
public abstract class ParticleSystem {
    protected final Level level;
    protected final BlockPos pos;
    protected boolean isAlive;
    protected int age;
    protected int maxAge;

    protected ParticleSystem(@NotNull Level level, @NotNull BlockPos pos, int maxAge) {
        this.level = level;
        this.pos = pos.immutable();
        this.isAlive = true;
        this.age = 0;
        this.maxAge = maxAge;
    }

    /**
     * Get the position of this particle system
     */
    @NotNull
    public BlockPos getPos() {
        return this.pos;
    }

    /**
     * Check if this particle system is still alive
     */
    public boolean isAlive() {
        return this.isAlive;
    }

    /**
     * Mark this particle system as dead
     */
    public void setDead() {
        this.isAlive = false;
    }

    /**
     * Get the current age of this system
     */
    public int getAge() {
        return this.age;
    }

    /**
     * Update this particle system.
     * Called every tick while the system is alive.
     */
    public void onUpdate() {
        if (!this.isAlive) {
            return;
        }

        this.age++;

        // Check if system should die
        if (shouldDie()) {
            setDead();
            return;
        }

        // Update logic
        think();

        // Update sounds
        soundUpdate();
    }

    /**
     * Check if this particle system should die.
     * Override to add custom death conditions.
     *
     * @return true if the system should be removed
     */
    protected boolean shouldDie() {
        // Die if max age reached
        if (this.maxAge > 0 && this.age >= this.maxAge) {
            return true;
        }

        // Die if level is not loaded
        if (!this.level.isLoaded(this.pos)) {
            return true;
        }

        return false;
    }

    /**
     * Main update logic for spawning particles.
     * Called every tick while alive.
     */
    protected abstract void think();

    /**
     * Update sound effects.
     * Called every tick while alive.
     * Override to add sound effects.
     */
    protected void soundUpdate() {
        // Default: no sound
    }
}
