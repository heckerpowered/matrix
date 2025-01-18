package heckerpowered.matrix.core

import heckerpowered.matrix.common.persistent.ChannelSequence
import java.util.*

interface MatrixLivingEntity {
    fun getChannelSequence(): MutableMap<UUID, ChannelSequence>
}