/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.client.TimeController;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    @Shadow
    private volatile boolean pause;

    private MinecraftMixin() {
    }

    @Shadow
    private boolean isLevelRunningNormally() {
        throw new AssertionError();
    }

    @Inject(method = "runTick", at = @At("HEAD"))
    private void beginRenderTick(boolean tick, CallbackInfo ci) {
        TimeController.beginRenderTick(Util.getMillis(), tick);
    }

    @Inject(method = "runTick", at = @At("TAIL"))
    private void updateStandaloneRenderTickState(boolean tick, CallbackInfo ci) {
        TimeController.standaloneRenderTickCounter.tick(pause);
        TimeController.standaloneRenderTickCounter.setTickFrozen(!isLevelRunningNormally());
    }
}
