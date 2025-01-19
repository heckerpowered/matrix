package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import kotlin.math.floor

class SonicBoomMagic : Magic(
    MatrixLanguage.magicSonicBoom,
    40,
    MatrixLanguage.magicSonicBoomDescription,
    34
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
        if (player == null) {
            return
        }
        val startPosition = player.eyePos
        val endPosition = target.eyePos
        val direction = endPosition.subtract(startPosition)
        val normalizedDirection = direction.normalize()

        val step = floor(direction.length()).toInt() + 7
        for (i in 1..step) {
            val currentPosition = startPosition.add(normalizedDirection.multiply(i.toDouble()))
            player.serverWorld.spawnParticles(
                ParticleTypes.SONIC_BOOM,
                currentPosition.x,
                currentPosition.y,
                currentPosition.z,
                1,
                0.0,
                0.0,
                0.0,
                0.0
            )
        }

        player.playSound(SoundEvents.ENTITY_WARDEN_SONIC_BOOM, 3.0F, 1.0F)
        if (target.damage(player.serverWorld.damageSources.sonicBoom(player), 10.0f)) {
            val horizontalKnockback =
                0.5 * (1.0 - target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE))
            val verticalKnockback =
                2.5 * (1.0 - target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE))
            target.addVelocity(
                normalizedDirection.x * verticalKnockback,
                normalizedDirection.y * horizontalKnockback,
                normalizedDirection.z * verticalKnockback
            )
        }
    }
}