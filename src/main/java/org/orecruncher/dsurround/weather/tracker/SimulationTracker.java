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
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import org.orecruncher.dsurround.Client;
import org.orecruncher.dsurround.weather.Weather;

import java.util.Random;

/**
 * Client-side weather simulation tracker.
 * Generates weather intensity based on vanilla rain strength and simulates thunder events.
 */
@OnlyIn(Dist.CLIENT)
public class SimulationTracker extends Tracker {

    private static final float MIN_INTENSITY = 0.0F;
    private static final float MAX_INTENSITY = 1.0F;

    protected float intensityLevel = 0.0F;
    protected float maxIntensityLevel = 0.0F;
    protected int nextThunderEvent = 0;
    protected Weather.Properties intensity = Weather.Properties.NONE;
    protected Random random;

    @Override
    protected String type() {
        return "SIMULATION";
    }

    @Override
    public Weather.Properties getWeatherProperties() {
        return this.intensity;
    }

    @Override
    public float getIntensityLevel() {
        return this.intensityLevel;
    }

    @Override
    public float getMaxIntensityLevel() {
        return this.maxIntensityLevel;
    }

    @Override
    public int getNextThunderEvent() {
        return this.nextThunderEvent;
    }

    @Override
    public boolean doVanilla() {
        return false;
    }

    @Override
    public boolean backgroundThunderPossible() {
        if (!Client.Config.weather.allowBackgroundThunder)
            return false;

        Level world = getWorld();
        if (world == null)
            return false;

        // No thunder in Nether or End
        int dimId = world.dimension().location().hashCode();
        return dimId != -1 && dimId != 1 && isThundering()
                && getIntensityLevel() >= Client.Config.weather.stormThunderThreshold;
    }

    @Override
    public void update() {
        Level world = getWorld();
        if (world != null && hasWeather(world)) {
            updateRainState();
            doAmbientThunder();
        } else {
            this.intensity = Weather.Properties.NONE;
            this.intensityLevel = 0F;
            this.maxIntensityLevel = 0F;
            this.nextThunderEvent = 0;
        }
    }

    private boolean hasWeather(Level world) {
        // Check if dimension supports weather (no weather in Nether/End)
        return world.dimensionType().hasSkyLight();
    }

    private void updateRainState() {
        final float vanillaIntensity = super.getIntensityLevel();

        if (vanillaIntensity > 0 && this.intensityLevel == 0F) {
            // Starting to rain. Generate a max intensity to be used in the simulation.
            // Use the current MC day as a seed to get some predictability across
            // clients on the server.
            this.random = new Random(generateSeed());

            final float result;
            final float delta = (float)(Client.Config.weather.defaultMaxRainStrength - Client.Config.weather.defaultMinRainStrength);
            if (delta <= 0.0F) {
                result = (float)Client.Config.weather.defaultMinRainStrength;
            } else {
                final float mid = delta / 2.0F;
                result = (float)Client.Config.weather.defaultMinRainStrength
                        + (this.random.nextFloat() + this.random.nextFloat()) * mid;
            }

            this.maxIntensityLevel = Mth.clamp(result, 0.01F, MAX_INTENSITY);

        } else if (vanillaIntensity == 0F && this.intensityLevel > 0F) {
            // Stopped raining
            this.maxIntensityLevel = 0F;
            this.nextThunderEvent = 0;
            this.random = null;
        }

        final float newIntensity = Mth.clamp(vanillaIntensity, 0.0F, this.maxIntensityLevel);
        setCurrentIntensity(newIntensity);
    }

    /**
     * Sets the rain intensity based on the intensity level provided.
     * This is called by the packet handler when the server wants to set the rain intensity
     * level on the client.
     */
    protected void setCurrentIntensity(final float level) {
        this.intensity = Weather.Properties.mapRainStrength(level);

        if (this.intensity == Weather.Properties.VANILLA)
            this.intensityLevel = 0F;
        else
            this.intensityLevel = Mth.clamp(level, MIN_INTENSITY, MAX_INTENSITY);
    }

    private static long generateSeed() {
        Level world = Minecraft.getInstance().level;
        if (world == null)
            return System.currentTimeMillis();

        // Use day time as seed for predictability
        long dayTime = world.getDayTime();
        long day = dayTime / 24000L;
        return day;
    }

    // Leveraged from WeatherGenerator
    private void doAmbientThunder() {
        // If it is thundering and the intensity exceeds our threshold...
        if (backgroundThunderPossible()) {
            int time = this.nextThunderEvent - 1;
            if (time <= 0) {
                final float intensity = getIntensityLevel();
                if (time == 0) {
                    final Player player = getRandomPlayer();
                    if (player != null) {
                        // TODO: Get proper sky height from dimension info
                        final float theY = 256.0F;
                        final BlockPos pos = new BlockPos((int)player.getX(), (int)theY, (int)player.getZ());
                        // TODO: Post ThunderEvent when event system is implemented
                        // MinecraftForge.EVENT_BUS.post(new ThunderEvent(player.level().dimension(), doFlash(intensity), pos));
                    }
                }

                // set new time
                time = nextThunderEvent(intensity);
            }
            this.nextThunderEvent = time;

        } else {
            // Clear out the timer data for the next storm
            this.nextThunderEvent = 0;
        }
    }

    private int nextThunderEvent(final float rainIntensity) {
        final float scale = 2.0F - rainIntensity;
        return this.random.nextInt((int) (450 * scale)) + 300;
    }

    protected boolean doFlash(final float rainIntensity) {
        final int randee = (int) (rainIntensity * 100.0F);
        return this.random.nextInt(150) <= randee;
    }

    private Player getRandomPlayer() {
        Level world = getWorld();
        if (world == null || world.players().isEmpty())
            return null;

        return world.players().get(this.random.nextInt(world.players().size()));
    }
}
