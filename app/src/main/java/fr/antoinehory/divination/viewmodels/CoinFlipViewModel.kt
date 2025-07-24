package fr.antoinehory.divination.viewmodels

import android.app.Application
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CoinFlipViewModel(application: Application) : ShakeDetectViewModel(application) {

    private enum class CoinSide(val textResult: String) {
        PILE("Pile !"),
        FACE("Face !")
    }

    private val _displayText = MutableStateFlow("")
    val displayText: StateFlow<String> = _displayText

    companion object {
        private const val FLIP_ANIMATION_DELAY_MS = 700L
        private const val INITIAL_MESSAGE_NO_ACCELEROMETER = "Secouez (accéléromètre non détecté)."
        private const val INITIAL_MESSAGE_SHAKE = "Secouez pour lancer la pièce !"
        private const val FLIPPING_MESSAGE = "Lancement..."
    }

    init {
        if (!isAccelerometerAvailable.value) {
            _displayText.value = INITIAL_MESSAGE_NO_ACCELEROMETER
        } else {
            _displayText.value = INITIAL_MESSAGE_SHAKE
        }
    }

    private fun flipCoinAndSetText() {
        val result = if (Math.random() < 0.5) CoinSide.PILE else CoinSide.FACE
        _displayText.value = result.textResult
    }

    override suspend fun onShakeDetected() {
        _displayText.value = FLIPPING_MESSAGE
        delay(FLIP_ANIMATION_DELAY_MS)
        flipCoinAndSetText()
        completeShakeProcessing()
    }
}
