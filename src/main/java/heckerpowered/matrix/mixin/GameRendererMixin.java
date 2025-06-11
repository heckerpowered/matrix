package heckerpowered.matrix.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import heckerpowered.matrix.client.MatrixHud;
import heckerpowered.matrix.client.TimeController;
import heckerpowered.matrix.client.render.PostProcessRenderer;
import heckerpowered.matrix.client.shader.BlurRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static heckerpowered.matrix.common.item.LightningChestplate1.isPhaseWalking;

@Mixin(GameRenderer.class)
class GameRendererMixin {
    @Shadow
    @Final
    private Camera camera;

    private GameRendererMixin() {
    }

    @Inject(method = "onResized", at = @At("HEAD"))
    private void onResized(int width, int height, CallbackInfo ci) {
        // UIBlurShader.setupDimensions(width, height);
        BlurRenderer.onResize(width, height);
        PostProcessRenderer.onResize(width, height);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", shift = At.Shift.BEFORE))
    private void beginRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        PostProcessRenderer.renderToMinecraftFramebuffer();
    }

    @Inject(method = "render", at = @At(value = "TAIL"))
    private void endRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        // PostProcessRenderer.endHudRender();
    }

    @Inject(method = "getFov", at = @At("TAIL"), cancellable = true)
    private void getFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        final var returnValue = cir.getReturnValueD();
        cir.setReturnValue(returnValue * MatrixHud.fovAnimation.getAnimatedValue());
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V"))
    private void updateCamera(Camera instance, BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta) {
        if (TimeController.getPlayerStandaloneRenderTick()) {
            camera.update(area, focusedEntity, thirdPerson, inverseView, TimeController.standaloneRenderTickCounter.getTickDelta(true));
        } else {
            camera.update(area, focusedEntity, thirdPerson, inverseView, tickDelta);
        }
    }

    @Inject(method = "getFov", at = @At("HEAD"))
    private void getFovHead(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir, @Local(argsOnly = true) LocalFloatRef tickDeltaRef) {
        if (TimeController.getPlayerStandaloneRenderTick()) {
            tickDeltaRef.set(TimeController.standaloneRenderTickCounter.getTickDelta(true));
        }
    }

    @Inject(method = "renderHand", at = @At("HEAD"))
    private void renderHand(Camera camera, float tickDelta, Matrix4f matrix4f, CallbackInfo ci, @Local(argsOnly = true) LocalFloatRef tickDeltaRef) {
        if (TimeController.getPlayerStandaloneRenderTick()) {
            tickDeltaRef.set(TimeController.standaloneRenderTickCounter.getTickDelta(true));
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void bobView(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        final var minecraft = MinecraftClient.getInstance();
        final var player = minecraft.player;
        if (player != null && isPhaseWalking(player)) {
            ci.cancel();
        }
    }
}
