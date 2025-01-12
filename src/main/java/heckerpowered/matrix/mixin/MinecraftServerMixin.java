package heckerpowered.matrix.mixin;

import heckerpowered.matrix.core.MatrixMinecraftServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickManager;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
class MinecraftServerMixin implements MatrixMinecraftServer {

    @Shadow
    private ServerTickManager tickManager;

    @Unique
    long matrixTickStartTimeNanos;

    @Unique
    long matrixTickEndTimeNanos;

    private MinecraftServerMixin() {
    }

    @SuppressWarnings("all")
    @Override
    public long getMatrixTickStartTimeNanos() {
        return matrixTickStartTimeNanos;
    }

    @SuppressWarnings("all")
    @Override
    public void setMatrixTickStartTimeNanos(long l) {
        matrixTickStartTimeNanos = l;
    }

    @SuppressWarnings("all")
    @Override
    public long getMatrixTickEndTimeNanos() {
        return matrixTickEndTimeNanos;
    }

    @SuppressWarnings("all")
    @Override
    public void setMatrixTickEndTimeNanos(long l) {
        matrixTickStartTimeNanos = l;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        matrixTickStartTimeNanos = Util.getMeasuringTimeNano();
        matrixTickEndTimeNanos = matrixTickStartTimeNanos + tickManager.getNanosPerTick();
    }
}
