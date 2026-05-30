/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.extension.MatrixDamageSource;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSource.class)
@Implements(@Interface(iface = MatrixDamageSource.class, prefix = "matrix$"))
class DamageSourceMixin implements MatrixDamageSource {
    @Unique
    private boolean isAdditionalDamage = false;

    public boolean matrix$isAdditionalDamage() {
        return isAdditionalDamage;
    }

    public void matrix$setAdditionalDamage(boolean b) {
        isAdditionalDamage = b;
    }
}