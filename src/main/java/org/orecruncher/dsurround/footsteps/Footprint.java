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

package org.orecruncher.dsurround.footsteps;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class Footprint {

    private FootprintStyle style;
    private LivingEntity entity;
    private Vec3 stepLoc;
    private boolean isRightFoot;
    private float rotation;
    private float scale;

    public static Footprint produce(@NotNull final FootprintStyle style, @NotNull final LivingEntity entity,
                                    @NotNull final Vec3 stepLoc, final float rotation, final float scale, final boolean rightFoot) {
        final Footprint print = new Footprint();
        print.style = style;
        print.entity = entity;
        print.stepLoc = stepLoc;
        print.rotation = rotation;
        print.isRightFoot = rightFoot;
        print.scale = scale;
        return print;
    }

    public FootprintStyle getStyle() {
        return this.style;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    @Nullable
    public Vec3 getStepLocation() {
        return this.stepLoc;
    }

    public boolean isRightFoot() {
        return this.isRightFoot;
    }

    public float getRotation() {
        return this.rotation;
    }

    public float getScale() {
        return this.scale;
    }
}
