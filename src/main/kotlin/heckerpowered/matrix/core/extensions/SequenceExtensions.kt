package heckerpowered.matrix.core.extensions

object SequenceExtensions {
    /**
     * Returns a sequence of up to [targetCount] elements from this sequence
     * for which [consumer] returns true.
     *
     * Iteration stops once [targetCount] elements satisfying [consumer] have been yielded.
     */
    inline fun <T> Sequence<T>.consumeWhile(
        targetCount: Int,
        crossinline consumer: (T) -> Boolean,
    ) = sequence {
        var count = 0
        for (element in this@consumeWhile) {
            if (consumer(element)) {
                count++
                yield(element)
            }
            if (count >= targetCount) {
                return@sequence
            }
        }
    }

    fun <T> Sequence<T>.drain() {
        forEach { _ -> /* Trigger side effects. */ }
    }
}