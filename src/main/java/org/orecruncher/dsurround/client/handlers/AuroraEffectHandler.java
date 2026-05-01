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

package org.orecruncher.dsurround.client.handlers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.dsurround.aurora.AuroraFactory;
import org.orecruncher.dsurround.lib.Library;
import org.orecruncher.dsurround.lib.logging.IModLog;

import javax.annotation.Nonnull;

/**
 * Manages aurora effects in the game world.
 * Delegates to AuroraFactory for actual aurora management.
 */
@OnlyIn(Dist.CLIENT)
public final class AuroraEffectHandler extends EffectHandlerBase {

    private static final IModLog LOGGER = Library.LOGGER;

    private final AuroraFactory factory = new AuroraFactory();

    public AuroraEffectHandler() {
        super("Aurora Effect");
    }

    @Override
    public void onConnect() {
        this.factory.clear();
    }

    @Override
    public void onDisconnect() {
        this.factory.clear();
    }

    @Override
    public void process(@Nonnull Player player) {
        // Update all auroras
        this.factory.tick();
    }

    /**
     * Render all active auroras.
     * Called from AuroraRenderHandler during render events.
     */
    public void render(PoseStack poseStack, float partialTicks) {
        this.factory.render(poseStack, partialTicks);
    }

    /**
     * Get the aurora factory for direct access if needed.
     */
    public AuroraFactory getFactory() {
        return this.factory;
    }
}
