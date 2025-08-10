package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.data.model.InteractionMode
import fr.antoinehory.divination.data.model.InteractionPreferences
import fr.antoinehory.divination.data.repository.UserPreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for orchestrating interaction detection based on user preferences.
 *
 * It manages different interaction modes (e.g., Shake, Tap) by observing user preferences
 * from [UserPreferencesRepository] and delegating detection to specific ViewModels
 * like [ShakeDetectViewModel] and [TapDetectViewModel]. It then emits a unified [InteractionEvent]
 * when an interaction is triggered, respecting a global cooldown period.
 *
 * @param application The application context.
 */
class InteractionDetectViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Repository to access and manage user interaction preferences.
     */
    private val userPreferencesRepository = UserPreferencesRepository(application.applicationContext)

    /**
     * ViewModel dedicated to detecting shake gestures.
     */
    private val shakeDetector = ShakeDetectViewModel(application)
    /**
     * ViewModel dedicated to detecting tap gestures.
     */
    private val tapDetector = TapDetectViewModel(application)

    /**
     * Exposes the current user interaction preferences as a [StateFlow].
     * Initializes with default preferences and updates eagerly.
     */
    val interactionPreferences: StateFlow<InteractionPreferences> =
        userPreferencesRepository.interactionPreferencesFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = InteractionPreferences()
            )

    /**
     * Internal [MutableSharedFlow] to emit triggered interaction events.
     * It has a small buffer and drops the oldest event on overflow to ensure responsiveness.
     */
    private val _interactionTriggered = MutableSharedFlow<InteractionEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    /**
     * Public [SharedFlow] that emits an [InteractionEvent] when an interaction is detected
     * according to the active mode and global cooldown.
     */
    val interactionTriggered: SharedFlow<InteractionEvent> = _interactionTriggered.asSharedFlow()

    /**
     * Coroutine job for collecting shake detection events. Null if shake detection is not active.
     */
    private var shakeJob: Job? = null
    /**
     * Coroutine job for collecting tap detection events. Null if tap detection is not active.
     */
    private var tapJob: Job? = null

    /**
     * Internal [MutableStateFlow] indicating whether the shake detection (accelerometer) is available.
     */
    private val _isShakeAvailable = MutableStateFlow(false)
    /**
     * Public [StateFlow] exposing the availability of shake detection.
     * `true` if an accelerometer is present and functional, `false` otherwise.
     */
    val isShakeAvailable: StateFlow<Boolean> = _isShakeAvailable.asStateFlow()

    /**
     * Flag to prevent processing multiple interactions simultaneously within a short timeframe.
     * Ensures that only one interaction is handled globally across detectors during the cooldown.
     */
    private var isProcessingGlobalInteraction = false
    /**
     * Cooldown period in milliseconds after an interaction is triggered, during which
     * subsequent detected interactions will be ignored.
     */
    private val globalInteractionCooldownMs = 500L

    /**
     * Initializes the ViewModel.
     * - Observes accelerometer availability and updates [isShakeAvailable]. If shake is the active mode
     *   but the accelerometer becomes unavailable, it defaults to tap mode.
     * - Observes changes in [interactionPreferences] to update active listeners.
     */
    init {
        viewModelScope.launch {
            shakeDetector.isAccelerometerAvailable.collect { available ->
                _isShakeAvailable.value = available
                // If shake mode is active but accelerometer becomes unavailable, switch to tap.
                if (!available && interactionPreferences.value.activeInteractionMode == InteractionMode.SHAKE) {
                    setActiveInteractionMode(InteractionMode.TAP)
                }
            }
        }

        viewModelScope.launch {
            interactionPreferences.collect { prefs ->
                updateListenersBasedOnPreferences(prefs)
            }
        }
    }

    /**
     * Updates the active interaction listeners (shake or tap) based on the provided [InteractionPreferences].
     *
     * If the preferred mode is SHAKE and the accelerometer is available, it registers the shake listener
     * and starts collecting shake events. It unregisters the tap listener.
     * If the preferred mode is TAP, it registers the tap listener and starts collecting tap events.
     * It unregisters the shake listener.
     * Listeners are only registered if not already active, and existing jobs are cancelled appropriately.
     *
     * @param prefs The current [InteractionPreferences] to determine which listeners to activate.
     */
    private fun updateListenersBasedOnPreferences(prefs: InteractionPreferences) {
        // Handle SHAKE mode
        if (prefs.activeInteractionMode == InteractionMode.SHAKE && _isShakeAvailable.value) {
            if (shakeJob == null || shakeJob?.isActive == false) {
                tapJob?.cancel() // Ensure tap is stopped before starting shake
                tapJob = null
                tapDetector.unregisterListener()

                shakeDetector.registerListener()
                shakeJob = viewModelScope.launch {
                    shakeDetector.interactionDetected.collect { event ->
                        handleDetectedInteraction(event, shakeDetector::completeInteractionProcessing)
                    }
                }
            }
        } else {
            shakeJob?.cancel()
            shakeJob = null
            shakeDetector.unregisterListener()
        }

        // Handle TAP mode
        // This condition allows TAP to be a fallback if SHAKE is selected but unavailable.
        if (prefs.activeInteractionMode == InteractionMode.TAP ||
            (prefs.activeInteractionMode == InteractionMode.SHAKE && !_isShakeAvailable.value) ) {
            if (tapJob == null || tapJob?.isActive == false) {
                shakeJob?.cancel() // Ensure shake is stopped before starting tap
                shakeJob = null
                shakeDetector.unregisterListener()

                tapDetector.registerListener()
                tapJob = viewModelScope.launch {
                    tapDetector.interactionDetected.collect { event ->
                        handleDetectedInteraction(event, tapDetector::completeInteractionProcessing)
                    }
                }
            }
        } else {
            // This case might be redundant if SHAKE is active and available, as tapJob would be null.
            // However, it ensures tap is stopped if preferences change away from TAP for other reasons.
            if (prefs.activeInteractionMode != InteractionMode.TAP) {
                tapJob?.cancel()
                tapJob = null
                tapDetector.unregisterListener()
            }
        }
    }

    /**
     * Handles a detected interaction event from one of the specific detectors.
     *
     * If no global interaction is currently being processed, this method:
     * 1. Sets [isProcessingGlobalInteraction] to `true`.
     * 2. Emits the [event] to the [_interactionTriggered] flow.
     * 3. Calls the [onProcessedCallback] provided by the detector (e.g., to reset its internal state).
     * 4. Enforces a [globalInteractionCooldownMs] delay.
     * 5. Resets [isProcessingGlobalInteraction] to `false`.
     *
     * If a global interaction is already being processed, it simply calls [onProcessedCallback].
     *
     * @param event The [InteractionEvent] detected.
     * @param onProcessedCallback A callback function from the detector (e.g., [ShakeDetectViewModel.completeInteractionProcessing])
     *                            to signal that the detector has finished its part of the processing for this event.
     */
    private suspend fun handleDetectedInteraction(event: InteractionEvent, onProcessedCallback: () -> Unit) {
        if (!isProcessingGlobalInteraction) {
            isProcessingGlobalInteraction = true
            _interactionTriggered.tryEmit(event)

            onProcessedCallback() // Allow detector to complete its specific processing

            delay(globalInteractionCooldownMs)
            isProcessingGlobalInteraction = false
        } else {
            // If global interaction is already processing, still let the specific detector complete its part.
            // This prevents the detector from getting stuck waiting if its event was ignored due to global cooldown.
            onProcessedCallback()
        }
    }

    /**
     * Sets the active interaction mode in user preferences.
     *
     * If the selected [mode] is SHAKE but the accelerometer is not available,
     * the preference change is not made.
     *
     * @param mode The [InteractionMode] to set as active.
     */
    fun setActiveInteractionMode(mode: InteractionMode) {
        viewModelScope.launch {
            // Prevent setting SHAKE mode if accelerometer is not available.
            if (mode == InteractionMode.SHAKE && !_isShakeAvailable.value) {
                // Optionally, could provide feedback to user or default to TAP here as well.
                return@launch
            }
            userPreferencesRepository.updateActiveInteractionMode(mode)
        }
    }

    /**
     * Notifies the [TapDetectViewModel] that a tap event has occurred on the screen.
     * This is typically called from the UI when a tap gesture is detected in TAP mode.
     */
    fun userTappedScreen() {
        // This method assumes that the tap detector is active if this is called.
        // The TapDetectViewModel itself will emit the event if its listener is registered.
        tapDetector.onScreenTapped()
    }

    /**
     * Called when the ViewModel is no longer used and will be destroyed.
     * Unregisters listeners from both shake and tap detectors and cancels any active jobs
     * to prevent memory leaks and stop background processing.
     */
    override fun onCleared() {
        super.onCleared()
        shakeDetector.unregisterListener()
        tapDetector.unregisterListener()
        shakeJob?.cancel()
        tapJob?.cancel()
    }
}

