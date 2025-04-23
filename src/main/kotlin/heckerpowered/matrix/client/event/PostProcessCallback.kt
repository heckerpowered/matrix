package heckerpowered.matrix.client.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory

fun interface PostProcessCallback {
    companion object {
        @JvmField
        val event: Event<PostProcessCallback> =
            EventFactory.createArrayBacked(PostProcessCallback::class.java) { listeners ->
                PostProcessCallback {
                    for (listener in listeners) {
                        listener.onPostProcess()
                    }
                }
            }
    }

    fun onPostProcess()
}