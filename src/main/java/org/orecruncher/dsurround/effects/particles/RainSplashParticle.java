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
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.orecruncher.dsurround.lib.gui.ColorPalette;
import org.orecruncher.dsurround.lib.random.Randomizer;

/**
 * Rain splash particle that appears when rain hits a surface.
 * Creates a small water spray effect with random motion.
 */
public class RainSplashParticle extends TextureSheetParticle {

    private static final float MOTION_SCALE = 0.4F;
    private static final float MOTION_Y_BASE = 0.1F;
    private static final float MOTION_Y_RANDOM = 0.2F;
    private static final float SIZE_SCALE = 0.07F;

    private final float scale;
    private final float texU1, texU2;
    private final float texV1, texV2;

    public RainSplashParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z, 0.0, 0.0, 0.0);

        var random = Randomizer.current();

        // Setup lifetime (8-10 ticks)
        this.lifetime = (int) (8.0F / (random.nextFloat() * 0.8F + 0.2F));
        this.scale = (random.nextFloat() * 0.5F + 0.5F) * 2.0F;

        // Setup motion - random spray direction
        this.xd = (random.nextDouble() * 2.0D - 1.0D) * MOTION_SCALE;
        this.yd = random.nextDouble() * MOTION_Y_RANDOM + MOTION_Y_BASE;
        this.zd = (random.nextDouble() * 2.0D - 1.0D) * MOTION_SCALE;

        // Normalize and scale motion
        double motionLength = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
        if (motionLength > 0) {
            double scale = (random.nextDouble() + random.nextDouble() + 1.0D) * 0.15F;
            this.xd = this.xd / motionLength * scale * MOTION_SCALE * 0.3D;
            this.yd = this.yd / motionLength * scale * MOTION_SCALE + MOTION_Y_BASE;
            this.zd = this.zd / motionLength * scale * MOTION_SCALE * 0.3D;
        }

        // Setup texture coordinates (4 possible textures in a 2x2 grid)
        int textureIdx = random.nextInt(4);
        int texX = textureIdx % 2;
        int texY = textureIdx / 2;
        this.texU1 = texX * 0.5F;
        this.texU2 = this.texU1 + 0.5F;
        this.texV1 = texY * 0.5F;
        this.texV2 = this.texV1 + 0.5F;

        // Setup size
        this.quadSize = SIZE_SCALE * this.scale;

        // Setup color based on biome water color
        var position = BlockPos.containing(x, y, z);
        var colorRgb = this.level.getBiome(position).value().getWaterColor();
        this.rCol = ColorPalette.getRed(colorRgb) / 255F;
        this.gCol = ColorPalette.getGreen(colorRgb) / 255F;
        this.bCol = ColorPalette.getBlue(colorRgb) / 255F;
        this.alpha = 1.0F;

        // Apply gravity
        this.gravity = 0.06F;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected float getU0() {
        return this.texU1;
    }

    @Override
    protected float getU1() {
        return this.texU2;
    }

    @Override
    protected float getV0() {
        return this.texV1;
    }

    @Override
    protected float getV1() {
        return this.texV2;
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

        // Apply motion
        this.yd -= 0.04D * this.gravity;
        this.move(this.xd, this.yd, this.zd);

        // Apply drag
        this.xd *= 0.98D;
        this.yd *= 0.98D;
        this.zd *= 0.98D;

        // Fade out over lifetime
        this.alpha = 1.0F - ((float) this.age / (float) this.lifetime);

        // Stop if on ground
        if (this.onGround) {
            this.xd *= 0.7D;
            this.zd *= 0.7D;
        }
    }
}
