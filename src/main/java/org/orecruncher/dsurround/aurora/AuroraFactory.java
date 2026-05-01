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

package org.orecruncher.dsurround.aurora;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.dsurround.Client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Factory and manager for aurora effects.
 * Handles creation, lifecycle, and rendering of auroras.
 */
@OnlyIn(Dist.CLIENT)
public class AuroraFactory {

    private static final int MAX_AURORAS = 3;
    private static final int SPAWN_CHECK_INTERVAL = 200; // ticks

    private final List<IAurora> auroras = new ArrayList<>();
    private int tickCounter = 0;
    private boolean enabled = true;

    /**
     * Update all active auroras.
     * Called once per tick.
     */
    public void tick() {
        if (!this.enabled || !Client.Config.aurora.enableAuroras) {
            // Fade out existing auroras
            for (IAurora aurora : this.auroras) {
                aurora.setFading(true);
            }
        }

        // Update all auroras
        for (IAurora aurora : this.auroras) {
            aurora.update();
        }

        // Remove completed auroras
        Iterator<IAurora> iterator = this.auroras.iterator();
        while (iterator.hasNext()) {
            IAurora aurora = iterator.next();
            if (aurora.isComplete()) {
                iterator.remove();
            }
        }

        // Spawn new auroras
        this.tickCounter++;
        if (this.tickCounter >= SPAWN_CHECK_INTERVAL) {
            this.tickCounter = 0;
            trySpawnAurora();
        }
    }

    /**
     * Render all active auroras.
     *
     * @param poseStack The pose stack for transformations
     * @param partialTick The partial tick for smooth interpolation
     */
    public void render(PoseStack poseStack, float partialTick) {
        if (!this.enabled || !Client.Config.aurora.enableAuroras) {
            return;
        }

        for (IAurora aurora : this.auroras) {
            poseStack.pushPose();
            aurora.render(poseStack, partialTick);
            poseStack.popPose();
        }
    }

    /**
     * Try to spawn a new aurora if conditions are met.
     */
    private void trySpawnAurora() {
        if (!this.enabled || !Client.Config.aurora.enableAuroras) {
            return;
        }

        // Check if we can spawn more auroras
        if (this.auroras.size() >= MAX_AURORAS) {
            return;
        }

        // Check if dimension supports auroras
        if (!AuroraUtils.dimensionHasAuroras()) {
            return;
        }

        // TODO: Add weather and biome checks when those systems are available
        // For now, spawn with a random chance
        if (Math.random() < 0.3) { // 30% chance
            spawnAurora();
        }
    }

    /**
     * Spawn a new aurora.
     */
    private void spawnAurora() {
        long seed = AuroraUtils.getSeed();
        IAurora aurora = new AuroraClassic(seed);
        this.auroras.add(aurora);
    }

    /**
     * Enable or disable aurora spawning.
     *
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Check if aurora spawning is enabled.
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Get the number of active auroras.
     *
     * @return The number of active auroras
     */
    public int getActiveCount() {
        return this.auroras.size();
    }

    /**
     * Clear all auroras immediately.
     */
    public void clear() {
        this.auroras.clear();
    }
}
