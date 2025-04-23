package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.effect.ManaOverloadEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WitchEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitchEntity.class)
class WitchEntityMixin {
    @Inject(method = "shootAt", at = @At("HEAD"), cancellable = true)
    private void shootAt(LivingEntity target, float pullProgress, CallbackInfo ci) {
        final var self = (WitchEntity) (Object) this;
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(self)) {
            ci.cancel();
        }
    }
}
