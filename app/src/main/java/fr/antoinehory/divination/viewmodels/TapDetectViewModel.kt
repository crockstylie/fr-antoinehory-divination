package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// Consider creating a sealed interface or class for InteractionEvent if it's used elsewhere
// and TapEvent is one specific type. For now, assuming TapEvent is defined elsewhere or is a simple object.
// Example:
// sealed interface InteractionEvent
// object TapEvent : InteractionEvent

/**
 * ViewModel responsible for detecting and signaling tap interactions on the screen.
 *
 * It provides a [SharedFlow] ([interactionDetected]) that emits an [InteractionEvent]
 * (currently [TapEvent]) when a tap is registered via [onScreenTapped].
 *
 * @param application The application context.
 */
class TapDetectViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Internal mutable shared flow to emit interaction events.
     * Replays 0 items, has an extra buffer capacity of 1, and drops the oldest event on buffer overflow.
     */
    private val _interactionDetected = MutableSharedFlow<InteractionEvent>(
        replay = 0, // No replay needed for one-shot tap events
        extraBufferCapacity = 1, // Buffer to hold one event if collector is not ready
        onBufferOverflow = BufferOverflow.DROP_OLDEST // Drop if new event comes before old one is collected
    )

    /**
     * Publicly exposed [SharedFlow] that emits an [InteractionEvent] when a screen tap is detected.
     * Observers can collect this flow to react to tap events.
     */
    val interactionDetected: SharedFlow<InteractionEvent> = _interactionDetected.asSharedFlow()

    /**
     * Called when a tap event occurs on the screen.
     * This function attempts to emit a [TapEvent] to the [_interactionDetected] flow.
     * Assumes [TapEvent] is an object or class instance representing a tap, defined elsewhere.
     */
    fun onScreenTapped() {
        // Assuming TapEvent is an object or class instance representing a tap
        _interactionDetected.tryEmit(TapEvent)
    }

    /**
     * Intended to be called by the observer after an [InteractionEvent] has been fully processed.
     * This can be used to reset state or acknowledge event handling if needed.
     *
     * (Currently, this function body is empty and needs implementation based on specific requirements.)
     */
    fun completeInteractionProcessing() {
        // TODO: Implement logic if needed, e.g., resetting a flag or state.
    }

    /**
     * Registers any necessary listeners for tap detection or related functionalities.
     * This could be used for more complex gesture detection or sensor integration if required.
     *
     * (Currently, this function body is empty and needs implementation based on specific requirements.)
     */
    fun registerListener() {
        // TODO: Implement listener registration logic if required.
    }

    /**
     * Unregisters any listeners that were previously set up by [registerListener].
     * This is important to prevent memory leaks, especially when the ViewModel is cleared.
     *
     * (Currently, this function body is empty and needs implementation based on specific requirements.)
     */
    fun unregisterListener() {
        // TODO: Implement listener unregistration logic if required.
    }

    /**
     * Called when the ViewModel is no longer used and will be destroyed.
     * This is the place to clean up resources, such as unregistering listeners.
     * It's good practice to call [unregisterListener] here if listeners are registered.
     */
    override fun onCleared() {
        super.onCleared()
        // unregisterListener() // Example: Call unregisterListener if it's implemented and used.
    }
}

// Placeholder for InteractionEvent and TapEvent if not defined elsewhere
// You should define these according to your application's needs.
// interface InteractionEvent // Base interface for different types of interactions
// object TapEvent : InteractionEvent // Specific event for a tap
