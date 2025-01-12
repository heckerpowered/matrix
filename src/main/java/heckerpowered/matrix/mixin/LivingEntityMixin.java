package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.effect.MatrixStatusEffects;
import heckerpowered.matrix.common.event.EntityTickCallback;
import heckerpowered.matrix.common.event.ReadDataCallback;
import heckerpowered.matrix.common.event.WriteDataCallback;
import heckerpowered.matrix.common.network.SyncManaPayload;
import heckerpowered.matrix.common.persistent.ChannelSequence;
import heckerpowered.matrix.common.persistent.ManaState;
import heckerpowered.matrix.core.MatrixLivingEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(LivingEntity.class)
class LivingEntityMixin implements MatrixLivingEntity {

    private final Map<UUID, ChannelSequence> channelingSequences = new HashMap<>();

    @Unique
    private LivingEntity self() {
        return (LivingEntity) (Object) this;
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource damageSource, CallbackInfo info) {
        final var self = self();
        final var manaOverload = Registries.STATUS_EFFECT.getEntry(MatrixStatusEffects.manaOverload);
        final var effect = self.getStatusEffect(manaOverload);
        if (effect == null) {
            return;
        }

        if (!(damageSource.getAttacker() instanceof final ServerPlayerEntity serverPlayer)) {
            return;
        }

        final var manaState = ManaState.getPlayerState(serverPlayer);
        manaState.setMana(manaState.getMana() + 20 * (effect.getAmplifier() + 1));
        ServerPlayNetworking.send(serverPlayer, new SyncManaPayload(manaState.getMana(), manaState.getMaxMana()));
    }

    @Inject(method = "getMaxAbsorption", at = @At("HEAD"), cancellable = true)
    private void getMaxAbsorption(CallbackInfoReturnable<Float> info) {
        final var self = self();
        if (self instanceof ServerPlayerEntity) {
            info.setReturnValue(Float.POSITIVE_INFINITY);
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        WriteDataCallback.EVENT.invoker().writeData(self(), nbt);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        ReadDataCallback.EVENT.invoker().readData(self(), nbt);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        EntityTickCallback.EVENT.invoker().onEntityTick(self());
    }

    @SuppressWarnings("all")
    @NotNull
    @Override
    public Map<UUID, ChannelSequence> getChannelSequence() {
        return channelingSequences;
    }
}
