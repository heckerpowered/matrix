/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import heckerpowered.matrix.Matrix;
import heckerpowered.matrix.common.combat.damage.*;
import heckerpowered.matrix.common.effect.MatrixStatusEffects;
import heckerpowered.matrix.common.entity.EntityProtection;
import heckerpowered.matrix.common.event.*;
import heckerpowered.matrix.common.item.RedstoneSuitKt;
import heckerpowered.matrix.common.item.WardenChestplateItem;
import heckerpowered.matrix.common.item.WardenSuitKt;
import heckerpowered.matrix.common.magic.channel.ChannelQueue;
import heckerpowered.matrix.common.network.SyncManaPayload;
import heckerpowered.matrix.common.persistent.ManaState;
import heckerpowered.matrix.core.Accumulator;
import heckerpowered.matrix.core.MatrixLivingEntity;
import heckerpowered.matrix.core.extension.LivingEntityExtension;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity implements MatrixLivingEntity {

    @SuppressWarnings("WrongEntityDataParameterClass")
    @Unique
    private static final TrackedData<Boolean> KILLED = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    @Unique
    private final Map<UUID, ChannelQueue> channelQueues = new HashMap<>();

    @Shadow
    @Final
    private Map<RegistryEntry<StatusEffect>, StatusEffectInstance> activeStatusEffects;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract @Nullable StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> effect);

    @Shadow
    public abstract ItemStack getEquippedStack(EquipmentSlot slot);

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    public abstract boolean isAlive();

    @Intrinsic(displace = true)
    private LivingEntity self() {
        return (LivingEntity) (Object) this;
    }

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void onDeath(DamageSource damageSource, CallbackInfo info) {
        if (LivingDeathCallback.EVENT.invoker().onDeath(self(), damageSource) == ActionResult.FAIL) {
            info.cancel();
            return;
        }
        if (!(damageSource.getAttacker() instanceof final ServerPlayerEntity serverPlayer)) {
            return;
        }

        // TODO: Consider to move this logic to the event.
        var restoreAmount = 0;
        final var self = self();
        final var manaOverload = MatrixStatusEffects.getManaOverloadEffect();
        final var effect = self.getStatusEffect(manaOverload);
        if (effect != null) {
            restoreAmount += (effect.getAmplifier() + 1) * 20;
        }

        final var manaState = ManaState.getPlayerState(serverPlayer);
        manaState.setMana(manaState.getMana() + restoreAmount);
        ServerPlayNetworking.send(serverPlayer, new SyncManaPayload(manaState.getMana(), manaState.getMaxMana()));
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        WriteDataCallback.EVENT.invoker().writeData(self(), nbt);
        final var matrixCompound = nbt.getCompound("MatrixMod");
        matrixCompound.putBoolean("Killed", dataTracker.get(KILLED));
        nbt.put("MatrixMod", matrixCompound);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        ReadDataCallback.EVENT.invoker().readData(self(), nbt);
        final var matrixCompound = nbt.getCompound("MatrixMod");
        dataTracker.set(KILLED, matrixCompound.getBoolean("Killed"));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        EntityTickCallback.EVENT.invoker().onEntityTick(self());
    }

    @Inject(method = "getArmor", at = @At("TAIL"), cancellable = true)
    private void getArmor(CallbackInfoReturnable<Integer> cir) {
        final var thoughness = cir.getReturnValueI();
        final var accumulator = new Accumulator(thoughness);
        GetArmorCallback.EVENT.invoker().getArmor(self(), accumulator);

        final var result = accumulator.accumulate();
        cir.setReturnValue((int) Math.round(result));
    }

    @Inject(method = "getAttributeValue", at = @At("TAIL"), cancellable = true)
    public void getAttributeValue(RegistryEntry<EntityAttribute> attribute, CallbackInfoReturnable<Double> cir) {
        final var attributeValue = cir.getReturnValueD();
        final var accumulator = new Accumulator(attributeValue);
        AccumulateAttributeValueCallback.EVENT.invoker().getAttributeValue(self(), attribute, accumulator);

        final var result = accumulator.accumulate();
        cir.setReturnValue(result);
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void damage(
            DamageSource source,
            float amount,
            CallbackInfoReturnable<Boolean> cir,
            @Local(argsOnly = true) LocalFloatRef amountReference,
            @Share(value = "rawDamage", namespace = Matrix.MOD_ID) LocalFloatRef rawDamageReference
    ) {
        final var self = (LivingEntity) (Object) this;
        final var attemptContext = new DamageAttemptContext(self, source, amount);
        DamagePipeline.attempt(attemptContext);
        if (attemptContext.isCancelled()) {
            cir.setReturnValue(false);
            return;
        }

        rawDamageReference.set(amount);
        final var computationContext = new DamageComputationContext(self, source, amount);
        DamagePipeline.computation(computationContext);
        if (computationContext.isCancelled()) {
            cir.setReturnValue(false);
            return;
        }

        amountReference.set(computationContext.computeDamage());
    }

    @ModifyArg(
            method = "damage",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"),
            index = 0
    )
    private DamageSource wrapSource(DamageSource source, @Share(value = "rawDamage", namespace = Matrix.MOD_ID) LocalFloatRef rawDamageReference) {
        return new DamageSourceEnvelope(source, rawDamageReference.get());
    }

    @ModifyVariable(
            method = "applyDamage",
            at = @At("HEAD"),
            argsOnly = true
    )
    private DamageSource unwrapSource(DamageSource source, @Share(value = "rawDamage", namespace = Matrix.MOD_ID) LocalFloatRef rawDamageReference) {
        if (source instanceof final DamageSourceEnvelope envelope) {
            rawDamageReference.set(envelope.getRawDamage());
            return envelope.getOrigin();
        }
        return source;
    }

    @SuppressWarnings("DuplicatedCode")
    @Definition(id = "modifyAppliedDamage", method = "Lnet/minecraft/entity/LivingEntity;modifyAppliedDamage(Lnet/minecraft/entity/damage/DamageSource;F)F")
    @Expression("? = ?.modifyAppliedDamage(?, ?)")
    @ModifyVariable(
            method = "applyDamage",
            at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER), argsOnly = true)
    private float applyDamage(
            float amount,
            @Local(argsOnly = true) DamageSource source,
            @Share(value = "rawDamage", namespace = Matrix.MOD_ID) LocalFloatRef rawDamageReference) {
        final var self = (LivingEntity) (Object) this;
        final var rawDamage = rawDamageReference.get();
        final var realizationContext = new DamageRealizationContext(self, source, rawDamage, amount);
        DamagePipeline.realization(realizationContext);

        final var retention = realizationContext.getRetention();
        final var realizedDamage = realizationContext.getRealizedDamage();
        final var outcomeContext = new DamageOutcomeContext(self, source, rawDamage, amount, retention);
        DamagePipeline.outcome(outcomeContext);

        final var settlementContext = new DamageSettlementContext(self, source, rawDamage, amount, realizedDamage);
        DamagePipeline.settlement(settlementContext);

        return settlementContext.getRemainingDamage();
    }

    // @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    // private void damage(
    //         DamageSource source,
    //         float amount,
    //         CallbackInfoReturnable<Boolean> cir,
    //         @Local(argsOnly = true) LocalRef<DamageSource> sourceReference,
    //         @Local(argsOnly = true) LocalFloatRef amountReference,
    //         @Share(value = "rawDamage", namespace = Matrix.MOD_ID) LocalFloatRef rawDamageReference
    // ) {
    //     final var attacker = source.getAttacker() instanceof LivingEntity ? (LivingEntity) source.getAttacker() : null;
    //     final var damageAccumulator = new DamageAccumulator(attacker, self(), source, amount, .0, 1.0, 1.0, false);
    //     if (attacker != null) {
    //         final var result = LivingAttackCallback.EVENT.invoker().onAttack(damageAccumulator);
    //         if (result == ActionResult.FAIL) {
    //             cir.setReturnValue(false);
    //             return;
    //         }
    //     }
    //     final var result = LivingHurtCallback.EVENT.invoker().onHurt(damageAccumulator);
    //     if (result == ActionResult.FAIL) {
    //         cir.setReturnValue(false);
    //     }
