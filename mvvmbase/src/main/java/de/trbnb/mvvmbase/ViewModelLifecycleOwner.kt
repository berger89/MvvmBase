package de.trbnb.mvvmbase

import android.annotation.SuppressLint
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * The custom lifecycle owner for ViewModels.
 *
 * Its lifecycle state is:
 * - After initialization: [Lifecycle.State.STARTED].
 * - After being destroyed: [Lifecycle.State.DESTROYED].
 */
internal class ViewModelLifecycleOwner(enforceMainThread: Boolean) : LifecycleOwner {
    @SuppressLint("VisibleForTests")
    private val registry = when (enforceMainThread) {
        true -> LifecycleRegistry(this)
        false -> LifecycleRegistry.createUnsafe(this)
    }

    init {
        onEvent(Event.INITIALIZED)
    }

    fun onEvent(event: Event) {
        registry.currentState = when (event) {
            Event.INITIALIZED -> Lifecycle.State.STARTED
            Event.DESTROYED -> Lifecycle.State.DESTROYED
        }
    }

    override fun getLifecycle() = registry

    /**
     * Enum for the specific Lifecycle of ViewModels.
     */
    internal enum class Event {
        INITIALIZED,
        DESTROYED
    }
}
