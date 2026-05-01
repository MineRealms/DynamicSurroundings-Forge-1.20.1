package org.orecruncher.dsurround.lib.footsteps;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.orecruncher.dsurround.Client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles armor sound effects (jingling) when players move with armor equipped.
 * Different armor materials produce different sounds and volumes.
 */
public class ArmorSoundHandler {
    private static ArmorSoundHandler INSTANCE;

    // Per-player timing for armor sounds
    private final Map<UUID, ArmorTiming> playerTimings;

    // Armor material to sound mapping
    private final Map<ArmorMaterial, SoundEvent> armorSounds;

    // Minimum time between armor sounds (in milliseconds)
    private static final long MIN_ARMOR_SOUND_INTERVAL = 400;

    private ArmorSoundHandler() {
        this.playerTimings = new HashMap<>();
        this.armorSounds = new HashMap<>();
        initializeArmorSounds();
    }

    @NotNull
    public static ArmorSoundHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ArmorSoundHandler();
        }
        return INSTANCE;
    }

    /**
     * Initialize armor material to sound mappings
     */
    private void initializeArmorSounds() {
        // Leather - soft rustling
        this.armorSounds.put(ArmorMaterials.LEATHER, SoundEvents.ARMOR_EQUIP_LEATHER);

        // Chain - metallic jingling
        this.armorSounds.put(ArmorMaterials.CHAIN, SoundEvents.ARMOR_EQUIP_CHAIN);

        // Iron - heavier metallic
        this.armorSounds.put(ArmorMaterials.IRON, SoundEvents.ARMOR_EQUIP_IRON);

        // Gold - lighter metallic
        this.armorSounds.put(ArmorMaterials.GOLD, SoundEvents.ARMOR_EQUIP_GOLD);

        // Diamond - crystalline
        this.armorSounds.put(ArmorMaterials.DIAMOND, SoundEvents.ARMOR_EQUIP_DIAMOND);

        // Netherite - deep metallic
        this.armorSounds.put(ArmorMaterials.NETHERITE, SoundEvents.ARMOR_EQUIP_NETHERITE);

        // Turtle - unique sound
        this.armorSounds.put(ArmorMaterials.TURTLE, SoundEvents.ARMOR_EQUIP_TURTLE);
    }

    /**
     * Called when a player moves. Determines if armor sounds should play.
     */
    public void onPlayerMove(@NotNull Player player, float movementSpeed) {
        // Check if armor sounds are enabled
        if (!Client.Config.footsteps.armorSounds) {
            return;
        }

        // Only process client-side player
        if (!player.level().isClientSide) {
            return;
        }

        // Don't play if not moving
        if (movementSpeed < 0.001f) {
            return;
        }

        // Get or create timing tracker
        ArmorTiming timing = getOrCreateTiming(player);

        // Check if enough time has passed
        long currentTime = System.currentTimeMillis();
        if (currentTime - timing.lastSoundTime < MIN_ARMOR_SOUND_INTERVAL) {
            return;
        }

        // Count armor pieces and determine dominant material
        ArmorInfo armorInfo = getArmorInfo(player);
        if (armorInfo.pieceCount == 0) {
            return; // No armor, no sound
        }

        // Play armor sound
        playArmorSound(player, armorInfo);
        timing.lastSoundTime = currentTime;
    }

    /**
     * Get information about the player's armor
     */
    @NotNull
    private ArmorInfo getArmorInfo(@NotNull Player player) {
        ArmorInfo info = new ArmorInfo();

        // Check all armor slots
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        }) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem armorItem) {
                info.pieceCount++;

                // Track the most common material (or just use the first one found)
                if (info.dominantMaterial == null) {
                    info.dominantMaterial = armorItem.getMaterial();
                }
            }
        }

        return info;
    }

    /**
     * Play the armor sound for the player
     */
    private void playArmorSound(@NotNull Player player, @NotNull ArmorInfo armorInfo) {
        // Get the sound for this armor material
        SoundEvent sound = this.armorSounds.getOrDefault(
                armorInfo.dominantMaterial,
                SoundEvents.ARMOR_EQUIP_GENERIC
        );

        // Calculate volume based on armor count and config
        float baseVolume = (float) Client.Config.footsteps.armorVolumeScale;
        float armorVolume = 0.3f + (armorInfo.pieceCount * 0.15f); // 0.3 to 0.9 based on pieces
        float volume = baseVolume * armorVolume;

        // Randomize pitch slightly
        float pitch = 0.9f + (player.getRandom().nextFloat() * 0.2f); // 0.9 to 1.1

        // Play the sound
        Level level = player.level();
        level.playLocalSound(
                player.getX(),
                player.getY(),
                player.getZ(),
                sound,
                SoundSource.PLAYERS,
                volume,
                pitch,
                false
        );
    }

    /**
     * Get or create timing tracker for a player
     */
    @NotNull
    private ArmorTiming getOrCreateTiming(@NotNull Player player) {
        return this.playerTimings.computeIfAbsent(player.getUUID(), uuid -> new ArmorTiming());
    }

    /**
     * Clear timing data for a player
     */
    public void clearPlayer(@NotNull UUID playerId) {
        this.playerTimings.remove(playerId);
    }

    /**
     * Clear all timing data
     */
    public void clearAll() {
        this.playerTimings.clear();
    }

    /**
     * Timing tracker for armor sounds
     */
    private static class ArmorTiming {
        long lastSoundTime = 0;
    }

    /**
     * Information about a player's armor
     */
    private static class ArmorInfo {
        int pieceCount = 0;
        ArmorMaterial dominantMaterial = null;
    }
}
