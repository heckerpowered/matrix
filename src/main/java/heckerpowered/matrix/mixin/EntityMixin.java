package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.event.EntityRemovedCallback;
import net.minecraft.entity.Entity;
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
        EntityRemovedCallback.EVENT.invoker().onEntityRemoved((Entity) (Object) this, reason);
    }
}
