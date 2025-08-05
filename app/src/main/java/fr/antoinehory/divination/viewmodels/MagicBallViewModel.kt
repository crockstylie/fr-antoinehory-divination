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

class MagicBallViewModel(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository
) : AndroidViewModel(application) {

    private val possibleAnswers: List<String> by lazy {
        application.resources.getStringArray(R.array.magic_ball_possible_answers).toList()
    }

    private val _currentResponse = MutableStateFlow("")
    val currentResponse: StateFlow<String> = _currentResponse.asStateFlow()

    private val _isPredicting = MutableStateFlow(false)
    val isPredicting: StateFlow<Boolean> = _isPredicting.asStateFlow()

    companion object {
        private const val PREDICTION_DELAY_MS = 1000L
        private const val FALLBACK_LOG_IDENTIFIER = "FALLBACK" // Identifiant pour les réponses par défaut
    }

    init {
        _currentResponse.value = application.getString(R.string.magic_ball_initial_prompt_generic)
    }

    private fun pickNewResponse(): String {
        val shufflingMessage = application.getString(R.string.magic_ball_shuffling_message)
        val initialGenericMessage = application.getString(R.string.magic_ball_initial_prompt_generic)

        val availableResponses = if (possibleAnswers.size > 1 &&
            _currentResponse.value != shufflingMessage &&
            _currentResponse.value != initialGenericMessage) {
            possibleAnswers.filterNot { it == _currentResponse.value }
        } else {
            possibleAnswers
        }
        
        return availableResponses.randomOrNull()
            ?: possibleAnswers.firstOrNull()
            ?: application.getString(R.string.magic_ball_default_answer_if_empty)
    }

    fun getNewPrediction() {
        if (_isPredicting.value) return

        viewModelScope.launch {
            _isPredicting.value = true
            _currentResponse.value = application.getString(R.string.magic_ball_shuffling_message)
            delay(PREDICTION_DELAY_MS)
            
            val newResponse = pickNewResponse()
            _currentResponse.value = newResponse
            
            // MODIFIÉ: Enregistre l'index ou un identifiant de fallback pour le log
            val answerIndex = possibleAnswers.indexOf(newResponse)
            val loggableResult = if (answerIndex != -1) {
                answerIndex.toString() // Log l'index sous forme de chaîne
            } else {
                // Ce cas se produirait si newResponse est la réponse par défaut
                // qui n'est pas dans la liste `possibleAnswers`
                FALLBACK_LOG_IDENTIFIER 
            }
            launchLogRepository.insertLog(GameType.MAGIC_EIGHT_BALL, loggableResult)
            
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

