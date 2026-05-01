package org.orecruncher.dsurround.effects.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Creates dust particles falling from blocks (e.g., sand, gravel)
 */
public class DustJetEffect extends BlockEffect {
    private static final int PARTICLE_COUNT = 2;

    public DustJetEffect() {
        super(BlockEffectType.DUST_JET, 30); // 30% chance to trigger
    }

    @Override
    protected boolean canTriggerAt(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        // Check if there's air below for dust to fall into
        BlockPos below = pos.below();
        return level.isEmptyBlock(below);
    }

    @Override
    public void doEffect(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        RandomSource random = level.random;

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double offsetX = random.nextGaussian() * 0.2;
            double offsetZ = random.nextGaussian() * 0.2;

            double particleX = pos.getX() + 0.5 + offsetX;
            double particleY = pos.getY() - 0.2;
            double particleZ = pos.getZ() + 0.5 + offsetZ;

            // Use block dust particle with the block's texture
            BlockParticleOption particleData = new BlockParticleOption(ParticleTypes.BLOCK, state);

            // Spawn falling dust particle
            level.addParticle(
                particleData,
                particleX, particleY, particleZ,
                0.0, -0.04, 0.0  // Downward motion
            );
        }
    }
}
