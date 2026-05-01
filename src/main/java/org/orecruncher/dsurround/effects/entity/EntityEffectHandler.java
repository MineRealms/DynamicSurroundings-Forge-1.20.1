package org.orecruncher.dsurround.effects.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages entity effects attached to a specific entity.
 * Updates all attached effects each tick and tracks entity lifecycle.
 */
public class EntityEffectHandler implements IEntityEffectHandlerState {

    /**
     * Dummy handler that does nothing - used when no effects apply to an entity
     */
    public static class Dummy extends EntityEffectHandler {
        public Dummy(@NotNull Entity entity) {
            super(entity);
        }

        @Override
        public void update() {
            // No-op
        }

        @Override
        public boolean isDummy() {
            return true;
        }

        @NotNull
        @Override
        public List<String> getAttachedEffects() {
            return List.of("Dummy EffectHandler");
        }
    }

    protected final WeakReference<Entity> subject;
    protected final List<EntityEffect> activeEffects;
    protected boolean isAlive = true;
    protected double rangeToPlayer;

    protected EntityEffectHandler(@NotNull Entity entity) {
        this.subject = new WeakReference<>(entity);
        this.activeEffects = new ArrayList<>();
    }

    public EntityEffectHandler(@NotNull Entity entity, @NotNull List<EntityEffect> effects) {
        this.subject = new WeakReference<>(entity);
        this.activeEffects = new ArrayList<>(effects);

        // Initialize all effects
        for (EntityEffect effect : this.activeEffects) {
            effect.initialize(this);
        }
    }

    /**
     * Update all attached effects
     */
    public void update() {
        if (!isAlive()) {
            return;
        }

        Entity entity = this.subject.get();
        if (entity == null) {
            this.isAlive = false;
            return;
        }

        this.isAlive = entity.isAlive();

        // Update distance to player
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            this.rangeToPlayer = entity.distanceToSqr(mc.player);
        }

        // Update all effects
        for (EntityEffect effect : this.activeEffects) {
            if (this.isAlive || effect.receiveLastCall()) {
                effect.update(entity);
            }
        }
    }

    /**
     * Check if this is a dummy handler
     */
    public boolean isDummy() {
        return false;
    }

    /**
     * Get list of attached effect names for diagnostics
     */
    @NotNull
    public List<String> getAttachedEffects() {
        if (this.activeEffects.isEmpty()) {
            return List.of("No effects");
        }

        List<String> result = new ArrayList<>();
        for (EntityEffect effect : this.activeEffects) {
            result.add(effect.toString());
        }
        return result;
    }

    // IEntityEffectHandlerState implementation

    @NotNull
    @Override
    public Optional<Entity> subject() {
        return Optional.ofNullable(this.subject.get());
    }

    @Override
    public boolean isAlive() {
        return this.isAlive;
    }

    @Override
    public double distanceToPlayerSq() {
        return this.rangeToPlayer;
    }
}
