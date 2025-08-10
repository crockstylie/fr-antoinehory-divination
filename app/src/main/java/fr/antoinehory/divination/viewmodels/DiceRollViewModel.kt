package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.database.dao.DiceSetDao
import fr.antoinehory.divination.data.model.DiceSet
import fr.antoinehory.divination.data.model.DiceType
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.data.repository.LaunchLogRepository
import fr.antoinehory.divination.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlinx.coroutines.delay

/**
 * Represents the result of a single die roll within a dice set.
 *
 * @property diceType The type of the die that was rolled (e.g., D6, D20).
 * @property value The numerical result of the roll.
 * @property configIndex The index of the [DiceConfig] within the [DiceSet.diceConfigs] list
 *                       that this die roll belongs to.
 * @property rollIndex The index of this specific roll within a [DiceConfig] that specifies multiple dice of the same type.
 * @property isLocked Indicates whether this die's result is currently locked and should not be re-rolled.
 */
data class IndividualDiceRollResult(
    val diceType: DiceType,
    val value: Int,
    val configIndex: Int,
    val rollIndex: Int,
    val isLocked: Boolean = false
)

/**
 * ViewModel for the dice rolling screen.
 *
 * Manages the state of dice rolls, including the active dice set, individual dice results,
 * locking/unlocking dice, performing rolls, calculating total values, and logging results.
 * It interacts with [DiceSetDao] to fetch dice set details, [UserPreferencesRepository]
 * to get the active dice set, and [LaunchLogRepository] to save roll history.
 *
 * @param application The application context, used for accessing resources (e.g., strings).
 * @param launchLogRepository Repository for managing launch log data.
 * @param userPreferencesRepository Repository for managing user preferences, like active dice set.
 * @param diceSetDao DAO for accessing dice set data from the database.
 */
