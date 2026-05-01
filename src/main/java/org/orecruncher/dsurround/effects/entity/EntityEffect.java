package org.orecruncher.dsurround.effects.entity;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for all entity effects.
 * Entity effects are behaviors attached to entities that update each tick.
 */
public abstract class EntityEffect {

    private IEntityEffectHandlerState state;

    /**
     * Returns the name of the effect
     */
    @NotNull
    public abstract String name();

    /**
     * Called during initialization to provide the handler state.
     * Override to perform initialization specific to the effect.
     */
    public void initialize(@NotNull IEntityEffectHandlerState state) {
        this.state = state;
    }

    /**
     * Get the handler state associated with this effect
     */
    @NotNull
    protected IEntityEffectHandlerState getState() {
        return this.state;
    }

    /**
     * Called each tick to update the effect state.
     *
     * @param entity The entity this effect is attached to
     */
    public abstract void update(@NotNull Entity entity);

    /**
     * Whether this effect should receive one final update call after the entity dies.
     * Default is false.
     */
    public boolean receiveLastCall() {
        return false;
    }

    @Override
    public String toString() {
        return name();
    }
}
