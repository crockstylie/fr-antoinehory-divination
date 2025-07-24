package fr.antoinehory.divination.viewmodels

import android.app.Application
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RockPaperScissorsViewModel(application: Application) : ShakeDetectViewModel(application) {

    private enum class Choice(val displayText: String) {
        ROCK("Pierre !"),
        PAPER("Feuille !"),
        SCISSORS("Ciseaux !")
    }

    private val _displayText = MutableStateFlow("")
    val displayText: StateFlow<String> = _displayText

    companion object {
        private const val PLAY_ANIMATION_DELAY_MS = 700L
        private const val INITIAL_MESSAGE_NO_ACCELEROMETER = "Secouez (accéléromètre non détecté)."
        private const val INITIAL_MESSAGE_SHAKE = "Secouez pour jouer !"
        private const val PLAYING_MESSAGE = "Shifoumi..."
    }

    init {
        if (!isAccelerometerAvailable.value) {
            _displayText.value = INITIAL_MESSAGE_NO_ACCELEROMETER
        } else {
            _displayText.value = INITIAL_MESSAGE_SHAKE
        }
    }

    private fun playAndSetText() {
        val choices = Choice.values()
        val randomChoice = choices.random()
        _displayText.value = randomChoice.displayText
    }

    override suspend fun onShakeDetected() {
        _displayText.value = PLAYING_MESSAGE
        delay(PLAY_ANIMATION_DELAY_MS)
        playAndSetText()
        completeShakeProcessing()
    }
}
