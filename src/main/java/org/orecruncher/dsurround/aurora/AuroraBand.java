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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.dsurround.lib.random.IRandomizer;

/**
 * An aurora band, ported from the 1.12.2 AuroraBand.
 *
 * Geometry: a ribbon walking out from its centre in both directions, accumulating
 * small random heading changes so it meanders in the XZ plane. Each node has a
 * fixed curtain height (posY). The visual "wave" is a travelling cosine applied
 * to dZ (horizontal sway) each rendered frame, with only a tiny dY wobble.
 */
@OnlyIn(Dist.CLIENT)
public class AuroraBand {

    private static final int NODE_COUNT = 64;
    private static final float NODE_LENGTH = 30.0F;
    private static final float NODE_WIDTH = 2.0F;
    private static final int ALPHA_LIMIT = 96;

    private static final float ANGLE1 = (float) (Math.PI / 16.0D);
    private static final float ANGLE2 = (float) Math.toRadians(90.0D / 7.0D);
    private static final float AURORA_SPEED = 0.75F;
    private static final float AURORA_AMPLITUDE = 18.0F;

    private final IRandomizer random;
    private final Panel[] nodes;

    private float cycle = 0.0F;

    public AuroraBand(IRandomizer random) {
        this.random = random;
        this.nodes = new Panel[NODE_COUNT];
        for (int i = 0; i < NODE_COUNT; i++) {
            this.nodes[i] = new Panel();
        }
    }

    /** Build the ribbon once, then apply the initial frame of the wave. */
    public void generate() {
        populate();
        taper();
        translate(0.0F);
    }

    public Panel[] getNodes() {
        return this.nodes;
    }

    public int getAlphaLimit() {
        return ALPHA_LIMIT;
    }

    /** Advance the wave phase, once per tick. */
    public void update() {
        if ((this.cycle += AURORA_SPEED) >= 360.0F) {
            this.cycle -= 360.0F;
        }
    }

    /**
     * Recalculate the travelling wave and the tetrahedral edges for this frame.
     * Called from AuroraClassic.render() with the render partial tick, exactly as
     * the 1.12.2 original does.
     */
    public void translate(float partialTick) {
        final float c = this.cycle + AURORA_SPEED * partialTick;
        for (int i = 0; i < this.nodes.length; i++) {
            final float f = (float) Math.cos(Math.toRadians((i << 3) + c));
            final Panel node = this.nodes[i];
            node.setDeltaZ(f * AURORA_AMPLITUDE);
            node.setDeltaY(f * 3.0F);
            node.applyThickness();
        }
    }

    /** 1.12.2 AuroraBand.populate(): meander in the XZ plane from the centre out. */
    private void populate() {
        final int bound = NODE_COUNT / 2 - 1;

        float angleTotal = 0.0F;
        for (int i = NODE_COUNT / 8 / 2 - 1; i >= 0; i--) {
            float angle = (this.random.nextFloat() - 0.5F) * 8.0F;
            angleTotal += angle;
            if (Math.abs(angleTotal) > 180.0F) {
                angle = -angle;
                angleTotal += angle;
            }

            for (int k = 7; k >= 0; k--) {
                final int idx = i * 8 + k;
                if (idx == bound) {
                    final Panel node = new Panel();
                    node.setPos(0.0D, AURORA_AMPLITUDE, 0.0D);
                    node.angle = angle;
                    this.nodes[idx] = node;
                } else {
                    final float y;
                    if (i == 0) {
                        y = (float) (Math.sin(ANGLE1 * k) * 7.0D + this.random.nextFloat() / 2.0F);
                    } else {
                        y = 10.0F + this.random.nextFloat() * 5.0F;
                    }

                    final Panel next = this.nodes[idx + 1];
                    final float subAngle = (float) next.angle + angle;
                    final double rads = Math.toRadians(subAngle);
                    final double z = next.posZ - Math.sin(rads) * NODE_LENGTH;
                    final double x = next.posX - Math.cos(rads) * NODE_LENGTH;

                    final Panel node = new Panel();
                    node.setPos(x, y, z);
                    node.angle = subAngle;
                    this.nodes[idx] = node;
                }
            }
        }

        angleTotal = 0.0F;
        for (int j = NODE_COUNT / 8 / 2; j < NODE_COUNT / 8; j++) {
            float angle = (this.random.nextFloat() - 0.5F) * 8.0F;
            angleTotal += angle;
            if (Math.abs(angleTotal) > 180.0F) {
                angle = -angle;
                angleTotal += angle;
            }

            for (int h = 0; h < 8; h++) {
                final float y;
                if (j == NODE_COUNT / 8 - 1) {
                    y = (float) (Math.cos(ANGLE2 * h) * 7.0D + this.random.nextFloat() / 2.0F);
                } else {
                    y = 10.0F + this.random.nextFloat() * 5.0F;
                }

                final Panel prev = this.nodes[j * 8 + h - 1];
                final float subAngle = (float) prev.angle + angle;
                final double rads = Math.toRadians(subAngle);
                final double z = prev.posZ + Math.sin(rads) * NODE_LENGTH;
                final double x = prev.posX + Math.cos(rads) * NODE_LENGTH;

                final Panel node = new Panel();
                node.setPos(x, y, z);
                node.angle = subAngle;
                this.nodes[j * 8 + h] = node;
            }
        }
    }

    /** 1.12.2 generateBands(): taper the head and tail of the ribbon. */
    private void taper() {
        final float factor = (float) (Math.PI / (NODE_COUNT / 4.0D));
        final int lowerBound = NODE_COUNT / 8 + 1;
        final int upperBound = NODE_COUNT * 7 / 8 - 1;

        int count = 0;
        for (int i = 0; i < NODE_COUNT; i++) {
            final float width;
            if (i < lowerBound) {
                width = (float) Math.sin(factor * count++) * NODE_WIDTH;
            } else if (i > upperBound) {
                width = (float) Math.sin(factor * count--) * NODE_WIDTH;
            } else {
                width = NODE_WIDTH;
            }
            this.nodes[i].setWidth(width);
        }
    }
}
