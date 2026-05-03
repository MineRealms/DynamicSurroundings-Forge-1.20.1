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

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.orecruncher.dsurround.footsteps.FootprintStyle;
import org.orecruncher.dsurround.lib.GameUtils;
import org.orecruncher.dsurround.lib.math.MathStuff;

/**
 * Footprint particle that renders on the ground surface.
 * Ported from 1.12.2 MoteFootprint with adaptations for 1.20.1 particle system.
 */
@OnlyIn(Dist.CLIENT)
public class FootprintParticle extends TextureSheetParticle {

    // Texture properties
    private static final float TEXEL_WIDTH = 1F / 8F;
    private static final float TEXEL_PRINT_WIDTH = TEXEL_WIDTH / 2F;

    // Basic footprint layout
    private static final float WIDTH = 0.125F;
    private static final float LENGTH = WIDTH * 2.0F;
    private static final Vector2f FIRST_POINT = new Vector2f(-WIDTH, LENGTH);
    private static final Vector2f SECOND_POINT = new Vector2f(WIDTH, LENGTH);
    private static final Vector2f THIRD_POINT = new Vector2f(WIDTH, -LENGTH);
    private static final Vector2f FOURTH_POINT = new Vector2f(-WIDTH, -LENGTH);

    // Micro Y adjuster to avoid z-fighting
    private static float zFighter = 0F;

    private final boolean isSnowLayer;
    private final BlockPos downPos;

    private final float texU1, texU2;
    private final float texV1, texV2;
    private final float scale;

    private final Vector2f firstPoint;
    private final Vector2f secondPoint;
    private final Vector2f thirdPoint;
    private final Vector2f fourthPoint;

    public FootprintParticle(@NotNull final FootprintStyle style, @NotNull final ClientLevel level,
                             final double x, final double y, final double z,
                             final float rotation, final float scale, final boolean isRight) {
        super(level, x, y, z, 0, 0, 0);

        this.lifetime = 200;

        // Z-fighting prevention
        if (++zFighter > 20)
            zFighter = 1;

        final BlockPos pos = BlockPos.containing(x, y, z);
        final BlockState state = level.getBlockState(pos);
        this.isSnowLayer = state.is(Blocks.SNOW);

        this.y += zFighter * 0.001F;

        // Calculate down position
        final float fraction = (float) (y - (int) y);
        if (this.isSnowLayer || fraction <= 0.0625F) {
            this.downPos = pos.below();
        } else {
            this.downPos = pos.immutable();
        }

        // Calculate UV coordinates
        float u1 = style.ordinal() * TEXEL_WIDTH + 1 / 256F;
        if (isRight)
            u1 += TEXEL_PRINT_WIDTH;
        this.texU1 = u1;
        this.texU2 = u1 + TEXEL_PRINT_WIDTH;
        this.texV1 = 0F;
        this.texV2 = 1F;
        this.scale = scale;

        // Pre-calculate rotated vertex points
        final float theRotation = (float) Math.toRadians(-rotation + 180);
        this.firstPoint = rotateScale(FIRST_POINT, theRotation, this.scale);
        this.secondPoint = rotateScale(SECOND_POINT, theRotation, this.scale);
        this.thirdPoint = rotateScale(THIRD_POINT, theRotation, this.scale);
        this.fourthPoint = rotateScale(FOURTH_POINT, theRotation, this.scale);

        // Set color to white (texture will provide color)
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.alpha = 0.8F;  // Start with visible alpha

        // No physics
        this.hasPhysics = false;
        this.gravity = 0.0F;
    }

    private static Vector2f rotateScale(Vector2f point, float rotation, float scale) {
        final float cos = Mth.cos(rotation);
        final float sin = Mth.sin(rotation);
        final float x = point.x * cos - point.y * sin;
        final float y = point.x * sin + point.y * cos;
        return new Vector2f(x * scale, y * scale);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        // Age faster when raining
        if (isRaining()) {
            this.age += (int) (getWeatherIntensity() * 4);
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Check if ground is still solid
        if (!this.level.getBlockState(this.downPos).isSolidRender(this.level, this.downPos)) {
            this.remove();
            return;
        }

        // Check if snow layer melted
        if (this.isSnowLayer) {
            final BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
            if (!this.level.getBlockState(pos).is(Blocks.SNOW)) {
                this.remove();
                return;
            }
        }

        // Calculate alpha fade
        float f = (float) this.age / (float) this.lifetime;
        f = f * f;
        this.alpha = MathStuff.clamp(1.0F - f, 0F, 1F) * 0.8F;  // Increased from 0.4 to 0.8 for better visibility
    }

    private boolean isRaining() {
        var levelOpt = GameUtils.getWorld();
        if (levelOpt.isEmpty())
            return false;
        return levelOpt.get().isRaining();
    }

    private float getWeatherIntensity() {
        var levelOpt = GameUtils.getWorld();
        if (levelOpt.isEmpty())
            return 0F;
        var level = levelOpt.get();
        final BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
        return level.getRainLevel(1.0F) * (level.isRainingAt(pos) ? 1.0F : 0.0F);
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTicks) {
        // Custom rendering for ground-aligned quad
        final float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - camera.getPosition().x);
        final float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - camera.getPosition().y);
        final float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - camera.getPosition().z);

        final int light = this.getLightColor(partialTicks);

        // Render ground-aligned quad (4 vertices in Y plane)
        buffer.vertex(x + this.firstPoint.x, y, z + this.firstPoint.y)
                .uv(this.texU1, this.texV2)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
        buffer.vertex(x + this.secondPoint.x, y, z + this.secondPoint.y)
                .uv(this.texU2, this.texV2)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
        buffer.vertex(x + this.thirdPoint.x, y, z + this.thirdPoint.y)
                .uv(this.texU2, this.texV1)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
        buffer.vertex(x + this.fourthPoint.x, y, z + this.fourthPoint.y)
                .uv(this.texU1, this.texV1)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return FootprintRenderType.INSTANCE;
    }
}
