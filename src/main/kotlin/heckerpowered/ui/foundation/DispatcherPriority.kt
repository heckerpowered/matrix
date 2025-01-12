package heckerpowered.ui.foundation

/**
 * Describes the priorities at which operations can be invoked by way of the Dispatcher.
 */
enum class DispatcherPriority {
    /**
     * Operations are processed when the system is idle.
     */
    SYSTEM_IDLE,

    /**
     * Operations are processed when the application is idle.
     */
    APPLICATION_IDLE,

    /**
     * Operations are processed when the current context is idle.
     */
    CONTEXT_IDLE,

    /**
     * Operations are processed when the current view is idle.
     */
    BACKGROUND,

    /**
     * Operations are processed at the same priority as input.
     */
    INPUT,

    /**
     * Operations are processed when layout and render has finished but just before items at input priority are serviced.
     * Specifically this is used when raising the Loaded event.
     */
    LOADED,

    /**
     * Operations processed at the same priority as rendering.
     */
    RENDER,

    /**
     * Operations are processed at the same priority as data binding.
     */
    DATA_BIND,

    /**
     * Operations are processed at normal priority. This is the typical application priority.
     */
    NORMAL,

    /**
     * Operations are processed before other asynchronous operations. This is the highest priority.
     */
    SEND
}