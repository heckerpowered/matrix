package heckerpowered.ui.foundation

import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import kotlin.collections.ArrayDeque

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