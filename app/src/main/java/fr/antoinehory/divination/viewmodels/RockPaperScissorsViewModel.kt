package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.data.repository.LaunchLogRepository
import fr.antoinehory.divination.data.database.entity.LaunchLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class RPSOutcome { ROCK, PAPER, SCISSORS }

class RockPaperScissorsViewModel(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository
) : AndroidViewModel(application) {

    private val _currentMessage = MutableStateFlow("")
    val currentMessage: StateFlow<String> = _currentMessage.asStateFlow()

    private val _rpsOutcome = MutableStateFlow<RPSOutcome?>(null)
    val rpsOutcome: StateFlow<RPSOutcome?> = _rpsOutcome.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _recentLogs = MutableStateFlow<List<LaunchLog>>(emptyList())
    val recentLogs: StateFlow<List<LaunchLog>> = _recentLogs.asStateFlow()

    companion object {
        private const val PROCESSING_DELAY_MS = 700L
        // S'assurer que c'est bien 10 ici, comme dans DiceRollViewModel si DiceRoll en affiche 10
        private const val MAX_RECENT_LOGS = 10
    }

    init {
        initializeRPSState(application.getString(R.string.rps_initial_prompt_generic))
        viewModelScope.launch {
            // Doit bien utiliser MAX_RECENT_LOGS (qui est 10)
            launchLogRepository.getRecentLogsByGameType(GameType.ROCK_PAPER_SCISSORS, MAX_RECENT_LOGS)
                .collect { logs ->
                    _recentLogs.value = logs
                }
        }
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
            _rpsOutcome.value?.let { outcome -> // S'assurer que outcome n'est pas null
                launchLogRepository.insertLog(GameType.ROCK_PAPER_SCISSORS, outcome.name)
            }
            _isProcessing.value = false
        }
    }
}

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
