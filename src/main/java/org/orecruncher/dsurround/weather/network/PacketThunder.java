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

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.orecruncher.dsurround.weather.thunder.ThunderManager;

import java.util.function.Supplier;

/**
 * Network packet for synchronizing thunder events from server to client.
 * Triggers thunder sounds and lightning flashes on the client.
 */
public class PacketThunder {

    private final ResourceLocation dimension;
    private final boolean doFlash;
    private final BlockPos position;

    public PacketThunder(ResourceLocation dimension, boolean doFlash, BlockPos position) {
        this.dimension = dimension;
        this.doFlash = doFlash;
        this.position = position.immutable();
    }

    /**
     * Encodes the packet data to the buffer.
     */
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(dimension);
        buf.writeBoolean(doFlash);
        buf.writeBlockPos(position);
    }

    /**
     * Decodes the packet data from the buffer.
     */
    public static PacketThunder decode(FriendlyByteBuf buf) {
        ResourceLocation dimension = buf.readResourceLocation();
        boolean doFlash = buf.readBoolean();
        BlockPos position = buf.readBlockPos();

        return new PacketThunder(dimension, doFlash, position);
    }

    /**
     * Handles the packet on the client side.
     */
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Trigger thunder event on client
            ThunderManager.handleServerThunder(dimension, doFlash, position);
        });
        context.setPacketHandled(true);
    }

    public ResourceLocation getDimension() {
        return dimension;
    }

    public boolean shouldFlash() {
        return doFlash;
    }

    public BlockPos getPosition() {
        return position;
    }
}
