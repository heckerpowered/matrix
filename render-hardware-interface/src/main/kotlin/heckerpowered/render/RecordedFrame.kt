package heckerpowered.render

/**
 * Represents a frame worth of recorded GPU work that is ready for submission.
 *
 * A recorded frame is produced by a command recorder after all commands for the
 * current frame have been described.
 *
 * The content of this object is backend-defined. It may contain recorded commands,
 * immediate execution metadata, or any internal representation needed for submission.
 */
interface RecordedFrame