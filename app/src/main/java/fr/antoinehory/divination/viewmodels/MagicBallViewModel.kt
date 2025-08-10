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
import fr.antoinehory.divination.data.database.entity.LaunchLog

/**
 * ViewModel for the Magic 8-Ball game screen.
 *
 * This ViewModel manages the game state, including possible answers, the current response,
 * the prediction state, and recent game logs. It interacts with [LaunchLogRepository]
 * to save game results.
 *
 * @param application The application context, used for accessing resources like string arrays.
 * @param launchLogRepository The repository for saving and retrieving game launch logs.
 */
class MagicBallViewModel(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository
) : AndroidViewModel(application) {

    /**
     * A list of possible answers for the Magic 8-Ball.
     * Loaded lazily from the `magic_ball_possible_answers` string array resource.
     */
    val possibleAnswers: List<String> by lazy {
        application.resources.getStringArray(R.array.magic_ball_possible_answers).toList()
    }

    /**
     * Internal [MutableStateFlow] for the current response displayed by the Magic 8-Ball.
     */
    private val _currentResponse = MutableStateFlow("")
    /**
     * Public [StateFlow] exposing the current response of the Magic 8-Ball.
     */
    val currentResponse: StateFlow<String> = _currentResponse.asStateFlow()

    /**
     * Internal [MutableStateFlow] indicating whether the Magic 8-Ball is currently "predicting".
     * `true` while simulating a prediction, `false` otherwise.
     */
    private val _isPredicting = MutableStateFlow(false)
    /**
     * Public [StateFlow] exposing the prediction state.
     */
    val isPredicting: StateFlow<Boolean> = _isPredicting.asStateFlow()

    /**
     * Internal [MutableStateFlow] for the list of recent Magic 8-Ball game logs.
     */
    private val _recentLogs = MutableStateFlow<List<LaunchLog>>(emptyList())
    /**
     * Public [StateFlow] exposing the list of recent game logs.
     */
    val recentLogs: StateFlow<List<LaunchLog>> = _recentLogs.asStateFlow()

    companion object {
        /** Delay in milliseconds to simulate the prediction process. */
        private const val PREDICTION_DELAY_MS = 1000L
        /** Maximum number of recent game logs to fetch and display. */
        private const val MAX_RECENT_LOGS = 10 // Added based on usage in init
    }

    /**
     * Initializes the ViewModel.
     * Sets the initial response message and launches a coroutine to load recent game logs.
     */
    init {
        _currentResponse.value = application.getString(R.string.magic_ball_initial_prompt_generic)
        viewModelScope.launch {
            launchLogRepository.getRecentLogsByGameType(GameType.MAGIC_EIGHT_BALL, MAX_RECENT_LOGS)
                .collect { logs ->
                    _recentLogs.value = logs
                }
        }
    }

    /**
     * Picks a new random response from the [possibleAnswers] list.
     *
     * It avoids picking the same response as the current one if multiple answers are available
     * and the current response is not one of the initial/shuffling messages.
     * If no answers are configured, it returns a default message.
     *
     * @return A string representing the new response.
     */
    private fun pickNewResponse(): String {
        val shufflingMessage = application.getString(R.string.magic_ball_shuffling_message)
        val initialGenericMessage = application.getString(R.string.magic_ball_initial_prompt_generic)
        val defaultAnswerIfEmpty = application.getString(R.string.magic_ball_default_answer_if_empty)

        if (possibleAnswers.isEmpty()) {
            return defaultAnswerIfEmpty
        }

        val availableResponses = if (possibleAnswers.size > 1 &&
            _currentResponse.value != shufflingMessage &&
            _currentResponse.value != initialGenericMessage) {
            // Filter out the current response if it's a valid answer and not an intermediate message
            possibleAnswers.filterNot { it == _currentResponse.value }
        } else {
            possibleAnswers
        }
        // If filtering resulted in an empty list (e.g., only one answer and it was current), fall back to all possible answers.
        val responsesToChooseFrom = if (availableResponses.isEmpty() && possibleAnswers.isNotEmpty()) possibleAnswers else availableResponses

        return responsesToChooseFrom.randomOrNull()
            ?: possibleAnswers.firstOrNull() // Fallback if randomOrNull gives null (e.g. empty list after filter)
            ?: defaultAnswerIfEmpty // Ultimate fallback
    }

    /**
     * Initiates a new prediction from the Magic 8-Ball.
     * Sets the predicting state, displays a shuffling message, simulates a delay,
     * picks a new response, logs the result, and then resets the predicting state.
     * Does nothing if a prediction is already in progress.
     */
    fun getNewPrediction() {
        if (_isPredicting.value) return

        viewModelScope.launch {
            _isPredicting.value = true
            _currentResponse.value = application.getString(R.string.magic_ball_shuffling_message)
            delay(PREDICTION_DELAY_MS)

            val newResponse = pickNewResponse()
            _currentResponse.value = newResponse

            launchLogRepository.insertLog(GameType.MAGIC_EIGHT_BALL, newResponse)

            _isPredicting.value = false
        }
    }
}

/**
 * Factory for creating instances of [MagicBallViewModel].
 *
 * This factory is necessary because [MagicBallViewModel] has constructor dependencies
 * on [Application] and [LaunchLogRepository], which need to be provided during ViewModel creation.
 *
 * @param application The application context.
 * @param launchLogRepository The repository for launch log data.
 */
class MagicBallViewModelFactory(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository
) : ViewModelProvider.Factory {
    /**
     * Creates a new instance of the given `modelClass`.
     *
     * @param modelClass A class whose instance is requested.
     * @return A newly created ViewModel.
     * @throws IllegalArgumentException if `modelClass` is not assignable from [MagicBallViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MagicBallViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MagicBallViewModel(application, launchLogRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
