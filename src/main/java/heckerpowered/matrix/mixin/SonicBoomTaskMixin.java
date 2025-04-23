package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.effect.ManaOverloadEffect;
import net.minecraft.entity.ai.brain.task.SonicBoomTask;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SonicBoomTask.class)
class SonicBoomTaskMixin {
    @Inject(method = "shouldRun(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/mob/WardenEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void shouldRun(ServerWorld serverWorld, WardenEntity wardenEntity, CallbackInfoReturnable<Boolean> cir) {
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(wardenEntity)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldKeepRunning(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/mob/WardenEntity;J)Z", at = @At("HEAD"), cancellable = true)
    private void shouldKeepRunning(ServerWorld serverWorld, WardenEntity wardenEntity, long l, CallbackInfoReturnable<Boolean> cir) {
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(wardenEntity)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "run(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/mob/WardenEntity;J)V", at = @At("HEAD"), cancellable = true)
    private void run(ServerWorld serverWorld, WardenEntity wardenEntity, long l, CallbackInfo ci) {
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(wardenEntity)) {
            ci.cancel();
        }
    }

    @Inject(method = "keepRunning(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/mob/WardenEntity;J)V", at = @At("HEAD"), cancellable = true)
    private void keepRunning(ServerWorld serverWorld, WardenEntity wardenEntity, long l, CallbackInfo ci) {
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(wardenEntity)) {
            ci.cancel();
        }
    }
}
