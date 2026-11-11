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

/**
 * A single node of an aurora band, matching the 1.12.2 Panel.
 * The band is a ribbon meandering in the XZ plane; each node carries a vertical
 * curtain whose height is posY. The travelling wave is applied to dZ (horizontal
 * sway), NOT to the curtain height - putting it on Y produces a row of sine-wave
 * columns instead of an aurora.
 */
@OnlyIn(Dist.CLIENT)
public class Panel {

    public double posX;
    public double posY;
    public double posZ;
    public double tetX;
    public double tetY;
    public double tetZ;
    public double tetX2;
    public double tetY2;
    public double tetZ2;

    public float u;
    public float v;

    public float alpha;

    /** Heading of this node along the ribbon, in degrees. */
    public double angle;

    /** Per-frame travelling wave offsets. */
    public double dZ;
    public double dY;

    private double sinDeg90;
    private double sinDeg270;

    /**
     * Create a new panel with default values.
     */
    public Panel() {
        this.alpha = 1.0F;
    }

    /** Curtain height with the wave applied, clamped so it never goes below the horizon. */
    public double getModdedY() {
        final double y = this.posY + this.dY;
        return y < 0.0D ? 0.0D : y;
    }

    /** Z position with the horizontal sway applied. */
    public double getModdedZ() {
        return this.posZ + this.dZ;
    }

    public void setDeltaZ(double f) {
        this.dZ = f;
    }

    public void setDeltaY(double f) {
        this.dY = f;
    }

    /**
     * Set the half-width of the curtain. cos(90) and cos(270) are 0, so both
     * tetrahedral X edges stay on posX; sin(90)=1 and sin(270)=-1 give the two
     * Z edges. Mirrors 1.12.2 Panel.setWidth().
     */
    public void setWidth(double w) {
        this.sinDeg90 = w;
        this.sinDeg270 = -w;
        this.tetX = this.posX;
        this.tetX2 = this.posX;
    }

    /** Apply the current wave and width to the tetrahedral edge coordinates. */
    public void applyThickness() {
        final double mZ = getModdedZ();
        this.tetZ = mZ + this.sinDeg90;
        this.tetZ2 = mZ + this.sinDeg270;
        this.tetY = getModdedY();
        this.tetY2 = this.tetY;
    }

    /**
     * Set the position of this panel.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return This panel for chaining
     */
    public Panel setPos(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        return this;
    }

    /**
     * Set the first tetrahedral coordinate.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return This panel for chaining
     */
    public Panel setTet(double x, double y, double z) {
        this.tetX = x;
        this.tetY = y;
        this.tetZ = z;
        return this;
    }

    /**
     * Set the second tetrahedral coordinate.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return This panel for chaining
     */
    public Panel setTet2(double x, double y, double z) {
        this.tetX2 = x;
        this.tetY2 = y;
        this.tetZ2 = z;
        return this;
    }

    /**
     * Set the texture coordinates.
     *
     * @param u U coordinate
     * @param v V coordinate
     * @return This panel for chaining
     */
    public Panel setUV(float u, float v) {
        this.u = u;
        this.v = v;
        return this;
    }

    /**
     * Set the alpha value.
     *
     * @param alpha Alpha value (0.0-1.0)
     * @return This panel for chaining
     */
    public Panel setAlpha(float alpha) {
        this.alpha = alpha;
        return this;
    }
}
