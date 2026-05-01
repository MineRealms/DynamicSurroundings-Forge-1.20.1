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
package org.orecruncher.dsurround.effects.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import org.jetbrains.annotations.NotNull;
import org.orecruncher.dsurround.lib.random.Randomizer;

/**
 * Snowflake particle that drifts down slowly with wind effects.
 * More realistic than vanilla snow particles.
 */
public class SnowParticle extends TextureSheetParticle {

    private static final float FALL_SPEED = 0.05F;
    private static final float DRIFT_STRENGTH = 0.1F;

    private final float driftPhase;
    private final float driftSpeed;

    public SnowParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z, 0.0, 0.0, 0.0);

        var random = Randomizer.current();

        // Setup lifetime (100-200 ticks = 5-10 seconds)
        this.lifetime = 100 + random.nextInt(100);

        // Setup size (small snowflakes)
        this.quadSize = 0.1F + random.nextFloat() * 0.15F;

        // Setup color (white with slight variation)
        this.rCol = 0.9F + random.nextFloat() * 0.1F;
        this.gCol = 0.9F + random.nextFloat() * 0.1F;
        this.bCol = 1.0F;
        this.alpha = 0.8F + random.nextFloat() * 0.2F;

        // Setup drift pattern (sinusoidal motion)
        this.driftPhase = random.nextFloat() * (float) Math.PI * 2.0F;
        this.driftSpeed = 0.02F + random.nextFloat() * 0.03F;

        // Initial motion - slow fall
        this.xd = (random.nextDouble() - 0.5D) * 0.05D;
        this.yd = -FALL_SPEED - random.nextDouble() * 0.02D;
        this.zd = (random.nextDouble() - 0.5D) * 0.05D;

        // No gravity (we control fall speed manually)
        this.gravity = 0.0F;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Calculate drift motion (sinusoidal pattern)
        float driftAngle = this.driftPhase + (this.age * this.driftSpeed);
        this.xd = Math.sin(driftAngle) * DRIFT_STRENGTH;
        this.zd = Math.cos(driftAngle) * DRIFT_STRENGTH;

        // Maintain slow fall
        this.yd = -FALL_SPEED - Randomizer.current().nextDouble() * 0.01D;

        // Move particle
        this.move(this.xd, this.yd, this.zd);

        // Fade out in last 40 ticks
        if (this.age > this.lifetime - 40) {
            float fadeProgress = (float) (this.lifetime - this.age) / 40.0F;
            this.alpha = (0.8F + Randomizer.current().nextFloat() * 0.2F) * fadeProgress;
        }

        // Remove if collided with ground
        if (this.onGround) {
            this.remove();
        }
    }

    @Override
    public int getLightColor(float partialTick) {
        // Snow particles should be bright
        int blockLight = 15;
        int skyLight = 15;
        return blockLight | (skyLight << 20);
    }
}
