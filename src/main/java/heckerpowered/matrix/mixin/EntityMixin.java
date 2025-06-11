package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.effect.MatrixStatusEffects;
import heckerpowered.matrix.common.entity.EntityProtection;
import heckerpowered.matrix.common.event.EntityRemovedCallback;
import heckerpowered.matrix.common.item.WardenChestplateItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
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

    @Inject(method = "remove", at = @At("HEAD"))
    private void onRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        EntityRemovedCallback.EVENT.invoker().onEntityRemoved((Entity) (Object) this, reason);
    }

    @Inject(method = "setPos", at = @At("HEAD"), cancellable = true)
    private void setPos(double x, double y, double z, CallbackInfo ci) {
        if (!(self() instanceof final LivingEntity self)) {
            return;
        }

        // This function may be called before the living entity's constructor is called,
        // exception will be thrown in that case.
        try {
            final var crippleMovement = MatrixStatusEffects.getCrippleMovementEffect();
            final var effect = self.getStatusEffect(crippleMovement);
            if (effect == null || effect.getDuration() <= 0) {
                return;
            }

            ci.cancel();
        } catch (Exception ignored) {
        }
    }

    @Inject(method = "isFireImmune", at = @At("HEAD"), cancellable = true)
    private void isFireImmune(CallbackInfoReturnable<Boolean> cir) {
        if (!(self() instanceof final LivingEntity self)) {
            return;
        }

        if (WardenChestplateItem.isAngered(self)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "slowMovement", at = @At("HEAD"), cancellable = true)
    private void slowMovement(BlockState state, Vec3d multiplier, CallbackInfo ci) {
        if (!(self() instanceof final LivingEntity self)) {
            return;
        }

        if (WardenChestplateItem.isAngered(self)) {
            ci.cancel();
        }
    }

    @Inject(method = "addVelocity(DDD)V", at = @At("HEAD"), cancellable = true)
    private void addVelocity(double deltaX, double deltaY, double deltaZ, CallbackInfo ci) {
        if (!(self() instanceof final LivingEntity self)) {
            return;
        }

        if (WardenChestplateItem.isAngered(self)) {
            ci.cancel();
        }
    }

    @Inject(method = "isInvulnerable", at = @At("TAIL"), cancellable = true)
    private void isInvulnerable(CallbackInfoReturnable<Boolean> cir) {
        final var protection = EntityProtection.getProtection(self());
        if (protection == EntityProtection.PROTECTED_COMPLETE) {
            cir.setReturnValue(true);
        }
    }
}
