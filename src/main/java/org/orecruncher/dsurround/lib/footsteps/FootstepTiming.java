package org.orecruncher.dsurround.lib.footsteps;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Calculates when footstep sounds should play based on player movement.
 * Tracks distance walked and timing to ensure footsteps play at appropriate intervals.
 */
public class FootstepTiming {
    // Distance thresholds for footstep sounds (in blocks)
    private static final float WALK_DISTANCE_THRESHOLD = 0.6f;
    private static final float RUN_DISTANCE_THRESHOLD = 0.4f;
    private static final float SNEAK_DISTANCE_THRESHOLD = 1.2f;

    private float distanceWalked;
    private long lastStepTime;
    private boolean wasOnGround;
    private boolean wasJumping;

    public FootstepTiming() {
        this.distanceWalked = 0.0f;
        this.lastStepTime = 0L;
        this.wasOnGround = true;
        this.wasJumping = false;
    }

    /**
     * Update the timing state and determine if a footstep should play.
     *
     * @param player The player entity
     * @param movementSpeed The current movement speed
     * @return true if a footstep sound should play
     */
    public boolean shouldPlayFootstep(@NotNull Player player, float movementSpeed) {
        // Don't play footsteps if player is not on ground
        if (!player.onGround()) {
            this.wasOnGround = false;
            return false;
        }

        // Check for landing (just touched ground)
        if (!this.wasOnGround) {
            this.wasOnGround = true;
            this.distanceWalked = 0.0f; // Reset distance on landing
            return false; // Landing sound is handled separately
        }

        // Don't play if not moving
        if (movementSpeed < 0.001f) {
            return false;
        }

        // Accumulate distance
        this.distanceWalked += movementSpeed;

        // Determine threshold based on movement state
        float threshold = getDistanceThreshold(player);

        // Check if we've walked far enough
        if (this.distanceWalked >= threshold) {
            this.distanceWalked = 0.0f;
            this.lastStepTime = System.currentTimeMillis();
            return true;
        }

        return false;
    }

    /**
     * Check if the player just landed (for landing sounds)
     */
    public boolean checkLanding(@NotNull Player player) {
        boolean isOnGround = player.onGround();
        boolean justLanded = isOnGround && !this.wasOnGround;
        this.wasOnGround = isOnGround;

        if (justLanded) {
            this.distanceWalked = 0.0f; // Reset distance on landing
        }

        return justLanded;
    }

    /**
     * Check if the player just jumped (for jump sounds)
     */
    public boolean checkJump(@NotNull Player player) {
        boolean isJumping = !player.onGround() && player.getDeltaMovement().y > 0;
        boolean justJumped = isJumping && !this.wasJumping;
        this.wasJumping = isJumping;
        return justJumped;
    }

    /**
     * Reset the timing state
     */
    public void reset() {
        this.distanceWalked = 0.0f;
        this.lastStepTime = 0L;
        this.wasOnGround = true;
        this.wasJumping = false;
    }

    /**
     * Get the distance threshold based on player movement state
     */
    private float getDistanceThreshold(@NotNull Player player) {
        if (player.isSprinting()) {
            return RUN_DISTANCE_THRESHOLD;
        } else if (player.isCrouching()) {
            return SNEAK_DISTANCE_THRESHOLD;
        } else {
            return WALK_DISTANCE_THRESHOLD;
        }
    }

    /**
     * Get the current distance walked since last footstep
     */
    public float getDistanceWalked() {
        return this.distanceWalked;
    }

    /**
     * Get the time of the last footstep
     */
    public long getLastStepTime() {
        return this.lastStepTime;
    }
}
