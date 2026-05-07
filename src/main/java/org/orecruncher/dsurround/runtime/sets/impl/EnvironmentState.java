package org.orecruncher.dsurround.runtime.sets.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.orecruncher.dsurround.lib.GameUtils;
import org.orecruncher.dsurround.lib.scripting.VariableSet;
import org.orecruncher.dsurround.processing.Scanners;
import org.orecruncher.dsurround.runtime.sets.IEnvironmentState;

public class EnvironmentState extends VariableSet<IEnvironmentState> implements IEnvironmentState {

    private final Scanners scanner;

    public EnvironmentState(Scanners scanner) {
        super("state");
        this.scanner = scanner;
    }

    @Override
    public IEnvironmentState getInterface() {
        return this;
    }

    @Override
    public boolean isInVillage() {
        return this.scanner.isInVillage();
    }

    @Override
    public boolean isInside() {
        return this.scanner.isInside();
    }

    @Override
    public boolean isUnderWater() {
        return this.scanner.isUnderwater();
    }

    @Override
    public boolean hasBlockNearby(String blockId, int range) {
        Level world = GameUtils.getWorld().orElse(null);
        if (world == null || world.isClientSide)
            return false;

        Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(blockId));
        if (block == null)
            return false;

        var player = world.getNearestPlayer(0.5, 0.5, 0.5, range * 2, false);
        if (player == null)
            return false;

        BlockPos playerPos = player.blockPosition();
        int count = 0;
        int checkRange = range + 1;
        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.getX() - checkRange, playerPos.getY() - checkRange, playerPos.getZ() - checkRange,
                playerPos.getX() + checkRange, playerPos.getY() + checkRange, playerPos.getZ() + checkRange)) {
            if (world.getBlockState(pos).is(block)) {
                count++;
                if (count > 4)
                    return true;
            }
        }
        return false;
    }
}
