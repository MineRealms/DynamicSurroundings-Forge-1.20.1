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

package org.orecruncher.dsurround.capabilities.entitydata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import org.orecruncher.dsurround.Constants;
import org.orecruncher.dsurround.lib.logging.ModLog;

import javax.annotation.Nonnull;

/**
 * Implementation of entity behavioral data.
 * Tracks attacking and fleeing states for entities.
 */
public final class EntityData implements IEntityDataSettable {

    private static final ModLog LOGGER = new ModLog(Constants.MOD_ID);

    public static final int NO_ENTITY = -1;

    private final Mob entity;
    private boolean isAttacking;
    private boolean isFleeing;
    private boolean needsSync;

    public EntityData() {
        this.entity = null;
    }

    public EntityData(@Nonnull Mob entity) {
        this.entity = entity;
    }

    @Override
    public Mob getEntity() {
        return this.entity;
    }

    @Override
    public int getEntityId() {
        return this.entity != null ? this.entity.getId() : NO_ENTITY;
    }

    @Override
    public boolean isAttacking() {
        return this.isAttacking;
    }

    @Override
    public void setAttacking(boolean flag) {
        this.needsSync = (this.isAttacking != flag) | this.needsSync;
        this.isAttacking = flag;
    }

    @Override
    public boolean isFleeing() {
        return this.isFleeing;
    }

    @Override
    public void setFleeing(boolean flag) {
        this.needsSync = (this.isFleeing != flag) | this.needsSync;
        this.isFleeing = flag;
    }

    public boolean needsSync() {
        return this.needsSync;
    }

    private void clearSync() {
        this.needsSync = false;
    }

    @Override
    public void sync() {
        if (needsSync() && this.entity != null && !this.entity.level().isClientSide) {
            // TODO: Send network packet to sync to clients
            // Network.sendToEntityViewers(this.entity, new PacketEntityData(this));
            clearSync();
        }
    }

    @Override
    @Nonnull
    public CompoundTag serializeNBT() {
        final CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("attacking", isAttacking());
        nbt.putBoolean("fleeing", isFleeing());
        return nbt;
    }

    @Override
    public void deserializeNBT(@Nonnull CompoundTag nbt) {
        setAttacking(nbt.getBoolean("attacking"));
        setFleeing(nbt.getBoolean("fleeing"));
    }
}
