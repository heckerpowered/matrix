package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.event.StatusEffectRemovedCallback;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
class ServerPlayerEntityMixin {
    private ServerPlayerEntityMixin() {
    }

    @Inject(method = "onStatusEffectRemoved", at = @At("HEAD"), cancellable = true)
    private void onStatusEffectRemoved(StatusEffectInstance effect, CallbackInfo ci) {
        final var result = StatusEffectRemovedCallback.EVENT.invoker().onStatusEffectRemoved((ServerPlayerEntity) (Object) this, effect);
        if (result == ActionResult.FAIL) {
            ci.cancel();
        }
    }
}
