package org.orecruncher.dsurround.effects.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Creates firefly particles around blocks (typically in forests at night)
 */
public class FireFlyEffect extends BlockEffect {

    public FireFlyEffect() {
        super(BlockEffectType.FIREFLY, 10); // 10% chance to trigger (rare)
    }

    @Override
    protected boolean canTriggerAt(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        // Check if it's night time
        long dayTime = level.getDayTime() % 24000;
        boolean isNight = dayTime >= 13000 && dayTime <= 23000;

        // Check light level (fireflies prefer darkness)
        int lightLevel = level.getBrightness(LightLayer.BLOCK, pos);

        return isNight && lightLevel < 8;
    }

    @Override
    public void doEffect(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        RandomSource random = level.random;

        // Only spawn occasionally (10% of the time when triggered)
        if (random.nextInt(10) != 0) {
            return;
        }

        // Get block shape - skip if empty
        var shape = state.getShape(level, pos);
        if (shape.isEmpty()) {
            return;
        }

        // Get block bounding box center
        AABB box = shape.bounds();
        Vec3 center = box.getCenter();

        double x = pos.getX() + center.x;
        double y = pos.getY() + box.maxY;
        double z = pos.getZ() + center.z;

        // Add some random offset
        x += (random.nextDouble() - 0.5) * 0.5;
        y += (random.nextDouble() - 0.5) * 0.5;
        z += (random.nextDouble() - 0.5) * 0.5;

        // Slow floating motion
        double motionX = (random.nextDouble() - 0.5) * 0.02;
        double motionY = random.nextDouble() * 0.02;
        double motionZ = (random.nextDouble() - 0.5) * 0.02;

        // Use glow particle for firefly effect
        level.addParticle(
            ParticleTypes.GLOW,
            x, y, z,
            motionX, motionY, motionZ
        );
    }
}
