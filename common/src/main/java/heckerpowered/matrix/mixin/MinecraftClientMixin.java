/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.client.MatrixClient;
import heckerpowered.matrix.client.MatrixHud;
import heckerpowered.matrix.client.TimeController;
import heckerpowered.matrix.client.event.FinishRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.2: MinecraftClient is Minecraft, render() is runTick(boolean), doAttack() is
 * startAttack(), and the render tick counter is a DeltaTracker.Timer
 * (beginRenderTick→advanceGameTime, tick→updatePauseState, setTickFrozen→updateFrozenState).
 * The former getFramebuffer() spoof hook moved to GameRendererMixin#mainRenderTarget.
 */
@Mixin(Minecraft.class)
abstract class MinecraftClientMixin {
    private MinecraftClientMixin() {
    }

    @Shadow
    public abstract void tick();

    @Inject(method = "startAttack", at = @At("HEAD"))
    private void doAttack(CallbackInfoReturnable<Boolean> cir) {
        MatrixHud.onDoAttack();
    }

    /**
     * The former "before the final framebuffer draw" anchor; the event currently has no
     * registered listeners, so end-of-frame is an equivalent firing point.
     */
    @Inject(method = "runTick", at = @At("TAIL"))
    private void onFinishedRender(boolean tick, CallbackInfo ci) {
        FinishRenderCallback.EVENT.invoker().onFinishRender();
    }

    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/DeltaTracker$Timer;advanceGameTime(J)I"))
    private int beginRenderTick(DeltaTracker.Timer instance, long timeMillis) {
        final var tickCount = instance.advanceGameTime(timeMillis);
        TimeController.beginRenderTick(timeMillis, true);
        return tickCount;
    }

    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/DeltaTracker$Timer;updatePauseState(Z)V"))
    private void tick(DeltaTracker.Timer instance, boolean paused) {
        instance.updatePauseState(paused);
        TimeController.standaloneRenderTickCounter.updatePauseState(paused);
    }

    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/DeltaTracker$Timer;updateFrozenState(Z)V"))
    private void setTickFrozen(DeltaTracker.Timer instance, boolean frozen) {
        instance.updateFrozenState(frozen);
        TimeController.standaloneRenderTickCounter.updateFrozenState(frozen);
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;<init>(Lcom/mojang/blaze3d/platform/WindowEventHandler;Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;ZLjava/lang/String;Lcom/mojang/blaze3d/platform/MonitorManager;Lcom/mojang/blaze3d/systems/GpuBackend;)V", shift = At.Shift.AFTER))
    private void createWindow(GameConfig args, CallbackInfo ci) {
        MatrixClient.onWindowInitialization();
    }
}
