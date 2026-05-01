/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.orecruncher.dsurround.aurora;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.dsurround.lib.GameUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Utility methods for aurora rendering and calculations.
 */
@OnlyIn(Dist.CLIENT)
public class AuroraUtils {

    private AuroraUtils() {
        // Utility class
    }

    public static final int PLAYER_FIXED_Y_OFFSET = 64;
    public static final int PLAYER_FIXED_Z_OFFSET = 150;

    public static final int AURORA_PEAK_AGE = 512;
    public static final int AURORA_AGE_RATE = 1;

    /**
     * Get the chunk render distance setting.
     *
     * @return The render distance in chunks
     */
    public static int getChunkRenderDistance() {
        return Minecraft.getInstance().options.renderDistance().get();
    }

    /**
     * Returns a time calculation based on the number of ticks that have occurred
     * combined with the current partial tick count.
     * Not usable for actual time calculations.
     *
     * @return Time in seconds (for animation)
     */
    public static float getTimeSeconds() {
        var level = GameUtils.getWorld().orElse(null);
        if (level == null) {
            return 0.0F;
        }

        long gameTicks = level.getGameTime();
        float partialTick = Minecraft.getInstance().getFrameTime();
        return ((float) gameTicks + partialTick) / 20.0F;
    }

    /**
     * Use cached dimension info to determine if auroras are possible for the dimension.
     *
     * @return true if auroras can spawn in this dimension
     */
    public static boolean dimensionHasAuroras() {
        // TODO: Implement dimension info check when capabilities are migrated
        // For now, only allow auroras in the overworld
        var level = GameUtils.getWorld().orElse(null);
        if (level == null) {
            return false;
        }

        return level.dimension() == net.minecraft.world.level.Level.OVERWORLD;
    }

    /**
     * Generate a seed for an aurora based on the current day.
     * This ensures auroras are consistent for a given day.
     *
     * @return A seed value
     */
    public static long getSeed() {
        // Get GMT day as seed base
        ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("GMT"));
        long daysSinceEpoch = now.toLocalDate().toEpochDay();

        // Add game day if available
        var level = GameUtils.getWorld().orElse(null);
        if (level != null) {
            long gameDay = level.getDayTime() / 24000L;
            return daysSinceEpoch + gameDay;
        }

        return daysSinceEpoch;
    }
}
