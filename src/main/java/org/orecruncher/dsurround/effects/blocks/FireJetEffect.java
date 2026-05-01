package org.orecruncher.dsurround.effects.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.orecruncher.dsurround.Client;

/**
 * Fire jet effect that spawns flame particles from hot blocks like lava.
 * Creates rising flame particles above lava sources.
 */
public class FireJetEffect extends BlockEffect {

    public FireJetEffect() {
        super(BlockEffectType.FIRE_JET, 30); // 30% chance to trigger
    }

    @Override
    protected boolean canTriggerAt(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        // Check if fire jets are enabled
        if (!Client.Config.blockEffects.flameJetEnabled) {
            return false;
        }

        // Must be lava or magma
        boolean isHotBlock = state.is(Blocks.LAVA) ||
                            state.is(Blocks.MAGMA_BLOCK) ||
                            state.getFluidState().is(Fluids.LAVA);

        if (!isHotBlock) {
            return false;
        }

        // Check if there's air above for flames to rise
        BlockPos above = pos.above();
        return level.getBlockState(above).isAir();
    }

    @Override
    public void doEffect(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        // Create or get existing fire jet particle system
        if (!ParticleSystemManager.getInstance().hasSystem(pos)) {
            int strength = state.is(Blocks.LAVA) ? 7 : 3; // Lava is stronger than magma
            FireJetParticleSystem system = new FireJetParticleSystem(level, pos, strength, 1, 100);
            ParticleSystemManager.getInstance().addSystem(system);
        }
    }

    /**
     * Particle system for fire jets
     */
    private static class FireJetParticleSystem extends ParticleJet {
        private boolean playedSound = false;

        public FireJetParticleSystem(@NotNull Level level, @NotNull BlockPos pos, int jetStrength, int updateFrequency, int maxAge) {
            super(level, pos, jetStrength, updateFrequency, maxAge);
        }

        @Override
        protected boolean shouldDie() {
            if (super.shouldDie()) {
                return true;
            }

            // Check if source block is still hot
            BlockState state = this.level.getBlockState(this.pos);
            boolean isHotBlock = state.is(Blocks.LAVA) ||
                                state.is(Blocks.MAGMA_BLOCK) ||
                                state.getFluidState().is(Fluids.LAVA);

            if (!isHotBlock) {
                return true;
            }

            // Check if air still above
            BlockPos above = this.pos.above();
            return !this.level.getBlockState(above).isAir();
        }

        @Override
        protected void spawnJetParticles() {
            // Spawn flame particles
            for (int i = 0; i < this.jetStrength; i++) {
                double x = this.pos.getX() + 0.5 + getScaledRandomOffset();
                double y = this.pos.getY() + 1.0;
                double z = this.pos.getZ() + 0.5 + getScaledRandomOffset();

                // Upward velocity with some randomness
                double vx = getRandomOffset() * 0.02;
                double vy = 0.05 + RANDOM.nextDouble() * 0.05;
                double vz = getRandomOffset() * 0.02;

                // Use flame or lava particles based on strength
                if (this.jetStrength > 5) {
                    this.level.addParticle(ParticleTypes.LAVA, x, y, z, vx, vy, vz);
                } else {
                    this.level.addParticle(ParticleTypes.FLAME, x, y, z, vx, vy, vz);
                }
            }
        }

        @Override
        protected void soundUpdate() {
            // Play fire sound once when created
            if (!this.playedSound) {
                this.playedSound = true;
                this.level.playLocalSound(
                    this.pos.getX() + 0.5,
                    this.pos.getY() + 0.5,
                    this.pos.getZ() + 0.5,
                    SoundEvents.FIRE_AMBIENT,
                    SoundSource.BLOCKS,
                    0.5f + RANDOM.nextFloat() * 0.2f,
                    0.8f + RANDOM.nextFloat() * 0.4f,
                    false
                );
            }
        }
    }
}
