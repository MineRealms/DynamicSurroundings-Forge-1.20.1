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
 * Each preset defines top (base/bright), middle, and bottom (fade) colors for the aurora gradient.
 * Based on the 1.12.2 color palette with additional variations.
 */
@OnlyIn(Dist.CLIENT)
public enum AuroraColor {

    // Classic aurora colors from 1.12.2
    AURORA_GREEN_1(
            new float[]{0.0F, 1.0F, 0.6F},      // Bright green base
            new float[]{0.0F, 1.0F, 0.6F},      // Middle
            new float[]{0.2F, 1.0F, 0.0F}       // Green fade
    ),
    BLUE_GREEN(
            new float[]{0.0F, 0.0F, 1.0F},      // Blue base
            new float[]{0.0F, 0.0F, 1.0F},      // Middle
            new float[]{0.0F, 1.0F, 0.0F}       // Green fade
    ),
    MAGENTA_GREEN(
            new float[]{1.0F, 0.0F, 1.0F},      // Magenta base
            new float[]{1.0F, 0.0F, 1.0F},      // Middle
            new float[]{0.0F, 1.0F, 0.0F}       // Green fade
    ),
    INDIGO_GREEN(
            new float[]{0.29F, 0.0F, 0.51F},    // Indigo base
            new float[]{0.29F, 0.0F, 0.51F},    // Middle
            new float[]{0.0F, 1.0F, 0.0F}       // Green fade
    ),
    TURQUOISE_LGREEN(
            new float[]{0.25F, 0.88F, 0.82F},   // Turquoise base
            new float[]{0.25F, 0.88F, 0.82F},   // Middle
            new float[]{0.56F, 0.93F, 0.56F}    // Light green fade
    ),
    YELLOW_RED(
            new float[]{1.0F, 1.0F, 0.0F},      // Yellow base
            new float[]{1.0F, 1.0F, 0.0F},      // Middle
            new float[]{1.0F, 0.0F, 0.0F}       // Red fade
    ),
    GREEN_RED(
            new float[]{0.0F, 1.0F, 0.0F},      // Green base
            new float[]{0.0F, 1.0F, 0.0F},      // Middle
            new float[]{1.0F, 0.0F, 0.0F}       // Red fade
    ),
    GREEN_YELLOW(
            new float[]{0.0F, 1.0F, 0.0F},      // Green base
            new float[]{0.0F, 1.0F, 0.0F},      // Middle
            new float[]{1.0F, 1.0F, 0.0F}       // Yellow fade
    ),
    RED_YELLOW(
            new float[]{1.0F, 0.0F, 0.0F},      // Red base
            new float[]{1.0F, 0.0F, 0.0F},      // Middle
            new float[]{1.0F, 1.0F, 0.0F}       // Yellow fade
    ),
    NAVY_INDIGO(
            new float[]{0.0F, 0.0F, 0.5F},      // Navy base
            new float[]{0.0F, 0.0F, 0.5F},      // Middle
            new float[]{0.29F, 0.0F, 0.51F}     // Indigo fade
    ),
    CYAN_MAGENTA(
            new float[]{0.0F, 1.0F, 1.0F},      // Cyan base
            new float[]{0.0F, 1.0F, 1.0F},      // Middle
            new float[]{1.0F, 0.0F, 1.0F}       // Magenta fade
    ),
    AURORA_CLASSIC(
            new float[]{0.0F, 1.0F, 0.41F},     // Aurora green base
            new float[]{0.0F, 0.41F, 0.58F},    // Aurora blue middle
            new float[]{1.0F, 0.08F, 0.58F}     // Aurora red fade
    ),

    // Warmer variations (increased luminance)
    YELLOW_RED_WARM(
            new float[]{1.0F, 1.0F, 0.3F},      // Warmer yellow
            new float[]{1.0F, 1.0F, 0.3F},
            new float[]{1.0F, 0.3F, 0.3F}       // Warmer red
    ),
    GREEN_RED_WARM(
            new float[]{0.3F, 1.0F, 0.3F},      // Warmer green
            new float[]{0.3F, 1.0F, 0.3F},
            new float[]{1.0F, 0.3F, 0.3F}       // Warmer red
    ),
    GREEN_YELLOW_WARM(
            new float[]{0.3F, 1.0F, 0.3F},      // Warmer green
            new float[]{0.3F, 1.0F, 0.3F},
            new float[]{1.0F, 1.0F, 0.3F}       // Warmer yellow
    ),
    BLUE_GREEN_WARM(
            new float[]{0.3F, 0.3F, 1.0F},      // Warmer blue
            new float[]{0.3F, 0.3F, 1.0F},
            new float[]{0.3F, 1.0F, 0.3F}       // Warmer green
    ),
    INDIGO_GREEN_WARM(
            new float[]{0.49F, 0.2F, 0.71F},    // Warmer indigo
            new float[]{0.49F, 0.2F, 0.71F},
            new float[]{0.3F, 1.0F, 0.3F}       // Warmer green
    ),
    AURORA_CLASSIC_WARM(
            new float[]{0.3F, 1.0F, 0.61F},     // Warmer aurora green
            new float[]{0.3F, 0.61F, 0.78F},    // Warmer aurora blue
            new float[]{1.0F, 0.38F, 0.78F}     // Warmer aurora red
    ),

    // Cooler variations (decreased luminance)
    YELLOW_RED_COOL(
            new float[]{0.7F, 0.7F, 0.0F},      // Cooler yellow
            new float[]{0.7F, 0.7F, 0.0F},
            new float[]{0.7F, 0.0F, 0.0F}       // Cooler red
    ),
    GREEN_RED_COOL(
            new float[]{0.0F, 0.7F, 0.0F},      // Cooler green
            new float[]{0.0F, 0.7F, 0.0F},
            new float[]{0.7F, 0.0F, 0.0F}       // Cooler red
    ),
    GREEN_YELLOW_COOL(
            new float[]{0.0F, 0.7F, 0.0F},      // Cooler green
            new float[]{0.0F, 0.7F, 0.0F},
            new float[]{0.7F, 0.7F, 0.0F}       // Cooler yellow
    ),
    BLUE_GREEN_COOL(
            new float[]{0.0F, 0.0F, 0.7F},      // Cooler blue
            new float[]{0.0F, 0.0F, 0.7F},
            new float[]{0.0F, 0.7F, 0.0F}       // Cooler green
    ),
    INDIGO_GREEN_COOL(
            new float[]{0.19F, 0.0F, 0.41F},    // Cooler indigo
            new float[]{0.19F, 0.0F, 0.41F},
            new float[]{0.0F, 0.7F, 0.0F}       // Cooler green
    ),
    AURORA_CLASSIC_COOL(
            new float[]{0.0F, 0.7F, 0.31F},     // Cooler aurora green
            new float[]{0.0F, 0.31F, 0.48F},    // Cooler aurora blue
            new float[]{0.7F, 0.0F, 0.48F}      // Cooler aurora red
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
     * Get the top color of the aurora gradient (base/bright color at bottom of aurora).
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
     * Get the bottom color of the aurora gradient (fade color at top of aurora).
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
