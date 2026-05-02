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
package org.orecruncher.dsurround.client.handlers.fog;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler;

/**
 * Implements the void fog (the fog at bedrock) of older versions of Minecraft.
 */
@OnlyIn(Dist.CLIENT)
public class BedrockFogRangeCalculator extends VanillaFogRangeCalculator {

    protected final FogResult cached = new FogResult();
    protected double skyLight;

    public BedrockFogRangeCalculator() {

    }

    @Override
    public String getName() {
        return "BedrockFogRangeCalculator";
    }

    @Override
    public FogResult calculate(ViewportEvent.RenderFog event) {

        this.cached.set(event);
        Level level = EnvironStateHandler.EnvironState.getWorld();

        // In 1.20.1, void particles are no longer a thing, but we can check if we're in the overworld
        // and below a certain Y level to apply bedrock fog
        if (level != null && level.dimension() == Level.OVERWORLD) {
            LivingEntity player = EnvironStateHandler.EnvironState.getPlayer();
            if (player != null) {
                double factor = (player.yOld + (player.getY() - player.yOld) * event.getPartialTick() + 4.0D) / 32.0D;
                double d0 = (this.skyLight / 16.0D) + factor;

                float end = event.getFarPlaneDistance();
                if (d0 < 1.0D) {
                    if (d0 < 0.0D) {
                        d0 = 0.0D;
                    }

                    d0 *= d0;
                    float f2 = 100.0F * (float) d0;

                    if (f2 < 5.0F) {
                        f2 = 5.0F;
                    }

                    if (end > f2) {
                        end = f2;
                    }
                }

                this.cached.set(event.getFogShape(), end, FogResult.DEFAULT_PLANE_SCALE);
            }
        }

        return this.cached;
    }

    @Override
    public void tick() {
        LivingEntity player = EnvironStateHandler.EnvironState.getPlayer();
        if (player != null) {
            // In 1.20.1, getBrightness() returns a float, we need to extract sky light differently
            // For now, use a simplified approach
            this.skyLight = player.level().getMaxLocalRawBrightness(player.blockPosition());
        }
    }
}
