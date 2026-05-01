package org.orecruncher.dsurround.effects.entity;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Interface providing state information to EntityEffects.
 * This allows effects to query information about their entity and handler.
 */
public interface IEntityEffectHandlerState {

    /**
     * Get the entity this handler is attached to
     */
    @NotNull
    Optional<Entity> subject();

    /**
     * Check if the handler is still alive (entity exists and is alive)
     */
    boolean isAlive();

    /**
     * Get the distance squared from the entity to the player
     */
    double distanceToPlayerSq();
}
