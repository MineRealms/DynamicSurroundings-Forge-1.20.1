package org.orecruncher.dsurround.effects.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.orecruncher.dsurround.Client;

/**
 * Waterfall effect that spawns splash particles and sounds where water flows down.
 * Detects flowing water with air below and calculates waterfall strength.
 */
public class WaterfallEffect extends BlockEffect {

    public WaterfallEffect() {
        super(BlockEffectType.WATERFALL, 40); // 40% chance to trigger
    }

    @Override
    protected boolean canTriggerAt(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        // Check if waterfalls are enabled
        if (!Client.Config.blockEffects.waterfallsEnabled) {
            return false;
        }

        // Must be flowing water
        if (!state.getFluidState().is(Fluids.FLOWING_WATER)) {
            return false;
        }

        // Check if there's air below (water is falling)
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);

        return belowState.isAir() || belowState.getFluidState().is(Fluids.WATER);
    }

    @Override
    public void doEffect(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        // Calculate waterfall strength (how many water blocks above)
        int strength = calculateWaterfallStrength(level, pos);

        if (strength < 1) {
            return;
        }

        // Create or get existing waterfall particle system
        if (!ParticleSystemManager.getInstance().hasSystem(pos)) {
            WaterfallParticleSystem system = new WaterfallParticleSystem(level, pos, strength, 1, 200);
            ParticleSystemManager.getInstance().addSystem(system);
        }
    }

    /**
     * Calculate waterfall strength by counting water blocks above
     */
    private int calculateWaterfallStrength(@NotNull Level level, @NotNull BlockPos pos) {
        int strength = 0;
        BlockPos.MutableBlockPos checkPos = pos.mutable();

        // Check up to 10 blocks above
        for (int i = 0; i < 10; i++) {
            checkPos.move(0, 1, 0);
            BlockState state = level.getBlockState(checkPos);

            if (state.getFluidState().is(Fluids.WATER)) {
                strength++;
            } else {
                break;
            }
        }

        return strength;
    }

    /**
     * Particle system for waterfalls
     */
    private static class WaterfallParticleSystem extends ParticleJet {
        private int soundCounter = 0;
        private static final int SOUND_INTERVAL = 40; // Play sound every 2 seconds

        public WaterfallParticleSystem(@NotNull Level level, @NotNull BlockPos pos, int jetStrength, int updateFrequency, int maxAge) {
            super(level, pos, jetStrength, updateFrequency, maxAge);
        }

        @Override
        protected boolean shouldDie() {
            if (super.shouldDie()) {
                return true;
            }

            // Check if water still flowing
            BlockState state = this.level.getBlockState(this.pos);
            if (!state.getFluidState().is(Fluids.FLOWING_WATER)) {
                return true;
            }

            // Check if air still below
            BlockPos below = this.pos.below();
            BlockState belowState = this.level.getBlockState(below);

            return !belowState.isAir() && !belowState.getFluidState().is(Fluids.WATER);
        }

        @Override
        protected void spawnJetParticles() {
            // Only spawn particles if enabled
            if (!Client.Config.blockEffects.enableWaterfallParticles) {
                return;
            }

            // Spawn splash particles
            int particleCount = Math.min(this.jetStrength, 5);
            for (int i = 0; i < particleCount; i++) {
                double x = this.pos.getX() + 0.5 + getScaledRandomOffset();
                double y = this.pos.getY() + 0.2;
                double z = this.pos.getZ() + 0.5 + getScaledRandomOffset();

                // Downward velocity
                double vx = getRandomOffset() * 0.1;
                double vy = -0.1 - RANDOM.nextDouble() * 0.1;
                double vz = getRandomOffset() * 0.1;

                this.level.addParticle(ParticleTypes.SPLASH, x, y, z, vx, vy, vz);

                // Add some falling water particles
                if (RANDOM.nextBoolean()) {
                    this.level.addParticle(ParticleTypes.FALLING_WATER, x, y, z, vx, vy, vz);
                }
            }
        }

        @Override
        protected void soundUpdate() {
            // Only play sounds if enabled
            if (!Client.Config.blockEffects.enableWaterfallSounds) {
                return;
            }

            this.soundCounter++;

            // Play sound periodically based on strength
            if (this.soundCounter >= SOUND_INTERVAL) {
                this.soundCounter = 0;

                // Volume based on waterfall strength
                float volume = 0.3f + (this.jetStrength * 0.1f);
                volume = Math.min(volume, 1.0f);

                // Pitch variation
                float pitch = 0.8f + RANDOM.nextFloat() * 0.4f;

                this.level.playLocalSound(
                    this.pos.getX() + 0.5,
                    this.pos.getY() + 0.5,
                    this.pos.getZ() + 0.5,
                    SoundEvents.WEATHER_RAIN,
                    SoundSource.BLOCKS,
                    volume,
                    pitch,
                    false
                );
            }
        }
    }
}
