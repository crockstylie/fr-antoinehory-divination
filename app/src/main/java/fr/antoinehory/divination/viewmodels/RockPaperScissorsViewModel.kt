package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel // AJOUT: Pour ViewModelProvider.Factory
import androidx.lifecycle.ViewModelProvider // AJOUT: Pour ViewModelProvider.Factory
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.model.GameType // AJOUT: Pour GameType
import fr.antoinehory.divination.data.repository.LaunchLogRepository // AJOUT: Repository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class RPSOutcome { ROCK, PAPER, SCISSORS }

class RockPaperScissorsViewModel(
    private val application: Application, // Gardé comme private val
    private val launchLogRepository: LaunchLogRepository // AJOUT: Repository
) : AndroidViewModel(application) {

    // private val app: Application = application // 'app' est redondant

    private val _currentMessage = MutableStateFlow("")
    val currentMessage: StateFlow<String> = _currentMessage.asStateFlow()

    private val _rpsOutcome = MutableStateFlow<RPSOutcome?>(null)
    val rpsOutcome: StateFlow<RPSOutcome?> = _rpsOutcome.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    companion object {
        private const val PROCESSING_DELAY_MS = 700L
    }

    init {
        initializeRPSState(application.getString(R.string.rps_initial_prompt_generic))
    }

    private fun initializeRPSState(initialMessage: String) {
        _rpsOutcome.value = null
        _currentMessage.value = initialMessage
        _isProcessing.value = false
    }

    private fun determineRPSOutcome() {
        val choices = RPSOutcome.entries
        val randomOutcome = choices[Random.nextInt(choices.size)]

        _rpsOutcome.value = randomOutcome
        _currentMessage.value = when (randomOutcome) {
            RPSOutcome.ROCK -> application.getString(R.string.rps_result_rock)
            RPSOutcome.PAPER -> application.getString(R.string.rps_result_paper)
            RPSOutcome.SCISSORS -> application.getString(R.string.rps_result_scissors)
        }
    }

    fun playGame() {
        if (_isProcessing.value) return

        viewModelScope.launch {
            _isProcessing.value = true
            _currentMessage.value = application.getString(R.string.rps_processing_message)
            _rpsOutcome.value = null
            delay(PROCESSING_DELAY_MS)
            determineRPSOutcome()
            // Enregistre le log après avoir déterminé le résultat
            _rpsOutcome.value?.let { outcome -> // S'assurer que l'outcome n'est pas null
                launchLogRepository.insertLog(GameType.ROCK_PAPER_SCISSORS, outcome.name)
            }
            _isProcessing.value = false
        }
    }
}

// AJOUT: Factory pour RockPaperScissorsViewModel
class RockPaperScissorsViewModelFactory(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RockPaperScissorsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RockPaperScissorsViewModel(application, launchLogRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
