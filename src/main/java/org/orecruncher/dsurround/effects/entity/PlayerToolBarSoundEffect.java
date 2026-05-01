package org.orecruncher.dsurround.effects.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.orecruncher.dsurround.Configuration;
import org.orecruncher.dsurround.Client;
import org.orecruncher.dsurround.items.IItemData;
import org.orecruncher.dsurround.items.ItemLibrary;

/**
 * Entity effect that plays sounds when player changes held items.
 * Tracks both main hand and off hand, including hotbar slot changes.
 */
public class PlayerToolBarSoundEffect extends EntityEffect {

    /**
     * Tracks a single hand (main or off hand)
     */
    protected static class HandTracker {
        protected final InteractionHand hand;
        protected Item lastHeld;

        public HandTracker(@NotNull Player player, @NotNull InteractionHand hand) {
            this.hand = hand;
            this.lastHeld = getItemForHand(player, hand);
        }

        protected Item getItemForHand(@NotNull Player player, @NotNull InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            return stack.getItem();
        }

        protected boolean triggerNewEquipSound(@NotNull Player player) {
            Item heldItem = getItemForHand(player, this.hand);
            return heldItem != this.lastHeld;
        }

        protected void clearState() {
            this.lastHeld = null;
        }

        public void update(@NotNull Player player) {
            if (triggerNewEquipSound(player)) {
                clearState();
                ItemStack currentStack = player.getItemInHand(this.hand);

                if (!currentStack.isEmpty()) {
                    IItemData itemData = ItemLibrary.getInstance().getItemData(currentStack);
                    ResourceLocation soundLocation = itemData.getEquipSound(currentStack);

                    if (soundLocation != null) {
                        // Play the equip sound
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.level != null) {
                            mc.level.playLocalSound(
                                player.getX(),
                                player.getY(),
                                player.getZ(),
                                net.minecraft.sounds.SoundEvent.createVariableRangeEvent(soundLocation),
                                SoundSource.PLAYERS,
                                0.5F,
                                1.0F,
                                false
                            );
                        }
                    }
                }

                this.lastHeld = currentStack.getItem();
            }
        }
    }

    /**
     * Main hand tracker that also monitors hotbar slot changes
     */
    protected static class MainHandTracker extends HandTracker {
        protected int lastSlot;

        public MainHandTracker(@NotNull Player player) {
            super(player, InteractionHand.MAIN_HAND);
            this.lastSlot = player.getInventory().selected;
        }

        @Override
        protected boolean triggerNewEquipSound(@NotNull Player player) {
            return this.lastSlot != player.getInventory().selected || super.triggerNewEquipSound(player);
        }

        @Override
        public void update(@NotNull Player player) {
            super.update(player);
            this.lastSlot = player.getInventory().selected;
        }
    }

    protected final MainHandTracker mainHand;
    protected final HandTracker offHand;

    public PlayerToolBarSoundEffect(@NotNull Player player) {
        this.mainHand = new MainHandTracker(player);
        this.offHand = new HandTracker(player, InteractionHand.OFF_HAND);
    }

    @NotNull
    @Override
    public String name() {
        return "Toolbar";
    }

    @Override
    public void update(@NotNull Entity entity) {
        // Check if toolbar sounds are enabled
        if (!Client.Config.entityEffects.enablePlayerToolbarEffect) {
            return;
        }

        // Only process players
        if (!(entity instanceof Player player)) {
            return;
        }

        this.mainHand.update(player);
        this.offHand.update(player);
    }
}