class DiceRollViewModel(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val diceSetDao: DiceSetDao
) : ViewModel() {

    /**
     * Internal [MutableStateFlow] for the message currently displayed to the user (e.g., prompt, error, result summary).
     */
    private val _currentMessage = MutableStateFlow(application.getString(R.string.dice_initial_prompt_generic))
    /**
     * Public [StateFlow] exposing the current message to be displayed on the UI.
     */
    val currentMessage: StateFlow<String> = _currentMessage.asStateFlow()

    /**
     * Internal [MutableStateFlow] indicating whether a dice roll is currently in progress.
     * `true` during the roll animation and processing, `false` otherwise.
     */
    private val _isRolling = MutableStateFlow(false)
    /**
     * Public [StateFlow] exposing the rolling state.
     */
    val isRolling: StateFlow<Boolean> = _isRolling.asStateFlow()

    /**
     * Internal [MutableStateFlow] for the currently active [DiceSet].
     * `null` if no dice set is active or found.
     */
    private val _activeDiceSet = MutableStateFlow<DiceSet?>(null)
    /**
     * Public [StateFlow] exposing the active [DiceSet].
     */
    val activeDiceSet: StateFlow<DiceSet?> = _activeDiceSet.asStateFlow()

    /**
     * Internal [MutableStateFlow] for the list of [IndividualDiceRollResult]s from the current roll or set.
     */
    private val _diceResults = MutableStateFlow<List<IndividualDiceRollResult>>(emptyList())
    /**
     * Public [StateFlow] exposing the current list of individual dice roll results.
     */
    val diceResults: StateFlow<List<IndividualDiceRollResult>> = _diceResults.asStateFlow()

    /**
     * A [StateFlow] that emits `true` if any of the dice in the current results are locked, `false` otherwise.
     * Useful for UI elements that depend on the presence of locked dice.
     */
    val hasLockedDice: StateFlow<Boolean> = _diceResults.map { results ->
        results.any { it.isLocked }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * Internal [MutableStateFlow] for the total sum of all dice rolled in the current set.
     * `null` if no dice have been rolled or results are cleared.
     */
    private val _totalRollValue = MutableStateFlow<Int?>(null)
    /**
     * Public [StateFlow] exposing the total value of the current dice roll.
     */
    val totalRollValue: StateFlow<Int?> = _totalRollValue.asStateFlow()

    /**
     * Internal [MutableStateFlow] for the list of recent dice roll logs.
     */
    private val _recentLogs = MutableStateFlow<List<fr.antoinehory.divination.data.database.entity.LaunchLog>>(emptyList())
    /**
     * Public [StateFlow] exposing the list of recent dice roll logs.
     */
    val recentLogs: StateFlow<List<fr.antoinehory.divination.data.database.entity.LaunchLog>> = _recentLogs.asStateFlow()

    /**
     * Initializes the ViewModel.
     * Loads recent dice roll logs and sets up a collector to observe changes in the active dice set ID
     * from [UserPreferencesRepository]. When the active set changes, it fetches the [DiceSet] details
     * from [DiceSetDao] and updates the UI state accordingly (e.g., clears previous results, updates prompts).
     */
    init {
        loadRecentLogs()
        viewModelScope.launch {
            userPreferencesRepository.activeDiceSetIdFlow.collectLatest { setId ->
                if (setId != null) {
                    diceSetDao.getDiceSetById(setId).collectLatest { set ->
                        _activeDiceSet.value = set
                        _diceResults.value = emptyList() // Clear results when set changes
                        _totalRollValue.value = null
                        if (set == null) {
                            _currentMessage.value = application.getString(R.string.error_active_set_not_found)
                        } else {
                            _currentMessage.value = application.getString(R.string.dice_prompt_with_set, set.name)
                        }
                    }
                } else {
                    _activeDiceSet.value = null
                    _diceResults.value = emptyList()
                    _totalRollValue.value = null
                    _currentMessage.value = application.getString(R.string.error_no_active_set)
                }
            }
        }
    }

    /**
     * Loads the most recent dice roll logs from the [LaunchLogRepository].
     * The number of logs fetched is limited (e.g., to 5).
     */
    private fun loadRecentLogs() {
        viewModelScope.launch {
            launchLogRepository.getRecentLogsByGameType(GameType.DICE_ROLL, 5).collect { logs ->
                _recentLogs.value = logs
            }
        }
    }

    /**
     * Toggles the lock state of a specific die in the current results.
     *
     * @param indexInResults The index of the [IndividualDiceRollResult] in the [_diceResults] list
     *                       whose lock state is to be toggled.
     */
    fun toggleLockState(indexInResults: Int) {
        if (indexInResults < 0 || indexInResults >= _diceResults.value.size) return

        val currentResults = _diceResults.value
        val resultToToggle = currentResults[indexInResults]
        val updatedResult = resultToToggle.copy(isLocked = !resultToToggle.isLocked)

        val newResultsList = currentResults.toMutableList()
        newResultsList[indexInResults] = updatedResult
        _diceResults.value = newResultsList
    }

    /**
     * Unlocks all currently locked dice in the results.
     * If no dice are locked, this function does nothing.
     */
    fun unlockAllDice() {
        if (_diceResults.value.any { it.isLocked }) {
            _diceResults.value = _diceResults.value.map { it.copy(isLocked = false) }
        }
    }

    /**
     * Performs a dice roll based on the active dice set or previous results.
     *
     * If an active set is not available or has no dice configurations (and no previous results exist),
     * an error message is displayed.
     *
     * The roll process involves:
     * 1. Setting the [isRolling] state to true.
     * 2. If re-rolling, non-locked dice are briefly shown as 0 for animation.
     * 3. If it's the first roll for the active set:
     *    - Generates new [IndividualDiceRollResult]s for each die in each [DiceConfig].
     * 4. If re-rolling existing results:
     *    - Locked dice retain their values.
     *    - Non-locked dice are re-rolled.
     * 5. Updates [_diceResults] and [_totalRollValue].
     * 6. Updates [_currentMessage] to display the results.
     * 7. Logs the roll details ([LaunchLogRepository.insertLog]).
     * 8. Reloads recent logs.
     * 9. Sets [isRolling] state back to false.
     *
     * Does nothing if a roll is already in progress.
     */
    fun performRoll() {
        if (_isRolling.value) return

        val activeSet = _activeDiceSet.value
        if (activeSet == null) {
            _currentMessage.value = application.getString(R.string.error_cannot_roll_no_set_or_config)
            return
        }
        // Also check if there are no results to re-roll from (e.g., after clearRoll)
        if (activeSet.diceConfigs.isEmpty() && _diceResults.value.isEmpty()) {
            _currentMessage.value = application.getString(R.string.error_cannot_roll_no_set_or_config)
            return
        }

        viewModelScope.launch {
            _isRolling.value = true
            val previousResults = _diceResults.value
            val newGeneratedResults = mutableListOf<IndividualDiceRollResult>()
            var newTotalSum = 0

            // Animation: Show non-locked dice as 0 briefly before showing new values
            val resultsForAnimation = previousResults.map { if (it.isLocked) it else it.copy(value = 0) }
            if (previousResults.isNotEmpty() && previousResults.any { !it.isLocked }) { // Only animate if there are non-locked dice to re-roll
                _diceResults.value = resultsForAnimation
                delay(200) // Brief delay for animation
            } else if (previousResults.isEmpty()) { // For the very first roll, just ensure a brief "rolling" state
                _diceResults.value = emptyList() // Show empty momentarily
                delay(200)
            }


            // Determine if this is a fresh roll from the set config or a re-roll of existing results
            if (previousResults.isEmpty()) { // Fresh roll based on activeSet.diceConfigs
                activeSet.diceConfigs.forEachIndexed { configIndex, diceConfig ->
                    repeat(diceConfig.count) { rollIndex ->
                        val rollValue = Random.nextInt(1, diceConfig.diceType.sides + 1)
                        newGeneratedResults.add(
                            IndividualDiceRollResult(
                                diceType = diceConfig.diceType,
                                value = rollValue,
                                configIndex = configIndex,
                                rollIndex = rollIndex,
                                isLocked = false // New dice are never locked initially
                            )
                        )
                        newTotalSum += rollValue
                    }
                }
            } else { // Re-roll based on previousResults (respecting locks)
                previousResults.forEach { result ->
                    if (result.isLocked) {
                        newGeneratedResults.add(result) // Keep locked dice as is
                        newTotalSum += result.value
                    } else {
                        val rollValue = Random.nextInt(1, result.diceType.sides + 1)
                        newGeneratedResults.add(
                            result.copy(value = rollValue, isLocked = false) // Re-roll non-locked dice
                        )
                        newTotalSum += rollValue
                    }
                }
            }

            _diceResults.value = newGeneratedResults
            _totalRollValue.value = newTotalSum
            _currentMessage.value = application.getString(R.string.results_for_set, activeSet.name)

            // Prepare log entry
            val logSummaryDetails = newGeneratedResults.joinToString {
                "${it.value}${if (it.isLocked) "(L)" else ""}/${it.diceType.sides}"
            }
            val logSummary = "Rolled ${activeSet.name}. Total: $newTotalSum.\nDetails: $logSummaryDetails"
            launchLogRepository.insertLog(GameType.DICE_ROLL, logSummary)
            loadRecentLogs() // Refresh recent logs list

            _isRolling.value = false
        }
    }

    /**
     * Clears the current dice roll results and total value.
     * Resets the [_currentMessage] to the appropriate prompt based on whether an active set is present.
     */
    fun clearRoll() {
        _diceResults.value = emptyList()
        _totalRollValue.value = null
        _currentMessage.value = _activeDiceSet.value?.let { application.getString(R.string.dice_prompt_with_set, it.name) }
            ?: application.getString(R.string.error_no_active_set)
    }
}

/**
 * Factory for creating instances of [DiceRollViewModel].
 *
 * This factory is necessary because [DiceRollViewModel] has multiple constructor dependencies
 * ([Application], [LaunchLogRepository], [UserPreferencesRepository], [DiceSetDao])
 * which need to be provided during ViewModel creation.
 *
 * @param application The application context.
 * @param launchLogRepository Repository for launch log data.
 * @param userPreferencesRepository Repository for user preferences.
 * @param diceSetDao DAO for dice set data.
 */
class DiceRollViewModelFactory(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val diceSetDao: DiceSetDao
) : ViewModelProvider.Factory {
    /**
     * Creates a new instance of the given `modelClass`.
     *
     * @param modelClass A class whose instance is requested.
     * @return A newly created ViewModel.
     * @throws IllegalArgumentException if `modelClass` is not assignable from [DiceRollViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiceRollViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiceRollViewModel(
                application,
                launchLogRepository,
                userPreferencesRepository,
                diceSetDao
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for DiceRollViewModelFactory")
    }
}
