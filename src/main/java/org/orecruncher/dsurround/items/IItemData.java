package org.orecruncher.dsurround.items;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for item data that provides sound information for items.
 */
public interface IItemData {

    /**
     * Get the item class for this item
     */
    @NotNull
    ItemClass getItemClass();

    /**
     * Get the sound to play when equipping this item
     */
    @Nullable
    ResourceLocation getEquipSound(@NotNull ItemStack stack);

    /**
     * Get the sound to play when swinging this item
     */
    @Nullable
    ResourceLocation getSwingSound(@NotNull ItemStack stack);

    /**
     * Get the sound to play when using this item (bow pull, shield block, etc.)
     */
    @Nullable
    ResourceLocation getUseSound(@NotNull ItemStack stack);
}
