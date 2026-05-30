package heckerpowered.render.api

/**
 * Represents a frame whose recording has ended and is waiting for submission.
 *
 * A pending frame can no longer be modified. Its next lifecycle step is
 * submission to the backend.
 */
interface PendingFrame {

    /**
     * Submits this frame for backend execution.
     */
    fun submit()
}