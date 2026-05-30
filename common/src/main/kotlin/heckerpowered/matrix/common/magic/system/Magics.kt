/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.system

import heckerpowered.matrix.common.magic.core.ExecutionPayload
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicExecutionPayloadSpecification
import heckerpowered.matrix.common.magic.core.asMagicUuid
import heckerpowered.matrix.common.magic.spell.*
import kotlinx.serialization.modules.plus
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.enchantment.Enchantment
import java.util.*

object Magics : Iterable<Magic> {
    private val registeredMagics = HashMap<UUID, Magic>()
    val all: Collection<Magic> = Collections.unmodifiableCollection(registeredMagics.values)

    fun registerMagic(magic: Magic) {
        val uuid = magic.definition.uuid
        require(registeredMagics.putIfAbsent(uuid, magic) == null) { "Duplicate Magic: $uuid" }

        if (magic is MagicExecutionPayloadSpecification) {
            ExecutionPayload.serializationModule += magic.serializerModule()
        }
    }

    private fun registerBuiltinMagics() {
        registerMagic(TargetPositioningMagic)
        registerMagic(DecisiveStrikeMagic)
        registerMagic(HealthStealMagic)
        registerMagic(ManaOverloadMagic)
        registerMagic(ExplosionMagic)
        registerMagic(KillMagic)
        registerMagic(SculkCatalystMagic)
        registerMagic(IgniteMagic)
        registerMagic(BreakingBadMagic)
        registerMagic(CrippleMovementMagic)
        registerMagic(MemoryWipeMagic)
        registerMagic(SpreadMagic)
        registerMagic(SystemCrashMagic)
        registerMagic(LightningBoltMagic)
        registerMagic(TeleportMagic)
        registerMagic(ArmorPenetrationMagic)
        registerMagic(SonicBoomMagic)
        registerMagic(BruteForceMagic)
        registerMagic(AttractMagic)
        registerMagic(LevitationMagic)
        registerMagic(AbsolvriftMagic)
        registerMagic(TuckInMagic)
    }

    fun onInitialize() {
        registerBuiltinMagics()
    }

    operator fun get(uuid: UUID): Magic? = registeredMagics[uuid]
    operator fun get(key: ResourceKey<Enchantment>): Magic? = registeredMagics[key.asMagicUuid()]
    operator fun get(identifier: Identifier): Magic? = registeredMagics[identifier.asMagicUuid()]

    override fun iterator(): Iterator<Magic> {
        return all.iterator()
    }
}
