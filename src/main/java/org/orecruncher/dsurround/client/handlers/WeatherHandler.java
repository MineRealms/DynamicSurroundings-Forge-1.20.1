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

package org.orecruncher.dsurround.client.handlers;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.dsurround.lib.Library;
import org.orecruncher.dsurround.lib.logging.IModLog;

import javax.annotation.Nonnull;

/**
 * Handles weather-related effects like thunder flashes.
 * Manages lightning bolt rendering and thunder sound coordination.
 */
@OnlyIn(Dist.CLIENT)
public final class WeatherHandler extends EffectHandlerBase {

    private static final IModLog LOGGER = Library.LOGGER;

    private int flashTimer = 0;

    public WeatherHandler() {
        super("Weather");
    }

    @Override
    public void onConnect() {
        this.flashTimer = 0;
    }

    @Override
    public void onDisconnect() {
        this.flashTimer = 0;
    }

    @Override
    public void process(@Nonnull Player player) {
        // Decrement flash timer
        if (this.flashTimer > 0) {
            this.flashTimer--;
            // TODO: Trigger lightning flash effect when timer > 0
            // This will be implemented when we have the rendering system ready
        }
    }

    /**
     * Trigger a lightning flash effect.
     * Called when thunder sounds play.
     */
    public void triggerFlash() {
        this.flashTimer = 2; // Flash for 2 ticks
    }

    /**
     * Check if a flash is currently active.
     */
    public boolean isFlashing() {
        return this.flashTimer > 0;
    }
}
