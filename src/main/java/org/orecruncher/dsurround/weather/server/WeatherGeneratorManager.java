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

package org.orecruncher.dsurround.weather.server;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.dsurround.Constants;
import org.orecruncher.dsurround.lib.logging.IModLog;
import org.orecruncher.dsurround.lib.logging.ModLog;
import org.orecruncher.dsurround.weather.network.PacketThunder;
import org.orecruncher.dsurround.weather.network.PacketWeatherUpdate;
import org.orecruncher.dsurround.weather.network.WeatherNetwork;

import java.util.HashMap;
import java.util.Map;

/**
 * Server-side weather manager that coordinates weather generation across all dimensions.
 * Manages weather generators for each dimension and handles tick updates.
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WeatherGeneratorManager {

    private static final IModLog LOGGER = new ModLog("WeatherGeneratorManager");

    // Map of dimension -> weather generator
    private static final Map<ResourceKey<Level>, WeatherGenerator> generators = new HashMap<>();

    /**
     * Gets or creates a weather generator for the specified level.
     *
     * @param level The server level
     * @return The weather generator for this level
     */
    public static WeatherGenerator getOrCreateGenerator(ServerLevel level) {
        ResourceKey<Level> dimension = level.dimension();

        return generators.computeIfAbsent(dimension, key -> {
            WeatherGenerator generator = createGenerator(level);
            LOGGER.info("Created %s weather generator for dimension %s",
                    generator.name(), dimension.location());
            return generator;
        });
    }

    /**
     * Creates the appropriate weather generator for a level.
     *
     * @param level The server level
     * @return A new weather generator
     */
    private static WeatherGenerator createGenerator(ServerLevel level) {
        ResourceKey<Level> dimension = level.dimension();

        // Nether and End don't have weather
        if (dimension == Level.NETHER || dimension == Level.END) {
            return new WeatherGeneratorVanilla(level);
        }

        // Overworld and custom dimensions use standard generator
        return new WeatherGenerator(level);
    }

    /**
     * Gets the weather data for a specific dimension.
     *
     * @param level The server level
     * @return The weather data, or null if no generator exists
     */
    public static WeatherGenerator.WeatherData getWeatherData(ServerLevel level) {
        WeatherGenerator generator = generators.get(level.dimension());
        return generator != null ? generator.getData() : null;
    }

    /**
     * Clears all weather generators.
     * Called when the server stops or world unloads.
     */
    public static void clear() {
        LOGGER.info("Clearing weather generators for %d dimensions", generators.size());
        generators.clear();
    }

    /**
     * Handles server tick events to update weather generators.
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        // Only process at the end of the tick
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        MinecraftServer server = event.getServer();
        if (server == null) {
            return;
        }

        // Update weather generators for all loaded dimensions
        for (ServerLevel level : server.getAllLevels()) {
            WeatherGenerator generator = getOrCreateGenerator(level);
            generator.update();

            // Send weather update to clients
            sendWeatherUpdate(level, generator);

            // Send thunder events if needed
            sendThunderEvents(level, generator);
        }
    }

    /**
     * Sends weather update packets to all players in the dimension.
     */
    private static void sendWeatherUpdate(ServerLevel level, WeatherGenerator generator) {
        // Only send if there are players in the dimension
        if (level.players().isEmpty()) {
            return;
        }

        WeatherGenerator.WeatherData data = generator.getData();

        PacketWeatherUpdate packet = new PacketWeatherUpdate(
                level.dimension().location(),
                data.getCurrentRainIntensity(),
                data.getRainIntensity(),
                (int) (level.getRainLevel(1.0F) * 100),  // Convert float to int (0-100 scale)
                level.getThunderLevel(1.0F),
                (int) (level.getThunderLevel(1.0F) * 100),  // Convert float to int (0-100 scale)
                data.getThunderTimer()
        );

        WeatherNetwork.sendWeatherUpdate(level, packet);
    }

    /**
     * Sends thunder event packets if a thunder event occurred.
     */
    private static void sendThunderEvents(ServerLevel level, WeatherGenerator generator) {
        WeatherGenerator.WeatherData data = generator.getData();

        // Check if a thunder event just occurred (timer was just reset)
        if (data.getThunderTimer() > 0 && data.getLastThunderPos() != null) {
            BlockPos pos = data.getLastThunderPos();

            // Only send if position is valid
            if (!pos.equals(BlockPos.ZERO)) {
                PacketThunder packet = new PacketThunder(
                        level.dimension().location(),
                        data.isLastThunderFlash(),
                        pos
                );

                WeatherNetwork.sendThunder(level, packet);

                // Clear the position to avoid sending duplicate events
                data.setLastThunderPos(BlockPos.ZERO);
            }
        }
    }

    /**
     * Gets diagnostic information about all weather generators.
     *
     * @return Diagnostic string
     */
    public static String getDiagnostics() {
        StringBuilder sb = new StringBuilder();
        sb.append("Weather Generators (").append(generators.size()).append("):\n");

        generators.forEach((dimension, generator) -> {
            WeatherGenerator.WeatherData data = generator.getData();
            sb.append(String.format("  %s [%s]: intensity=%.2f, current=%.2f, thunder=%d\n",
                    dimension.location(),
                    generator.name(),
                    data.getRainIntensity(),
                    data.getCurrentRainIntensity(),
                    data.getThunderTimer()));
        });

        return sb.toString();
    }
}
