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

import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Tracks the lifecycle of an aurora effect.
 * Manages the fade-in, peak, and fade-out phases.
 */
@OnlyIn(Dist.CLIENT)
public class AuroraLifeTracker {

    protected final int peakAge;
    protected final int ageDelta;
    protected int timer;
    protected boolean isAlive = true;
    protected boolean isFading = false;

    /**
     * Create a new aurora life tracker.
     *
     * @param peakAge The age at which the aurora reaches peak brightness
     * @param ageDelta The rate at which the aurora ages per tick
     */
    public AuroraLifeTracker(int peakAge, int ageDelta) {
        this.peakAge = peakAge;
        this.ageDelta = ageDelta;
    }

    /**
     * Check if the aurora is still alive.
     *
     * @return true if the aurora is alive
     */
    public boolean isAlive() {
        return this.isAlive;
    }

    /**
     * Check if the aurora is fading.
     *
     * @return true if the aurora is fading
     */
    public boolean isFading() {
        return this.isFading;
    }

    /**
     * Set the fading state of the aurora.
     *
     * @param flag true to start fading, false to stop fading
     */
    public void setFading(boolean flag) {
        this.isFading = flag;
    }

    /**
     * Kill the aurora immediately.
     */
    public void kill() {
        this.isAlive = false;
        this.timer = 0;
    }

    /**
     * Get the age ratio of the aurora (0.0 to 1.0).
     * Used for calculating alpha/brightness.
     *
     * @return The age ratio
     */
    public float ageRatio() {
        return (float) this.timer / (float) this.peakAge;
    }

    /**
     * Update the aurora lifecycle.
     * Called once per tick.
     */
    public void update() {
        if (!this.isAlive) {
            return;
        }

        if (this.isFading) {
            this.timer -= this.ageDelta;
        } else {
            this.timer += this.ageDelta;
        }

        this.timer = Mth.clamp(this.timer, 0, this.peakAge);

        if (this.timer == 0 && this.isFading) {
            this.isAlive = false;
        }
    }
}
