@file:JvmName("MouseMixin")
@file:Mixin(Mouse::class)

package heckerpowered.matrix.mixin

import heckerpowered.matrix.client.MatrixHud
import net.minecraft.client.Mouse
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Inject(method = ["onMouseScroll"], at = [At("HEAD")])
private fun onMouseScroll(window: Long, horizontal: Double, vertical: Double, info: CallbackInfo) {
    if (vertical > 0) {
        MatrixHud.nextMagic()
    } else if (vertical < 0) {
        MatrixHud.previousMagic()
    }
}