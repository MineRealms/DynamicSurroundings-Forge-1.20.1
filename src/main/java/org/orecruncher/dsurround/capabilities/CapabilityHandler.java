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

package org.orecruncher.dsurround.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.dsurround.Constants;
import org.orecruncher.dsurround.capabilities.dimension.DimensionInfo;
import org.orecruncher.dsurround.capabilities.dimension.IDimensionInfo;
import org.orecruncher.dsurround.capabilities.entitydata.EntityData;
import org.orecruncher.dsurround.capabilities.entitydata.IEntityData;
import org.orecruncher.dsurround.capabilities.entityfx.EntityFXData;
import org.orecruncher.dsurround.capabilities.entityfx.IEntityFX;
import org.orecruncher.dsurround.capabilities.season.ISeasonInfo;
import org.orecruncher.dsurround.capabilities.season.SeasonInfo;
import org.orecruncher.dsurround.lib.logging.ModLog;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Central registry for all Dynamic Surroundings capabilities.
 * Handles registration and attachment of capabilities to entities and levels.
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityHandler {

    private static final ModLog LOGGER = new ModLog(Constants.MOD_ID);

    // Capability instances
    public static final Capability<IDimensionInfo> DIMENSION_INFO = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<ISeasonInfo> SEASON_INFO = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<IEntityData> ENTITY_DATA = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<IEntityFX> ENTITY_FX = CapabilityManager.get(new CapabilityToken<>() {});

    // Resource locations for capability IDs
    private static final ResourceLocation DIMENSION_INFO_ID = new ResourceLocation(Constants.MOD_ID, "dimension_info");
    private static final ResourceLocation SEASON_INFO_ID = new ResourceLocation(Constants.MOD_ID, "season_info");
    private static final ResourceLocation ENTITY_DATA_ID = new ResourceLocation(Constants.MOD_ID, "entity_data");
    private static final ResourceLocation ENTITY_FX_ID = new ResourceLocation(Constants.MOD_ID, "entity_fx");

    /**
     * Attach capabilities to Level (dimension info and season info).
     */
    @SubscribeEvent
    public static void attachLevelCapabilities(AttachCapabilitiesEvent<Level> event) {
        Level level = event.getObject();

        // Attach DimensionInfo (both client and server)
        DimensionInfo dimInfo = new DimensionInfo(level);
        event.addCapability(DIMENSION_INFO_ID, new DimensionInfoProvider(dimInfo));

        // Attach SeasonInfo (client only)
        if (level.isClientSide) {
            SeasonInfo seasonInfo = SeasonInfo.factory(level);
            event.addCapability(SEASON_INFO_ID, new SeasonInfoProvider(seasonInfo));
        }
    }

    /**
     * Attach capabilities to Entity (entity data and entity FX).
     */
    @SubscribeEvent
    public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();

        // Attach EntityData to Mob entities (both client and server)
        if (entity instanceof Mob mob) {
            EntityData entityData = new EntityData(mob);
            event.addCapability(ENTITY_DATA_ID, new EntityDataProvider(entityData));
        }

        // Attach EntityFX to all living entities (client only)
        if (entity.level().isClientSide && entity instanceof net.minecraft.world.entity.LivingEntity) {
            EntityFXData entityFX = new EntityFXData();
            event.addCapability(ENTITY_FX_ID, new EntityFXProvider(entityFX));
        }
    }

    // ===================================
    // Capability Providers
    // ===================================

    /**
     * Provider for DimensionInfo capability.
     */
    private static class DimensionInfoProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        private final DimensionInfo instance;
        private final LazyOptional<IDimensionInfo> holder;

        public DimensionInfoProvider(DimensionInfo instance) {
            this.instance = instance;
            this.holder = LazyOptional.of(() -> instance);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return DIMENSION_INFO.orEmpty(cap, holder);
        }

        @Override
        public CompoundTag serializeNBT() {
            return instance.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            instance.deserializeNBT(nbt);
        }
    }

    /**
     * Provider for SeasonInfo capability (no serialization needed).
     */
    private static class SeasonInfoProvider implements ICapabilityProvider {
        private final SeasonInfo instance;
        private final LazyOptional<ISeasonInfo> holder;

        public SeasonInfoProvider(SeasonInfo instance) {
            this.instance = instance;
            this.holder = LazyOptional.of(() -> instance);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return SEASON_INFO.orEmpty(cap, holder);
        }
    }

    /**
     * Provider for EntityData capability.
     */
    private static class EntityDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        private final EntityData instance;
        private final LazyOptional<IEntityData> holder;

        public EntityDataProvider(EntityData instance) {
            this.instance = instance;
            this.holder = LazyOptional.of(() -> instance);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return ENTITY_DATA.orEmpty(cap, holder);
        }

        @Override
        public CompoundTag serializeNBT() {
            return instance.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            instance.deserializeNBT(nbt);
        }
    }

    /**
     * Provider for EntityFX capability (no serialization needed).
     */
    private static class EntityFXProvider implements ICapabilityProvider {
        private final EntityFXData instance;
        private final LazyOptional<IEntityFX> holder;

        public EntityFXProvider(EntityFXData instance) {
            this.instance = instance;
            this.holder = LazyOptional.of(() -> instance);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return ENTITY_FX.orEmpty(cap, holder);
        }
    }

    // ===================================
    // Helper methods to get capabilities
    // ===================================

    /**
     * Gets the DimensionInfo capability from a level.
     */
    @Nullable
    public static IDimensionInfo getDimensionInfo(@Nonnull Level level) {
        return level.getCapability(DIMENSION_INFO).orElse(null);
    }

    /**
     * Gets the SeasonInfo capability from a level.
     */
    @Nullable
    public static ISeasonInfo getSeasonInfo(@Nonnull Level level) {
        return level.getCapability(SEASON_INFO).orElse(null);
    }

    /**
     * Gets the EntityData capability from an entity.
     */
    @Nullable
    public static IEntityData getEntityData(@Nonnull Entity entity) {
        return entity.getCapability(ENTITY_DATA).orElse(null);
    }

    /**
     * Gets the EntityFX capability from an entity.
     */
    @Nullable
    public static IEntityFX getEntityFX(@Nonnull Entity entity) {
        return entity.getCapability(ENTITY_FX).orElse(null);
    }
}
