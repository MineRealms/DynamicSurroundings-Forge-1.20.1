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

package org.orecruncher.dsurround.weather.server;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.orecruncher.dsurround.lib.logging.IModLog;
import org.orecruncher.dsurround.lib.logging.ModLog;

import java.util.List;

/**
 * Server-side weather generator for Dynamic Surroundings.
 * Manages weather intensity and thunder events for multiplayer synchronization.
 */
public class WeatherGenerator {

    private static final IModLog LOGGER = new ModLog("WeatherGenerator");

    protected final RandomSource random;
    protected final ServerLevel level;
    protected final WeatherData data;

    public WeatherGenerator(ServerLevel level) {
        this.level = level;
        this.random = RandomSource.create();
        this.data = new WeatherData();
    }

    /**
     * Gets the name of this weather generator type.
     */
    public String name() {
        return "STANDARD";
    }

    /**
     * Calculates the next thunder event time based on rain intensity.
     *
     * @param rainIntensity Current rain intensity (0.0 to 1.0)
     * @return Ticks until next thunder event
     */
    protected int nextThunderEvent(float rainIntensity) {
        float scale = 2.0F - rainIntensity;
        return random.nextInt((int) (450 * scale)) + 300;
    }

    /**
     * Determines if a lightning flash should occur.
     *
     * @param rainIntensity Current rain intensity
     * @return true if flash should occur
     */
    protected boolean shouldFlash(float rainIntensity) {
        int threshold = (int) (rainIntensity * 100.0F);
        return random.nextInt(150) <= threshold;
    }

    /**
     * Gets the server level data.
     */
    protected ServerLevelData getLevelData() {
        return (ServerLevelData) level.getLevelData();
    }

    /**
     * Pre-processing hook for subclasses.
     */
    protected void preProcess() {
        // Hook for subclasses
    }

    /**
     * Processes rain intensity updates.
     */
    protected void processRain() {
        ServerLevelData levelData = getLevelData();

        if (levelData.isRaining()) {
            // If intensity is 0, we need to establish a new strength
            if (data.getRainIntensity() == 0.0F) {
                data.randomizeRain(random);
                LOGGER.debug("Rain intensity set to %f, duration %d ticks",
                        data.getRainIntensity(), levelData.getRainTime());
            }
            data.setCurrentRainIntensity(level.getRainLevel(1.0F));
        } else {
            float currentStrength = level.getRainLevel(1.0F);

            if (currentStrength > 0.0F) {
                // Rain is fading out
                data.setCurrentRainIntensity(currentStrength);
            } else if (data.getCurrentRainIntensity() > 0) {
                // Rain has stopped
                data.setRainIntensity(0);
                data.setCurrentRainIntensity(0);
                LOGGER.debug("Rain has stopped, next rain in %d ticks", levelData.getRainTime());
            } else if (data.getRainIntensity() > 0) {
                data.setRainIntensity(0);
            }
        }
    }

    /**
     * Processes ambient thunder effects.
     */
    protected void processAmbientThunder() {
        ServerLevelData levelData = getLevelData();
        float intensity = data.getCurrentRainIntensity();

        // Thunder threshold - only generate thunder when rain is strong enough
        float thunderThreshold = 0.75F;

        if (levelData.isThundering() && intensity >= thunderThreshold) {
            int timer = data.getThunderTimer() - 1;

            if (timer <= 0) {
                if (timer == 0) {
                    // Generate thunder event at a random player's location
                    List<ServerPlayer> players = level.players();
                    if (!players.isEmpty()) {
                        ServerPlayer randomPlayer = players.get(random.nextInt(players.size()));
                        float skyHeight = level.getHeight();
                        BlockPos thunderPos = new BlockPos(
                                (int) randomPlayer.getX(),
                                (int) skyHeight,
                                (int) randomPlayer.getZ()
                        );

                        boolean flash = shouldFlash(intensity);
                        data.setLastThunderPos(thunderPos);
                        data.setLastThunderFlash(flash);

                        LOGGER.debug("Thunder event at %s, flash=%b", thunderPos, flash);
                    }
                }
                timer = nextThunderEvent(intensity);
            }

            data.setThunderTimer(timer);
        } else {
            // Clear thunder timer when not thundering
            data.setThunderTimer(0);
        }
    }

    /**
     * Post-processing hook for subclasses.
     */
    protected void postProcess() {
        // Hook for subclasses
    }

    /**
     * Main update method called each tick.
     */
    public final void update() {
        process();
    }

    /**
     * Processes all weather updates.
     */
    protected void process() {
        preProcess();
        processRain();
        processAmbientThunder();
        postProcess();
    }

    /**
     * Gets the current weather data.
     */
    public WeatherData getData() {
        return data;
    }

    /**
     * Container for weather state data.
     */
    public static class WeatherData {
        private float rainIntensity = 0.0F;
        private float currentRainIntensity = 0.0F;
        private int thunderTimer = 0;
        private BlockPos lastThunderPos = BlockPos.ZERO;
        private boolean lastThunderFlash = false;

        public float getRainIntensity() {
            return rainIntensity;
        }

        public void setRainIntensity(float intensity) {
            this.rainIntensity = intensity;
        }

        public float getCurrentRainIntensity() {
            return currentRainIntensity;
        }

        public void setCurrentRainIntensity(float intensity) {
            this.currentRainIntensity = intensity;
        }

        public int getThunderTimer() {
            return thunderTimer;
        }

        public void setThunderTimer(int timer) {
            this.thunderTimer = timer;
        }

        public BlockPos getLastThunderPos() {
            return lastThunderPos;
        }

        public void setLastThunderPos(BlockPos pos) {
            this.lastThunderPos = pos;
        }

        public boolean isLastThunderFlash() {
            return lastThunderFlash;
        }

        public void setLastThunderFlash(boolean flash) {
            this.lastThunderFlash = flash;
        }

        /**
         * Randomizes rain intensity for a new storm.
         */
        public void randomizeRain(RandomSource random) {
            // Generate intensity between 0.5 and 1.0 for varied storms
            this.rainIntensity = 0.5F + random.nextFloat() * 0.5F;
        }
    }
}
