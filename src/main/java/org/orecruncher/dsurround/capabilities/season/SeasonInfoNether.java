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

package org.orecruncher.dsurround.capabilities.season;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Season info implementation for the Nether dimension.
 * The Nether has no precipitation and extreme heat.
 */
@OnlyIn(Dist.CLIENT)
public class SeasonInfoNether extends SeasonInfo {

    private static final float NETHER_TEMP = 2.0F;

    public SeasonInfoNether(@Nonnull Level level) {
        super(level);
    }

    @Override
    public float getFloatTemperature(@Nonnull net.minecraft.world.level.biome.Biome biome, @Nonnull BlockPos pos) {
        return NETHER_TEMP;
    }

    @Override
    public float getTemperature(@Nonnull BlockPos pos) {
        return NETHER_TEMP;
    }

    @Override
    @Nonnull
    public PrecipitationType getPrecipitationType(@Nonnull BlockPos pos) {
        return PrecipitationType.NONE;
    }

    @Override
    public boolean canWaterFreeze(@Nonnull BlockPos pos) {
        return false;
    }

    @Override
    public boolean showFrostBreath(@Nonnull BlockPos pos) {
        return false;
    }
}
