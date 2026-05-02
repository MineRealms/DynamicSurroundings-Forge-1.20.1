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

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler;
import org.orecruncher.dsurround.lib.Color;
import org.orecruncher.dsurround.lib.di.ContainerManager;
import org.orecruncher.dsurround.lib.math.MathStuff;
import org.orecruncher.dsurround.runtime.sets.impl.WeatherVariables;

@OnlyIn(Dist.CLIENT)
public class BiomeFogColorCalculator extends VanillaFogColorCalculator {

    protected int posX;
    protected int posZ;

    // Last pass calculations. We can reuse if possible to avoid scanning
    // the area, again.
    protected double weightBiomeFog;
    protected Color biomeFogColor;
    protected boolean doScan = true;

    @Override
    public Color calculate(ViewportEvent.ComputeFogColor event) {

        LivingEntity player = EnvironStateHandler.EnvironState.getPlayer();
        Level level = EnvironStateHandler.EnvironState.getWorld();

        if (player == null || level == null) {
            return super.calculate(event);
        }

        int playerX = MathStuff.floor(player.getX());
        int playerZ = MathStuff.floor(player.getZ());

        // Get render distance for fog blending
        int distance = Minecraft.getInstance().options.renderDistance().get();
        distance = MathStuff.clamp(distance / 2, 2, 8);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);
        this.doScan |= this.posX != playerX || this.posZ != playerZ;

        if (this.doScan) {
            this.doScan = false;
            this.posX = playerX;
            this.posZ = playerZ;
            this.biomeFogColor = new Color(0, 0, 0);
            this.weightBiomeFog = 0;

            for (int x = -distance; x <= distance; ++x) {
                for (int z = -distance; z <= distance; ++z) {
                    pos.set(playerX + x, 0, playerZ + z);

                    // If the chunk is not available doScan will be set true. This will force
                    // another scan on the next tick.
                    if (level instanceof ClientLevel clientLevel && !clientLevel.hasChunkAt(pos)) {
                        this.doScan = true;
                        continue;
                    }

                    Biome biome = level.getBiome(pos).value();

                    // TODO: Need BiomeInfo system to get fog color properties
                    // For now, skip custom fog colors
                    Color color = null;

                    if (color != null) {
                        this.biomeFogColor.add(color);
                        this.weightBiomeFog += 1F;
                    }
                }
            }
        }

        // If we have nothing then just return whatever Vanilla wanted
        if (this.weightBiomeFog == 0 || distance == 0)
            return super.calculate(event);

        // Calculate the scale based on sunlight and celestial angle
        float partialTicks = (float) event.getPartialTick();
        float celestialAngle = level.getTimeOfDay(partialTicks);
        float baseScale = MathStuff.clamp(MathStuff.cos(celestialAngle * MathStuff.PI_F * 2.0F) * 2.0F + 0.5F, 0, 1);

        double rScale = baseScale * 0.94F + 0.06F;
        double gScale = baseScale * 0.94F + 0.06F;
        double bScale = baseScale * 0.91F + 0.09F;

        // Adjust the scale further based on rain and thunder
        WeatherVariables weather = ContainerManager.resolve(WeatherVariables.class);
        float rainStrength = weather.getRainIntensity();
        if (rainStrength > 0) {
            rScale *= 1 - rainStrength * 0.5f;
            gScale *= 1 - rainStrength * 0.5f;
            bScale *= 1 - rainStrength * 0.4f;
        }

        float thunderStrength = weather.getThunderIntensity();
        if (thunderStrength > 0) {
            rScale *= 1 - thunderStrength * 0.5f;
            gScale *= 1 - thunderStrength * 0.5f;
            bScale *= 1 - thunderStrength * 0.5f;
        }

        // Normalize the blended color components based on the biome weight
        Color fogColor = new Color(this.biomeFogColor);
        fogColor.scale(
                (float) (rScale / this.weightBiomeFog),
                (float) (gScale / this.weightBiomeFog),
                (float) (bScale / this.weightBiomeFog));

        Color processedColor = applyPlayerEffects(level, player, fogColor, partialTicks);

        double weightMixed = (distance * 2 + 1) * (distance * 2 + 1);
        double weightDefault = weightMixed - this.weightBiomeFog;
        Color vanillaColor = super.calculate(event);

        processedColor.scale((float) this.weightBiomeFog);
        vanillaColor.scale((float) weightDefault);
        return processedColor.add(vanillaColor).scale((float) (1 / weightMixed));
    }

    protected Color applyPlayerEffects(Level level, LivingEntity player, Color fogColor, float renderPartialTicks) {
        // In 1.20.1, dimension effects are handled differently
        float darkScale = (float) ((player.yOld + (player.getY() - player.yOld) * renderPartialTicks)
                * level.dimensionType().coordinateScale());

        // If the player is blind need to darken it further
        MobEffectInstance potionEffect = player.getEffect(MobEffects.BLINDNESS);
        if (potionEffect != null) {
            int duration = potionEffect.getDuration();
            darkScale *= (duration < 20) ? (1 - duration / 20f) : 0;
        }

        if (darkScale < 1) {
            darkScale = (darkScale < 0) ? 0 : darkScale * darkScale;
            fogColor.scale(darkScale);
        }

        // If the player has night vision going need to lighten it a bit
        potionEffect = player.getEffect(MobEffects.NIGHT_VISION);
        if (potionEffect != null) {
            int duration = potionEffect.getDuration();
            float brightness = (duration > 200) ? 1
                    : 0.7f + MathStuff.sin((duration - renderPartialTicks) * MathStuff.PI_F * 0.2f) * 0.3f;

            float scale = 1 / fogColor.red();
            scale = Math.min(scale, 1F / fogColor.green());
            scale = Math.min(scale, 1F / fogColor.blue());

            return fogColor.scale((1F - brightness) + scale * brightness);
        }

        return fogColor;
    }
}
