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
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler;
import org.orecruncher.dsurround.lib.di.ContainerManager;
import org.orecruncher.dsurround.lib.math.MathStuff;
import org.orecruncher.dsurround.runtime.sets.impl.WeatherVariables;

/**
 * Scans the biome area around the player to determine the fog parameters.
 */
@OnlyIn(Dist.CLIENT)
public class BiomeFogRangeCalculator extends VanillaFogRangeCalculator {

    protected static final int DISTANCE = 20;
    protected static final float DUST_FOG_IMPACT = 0.9F;

    private static class Context {
        public int posX;
        public int posZ;
        public float rain;
        public float lastFarPlane;
        public boolean doScan = true;
        public final FogResult cached = new FogResult();

        public boolean returnCached(int pX, int pZ, float r, ViewportEvent.RenderFog event) {
            return !this.doScan && pX == this.posX && pZ == this.posZ && r == this.rain
                    && this.lastFarPlane == event.getFarPlaneDistance() && this.cached.isValid(event);
        }
    }

    protected final Context[] context = { new Context(), new Context() };

    public BiomeFogRangeCalculator() {

    }

    @Override
    public String getName() {
        return "BiomeFogRangeCalculator";
    }

    @Override
    public FogResult calculate(ViewportEvent.RenderFog event) {

        LivingEntity player = EnvironStateHandler.EnvironState.getPlayer();
        Level level = EnvironStateHandler.EnvironState.getWorld();

        if (player == null || level == null) {
            return new FogResult(event);
        }

        int playerX = MathStuff.floor(player.getX());
        int playerZ = MathStuff.floor(player.getZ());
        WeatherVariables weather = ContainerManager.resolve(WeatherVariables.class);
        float rainStr = weather.getRainIntensity();

        // Use context based on fog shape (SPHERE vs CYLINDER)
        Context ctx = this.context[event.getFogShape().ordinal() % 2];

        if (ctx.returnCached(playerX, playerZ, rainStr, event))
            return ctx.cached;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);

        float fpDistanceBiomeFog = 0F;
        float weightBiomeFog = 0;

        boolean isRaining = level.isRaining();
        ctx.rain = rainStr;
        ctx.doScan = false;

        for (int x = -DISTANCE; x <= DISTANCE; ++x) {
            for (int z = -DISTANCE; z <= DISTANCE; ++z) {
                pos.set(playerX + x, 0, playerZ + z);

                // Check if chunk is loaded
                if (level instanceof ClientLevel clientLevel && !clientLevel.hasChunkAt(pos)) {
                    ctx.doScan = true;
                    continue;
                }

                Biome biome = level.getBiome(pos).value();

                // TODO: Need BiomeInfo system to get fog properties
                // For now, use simplified logic
                float distancePart = 1F;
                float weightPart = 1;

                // Simplified fog calculation - will be enhanced when BiomeInfo is available
                if (isRaining) {
                    // Apply rain fog effect
                    distancePart = 1F - 0.3F * rainStr;
                }

                fpDistanceBiomeFog += distancePart;
                weightBiomeFog += weightPart;
            }
        }

        float weightMixed = (DISTANCE * 2 + 1) * (DISTANCE * 2 + 1);
        float weightDefault = weightMixed - weightBiomeFog;

        float fpDistanceBiomeFogAvg = (weightBiomeFog == 0) ? 0 : fpDistanceBiomeFog / weightBiomeFog;

        float rangeConst = Math.max(240, event.getFarPlaneDistance() - 16);
        float farPlaneDistance = (fpDistanceBiomeFog * rangeConst + event.getFarPlaneDistance() * weightDefault)
                / weightMixed;
        float farPlaneDistanceScaleBiome = (0.1f * (1 - fpDistanceBiomeFogAvg) + 0.75f * fpDistanceBiomeFogAvg);
        float farPlaneDistanceScale = (farPlaneDistanceScaleBiome * weightBiomeFog + 0.75f * weightDefault)
                / weightMixed;

        ctx.posX = playerX;
        ctx.posZ = playerZ;
        ctx.lastFarPlane = event.getFarPlaneDistance();
        farPlaneDistance = Math.min(farPlaneDistance, event.getFarPlaneDistance());

        ctx.cached.set(event.getFogShape(), farPlaneDistance, farPlaneDistanceScale);

        return ctx.cached;
    }
}
