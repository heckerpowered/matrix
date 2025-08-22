/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.network.SystemCrashPayload
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

object SystemCrashMagic : Magic(MatrixLanguage.magicSystemCrash, 100, MatrixLanguage.magicSystemCrashDescription, 200) {

    override fun channel(player: PlayerEntity, target: LivingEntity, sequence: ChannelSequence, data: MagicData) {
        if (target is ServerPlayerEntity) {
            ServerPlayNetworking.send(target, SystemCrashPayload())
        }
    }
}