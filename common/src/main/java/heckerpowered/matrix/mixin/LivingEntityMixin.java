/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import heckerpowered.matrix.common.combat.damage.*;
import heckerpowered.matrix.common.entity.EntityPolarity;
import heckerpowered.matrix.common.entity.ability.HealMeasurementScope;
import heckerpowered.matrix.common.entity.rule.*;
import heckerpowered.matrix.common.item.RedstoneSuitKt;
import heckerpowered.matrix.common.item.WardenSuitKt;
import heckerpowered.matrix.common.magic.channel.ChannelQueue;
import heckerpowered.matrix.extension.MatrixLivingEntity;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(LivingEntity.class)
@Implements(@Interface(iface = MatrixLivingEntity.class, prefix = "matrix$"))
abstract class LivingEntityMixin extends Entity {
    @Unique
    private final Map<UUID, ChannelQueue> channelQueues = new HashMap<>();
    @Shadow
    public int deathTime;
    @Unique
    private long polarity;
    @Unique
    private float healthSpoofValue;

    public LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    public abstract boolean isAlive();

    @Shadow
    public abstract ItemStack getItemBySlot(EquipmentSlot slot);

    @Unique
    private LivingEntity livingSelf() {
        return (LivingEntity) (Object) this;
    }

    @WrapMethod(method = "die")
    private void wrapDie(DamageSource source, Operation<Void> original) {
        if (livingSelf() instanceof Player) {
            original.call(source);
            return;
        }
        if ((polarity & EntityPolarity.SUPPRESS_DEATH) != 0) {
            return;
        }

        final var context = new LivingDeathContext(livingSelf(), source);
        EntityRulePipeline.onLivingDeath(context);
        if (!context.getAllow()) {
            context.applyDecision();
            return;
        }

        original.call(context.getDamageSource());
    }

