package org.orecruncher.dsurround.effects.entity;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Central repository for entity effect factories and filters.
 * Manages registration and creation of entity effect handlers.
 */
public class EntityEffectLibrary {

    private final List<IEntityEffectFactoryFilter> filters = new ArrayList<>();
    private final List<IEntityEffectFactory> factories = new ArrayList<>();

    /**
     * Register an effect factory with its filter.
     * The filter determines which entities the factory applies to.
     *
     * @param filter The filter to check if factory applies
     * @param factory The factory to create effects
     */
    public void register(@NotNull IEntityEffectFactoryFilter filter, @NotNull IEntityEffectFactory factory) {
        this.filters.add(filter);
        this.factories.add(factory);
    }

    /**
     * Create an entity effect handler for the given entity.
     * Applies all matching factories based on their filters.
     *
     * @param entity The entity to create a handler for
     * @return An effect handler (may be a dummy if no effects apply)
     */
    @NotNull
    public Optional<EntityEffectHandler> create(@NotNull Entity entity) {
        List<EntityEffect> effectsToApply = new ArrayList<>();

        // Check all registered factories
        for (int i = 0; i < this.filters.size(); i++) {
            if (this.filters.get(i).applies(entity)) {
                List<EntityEffect> effects = this.factories.get(i).create(entity);
                effectsToApply.addAll(effects);
            }
        }

        // Create handler
        EntityEffectHandler handler;
        if (!effectsToApply.isEmpty()) {
            handler = new EntityEffectHandler(entity, effectsToApply);
        } else {
            // No effects - return dummy handler
            handler = new EntityEffectHandler.Dummy(entity);
        }

        return Optional.of(handler);
    }

    /**
     * Get the number of registered factories
     */
    public int getFactoryCount() {
        return this.factories.size();
    }
}
