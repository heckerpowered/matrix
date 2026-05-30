/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.entity.EntityPolarity;
import heckerpowered.matrix.common.entity.rule.EntityRulePipeline;
import heckerpowered.matrix.common.entity.rule.EntityUpdateContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
class EntityMixin {
    private EntityMixin() {
    }

    @Unique
    private Entity self() {
        return (Entity) (Object) this;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        EntityRulePipeline.onUpdate(new EntityUpdateContext(self()));
    }

    @Inject(method = "setPos(DDD)V", at = @At("HEAD"), cancellable = true)
    private void setPos(double x, double y, double z, CallbackInfo ci) {
        if (!(self() instanceof final LivingEntity self)) return;
        if ((self.getPolarity() & EntityPolarity.CRIPPLE_MOVEMENT) != 0L) {
            ci.cancel();
        }
    }

    @Inject(method = "setPosRaw", at = @At("HEAD"), cancellable = true)
    private void setPosRaw(double x, double y, double z, CallbackInfo ci) {
        if (!(self() instanceof final LivingEntity self)) return;
        if ((self.getPolarity() & EntityPolarity.CRIPPLE_MOVEMENT) != 0L) {
            ci.cancel();
        }
    }


    @Inject(method = "fireImmune", at = @At("HEAD"), cancellable = true)
    private void isFireImmune(CallbackInfoReturnable<Boolean> cir) {
        if (!(self() instanceof final LivingEntity self)) return;
        if ((self.getPolarity() & EntityPolarity.FIRE_IMMUNE) != 0L) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "makeStuckInBlock", at = @At("HEAD"), cancellable = true)
    private void makeStuckInBlock(BlockState blockState, Vec3 speedMultiplier, CallbackInfo ci) {
        if (!(self() instanceof final LivingEntity self)) return;
        if ((self.getPolarity() & EntityPolarity.SLOW_IMMUNE) != 0L) {
            ci.cancel();
        }
    }
}
