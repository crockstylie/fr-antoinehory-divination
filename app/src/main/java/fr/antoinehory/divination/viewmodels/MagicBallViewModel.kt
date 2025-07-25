package fr.antoinehory.divination.viewmodels

import android.app.Application
import fr.antoinehory.divination.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MagicBallViewModel(application: Application) : ShakeDetectViewModel(application) {

    // Récupérer une instance de l'Application pour accéder aux ressources
    private val app: Application = application

    // Charger les réponses depuis le string-array dans strings.xml
    private val possibleAnswers: List<String> by lazy {
        app.resources.getStringArray(R.array.magic_ball_possible_answers).toList()
    }

    private val _currentResponse = MutableStateFlow("")
    val currentResponse: StateFlow<String> = _currentResponse

    companion object {
        private const val SHUFFLE_ANIMATION_DELAY_MS = 1000L
    }

    init {
        // Déterminer le message initial en fonction de la disponibilité de l'accéléromètre
        // et charger la chaîne correspondante depuis strings.xml.
        // `isAccelerometerAvailable` provient de ShakeDetectViewModel.
        if (!isAccelerometerAvailable.value) {
            _currentResponse.value = app.getString(R.string.magic_ball_initial_prompt_no_accelerometer)
        } else {
            _currentResponse.value = app.getString(R.string.magic_ball_initial_prompt_shake)
        }
    }

    private fun pickNewResponse() {
        // Récupérer les messages "non-réponse" pour les exclure de la sélection aléatoire
        // si la réponse actuelle est l'un d'eux.
        val shufflingMessage = app.getString(R.string.magic_ball_shuffling_message)
        val initialShakeMessage = app.getString(R.string.magic_ball_initial_prompt_shake)
        val initialNoAccelerometerMessage = app.getString(R.string.magic_ball_initial_prompt_no_accelerometer)

        val availableResponses = if (possibleAnswers.size > 1 &&
            _currentResponse.value != shufflingMessage &&
            _currentResponse.value != initialShakeMessage &&
            _currentResponse.value != initialNoAccelerometerMessage) {
            // S'assurer que la réponse actuelle n'est pas un message de service
            // ET qu'elle n'est pas la réponse précédente pour éviter les répétitions immédiates.
            possibleAnswers.filterNot { it == _currentResponse.value }
        } else {
            possibleAnswers
        }

        _currentResponse.value = availableResponses.randomOrNull()
            ?: possibleAnswers.firstOrNull() // Fallback si la liste filtrée est vide
                    ?: initialShakeMessage // Fallback ultime au message d'invite par défaut
    }

    // `onShakeDetected` et `completeShakeProcessing` sont hérités de ShakeDetectViewModel
    // et leur logique interne reste la même, mais les messages qu'ils utilisent sont maintenant des ressources.
    override suspend fun onShakeDetected() {
        _currentResponse.value = app.getString(R.string.magic_ball_shuffling_message) // Utiliser la ressource
        delay(SHUFFLE_ANIMATION_DELAY_MS)
        pickNewResponse()
        completeShakeProcessing() // Méthode de la classe parente ShakeDetectViewModel
    }
}
