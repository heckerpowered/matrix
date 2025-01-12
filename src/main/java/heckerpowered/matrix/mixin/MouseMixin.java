package heckerpowered.matrix.mixin;

import heckerpowered.matrix.client.MatrixHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
class MouseMixin {
    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        final var minecraft = MinecraftClient.getInstance();
        if (vertical < 0) {
            var overclocking = false;
            if (minecraft.mouse.wasLeftButtonClicked()) {
                MatrixHud.underclockMagic();
                overclocking = true;
            }
            if (minecraft.mouse.wasRightButtonClicked()) {
                MatrixHud.underclockMana();
                overclocking = true;
            }
            if (!overclocking) {
                MatrixHud.nextMagic();
            }
        } else if (vertical > 0) {
            var underclocking = false;
            if (minecraft.mouse.wasLeftButtonClicked()) {
                MatrixHud.overclockMagic();
                underclocking = true;
            }
            if (minecraft.mouse.wasRightButtonClicked()) {
                MatrixHud.overclockMana();
                underclocking = true;
            }
            if (!underclocking) {
                MatrixHud.previousMagic();
            }
        }
    }
}
