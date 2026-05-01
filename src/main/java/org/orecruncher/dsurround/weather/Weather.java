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

package org.orecruncher.dsurround.weather;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.dsurround.Client;
import org.orecruncher.dsurround.weather.thunder.ThunderManager;
import org.orecruncher.dsurround.weather.tracker.ServerDrivenTracker;
import org.orecruncher.dsurround.weather.tracker.SimulationTracker;
import org.orecruncher.dsurround.weather.tracker.Tracker;

/**
 * Central weather state manager for Dynamic Surroundings.
 * Manages weather intensity levels and provides access to weather properties.
 */
@OnlyIn(Dist.CLIENT)
public class Weather {

    // Vanilla rain sound as fallback
    private static final ResourceLocation VANILLA_RAIN_SOUND = new ResourceLocation("minecraft", "weather.rain");

    /**
     * Weather intensity levels with associated textures and properties.
     */
    public enum Properties {
        //@formatter:off
        VANILLA(-10.0F, null),
        NONE(0.0F, "calm"),
        CALM(0.125F, "calm"),
        LIGHT(0.25F, "light"),
        GENTLE(0.365F, "gentle"),
        MODERATE(0.5F, "moderate"),
        HEAVY(0.625F, "heavy"),
        STRONG(0.75F, "strong"),
        INTENSE(0.875F, "intense"),
        TORRENTIAL(1.0F, "torrential");
        //@formatter:on

        private static final float MIN_INTENSITY = 0.0F;
        private static final float MAX_INTENSITY = 1.0F;

        private final float level;
        private final ResourceLocation rainTexture;
        private final ResourceLocation snowTexture;
        private final ResourceLocation dustTexture;

        Properties(final float level, final String intensity) {
            this.level = level;
            if (intensity == null) {
                // VANILLA uses default Minecraft textures
                this.rainTexture = new ResourceLocation("textures/environment/rain.png");
                this.snowTexture = new ResourceLocation("textures/environment/snow.png");
                this.dustTexture = new ResourceLocation("dsurround", "textures/environment/dust_calm.png");
            } else {
                this.rainTexture = new ResourceLocation("dsurround",
                        String.format("textures/environment/rain_%s.png", intensity));
                this.snowTexture = new ResourceLocation("dsurround",
                        String.format("textures/environment/snow_%s.png", intensity));
                this.dustTexture = new ResourceLocation("dsurround",
                        String.format("textures/environment/dust_%s.png", intensity));
            }
        }

        public float getLevel() {
            return this.level;
        }

        public ResourceLocation getRainTexture() {
            return this.rainTexture;
        }

        public ResourceLocation getDustTexture() {
            return this.dustTexture;
        }

        public ResourceLocation getSnowTexture() {
            return this.snowTexture;
        }

        public SoundEvent getStormSound() {
            // TODO: Return custom storm sound based on configuration
            return SoundEvent.createVariableRangeEvent(VANILLA_RAIN_SOUND);
        }

        public SoundEvent getDustSound() {
            // TODO: Return custom dust sound
            return SoundEvent.createVariableRangeEvent(new ResourceLocation("dsurround", "dust"));
        }

        /**
         * Maps a rain strength value to the appropriate weather property.
         *
         * @param str Rain strength (0.0 to 1.0)
         * @return Corresponding weather property
         */
        public static Properties mapRainStrength(float str) {
            // If the level is Vanilla it means that the rainfall in the dimension
            // is to be that of Vanilla.
            if (str == Properties.VANILLA.getLevel()) {
                return Properties.VANILLA;
            }

            str = Mth.clamp(str, MIN_INTENSITY, MAX_INTENSITY);
            Properties result = Properties.NONE;

            for (Properties p : Properties.values()) {
                if (str <= p.getLevel()) {
                    result = p;
                    break;
                }
            }

            return result;
        }
    }

    // Start with the simulation tracker (client-side prediction)
    private static Tracker tracker = new SimulationTracker();

    // Thunder manager for handling thunder effects
    private static ThunderManager thunderManager = new ThunderManager();

    private static Level getWorld() {
        return Minecraft.getInstance().level;
    }

    public static boolean isRaining() {
        return tracker.isRaining();
    }

    public static boolean isThundering() {
        return tracker.isThundering();
    }

    public static Properties getWeatherProperties() {
        return tracker.getWeatherProperties();
    }

    public static float getIntensityLevel() {
        return tracker.getIntensityLevel();
    }

    public static float getMaxIntensityLevel() {
        return tracker.getMaxIntensityLevel();
    }

    public static int getNextRainChange() {
        return tracker.getNextRainChange();
    }

    public static float getThunderStrength() {
        return tracker.getThunderStrength();
    }

    public static int getNextThunderChange() {
        return tracker.getNextThunderChange();
    }

    public static int getNextThunderEvent() {
        return tracker.getNextThunderEvent();
    }

    public static float getCurrentVolume() {
        return tracker.getCurrentVolume();
    }

    public static SoundEvent getCurrentStormSound() {
        return tracker.getCurrentStormSound();
    }

    public static boolean notDoVanilla() {
        return !tracker.doVanilla();
    }

    public static void update() {
        tracker.update();
        thunderManager.tick();
    }

    /**
     * Registers the weather tracker based on server availability.
     * If server is available, uses server-driven tracker for synchronization.
     * Otherwise, uses client-side simulation.
     *
     * @param serverAvailable Whether the server has Dynamic Surroundings installed
     */
    public static void register(final boolean serverAvailable) {
        if (serverAvailable) {
            tracker = new ServerDrivenTracker();
        } else {
            tracker = new SimulationTracker();
        }
    }

    /**
     * Unregisters the weather tracker and reverts to simulation mode.
     */
    public static void unregister() {
        tracker = new SimulationTracker();
        thunderManager.reset();
    }

    public static String diagnostic() {
        return tracker.toString();
    }
}
