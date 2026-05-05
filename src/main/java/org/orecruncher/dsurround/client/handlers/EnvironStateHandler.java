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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.dsurround.capabilities.CapabilityHandler;
import org.orecruncher.dsurround.capabilities.ClientCapabilityHandler;
import org.orecruncher.dsurround.capabilities.dimension.IDimensionInfo;
import org.orecruncher.dsurround.capabilities.season.ISeasonInfo;
import org.orecruncher.dsurround.capabilities.season.TemperatureRating;

import javax.annotation.Nonnull;

/**
 * Tracks and provides access to environmental state information.
 * This handler runs first each tick to gather state that other handlers depend on.
 */
@OnlyIn(Dist.CLIENT)
public class EnvironStateHandler extends EffectHandlerBase {

    /**
     * Internal data structure holding all environmental state.
     */
    protected static class EnvironStateData {
        // Dimension info
        public IDimensionInfo dimInfo;
        public String dimensionName = "";

        // Position and location
        public BlockPos playerPosition = BlockPos.ZERO;
        public boolean inside;
        public boolean isUnderground;
        public boolean isInSpace;
        public boolean isInClouds;
        public int lightLevel;

        // Temperature and weather
        public TemperatureRating playerTemperature = TemperatureRating.MILD;
        public TemperatureRating biomeTemperature = TemperatureRating.MILD;

        // Equipment
        public ItemStack armorStack = ItemStack.EMPTY;
        public ItemStack footArmorStack = ItemStack.EMPTY;

        // Village
        public boolean inVillage;

        // Timing
        public int tickCounter = 1;
    }

    /**
     * Public API for accessing environmental state.
     * Other handlers and systems query this class for current state.
     */
    public static class EnvironState {
        private static EnvironStateData data = new EnvironStateData();

        public static IDimensionInfo getDimensionInfo() {
            return data.dimInfo;
        }

        public static String getDimensionName() {
            return data.dimensionName;
        }

        public static ResourceLocation getDimensionId() {
            Level world = getWorld();
            return world != null ? world.dimension().location() : null;
        }

        public static Player getPlayer() {
            return Minecraft.getInstance().player;
        }

        public static Level getWorld() {
            return Minecraft.getInstance().level;
        }

        public static long getTimeOfDay() {
            Level world = getWorld();
            return world != null ? world.getDayTime() % 24000L : 0;
        }

        public static Biome getPlayerBiome() {
            Level world = getWorld();
            if (world == null) return null;
            return world.getBiome(getPlayerPosition()).value();
        }

        public static BlockPos getPlayerPosition() {
            return data.playerPosition;
        }

        public static TemperatureRating getPlayerTemperature() {
            return data.playerTemperature;
        }

        public static TemperatureRating getBiomeTemperature() {
            return data.biomeTemperature;
        }

        public static boolean isPlayerInside() {
            return data.inside;
        }

        public static boolean isPlayerUnderground() {
            return data.isUnderground;
        }

        public static boolean isPlayerInSpace() {
            return data.isInSpace;
        }

        public static boolean isPlayerInClouds() {
            return data.isInClouds;
        }

        public static ItemStack getPlayerItemStack() {
            return data.armorStack;
        }

        public static ItemStack getPlayerFootArmorStack() {
            return data.footArmorStack;
        }

        public static boolean inVillage() {
            return data.inVillage;
        }

        public static void setInVillage(boolean flag) {
            data.inVillage = flag;
        }

        public static int getLightLevel() {
            return data.lightLevel;
        }

        public static int getTickCounter() {
            return data.tickCounter;
        }

        public static float getPartialTick() {
            return Minecraft.getInstance().getPartialTick();
        }

        // Player state queries
        public static boolean isPlayerHurt() {
            Player player = getPlayer();
            if (player == null) return false;
            return player.getHealth() <= (0.4f * player.getMaxHealth());
        }

        public static boolean isPlayerHungry() {
            Player player = getPlayer();
            if (player == null) return false;
            return !player.isCreative() && player.getFoodData().getFoodLevel() <= 6;
        }

