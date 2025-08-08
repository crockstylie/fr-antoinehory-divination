package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.data.repository.LaunchLogRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
// Import nécessaire pour LaunchLog
import fr.antoinehory.divination.data.database.entity.LaunchLog

class MagicBallViewModel(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository
) : AndroidViewModel(application) {

    val possibleAnswers: List<String> by lazy {
        application.resources.getStringArray(R.array.magic_ball_possible_answers).toList()
    }

    private val _currentResponse = MutableStateFlow("")
    val currentResponse: StateFlow<String> = _currentResponse.asStateFlow()

    private val _isPredicting = MutableStateFlow(false)
    val isPredicting: StateFlow<Boolean> = _isPredicting.asStateFlow()

    private val _recentLogs = MutableStateFlow<List<LaunchLog>>(emptyList())
    val recentLogs: StateFlow<List<LaunchLog>> = _recentLogs.asStateFlow()

    companion object {
        private const val PREDICTION_DELAY_MS = 1000L
        // MODIFICATION: FALLBACK_LOG_IDENTIFIER n'est plus utilisé pour le logging de la réponse.
        // Il peut être conservé s'il a d'autres usages, sinon il pourrait être supprimé.
        // Pour l'instant, je le laisse commenté au cas où.
        // const val FALLBACK_LOG_IDENTIFIER = "FALLBACK"
    }

    init {
        _currentResponse.value = application.getString(R.string.magic_ball_initial_prompt_generic)
        viewModelScope.launch {
            launchLogRepository.getRecentLogsByGameType(GameType.MAGIC_EIGHT_BALL, 10)
                .collect { logs ->
                    _recentLogs.value = logs
                }
        }
    }

    private fun pickNewResponse(): String {
        val shufflingMessage = application.getString(R.string.magic_ball_shuffling_message)
        val initialGenericMessage = application.getString(R.string.magic_ball_initial_prompt_generic)
        val defaultAnswerIfEmpty = application.getString(R.string.magic_ball_default_answer_if_empty)

        // Si possibleAnswers est vide, retourner directement la réponse par défaut.
        if (possibleAnswers.isEmpty()) {
            return defaultAnswerIfEmpty
        }

        val availableResponses = if (possibleAnswers.size > 1 &&
            _currentResponse.value != shufflingMessage &&
            _currentResponse.value != initialGenericMessage) {
            possibleAnswers.filterNot { it == _currentResponse.value }
        } else {
            possibleAnswers
        }

        // S'assurer que availableResponses n'est pas vide après le filtrage.
        // Si elle devient vide (par exemple, si possibleAnswers n'avait qu'un seul élément qui était le _currentResponse),
        // on retourne la première réponse de la liste originale, ou la réponse par défaut si la liste originale était vide.
        return availableResponses.randomOrNull()
            ?: possibleAnswers.firstOrNull() // Pour le cas où availableResponses serait vide après filtrage
            ?: defaultAnswerIfEmpty
    }

    fun getNewPrediction() {
        if (_isPredicting.value) return

        viewModelScope.launch {
            _isPredicting.value = true
            _currentResponse.value = application.getString(R.string.magic_ball_shuffling_message)
            delay(PREDICTION_DELAY_MS)

            val newResponse = pickNewResponse()
            _currentResponse.value = newResponse

            // MODIFICATION: Enregistrer la chaîne de réponse complète directement.
            // La variable 'newResponse' contient déjà la chaîne de caractères à enregistrer,
            // que ce soit une réponse de 'possibleAnswers' ou la réponse par défaut.
            launchLogRepository.insertLog(GameType.MAGIC_EIGHT_BALL, newResponse)

            _isPredicting.value = false
        }
    }
}

class MagicBallViewModelFactory(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MagicBallViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MagicBallViewModel(application, launchLogRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

