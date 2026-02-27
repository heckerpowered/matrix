/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity

import heckerpowered.matrix.common.combat.damage.DamageAttemptContext
import heckerpowered.matrix.common.combat.damage.DamageAttemptRule
import heckerpowered.matrix.common.event.LivingDeathCallback
import heckerpowered.matrix.common.item.WardenChestplateItem
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.core.MatrixLivingEntity
import heckerpowered.matrix.core.killed
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.util.ActionResult

/**
 * Types of entity protection. Each type uses a different strategy to determine how the entity is protected.
 */
enum class EntityProtection {
    /**
     * Entities of this type are not affected by any protection strategy.
     */
    NONE,

    /**
     * For entities of this type, alive checks will always return true.
     * Behavior:
     * - `getHealth` is guaranteed not to return a value less than 1.
     * - `getMaxHealth` is guaranteed not to return a value less than 1.
     * - `setHealth` will not allow setting health below 1.
     * - `isAlive` always returns true.
     * - All `onDeath` calls are unconditionally rejected.
     */
    DEAD,

    /**
     * PROTECTED:
     * For entities of this type, alive checks will always return true.
     * Behavior:
     * - `getHealth` is guaranteed not to return a value less than 1.
     * - `getMaxHealth` is guaranteed not to return a value less than 1.
     * - `setHealth` will not allow setting health below 1.
     * - `isAlive` always returns true.
     * - All `onDeath` calls are unconditionally rejected.
     */
    PROTECTED,

    /**
     * PROTECTED_COMPLETE:
     * Extends the behavior of PROTECTED with the following:
     * - All damage is unconditionally rejected, including `damage`, `applyDamage`, and `onDamaged` calls.
     * - `isInvulnerableTo` and `isInvulnerable` always return true.
     * - `getHealth` always returns the same value as `getMaxHealth`.
     * - `getMaxHealth` is guaranteed not to return a value less than 1.
     * - `canHit` and `canTakeDamage` are always false.
     */
    PROTECTED_COMPLETE;

    /**
     * Returns if the protection is [PROTECTED] or [PROTECTED_COMPLETE].
     */
    fun isProtected(): Boolean {
        return this == PROTECTED || this == PROTECTED_COMPLETE
    }

    companion object {
        /**
         * Determines the protection level of an Entity.
         */
        @JvmStatic
        val Entity.protection: EntityProtection
            get() {
                if (this !is LivingEntity || this !is MatrixLivingEntity) {
                    return NONE
                }

                if (WardenChestplateItem.isAngered(this)) {
                    return PROTECTED_COMPLETE
                }

                if (killed) {
                    return DEAD
                }

                return NONE
            }

        init {
            LivingDeathCallback.EVENT.register(::onLivingDeath)
            RuleRegistry.register<DamageAttemptRule>(DamageProtectionRule)
        }

        fun onLivingDeath(entity: LivingEntity, damageSource: DamageSource): ActionResult {
            if (entity.protection.isProtected()) {
                return ActionResult.FAIL
            }

            return ActionResult.PASS
        }

        private object DamageProtectionRule : DamageAttemptRule {
            override fun onAttempt(context: DamageAttemptContext) {
                if (context.target.protection == PROTECTED_COMPLETE) {
                    context.cancel()
                }
            }
        }
    }
}
