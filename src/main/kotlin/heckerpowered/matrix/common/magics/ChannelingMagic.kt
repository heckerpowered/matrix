package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic

data class ChannelingMagic(
    val magic: Magic,
    var currentChannelTime: Long,
    val channelTime: Long,
    val cost: Long,
    val data: MagicData,
)