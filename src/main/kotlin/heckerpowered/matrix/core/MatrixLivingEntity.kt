package heckerpowered.matrix.core

import heckerpowered.matrix.common.persistent.ChannelSequence
import java.util.*

interface MatrixLivingEntity {
    var killed: Boolean
    fun getChannelSequence(): MutableMap<UUID, ChannelSequence>
}