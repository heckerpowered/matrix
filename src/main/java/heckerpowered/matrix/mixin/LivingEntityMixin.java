package heckerpowered.matrix.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import heckerpowered.matrix.common.effect.MatrixStatusEffectsKt;
import heckerpowered.matrix.common.event.*;
import heckerpowered.matrix.common.item.RedstoneSuitKt;
import heckerpowered.matrix.common.item.WardenChestplateItem;
import heckerpowered.matrix.common.item.WardenSuitKt;
import heckerpowered.matrix.common.network.SyncManaPayload;
import heckerpowered.matrix.common.persistent.ChannelSequence;
import heckerpowered.matrix.common.persistent.ManaState;
import heckerpowered.matrix.core.MatrixLivingEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(LivingEntity.class)
abstract
class LivingEntityMixin implements MatrixLivingEntity {

    @Unique
    private final Map<UUID, ChannelSequence> channelingSequences = new HashMap<>();

    @Shadow
    @Final
    private Map<RegistryEntry<StatusEffect>, StatusEffectInstance> activeStatusEffects;

    @Shadow
    public abstract @Nullable StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> effect);

    @Shadow
    public abstract ItemStack getEquippedStack(EquipmentSlot slot);

    @Unique
    private LivingEntity self() {
        return (LivingEntity) (Object) this;
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource damageSource, CallbackInfo info) {
        if (!(damageSource.getAttacker() instanceof final ServerPlayerEntity serverPlayer)) {
            return;
        }

        var restoreAmount = 40;
        final var self = self();
        final var manaOverload = MatrixStatusEffectsKt.getManaOverloadEffect();
        final var effect = self.getStatusEffect(manaOverload);
        if (effect != null) {
            restoreAmount += (effect.getAmplifier() + 1) * 20;
        }

        final var manaState = ManaState.getPlayerState(serverPlayer);
        manaState.setMana(manaState.getMana() + restoreAmount);
        ServerPlayNetworking.send(serverPlayer, new SyncManaPayload(manaState.getMana(), manaState.getMaxMana()));
    }

    @Inject(method = "getMaxAbsorption", at = @At("HEAD"), cancellable = true)
    private void getMaxAbsorption(CallbackInfoReturnable<Float> info) {
        info.setReturnValue(Float.POSITIVE_INFINITY);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        WriteDataCallback.event.invoker().writeData(self(), nbt);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        ReadDataCallback.event.invoker().readData(self(), nbt);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        EntityTickCallback.event.invoker().onEntityTick(self());
    }

    @Inject(method = "getArmor", at = @At("TAIL"), cancellable = true)
    private void getArmor(CallbackInfoReturnable<Integer> cir) {
        final var self = self();
        final var armorPenetration = MatrixStatusEffectsKt.getArmorPenetrationEffect();
        final var effect = self.getStatusEffect(armorPenetration);
        if (effect == null) {
            return;
        }

        final var armor = cir.getReturnValue();
        final var percentage = 1 - (effect.getAmplifier() + 1) * 0.2;
        cir.setReturnValue((int) (armor * percentage));
    }

    @Inject(method = "getAttributeValue", at = @At("TAIL"), cancellable = true)
    public void getAttributeValue(RegistryEntry<EntityAttribute> attribute, CallbackInfoReturnable<Double> cir) {
        if (attribute == EntityAttributes.GENERIC_ARMOR_TOUGHNESS) {
            final var self = self();
            final var armorPenetration = MatrixStatusEffectsKt.getArmorPenetrationEffect();
            final var effect = self.getStatusEffect(armorPenetration);
            if (effect == null) {
                return;
            }

            final var thoughness = cir.getReturnValue();
            final var percentage = 1 - (effect.getAmplifier() + 1) * 0.2;
            cir.setReturnValue(thoughness * percentage);
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) LocalRef<DamageSource> sourceReference, @Local(argsOnly = true) LocalFloatRef amountReference) {
        final var attacker = source.getAttacker() instanceof LivingEntity ? (LivingEntity) source.getAttacker() : null;
        final var damageAccumulator = new DamageAccumulator(attacker, self(), source, amount, .0, 1.0, false);
        if (attacker != null) {
            final var result = LivingAttackCallback.event.invoker().onAttack(damageAccumulator);
            if (result == ActionResult.FAIL) {
                cir.setReturnValue(false);
                return;
            }
        }
        final var result = LivingHurtCallback.event.invoker().onHurt(damageAccumulator);
        if (result == ActionResult.FAIL) {
            cir.setReturnValue(false);
        }

        sourceReference.set(damageAccumulator.getDamageSource());
        amountReference.set((float) damageAccumulator.accumulateDamage());
    }

    @Inject(method = "applyDamage", at = @At("HEAD"), cancellable = true)
    private void applyDamage(DamageSource source, float amount, CallbackInfo ci, @Local(argsOnly = true) LocalRef<DamageSource> sourceReference, @Local(argsOnly = true) LocalFloatRef amountReference) {
        final var livingDamageEvent = new LivingDamageEvent(self(), source, amount);
        final var result = LivingDamageCallback.event.invoker().onHurt(livingDamageEvent);
        if (result == ActionResult.FAIL) {
            ci.cancel();
        }

        sourceReference.set(livingDamageEvent.getDamageSource());
        amountReference.set(livingDamageEvent.getAmount());
    }

    @Inject(method = "onStatusEffectRemoved", at = @At(value = "HEAD"), cancellable = true)
    private void onStatusEffectRemoved(StatusEffectInstance effect, CallbackInfo ci) {
        final var result = StatusEffectRemovedCallback.event.invoker().onStatusEffectRemoved(self(), effect);
        if (result == ActionResult.FAIL) {
            ci.cancel();
        }
    }

    @Inject(method = "onEquipStack", at = @At("HEAD"))
    private void onEquipStack(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack, CallbackInfo ci) {
        ItemStackEquippedCallback.event.invoker().onItemStackEquipped(self(), slot, oldStack, newStack);
    }

    @Inject(method = "takeKnockback", at = @At("HEAD"), cancellable = true)
    private void takeKnockback(double strength, double x, double z, CallbackInfo ci, @Local(argsOnly = true, ordinal = 0) LocalDoubleRef strengthReference, @Local(argsOnly = true, ordinal = 1) LocalDoubleRef xReference, @Local(argsOnly = true, ordinal = 2) LocalDoubleRef zReference) {
        final var event = new LivingKnockbackEvent(self(), strength, x, z);
        final var result = LivingKnockbackCallback.event.invoker().onKnockback(event);
        strengthReference.set(event.getStrength());
        xReference.set(event.getX());
        zReference.set(event.getZ());
        if (result == ActionResult.FAIL) {
            ci.cancel();
        }
    }

    @Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
    private void canWalkOnFluid(CallbackInfoReturnable<Boolean> cir) {
        if (WardenSuitKt.isWardenArmorAngered(self())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canHaveStatusEffect", at = @At("HEAD"), cancellable = true)
    private void canHaveStatusEffect(StatusEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
        if (!WardenChestplateItem.isAngered(self())) {
            return;
        }

        final var angeredEffectInstance = getStatusEffect(MatrixStatusEffectsKt.getAngeredEffect());
        if (angeredEffectInstance == null) {
            return;
        }
        if (effect.getEffectType() == StatusEffects.DARKNESS && angeredEffectInstance.getDuration() >= effect.getDuration()) {
            cir.setReturnValue(true);
            return;
        }

        if (!effect.getEffectType().value().isBeneficial()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canBreatheInWater", at = @At("HEAD"), cancellable = true)
    private void canBreatheInWater(CallbackInfoReturnable<Boolean> cir) {
        final var helmet = getEquippedStack(EquipmentSlot.HEAD);
        if (!RedstoneSuitKt.isRedstoneSuit(helmet)) {
            return;
        }
        if (RedstoneSuitKt.getRedstoneSuitPower(helmet) > 0) {
            cir.setReturnValue(true);
        }
    }

    @SuppressWarnings("all")
    @NotNull
    @Override
    public Map<UUID, ChannelSequence> getChannelSequence() {
        return channelingSequences;
    }
}