package org.orecruncher.dsurround.items;

import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Library for mapping items to their ItemClass and sound data.
 */
public class ItemLibrary {

    private static ItemLibrary INSTANCE;

    private final Map<Item, IItemData> itemDataMap = new HashMap<>();
    private final IItemData defaultItemData = SimpleItemData.CACHE.get(ItemClass.NONE);

    private ItemLibrary() {
        // Will be populated during initialization
    }

    @NotNull
    public static ItemLibrary getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemLibrary();
        }
        return INSTANCE;
    }

    /**
     * Initialize the library with default mappings for vanilla items
     */
    public void initialize() {
        // This will be called during mod initialization
        // For now, we'll use a simple classification system
    }

    /**
     * Get item data for the given item
     */
    @NotNull
    public IItemData getItemData(@NotNull Item item) {
        return this.itemDataMap.getOrDefault(item, getDefaultItemData(item));
    }

    /**
     * Get item data for the given item stack
     */
    @NotNull
    public IItemData getItemData(@NotNull ItemStack stack) {
        if (stack.isEmpty()) {
            return SimpleItemData.CACHE.get(ItemClass.EMPTY);
        }
        return getItemData(stack.getItem());
    }

    /**
     * Get default item data based on item type
     */
    @NotNull
    private IItemData getDefaultItemData(@NotNull Item item) {
        ItemClass itemClass = classifyItem(item);
        return SimpleItemData.CACHE.get(itemClass);
    }

    /**
     * Classify an item based on its type
     */
    @NotNull
    private ItemClass classifyItem(@NotNull Item item) {
        // Armor
        if (item instanceof ArmorItem armorItem) {
            // Use the armor material's name to determine class
            String materialName = armorItem.getMaterial().toString().toLowerCase();
            if (materialName.contains("leather")) return ItemClass.LEATHER;
            if (materialName.contains("chainmail") || materialName.contains("chain")) return ItemClass.CHAIN;
            if (materialName.contains("iron")) return ItemClass.IRON;
            if (materialName.contains("gold")) return ItemClass.GOLD;
            if (materialName.contains("diamond")) return ItemClass.DIAMOND;
            if (materialName.contains("netherite")) return ItemClass.NETHERITE;
            return ItemClass.NONE;
        }

        // Weapons and tools
        if (item instanceof SwordItem) return ItemClass.SWORD;
        if (item instanceof AxeItem) return ItemClass.AXE;
        if (item instanceof BowItem) return ItemClass.BOW;
        if (item instanceof CrossbowItem) return ItemClass.CROSSBOW;
        if (item instanceof ShieldItem) return ItemClass.SHIELD;
        if (item instanceof TieredItem) return ItemClass.TOOL; // Pickaxe, Shovel, Hoe

        // Other items
        if (item.isEdible()) return ItemClass.FOOD;
        if (item instanceof BookItem || item instanceof EnchantedBookItem || item instanceof WritableBookItem || item instanceof WrittenBookItem) {
            return ItemClass.BOOK;
        }
        if (item instanceof PotionItem || item instanceof SplashPotionItem || item instanceof LingeringPotionItem) {
            return ItemClass.POTION;
        }

        return ItemClass.NONE;
    }

    /**
     * Register custom item data
     */
    public void register(@NotNull Item item, @NotNull IItemData data) {
        this.itemDataMap.put(item, data);
    }

    /**
     * Clear all mappings
     */
    public void clear() {
        this.itemDataMap.clear();
    }
}
