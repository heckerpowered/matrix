/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.core

import net.minecraft.server.MinecraftServer

interface MatrixMinecraftServer {
    var matrixTickStartTimeNanos: Long
    var matrixTickEndTimeNanos: Long
}

var MinecraftServer.matrixTickStartTimeNanos: Long
    get() = (this as MatrixMinecraftServer).matrixTickStartTimeNanos
    set(value) {
        (this as MatrixMinecraftServer).matrixTickStartTimeNanos = value
    }

var MinecraftServer.matrixTickEndTimeNanos: Long
    get() = (this as MatrixMinecraftServer).matrixTickEndTimeNanos
    set(value) {
        (this as MatrixMinecraftServer).matrixTickEndTimeNanos = value
    }