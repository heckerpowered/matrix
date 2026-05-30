package heckerpowered.render

import heckerpowered.render.api.MemorySpan

/**
 * Records GPU work for a single frame.
 *
 * A command recorder is the public entry point for describing rendering and
 * compute work. It exposes a structured command interface rather than a
 * mutable global state machine.
 *
 * The recorder owns the lifetime of all pass scopes created from it. Pass
 * scopes are always closed by the recorder implementation, even if the block
 * exits with an exception.
 *
 * A command recorder is frame-local. It must not be used after [finish]
 * has been called.
 */
interface CommandRecorder {
    /**
     * Uploads byte data into a GPU buffer.
     *
     * The data is copied into [buffer] starting at [destinationOffsetBytes].
     * The backend decides how this upload is implemented.
     */
    fun uploadBuffer(buffer: GpuBuffer, data: MemorySpan, destinationOffsetBytes: Long = 0)

    /**
     * Finalizes recording and produces a frame submission payload.
     *
     * After this call, no additional commands may be recorded through this
     * recorder instance.
     */
    fun finish(): RecordedFrame
}