package fr.antoinehory.divination

import android.app.Application // Nécessaire pour accéder au Context
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.key.type
import androidx.lifecycle.AndroidViewModel // Changer de ViewModel à AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// Changer de ViewModel à AndroidViewModel pour pouvoir obtenir le contexte de l'application
class MagicBallViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val _currentResponse = mutableStateOf("Secouez pour obtenir une réponse")
    val currentResponse: State<String> = _currentResponse

    private val _isShuffling = mutableStateOf(false)
    val isShuffling: State<Boolean> = _isShuffling

    // --- AJOUTS POUR LE CAPTEUR ---
    private var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var lastUpdate: Long = 0
    private var last_x: Float = 0.0f
    private var last_y: Float = 0.0f
    private var last_z: Float = 0.0f
    private val shakeThreshold = 800 // Ajuste ce seuil si nécessaire

    private val _isAccelerometerAvailable = mutableStateOf<Boolean?>(null)
    val isAccelerometerAvailable: State<Boolean?> = _isAccelerometerAvailable
    // --- FIN DES AJOUTS POUR LE CAPTEUR ---


    private val possibleAnswers = listOf(
        "C'est certain.",
        "Sans aucun doute.",
        "Oui, absolument.",
        "Tu peux compter dessus.",
        "Selon toute vraisemblance.",
        "Très probable.",
        "Les perspectives sont bonnes.",
        "Oui.",
        "Les signes pointent vers oui.",
        "Réponse brumeuse, essaie encore.",
        "Demande à nouveau plus tard.",
        "Mieux vaut ne pas te le dire maintenant.",
        "Impossible de prédire maintenant.",
        "Concentre-toi et demande à nouveau.",
        "Ne compte pas dessus.",
        "Ma réponse est non.",
        "Mes sources disent non.",
        "Les perspectives ne sont pas si bonnes.",
        "Très peu probable."
    )

    init {
        // Initialisation du SensorManager
        sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        _isAccelerometerAvailable.value = (accelerometer != null)
    }

    fun startShuffleAndShowResponse() {
        if (_isShuffling.value) return // Empêche les déclenchements multiples

        viewModelScope.launch {
            _isShuffling.value = true
            _currentResponse.value = "..." // Indication de mélange

            val totalShuffleTime = 1500L // Durée totale du mélange en ms
            val interval = 100L // Intervalle de changement de texte en ms
            var elapsedTime = 0L

            while (elapsedTime < totalShuffleTime) {
                _currentResponse.value = possibleAnswers[Random.nextInt(possibleAnswers.size)]
                delay(interval)
                elapsedTime += interval
            }

            _currentResponse.value = possibleAnswers[Random.nextInt(possibleAnswers.size)]
            _isShuffling.value = false
        }
    }

    // --- MÉTHODES POUR GÉRER LE LISTENER DU CAPTEUR ---
    fun registerSensorListener() {
        if (_isAccelerometerAvailable.value == true) {
            accelerometer?.also { accel ->
                sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    fun unregisterSensorListener() {
        if (_isAccelerometerAvailable.value == true) {
            sensorManager.unregisterListener(this)
        }
    }

    // --- IMPLÉMENTATION DE SensorEventListener ---
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            if ((currentTime - lastUpdate) > 100) { // Limite la fréquence de vérification
                val diffTime = (currentTime - lastUpdate)
                lastUpdate = currentTime

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val speed = kotlin.math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000

                if (speed > shakeThreshold) {
                    if (!_isShuffling.value) {
                        startShuffleAndShowResponse()
                    }
                }
                last_x = x
                last_y = y
                last_z = z
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Non utilisé pour cette application
    }

    // Assure-toi de désenregistrer le listener quand le ViewModel est détruit
    override fun onCleared() {
        super.onCleared()
        unregisterSensorListener()
    }
}
