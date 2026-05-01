package org.orecruncher.dsurround.effects.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.orecruncher.dsurround.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Scans blocks around the player to trigger block effects.
 * Uses a random sampling approach similar to Minecraft's vanilla particle system.
 */
public class BlockEffectScanner {
    private static final Random RANDOM = new Random();

    // Number of random blocks to check per tick
    private static final int ITERATIONS_PER_TICK = 667;

    // Registered block effects
    private final List<BlockEffect> effects;

    // Temporary position for scanning
    private final BlockPos.MutableBlockPos scanPos;

    public BlockEffectScanner() {
        this.effects = new ArrayList<>();
        this.scanPos = new BlockPos.MutableBlockPos();
    }

    /**
     * Register a block effect to be checked during scanning
     */
    public void registerEffect(@NotNull BlockEffect effect) {
        this.effects.add(effect);
    }

    /**
     * Clear all registered effects
     */
    public void clearEffects() {
        this.effects.clear();
    }

    /**
     * Scan blocks around the player and trigger effects.
     * Called every tick.
     */
    public void onTick() {
        Player player = Minecraft.getInstance().player;
        if (player == null || this.effects.isEmpty()) {
            return;
        }

        Level level = player.level();
        if (!level.isClientSide) {
            return;
        }

        // Get scan range from config
        int range = Client.Config.blockEffects.blockEffectRange;

        BlockPos playerPos = player.blockPosition();

        // Perform random block sampling
        for (int i = 0; i < ITERATIONS_PER_TICK; i++) {
            // Random position within range
            int x = playerPos.getX() + RANDOM.nextInt(range * 2 + 1) - range;
            int y = playerPos.getY() + RANDOM.nextInt(range * 2 + 1) - range;
            int z = playerPos.getZ() + RANDOM.nextInt(range * 2 + 1) - range;

            this.scanPos.set(x, y, z);

            // Skip if not loaded
            if (!level.isLoaded(this.scanPos)) {
                continue;
            }

            // Get block state
            BlockState state = level.getBlockState(this.scanPos);
            if (state.isAir()) {
                continue;
            }

            // Check all registered effects
            for (BlockEffect effect : this.effects) {
                if (effect.canTrigger(level, this.scanPos, state)) {
                    effect.doEffect(level, this.scanPos, state);
                    // Only trigger one effect per block per tick
                    break;
                }
            }
        }
    }

    /**
     * Get the number of registered effects
     */
    public int getEffectCount() {
        return this.effects.size();
    }
}
