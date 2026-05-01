package org.orecruncher.dsurround.effects.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Base class for jet-style particle effects (steam, fire, bubbles, etc.).
 * Manages jet strength, spawn frequency, and particle spawning logic.
 */
public abstract class ParticleJet extends ParticleSystem {
    protected static final Random RANDOM = new Random();

    protected final int jetStrength; // 1-10, intensity of the jet
    protected final int updateFrequency; // ticks between particle spawns
    protected int updateCounter;

    protected ParticleJet(@NotNull Level level, @NotNull BlockPos pos, int jetStrength, int updateFrequency, int maxAge) {
        super(level, pos, maxAge);
        this.jetStrength = Math.max(1, Math.min(10, jetStrength));
        this.updateFrequency = Math.max(1, updateFrequency);
        this.updateCounter = 0;
    }

    /**
     * Get the jet strength (1-10)
     */
    public int getJetStrength() {
        return this.jetStrength;
    }

    @Override
    protected void think() {
        this.updateCounter++;

        // Only spawn particles at the specified frequency
        if (this.updateCounter >= this.updateFrequency) {
            this.updateCounter = 0;
            spawnJetParticles();
        }
    }

    /**
     * Spawn particles for this jet.
     * Called at the update frequency.
     */
    protected abstract void spawnJetParticles();

    /**
     * Get a random offset for particle spawning.
     * Returns a value between -0.5 and 0.5.
     */
    protected double getRandomOffset() {
        return (RANDOM.nextDouble() - 0.5);
    }

    /**
     * Get a random offset scaled by jet strength.
     * Stronger jets have more spread.
     */
    protected double getScaledRandomOffset() {
        return getRandomOffset() * (this.jetStrength / 10.0);
    }
}
