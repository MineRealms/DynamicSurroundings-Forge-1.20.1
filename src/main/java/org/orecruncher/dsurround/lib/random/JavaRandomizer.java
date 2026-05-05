package org.orecruncher.dsurround.lib.random;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.jetbrains.annotations.NotNull;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/**
 * Implementation of IRandomizer that wraps a Java RandomGenerator
 */
@SuppressWarnings("unused")
final class JavaRandomizer implements IRandomizer {

    // Fallback chain for random number generators
    // Try modern algorithms first, fall back to universally available ones
    private static final String[] PREFERRED_ALGORITHMS = {
            "L64X128MixRandom",      // Fast, good quality (Java 17+)
            "Xoroshiro128PlusPlus",  // Fast, good quality (may not be available)
            "L32X64MixRandom",       // Smaller state, still good
            "Random"                 // Always available fallback
    };

    public static final String DEFAULT_ALGORITHM = "L64X128MixRandom";
    public static final String XOROSHIRO_128_PLUS_PLUS = "Xoroshiro128PlusPlus";

    private final RandomGenerator generator;
    private final String actualAlgorithm;

    /**
     * Creates a JavaRandomizer with the specified algorithm.
     * If the algorithm is not available, tries fallback algorithms.
     */
    public JavaRandomizer(String algorithm) {
        RandomGenerator gen = null;
        String usedAlgorithm = null;

        // Try the requested algorithm first
        try {
            gen = RandomGeneratorFactory.of(algorithm).create();
            usedAlgorithm = algorithm;
        } catch (IllegalArgumentException e) {
            // Algorithm not available, try fallbacks
            for (String fallback : PREFERRED_ALGORITHMS) {
                try {
                    gen = RandomGeneratorFactory.of(fallback).create();
                    usedAlgorithm = fallback;
                    break;
                } catch (IllegalArgumentException ignored) {
                    // Try next fallback
                }
            }
        }

        if (gen == null) {
            throw new IllegalStateException("No random number generator algorithm available");
        }

        this.generator = gen;
        this.actualAlgorithm = usedAlgorithm;
    }

    public JavaRandomizer(String algorithm, final long seed) {
        RandomGenerator gen = null;
        String usedAlgorithm = null;

        // Try the requested algorithm first
        try {
            gen = RandomGeneratorFactory.of(algorithm).create(seed);
            usedAlgorithm = algorithm;
        } catch (IllegalArgumentException e) {
            // Algorithm not available, try fallbacks
            for (String fallback : PREFERRED_ALGORITHMS) {
                try {
                    gen = RandomGeneratorFactory.of(fallback).create(seed);
                    usedAlgorithm = fallback;
                    break;
                } catch (IllegalArgumentException ignored) {
                    // Try next fallback
                }
            }
        }

        if (gen == null) {
            throw new IllegalStateException("No random number generator algorithm available");
        }

        this.generator = gen;
        this.actualAlgorithm = usedAlgorithm;
    }

    public String getActualAlgorithm() {
        return this.actualAlgorithm;
    }

    @Override
    public @NotNull RandomSource fork() {
        return new JavaRandomizer(DEFAULT_ALGORITHM, this.nextLong());
    }

    @Override
    public @NotNull PositionalRandomFactory forkPositional() {
        return new LegacyRandomSource.LegacyPositionalRandomFactory(this.nextLong());
    }

    @Override
    public void setSeed(long l) {
        // Pray I do not alter the deal any further...
    }

    @Override
    public int nextInt() {
        return this.generator.nextInt();
    }

    @Override
    public int nextInt(int bound) {
        return this.generator.nextInt(bound);
    }

    @Override
    public boolean nextBoolean() {
        return this.generator.nextBoolean();
    }

    @Override
    public double nextDouble() {
        return this.generator.nextDouble();
    }

    @Override
    public float nextFloat() {
        return this.generator.nextFloat();
    }

    @Override
    public double nextGaussian() {
        return this.generator.nextGaussian();
    }

    @Override
    public long nextLong() {
        return this.generator.nextLong();
    }
}
