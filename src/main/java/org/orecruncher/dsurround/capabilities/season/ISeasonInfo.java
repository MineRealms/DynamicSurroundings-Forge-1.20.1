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
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for season-related information and temperature queries.
 * Client-side only capability for determining weather effects and temperature.
 */
@OnlyIn(Dist.CLIENT)
public interface ISeasonInfo {

    /**
     * Gets the world this season info is attached to.
     */
    @Nullable
    Level getWorld();

    /**
     * Gets the current season type.
     */
    @Nonnull
    SeasonType getSeasonType();

    /**
     * Gets the current season sub-type (early/mid/late).
     */
    @Nonnull
    SeasonType.SubType getSeasonSubType();

    /**
     * Gets a localized string representation of the current season.
     */
    @Nonnull
    String getSeasonString();

    /**
     * Gets the temperature rating at the player's current position.
     */
    @Nonnull
    TemperatureRating getPlayerTemperature();

    /**
     * Gets the temperature rating for a specific position.
     */
    @Nonnull
    TemperatureRating getBiomeTemperature(@Nonnull BlockPos pos);

    /**
     * Gets the height at which precipitation occurs for a given position.
     */
    @Nonnull
    BlockPos getPrecipitationHeight(@Nonnull BlockPos pos);

    /**
     * Gets the float temperature value for a biome at a specific position.
     */
    float getFloatTemperature(@Nonnull Biome biome, @Nonnull BlockPos pos);

    /**
     * Gets the temperature at a specific position.
     */
    float getTemperature(@Nonnull BlockPos pos);

    /**
     * Checks if water can freeze at the given position.
     */
    boolean canWaterFreeze(@Nonnull BlockPos pos);

    /**
     * Checks if frost breath should be shown at the given position.
     */
    boolean showFrostBreath(@Nonnull BlockPos pos);

    /**
     * Determines the type of precipitation for a given position.
     */
    @Nonnull
    PrecipitationType getPrecipitationType(@Nonnull BlockPos pos);
}
