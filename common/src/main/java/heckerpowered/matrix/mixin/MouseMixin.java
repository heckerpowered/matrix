/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.client.MatrixHud;
import heckerpowered.matrix.client.core.AimAssist;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.2: onMouseScroll→onScroll, onMouseButton→onButton (button/modifiers wrapped in
 * MouseButtonInfo), updateMouse(timeDelta)→handleAccumulatedMovement() (no time argument —
 * the frame partial tick is read from the delta tracker instead, which is what the old
 * timeDelta fed into AimAssist's view-rotation interpolation).
 */
@Mixin(MouseHandler.class)
class MouseMixin {
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (!MatrixHud.shouldRenderHud()) {
            return;
        }
        final var minecraft = Minecraft.getInstance();
        if (vertical < 0) {
            if (MatrixHud.isPressingRightMouseButton) {
                MatrixHud.previousZoomLevel();
                ci.cancel();
            } else {
                MatrixHud.nextMagic();
                ci.cancel();
            }
        } else if (vertical > 0) {
            if (MatrixHud.isPressingRightMouseButton) {
                MatrixHud.nextZoomLevel();
                ci.cancel();
            } else {
                MatrixHud.previousMagic();
                ci.cancel();
            }
        }
    }

    @Inject(method = "handleAccumulatedMovement", at = @At("HEAD"), cancellable = true)
    private void updateMouse(CallbackInfo ci) {
        final var minecraft = Minecraft.getInstance();
        // The 1.21 updateMouse hook only ever ran with a player present; 26.2 calls
        // handleAccumulatedMovement unconditionally (main menu included), so guard here.
        if (minecraft.player == null) {
            return;
        }
        final var timeDelta = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        if (AimAssist.onMouseUpdate(timeDelta)) {
            ci.cancel();
        }
    }

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, MouseButtonInfo info, int action, CallbackInfo ci) {
        if (MatrixHud.onMouseButton(window, info.button(), action, info.modifiers())) {
            ci.cancel();
        }
    }
}
