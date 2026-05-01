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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Enum representing temperature ratings for biomes and locations.
 */
@OnlyIn(Dist.CLIENT)
public enum TemperatureRating {
    ICY,
    COLD,
    COOL,
    MILD,
    WARM,
    HOT;

    /**
     * Converts a temperature float value to a TemperatureRating.
     *
     * @param temp Temperature value (typically 0.0 to 2.0 from Minecraft biomes)
     * @return Corresponding TemperatureRating
     */
    public static TemperatureRating fromTemp(float temp) {
        if (temp < 0.15F)
            return ICY;
        if (temp < 0.3F)
            return COLD;
        if (temp < 0.5F)
            return COOL;
        if (temp < 0.8F)
            return MILD;
        if (temp < 1.2F)
            return WARM;
        return HOT;
    }
}
