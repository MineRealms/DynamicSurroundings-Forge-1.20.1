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

package org.orecruncher.dsurround.weather.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.dsurround.Constants;
import org.orecruncher.dsurround.weather.Weather;

/**
 * Event handler for weather rendering.
 * Hooks into Forge's render pipeline to replace vanilla weather with enhanced effects.
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WeatherRenderHandler {

    private static final StormRenderer stormRenderer = new StormRenderer();
    private static int renderTicks = 0;

    /**
     * Handles weather rendering during the level render stage.
     * Replaces vanilla weather rendering with Dynamic Surroundings effects.
     */
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // Only render during the WEATHER stage
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }

        // Check if we should override vanilla weather
        if (!Weather.notDoVanilla()) {
            return;
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        // Get render context
        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();
        float partialTick = event.getPartialTick();

        // Increment render ticks
        renderTicks++;

        // Render our custom weather
        poseStack.pushPose();
        try {
            stormRenderer.render(level, renderTicks, partialTick, camera, poseStack);
        } finally {
            poseStack.popPose();
        }
    }

    /**
     * Resets the render tick counter.
     * Called when the world changes or renderer is reset.
     */
    public static void reset() {
        renderTicks = 0;
    }
}