        public static boolean isPlayerBurning() {
            Player player = getPlayer();
            return player != null && player.isOnFire();
        }

        public static boolean isPlayerSuffocating() {
            Player player = getPlayer();
            if (player == null) return false;
            return !player.isCreative() && player.getAirSupply() <= 0;
        }

        public static boolean isPlayerFlying() {
            Player player = getPlayer();
            return player != null && player.getAbilities().flying;
        }

        public static boolean isPlayerSprinting() {
            Player player = getPlayer();
            return player != null && player.isSprinting();
        }

        public static boolean isPlayerInLava() {
            Player player = getPlayer();
            return player != null && player.isInLava();
        }

        public static boolean isPlayerInvisible() {
            Player player = getPlayer();
            return player != null && player.isInvisible();
        }

        public static boolean isPlayerInWater() {
            Player player = getPlayer();
            return player != null && player.isInWater();
        }

        public static boolean isPlayerMoving() {
            Player player = getPlayer();
            if (player == null) return false;
            return player.walkDist != player.walkDistO;
        }

        protected static void setData(EnvironStateData newData) {
            data = newData;
        }

        protected static EnvironStateData getData() {
            return data;
        }
    }

    public EnvironStateHandler() {
        super("Environment State Handler");
    }

    @Override
    public void process(@Nonnull Player player) {
        EnvironStateData data = EnvironState.getData();
        Level world = player.level();

        // Advance tick counter if not paused
        if (!Minecraft.getInstance().isPaused()) {
            data.tickCounter++;
        }

        // Get dimension info from capability
        data.dimInfo = CapabilityHandler.getDimensionInfo(world);
        data.dimensionName = world.dimension().location().toString();

        // Player position
        data.playerPosition = player.blockPosition();

        // Get season info for temperature
        ISeasonInfo seasonInfo = ClientCapabilityHandler.getSeasonInfo(world);
        if (seasonInfo != null) {
            data.playerTemperature = seasonInfo.getPlayerTemperature();
            data.biomeTemperature = seasonInfo.getBiomeTemperature(data.playerPosition);
        } else {
            data.playerTemperature = TemperatureRating.MILD;
            data.biomeTemperature = TemperatureRating.MILD;
        }

        // Equipment
        data.armorStack = getEffectiveArmorStack(player);
        data.footArmorStack = getFootArmorStack(player);

        // Light level
        int blockLight = world.getBrightness(LightLayer.BLOCK, data.playerPosition);
        int skyLight = world.getBrightness(LightLayer.SKY, data.playerPosition);
        data.lightLevel = Math.max(blockLight, skyLight);

        // Inside/outside detection (simplified for now)
        data.inside = !world.canSeeSky(data.playerPosition);

        // Underground/space/clouds detection (placeholder - needs biome registry)
        data.isUnderground = data.playerPosition.getY() < 50 && data.inside;
        data.isInSpace = data.playerPosition.getY() > 256;
        data.isInClouds = data.playerPosition.getY() > 128 && data.playerPosition.getY() < 196;
    }

    /**
     * Gets the effective armor stack for sound/visual effects.
     */
    private ItemStack getEffectiveArmorStack(Player player) {
        // Check chest armor first, then legs, then head
        ItemStack chest = player.getInventory().getArmor(2);
        if (!chest.isEmpty()) return chest;

        ItemStack legs = player.getInventory().getArmor(1);
        if (!legs.isEmpty()) return legs;

        ItemStack head = player.getInventory().getArmor(3);
        if (!head.isEmpty()) return head;

        return ItemStack.EMPTY;
    }

    /**
     * Gets the foot armor stack.
     */
    private ItemStack getFootArmorStack(Player player) {
        return player.getInventory().getArmor(0); // Boots
    }

    @Override
    public void onConnect() {
        reset();
    }

    @Override
    public void onDisconnect() {
        reset();
    }

    private void reset() {
        EnvironState.setData(new EnvironStateData());
    }
}
