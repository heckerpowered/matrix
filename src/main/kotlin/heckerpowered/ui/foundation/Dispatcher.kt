/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.ui.foundation

import java.util.concurrent.Future
import java.util.concurrent.FutureTask

class Dispatcher {
    private val taskQueue = ArrayDeque<Future<*>>()

    fun <T> queue(action: () -> T): Future<T> {
        FutureTask(action).also { taskQueue.add(it) }.also { return it }
    }

    fun run() {
        while (taskQueue.isNotEmpty()) {
            val task = taskQueue.removeFirst()
            task.get()
        }
    }
}