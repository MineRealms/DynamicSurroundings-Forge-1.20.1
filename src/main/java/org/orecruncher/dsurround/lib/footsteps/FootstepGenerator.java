package org.orecruncher.dsurround.lib.footsteps;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.orecruncher.dsurround.Client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Generates footstep sounds based on player movement and block types.
 * Manages timing and sound selection for different movement types.
 */
public class FootstepGenerator {
    private static FootstepGenerator INSTANCE;

    // Per-player timing trackers
    private final Map<UUID, FootstepTiming> playerTimings;

    private FootstepGenerator() {
        this.playerTimings = new HashMap<>();
    }

    @NotNull
    public static FootstepGenerator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FootstepGenerator();
        }
        return INSTANCE;
    }

    /**
     * Called when a player moves. Determines if footstep sounds should play.
     */
    public void onPlayerMove(@NotNull Player player, @NotNull Vec3 movement) {
        // Check if footsteps are enabled
        if (!Client.Config.footsteps.enabled) {
            return;
        }

        // Only process client-side player
        if (!player.level().isClientSide) {
            return;
        }

        // Don't play footsteps if player is flying or swimming
        if (player.getAbilities().flying || player.isSwimming() || player.isInWater()) {
            return;
        }

        // Get or create timing tracker for this player
        FootstepTiming timing = getOrCreateTiming(player);

        // Check for landing
        if (timing.checkLanding(player)) {
            playFootstepSound(player, MovementType.LAND);
            return;
        }

        // Calculate horizontal movement speed
        float movementSpeed = (float) Math.sqrt(movement.x * movement.x + movement.z * movement.z);

        // Check if we should play a footstep
        if (timing.shouldPlayFootstep(player, movementSpeed)) {
            MovementType type = determineMovementType(player);
            playFootstepSound(player, type);
        }

        // Check for armor sounds
        ArmorSoundHandler.getInstance().onPlayerMove(player, movementSpeed);
    }

    /**
     * Called when a player jumps
     */
    public void onPlayerJump(@NotNull Player player) {
        // Check if footsteps are enabled
        if (!Client.Config.footsteps.enabled) {
            return;
        }

        // Only process client-side player
        if (!player.level().isClientSide) {
            return;
        }

        playFootstepSound(player, MovementType.JUMP);
    }

    /**
     * Play a footstep sound for the given player and movement type
     */
    private void playFootstepSound(@NotNull Player player, @NotNull MovementType type) {
        // Get the block position under the player's feet
        BlockPos footPos = getFootstepPosition(player);
        if (footPos == null) {
            return;
        }

        // Get the block state
        Level level = player.level();
        BlockState state = level.getBlockState(footPos);

        // Get the acoustic profile for this block
        AcousticProfile acoustic = AcousticsManager.getInstance()
            .getBlockAcoustics()
            .getAcoustic(state);

        // Select the appropriate sound based on movement type
        SoundEvent sound = selectSound(acoustic, type);
        if (sound == null) {
            return;
        }

        // Calculate volume and pitch
        float volume = calculateVolume(player, acoustic, type);
        float pitch = acoustic.getRandomPitch();

        // Play the sound
        level.playLocalSound(
            player.getX(),
            player.getY(),
            player.getZ(),
            sound,
            SoundSource.PLAYERS,
            volume,
            pitch,
            false
        );
    }

    /**
     * Get the block position where footsteps should be calculated
     */
    private BlockPos getFootstepPosition(@NotNull Player player) {
        // Start at player's feet
        BlockPos pos = player.blockPosition().below();

        // If the block at feet is air, check one more block down
        if (player.level().getBlockState(pos).isAir()) {
            pos = pos.below();
        }

        // If still air, no footstep
        if (player.level().getBlockState(pos).isAir()) {
            return null;
        }

        return pos;
    }

    /**
     * Determine the movement type based on player state
     */
    @NotNull
    private MovementType determineMovementType(@NotNull Player player) {
        if (player.isSprinting()) {
            return MovementType.RUN;
        } else if (player.isCrouching()) {
            return MovementType.SNEAK;
        } else {
            return MovementType.WALK;
        }
    }

    /**
     * Select the appropriate sound from the acoustic profile
     */
    private SoundEvent selectSound(@NotNull AcousticProfile acoustic, @NotNull MovementType type) {
        return switch (type) {
            case WALK, SNEAK -> acoustic.getWalkSound();
            case RUN -> acoustic.getRunSound();
            case JUMP -> acoustic.getJumpSound();
            case LAND -> acoustic.getLandSound();
        };
    }

    /**
     * Calculate the volume for the footstep sound
     */
    private float calculateVolume(@NotNull Player player, @NotNull AcousticProfile acoustic, @NotNull MovementType type) {
        float baseVolume = (float) Client.Config.footsteps.volumeScale;
        float acousticVolume = acoustic.getVolumeScale();

        // Reduce volume for sneaking
        if (type == MovementType.SNEAK) {
            acousticVolume *= 0.3f;
        }

        // Check if this is the first-person player
        boolean isFirstPerson = player == Minecraft.getInstance().player;
        if (isFirstPerson && !Client.Config.footsteps.firstPersonFootsteps) {
            acousticVolume *= 0.5f; // Reduce volume for first-person
        }

        return baseVolume * acousticVolume;
    }

    /**
     * Get or create a timing tracker for a player
     */
    @NotNull
    private FootstepTiming getOrCreateTiming(@NotNull Player player) {
        return this.playerTimings.computeIfAbsent(player.getUUID(), uuid -> new FootstepTiming());
    }

    /**
     * Clear timing data for a player (e.g., when they disconnect)
     */
    public void clearPlayer(@NotNull UUID playerId) {
        this.playerTimings.remove(playerId);
    }

    /**
     * Clear all timing data
     */
    public void clearAll() {
        this.playerTimings.clear();
    }
}
