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
import kotlin.random.Random
import fr.antoinehory.divination.data.database.entity.LaunchLog

/**
 * ViewModel for the Coin Flip game screen.
 *
 * This ViewModel manages the game state, including the current face of the coin,
 * the message displayed to the user, the flipping animation state, and recent game logs.
 * It interacts with [LaunchLogRepository] to save game results.
 *
 * @param application The application context, used for accessing resources like strings.
 * @param launchLogRepository The repository for saving and retrieving game launch logs.
 */
class CoinFlipViewModel(
    application: Application,
    private val launchLogRepository: LaunchLogRepository
) : AndroidViewModel(application) {

    /** Application context, used for accessing string resources. */
    private val app: Application = application

    /**
     * Internal [MutableStateFlow] for the current face of the coin (Heads or Tails).
     * `null` if the coin has not been flipped yet or is in the process of flipping.
     */
    private val _coinFace = MutableStateFlow<CoinFace?>(null)
    /**
     * Public [StateFlow] exposing the current face of the coin.
     */
    val coinFace: StateFlow<CoinFace?> = _coinFace.asStateFlow()

    /**
     * Internal [MutableStateFlow] for the message currently displayed to the user
     * (e.g., prompt, flipping message, result).
     */
    private val _currentMessage = MutableStateFlow("")
    /**
     * Public [StateFlow] exposing the current message to be displayed.
     */
    val currentMessage: StateFlow<String> = _currentMessage.asStateFlow()

    /**
     * Internal [MutableStateFlow] indicating whether the coin flip animation is active.
     * `true` during the flip animation, `false` otherwise.
     */
    private val _isFlipping = MutableStateFlow(false)
    /**
     * Public [StateFlow] exposing the flipping state of the coin.
     */
    val isFlipping: StateFlow<Boolean> = _isFlipping.asStateFlow()

    /**
     * Internal [MutableStateFlow] for the list of recent coin flip game logs.
     */
    private val _recentLogs = MutableStateFlow<List<LaunchLog>>(emptyList())
    /**
     * Public [StateFlow] exposing the list of recent game logs.
     */
    val recentLogs: StateFlow<List<LaunchLog>> = _recentLogs.asStateFlow()

    companion object {
        /** Delay in milliseconds for the coin flip animation. */
        private const val FLIP_ANIMATION_DELAY_MS = 1000L
        /** Maximum number of recent game logs to fetch and display. */
        private const val MAX_RECENT_LOGS = 10 // Added based on usage in init
    }

    /**
     * Initializes the ViewModel.
     * Sets the initial coin flip prompt message and launches a coroutine to load recent game logs.
     */
    init {
        _currentMessage.value = app.getString(R.string.coin_flip_initial_prompt_generic)
        viewModelScope.launch {
            launchLogRepository.getRecentLogsByGameType(GameType.COIN_FLIP, MAX_RECENT_LOGS)
                .collect { logs ->
                    _recentLogs.value = logs
                }
        }
    }

    /**
     * Determines the result of the coin flip (Heads or Tails) randomly.
     * Updates the [_coinFace] and [_currentMessage] with the result.
     * Logs the outcome using [LaunchLogRepository].
     */
    private fun determineFlipResult() {
        viewModelScope.launch {
            val isHeads = Random.nextBoolean()
            val resultFace = if (isHeads) CoinFace.HEADS else CoinFace.TAILS
            _coinFace.value = resultFace
            _currentMessage.value = if (isHeads) {
                app.getString(R.string.coin_flip_result_heads)
            } else {
                app.getString(R.string.coin_flip_result_tails)
            }

            launchLogRepository.insertLog(
                gameType = GameType.COIN_FLIP,
                result = resultFace.name
            )
        }
    }

    /**
     * Performs a coin flip.
     * Sets the flipping state, displays a flipping message, clears the previous coin face,
     * simulates the flip animation delay, determines the result, and then resets the flipping state.
     * Does nothing if a flip is already in progress.
     */
    fun performCoinFlip() {
        if (_isFlipping.value) return

        viewModelScope.launch {
            _isFlipping.value = true
            _currentMessage.value = app.getString(R.string.coin_flip_flipping_message)
            _coinFace.value = null // Clear previous face during flip
            delay(FLIP_ANIMATION_DELAY_MS)
            determineFlipResult()
            _isFlipping.value = false
        }
    }
}

/**
 * Represents the possible faces of a coin.
 */
enum class CoinFace {
    /** Represents the "Heads" side of the coin. */
    HEADS,
    /** Represents the "Tails" side of the coin. */
    TAILS
}

/**
 * Factory for creating instances of [CoinFlipViewModel].
 *
 * This factory is necessary because [CoinFlipViewModel] has constructor dependencies
 * on [Application] and [LaunchLogRepository], which need to be provided during ViewModel creation.
 *
 * @param application The application context.
 * @param launchLogRepository The repository for launch log data.
 */
class CoinFlipViewModelFactory(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository
) : ViewModelProvider.Factory {
    /**
     * Creates a new instance of the given `modelClass`.
     *
     * @param T The type of the ViewModel to create.
     * @param modelClass A class whose instance is requested.
     * @return A newly created ViewModel.
     * @throws IllegalArgumentException if `modelClass` is not assignable from [CoinFlipViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CoinFlipViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CoinFlipViewModel(application, launchLogRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}