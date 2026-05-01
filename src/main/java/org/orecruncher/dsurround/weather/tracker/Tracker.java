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

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.dsurround.weather.Weather;

/**
 * Base weather tracker that provides vanilla Minecraft weather behavior.
 * Subclasses can override methods to provide custom weather simulation or server synchronization.
 */
@OnlyIn(Dist.CLIENT)
public class Tracker {

    public Tracker() {
    }

    protected String type() {
        return "VANILLA";
    }

    public final boolean isRaining() {
        return getIntensityLevel() > 0F;
    }

    public final boolean isThundering() {
        return getWorld().isThundering();
    }

    public Weather.Properties getWeatherProperties() {
        return Weather.Properties.VANILLA;
    }

    public float getIntensityLevel() {
        return getWorld().getRainLevel(1.0F);
    }

    public float getMaxIntensityLevel() {
        return 1.0F;
    }

    public int getNextRainChange() {
        // TODO: Access rain time from level data when available
        return 0;
    }

    public float getThunderStrength() {
        return getWorld().getThunderLevel(1.0F);
    }

    public int getNextThunderChange() {
        // TODO: Access thunder time from level data when available
        return 0;
    }

    public int getNextThunderEvent() {
        return 0;
    }

    public float getCurrentVolume() {
        return 0.66F;
    }

    public SoundEvent getCurrentStormSound() {
        return Weather.Properties.VANILLA.getStormSound();
    }

    public SoundEvent getCurrentDustSound() {
        return Weather.Properties.VANILLA.getDustSound();
    }

    public boolean doVanilla() {
        return true;
    }

    public boolean backgroundThunderPossible() {
        return false;
    }

    public void update() {
        // Base implementation does nothing
    }

    @Override
    public String toString() {
        final Weather.Properties props = getWeatherProperties();
        final StringBuilder builder = new StringBuilder();
        builder.append("Storm: ").append(props.name());
        builder.append(" ").append(getIntensityLevel()).append('/').append(getMaxIntensityLevel());
        builder.append(" vanilla: ").append(getWorld().getRainLevel(1.0F));
        if (backgroundThunderPossible())
            builder.append(" thunder event: ").append(getNextThunderEvent());
        builder.append(" (").append(type()).append(')');
        return builder.toString();
    }

    protected static Level getWorld() {
        return Minecraft.getInstance().level;
    }
}
