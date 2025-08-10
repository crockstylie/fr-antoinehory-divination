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

/**
 * Represents the possible outcomes of a Rock-Paper-Scissors game.
 */
enum class RPSOutcome { ROCK, PAPER, SCISSORS }

/**
 * ViewModel for the Rock-Paper-Scissors game screen.
 *
 * This ViewModel manages the game state, including the current message displayed to the user,
 * the outcome of the game, processing state, and recent game logs. It interacts with
 * [LaunchLogRepository] to save game results.
 *
 * @param application The application context, used for accessing resources like strings.
 * @param launchLogRepository The repository for saving and retrieving game launch logs.
 */
class RockPaperScissorsViewModel(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository
) : AndroidViewModel(application) {

    /**
     * Internal [MutableStateFlow] for the message currently displayed to the user
     * (e.g., prompt, processing message, result).
     */
    private val _currentMessage = MutableStateFlow("")
    /**
     * Public [StateFlow] exposing the current message to be displayed.
     */
    val currentMessage: StateFlow<String> = _currentMessage.asStateFlow()

    /**
     * Internal [MutableStateFlow] for the outcome of the Rock-Paper-Scissors game.
     * `null` if the game is not yet played or is in progress.
     */
    private val _rpsOutcome = MutableStateFlow<RPSOutcome?>(null)
    /**
     * Public [StateFlow] exposing the outcome of the game.
     */
    val rpsOutcome: StateFlow<RPSOutcome?> = _rpsOutcome.asStateFlow()

    /**
     * Internal [MutableStateFlow] indicating whether the game logic is currently processing.
     * `true` while simulating the game, `false` otherwise.
     */
    private val _isProcessing = MutableStateFlow(false)
    /**
     * Public [StateFlow] exposing the processing state of the game.
     */
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    /**
     * Internal [MutableStateFlow] for the list of recent Rock-Paper-Scissors game logs.
     */
    private val _recentLogs = MutableStateFlow<List<LaunchLog>>(emptyList())
    /**
     * Public [StateFlow] exposing the list of recent game logs.
     */
    val recentLogs: StateFlow<List<LaunchLog>> = _recentLogs.asStateFlow()

    companion object {
        /** Delay in milliseconds to simulate game processing. */
        private const val PROCESSING_DELAY_MS = 700L
        /** Maximum number of recent game logs to fetch and display. */
        private const val MAX_RECENT_LOGS = 10
    }

    /**
     * Initializes the ViewModel.
     * Sets the initial game state message and launches a coroutine to load recent game logs.
     */
    init {
        initializeRPSState(application.getString(R.string.rps_initial_prompt_generic))
        viewModelScope.launch {
            launchLogRepository.getRecentLogsByGameType(GameType.ROCK_PAPER_SCISSORS, MAX_RECENT_LOGS)
                .collect { logs ->
                    _recentLogs.value = logs
                }
        }
    }

    /**
     * Resets the game state to its initial values.
     *
     * @param initialMessage The message to display when the game is initialized.
     */
    private fun initializeRPSState(initialMessage: String) {
        _rpsOutcome.value = null
        _currentMessage.value = initialMessage
        _isProcessing.value = false
    }

    /**
     * Determines a random outcome for the Rock-Paper-Scissors game and updates
     * the [_rpsOutcome] and [_currentMessage] accordingly.
     */
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

    /**
     * Starts a new Rock-Paper-Scissors game.
     * Sets the processing state, simulates a delay, determines the outcome,
     * logs the result, and then resets the processing state.
     * Does nothing if a game is already in progress.
     */
    fun playGame() {
        if (_isProcessing.value) return

        viewModelScope.launch {
            _isProcessing.value = true
            _currentMessage.value = application.getString(R.string.rps_processing_message)
            _rpsOutcome.value = null // Clear previous outcome during processing
            delay(PROCESSING_DELAY_MS)
            determineRPSOutcome()
            _rpsOutcome.value?.let { outcome -> // Ensure outcome is not null before logging
                launchLogRepository.insertLog(GameType.ROCK_PAPER_SCISSORS, outcome.name)
            }
            _isProcessing.value = false
        }
    }
}

/**
 * Factory for creating instances of [RockPaperScissorsViewModel].
 *
 * This factory is necessary because [RockPaperScissorsViewModel] has constructor dependencies
 * on [Application] and [LaunchLogRepository], which need to be provided during ViewModel creation.
 *
 * @param application The application context.
 * @param launchLogRepository The repository for launch log data.
 */
class RockPaperScissorsViewModelFactory(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository
) : ViewModelProvider.Factory {
    /**
     * Creates a new instance of the given `modelClass`.
     *
     * @param modelClass A class whose instance is requested.
     * @return A newly created ViewModel.
     * @throws IllegalArgumentException if `modelClass` is not assignable from [RockPaperScissorsViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RockPaperScissorsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RockPaperScissorsViewModel(application, launchLogRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
