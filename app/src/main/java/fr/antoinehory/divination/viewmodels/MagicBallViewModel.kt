package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel // Changement de l'héritage
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MagicBallViewModel(application: Application) : AndroidViewModel(application) { // Plus d'héritage de ShakeDetectViewModel

    private val app: Application = application

    private val possibleAnswers: List<String> by lazy {
        app.resources.getStringArray(R.array.magic_ball_possible_answers).toList()
    }

    private val _currentResponse = MutableStateFlow("")
    val currentResponse: StateFlow<String> = _currentResponse.asStateFlow()

    // Nouvel état pour gérer l'animation de "prédiction en cours"
    private val _isPredicting = MutableStateFlow(false)
    val isPredicting: StateFlow<Boolean> = _isPredicting.asStateFlow()

    companion object {
        private const val PREDICTION_DELAY_MS = 1000L // Délai pour simuler la réflexion
    }

    init {
        // Message initial simple. L'UI ou InteractionDetectViewModel gérera les invites plus spécifiques.
        _currentResponse.value = app.getString(R.string.magic_ball_initial_prompt_generic)
    }

    private fun pickNewResponse() {
        val shufflingMessage = app.getString(R.string.magic_ball_shuffling_message) // Peut toujours être utilisé si souhaité
        val initialGenericMessage = app.getString(R.string.magic_ball_initial_prompt_generic)

        val availableResponses = if (possibleAnswers.size > 1 &&
            _currentResponse.value != shufflingMessage && // Si vous gardez le message de mélange
            _currentResponse.value != initialGenericMessage) {
            possibleAnswers.filterNot { it == _currentResponse.value }
        } else {
            possibleAnswers
        }

        _currentResponse.value = availableResponses.randomOrNull()
            ?: possibleAnswers.firstOrNull()
                    ?: app.getString(R.string.magic_ball_default_answer_if_empty) // Un fallback si tout échoue
    }

    // Fonction publique que l'UI appellera lorsqu'une interaction est détectée
    fun getNewPrediction() {
        if (_isPredicting.value) return // Empêcher les appels multiples pendant une prédiction

        viewModelScope.launch {
            _isPredicting.value = true
            _currentResponse.value = app.getString(R.string.magic_ball_shuffling_message) // Message d'attente
            delay(PREDICTION_DELAY_MS)
            pickNewResponse()
            _isPredicting.value = false
        }
    }
}

