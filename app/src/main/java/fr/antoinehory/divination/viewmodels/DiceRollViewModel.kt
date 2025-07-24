package fr.antoinehory.divination.viewmodels

import android.app.Application
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class DiceRollViewModel(application: Application) : ShakeDetectViewModel(application) {

    private val _displayText = MutableStateFlow("")
    val displayText: StateFlow<String> = _displayText

    companion object {
        private const val ROLL_ANIMATION_DELAY_MS = 700L
        private const val INITIAL_MESSAGE_NO_ACCELEROMETER = "Secouez (accéléromètre non détecté)."
        private const val INITIAL_MESSAGE_SHAKE = "Secouez pour lancer le dé !"
        private const val ROLLING_MESSAGE = "Lancement du dé..."
    }

    init {
        if (!isAccelerometerAvailable.value) {
            _displayText.value = INITIAL_MESSAGE_NO_ACCELEROMETER
        } else {
            _displayText.value = INITIAL_MESSAGE_SHAKE
        }
    }

    private fun rollD6AndSetText() {
        val result = Random.nextInt(1, 7) // Génère un nombre entre 1 et 6 inclus
        _displayText.value = "Résultat : $result"
    }

    override suspend fun onShakeDetected() {
        _displayText.value = ROLLING_MESSAGE
        delay(ROLL_ANIMATION_DELAY_MS)
        rollD6AndSetText()
        completeShakeProcessing()
    }
}