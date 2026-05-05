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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.dsurround.Constants;
import org.orecruncher.dsurround.capabilities.entityfx.EntityFXData;
import org.orecruncher.dsurround.capabilities.entityfx.IEntityFX;
import org.orecruncher.dsurround.capabilities.season.ISeasonInfo;
import org.orecruncher.dsurround.capabilities.season.SeasonInfo;
import org.orecruncher.dsurround.lib.logging.ModLog;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Client-only capability handler for client-specific capabilities.
 * This class is only loaded on the client side to avoid server crashes.
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientCapabilityHandler {

    private static final ModLog LOGGER = new ModLog(Constants.MOD_ID + ".clientcap");

    public static final Capability<ISeasonInfo> SEASON_INFO = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<IEntityFX> ENTITY_FX = CapabilityManager.get(new CapabilityToken<>() {});

    private static final ResourceLocation SEASON_INFO_ID = new ResourceLocation(Constants.MOD_ID, "season_info");
    private static final ResourceLocation ENTITY_FX_ID = new ResourceLocation(Constants.MOD_ID, "entity_fx");

    @SubscribeEvent
    public static void attachLevelCapabilities(AttachCapabilitiesEvent<Level> event) {
        Level level = event.getObject();

        if (level.isClientSide) {
            SeasonInfo seasonInfo = SeasonInfo.factory(level);
            event.addCapability(SEASON_INFO_ID, new SeasonInfoProvider(seasonInfo));
            LOGGER.debug("Attached season info capability to level {}", level.dimension().location());
        }
    }

    @SubscribeEvent
    public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();

        if (entity.level().isClientSide && entity instanceof LivingEntity) {
            EntityFXData entityFX = new EntityFXData();
            event.addCapability(ENTITY_FX_ID, new EntityFXProvider(entityFX));
        }
    }

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

    @Nullable
    public static ISeasonInfo getSeasonInfo(@Nonnull Level level) {
        return level.getCapability(SEASON_INFO).orElse(null);
    }

    @Nullable
    public static IEntityFX getEntityFX(@Nonnull Entity entity) {
        return entity.getCapability(ENTITY_FX).orElse(null);
    }
}