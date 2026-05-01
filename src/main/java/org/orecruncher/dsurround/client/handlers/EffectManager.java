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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.dsurround.Constants;
import org.orecruncher.dsurround.lib.Library;
import org.orecruncher.dsurround.lib.logging.IModLog;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central manager for all client-side effect handlers.
 * Coordinates initialization, tick processing, and lifecycle management.
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EffectManager {

    private static final IModLog LOGGER = Library.LOGGER;
    private static final EffectManager INSTANCE = new EffectManager();
    private static boolean isConnected = false;

    private final List<EffectHandlerBase> effectHandlers = new ArrayList<>();
    private final Map<Class<? extends EffectHandlerBase>, EffectHandlerBase> services = new HashMap<>();

    private EffectManager() {
        // Private constructor - singleton pattern
    }

    public static EffectManager instance() {
        return INSTANCE;
    }

    /**
     * Registers an effect handler with the manager.
     */
    private void register(@Nonnull EffectHandlerBase handler) {
        this.effectHandlers.add(handler);
        this.services.put(handler.getClass(), handler);
        LOGGER.debug("Registered handler [%s]", handler.getClass().getSimpleName());
    }

    /**
     * Initializes all effect handlers in the correct order.
     * EnvironStateHandler must go first as it sets up state for others.
     */
    private void initializeHandlers() {
        // EnvironStateHandler goes first - it sets up state for the rest
        register(new EnvironStateHandler());

        // TODO: Register other handlers as they are implemented
        // register(new AreaBlockEffectsHandler());
        // register(new FogHandler());
        // register(new ParticleSystemHandler());
        // register(new BiomeSoundEffectsHandler());
        // register(new AuroraEffectHandler());
        // register(new WeatherHandler());
        // register(new FxHandler());
        // register(SoundEffectHandler.INSTANCE);
        // register(new DiagnosticHandler());

        LOGGER.info("Initialized %d effect handlers", this.effectHandlers.size());
    }

    /**
     * Looks up a registered handler service by class.
     */
    @SuppressWarnings("unchecked")
    public <T extends EffectHandlerBase> T lookupService(@Nonnull Class<T> serviceClass) {
        EffectHandlerBase handler = this.services.get(serviceClass);
        if (handler == null) {
            LOGGER.warn("Unable to locate handler service [%s]", serviceClass.getSimpleName());
        }
        return (T) handler;
    }

    /**
     * Returns whether the manager is currently connected and active.
     */
    public static boolean isConnected() {
        return isConnected;
    }

    /**
     * Connects the effect manager when joining a world.
     * Initializes all handlers and starts processing.
     */
    public static void connect() {
        if (isConnected) {
            LOGGER.warn("Attempt to initialize EffectManager when already initialized");
            disconnect();
        }

        INSTANCE.initializeHandlers();

        for (EffectHandlerBase handler : INSTANCE.effectHandlers) {
            handler.onConnect();
        }

        isConnected = true;
        LOGGER.info("EffectManager connected");
    }

    /**
     * Disconnects the effect manager when leaving a world.
     * Cleans up all handlers and stops processing.
     */
    public static void disconnect() {
        if (isConnected) {
            for (EffectHandlerBase handler : INSTANCE.effectHandlers) {
                handler.onDisconnect();
            }

            INSTANCE.effectHandlers.clear();
            INSTANCE.services.clear();

            isConnected = false;
            LOGGER.info("EffectManager disconnected");
        }
    }

    /**
     * Checks if the client is ready for processing.
     */
    private boolean checkReady() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.isPaused()) {
            return false;
        }

        if (mc.options == null) {
            return false;
        }

        Player player = mc.player;
        return player != null && player.level() != null;
    }

    /**
     * Main tick processing - called every client tick.
     */
    private void onTick(@Nonnull TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        if (!checkReady()) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        // Get current tick from EnvironState
        int tick = EnvironStateHandler.EnvironState.getTickCounter();

        // Process all handlers
        for (EffectHandlerBase handler : this.effectHandlers) {
            try {
                if (handler.doTick(tick)) {
                    handler.process(player);
                }
            } catch (Exception e) {
                LOGGER.error(e, "Error processing handler %s", handler.getHandlerName());
            }
        }
    }

    /**
     * Event handler for client ticks.
     */
    @SubscribeEvent
    public static void onClientTick(@Nonnull TickEvent.ClientTickEvent event) {
        if (isConnected) {
            INSTANCE.onTick(event);
        }
    }
}
