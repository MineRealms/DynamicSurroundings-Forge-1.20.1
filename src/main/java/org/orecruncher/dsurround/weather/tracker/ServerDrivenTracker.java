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

package org.orecruncher.dsurround.weather.tracker;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Server-driven weather tracker that receives weather updates from the server.
 * Used when Dynamic Surroundings is installed on both client and server.
 */
@OnlyIn(Dist.CLIENT)
public class ServerDrivenTracker extends SimulationTracker {

    protected int nextRainChange = 0;
    protected float thunderStrength = 0.0F;
    protected int nextThunderChange = 0;

    @Override
    protected String type() {
        return "SERVER";
    }

    @Override
    public int getNextRainChange() {
        return this.nextRainChange;
    }

    @Override
    public float getThunderStrength() {
        return this.thunderStrength;
    }

    @Override
    public int getNextThunderChange() {
        return this.nextThunderChange;
    }

    @Override
    public float getCurrentVolume() {
        return 0.05F + 0.95F * this.intensityLevel;
    }

    @Override
    public SoundEvent getCurrentStormSound() {
        return this.intensity.getStormSound();
    }

    @Override
    public SoundEvent getCurrentDustSound() {
        return this.intensity.getDustSound();
    }

    /**
     * Updates weather state from server event.
     * TODO: Implement WeatherUpdateEvent and call this method when event is received.
     *
     * @param rainIntensity Current rain intensity
     * @param maxRainIntensity Maximum rain intensity
     * @param nextRainChange Ticks until next rain change
     * @param thunderStrength Thunder strength
     * @param nextThunderChange Ticks until next thunder change
     * @param nextThunderEvent Ticks until next thunder event
     */
    public void updateFromServer(float rainIntensity, float maxRainIntensity, int nextRainChange,
                                  float thunderStrength, int nextThunderChange, int nextThunderEvent) {
        this.maxIntensityLevel = maxRainIntensity;
        this.nextRainChange = nextRainChange;
        this.thunderStrength = thunderStrength;
        this.nextThunderChange = nextThunderChange;
        this.nextThunderEvent = nextThunderEvent;
        setCurrentIntensity(rainIntensity);
    }

    @Override
    public void update() {
        // Don't want to do the simulation - we get updates from the server
    }
}
