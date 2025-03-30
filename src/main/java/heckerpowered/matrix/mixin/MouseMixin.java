package heckerpowered.matrix.mixin;

import heckerpowered.matrix.client.MatrixHud;
import heckerpowered.matrix.client.core.AimAssist;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
class MouseMixin {
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (!MatrixHud.shouldRenderHud()) {
            return;
        }
        final var minecraft = MinecraftClient.getInstance();
        if (vertical < 0) {
            if (MatrixHud.isPressingRightMouseButton) {
                MatrixHud.previousZoomLevel();
                ci.cancel();
            } else {
                MatrixHud.nextMagic();
                ci.cancel();
            }
        } else if (vertical > 0) {
            if (MatrixHud.isPressingRightMouseButton) {
                MatrixHud.nextZoomLevel();
                ci.cancel();
            } else {
                MatrixHud.previousMagic();
                ci.cancel();
            }
        }
    }

    @Inject(method = "updateMouse", at = @At("TAIL"))
    private void updateMouse(double timeDelta, CallbackInfo ci) {
        AimAssist.onMouseUpdate(timeDelta);
    }

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (MatrixHud.onMouseButton(window, button, action, mods)) {
            ci.cancel();
        }
    }
}
