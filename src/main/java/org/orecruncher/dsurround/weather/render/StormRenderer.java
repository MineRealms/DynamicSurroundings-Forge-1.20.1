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

package org.orecruncher.dsurround.weather.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.orecruncher.dsurround.capabilities.CapabilityHandler;
import org.orecruncher.dsurround.capabilities.season.ISeasonInfo;
import org.orecruncher.dsurround.capabilities.season.PrecipitationType;
import org.orecruncher.dsurround.weather.Weather;

/**
 * Advanced weather renderer for Dynamic Surroundings.
 * Renders rain, snow, and dust storms with intensity-based textures.
 */
@OnlyIn(Dist.CLIENT)
public class StormRenderer {

    // Pre-calculated rain coordinates for smooth rendering
    private static final double[] RAIN_X_COORDS = new double[1024];
    private static final double[] RAIN_Y_COORDS = new double[1024];

    static {
        // Pre-calculate rain particle offsets in a circular pattern
        for (int i = 0; i < 32; ++i) {
            for (int j = 0; j < 32; ++j) {
                final double f2 = j - 16;
                final double f3 = i - 16;
                final double f4 = Math.sqrt(f2 * f2 + f3 * f3);
                RAIN_X_COORDS[i << 5 | j] = (-f3 / f4) * 0.5D;
                RAIN_Y_COORDS[i << 5 | j] = (f2 / f4) * 0.5D;
            }
        }
    }

