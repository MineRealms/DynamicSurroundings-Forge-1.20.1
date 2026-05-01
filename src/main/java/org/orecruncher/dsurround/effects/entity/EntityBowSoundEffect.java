package org.orecruncher.dsurround.effects.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import org.jetbrains.annotations.NotNull;
import org.orecruncher.dsurround.Configuration;
import org.orecruncher.dsurround.Client;
import org.orecruncher.dsurround.items.IItemData;
import org.orecruncher.dsurround.items.ItemLibrary;

/**
 * Entity effect that plays sounds when using bows, crossbows, or shields.
 * Detects when player starts using these items and plays appropriate sound.
 */
public class EntityBowSoundEffect extends EntityEffect {

    private ItemStack lastActiveStack = ItemStack.EMPTY;

    @NotNull
    @Override
    public String name() {
        return "Bow Sound";
    }

    @Override
    public void update(@NotNull Entity entity) {
        // Check if bow pull sounds are enabled
        if (!Client.Config.entityEffects.enableBowPull) {
            return;
        }

        // Only process living entities
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        // Get the currently active item (being used)
        ItemStack currentStack = livingEntity.getUseItem();

        if (!currentStack.isEmpty()) {
            // Check if this is a new item being used
            if (!ItemStack.isSameItemSameTags(currentStack, this.lastActiveStack)) {
                // Check if it's a bow, crossbow, or shield
                if (currentStack.getItem() instanceof BowItem ||
                    currentStack.getItem() instanceof CrossbowItem ||
                    currentStack.getItem() instanceof ShieldItem) {

                    // Get item data and sound
                    IItemData itemData = ItemLibrary.getInstance().getItemData(currentStack);
                    ResourceLocation soundLocation = itemData.getUseSound(currentStack);

                    if (soundLocation != null) {
                        // Play the use sound
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.level != null) {
                            mc.level.playLocalSound(
                                entity.getX(),
                                entity.getY(),
                                entity.getZ(),
                                net.minecraft.sounds.SoundEvent.createVariableRangeEvent(soundLocation),
                                SoundSource.PLAYERS,
                                0.5F,
                                1.0F,
                                false
                            );
                        }
                    }
                }

                this.lastActiveStack = currentStack.copy();
            }
        } else {
            this.lastActiveStack = ItemStack.EMPTY;
        }
    }
}
