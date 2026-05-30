package heckerpowered.render.api

import java.nio.ByteBuffer

/**
 * Represents an immutable, non-owning view over a contiguous memory range.
 *
 * A memory span does not own the underlying storage. It behaves like a
 * read-only contiguous sequence of bytes whose visible range is defined by
 * the underlying memory view.
 */
class MemorySpan internal constructor(
    internal val readableView: ByteBuffer
) {
    /**
     * Returns the number of visible bytes in this span.
     */
     val sizeBytes: Int
         get() = readableView.remaining()

    /**
     * Returns whether this span contains no bytes.
     */
     val isEmpty: Boolean
         get() = sizeBytes == 0

    /**
     * Returns the byte at the specified position.
     *
     * @param indexBytes The zero-based byte index within this span.
     * @return The byte stored at the specified position.
     *
     * @throws IllegalArgumentException If indexBytes is negative or not less than sizeBytes.
     */
    operator fun get(indexBytes: Int): Byte {
        require(indexBytes >= 0)
        require(indexBytes < sizeBytes)
        return readableView.get(indexBytes)
    }


    /**
     * Obtains a subview over a contiguous byte range of this span.
     *
     * The returned span contains sizeBytes bytes starting at offsetBytes.
     *
     * @param offsetBytes The zero-based byte offset of the first byte in the subspan.
     * @param sizeBytes The number of bytes in the subspan.
     * @return A span representing the requested byte range.
     *
     * @throws IllegalArgumentException If offsetBytes is negative, if sizeBytes is negative,
     * or if offsetBytes + sizeBytes is greater than this span's sizeBytes.
     */
    fun subspan(offsetBytes: Int, sizeBytes: Int): MemorySpan {
        require(offsetBytes >= 0)
        require(sizeBytes >= 0)
        require(offsetBytes + sizeBytes <= this.sizeBytes)

        val subspanView = readableView.duplicate()
        subspanView.position(offsetBytes)
        subspanView.limit(offsetBytes + sizeBytes)

        return MemorySpan(subspanView.slice().asReadOnlyBuffer())
    }

    /**
     * Obtains a subview consisting of the first sizeBytes bytes of this span.
     *
     * @param sizeBytes The number of bytes to include from the beginning of this span.
     * @return A span representing the first sizeBytes bytes.
     *
     * @throws IllegalArgumentException If sizeBytes is negative or greater than this span's sizeBytes.
     */
    fun take(sizeBytes: Int): MemorySpan {
        require(sizeBytes >= 0)
        require(sizeBytes <= this.sizeBytes)
        return subspan(offsetBytes = 0, sizeBytes = sizeBytes)
    }

    /**
     * Obtains a subview consisting of the last sizeBytes bytes of this span.
     *
     * @param sizeBytes The number of bytes to include from the end of this span.
     * @return A span representing the last sizeBytes bytes.
     *
     * @throws IllegalArgumentException If sizeBytes is negative or greater than this span's sizeBytes.
     */
    fun takeLast(sizeBytes: Int): MemorySpan {
        require(sizeBytes >= 0)
        require(sizeBytes <= this.sizeBytes)
        return subspan(offsetBytes = this.sizeBytes - sizeBytes, sizeBytes = sizeBytes)
    }

    /**
     * Returns a ByteBuffer representation of this span.
     *
     * The returned buffer represents the same visible memory range as this span.
     * The returned object is a different view over the same underlying storage.
     *
     * Callers must not rely on object identity or on mutating the returned
     * buffer to change this span itself.
     *
     * @return A ByteBuffer view over the visible memory range of this span.
     */
    fun asByteBuffer(): ByteBuffer {
        return readableView.duplicate()
    }

    companion object {
        /**
         * Creates a memory span from the remaining visible range of buffer.
         *
         * The created span covers the bytes in the interval [buffer.position(), buffer.limit()).
         *
         * @param buffer The source buffer whose remaining visible range will be used.
         * @return A read-only span over the remaining visible range of buffer.
         */
        fun fromRemaining(buffer: ByteBuffer): MemorySpan {
            return MemorySpan(buffer.slice().asReadOnlyBuffer())
        }
    }
 }