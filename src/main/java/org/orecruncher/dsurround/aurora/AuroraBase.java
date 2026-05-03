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

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.dsurround.lib.random.IRandomizer;
import org.orecruncher.dsurround.lib.random.Randomizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for aurora implementations.
 * Provides common functionality for lifecycle management and geometry generation.
 */
@OnlyIn(Dist.CLIENT)
public abstract class AuroraBase implements IAurora {

    protected static final float ANGLE = Mth.PI / 2.0F;

    protected final long seed;
    protected final IRandomizer random;
    protected final AuroraLifeTracker tracker;
    protected final AuroraColor preset;

    protected final List<Panel> panels = new ArrayList<>();

    protected double posX;
    protected double posY;
    protected double posZ;

    protected int length;
    protected int nodeCount;
    protected int nodeLength;

    protected float[] topColor;
    protected float[] middleColor;
    protected float[] bottomColor;

    /**
     * Create a new aurora base.
     *
     * @param seed The seed for random generation
     */
    protected AuroraBase(long seed) {
        this.seed = seed;
        this.random = Randomizer.current();
        this.random.setSeed(seed);
        this.tracker = new AuroraLifeTracker(AuroraUtils.AURORA_PEAK_AGE, AuroraUtils.AURORA_AGE_RATE);
        this.preset = AuroraColor.random(this.random);

        this.topColor = this.preset.getTopColor();
        this.middleColor = this.preset.getMiddleColor();
        this.bottomColor = this.preset.getBottomColor();

        // Aurora position is relative to player, not world space
        // These are just offsets, not absolute positions
        this.posX = 0;
        this.posY = 0;
        this.posZ = 0;

        // Note: Don't call generateGeometry() here!
        // It will be called by subclass after their fields are initialized
    }

    /**
     * Generate the aurora geometry (panels).
     * Must be implemented by subclasses.
     */
    protected abstract void generateGeometry();

    @Override
    public void setFading(boolean flag) {
        this.tracker.setFading(flag);
    }

    @Override
    public boolean isDying() {
        return this.tracker.isFading();
    }

    @Override
    public void update() {
        this.tracker.update();
    }

    @Override
    public boolean isComplete() {
        return !this.tracker.isAlive();
    }

    /**
     * Get the current alpha value based on lifecycle.
     *
     * @return Alpha value (0.0-1.0)
     */
    protected float getAlpha() {
        return this.tracker.ageRatio();
    }

    /**
     * Translate the aurora to maintain fixed offset relative to player.
     * The aurora should appear as a distant sky effect that doesn't move with camera rotation.
     *
     * @param poseStack The pose stack for transformations
     */
    protected void translate(PoseStack poseStack) {
        // Get camera position to cancel out world translation
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        // Cancel out the camera's world position so aurora stays fixed relative to player
        // Then apply fixed offsets for aurora positioning
        poseStack.translate(
            -cameraPos.x,  // Cancel X movement
            AuroraUtils.PLAYER_FIXED_Y_OFFSET - cameraPos.y,  // Fixed height, cancel Y movement
            AuroraUtils.PLAYER_FIXED_Z_OFFSET - cameraPos.z   // Fixed distance, cancel Z movement
        );
    }

    /**
     * Get the list of panels for rendering.
     *
     * @return The list of panels
     */
    public List<Panel> getPanels() {
        return this.panels;
    }

    /**
     * Get the top color.
     *
     * @return RGB color array
     */
    public float[] getTopColor() {
        return this.topColor;
    }

    /**
     * Get the middle color.
     *
     * @return RGB color array
     */
    public float[] getMiddleColor() {
        return this.middleColor;
    }

    /**
     * Get the bottom color.
     *
     * @return RGB color array
     */
    public float[] getBottomColor() {
        return this.bottomColor;
    }
}
