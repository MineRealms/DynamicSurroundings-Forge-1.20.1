package org.orecruncher.dsurround.items;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple default item data implementation that uses ItemClass settings.
 * Provides reusable singletons via CACHE.
 */
public class SimpleItemData implements IItemData {

    public static final Map<ItemClass, SimpleItemData> CACHE = new HashMap<>();

    static {
        for (ItemClass ic : ItemClass.values()) {
            CACHE.put(ic, new SimpleItemData(ic));
        }
    }

    protected final ItemClass itemClass;

    public SimpleItemData(@NotNull ItemClass itemClass) {
        this.itemClass = itemClass;
    }

    @NotNull
    @Override
    public ItemClass getItemClass() {
        return this.itemClass;
    }

    @Nullable
    @Override
    public ResourceLocation getEquipSound(@NotNull ItemStack stack) {
        return this.itemClass.getEquipSound();
    }

    @Nullable
    @Override
    public ResourceLocation getSwingSound(@NotNull ItemStack stack) {
        return this.itemClass.getSwingSound();
    }

    @Nullable
    @Override
    public ResourceLocation getUseSound(@NotNull ItemStack stack) {
        return this.itemClass.getUseSound();
    }
}
