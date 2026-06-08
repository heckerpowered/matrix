/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import heckerpowered.matrix.client.ui.foundation.animation.AnimationClock
import heckerpowered.matrix.client.ui.foundation.animation.DoubleAnimation
import heckerpowered.matrix.client.ui.foundation.animation.EasingMode
import heckerpowered.matrix.client.ui.foundation.animation.ElasticEase
import heckerpowered.matrix.common.magic.channel.ChannelEntry
import heckerpowered.matrix.common.magic.core.Magic
import net.minecraft.world.entity.LivingEntity
import java.time.Duration
import kotlin.math.max

class ChannelAnimation(
    val magic: Magic,
    val sourceEntry: ChannelEntry? = null,
) {
    companion object {
        private val easingFunction = ElasticEase().also {
            it.easingMode = EasingMode.OUT
            it.oscillations = 0
        }
    }

    private val shownAnimationClock = AnimationClock(Duration.ofMillis(300), -50.0, .0)
    val opacityAnimationClock = AnimationClock(Duration.ofMillis(300), 0.0, 1.0)
    var shownAnimation = DoubleAnimation(shownAnimationClock, easingFunction)
    var opacityAnimation = DoubleAnimation(opacityAnimationClock, easingFunction)

    var channelTime = 0L
    var currentChannelTime = .0
    var initialProgressOffset = 0F
    private var completionAnimationStarted = false

    init {
        shownAnimationClock.start()
        opacityAnimationClock.start()
    }

    fun tick(entity: LivingEntity, tickAmount: Double = 1.0) {
        sourceEntry?.let {
            currentChannelTime = max(currentChannelTime, it.currentChannelTime.toDouble())
        }

        if (completionAnimationStarted) {
            currentChannelTime = max(currentChannelTime, channelTime + 1.0)
            return
        }

        currentChannelTime += tickAmount.coerceAtLeast(.0)
        if (currentChannelTime >= channelTime) {
            currentChannelTime = channelTime + 1.0
            completionAnimationStarted = true
            opacityAnimationClock.from = opacityAnimation.animatedValue
            opacityAnimationClock.to = 0.0
            opacityAnimationClock.start()

            ChannelSequenceRenderer.offsetAnimationMap[entity]?.let {
                it.xOffsetAnimationClock.from = .0
                it.xOffsetAnimationClock.to = -24.0
                it.xOffsetAnimationClock.start()
            }
        }
    }

    fun syncProgress(entity: LivingEntity, currentTime: Double) {
        if (completionAnimationStarted) {
            currentChannelTime = max(currentChannelTime, channelTime + 1.0)
            return
        }

        currentChannelTime = currentTime.coerceAtLeast(.0)
        if (currentChannelTime >= channelTime) {
            currentChannelTime = channelTime + 1.0
            completionAnimationStarted = true
            opacityAnimationClock.from = opacityAnimation.animatedValue
            opacityAnimationClock.to = 0.0
            opacityAnimationClock.start()

            ChannelSequenceRenderer.offsetAnimationMap[entity]?.let {
                it.xOffsetAnimationClock.from = .0
                it.xOffsetAnimationClock.to = -24.0
                it.xOffsetAnimationClock.start()
            }
        }
    }
}
