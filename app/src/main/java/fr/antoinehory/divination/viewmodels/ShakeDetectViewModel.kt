// Fichier: ShakeDetectViewModel.kt
package fr.antoinehory.divination.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.ui.input.key.type
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

// --- IMPORTS NÉCESSAIRES ---
import fr.antoinehory.divination.viewmodels.InteractionEvent // Importer l'interface globale
import fr.antoinehory.divination.viewmodels.ShakeEvent     // Importer l'objet ShakeEvent global

class ShakeDetectViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _isAccelerometerAvailable = MutableStateFlow(accelerometer != null)
    val isAccelerometerAvailable: StateFlow<Boolean> = _isAccelerometerAvailable.asStateFlow()

    private val _interactionDetected = MutableSharedFlow<InteractionEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val interactionDetected: SharedFlow<InteractionEvent> = _interactionDetected.asSharedFlow()

    private var lastShakeTime: Long = 0
    private var shakeCount = 0
    private var firstShakeTime: Long = 0
    private var isProcessingShake = false // Flag interne pour ce détecteur

    companion object {
        private const val SHAKE_THRESHOLD_GRAVITY = 2.7f
        private const val SHAKE_SLOP_TIME_MS = 500L
        private const val SHAKE_COUNT_RESET_TIME_MS = 3000L
        private const val SHAKE_MIN_COUNT = 1 // Nombre de mouvements rapides pour déclencher une secousse
        private const val PROCESSING_TIMEOUT_MS = 1500L
    }

    fun registerListener() {
        if (_isAccelerometerAvailable.value) {
            accelerometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    fun unregisterListener() {
        if (_isAccelerometerAvailable.value) {
            sensorManager.unregisterListener(this, accelerometer)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        if (isProcessingShake) return

        val currentTime = System.currentTimeMillis()
        if ((currentTime - lastShakeTime) < SHAKE_SLOP_TIME_MS && firstShakeTime != 0L) return


        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            if (firstShakeTime == 0L || (currentTime - firstShakeTime) > SHAKE_COUNT_RESET_TIME_MS) {
                firstShakeTime = currentTime
                shakeCount = 0
            }
            shakeCount++
            lastShakeTime = currentTime // Mettre à jour lastShakeTime à chaque mouvement détecté au-dessus du seuil

            if (shakeCount >= SHAKE_MIN_COUNT) {
                isProcessingShake = true
                // Réinitialiser les compteurs pour la prochaine détection de séquence de secousses
                shakeCount = 0
                firstShakeTime = 0L // Important pour que le prochain mouvement redémarre le comptage de séquence

                val emitted = _interactionDetected.tryEmit(ShakeEvent) // Émet l'objet ShakeEvent global
                if (!emitted) {
                    // Log.d("ShakeDetectVM", "ShakeEvent emission failed")
                    viewModelScope.launch {
                        delay(PROCESSING_TIMEOUT_MS)
                        if (isProcessingShake) {
                            isProcessingShake = false
                            // Log.d("ShakeDetectVM", "isProcessingShake reset due to emission failure timeout")
                        }
                    }
                }
            }
        }
    }

    fun completeInteractionProcessing() {
        if (isProcessingShake) {
            isProcessingShake = false
            // Log.d("ShakeDetectVM", "Shake interaction processing completed by orchestrator.")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Non utilisé
    }

    override fun onCleared() {
        super.onCleared()
        unregisterListener()
    }
}
