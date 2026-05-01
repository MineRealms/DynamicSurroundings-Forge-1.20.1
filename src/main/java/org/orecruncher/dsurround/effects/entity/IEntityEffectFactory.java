package org.orecruncher.dsurround.effects.entity;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Factory interface for creating entity effects.
 * Each factory can create one or more effects for a given entity.
 */
public interface IEntityEffectFactory {

    /**
     * Create entity effects for the given entity
     *
     * @param entity The entity to create effects for
     * @return List of effects to attach to the entity
     */
    @NotNull
    List<EntityEffect> create(@NotNull Entity entity);
}
