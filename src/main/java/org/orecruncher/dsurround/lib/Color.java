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
package org.orecruncher.dsurround.lib;

/**
 * Simple RGB color class for fog calculations
 */
public class Color {
    private float red;
    private float green;
    private float blue;

    public Color(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public Color(int red, int green, int blue) {
        this(red / 255F, green / 255F, blue / 255F);
    }

    public Color(Color other) {
        this.red = other.red;
        this.green = other.green;
        this.blue = other.blue;
    }

    public float red() {
        return this.red;
    }

    public float green() {
        return this.green;
    }

    public float blue() {
        return this.blue;
    }

    public Color add(Color other) {
        this.red += other.red;
        this.green += other.green;
        this.blue += other.blue;
        return this;
    }

    public Color scale(float factor) {
        this.red *= factor;
        this.green *= factor;
        this.blue *= factor;
        return this;
    }

    public Color scale(float rScale, float gScale, float bScale) {
        this.red *= rScale;
        this.green *= gScale;
        this.blue *= bScale;
        return this;
    }

    @Override
    public String toString() {
        return String.format("Color[r=%.3f, g=%.3f, b=%.3f]", red, green, blue);
    }
}
