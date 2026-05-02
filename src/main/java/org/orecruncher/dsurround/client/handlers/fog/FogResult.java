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

import com.mojang.blaze3d.shaders.FogShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;

@OnlyIn(Dist.CLIENT)
public final class FogResult {

    public static final float DEFAULT_PLANE_SCALE = 0.75F;

    private FogShape fogShape;
    private float start;
    private float end;

    public FogResult() {
        this.fogShape = FogShape.SPHERE;
        this.end = this.start = 0F;
    }

    public FogResult(FogShape fogShape, float distance, float scale) {
        this.set(fogShape, distance, scale);
    }

    public FogResult(float start, float end) {
        this.set(start, end);
    }

    public FogResult(ViewportEvent.RenderFog event) {
        this.set(event);
    }

    public void set(FogShape fogShape, float distance, float scale) {
        this.fogShape = fogShape;
        this.start = distance * scale;
        this.end = distance;
    }

    public void set(float start, float end) {
        this.fogShape = FogShape.SPHERE;
        this.start = start;
        this.end = end;
    }

    public void set(ViewportEvent.RenderFog event) {
        this.set(event.getFogShape(), event.getFarPlaneDistance(), DEFAULT_PLANE_SCALE);
    }

    public FogShape getFogShape() {
        return this.fogShape;
    }

    public float getStart() {
        return this.start;
    }

    public float getEnd() {
        return this.end;
    }

    public boolean isValid(ViewportEvent.RenderFog event) {
        return this.end > this.start && event.getFogShape() == this.fogShape;
    }

    @Override
    public String toString() {
        return String.format("[shape: %s, start: %f, end: %f]", this.fogShape, this.start, this.end);
    }

}
