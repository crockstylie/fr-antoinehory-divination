package fr.antoinehory.divination.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * ViewModel responsible for detecting shake gestures using the device's accelerometer.
 *
 * It monitors sensor data to identify shake patterns and emits an [InteractionEvent]
 * (specifically [ShakeEvent]) when a shake is detected. It also provides a [StateFlow]
 * to indicate if the accelerometer is available on the device.
 *
 * @param application The application context, used to access system services like [SensorManager].
 */
class ShakeDetectViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    /**
     * Indicates whether an accelerometer sensor is available on the device.
     * Emits `true` if available, `false` otherwise.
     */
    private val _isAccelerometerAvailable = MutableStateFlow(accelerometer != null)
    val isAccelerometerAvailable: StateFlow<Boolean> = _isAccelerometerAvailable.asStateFlow()

    /**
     * Internal mutable shared flow to emit interaction events (shakes).
     * Replays 0 items, has an extra buffer capacity of 1, and drops the oldest event on buffer overflow.
     */
    private val _interactionDetected = MutableSharedFlow<InteractionEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    /**
     * Publicly exposed [SharedFlow] that emits a [ShakeEvent] when a shake gesture is detected.
     */
    val interactionDetected: SharedFlow<InteractionEvent> = _interactionDetected.asSharedFlow()

    private var lastShakeTime: Long = 0
    private var shakeCount = 0
    private var firstShakeTime: Long = 0
    private var isProcessingShake = false // Flag interne pour ce détecteur

    companion object {
        /** Minimum force of acceleration (normalized G-force) to register as a potential part of a shake. */
        private const val SHAKE_THRESHOLD_GRAVITY = 2.7f
        /** Minimum time interval in milliseconds between individual movements to be considered part of the same shake sequence. */
        private const val SHAKE_SLOP_TIME_MS = 500L
        /** Time in milliseconds after which the shake count resets if no new qualifying movement is detected. */
        private const val SHAKE_COUNT_RESET_TIME_MS = 3000L
        /** Minimum number of rapid movements required to trigger a shake event. */
        private const val SHAKE_MIN_COUNT = 1 // Nombre de mouvements rapides pour déclencher une secousse
        /** Timeout in milliseconds to reset [isProcessingShake] if event emission fails or is not acknowledged. */
        private const val PROCESSING_TIMEOUT_MS = 1500L
    }

    /**
     * Registers the [SensorEventListener] to start listening to accelerometer sensor events.
     * Does nothing if the accelerometer is not available.
     */
    fun registerListener() {
        if (_isAccelerometerAvailable.value) {
            accelerometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    /**
     * Unregisters the [SensorEventListener] to stop listening to accelerometer sensor events.
     * Does nothing if the accelerometer is not available or was not registered.
     */
    fun unregisterListener() {
        if (_isAccelerometerAvailable.value) {
            sensorManager.unregisterListener(this, accelerometer)
        }
    }

    /**
     * Called when sensor values have changed.
     * This method contains the core logic for shake detection based on accelerometer data.
     * It calculates the G-force and checks if it exceeds [SHAKE_THRESHOLD_GRAVITY].
     * It then counts qualifying movements within a time window ([SHAKE_SLOP_TIME_MS], [SHAKE_COUNT_RESET_TIME_MS])
     * to determine if a shake gesture ([SHAKE_MIN_COUNT]) has occurred.
     *
     * @param event The [SensorEvent] containing the new sensor data.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        if (isProcessingShake) return // Avoid processing new sensor data while a shake is already being handled

        val currentTime = System.currentTimeMillis()
        // Ignore movements too close to the last one if a shake sequence has started
        if ((currentTime - lastShakeTime) < SHAKE_SLOP_TIME_MS && firstShakeTime != 0L) return


        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        // Calculate the magnitude of the acceleration vector, normalized to G-force
        val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            // If this is the first movement in a potential sequence, or if the reset time has passed
            if (firstShakeTime == 0L || (currentTime - firstShakeTime) > SHAKE_COUNT_RESET_TIME_MS) {
                firstShakeTime = currentTime
                shakeCount = 0
            }
            shakeCount++
            lastShakeTime = currentTime // Update lastShakeTime for each movement exceeding the threshold

            if (shakeCount >= SHAKE_MIN_COUNT) {
                isProcessingShake = true // Set flag to indicate a shake is being processed
                // Reset counters for the next shake detection sequence
                shakeCount = 0
                firstShakeTime = 0L // Important for the next movement to restart the sequence count

                val emitted = _interactionDetected.tryEmit(ShakeEvent) // Emit the global ShakeEvent object
                if (!emitted) {
                    // Log.d("ShakeDetectVM", "ShakeEvent emission failed")
                    // If emission fails (e.g., buffer full, collector busy), set a timeout to reset isProcessingShake
                    viewModelScope.launch {
                        delay(PROCESSING_TIMEOUT_MS)
                        if (isProcessingShake) { // Check again in case it was completed by other means
                            isProcessingShake = false
                            // Log.d("ShakeDetectVM", "isProcessingShake reset due to emission failure timeout")
                        }
                    }
                }
            }
        }
    }

    /**
     * Allows an external component (e.g., an orchestrator ViewModel) to signal that the
     * previously detected shake interaction has been fully processed.
     * This resets the [isProcessingShake] flag, allowing new shakes to be detected.
     */
    fun completeInteractionProcessing() {
        if (isProcessingShake) {
            isProcessingShake = false
            // Log.d("ShakeDetectVM", "Shake interaction processing completed by orchestrator.")
        }
    }

    /**
     * Called when the accuracy of the registered sensor has changed.
     * This method is not used in this ViewModel.
     *
     * @param sensor The [Sensor] whose accuracy changed.
     * @param accuracy The new accuracy of this sensor.
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Non utilisé
    }

    /**
     * Called when the ViewModel is no longer used and will be destroyed.
     * Ensures that the sensor listener is unregistered to prevent memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        unregisterListener()
    }
}
