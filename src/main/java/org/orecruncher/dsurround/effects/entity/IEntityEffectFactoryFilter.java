package org.orecruncher.dsurround.effects.entity;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Filter interface to determine if an effect factory should be applied to an entity.
 */
@FunctionalInterface
public interface IEntityEffectFactoryFilter {

    /**
     * Check if the factory should be applied to the given entity
     *
     * @param entity The entity to check
     * @return true if the factory should create effects for this entity
     */
    boolean applies(@NotNull Entity entity);
}
