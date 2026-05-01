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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Interface for aurora implementations.
 * Defines the lifecycle and rendering methods for aurora effects.
 */
@OnlyIn(Dist.CLIENT)
public interface IAurora {

    /**
     * Instructs the aurora to start the process of decay (i.e. start to fade).
     *
     * @param flag true to start fading, false to stop fading
     */
    void setFading(boolean flag);

    /**
     * Indicates if the aurora is in the process of dying.
     *
     * @return true if the aurora is fading
     */
    boolean isDying();

    /**
     * Perform the necessary housekeeping for the aurora.
     * Occurs once a tick.
     */
    void update();

    /**
     * Indicates if an aurora has completed its life cycle and can be removed.
     *
     * @return true if the aurora is complete
     */
    boolean isComplete();

    /**
     * Render the aurora to the client screen.
     * It is possible that other updates can occur to the state,
     * such as doing the transformations to animate.
     *
     * @param poseStack The pose stack for transformations
     * @param partialTick The partial tick for smooth interpolation
     */
    void render(PoseStack poseStack, float partialTick);
}