//
    //     sourceReference.set(damageAccumulator.getDamageSource());
    //     amountReference.set((float) damageAccumulator.accumulateDamage());
    // }

    // @Inject(method = "applyDamage", at = @At("HEAD"), cancellable = true)
    // private void applyDamage(DamageSource source, float amount, CallbackInfo ci, @Local(argsOnly = true) LocalRef<DamageSource> sourceReference, @Local(argsOnly = true) LocalFloatRef amountReference) {
    //     final var livingDamageEvent = new LivingDamageEvent(self(), source, amount);
    //     final var result = LivingDamageCallback.EVENT.invoker().onHurt(livingDamageEvent);
    //     if (result == ActionResult.FAIL) {
    //         ci.cancel();
    //     }
//
    //     sourceReference.set(livingDamageEvent.getDamageSource());
    //     amountReference.set(livingDamageEvent.getAmount());
    // }

    @Inject(method = "onStatusEffectRemoved", at = @At(value = "HEAD"), cancellable = true)
    private void onStatusEffectRemoved(StatusEffectInstance effect, CallbackInfo ci) {
        final var result = StatusEffectRemovedCallback.EVENT.invoker().onStatusEffectRemoved(self(), effect);
        if (result == ActionResult.FAIL) {
            ci.cancel();
        }
    }

    @Inject(method = "onEquipStack", at = @At("HEAD"))
    private void onEquipStack(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack, CallbackInfo ci) {
        ItemStackEquippedCallback.EVENT.invoker().onItemStackEquipped(self(), slot, oldStack, newStack);
    }

    @Inject(method = "takeKnockback", at = @At("HEAD"), cancellable = true)
    private void takeKnockback(double strength, double x, double z, CallbackInfo ci, @Local(argsOnly = true, ordinal = 0) LocalDoubleRef strengthReference, @Local(argsOnly = true, ordinal = 1) LocalDoubleRef xReference, @Local(argsOnly = true, ordinal = 2) LocalDoubleRef zReference) {
        final var event = new LivingKnockbackEvent(self(), strength, x, z);
        final var result = LivingKnockbackCallback.EVENT.invoker().onKnockback(event);
        strengthReference.set(event.getStrength());
        xReference.set(event.getX());
        zReference.set(event.getZ());
        if (result == ActionResult.FAIL) {
            ci.cancel();
        }
    }

    @Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
    private void canWalkOnFluid(FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        // Remove condition "fluidState != Fluids.EMPTY.getDefaultState()"
        // may cause AI pathfinding infinite loop.
        // (LandPathNodeMaker#getStart) -> while (this.entity.canWalkOnFluid(blockState.getFluidState())) ...
        if (WardenSuitKt.isWardenArmorAngered(self()) && fluidState != Fluids.EMPTY.getDefaultState()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canHaveStatusEffect", at = @At("HEAD"), cancellable = true)
    private void canHaveStatusEffect(StatusEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
        final var result = CanHaveStatusEffectCallback.EVENT.invoker().canHaveStatusEffect(self(), effect);
        if (result == ActionResult.FAIL) {
            cir.setReturnValue(false);
            return;
        }

        if (!WardenChestplateItem.isAngered(self())) {
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
    public Map<UUID, ChannelQueue> getChannelQueues() {
        return channelQueues;
    }

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void heal(float amount, CallbackInfo ci, @Local(argsOnly = true) LocalFloatRef amountReference) {
        final var livingHealEvent = new LivingHealEvent(self(), amount);
        final var result = LivingHealCallback.EVENT.invoker().onHeal(livingHealEvent);
        if (result == ActionResult.FAIL) {
            ci.cancel();
        }

        amountReference.set(livingHealEvent.getAmount());
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(KILLED, false);
    }

    @Inject(method = "getHealth", at = @At("TAIL"), cancellable = true)
    private void getHealth(CallbackInfoReturnable<Float> cir) {
        final var protection = EntityProtection.getProtection(self());
        switch (protection) {
            case NONE -> {
            }
            case DEAD -> cir.setReturnValue(.0F);
            case PROTECTED -> cir.setReturnValue(Math.max(cir.getReturnValueF(), 1.0F));
            case PROTECTED_COMPLETE -> cir.setReturnValue(Math.max(getMaxHealth(), 20.0F));
        }
    }

    @Inject(method = "getMaxHealth", at = @At("TAIL"), cancellable = true)
    private void getMaxHealth(CallbackInfoReturnable<Float> cir) {
        final var maxHealth = cir.getReturnValueF();
        final var protection = EntityProtection.getProtection(self());
        switch (protection) {
            case NONE, DEAD -> {
            }
            case PROTECTED -> {
                if (Float.isNaN(maxHealth)) {
                    cir.setReturnValue(1.0F);
                } else {
                    cir.setReturnValue(Math.max(maxHealth, 1.0F));
                }
            }
            case PROTECTED_COMPLETE -> {
                if (Float.isNaN(maxHealth)) {
                    cir.setReturnValue(20.0F);
                } else {
                    cir.setReturnValue(Math.max(maxHealth, 20.0F));
                }
            }
        }
    }

    @Inject(method = "setHealth", at = @At("HEAD"))
    private void setHealth(float health, CallbackInfo ci, @Local(argsOnly = true) LocalFloatRef healthReference) {
        final var protection = EntityProtection.getProtection(self());
        switch (protection) {
            case NONE -> {
            }
            case DEAD -> healthReference.set(0F);
            case PROTECTED -> {
                if (health < 1.0F || Float.isNaN(health)) {
                    healthReference.set(1.0F);
                }
            }
            case PROTECTED_COMPLETE -> healthReference.set(getMaxHealth());
        }
    }

    @Inject(method = "isAlive", at = @At("HEAD"), cancellable = true)
    private void isAlive(CallbackInfoReturnable<Boolean> cir) {
        final var protection = EntityProtection.getProtection(self());
        if (protection.isProtected()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canHit", at = @At("HEAD"), cancellable = true)
    private void canHit(CallbackInfoReturnable<Boolean> cir) {
        final var protection = EntityProtection.getProtection(self());
        if (protection == EntityProtection.PROTECTED_COMPLETE) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canTakeDamage", at = @At("HEAD"), cancellable = true)
    private void canTakeDamage(CallbackInfoReturnable<Boolean> cir) {
        final var protection = EntityProtection.getProtection(self());
        if (protection == EntityProtection.PROTECTED_COMPLETE) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void isInvulnerableTo(CallbackInfoReturnable<Boolean> cir) {
        final var protection = EntityProtection.getProtection(self());
        if (protection == EntityProtection.PROTECTED_COMPLETE) {
            cir.setReturnValue(true);
        }
    }

    @ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true)
    private float modifyHealAmount(float amount) {
        return (float) (amount * LivingEntityExtension.getHealingMultiplier((LivingEntity) (Object) this));
    }

    @Unique
    @Override
    public boolean getMatrix$killed() {
        return dataTracker.get(KILLED);
    }

    @Unique
    @Override
    public void setMatrix$killed(boolean killed) {
        dataTracker.set(KILLED, killed);
    }
}