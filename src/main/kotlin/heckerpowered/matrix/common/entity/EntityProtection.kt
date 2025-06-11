package heckerpowered.matrix.common.entity

import heckerpowered.matrix.common.event.*
import heckerpowered.matrix.common.item.WardenChestplateItem
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
            LivingAttackCallback.EVENT.register(::onLivingAttack)
            LivingDamageCallback.EVENT.register(::onLivingDamage)
        }

        fun onLivingDeath(entity: LivingEntity, damageSource: DamageSource): ActionResult {
            if (entity.protection.isProtected()) {
                return ActionResult.FAIL
            }

            return ActionResult.PASS
        }

        fun onLivingAttack(event: DamageAccumulator): ActionResult {
            if (event.target.protection == PROTECTED_COMPLETE) {
                return ActionResult.FAIL
            }

            return ActionResult.PASS
        }

        fun onLivingDamage(event: LivingDamageEvent): ActionResult {
            if (event.entity.protection == PROTECTED_COMPLETE) {
                return ActionResult.FAIL
            }

            return ActionResult.PASS
        }
    }
}