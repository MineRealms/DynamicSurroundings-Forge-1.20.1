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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.dsurround.lib.random.IRandomizer;

/**
 * Predefined color sets for aurora effects.
 * Each preset defines top, middle, and bottom colors for the aurora gradient.
 */
@OnlyIn(Dist.CLIENT)
public enum AuroraColor {

    BLUE_GREEN(
            new float[]{0.0F, 1.0F, 0.41F},
            new float[]{0.0F, 0.78F, 0.78F},
            new float[]{0.0F, 1.0F, 0.41F}
    ),
    GREEN(
            new float[]{0.28F, 0.5F, 0.0F},
            new float[]{0.47F, 0.87F, 0.19F},
            new float[]{0.28F, 0.5F, 0.0F}
    ),
    RED_GREEN(
            new float[]{1.0F, 0.0F, 0.0F},
            new float[]{0.28F, 0.5F, 0.0F},
            new float[]{0.47F, 0.87F, 0.19F}
    ),
    PURPLE(
            new float[]{0.62F, 0.31F, 0.62F},
            new float[]{0.81F, 0.69F, 0.87F},
            new float[]{0.62F, 0.31F, 0.62F}
    ),
    YELLOW(
            new float[]{1.0F, 1.0F, 0.0F},
            new float[]{1.0F, 0.84F, 0.0F},
            new float[]{1.0F, 1.0F, 0.0F}
    ),
    ORANGE(
            new float[]{1.0F, 0.5F, 0.0F},
            new float[]{1.0F, 0.64F, 0.0F},
            new float[]{1.0F, 0.5F, 0.0F}
    ),
    BLUE(
            new float[]{0.0F, 0.0F, 1.0F},
            new float[]{0.25F, 0.41F, 0.88F},
            new float[]{0.0F, 0.0F, 1.0F}
    ),
    RED(
            new float[]{1.0F, 0.0F, 0.0F},
            new float[]{0.86F, 0.08F, 0.24F},
            new float[]{1.0F, 0.0F, 0.0F}
    );

    private final float[] topColor;
    private final float[] middleColor;
    private final float[] bottomColor;

    AuroraColor(float[] topColor, float[] middleColor, float[] bottomColor) {
        this.topColor = topColor;
        this.middleColor = middleColor;
        this.bottomColor = bottomColor;
    }

    /**
     * Get the top color of the aurora gradient.
     *
     * @return RGB color array [r, g, b] with values 0.0-1.0
     */
    public float[] getTopColor() {
        return this.topColor;
    }

    /**
     * Get the middle color of the aurora gradient.
     *
     * @return RGB color array [r, g, b] with values 0.0-1.0
     */
    public float[] getMiddleColor() {
        return this.middleColor;
    }

    /**
     * Get the bottom color of the aurora gradient.
     *
     * @return RGB color array [r, g, b] with values 0.0-1.0
     */
    public float[] getBottomColor() {
        return this.bottomColor;
    }

    /**
     * Get a random aurora color preset.
     *
     * @param random The randomizer to use
     * @return A random aurora color preset
     */
    public static AuroraColor random(IRandomizer random) {
        AuroraColor[] values = values();
        return values[random.nextInt(values.length)];
    }
}
