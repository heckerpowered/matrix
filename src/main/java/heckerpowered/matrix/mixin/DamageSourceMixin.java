/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.extension.DamageSourceExtension;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSource.class)
class DamageSourceMixin implements DamageSourceExtension {
    @Unique
    private boolean isAdditionalDamage = false;

    @Override
    public boolean matrix$isAdditionalDamage() {
        return isAdditionalDamage;
    }

    @Override
    public void matrix$setAdditionalDamage(boolean value) {
        isAdditionalDamage = value;
    }
}