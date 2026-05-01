package org.orecruncher.dsurround.items;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;

/**
 * Classification of items for sound effects.
 * Each class defines the sounds played when swinging, using, or equipping the item.
 */
public enum ItemClass {

    EMPTY(null, null, null, false),
    NONE(null, null, "item.armor.equip_generic", false),

    // Armor types
    LEATHER("item.armor.equip_leather", true),
    CHAIN("item.armor.equip_chain", true),
    IRON("item.armor.equip_iron", true),
    GOLD("item.armor.equip_gold", true),
    DIAMOND("item.armor.equip_diamond", true),
    NETHERITE("item.armor.equip_netherite", true),

    // Weapons and tools
    SHIELD("entity.player.attack.sweep", "item.shield.block", "item.armor.equip_generic", false),
    SWORD("entity.player.attack.sweep", null, "item.armor.equip_generic", false),
    AXE("entity.player.attack.sweep", null, "item.armor.equip_generic", false),
    BOW("entity.player.attack.sweep", "item.crossbow.loading_start", "item.armor.equip_generic", false),
    CROSSBOW("entity.player.attack.sweep", "item.crossbow.loading_start", "item.armor.equip_generic", false),
    TOOL("entity.player.attack.sweep", null, "item.armor.equip_generic", false),

    // Other items
    FOOD(null, null, "item.armor.equip_generic", false),
    BOOK(null, null, "item.book.page_turn", false),
    POTION(null, null, "item.bottle.fill", false);

    private final String swingSound;
    private final String useSound;
    private final String equipSound;
    private final boolean isArmor;

    ItemClass(@Nullable String sound) {
        this(sound, sound, sound, false);
    }

    ItemClass(@Nullable String sound, boolean isArmor) {
        this(sound, sound, sound, isArmor);
    }

    ItemClass(@Nullable String swingSound, @Nullable String useSound, @Nullable String equipSound, boolean isArmor) {
        this.swingSound = swingSound;
        this.useSound = useSound;
        this.equipSound = equipSound;
        this.isArmor = isArmor;
    }

    @Nullable
    public ResourceLocation getSwingSound() {
        return this.swingSound != null ? new ResourceLocation(this.swingSound) : null;
    }

    @Nullable
    public ResourceLocation getUseSound() {
        return this.useSound != null ? new ResourceLocation(this.useSound) : null;
    }

    @Nullable
    public ResourceLocation getEquipSound() {
        return this.equipSound != null ? new ResourceLocation(this.equipSound) : null;
    }

    public boolean isArmor() {
        return this.isArmor;
    }
}
