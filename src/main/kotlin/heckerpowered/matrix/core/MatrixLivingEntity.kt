package heckerpowered.matrix.core

import heckerpowered.matrix.common.persistent.ChannelSequence
import java.util.UUID

interface MatrixLivingEntity {
    fun getChannelSequence(): MutableMap<UUID, ChannelSequence>
}