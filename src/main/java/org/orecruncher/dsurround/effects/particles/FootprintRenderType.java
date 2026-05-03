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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.orecruncher.dsurround.Constants;

/**
 * Custom particle render type for footprints.
 * Footprints need special rendering:
 * - Ground-aligned (not billboarded)
 * - Depth mask disabled (render on surface)
 * - Standard alpha blending
 */
@OnlyIn(Dist.CLIENT)
public class FootprintRenderType implements ParticleRenderType {

    public static final ResourceLocation FOOTPRINT_TEXTURE =
        new ResourceLocation(Constants.MOD_ID, "textures/particles/footprint.png");

    public static final FootprintRenderType INSTANCE = new FootprintRenderType();

    private FootprintRenderType() {
    }

    @Override
    public void begin(@NotNull BufferBuilder builder, @NotNull TextureManager textureManager) {
        RenderSystem.depthMask(false);  // Disable depth writing (don't write to depth buffer)
        // Keep depth test ENABLED so footprints are occluded by blocks in front
        RenderSystem.setShaderTexture(0, FOOTPRINT_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // CRITICAL: Must call buffer.begin() to start the BufferBuilder!
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
    }

    @Override
    public void end(@NotNull Tesselator tesselator) {
        tesselator.end();
        RenderSystem.depthMask(true);  // Re-enable depth writing
        RenderSystem.disableBlend();
    }

    @Override
    public String toString() {
        return "FOOTPRINT";
    }
}
