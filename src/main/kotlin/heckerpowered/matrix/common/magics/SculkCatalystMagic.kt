package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.effect.bloodPactActive
import heckerpowered.matrix.common.event.ReadDataCallback
import heckerpowered.matrix.common.event.WriteDataCallback
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.common.persistent.getChannelSequence
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import heckerpowered.matrix.core.MatrixLivingEntity
import heckerpowered.matrix.core.getNearestEntities
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.boss.WitherEntity
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtList
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents

object SculkCatalystMagic : Magic(MatrixLanguage.sculkCatalystMagic, 12, MatrixLanguage.sculkCatalystMagicDescription, 9 * 20) {
    private class SculkCatalystMagicData(
        var bounces: Long = 0,
        tag: NbtCompound = NbtCompound(),
    ) : MagicData(tag = tag) {
        companion object {
            const val BOUNCES_TAG = "Bounces"
        }

        override fun readFromTag() {
            super.readFromTag()
            bounces = tag.getLong(BOUNCES_TAG)
        }

        override fun writeToTag() {
            super.writeToTag()
            tag.putLong(BOUNCES_TAG, bounces)
        }
    }

    init {
        WriteDataCallback.EVENT.register(::onWriteData)
        ReadDataCallback.EVENT.register(::onReadData)
    }

    private fun onWriteData(entity: LivingEntity, nbt: NbtCompound) {
        if (entity !is PlayerEntity) return

        val trackedEntities = sculkCatalystTracker[entity] ?: return
        val idList = NbtList()
        for (tracked in trackedEntities) {
            idList.add(NbtInt.of(tracked.id))
        }

        val matrixNbt = NbtCompound()
        matrixNbt.put("TrackedEntityIds", idList)
        nbt.put("MatrixMod", matrixNbt)
    }

    private fun onReadData(entity: LivingEntity, nbt: NbtCompound) {
        if (entity !is PlayerEntity) {
            return
        }
        val world = entity.world
        if (!nbt.contains("MatrixMod", NbtElement.COMPOUND_TYPE.toInt())) {
            return
        }
        val matrixNbt = nbt.getCompound("MatrixMod")

        if (!matrixNbt.contains("TrackedEntityIds", NbtElement.LIST_TYPE.toInt())) {
            return
        }
        val idList = matrixNbt.getList("TrackedEntityIds", NbtElement.INT_TYPE.toInt())

        val resolvedEntities = mutableListOf<LivingEntity>()
        for (i in 0 until idList.size) {
            val id = (idList[i] as? NbtInt)?.intValue() ?: continue
            val tracked = world.getEntityById(id) as? LivingEntity
            if (tracked != null) {
                resolvedEntities.add(tracked)
            }
        }

        sculkCatalystTracker[entity] = resolvedEntities
    }

    private val sculkCatalystTracker = mutableMapOf<PlayerEntity, MutableList<LivingEntity>>()

    override fun channel(player: PlayerEntity, target: LivingEntity, sequence: ChannelSequence, data: MagicData) {
        super.channel(player, target, sequence, data)
        sculkCatalystTracker.computeIfAbsent(player) {
            mutableListOf()
        }.add(target)
    }

    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence, data: MagicData) {
        super.cast(player, target, sequence, data)
        val sculkCatalystData = data as? SculkCatalystMagicData ?: SculkCatalystMagicData(tag = data.tag)
        val bounces = ++sculkCatalystData.bounces

        val damageSource = MemoryEraseMagic.getDamageSource(player, target, sequence) { player?.damageSources?.create(MatrixDamageTypes.magic, player) }
        if (target !is WitherEntity && target !is EnderDragonEntity && target !is PlayerEntity) {
            target.damage(damageSource, Float.POSITIVE_INFINITY)
            target.health = .0F
            (target as MatrixLivingEntity).killed = true
            target.onDeath(damageSource)
        } else {
            target.damage(damageSource, target.maxHealth * 4.0F)
        }
        if (!target.isAlive) {
            (target.world as? ServerWorld)?.apply {
                repeat(10) {
                    spawnParticles(ParticleTypes.SCULK_SOUL, target.x, target.y, target.z, 20, 0.1, .1, 0.1, 0.25)
                }

                playSound(null, target.x, target.y, target.z, SoundEvents.BLOCK_SCULK_SENSOR_CLICKING, SoundCategory.PLAYERS, 1.0F, 1.0F, random.nextLong())
                playSound(null, target.x, target.y, target.z, SoundEvents.BLOCK_SCULK_SHRIEKER_SHRIEK, SoundCategory.PLAYERS, 1.0F, 1.0F, random.nextLong())
            }
        }

        if (bounces > 5 || data.isSpread || player == null || target.isAlive) {
            return
        }

        val nearestEntity = target.getNearestEntities(20.0) {
            it is LivingEntity
                    && (it.getChannelSequence(player)?.channelingMagicCount() ?: 0) == 0
                    && it != player
                    && it.isAlive
        } as? LivingEntity ?: return
        ChannelSequence.channelMagic(SculkCatalystMagic, player, nearestEntity, true, data = sculkCatalystData)
    }

    override fun getBaseCost(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?, data: MagicData): Long {
        val bounces = (data as? SculkCatalystMagicData)?.bounces ?: 0
        return getNormalCost() + bounces.coerceAtMost(5) * 6
    }

    override fun getCost(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?, data: MagicData): Long {
        if (player.bloodPactActive) {
            return (super.getCost(player, target, sequence, data) * 0.5).toLong()
        }
        return super.getCost(player, target, sequence, data)
    }

    override fun getBaseChannelTime(player: PlayerEntity, target: LivingEntity, sequence: ChannelSequence?, data: MagicData): Long {
        val bounces = (data as? SculkCatalystMagicData)?.bounces ?: 0
        return when (bounces) {
            0L -> 9 * 20
            1L -> 110 // 5.5 * 20 = 5.5s
            2L -> 3 * 20
            3L -> 30 // 30 = 1.5 * 20 = 2.5s
            4L -> 20
            5L -> 10 // 0.5s
            else -> 10 // 0.5s
        }
    }

    override fun availableStatus(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?): MagicAvailableStatus {
        sculkCatalystTracker[player]?.removeIf {
            val sequence = player.getChannelSequence(it) ?: return@removeIf true
            return@removeIf !it.isAlive || sequence.channelingMagicCount() == 0
        }

        val sculkCatalystIsAlreadyActive = sculkCatalystTracker[player]?.any {
            it.isAlive && (it.getChannelSequence(player)?.channelingMagics()?.firstOrNull()?.magic == SculkCatalystMagic)
        }
        if (sculkCatalystIsAlreadyActive == true) {
            return MagicAvailableStatus.SCULK_CATALYST_IS_ALREADY_ACTIVE
        }
        return super.availableStatus(player, target, sequence)
    }
}