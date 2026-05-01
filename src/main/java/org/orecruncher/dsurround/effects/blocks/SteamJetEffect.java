package org.orecruncher.dsurround.effects.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.orecruncher.dsurround.Client;

/**
 * Steam jet effect that spawns steam particles where hot blocks meet water.
 * Checks for lava or magma blocks adjacent to water.
 */
public class SteamJetEffect extends BlockEffect {

    public SteamJetEffect() {
        super(BlockEffectType.STEAM_JET, 50); // 50% chance to trigger
    }

    @Override
    protected boolean canTriggerAt(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        // Check if steam jets are enabled
        if (!Client.Config.blockEffects.steamColumnEnabled) {
            return false;
        }

        // Must be water
        if (!state.getFluidState().is(Fluids.WATER)) {
            return false;
        }

        // Check for hot blocks nearby (lava or magma)
        return hasHotBlockNearby(level, pos);
    }

    @Override
    public void doEffect(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        // Create or get existing steam jet particle system
        if (!ParticleSystemManager.getInstance().hasSystem(pos)) {
            SteamJetParticleSystem system = new SteamJetParticleSystem(level, pos, 5, 2, 200);
            ParticleSystemManager.getInstance().addSystem(system);
        }
    }

    /**
     * Check if there's a hot block (lava or magma) adjacent to this position
     */
    private boolean hasHotBlockNearby(@NotNull Level level, @NotNull BlockPos pos) {
        // Check all 6 adjacent positions
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    // Skip center and diagonal positions
                    if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1) {
                        continue;
                    }

                    BlockPos checkPos = pos.offset(dx, dy, dz);
                    BlockState checkState = level.getBlockState(checkPos);

                    // Check for lava or magma
                    if (checkState.is(Blocks.LAVA) ||
                        checkState.is(Blocks.MAGMA_BLOCK) ||
                        checkState.getFluidState().is(Fluids.LAVA)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Particle system for steam jets
     */
    private static class SteamJetParticleSystem extends ParticleJet {

        public SteamJetParticleSystem(@NotNull Level level, @NotNull BlockPos pos, int jetStrength, int updateFrequency, int maxAge) {
            super(level, pos, jetStrength, updateFrequency, maxAge);
        }

        @Override
        protected boolean shouldDie() {
            // Die if parent conditions no longer met
            if (super.shouldDie()) {
                return true;
            }

            // Check if water still present
            BlockState state = this.level.getBlockState(this.pos);
            if (!state.getFluidState().is(Fluids.WATER)) {
                return true;
            }

            // Check if hot block still nearby
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1) {
                            continue;
                        }

                        BlockPos checkPos = this.pos.offset(dx, dy, dz);
                        BlockState checkState = this.level.getBlockState(checkPos);

                        if (checkState.is(Blocks.LAVA) ||
                            checkState.is(Blocks.MAGMA_BLOCK) ||
                            checkState.getFluidState().is(Fluids.LAVA)) {
                            return false; // Still has hot block nearby
                        }
                    }
                }
            }

            return true; // No hot block nearby, die
        }

        @Override
        protected void spawnJetParticles() {
            // Spawn steam particles
            for (int i = 0; i < this.jetStrength; i++) {
                double x = this.pos.getX() + 0.5 + getScaledRandomOffset();
                double y = this.pos.getY() + 0.5;
                double z = this.pos.getZ() + 0.5 + getScaledRandomOffset();

                // Upward velocity with some randomness
                double vx = getRandomOffset() * 0.05;
                double vy = 0.1 + RANDOM.nextDouble() * 0.1;
                double vz = getRandomOffset() * 0.05;

                this.level.addParticle(ParticleTypes.CLOUD, x, y, z, vx, vy, vz);
            }
        }
    }
}