    @WrapOperation(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;die(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void die(LivingEntity instance, DamageSource source, Operation<Void> original) {
        if ((polarity & EntityPolarity.SUPPRESS_DEATH) != 0L) {
            return;
        }

        original.call(instance, source);
    }

    @Inject(method = "tickDeath", at = @At("HEAD"), cancellable = true)
    private void tickDeath(CallbackInfo ci) {
        if ((polarity & EntityPolarity.SUPPRESS_DEATH) != 0) {
            deathTime = 0;
            ci.cancel();
        }
    }

    @WrapOperation(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;tickDeath()V"))
    private void tickDeath(LivingEntity instance, Operation<Void> original) {
        if ((polarity & EntityPolarity.SUPPRESS_DEATH) != 0L) {
            deathTime = 0;
            return;
        }

        original.call(instance);
    }

    @Inject(method = "isDeadOrDying", at = @At("HEAD"), cancellable = true)
    private void isDeadOrDying(CallbackInfoReturnable<Boolean> cir) {
        if ((polarity & EntityPolarity.SUPPRESS_DEATH) != 0L) {
            cir.setReturnValue(false);
        } else if ((polarity & EntityPolarity.FORCE_DEATH_CHECK) != 0L) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "actuallyHurt", at = @At("HEAD"), cancellable = true)
    private void actuallyHurt(ServerLevel level, DamageSource source, float dmg, CallbackInfo ci) {
        if ((polarity & EntityPolarity.REJECT_DAMAGE) != 0L) {
            ci.cancel();
        }
    }

    @WrapMethod(method = "hurtServer")
    private boolean wrapHurtServer(ServerLevel level, DamageSource source, float damage, Operation<Boolean> original) {
        if ((polarity & EntityPolarity.REJECT_DAMAGE) != 0L) {
            return false;
        }

        final var self = (LivingEntity) (Object) this;
        final var attemptContext = new DamageAttemptContext(self, source, damage);
        DamagePipeline.attempt(attemptContext);
        if (attemptContext.isCancelled()) {
            return false;
        }

        final var computationContext = new DamageComputationContext(self, source, damage);
        DamagePipeline.computation(computationContext);
        if (computationContext.isCancelled()) {
            return false;
        }

        return original.call(level, new DamageSourceEnvelope(source, damage), computationContext.computeDamage());
    }

    @ModifyVariable(
            method = "actuallyHurt",
            at = @At(value = "STORE"),
            ordinal = 1)
    private float actuallyHurt(
            float dmg,
            ServerLevel level,
            DamageSource source,
            float originalDamage) {
        final var self = (LivingEntity) (Object) this;
        final var originSource = source instanceof final DamageSourceEnvelope envelope ? envelope.getOrigin() : source;
        final float rawDamage = source instanceof final DamageSourceEnvelope envelope ? envelope.getRawDamage() : originalDamage;

        final var realizationContext = new DamageRealizationContext(self, originSource, rawDamage, dmg);
        DamagePipeline.realization(realizationContext);

        final var retention = realizationContext.getRetention();
        final var realizedDamage = realizationContext.getRealizedDamage();
        final var outcomeContext = new DamageOutcomeContext(self, originSource, rawDamage, dmg, retention);
        DamagePipeline.outcome(outcomeContext);

        final var settlementContext = new DamageSettlementContext(self, originSource, rawDamage, dmg, realizedDamage);
        DamagePipeline.settlement(settlementContext);

        return settlementContext.getRemainingDamage();
    }

    /**
     * &#064;Inject(method  = "die", at = @At("HEAD"), cancellable = true)
     * private void onDeath(DamageSource damageSource, CallbackInfo info) {
     * if (LivingDeathCallback.EVENT.invoker().onDeath(livingSelf(), damageSource) == ActionResult.FAIL) {
     * info.cancel();
     * return;
     * }
     * if (!(damageSource.getAttacker() instanceof final ServerPlayerEntity serverPlayer)) {
     * return;
     * }
     * <p>
     * // TODO: Consider to move this logic to the event.
     * var restoreAmount = 0;
     * final var self = livingSelf();
     * final var manaOverload = MatrixStatusEffects.getManaOverloadEffect();
     * final var effect = self.getStatusEffect(manaOverload);
     * if (effect != null) {
     * restoreAmount += (effect.getAmplifier() + 1) * 20;
     * }
     * <p>
     * final var manaState = ManaState.getPlayerState(serverPlayer);
     * manaState.setMana(manaState.getMana() + restoreAmount);
     * ServerPlayNetworking.send(serverPlayer, new ClientboundSyncManaPayload(manaState.getMana(), manaState.getMaxMana()));
     * }
     **/

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void addAdditionalSaveData(ValueOutput output, CallbackInfo ci) {
        LivingPersistencePipeline.save(new LivingSaveContext(livingSelf(), output));
        output.putLong("matrix_polarity", polarity);
        output.putFloat("matrix_health_spoof_value", healthSpoofValue);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readAdditionalSaveData(ValueInput input, CallbackInfo ci) {
        LivingPersistencePipeline.load(new LivingLoadContext(livingSelf(), input));
        input.read("matrix_polarity", Codec.LONG).ifPresent(this::matrix$setPolarity);
        input.read("matrix_health_spoof_value", Codec.FLOAT).ifPresent(this::matrix$setHealthSpoofValue);
    }

    @Inject(method = "getAttributeValue", at = @At("RETURN"), cancellable = true)
    public void getAttributeValue(Holder<Attribute> attribute, CallbackInfoReturnable<Double> cir) {
        final var attributeValue = cir.getReturnValueD();
        final var context = new AttributeComputationContext(livingSelf(), attribute, attributeValue);
        EntityRulePipeline.onComputation(context);
        cir.setReturnValue(context.getFinalValue());
    }

    @Inject(method = "removeEffect", at = @At(value = "HEAD"), cancellable = true)
    private void removeEffect(Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        final var context = new EffectRemovalContext(livingSelf(), effect);
        EntityRulePipeline.onEffectRemoval(context);
        if (!context.isAllowed()) {
            cir.cancel();
        }
    }

    @Inject(method = "onEffectsRemoved", at = @At(value = "RETURN"))
    private void onEffectsRemoved(Collection<MobEffectInstance> effects, CallbackInfo ci) {
        for (final MobEffectInstance effect : effects) {
            final var context = new EffectRemovedContext(livingSelf(), effect);
            EntityRulePipeline.onEffectRemoved(context);
        }
    }

    @Inject(method = "onEquipItem", at = @At("HEAD"))
    private void onEquipStack(EquipmentSlot slot, ItemStack oldStack, ItemStack stack, CallbackInfo ci) {
        final var context = new EquipItemContext(livingSelf(), slot, oldStack, stack);
        EntityRulePipeline.onEquipItem(context);
    }

    @WrapMethod(method = "knockback")
    private void wrapKnockback(double power, double x, double z, Operation<Void> original) {
        final var context = new KnockbackContext(livingSelf(), power, x, z);
        EntityRulePipeline.onKnockback(context);

        if (context.isCancelled()) {
            return;
        }

        original.call(context.getPower(), context.getX(), context.getZ());
    }

    @Inject(method = "canStandOnFluid", at = @At("HEAD"), cancellable = true)
    private void canWalkOnFluid(FluidState fluid, CallbackInfoReturnable<Boolean> cir) {
        // Remove condition "fluidState != Fluids.EMPTY.getDefaultState()"
        // may cause AI pathfinding infinite loop.
        //
        // Yarn: (LandPathNodeMaker#getStart) -> while (this.entity.canWalkOnFluid(blockState.getFluidState())) ...
        // Official: (WalkNodeEvaluator#getStart) -> while (this.mob.canStandOnFluid(blockState.getFluidState())) ...
        //
        // The loop relies on eventually reaching the first fluid state the entity cannot
        // stand on, then stepping back down to the walkable fluid surface. Air blocks use
        // Fluids.EMPTY as their fluid state. If EMPTY is also treated as walkable, the
        // search may keep moving upward through air forever and cause an infinite loop.
        if (WardenSuitKt.isWardenArmorAngered(livingSelf()) && fluid != Fluids.EMPTY.defaultFluidState()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canBeAffected", at = @At("RETURN"), cancellable = true)
    private void canBeAffected(MobEffectInstance newEffect, CallbackInfoReturnable<Boolean> cir) {
        final var context = new EffectRestrictionContext(livingSelf(), newEffect);
        EntityRulePipeline.canBeAffected(context);
        if (!context.isAllowed()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canBreatheUnderwater", at = @At("HEAD"), cancellable = true)
    private void canBreatheUnderwater(CallbackInfoReturnable<Boolean> cir) {
        final var helmet = getItemBySlot(EquipmentSlot.HEAD);
        if (!RedstoneSuitKt.isRedstoneSuit(helmet)) {
            return;
        }
        if (RedstoneSuitKt.getRedstoneSuitPower(helmet) > 0) {
            cir.setReturnValue(true);
        }
    }


    @WrapMethod(method = "heal")
    private void heal(float heal, Operation<Void> original) {
        final var previousHealth = livingSelf().getHealth();

        final var context = new LivingHealContext(livingSelf(), heal);
        EntityRulePipeline.onHeal(context);

        final var resolvedAmount = (float) (context.getHealAmount() * context.getMultiplier());
        heal = resolvedAmount;

        original.call(heal);

        final var stack = HealMeasurementScope.current();
        if (stack != null) {
            stack.setResolvedAmount(resolvedAmount);

            final var restoredHealth = livingSelf().getHealth() - previousHealth;
            stack.setRestoredHealth(Math.max(restoredHealth, 0));
        }
    }

    @Unique
    private float getSpoofedHealth() {
        return Math.max(healthSpoofValue, 1.0F);
    }

    @Inject(method = "getHealth", at = @At("RETURN"), cancellable = true, order = Integer.MAX_VALUE)
    private void getHealth(CallbackInfoReturnable<Float> cir) {
        final var health = cir.getReturnValueF();
        if ((polarity & EntityPolarity.HEALTH_SPOOF) != 0L) {
            cir.setReturnValue(Math.max(health, getSpoofedHealth()));
        } else if ((polarity & EntityPolarity.ZERO_HEALTH_SPOOF) != 0) {
            cir.setReturnValue(0F);
        }
    }

    @Inject(method = "getMaxHealth", at = @At("RETURN"), cancellable = true, order = Integer.MAX_VALUE)
    private void getMaxHealth(CallbackInfoReturnable<Float> cir) {
        final var maxHealth = cir.getReturnValueF();
        if ((polarity & EntityPolarity.HEALTH_SPOOF) != 0) {
            cir.setReturnValue(Math.max(maxHealth, getSpoofedHealth()));
        }
    }

    @ModifyVariable(method = "setHealth", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float setHealth(float health) {
        if ((polarity & EntityPolarity.HEALTH_SPOOF) != 0L) {
            return Math.max(health, getSpoofedHealth());
        } else if ((polarity & EntityPolarity.ZERO_HEALTH_SPOOF) != 0) {
            return 0F;
        }
        return health;
    }

    @Inject(method = "attackable", at = @At("HEAD"), cancellable = true)
    private void attackable(CallbackInfoReturnable<Boolean> cir) {
        if ((polarity & EntityPolarity.UNTARGETABLE) != 0L) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void isInvulnerableTo(CallbackInfoReturnable<Boolean> cir) {
        if ((polarity & EntityPolarity.INVULNERABLE) != 0L) {
            cir.setReturnValue(true);
        }
    }

    public long matrix$getPolarity() {
        return polarity;
    }

    public void matrix$setPolarity(final long polarity) {
        this.polarity = polarity;
    }

    public float matrix$getHealthSpoofValue() {
        return healthSpoofValue;
    }

    public void matrix$setHealthSpoofValue(float healthSpoofValue) {
        this.healthSpoofValue = healthSpoofValue;
    }

    public @NonNull Map<UUID, ChannelQueue> matrix$getChannelQueues() {
        return channelQueues;
    }
}
