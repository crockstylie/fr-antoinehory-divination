package fr.antoinehory.divination.viewmodels

import android.app.Application
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MagicBallViewModel(application: Application) : ShakeDetectViewModel(application) {

    private val possibleAnswers = listOf(
        "C'est certain.", "Sans aucun doute.", "Oui, absolument.", "Tu peux compter dessus.",
        "Selon toute vraisemblance.", "Très probable.", "Les perspectives sont bonnes.", "Oui.",
        "Les signes pointent vers oui.", "Réponse brumeuse, essaie encore.", "Demande à nouveau plus tard.",
        "Mieux vaut ne pas te le dire maintenant.", "Impossible de prédire maintenant.",
        "Concentre-toi et demande à nouveau.", "Ne compte pas dessus.", "Ma réponse est non.",
        "Mes sources disent non.", "Les perspectives ne sont pas si bonnes.", "Très peu probable."
    )

    private val _currentResponse = MutableStateFlow("")
    val currentResponse: StateFlow<String> = _currentResponse

    companion object {
        private const val SHUFFLE_ANIMATION_DELAY_MS = 1000L
        private const val INITIAL_MESSAGE_NO_ACCELEROMETER = "Secouez l'appareil (accéléromètre non détecté)."
        private const val INITIAL_MESSAGE_SHAKE = "Secouez l'appareil pour obtenir une réponse."
        private const val SHUFFLING_MESSAGE = "..."
    }

    init {
        if (!isAccelerometerAvailable.value) {
            _currentResponse.value = INITIAL_MESSAGE_NO_ACCELEROMETER
        } else {
            _currentResponse.value = INITIAL_MESSAGE_SHAKE
        }
    }

    private fun pickNewResponse() {
        val availableResponses = if (possibleAnswers.size > 1 && _currentResponse.value != SHUFFLING_MESSAGE && _currentResponse.value != INITIAL_MESSAGE_SHAKE && _currentResponse.value != INITIAL_MESSAGE_NO_ACCELEROMETER) {
            possibleAnswers.filterNot { it == _currentResponse.value }
        } else {
            possibleAnswers
        }
        _currentResponse.value = availableResponses.randomOrNull() ?: possibleAnswers.firstOrNull() ?: INITIAL_MESSAGE_SHAKE
    }

    override suspend fun onShakeDetected() {
        _currentResponse.value = SHUFFLING_MESSAGE
        delay(SHUFFLE_ANIMATION_DELAY_MS)
        pickNewResponse()
        completeShakeProcessing()
    }
}
