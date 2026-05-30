package heckerpowered.render.api

/**
 * Represents an active frame recording session.
 *
 * A frame recorder owns the lifecycle of a single in-progress frame recording.
 * It exposes a frame scope that may be passed through long rendering call chains,
 * while retaining responsibility for ending the recording session.
 */
interface FrameRecorder {
    /**
     * The frame-local command recording scope associated with this recorder.
     *
     * Ordinary rendering systems use this scope to append commands to the
     * current frame without managing frame lifetime directly.
     */
    val frameScope: FrameScope

    /**
     * Ends recording of the current frame.
     *
     * After this call, no additional commands may be recorded for this frame.
     *
     * @return A pending frame that may be submitted for backend execution.
     */
    fun end(): PendingFrame
}