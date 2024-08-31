package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.effect.MatrixStatusEffects;
import heckerpowered.matrix.common.network.SyncManaPayload;
import heckerpowered.matrix.common.persistent.ManaState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
class LivingEntityMixin {
    @Unique
    private LivingEntity self() {
        return (LivingEntity) (Object) this;
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource damageSource, CallbackInfo info) {
        final var self = self();
        final var manaOverload = Registries.STATUS_EFFECT.getEntry(MatrixStatusEffects.manaOverload);
        if (!self.hasStatusEffect(manaOverload)) {
            return;
        }

        if (!(damageSource.getAttacker() instanceof final ServerPlayerEntity serverPlayer)) {
            return;
        }

        final var manaState = ManaState.getPlayerState(serverPlayer);
        manaState.setMana(manaState.getMana() + 20);
        ServerPlayNetworking.send(serverPlayer, new SyncManaPayload(manaState.getMana(), manaState.getMaxMana()));
    }

    @Inject(method = "getMaxAbsorption", at = @At("HEAD"), cancellable = true)
    private void getMaxAbsorption(CallbackInfoReturnable<Float> info) {
        final var self = self();
        if (self instanceof final ServerPlayerEntity serverPlayer) {
            info.setReturnValue(Float.POSITIVE_INFINITY);
        }
    }
}
