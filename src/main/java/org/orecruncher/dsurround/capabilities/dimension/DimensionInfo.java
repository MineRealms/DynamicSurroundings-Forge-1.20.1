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
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.orecruncher.dsurround.lib.math.MathStuff;

import java.text.DecimalFormat;
import java.util.Random;

/**
 * Implementation of dimension information and weather state tracking.
 * Stores both static dimension properties and dynamic weather data.
 */
public final class DimensionInfo implements IDimensionInfoEx {

    public static final IDimensionInfo NONE = new DimensionInfo();

    public static final float MIN_INTENSITY = 0.0F;
    public static final float MAX_INTENSITY = 1.0F;

    private static final int SPACE_HEIGHT_OFFSET = 32;
    private static final DecimalFormat FORMATTER = new DecimalFormat("0");

    protected final Random random = new Random();
    protected final Level level;

    // Rain/weather tracking data
    private float intensity = 0.0F;
    private float currentIntensity = 0.0F;
    private float minIntensity = 0.1F; // Default min rain strength
    private float maxIntensity = 1.0F; // Default max rain strength
    private int thunderTimer = 0;

    // Dimension attributes
    protected final String dimensionId;
    protected final String name;
    protected int seaLevel;
    protected int skyHeight;
    protected int cloudHeight;
    protected int spaceHeight;
    protected boolean hasHaze = false;
    protected boolean hasAuroras = false;
    protected boolean hasWeather = false;
    protected boolean hasFog = false;
    protected boolean alwaysOutside = false;
    protected boolean playBiomeSounds = true;

    /**
     * Default constructor for NONE instance.
     */
    public DimensionInfo() {
        this.level = null;
        this.dimensionId = "none";
        this.name = "<DEFAULT NONE>";
        this.seaLevel = 64;
        this.skyHeight = 256;
        this.cloudHeight = 128;
        this.spaceHeight = 288;
    }

    /**
     * Constructor for a specific level.
     */
    public DimensionInfo(Level level) {
        this.level = level;

        // Get dimension key
        ResourceKey<Level> dimKey = level.dimension();
        this.dimensionId = dimKey.location().toString();
        this.name = dimKey.location().getPath();

        // Set height properties
        this.seaLevel = level.getSeaLevel();
        this.skyHeight = level.getHeight();
        this.cloudHeight = this.skyHeight;
        this.spaceHeight = this.skyHeight + SPACE_HEIGHT_OFFSET;

        // Determine if this is an overworld-like dimension
        if (level.dimensionType().hasSkyLight()) {
            this.hasWeather = true;
            this.hasAuroras = true;
            this.hasFog = true;
        }

        // TODO: Load dimension config overrides from configuration system
        // This would allow customization of hasHaze, hasAuroras, etc.
    }

    // ===================================
    // IDimensionInfo implementation
    // ===================================

    @Override
    public String getId() {
        return this.dimensionId;
    }

    @Override
    public int getSeaLevel() {
        return this.seaLevel;
    }

    @Override
    public int getSkyHeight() {
        return this.skyHeight;
    }

    @Override
    public int getCloudHeight() {
        return this.cloudHeight;
    }

    @Override
    public int getSpaceHeight() {
        return this.spaceHeight;
    }

    @Override
    public boolean hasHaze() {
        return this.hasHaze;
    }

    @Override
    public boolean hasAuroras() {
        return this.hasAuroras;
    }

    @Override
    public boolean hasWeather() {
        return this.hasWeather;
    }

    @Override
    public boolean hasFog() {
        return this.hasFog;
    }

    @Override
    public boolean playBiomeSounds() {
        return this.playBiomeSounds;
    }

    @Override
    public boolean alwaysOutside() {
        return this.alwaysOutside;
    }

    // ===================================
    // IDimensionInfoEx implementation
    // ===================================

    @Override
    public float getRainIntensity() {
        return this.intensity;
    }

    @Override
    public float getCurrentRainIntensity() {
        return this.currentIntensity;
    }

    @Override
    public void setRainIntensity(float intensity) {
        this.intensity = MathStuff.clamp(intensity, MIN_INTENSITY, MAX_INTENSITY);
    }

    @Override
    public void setCurrentRainIntensity(float intensity) {
        this.currentIntensity = MathStuff.clamp(intensity, 0, this.intensity);
    }

    @Override
    public float getMinRainIntensity() {
        return this.minIntensity;
    }

    @Override
    public void setMinRainIntensity(float intensity) {
        this.minIntensity = MathStuff.clamp(intensity, MIN_INTENSITY, this.maxIntensity);
    }

    @Override
    public float getMaxRainIntensity() {
        return this.maxIntensity;
    }

    @Override
    public void setMaxRainIntensity(float intensity) {
        this.maxIntensity = MathStuff.clamp(intensity, this.minIntensity, MAX_INTENSITY);
    }

    @Override
    public int getThunderTimer() {
        return this.thunderTimer;
    }

    @Override
    public void setThunderTimer(int time) {
        this.thunderTimer = MathStuff.clamp(time, 0, Integer.MAX_VALUE);
    }

    @Override
    public void randomizeRain() {
        final float result;
        final float delta = this.maxIntensity - this.minIntensity;
        if (delta <= 0.0F) {
            result = this.minIntensity;
        } else {
            final float mid = delta / 2.0F;
            result = this.minIntensity + this.random.nextFloat() * mid + this.random.nextFloat() * mid;
        }
        setRainIntensity(MathStuff.clamp(result, 0.01F, MAX_INTENSITY));
        setCurrentRainIntensity(0.0F);
    }

    @Override
    public CompoundTag serializeNBT() {
        final CompoundTag nbt = new CompoundTag();
        nbt.putFloat("intensity", getRainIntensity());
        nbt.putFloat("currentIntensity", getCurrentRainIntensity());
        nbt.putFloat("minIntensity", getMinRainIntensity());
        nbt.putFloat("maxIntensity", getMaxRainIntensity());
        nbt.putInt("thunderTimer", getThunderTimer());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        setRainIntensity(nbt.getFloat("intensity"));
        setCurrentRainIntensity(nbt.getFloat("currentIntensity"));
        setMinRainIntensity(nbt.getFloat("minIntensity"));
        setMaxRainIntensity(nbt.getFloat("maxIntensity"));
        setThunderTimer(nbt.getInt("thunderTimer"));
    }

    @Override
    public String configString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("dim ").append(getId()).append(": ");
        builder.append("rainIntensity [").append(FORMATTER.format(getMinRainIntensity() * 100));
        builder.append(",").append(FORMATTER.format(getMaxRainIntensity() * 100));
        builder.append("]");
        return builder.toString();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("dim ").append(getId()).append(": ");
        builder.append("rainIntensity: ").append(FORMATTER.format(getRainIntensity() * 100));
        builder.append('/').append(FORMATTER.format(getCurrentRainIntensity() * 100));
        builder.append(" [").append(FORMATTER.format(getMinRainIntensity() * 100));
        builder.append(",").append(FORMATTER.format(getMaxRainIntensity() * 100));
        builder.append("], thunderTimer: ").append(getThunderTimer());
        return builder.toString();
    }
}
