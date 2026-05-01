package org.orecruncher.dsurround.effects.blocks;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.orecruncher.dsurround.Client;
import org.orecruncher.dsurround.lib.Library;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manages all active particle systems in the world.
 * Handles creation, updating, and removal of particle systems.
 */
public class ParticleSystemManager {
    private static ParticleSystemManager INSTANCE;

    // Map of block position to active particle system
    private final Map<BlockPos, ParticleSystem> activeSystems;

    // List of systems to remove (populated during update)
    private final List<BlockPos> toRemove;

    private ParticleSystemManager() {
        this.activeSystems = new Object2ObjectOpenHashMap<>();
        this.toRemove = new ArrayList<>();
    }

    @NotNull
    public static ParticleSystemManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ParticleSystemManager();
        }
        return INSTANCE;
    }

    /**
     * Add or replace a particle system at the given position.
     * Only one system can exist per block position.
     *
     * @param system The particle system to add
     */
    public void addSystem(@NotNull ParticleSystem system) {
        // Remove existing system at this position if present
        ParticleSystem existing = this.activeSystems.get(system.getPos());
        if (existing != null) {
            existing.setDead();
        }

        this.activeSystems.put(system.getPos(), system);
    }

    /**
     * Get the particle system at the given position, if any.
     *
     * @param pos The block position
     * @return The particle system, or null if none exists
     */
    @Nullable
    public ParticleSystem getSystem(@NotNull BlockPos pos) {
        return this.activeSystems.get(pos);
    }

    /**
     * Check if a particle system exists at the given position.
     *
     * @param pos The block position
     * @return true if a system exists
     */
    public boolean hasSystem(@NotNull BlockPos pos) {
        return this.activeSystems.containsKey(pos);
    }

    /**
     * Remove the particle system at the given position.
     *
     * @param pos The block position
     */
    public void removeSystem(@NotNull BlockPos pos) {
        ParticleSystem system = this.activeSystems.remove(pos);
        if (system != null) {
            system.setDead();
        }
    }

    /**
     * Update all active particle systems.
     * Called every tick.
     */
    public void onTick() {
        if (this.activeSystems.isEmpty()) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        // Get the effect range from config
        int effectRange = Client.Config.blockEffects.blockEffectRange;
        int effectRangeSq = effectRange * effectRange;

        this.toRemove.clear();

        // Update systems within range
        for (Map.Entry<BlockPos, ParticleSystem> entry : this.activeSystems.entrySet()) {
            BlockPos pos = entry.getKey();
            ParticleSystem system = entry.getValue();

            // Check if system is within range
            if (player.blockPosition().distSqr(pos) > effectRangeSq) {
                continue; // Skip systems out of range
            }

            // Update the system
            system.onUpdate();

            // Mark dead systems for removal
            if (!system.isAlive()) {
                this.toRemove.add(pos);
            }
        }

        // Remove dead systems
        for (BlockPos pos : this.toRemove) {
            this.activeSystems.remove(pos);
        }

        if (!this.toRemove.isEmpty()) {
            Library.LOGGER.debug("Removed {} dead particle systems", this.toRemove.size());
        }
    }

    /**
     * Clear all particle systems.
     * Called when disconnecting or changing dimensions.
     */
    public void clear() {
        for (ParticleSystem system : this.activeSystems.values()) {
            system.setDead();
        }
        this.activeSystems.clear();
        Library.LOGGER.info("Cleared all particle systems");
    }

    /**
     * Get the number of active particle systems.
     */
    public int getActiveCount() {
        return this.activeSystems.size();
    }
}
