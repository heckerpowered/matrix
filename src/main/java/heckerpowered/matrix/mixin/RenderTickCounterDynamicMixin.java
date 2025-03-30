package heckerpowered.matrix.mixin;

import heckerpowered.matrix.client.TimeController;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.Dynamic.class)
class RenderTickCounterDynamicMixin {
    private RenderTickCounterDynamicMixin() {
    }

    @Inject(method = "getTickDelta", at = @At("HEAD"), cancellable = true)
    public void getTickDelta(boolean bl, CallbackInfoReturnable<Float> cir) {
        if (TimeController.getPlayerStandaloneRenderTick() && TimeController.getPlayerImmuneTimeScale()) {
            // cir.setReturnValue(TimeController.renderTickCounter.tickDelta);
        }
    }
}