    private final RandomSource random = RandomSource.create();
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    /**
     * Main render method for weather effects.
     * Called each frame to render rain, snow, or dust particles.
     *
     * @param level The client level
     * @param ticks Current render ticks
     * @param partialTick Partial tick for smooth interpolation
     * @param camera The camera position
     * @param poseStack The pose stack for transformations
     */
    public void render(ClientLevel level, int ticks, float partialTick, Camera camera, PoseStack poseStack) {
        // Check if weather should be rendered
        float rainStrength = Weather.getIntensityLevel();
        if (rainStrength <= 0.0F) {
            return;
        }

        // Get camera position
        double cameraX = camera.getPosition().x;
        double cameraY = camera.getPosition().y;
        double cameraZ = camera.getPosition().z;

        int playerX = Mth.floor(cameraX);
        int playerY = Mth.floor(cameraY);
        int playerZ = Mth.floor(cameraZ);

        // Calculate alpha based on rain strength
        float maxIntensity = Weather.getMaxIntensityLevel();
        float alphaRatio = rainStrength / maxIntensity;

        // Determine render range based on graphics settings
        Minecraft mc = Minecraft.getInstance();
        int range = mc.options.graphicsMode().get().getId() >= 2 ? 10 : 5;

        // Setup rendering state
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(Minecraft.useShaderTransparency());

        // Get current weather properties
        Weather.Properties weatherProps = Weather.getWeatherProperties();
        ISeasonInfo seasonInfo = CapabilityHandler.getSeasonInfo(level);

        // Render weather particles in a grid around the player
        int renderCount = ticks;
        float animationTime = renderCount + partialTick;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        // Track current texture to minimize texture switches
        ResourceLocation currentTexture = null;
        boolean isBuilding = false;

        for (int gridZ = playerZ - range; gridZ <= playerZ + range; ++gridZ) {
            for (int gridX = playerX - range; gridX <= playerX + range; ++gridX) {
                // Get pre-calculated rain offsets
                int idx = (gridZ - playerZ + 16) * 32 + gridX - playerX + 16;
                double rainX = RAIN_X_COORDS[idx];
                double rainY = RAIN_Y_COORDS[idx];

                // Get precipitation height for this position
                mutablePos.set(gridX, 0, gridZ);
                int precipHeight = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, mutablePos).getY();

                // Calculate render bounds
                int minY = Math.max(playerY - range, precipHeight);
                int maxY = Math.max(playerY + range, precipHeight);

                if (minY >= maxY) {
                    continue;
                }

                // Check biome precipitation type
                mutablePos.set(gridX, minY, gridZ);
                Biome biome = level.getBiome(mutablePos).value();

                // Determine precipitation type from season info
                if (seasonInfo == null) {
                    continue;
                }

                PrecipitationType precipitation = seasonInfo.getPrecipitationType(mutablePos);

                if (precipitation == PrecipitationType.NONE) {
                    continue;
                }

                // Calculate distance fade
                double dx = gridX + 0.5F - cameraX;
                double dz = gridZ + 0.5F - cameraZ;
                float distanceFactor = (float) Math.sqrt(dx * dx + dz * dz) / range;

                // Get lighting for this position
                mutablePos.set(gridX, Math.max(precipHeight, playerY), gridZ);
                int blockLight = level.getBrightness(LightLayer.BLOCK, mutablePos);
                int skyLight = level.getBrightness(LightLayer.SKY, mutablePos);
                int packedLight = LightTexture.pack(blockLight, skyLight);

                // Seed random for consistent particle generation
                int seed = (gridZ << 16) ^ gridX;
                random.setSeed(seed);

                // Determine texture based on precipitation type
                ResourceLocation texture;
                float red = 1.0F, green = 1.0F, blue = 1.0F;
                double texOffsetU, texOffsetV;
                float alpha;

                if (precipitation == PrecipitationType.RAIN) {
                    texture = weatherProps.getRainTexture();

                    // Rain animation - vertical scrolling
                    texOffsetV = ((double) (renderCount + seed & 31) + (double) partialTick) / 32.0D
                                 * (3.0D + random.nextDouble());
                    texOffsetU = 0.0D;

                    alpha = ((1.0F - distanceFactor * distanceFactor) * 0.5F + 0.5F) * alphaRatio;

                } else if (precipitation == PrecipitationType.SNOW) {
                    texture = weatherProps.getSnowTexture();

                    // Snow animation - gentle drift
                    texOffsetV = ((renderCount & 511) + partialTick) / 512.0F;
                    texOffsetU = random.nextDouble() + (double) animationTime * 0.01F * (float) random.nextGaussian();

                    alpha = ((1.0F - distanceFactor * distanceFactor) * 0.3F + 0.5F) * alphaRatio;

                } else {
                    // Dust storm
                    texture = weatherProps.getDustTexture();

                    // Dust animation - chaotic movement
                    texOffsetV = ((renderCount & 511) + partialTick) / 512.0F;
                    texOffsetU = random.nextDouble() + (double) animationTime * 0.2F * (float) random.nextGaussian();

                    // Dust color (sandy brown)
                    red = 0.9F;
                    green = 0.75F;
                    blue = 0.6F;

                    alpha = ((1.0F - distanceFactor * distanceFactor) * 0.3F + 0.5F) * alphaRatio;
                }

                // Switch texture if needed
                if (currentTexture != texture) {
                    if (isBuilding) {
                        tesselator.end();
                    }

                    RenderSystem.setShader(GameRenderer::getParticleShader);
                    RenderSystem.setShaderTexture(0, texture);

                    bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                    currentTexture = texture;
                    isBuilding = true;
                }

                // Render the weather particle quad
                Matrix4f matrix = poseStack.last().pose();

                // Calculate world positions relative to camera
                double x0 = gridX - rainX + 0.5D - cameraX;
                double x1 = gridX + rainX + 0.5D - cameraX;
                double z0 = gridZ - rainY + 0.5D - cameraZ;
                double z1 = gridZ + rainY + 0.5D - cameraZ;
                double y0 = minY - cameraY;
                double y1 = maxY - cameraY;

                // Add vertices for the quad
                bufferBuilder.vertex(matrix, (float) x0, (float) y0, (float) z0)
                        .uv((float) texOffsetU, (float) (y0 * 0.25D + texOffsetV))
                        .color(red, green, blue, alpha)
                        .uv2(packedLight)
                        .endVertex();

                bufferBuilder.vertex(matrix, (float) x1, (float) y0, (float) z1)
                        .uv((float) (1.0D + texOffsetU), (float) (y0 * 0.25D + texOffsetV))
                        .color(red, green, blue, alpha)
                        .uv2(packedLight)
                        .endVertex();

                bufferBuilder.vertex(matrix, (float) x1, (float) y1, (float) z1)
                        .uv((float) (1.0D + texOffsetU), (float) (y1 * 0.25D + texOffsetV))
                        .color(red, green, blue, alpha)
                        .uv2(packedLight)
                        .endVertex();

                bufferBuilder.vertex(matrix, (float) x0, (float) y1, (float) z0)
                        .uv((float) texOffsetU, (float) (y1 * 0.25D + texOffsetV))
                        .color(red, green, blue, alpha)
                        .uv2(packedLight)
                        .endVertex();
            }
        }

        // Finish rendering
        if (isBuilding) {
            tesselator.end();
        }

        // Restore rendering state
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
