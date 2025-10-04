/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.render

import heckerpowered.matrix.client.ui.foundation.animation.AnimationClock
import heckerpowered.matrix.client.ui.foundation.animation.DoubleAnimation
import heckerpowered.matrix.client.ui.foundation.animation.EasingMode
import heckerpowered.matrix.client.ui.foundation.animation.ElasticEase
import heckerpowered.matrix.common.magic.Magic
import net.minecraft.entity.LivingEntity
import java.time.Duration

class ChannelAnimation(
    val magic: Magic,
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
    var currentChannelTime = 0L
    var initialProgressOffset = 0F

    init {
        shownAnimationClock.start()
        opacityAnimationClock.start()
    }

    fun tick(entity: LivingEntity) {
        if (currentChannelTime++ == channelTime) {
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