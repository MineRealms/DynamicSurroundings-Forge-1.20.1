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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import org.orecruncher.dsurround.weather.tracker.ServerDrivenTracker;

import java.util.function.Supplier;

/**
 * Network packet for synchronizing weather state from server to client.
 * Contains all weather-related data needed for client-side rendering and effects.
 */
public class PacketWeatherUpdate {

    private final ResourceLocation dimension;
    private final float intensity;
    private final float maxIntensity;
    private final int nextRainChange;
    private final float thunderStrength;
    private final int thunderChange;
    private final int thunderEvent;

    public PacketWeatherUpdate(
            ResourceLocation dimension,
            float intensity,
            float maxIntensity,
            int nextRainChange,
            float thunderStrength,
            int thunderChange,
            int thunderEvent) {
        this.dimension = dimension;
        this.intensity = intensity;
        this.maxIntensity = maxIntensity;
        this.nextRainChange = nextRainChange;
        this.thunderStrength = thunderStrength;
        this.thunderChange = thunderChange;
        this.thunderEvent = thunderEvent;
    }

    /**
     * Encodes the packet data to the buffer.
     */
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(dimension);
        buf.writeFloat(intensity);
        buf.writeFloat(maxIntensity);
        buf.writeInt(nextRainChange);
        buf.writeFloat(thunderStrength);
        buf.writeInt(thunderChange);
        buf.writeInt(thunderEvent);
    }

    /**
     * Decodes the packet data from the buffer.
     */
    public static PacketWeatherUpdate decode(FriendlyByteBuf buf) {
        ResourceLocation dimension = buf.readResourceLocation();
        float intensity = buf.readFloat();
        float maxIntensity = buf.readFloat();
        int nextRainChange = buf.readInt();
        float thunderStrength = buf.readFloat();
        int thunderChange = buf.readInt();
        int thunderEvent = buf.readInt();

        return new PacketWeatherUpdate(
                dimension,
                intensity,
                maxIntensity,
                nextRainChange,
                thunderStrength,
                thunderChange,
                thunderEvent
        );
    }

    /**
     * Handles the packet on the client side.
     */
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Update the server-driven tracker with the new weather data
            ServerDrivenTracker.updateFromServer(
                    dimension,
                    intensity,
                    maxIntensity,
                    nextRainChange,
                    thunderStrength,
                    thunderChange,
                    thunderEvent
            );
        });
        context.setPacketHandled(true);
    }

    public ResourceLocation getDimension() {
        return dimension;
    }

    public float getIntensity() {
        return intensity;
    }

    public float getMaxIntensity() {
        return maxIntensity;
    }

    public int getNextRainChange() {
        return nextRainChange;
    }

    public float getThunderStrength() {
        return thunderStrength;
    }

    public int getThunderChange() {
        return thunderChange;
    }

    public int getThunderEvent() {
        return thunderEvent;
    }
}
