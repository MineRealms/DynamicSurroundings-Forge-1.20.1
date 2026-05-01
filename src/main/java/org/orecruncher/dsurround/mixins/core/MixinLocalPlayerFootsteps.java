package org.orecruncher.dsurround.mixins.core;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
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
        LocalPlayer player = (LocalPlayer) (Object) this;
        FootstepGenerator.getInstance().onPlayerMove(player, movement);
    }

    /**
     * Hook into the aiStep method to detect jumping
     * We check for jump input here because jumpFromGround is protected
     */
    @Inject(method = "aiStep", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;jumpFromGround()V"))
    private void dsurround_onJump(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        FootstepGenerator.getInstance().onPlayerJump(player);
    }
}
