package org.orecruncher.dsurround.mixins.core;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.orecruncher.dsurround.lib.Library;
import org.orecruncher.dsurround.lib.footsteps.FootstepGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to hook into LocalPlayer movement for footstep sound generation
 */
@Mixin(LocalPlayer.class)
public class MixinLocalPlayerFootsteps {

    private static boolean loggedOnce = false;

    /**
     * Hook into the move method to detect player movement and trigger footstep sounds
     */
    @Inject(method = "move", at = @At("TAIL"))
    private void dsurround_onMove(MoverType type, Vec3 movement, CallbackInfo ci) {
        try {
            LocalPlayer player = (LocalPlayer) (Object) this;
            if (!loggedOnce) {
                Library.LOGGER.info("Footprint: MixinLocalPlayerFootsteps.dsurround_onMove called!");
                loggedOnce = true;
            }
            Library.LOGGER.info("Footprint: About to call FootstepGenerator.getInstance()");
            FootstepGenerator generator = FootstepGenerator.getInstance();
            Library.LOGGER.info("Footprint: Got generator instance, calling onPlayerMove");
            generator.onPlayerMove(player, movement);
            Library.LOGGER.info("Footprint: onPlayerMove completed");
        } catch (Exception e) {
            Library.LOGGER.error(e, "Footprint: Exception in dsurround_onMove");
        }
    }

    /**
     * Hook into the aiStep method to detect jumping
     * We inject at HEAD and check if the player is jumping
     */
    @Inject(method = "aiStep", at = @At("HEAD"))
    private void dsurround_onAiStep(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;

        // Check if player just started jumping (onGround and has upward velocity)
        if (player.onGround() && player.input.jumping && !player.getAbilities().flying) {
            FootstepGenerator.getInstance().onPlayerJump(player);
        }
    }
}
