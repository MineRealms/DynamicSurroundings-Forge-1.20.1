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

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.dsurround.lib.random.IRandomizer;
import org.orecruncher.dsurround.lib.random.Randomizer;

import javax.annotation.Nonnull;

/**
 * Base class for all effect handlers.
 * Provides common functionality like timing, lifecycle management, and helper methods.
 */
@OnlyIn(Dist.CLIENT)
public abstract class EffectHandlerBase {

    protected final IRandomizer random = Randomizer.current();
    private final String handlerName;

    protected EffectHandlerBase(@Nonnull String name) {
        this.handlerName = name;
    }

    /**
     * Gets the handler name for logging purposes.
     */
    public final String getHandlerName() {
        return this.handlerName;
    }

    /**
     * Indicates whether the handler needs to be invoked for the given tick.
     *
     * @param tick Current tick counter
     * @return true if the handler should process this tick
     */
    public boolean doTick(int tick) {
        return true;
    }

    /**
     * Main processing logic for the handler.
     * Called every tick if doTick() returns true.
     *
     * @param player The current player
     */
    public void process(@Nonnull Player player) {
        // Override in subclasses
    }

    /**
     * Called when the client connects to a server.
     * Use for initialization and resetting state.
     */
    public void onConnect() {
        // Override in subclasses
    }

    /**
     * Called when the client disconnects from a server.
     * Use for cleanup and releasing resources.
     */
    public void onDisconnect() {
        // Override in subclasses
    }

    // Helper methods
    protected static Player getPlayer() {
        return Minecraft.getInstance().player;
    }

    protected static net.minecraft.client.multiplayer.ClientLevel getWorld() {
        return Minecraft.getInstance().level;
    }

    @Override
    public String toString() {
        return "EffectHandler{" + handlerName + "}";
    }
}
