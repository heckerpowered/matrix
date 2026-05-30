/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent

fun interface QueueLoadedRule {
    fun onLoaded(context: QueueLoadedContext)
}