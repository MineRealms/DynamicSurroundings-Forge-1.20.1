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

import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.dsurround.lib.random.IRandomizer;

/**
 * Represents a band of aurora panels that form a wave-like structure.
 */
@OnlyIn(Dist.CLIENT)
public class AuroraBand {

    private static final int NODE_COUNT = 64;
    private static final int NODE_LENGTH = 64;
    private static final float WAVE_AMPLITUDE = 10.0F;
    private static final float WAVE_FREQUENCY = 0.1F;

    private final IRandomizer random;
    private final Panel[] nodes;
    private final float[] waveTracker;

    private float offset = 0.0F;

    public AuroraBand(IRandomizer random) {
        this.random = random;
        this.nodes = new Panel[NODE_COUNT];
        this.waveTracker = new float[NODE_COUNT];

        for (int i = 0; i < NODE_COUNT; i++) {
            this.nodes[i] = new Panel();
            this.waveTracker[i] = this.random.nextFloat() * Mth.TWO_PI;
        }
    }

    /**
     * Generate the initial geometry for the band.
     */
    public void generate() {
        for (int i = 0; i < NODE_COUNT; i++) {
            Panel node = this.nodes[i];

            // Base position along the band
            double posX = i * NODE_LENGTH;
            double posZ = 0.0;

            // Wave motion
            float wave = Mth.sin(this.waveTracker[i]) * WAVE_AMPLITUDE;
            double posY = 1.0 + wave;

            // Tetrahedral coordinates for thickness
            double width = 5.0 + this.random.nextFloat() * 3.0;
            double tetX = posX;
            double tetZ = posZ - width;
            double tetX2 = posX;
            double tetZ2 = posZ + width;

            node.setPos(posX, posY, posZ);
            node.setTet(tetX, posY, tetZ);
            node.setTet2(tetX2, posY, tetZ2);
        }
    }

    /**
     * Update the band animation.
     * Called once per tick.
     */
    public void update() {
        this.offset += 0.01F;

        for (int i = 0; i < NODE_COUNT; i++) {
            this.waveTracker[i] += WAVE_FREQUENCY;

            Panel node = this.nodes[i];
            float wave = Mth.sin(this.waveTracker[i]) * WAVE_AMPLITUDE;
            node.posY = 1.0 + wave;
            node.tetY = node.posY;
            node.tetY2 = node.posY;
        }
    }

    /**
     * Get the array of nodes in this band.
     *
     * @return The node array
     */
    public Panel[] getNodes() {
        return this.nodes;
    }
}
