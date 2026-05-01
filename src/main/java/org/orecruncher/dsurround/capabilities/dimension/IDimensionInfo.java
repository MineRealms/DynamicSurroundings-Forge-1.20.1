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

package org.orecruncher.dsurround.capabilities.dimension;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Interface for dimension-specific information and properties.
 * Provides access to dimension characteristics like height limits,
 * weather capabilities, and environmental features.
 */
public interface IDimensionInfo extends INBTSerializable<CompoundTag> {

    /**
     * Gets the dimension ID.
     */
    String getId();

    /**
     * Gets the sea level height for this dimension.
     */
    int getSeaLevel();

    /**
     * Gets the maximum sky height for this dimension.
     */
    int getSkyHeight();

    /**
     * Gets the cloud rendering height.
     */
    int getCloudHeight();

    /**
     * Gets the space height (above sky).
     */
    int getSpaceHeight();

    /**
     * Indicates if this dimension has haze effects.
     */
    boolean hasHaze();

    /**
     * Indicates if this dimension can have auroras.
     */
    boolean hasAuroras();

    /**
     * Indicates if this dimension has weather.
     */
    boolean hasWeather();

    /**
     * Indicates if this dimension has fog effects.
     */
    boolean hasFog();

    /**
     * Indicates if biome sounds should play in this dimension.
     */
    boolean playBiomeSounds();

    /**
     * Indicates if this dimension is always considered "outside".
     */
    boolean alwaysOutside();
}
