/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.extension.MatrixLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

import static heckerpowered.matrix.common.item.LightningChestplate1.isBorrowedTime;

@Mixin(Level.class)
@Implements(@Interface(iface = MatrixLevel.class, prefix = "matrix$"))
abstract
class LevelMixin implements MatrixLevel {
    @Shadow
    protected abstract LevelEntityGetter<Entity> getEntities();

    private LevelMixin() {
    }

    public @NotNull LevelEntityGetter<@NotNull Entity> matrix$getEntityGetter() {
        return getEntities();
    }


    // @Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
    // private void tickEntity(Consumer<Entity> tickConsumer, Entity entity, CallbackInfo ci) {
    //     if (entity instanceof final PlayerEntity player && isBorrowedTime(player)) {
    //         // Player is standalone ticking.
    //         ci.cancel();
    //     }
    // }
}
