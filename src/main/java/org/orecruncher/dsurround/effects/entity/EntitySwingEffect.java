package org.orecruncher.dsurround.effects.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.orecruncher.dsurround.Configuration;
import org.orecruncher.dsurround.Client;
import org.orecruncher.dsurround.items.IItemData;
import org.orecruncher.dsurround.items.ItemLibrary;

/**
 * Entity effect that plays sounds when entities swing items.
 * Detects swing animation and plays appropriate sound based on item type.
 */
public class EntitySwingEffect extends EntityEffect {

    private int swingProgress = 0;
    private boolean isSwinging = false;

    @NotNull
    @Override
    public String name() {
        return "Item Swing";
    }

    @Override
    public void update(@NotNull Entity entity) {
        // Check if swing sounds are enabled
        if (!Client.Config.entityEffects.enableSwingEffect) {
            return;
        }

        // Only process living entities
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        // Check if entity is swinging
        if (livingEntity.swingingArm != null && livingEntity.swingTime > this.swingProgress) {
            if (!this.isSwinging) {
                // Get the item being swung
                ItemStack currentItem = livingEntity.getItemInHand(livingEntity.swingingArm);

                if (!currentItem.isEmpty()) {
                    // Get item data and sound
                    IItemData itemData = ItemLibrary.getInstance().getItemData(currentItem);
                    ResourceLocation soundLocation = itemData.getSwingSound(currentItem);

                    if (soundLocation != null) {
                        // Play the swing sound
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
            }

            this.isSwinging = true;
        } else {
            this.isSwinging = false;
        }

        this.swingProgress = livingEntity.swingTime;
    }
}
