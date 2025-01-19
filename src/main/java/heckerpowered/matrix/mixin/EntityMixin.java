package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.effect.MatrixStatusEffectsKt;
import heckerpowered.matrix.common.event.EntityRemovedCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
class EntityMixin {
    private EntityMixin() {
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void onRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        EntityRemovedCallback.event.invoker().onEntityRemoved((Entity) (Object) this, reason);
    }

    @SuppressWarnings("all")
    @Inject(method = "setPos", at = @At("HEAD"), cancellable = true)
    private void setPos(double x, double y, double z, CallbackInfo ci) {
        if (!(((Object) this) instanceof final LivingEntity self)) {
            return;
        }

        // This function may be called before the living entity's constructor is called,
        // exception will be thrown in that case.
        try {
            final var crippleMovement = MatrixStatusEffectsKt.getCrippleMovementEffect();
            final var effect = self.getStatusEffect(crippleMovement);
            if (effect == null) {
                return;
            }

            ci.cancel();
        } catch (Exception e) {
        }
    }
}
