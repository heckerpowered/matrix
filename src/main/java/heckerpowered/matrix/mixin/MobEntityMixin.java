package heckerpowered.matrix.mixin;

import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public class MobEntityMixin {
    private MobEntityMixin() {
    }

    private MobEntity self() {
        return (MobEntity) (Object) this;
    }

    private boolean memoryErased() {
        return true;
    }

    @Inject(method = "getTarget", at = @At("HEAD"), cancellable = true)
    private void getTarget(CallbackInfoReturnable<MobEntity> cir) {

    }
}
