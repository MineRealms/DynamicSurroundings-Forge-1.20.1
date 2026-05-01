package org.orecruncher.dsurround.effects.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.orecruncher.dsurround.lib.logging.IModLog;
import org.orecruncher.dsurround.lib.logging.ModLog;

import java.util.*;

/**
 * Manages entity effect handlers for all entities in the world.
 * Tracks active handlers and updates them each tick.
 */
public class EntityEffectsManager {

    private static final IModLog LOGGER = new ModLog("EntityEffectsManager");
    private static EntityEffectsManager INSTANCE;

    private final EntityEffectLibrary library;
    private final Map<Integer, EntityEffectHandler> handlers = new HashMap<>();
    private final Set<Integer> toRemove = new HashSet<>();

    private EntityEffectsManager() {
        this.library = new EntityEffectLibrary();
        registerDefaultEffects();
    }

    /**
     * Get the singleton instance
     */
    @NotNull
    public static EntityEffectsManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EntityEffectsManager();
        }
        return INSTANCE;
    }

    /**
     * Register default entity effects
     */
    private void registerDefaultEffects() {
        // Register swing effect for all living entities
        this.library.register(
            entity -> entity instanceof net.minecraft.world.entity.LivingEntity,
            entity -> java.util.List.of(new EntitySwingEffect())
        );

        // Register bow/shield effect for all living entities
        this.library.register(
            entity -> entity instanceof net.minecraft.world.entity.LivingEntity,
            entity -> java.util.List.of(new EntityBowSoundEffect())
        );

        // Register toolbar effect for players only
        this.library.register(
            entity -> entity instanceof net.minecraft.world.entity.player.Player,
            entity -> java.util.List.of(new PlayerToolBarSoundEffect((net.minecraft.world.entity.player.Player) entity))
        );

        LOGGER.info("Entity effects library initialized with %d factories", this.library.getFactoryCount());
    }

    /**
     * Get or create a handler for the given entity
     */
    @NotNull
    public EntityEffectHandler getOrCreateHandler(@NotNull Entity entity) {
        int entityId = entity.getId();

        EntityEffectHandler handler = this.handlers.get(entityId);
        if (handler == null) {
            // Create new handler
            Optional<EntityEffectHandler> newHandler = this.library.create(entity);
            handler = newHandler.orElse(new EntityEffectHandler.Dummy(entity));
            this.handlers.put(entityId, handler);
        }

        return handler;
    }

    /**
     * Update all active entity effect handlers
     */
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;

        if (level == null) {
            return;
        }

        this.toRemove.clear();

        // Update all handlers
        for (Map.Entry<Integer, EntityEffectHandler> entry : this.handlers.entrySet()) {
            EntityEffectHandler handler = entry.getValue();

            // Skip dummy handlers
            if (handler.isDummy()) {
                continue;
            }

            // Update handler
            handler.update();

            // Mark dead handlers for removal
            if (!handler.isAlive()) {
                this.toRemove.add(entry.getKey());
            }
        }

        // Remove dead handlers
        for (Integer id : this.toRemove) {
            this.handlers.remove(id);
        }
    }

    /**
     * Clear all handlers (called on disconnect/dimension change)
     */
    public void clear() {
        this.handlers.clear();
        LOGGER.debug("Cleared all entity effect handlers");
    }

    /**
     * Get statistics for diagnostics
     */
    @NotNull
    public String getStatistics() {
        int total = this.handlers.size();
        int active = 0;
        int dummy = 0;

        for (EntityEffectHandler handler : this.handlers.values()) {
            if (handler.isDummy()) {
                dummy++;
            } else if (handler.isAlive()) {
                active++;
            }
        }

        return String.format("Entity Effects: %d total (%d active, %d dummy)", total, active, dummy);
    }

    /**
     * Get the effect library for registration
     */
    @NotNull
    public EntityEffectLibrary getLibrary() {
        return this.library;
    }
}
