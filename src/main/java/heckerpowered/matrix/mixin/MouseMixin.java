package heckerpowered.matrix.mixin;

import heckerpowered.matrix.client.MatrixHud;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
class MouseMixin {
    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (vertical > 0) {
            MatrixHud.nextMagic();
        } else if (vertical < 0) {
            MatrixHud.previousMagic();
        }
    }
}
