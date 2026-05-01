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
 * Bubble jet effect that spawns bubble particles rising from underwater blocks.
 * Creates columns of bubbles from certain blocks underwater.
 */
public class BubbleJetEffect extends BlockEffect {

    public BubbleJetEffect() {
        super(BlockEffectType.BUBBLE_JET, 25); // 25% chance to trigger
    }

    @Override
    protected boolean canTriggerAt(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        // Check if bubble jets are enabled
        if (!Client.Config.blockEffects.bubbleColumnEnabled) {
            return false;
        }

        // Must be underwater (water above)
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        if (!aboveState.getFluidState().is(Fluids.WATER)) {
            return false;
        }

        // Check if this is a bubble-producing block
        return isBubbleProducingBlock(state);
    }

    @Override
    public void doEffect(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        // Create or get existing bubble jet particle system
        if (!ParticleSystemManager.getInstance().hasSystem(pos)) {
            int strength = calculateBubbleStrength(state);
            BubbleJetParticleSystem system = new BubbleJetParticleSystem(level, pos, strength, 2, 150);
            ParticleSystemManager.getInstance().addSystem(system);
        }
    }

    /**
     * Check if this block produces bubbles
     */
    private boolean isBubbleProducingBlock(@NotNull BlockState state) {
        // Magma blocks produce downward bubbles (vanilla mechanic)
        // Soul sand produces upward bubbles (vanilla mechanic)
        // We'll add bubbles to other blocks for variety
        return state.is(Blocks.GRAVEL) ||
               state.is(Blocks.SAND) ||
               state.is(Blocks.DIRT) ||
               state.is(Blocks.CLAY) ||
               state.is(Blocks.STONE);
    }

    /**
     * Calculate bubble strength based on block type
     */
    private int calculateBubbleStrength(@NotNull BlockState state) {
        if (state.is(Blocks.GRAVEL) || state.is(Blocks.SAND)) {
            return 3; // More bubbles from loose materials
        }
        return 2; // Default strength
    }

    /**
     * Particle system for bubble jets
     */
    private static class BubbleJetParticleSystem extends ParticleJet {

        public BubbleJetParticleSystem(@NotNull Level level, @NotNull BlockPos pos, int jetStrength, int updateFrequency, int maxAge) {
            super(level, pos, jetStrength, updateFrequency, maxAge);
        }

        @Override
        protected boolean shouldDie() {
            if (super.shouldDie()) {
                return true;
            }

            // Check if still underwater
            BlockPos above = this.pos.above();
            BlockState aboveState = this.level.getBlockState(above);
            if (!aboveState.getFluidState().is(Fluids.WATER)) {
                return true;
            }

            // Check if source block still present
            BlockState state = this.level.getBlockState(this.pos);
            return state.isAir();
        }

        @Override
        protected void spawnJetParticles() {
            // Spawn bubble particles
            for (int i = 0; i < this.jetStrength; i++) {
                double x = this.pos.getX() + 0.5 + getScaledRandomOffset();
                double y = this.pos.getY() + 1.0;
                double z = this.pos.getZ() + 0.5 + getScaledRandomOffset();

                // Upward velocity with some randomness
                double vx = getRandomOffset() * 0.02;
                double vy = 0.05 + RANDOM.nextDouble() * 0.05;
                double vz = getRandomOffset() * 0.02;

                // Use bubble particles
                this.level.addParticle(ParticleTypes.BUBBLE, x, y, z, vx, vy, vz);

                // Occasionally add bubble column particles for variety
                if (RANDOM.nextInt(3) == 0) {
                    this.level.addParticle(ParticleTypes.BUBBLE_COLUMN_UP, x, y, z, vx, vy, vz);
                }
            }
        }
    }
}