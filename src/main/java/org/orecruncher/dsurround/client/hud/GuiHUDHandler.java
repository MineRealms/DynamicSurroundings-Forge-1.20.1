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

package org.orecruncher.dsurround.client.hud;

import org.orecruncher.dsurround.client.handlers.EnvironStateHandler;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public final class GuiHUDHandler {

    private static GuiHUDHandler INSTANCE;

    private GuiHUDHandler() {
        register(new InspectionHUD());
    }

    private final List<IGuiOverlay> overlays = new ArrayList<>();

    public void register(final IGuiOverlay overlay) {
        this.overlays.add(overlay);
    }

    public static void register() {
        INSTANCE = new GuiHUDHandler();
        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    public static void unregister() {
        if (INSTANCE != null) {
            MinecraftForge.EVENT_BUS.unregister(INSTANCE);
            INSTANCE = null;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRenderGuiOverlayPre(final RenderGuiOverlayEvent.Pre event) {
        this.overlays.forEach(o -> o.doRender(event));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRenderGuiOverlayPost(final RenderGuiOverlayEvent.Post event) {
        this.overlays.forEach(o -> o.doRender(event));
    }

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END || Minecraft.getInstance().isPaused())
            return;

        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return;

        final int tickRef = EnvironStateHandler.EnvironState.getTickCounter();
        this.overlays.forEach(o -> o.doTick(tickRef));
    }
}
