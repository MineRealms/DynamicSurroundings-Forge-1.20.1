/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.orecruncher.dsurround.capabilities.entitydata;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.orecruncher.dsurround.Constants;
import org.orecruncher.dsurround.lib.logging.ModLog;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for assessing entity behavioral states based on their AI goals.
 * Determines if an entity is attacking or fleeing by examining active goals.
 */
public final class EntityDataTables {

    private static final ModLog LOGGER = new ModLog(Constants.MOD_ID);

    /**
     * Describes the type of AI goal.
     */
    private enum GoalType {
        NONE,
        ATTACK,
        FLEE
    }

    // Class types mapped to a GoalType
    private static final Map<Class<?>, GoalType> AI_GOALS = new HashMap<>();

    static {
        // Attack goals
        AI_GOALS.put(MeleeAttackGoal.class, GoalType.ATTACK);
        AI_GOALS.put(RangedAttackGoal.class, GoalType.ATTACK);
        AI_GOALS.put(RangedBowAttackGoal.class, GoalType.ATTACK);
        AI_GOALS.put(RangedCrossbowAttackGoal.class, GoalType.ATTACK);
        AI_GOALS.put(LeapAtTargetGoal.class, GoalType.ATTACK);
        AI_GOALS.put(NearestAttackableTargetGoal.class, GoalType.ATTACK);
        AI_GOALS.put(HurtByTargetGoal.class, GoalType.ATTACK);

        // Flee goals
        AI_GOALS.put(AvoidEntityGoal.class, GoalType.FLEE);
        AI_GOALS.put(FleeSunGoal.class, GoalType.FLEE);
        AI_GOALS.put(PanicGoal.class, GoalType.FLEE);
        AI_GOALS.put(RunAroundLikeCrazyGoal.class, GoalType.FLEE);

        // TODO: Add more goal types as needed for specific mobs
    }

    /**
     * Finds the goal type for a given AI goal instance.
     */
    @Nonnull
    private static GoalType findGoalType(@Nonnull Goal goal) {
        Class<?> goalClass = goal.getClass();

        // Direct lookup
        GoalType type = AI_GOALS.get(goalClass);
        if (type != null) {
            return type;
        }

        // Check inheritance
        for (Map.Entry<Class<?>, GoalType> entry : AI_GOALS.entrySet()) {
            if (entry.getKey().isInstance(goal)) {
                type = entry.getValue();
                // Cache for future lookups
                AI_GOALS.put(goalClass, type);
                return type;
            }
        }

        // Not a goal we're interested in
        AI_GOALS.put(goalClass, GoalType.NONE);
        return GoalType.NONE;
    }

    /**
     * Evaluates if the entity has any active goals of the desired type.
     */
    private static boolean hasActiveGoal(@Nonnull Mob entity, @Nonnull GoalType desiredType) {
        try {
            // Check running goals in the goal selector
            var runningGoals = entity.goalSelector.getRunningGoals();
            for (var wrappedGoal : runningGoals.toList()) {
                Goal goal = wrappedGoal.getGoal();
                if (findGoalType(goal) == desiredType) {
                    return true;
                }
            }

            // Check target selector goals
            var targetGoals = entity.targetSelector.getRunningGoals();
            for (var wrappedGoal : targetGoals.toList()) {
                Goal goal = wrappedGoal.getGoal();
                if (findGoalType(goal) == desiredType) {
                    return true;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Error evaluating entity goals for %s: %s", entity.getClass().getSimpleName(), e.getMessage());
        }

        return false;
    }

    /**
     * Assesses the entity's current behavioral state and updates the data.
     *
     * @param data The entity data to update
     */
    public static void assess(@Nonnull IEntityDataSettable data) {
        Mob entity = data.getEntity();
        if (entity == null) {
            return;
        }

        boolean isAttacking = hasActiveGoal(entity, GoalType.ATTACK);
        boolean isFleeing = hasActiveGoal(entity, GoalType.FLEE);

        data.setAttacking(isAttacking);
        data.setFleeing(isFleeing);
    }
}
