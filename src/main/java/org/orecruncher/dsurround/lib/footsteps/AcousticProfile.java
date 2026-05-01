package org.orecruncher.dsurround.lib.footsteps;

import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Defines sound properties for a material type (e.g., stone, wood, grass).
 * Contains different sound sets for various movement types.
 */
public class AcousticProfile {
    private static final Random RANDOM = new Random();

    private final String name;
    private final List<SoundEvent> walkSounds;
    private final List<SoundEvent> runSounds;
    private final List<SoundEvent> jumpSounds;
    private final List<SoundEvent> landSounds;
    private final float volumeScale;
    private final float pitchVariation;

    public AcousticProfile(@NotNull String name,
                          @NotNull List<SoundEvent> walkSounds,
                          @NotNull List<SoundEvent> runSounds,
                          @NotNull List<SoundEvent> jumpSounds,
                          @NotNull List<SoundEvent> landSounds,
                          float volumeScale,
                          float pitchVariation) {
        this.name = name;
        this.walkSounds = new ArrayList<>(walkSounds);
        this.runSounds = new ArrayList<>(runSounds);
        this.jumpSounds = new ArrayList<>(jumpSounds);
        this.landSounds = new ArrayList<>(landSounds);
        this.volumeScale = volumeScale;
        this.pitchVariation = pitchVariation;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    /**
     * Get a random walk sound from this profile
     */
    @NotNull
    public SoundEvent getWalkSound() {
        return getRandomSound(this.walkSounds);
    }

    /**
     * Get a random run sound from this profile
     */
    @NotNull
    public SoundEvent getRunSound() {
        return getRandomSound(this.runSounds);
    }

    /**
     * Get a random jump sound from this profile
     */
    @NotNull
    public SoundEvent getJumpSound() {
        return getRandomSound(this.jumpSounds);
    }

    /**
     * Get a random land sound from this profile
     */
    @NotNull
    public SoundEvent getLandSound() {
        return getRandomSound(this.landSounds);
    }

    /**
     * Get the volume scale for this acoustic profile
     */
    public float getVolumeScale() {
        return this.volumeScale;
    }

    /**
     * Get a randomized pitch value based on the pitch variation
     */
    public float getRandomPitch() {
        return 1.0f + (RANDOM.nextFloat() - 0.5f) * 2.0f * this.pitchVariation;
    }

    private SoundEvent getRandomSound(@NotNull List<SoundEvent> sounds) {
        if (sounds.isEmpty()) {
            throw new IllegalStateException("No sounds available for acoustic profile: " + this.name);
        }
        return sounds.get(RANDOM.nextInt(sounds.size()));
    }

    public static class Builder {
        private String name;
        private final List<SoundEvent> walkSounds = new ArrayList<>();
        private final List<SoundEvent> runSounds = new ArrayList<>();
        private final List<SoundEvent> jumpSounds = new ArrayList<>();
        private final List<SoundEvent> landSounds = new ArrayList<>();
        private float volumeScale = 1.0f;
        private float pitchVariation = 0.1f;

        public Builder name(@NotNull String name) {
            this.name = name;
            return this;
        }

        public Builder addWalkSound(@NotNull SoundEvent sound) {
            this.walkSounds.add(sound);
            return this;
        }

        public Builder addRunSound(@NotNull SoundEvent sound) {
            this.runSounds.add(sound);
            return this;
        }

        public Builder addJumpSound(@NotNull SoundEvent sound) {
            this.jumpSounds.add(sound);
            return this;
        }

        public Builder addLandSound(@NotNull SoundEvent sound) {
            this.landSounds.add(sound);
            return this;
        }

        public Builder volumeScale(float scale) {
            this.volumeScale = scale;
            return this;
        }

        public Builder pitchVariation(float variation) {
            this.pitchVariation = variation;
            return this;
        }

        @NotNull
        public AcousticProfile build() {
            if (this.name == null) {
                throw new IllegalStateException("Acoustic profile name must be set");
            }
            // Use walk sounds as fallback for other types if not specified
            List<SoundEvent> run = this.runSounds.isEmpty() ? this.walkSounds : this.runSounds;
            List<SoundEvent> jump = this.jumpSounds.isEmpty() ? this.walkSounds : this.jumpSounds;
            List<SoundEvent> land = this.landSounds.isEmpty() ? this.walkSounds : this.landSounds;

            return new AcousticProfile(this.name, this.walkSounds, run, jump, land,
                                      this.volumeScale, this.pitchVariation);
        }
    }
}
