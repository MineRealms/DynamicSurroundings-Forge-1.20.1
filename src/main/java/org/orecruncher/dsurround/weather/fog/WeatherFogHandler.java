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
package org.orecruncher.dsurround.weather.fog;

import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.orecruncher.dsurround.Configuration;
import org.orecruncher.dsurround.lib.di.ContainerManager;
import org.orecruncher.dsurround.weather.Weather;

/**
 * Handles weather-based fog effects by modifying fog density based on weather intensity.
 * The stronger the weather, the denser the fog becomes.
 */
public class WeatherFogHandler {

    private final Configuration config;

    public WeatherFogHandler() {
        this.config = ContainerManager.resolve(Configuration.class);
    }

    @SubscribeEvent
    public void onRenderFog(ViewportEvent.RenderFog event) {
        // Check if weather fog effects are enabled
        if (!this.config.weather.enableWeatherFog)
            return;

        // Get current weather intensity
        float rainStrength = Weather.getIntensityLevel();
        if (rainStrength <= 0)
            return;

        // Only apply fog in overworld-like dimensions
        Camera camera = event.getCamera();
        if (camera.getEntity() == null || !(camera.getEntity().level() instanceof ClientLevel))
            return;

        // Calculate fog scaling based on weather intensity and config
        float startReduction = (float) this.config.weather.fogStartReduction;
        float endReduction = (float) this.config.weather.fogEndReduction;

        float startScale = 1F - (startReduction * rainStrength);
        float endScale = 1F - (endReduction * rainStrength);

        // Get current fog distances
        float farPlaneDistance = event.getFarPlaneDistance();
        float nearPlaneDistance = event.getNearPlaneDistance();

        // Apply weather-based fog reduction
        float newNearPlane = nearPlaneDistance * startScale;
        float newFarPlane = farPlaneDistance * endScale;

        // Ensure near plane is always less than far plane
        if (newNearPlane < newFarPlane) {
            event.setNearPlaneDistance(newNearPlane);
            event.setFarPlaneDistance(newFarPlane);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        // Future: Could modify fog color based on weather type (e.g., darker during storms)
        // For now, we'll let vanilla handle fog color
    }
}
