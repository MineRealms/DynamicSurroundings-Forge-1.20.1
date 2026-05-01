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

package org.orecruncher.dsurround.weather.network;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.orecruncher.dsurround.Constants;
import org.orecruncher.dsurround.lib.logging.IModLog;
import org.orecruncher.dsurround.lib.logging.ModLog;

/**
 * Network channel manager for weather-related packets.
 * Handles registration and distribution of weather synchronization packets.
 */
public class WeatherNetwork {

    private static final IModLog LOGGER = new ModLog("WeatherNetwork");

    private static final String PROTOCOL_VERSION = "1";
    private static final ResourceLocation CHANNEL_NAME = new ResourceLocation(Constants.MOD_ID, "weather");

    private static SimpleChannel CHANNEL;
    private static int packetId = 0;

    /**
     * Initializes the network channel and registers packets.
     */
    public static void initialize() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                CHANNEL_NAME,
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        // Register weather update packet (server -> client)
        CHANNEL.messageBuilder(PacketWeatherUpdate.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketWeatherUpdate::encode)
                .decoder(PacketWeatherUpdate::decode)
                .consumerMainThread(PacketWeatherUpdate::handle)
                .add();

        // Register thunder packet (server -> client)
        CHANNEL.messageBuilder(PacketThunder.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketThunder::encode)
                .decoder(PacketThunder::decode)
                .consumerMainThread(PacketThunder::handle)
                .add();

        LOGGER.info("Weather network channel initialized");
    }

    private static int nextId() {
        return packetId++;
    }

    /**
     * Sends a weather update packet to all players in a dimension.
     *
     * @param level The server level
     * @param packet The weather update packet
     */
    public static void sendWeatherUpdate(ServerLevel level, PacketWeatherUpdate packet) {
        CHANNEL.send(PacketDistributor.DIMENSION.with(level::dimension), packet);
    }

    /**
     * Sends a thunder packet to all players in a dimension.
     *
     * @param level The server level
     * @param packet The thunder packet
     */
    public static void sendThunder(ServerLevel level, PacketThunder packet) {
        CHANNEL.send(PacketDistributor.DIMENSION.with(level::dimension), packet);
    }

    /**
     * Sends a weather update packet to a specific player.
     *
     * @param player The target player
     * @param packet The weather update packet
     */
    public static void sendWeatherUpdateToPlayer(ServerPlayer player, PacketWeatherUpdate packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    /**
     * Sends a thunder packet to a specific player.
     *
     * @param player The target player
     * @param packet The thunder packet
     */
    public static void sendThunderToPlayer(ServerPlayer player, PacketThunder packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
