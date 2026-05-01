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

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Default implementation of season information.
 * Provides temperature and precipitation queries based on vanilla biome data.
 */
@OnlyIn(Dist.CLIENT)
public class SeasonInfo implements ISeasonInfo {

    protected static final float FREEZE_TEMP = 0.15F;
    protected static final float BREATH_TEMP = 0.2F;

    protected final Level level;

    public SeasonInfo() {
        this.level = null;
    }

    public SeasonInfo(@Nonnull Level level) {
        this.level = level;
    }

    @Override
    @Nullable
    public Level getWorld() {
        return this.level;
    }

    @Override
    @Nonnull
    public SeasonType getSeasonType() {
        return SeasonType.NONE;
    }

    @Override
    @Nonnull
    public SeasonType.SubType getSeasonSubType() {
        return SeasonType.SubType.NONE;
    }

    @Override
    @Nonnull
    public String getSeasonString() {
        // TODO: Implement localization
        return "No Season";
    }

    @Override
    @Nonnull
    public TemperatureRating getPlayerTemperature() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            return getBiomeTemperature(mc.player.blockPosition());
        }
        return TemperatureRating.MILD;
    }

    @Override
    @Nonnull
    public TemperatureRating getBiomeTemperature(@Nonnull BlockPos pos) {
        return TemperatureRating.fromTemp(getTemperature(pos));
    }

    @Override
    @Nonnull
    public BlockPos getPrecipitationHeight(@Nonnull BlockPos pos) {
        if (this.level != null) {
            return this.level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, pos);
        }
        return pos;
    }

    @Override
    public float getFloatTemperature(@Nonnull Biome biome, @Nonnull BlockPos pos) {
        // In 1.20.1, use biome.getTemperature(pos)
        return biome.getBaseTemperature();
    }

    @Override
    public float getTemperature(@Nonnull BlockPos pos) {
        if (this.level != null) {
            Biome biome = this.level.getBiome(pos).value();
            return getFloatTemperature(biome, pos);
        }
        return 0.5F;
    }

    @Override
    public boolean canWaterFreeze(@Nonnull BlockPos pos) {
        return getTemperature(pos) < FREEZE_TEMP;
    }

    @Override
    public boolean showFrostBreath(@Nonnull BlockPos pos) {
        return getTemperature(pos) < BREATH_TEMP;
    }

    @Override
    @Nonnull
    public PrecipitationType getPrecipitationType(@Nonnull BlockPos pos) {
        if (this.level == null) {
            return PrecipitationType.NONE;
        }

        Biome biome = this.level.getBiome(pos).value();

        // Check if biome has precipitation
        if (biome.getPrecipitationAt(pos) == Biome.Precipitation.NONE) {
            return PrecipitationType.NONE;
        }

        // Determine rain vs snow based on temperature
        float temp = getFloatTemperature(biome, pos);
        if (temp < FREEZE_TEMP) {
            return PrecipitationType.SNOW;
        }

        return PrecipitationType.RAIN;
    }

    /**
     * Factory method to create appropriate SeasonInfo for a level.
     * Can be extended to support season mods like Serene Seasons.
     */
    @Nonnull
    public static SeasonInfo factory(@Nonnull Level level) {
        // Check for Nether dimension
        if (level.dimension() == Level.NETHER) {
            return new SeasonInfoNether(level);
        }

        // TODO: Add support for Serene Seasons mod if present
        // if (ModEnvironment.SereneSeasons.isLoaded()) {
        //     return new SeasonInfoSereneSeasons(level);
        // }

        return new SeasonInfo(level);
    }
}
