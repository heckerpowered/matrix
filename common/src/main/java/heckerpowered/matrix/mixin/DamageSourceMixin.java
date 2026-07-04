/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.extension.MatrixDamageSource;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSource.class)
class DamageSourceMixin implements MatrixDamageSource {
    @Unique
    private boolean isAdditionalDamage = false;

    @Override
    public boolean isAdditionalDamage() {
        return isAdditionalDamage;
    }

    @Override
    public void setAdditionalDamage(boolean b) {
        isAdditionalDamage = b;
    }
}