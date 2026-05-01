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

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.dsurround.Constants;
import org.orecruncher.dsurround.capabilities.entitydata.EntityDataTables;
import org.orecruncher.dsurround.capabilities.entitydata.IEntityDataSettable;

/**
 * Event handler for updating entity capabilities during entity ticks.
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityCapabilityEvents {

    /**
     * Called when an entity is being updated.
     * Evaluates entity AI states and syncs to clients if needed.
     */
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        Entity entity = event.getEntity();

        // Only process on server side, and only every 5 ticks (4 times per second)
        if (entity.level().isClientSide || (entity.tickCount % 5) != 0) {
            return;
        }

        // Get the entity data capability
        var data = CapabilityHandler.getEntityData(entity);
        if (data instanceof IEntityDataSettable settable) {
            // Assess the entity's current state
            EntityDataTables.assess(settable);
            // Sync to clients if state changed
            settable.sync();
        }
    }
}
