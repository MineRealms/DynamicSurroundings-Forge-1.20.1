package org.orecruncher.dsurround.lib.footsteps;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.orecruncher.dsurround.Client;
import org.orecruncher.dsurround.effects.particles.FootprintParticle;
import org.orecruncher.dsurround.footsteps.Footprint;
import org.orecruncher.dsurround.footsteps.FootprintStyle;
import org.orecruncher.dsurround.lib.Library;

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

    // Track which foot is next (per player)
    private final Map<UUID, Boolean> isRightFoot;

    private FootstepGenerator() {
        this.playerTimings = new HashMap<>();
        this.isRightFoot = new HashMap<>();
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

            // Generate footprint visual effect
            if (Client.Config.footsteps.enableFootprints) {
                generateFootprint(player, type);
            }
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
     * Generate a footprint visual effect
     */
    private void generateFootprint(@NotNull Player player, @NotNull MovementType type) {
        // Don't generate footprints when jumping or landing (handled separately)
        if (type == MovementType.JUMP || type == MovementType.LAND) {
            return;
        }

        // Don't generate footprints if player is invisible
        if (player.isInvisible()) {
            return;
        }

        // Get footprint style
        FootprintStyle style = FootprintStyle.getStyle(Client.Config.footsteps.footprintStyle);

        // Get which foot
        boolean rightFoot = this.isRightFoot.getOrDefault(player.getUUID(), false);
        this.isRightFoot.put(player.getUUID(), !rightFoot);

        // Calculate foot position based on player rotation
        float rotation = player.getYRot();
        float rotRad = (float) Math.toRadians(rotation);
        float footOffset = 0.2F; // Distance from center to foot
        float feetDistanceToCenter = rightFoot ? -footOffset : footOffset;

        double footX = player.getX() + Mth.cos(rotRad) * feetDistanceToCenter;
        double footZ = player.getZ() + Mth.sin(rotRad) * feetDistanceToCenter;

        // Get Y position (slightly above ground to avoid z-fighting)
        BlockPos footPos = getFootstepPosition(player);
        if (footPos == null) {
            return;
        }

        // Check if the surface supports footprints
        Level level = player.level();
        BlockState state = level.getBlockState(footPos);
        if (!shouldLeaveFootprint(state)) {
            return;
        }

        // Calculate Y position using bounding box (like 1.12.2)
        double entityY = player.getBoundingBox().minY;
        double blockY = getBlockTopY(level, state, footPos, entityY);
        double footY = Math.max(entityY, blockY);

        // Create footprint data
        Vec3 footLocation = new Vec3(footX, footY, footZ);
        Footprint print = Footprint.produce(style, player, footLocation, rotation, 1.0F, rightFoot);

        // Spawn footprint particle
        spawnFootprintParticle(print);
    }

    /**
     * Get the top Y coordinate of a block, similar to 1.12.2's getBoundingBoxY
     */
    private double getBlockTopY(@NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, double baseY) {
        // Get the block's shape (bounding box)
        var shape = state.getShape(level, pos);
        if (shape.isEmpty()) {
            return baseY;
        }

        // Get the collision shape
        var collisionShape = state.getCollisionShape(level, pos);

        double shapeMaxY = shape.max(net.minecraft.core.Direction.Axis.Y);
        double collisionMaxY = collisionShape.isEmpty() ? baseY : collisionShape.max(net.minecraft.core.Direction.Axis.Y);

        if (shapeMaxY == collisionMaxY) {
            return baseY;
        }

        return Math.max(baseY, pos.getY() + Math.max(shapeMaxY, collisionMaxY));
    }

    /**
     * Check if a block state should leave footprints
     */
    private boolean shouldLeaveFootprint(@NotNull BlockState state) {
        // Footprints appear on snow, sand, dirt, grass, etc.
        // For now, allow footprints on all solid blocks
        // TODO: Add configuration for specific block types
        return state.isSolidRender(Minecraft.getInstance().level, BlockPos.ZERO);
    }

    /**
     * Spawn a footprint particle in the world
     */
    private void spawnFootprintParticle(@NotNull Footprint print) {
        Vec3 loc = print.getStepLocation();
        if (loc == null) {
            return;
        }

        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        FootprintParticle particle = new FootprintParticle(
            print.getStyle(),
            level,
            loc.x,
            loc.y,
            loc.z,
            print.getRotation(),
            print.getScale(),
            print.isRightFoot()
        );

        Minecraft.getInstance().particleEngine.add(particle);
    }

    /**
     * Clear timing data for a player (e.g., when they disconnect)
     */
    public void clearPlayer(@NotNull UUID playerId) {
        this.playerTimings.remove(playerId);
        this.isRightFoot.remove(playerId);
    }

    /**
     * Clear all timing data
     */
    public void clearAll() {
        this.playerTimings.clear();
        this.isRightFoot.clear();
    }
}
