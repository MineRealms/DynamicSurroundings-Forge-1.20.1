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
 * Represents a single panel/node in an aurora band.
 * Each panel has a position, texture coordinates, and alpha value.
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

    /**
     * Create a new panel with default values.
     */
    public Panel() {
        this.posX = 0.0;
        this.posY = 0.0;
        this.posZ = 0.0;
        this.tetX = 0.0;
        this.tetY = 0.0;
        this.tetZ = 0.0;
        this.tetX2 = 0.0;
        this.tetY2 = 0.0;
        this.tetZ2 = 0.0;
        this.u = 0.0F;
        this.v = 0.0F;
        this.alpha = 1.0F;
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
