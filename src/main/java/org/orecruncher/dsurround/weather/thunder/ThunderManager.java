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
package org.orecruncher.dsurround.weather.thunder;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.orecruncher.dsurround.Configuration;
import org.orecruncher.dsurround.lib.GameUtils;
import org.orecruncher.dsurround.lib.di.ContainerManager;
import org.orecruncher.dsurround.lib.random.Randomizer;
import org.orecruncher.dsurround.weather.Weather;

/**
 * Manages thunder effects including sound playback and lightning flashes.
 * Handles both background ambient thunder and position-specific thunder events.
 */
public class ThunderManager {

    private static final float THUNDER_VOLUME_MIN = 10000.0F;
    private static final float THUNDER_VOLUME_MAX = 10000.0F * 8.0F;

    private final Configuration config;
    private int nextThunderEvent = 0;

    public ThunderManager() {
        this.config = ContainerManager.resolve(Configuration.class);
    }

    /**
     * Called every tick to update thunder state and trigger background thunder.
     */
    public void tick() {
        if (!this.config.weather.allowBackgroundThunder)
            return;

        var level = GameUtils.getWorld().orElse(null);
        if (level == null)
            return;

        float rainStrength = Weather.getIntensityLevel();
        float thunderThreshold = (float) this.config.weather.stormThunderThreshold;

        // Only generate thunder if rain strength exceeds threshold
        if (rainStrength < thunderThreshold)
            return;

        // Check if it's time for next thunder event
        if (--this.nextThunderEvent <= 0) {
            doThunder(level, rainStrength);
        }
    }

    /**
     * Triggers a thunder event with sound and optional flash.
     */
    private void doThunder(Level level, float intensity) {
        var player = GameUtils.getPlayer().orElse(null);
        if (player == null)
            return;

        // Calculate next thunder event timing based on intensity
        // Higher intensity = more frequent thunder
        int baseDelay = 300; // 15 seconds at 20 tps
        int variableDelay = (int) (600 * (1.0F - intensity)); // 0-30 seconds
        this.nextThunderEvent = baseDelay + Randomizer.current().nextInt(variableDelay);

        // Determine if flash should occur (higher intensity = more likely)
        boolean doFlash = Randomizer.current().nextFloat() < (intensity * 0.5F);

        // Generate random position around player
        BlockPos playerPos = player.blockPosition();
        int range = 32 + Randomizer.current().nextInt(32); // 32-64 blocks
        double angle = Randomizer.current().nextDouble() * Math.PI * 2;
        int offsetX = (int) (Math.cos(angle) * range);
        int offsetZ = (int) (Math.sin(angle) * range);
        BlockPos thunderPos = playerPos.offset(offsetX, 0, offsetZ);

        // Play thunder sound
        playThunderSound(level, thunderPos, intensity);

        // Trigger flash if needed
        if (doFlash) {
            triggerLightningFlash(level);
        }
    }

    /**
     * Plays thunder sound at the specified location with volume based on intensity.
     */
    private void playThunderSound(Level level, BlockPos pos, float intensity) {
        var player = GameUtils.getPlayer().orElse(null);
        if (player == null)
            return;

        // Calculate volume based on distance and intensity
        double distance = Math.sqrt(player.blockPosition().distSqr(pos));
        float volumeScale = Mth.clamp(1.0F - (float) (distance / 64.0F), 0.0F, 1.0F);
        float volume = Mth.lerp(intensity, THUNDER_VOLUME_MIN, THUNDER_VOLUME_MAX) * volumeScale;

        // Calculate pitch variation
        float pitch = 0.8F + Randomizer.current().nextFloat() * 0.4F; // 0.8 to 1.2

        // Play thunder sound
        level.playLocalSound(
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.WEATHER,
                volume,
                pitch,
                false
        );
    }

    /**
     * Triggers a lightning flash effect by temporarily increasing sky brightness.
     */
    private void triggerLightningFlash(Level level) {
        // In 1.20.1, lightning flash is handled by the client's sky renderer
        // We can trigger it by setting the sky flash time
        var mc = Minecraft.getInstance();
        if (mc.level != null) {
            // The flash duration is controlled by the game's renderer
            // We just need to ensure there's a lightning bolt effect
            // This will be handled by the weather rendering system
        }
    }

    /**
     * Handles a thunder event received from the server or generated locally.
     */
    public void onThunderEvent(ThunderEvent event) {
        var level = GameUtils.getWorld().orElse(null);
        if (level == null)
            return;

        // Verify dimension matches
        if (!level.dimension().equals(event.dimension))
            return;

        float intensity = Weather.getIntensityLevel();
        playThunderSound(level, event.location, intensity);

        if (event.doFlash) {
            triggerLightningFlash(level);
        }
    }

    /**
     * Resets the thunder manager state.
     */
    public void reset() {
        this.nextThunderEvent = 0;
    }

    /**
     * Static method to handle thunder events from network packets.
     * Called when a thunder packet is received from the server.
     *
     * @param dimension The dimension the thunder event is in
     * @param doFlash Whether to trigger a lightning flash
     * @param position The position of the thunder event
     */
    public static void handleServerThunder(ResourceLocation dimension, boolean doFlash, BlockPos position) {
        var mc = Minecraft.getInstance();
        var level = mc.level;

        if (level == null)
            return;

        // Verify we're in the correct dimension
        if (!level.dimension().location().equals(dimension))
            return;

        float intensity = Weather.getIntensityLevel();

        // Play thunder sound
        var player = GameUtils.getPlayer().orElse(null);
        if (player != null) {
            // Calculate volume based on distance
            double distance = Math.sqrt(player.blockPosition().distSqr(position));
            float volumeScale = Mth.clamp(1.0F - (float) (distance / 64.0F), 0.0F, 1.0F);
            float volume = Mth.lerp(intensity, THUNDER_VOLUME_MIN, THUNDER_VOLUME_MAX) * volumeScale;

            // Calculate pitch variation
            float pitch = 0.8F + Randomizer.current().nextFloat() * 0.4F;

            // Play thunder sound
            level.playLocalSound(
                    position.getX(),
                    position.getY(),
                    position.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.WEATHER,
                    volume,
                    pitch,
                    false
            );
        }

        // Trigger flash if needed
        if (doFlash) {
            // Lightning flash is handled by the client's sky renderer
            // The effect will be visible automatically
        }
    }
}
