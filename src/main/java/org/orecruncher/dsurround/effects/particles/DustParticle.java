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
 * Dust particle for desert dust storms.
 * Drifts horizontally with wind and slowly falls.
 */
public class DustParticle extends TextureSheetParticle {

    private static final float DUST_COLOR_R = 0.8F;
    private static final float DUST_COLOR_G = 0.7F;
    private static final float DUST_COLOR_B = 0.5F;
    private static final float WIND_STRENGTH = 0.3F;
    private static final float FALL_SPEED = 0.01F;

    private final float windX;
    private final float windZ;

    public DustParticle(ClientLevel level, double x, double y, double z, double windX, double windZ) {
        super(level, x, y, z, 0.0, 0.0, 0.0);

        var random = Randomizer.current();

        // Setup lifetime (60-100 ticks = 3-5 seconds)
        this.lifetime = 60 + random.nextInt(40);

        // Setup size (small dust particles)
        this.quadSize = 0.1F + random.nextFloat() * 0.2F;

        // Setup color (sandy/dusty color)
        this.rCol = DUST_COLOR_R + random.nextFloat() * 0.1F;
        this.gCol = DUST_COLOR_G + random.nextFloat() * 0.1F;
        this.bCol = DUST_COLOR_B + random.nextFloat() * 0.1F;
        this.alpha = 0.6F + random.nextFloat() * 0.2F;

        // Setup wind direction
        this.windX = (float) windX * WIND_STRENGTH;
        this.windZ = (float) windZ * WIND_STRENGTH;

        // Initial motion - mostly horizontal with slight randomness
        this.xd = this.windX + (random.nextDouble() - 0.5D) * 0.1D;
        this.yd = -FALL_SPEED + (random.nextDouble() - 0.5D) * 0.02D;
        this.zd = this.windZ + (random.nextDouble() - 0.5D) * 0.1D;

        // No gravity (dust floats)
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

        // Apply wind drift
        this.xd = this.windX + (Randomizer.current().nextDouble() - 0.5D) * 0.05D;
        this.zd = this.windZ + (Randomizer.current().nextDouble() - 0.5D) * 0.05D;

        // Slow fall
        this.yd = -FALL_SPEED + (Randomizer.current().nextDouble() - 0.5D) * 0.01D;

        // Move particle
        this.move(this.xd, this.yd, this.zd);

        // Fade out in last 20 ticks
        if (this.age > this.lifetime - 20) {
            float fadeProgress = (float) (this.lifetime - this.age) / 20.0F;
            this.alpha = (0.6F + Randomizer.current().nextFloat() * 0.2F) * fadeProgress;
        }

        // Remove if collided with ground
        if (this.onGround) {
            this.remove();
        }
    }

    @Override
    public int getLightColor(float partialTick) {
        // Dust particles should be affected by ambient light
        int blockLight = 15; // Full brightness
        int skyLight = 15;
        return blockLight | (skyLight << 20);
    }
}
