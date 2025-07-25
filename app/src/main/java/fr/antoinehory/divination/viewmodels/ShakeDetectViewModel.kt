package fr.antoinehory.divination.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

abstract class ShakeDetectViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _isAccelerometerAvailable = MutableStateFlow(accelerometer != null)
    val isAccelerometerAvailable: StateFlow<Boolean> = _isAccelerometerAvailable

    private val _isProcessingShake = MutableStateFlow(false) // Indique si une secousse est en cours de traitement
    val isProcessingShake: StateFlow<Boolean> = _isProcessingShake

    private var lastShakeTime: Long = 0
    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private var lastZ: Float = 0f

    companion object {
        // Ajuste ces valeurs selon la sensibilité désirée
        private const val SHAKE_THRESHOLD_GRAVITY = 2.7f // Seuil basé sur la force g
        private const val SHAKE_SLOP_TIME_MS = 500 // Temps minimum entre les lectures pour considérer une secousse
        private const val SHAKE_COUNT_RESET_TIME_MS = 3000 // Temps pour réinitialiser le compteur de secousses
        private const val SHAKE_MIN_COUNT = 1 // Nombre de mouvements rapides pour déclencher une secousse
    }

    private var shakeCount = 0
    private var firstShakeTime: Long = 0

    init {
        if (accelerometer == null) {
            _isAccelerometerAvailable.value = false
        }
    }

    fun registerSensorListener() {
        if (_isAccelerometerAvailable.value) {
            accelerometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    fun unregisterSensorListener() {
        if (_isAccelerometerAvailable.value) {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            if ((currentTime - lastShakeTime) > SHAKE_SLOP_TIME_MS) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val gX = x / SensorManager.GRAVITY_EARTH
                val gY = y / SensorManager.GRAVITY_EARTH
                val gZ = z / SensorManager.GRAVITY_EARTH

                // gForce est la racine carrée de la somme des carrés des accélérations normalisées
                val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

                if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                    if (firstShakeTime == 0L || (currentTime - firstShakeTime) > SHAKE_COUNT_RESET_TIME_MS) {
                        firstShakeTime = currentTime
                        shakeCount = 0
                    }
                    shakeCount++
                    if (shakeCount >= SHAKE_MIN_COUNT && !_isProcessingShake.value) {
                        lastShakeTime = currentTime // Met à jour le temps de la dernière secousse traitée
                        shakeCount = 0 // Réinitialise pour la prochaine détection
                        firstShakeTime = 0L
                        viewModelScope.launch {
                            _isProcessingShake.value = true
                            onShakeDetected()
                            // _isProcessingShake sera remis à false par la sous-classe
                        }
                    }
                }
                // Mise à jour pour la prochaine détection de mouvement (pas utilisé dans ce modèle de secousse mais bon à avoir)
                // lastX = x
                // lastY = y
                // lastZ = z
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Pas nécessaire pour cette implémentation
    }

    protected abstract suspend fun onShakeDetected()

    protected fun completeShakeProcessing() {
        _isProcessingShake.value = false
    }

    override fun onCleared() {
        super.onCleared()
        unregisterSensorListener()
    }
}
