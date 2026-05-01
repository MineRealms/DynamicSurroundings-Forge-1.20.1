package org.orecruncher.dsurround.effects.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Creates fountain effect - particles shooting upward from blocks
 */
public class FountainJetEffect extends BlockEffect {

    public FountainJetEffect() {
        super(BlockEffectType.FOUNTAIN_JET, 25); // 25% chance to trigger
    }

    @Override
    protected boolean canTriggerAt(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        // Check if there's air above for fountain to shoot into
        BlockPos above = pos.above();
        return level.isEmptyBlock(above);
    }

    @Override
    public void doEffect(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        RandomSource random = level.random;

        // Spawn one particle per tick
        double motionX = random.nextGaussian() * 0.03;
        double motionZ = random.nextGaussian() * 0.03;
        double motionY = 0.5; // Strong upward motion

        double offsetX = random.nextGaussian() * 0.2;
        double offsetZ = random.nextGaussian() * 0.2;

        double particleX = pos.getX() + 0.5 + offsetX;
        double particleY = pos.getY() + 1.1;
        double particleZ = pos.getZ() + 0.5 + offsetZ;

        // Use block particle with upward motion
        BlockParticleOption particleData = new BlockParticleOption(ParticleTypes.BLOCK, state);

        level.addParticle(
            particleData,
            particleX, particleY, particleZ,
            motionX, motionY, motionZ
        );
    }
}
