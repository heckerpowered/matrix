/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.channel

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import java.util.*

/**
 * Represents the identity of a magic caster at a given stage of the magic lifecycle.
 *
 * A [CasterContext] does not necessarily correspond to a concrete in-world entity.
 * It is a *rule-level identity*, used to determine how magic should be evaluated,
 * attributed, and resolved.
 *
 * Key characteristics:
 * - Always bound to a [ServerLevel].
 * - May or may not currently have an associated entity.
 * - May transition between concrete and detached forms over time.
 *
 * Typical lifecycle:
 * - Channel start: usually a [PlayerCaster] or [EntityCaster].
 * - During channeling: may become [DetachedCaster] (e.g. player death or logout).
 * - Cast moment: resolved once via [tryResolve] to reflect current world state.
 *
 * Implementations are intentionally minimal to keep rule logic decoupled from
 * entity lifetime and network state.
 *
 * @see PlayerCaster
 * @see EntityCaster
 * @see DetachedCaster
 */
sealed interface CasterContext {
    val level: Level

    companion object {
        fun fromEntity(entity: LivingEntity): CasterContext = when (entity) {
            is ServerPlayer -> PlayerCaster(entity)
            else -> EntityCaster(entity)
        }
    }
}

/**
 * Returns the underlying [LivingEntity] if this caster currently has one.
 *
 * This is a *best-effort view* of the caster as a physical entity.
 * Absence of an entity is a valid and expected state (e.g. detached casters).
 *
 * @return the associated [LivingEntity], or null if none exists.
 */
fun CasterContext.entityOrNull(): LivingEntity? = (this as? HasEntity)?.entity

/**
 * Attempts to cast the [CasterContext] to a [HasEntity] and then to a [ServerPlayer].
 *
 * This function is useful when you need to determine if the current context is associated with a player entity.
 * It first checks if the [CasterContext] implements the [HasEntity] interface, and if so, it tries to cast
 * the entity to a [ServerPlayer]. If the cast is successful, it returns the [ServerPlayer];
 * otherwise, it returns null.
 *
 * @return The [ServerPlayer] if the context is associated with a player, or null if not.
 */
fun CasterContext.asPlayerOrNull(): ServerPlayer? = (this as? HasEntity)?.entity as? ServerPlayer

/**
 * Attempts to refresh this caster's identity based on the current world state.
 *
 * This function is intended to be called at *cast time* to ensure that a
 * previously detached caster is rebound to a live entity if possible.
 *
 * Resolution rules:
 * - Non-detached casters are returned unchanged.
 * - A [DetachedCaster] with a resolvable UUID may become:
 *   - [PlayerCaster] if the entity is an online player.
 *   - [EntityCaster] if the entity is a non-player living entity.
 * - If resolution fails, the caster remains detached.
 *
 * The operation is:
 * - Side-effect free
 * - Idempotent
 * - Best-effort (failure is expected and valid)
 *
 * @return a refreshed [CasterContext] reflecting the current world state.
 */
fun CasterContext.tryResolve(): CasterContext {
    if (this !is DetachedCaster || ownerUuid == null) {
        return this
    }

    return when (val entity = level.getEntity(ownerUuid)) {
        is ServerPlayer -> PlayerCaster(entity)
        is LivingEntity -> EntityCaster(entity)
        else -> DetachedCaster(level, ownerUuid)
    }
}

/**
 * Executes [block] with the underlying [LivingEntity] if one is available.
 *
 * This is a convenience utility for effect-level logic that operates only
 * when a physical entity is present.
 *
 * @param block operation to execute with the entity.
 * @return the result of [block], or null if no entity is available.
 */
inline fun <T> CasterContext.withEntity(block: (LivingEntity) -> T): T? {
    val entity = entityOrNull() ?: return null
    return block(entity)
}

/**
 * A caster context that is no longer bound to a concrete entity.
 *
 * Detached casters commonly occur when:
 * - A player logs out during channeling.
 * - A caster entity dies before the magic is cast.
 * - A magic effect propagates beyond its original source.
 *
 * The optional [ownerUuid] is used for attribution and best-effort resolution
 * at cast time via [tryResolve].
 */
data class DetachedCaster(
    override val level: Level,
    val ownerUuid: UUID?,
) : CasterContext

/**
 * A caster context backed by an online player.
 *
 * This represents the strongest form of caster identity, granting access
 * to player-specific mechanics such as equipment, enchantments, mana systems,
 * and advancement attribution.
 */
data class PlayerCaster(val player: ServerPlayer) : CasterContext, HasEntity {
    override val entity get() = player
    override val level get() = player.level()
}

/**
 * A caster context backed by a non-player living entity.
 *
 * This is commonly used for AI-controlled casters, mobs, or entities that
 * can participate in magic without player-specific mechanics.
 */
data class EntityCaster(override val entity: LivingEntity) : CasterContext, HasEntity {
    override val level get() = entity.level()
}