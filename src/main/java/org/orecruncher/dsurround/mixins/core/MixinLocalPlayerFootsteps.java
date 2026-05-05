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

    /**
     * Hook into the move method to detect player movement and trigger footstep sounds
     */
    @Inject(method = "move", at = @At("TAIL"))
    private void dsurround_onMove(MoverType type, Vec3 movement, CallbackInfo ci) {
        try {
            LocalPlayer player = (LocalPlayer) (Object) this;
            FootstepGenerator generator = FootstepGenerator.getInstance();
            generator.onPlayerMove(player, movement);
        } catch (Exception e) {
            Library.LOGGER.error(e, "Exception in footstep movement handler");
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
