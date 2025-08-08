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

data class IndividualDiceRollResult(
    val diceType: DiceType,
    val value: Int,
    val configIndex: Int,
    val rollIndex: Int,
    val isLocked: Boolean = false
)

class DiceRollViewModel(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val diceSetDao: DiceSetDao
) : ViewModel() {

    private val _currentMessage = MutableStateFlow(application.getString(R.string.dice_initial_prompt_generic))
    val currentMessage: StateFlow<String> = _currentMessage.asStateFlow()

    private val _isRolling = MutableStateFlow(false)
    val isRolling: StateFlow<Boolean> = _isRolling.asStateFlow()

    private val _activeDiceSet = MutableStateFlow<DiceSet?>(null)
    val activeDiceSet: StateFlow<DiceSet?> = _activeDiceSet.asStateFlow()

    private val _diceResults = MutableStateFlow<List<IndividualDiceRollResult>>(emptyList())
    val diceResults: StateFlow<List<IndividualDiceRollResult>> = _diceResults.asStateFlow()

    val hasLockedDice: StateFlow<Boolean> = _diceResults.map { results ->
        results.any { it.isLocked }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _totalRollValue = MutableStateFlow<Int?>(null)
    val totalRollValue: StateFlow<Int?> = _totalRollValue.asStateFlow()

    private val _recentLogs = MutableStateFlow<List<fr.antoinehory.divination.data.database.entity.LaunchLog>>(emptyList())
    val recentLogs: StateFlow<List<fr.antoinehory.divination.data.database.entity.LaunchLog>> = _recentLogs.asStateFlow()

    init {
        loadRecentLogs()
        viewModelScope.launch {
            userPreferencesRepository.activeDiceSetIdFlow.collectLatest { setId ->
                if (setId != null) {
                    diceSetDao.getDiceSetById(setId).collectLatest { set ->
                        _activeDiceSet.value = set
                        _diceResults.value = emptyList()
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

    private fun loadRecentLogs() {
        viewModelScope.launch {
            launchLogRepository.getRecentLogsByGameType(GameType.DICE_ROLL, 5).collect { logs ->
                _recentLogs.value = logs
            }
        }
    }

    fun toggleLockState(indexInResults: Int) {
        if (indexInResults < 0 || indexInResults >= _diceResults.value.size) return

        val currentResults = _diceResults.value
        val resultToToggle = currentResults[indexInResults]
        val updatedResult = resultToToggle.copy(isLocked = !resultToToggle.isLocked)

        val newResultsList = currentResults.toMutableList()
        newResultsList[indexInResults] = updatedResult
        _diceResults.value = newResultsList
    }

    fun unlockAllDice() {
        if (_diceResults.value.any { it.isLocked }) {
            _diceResults.value = _diceResults.value.map { it.copy(isLocked = false) }
        }
    }

    fun performRoll() {
        if (_isRolling.value) return

        val activeSet = _activeDiceSet.value
        if (activeSet == null) {
            _currentMessage.value = application.getString(R.string.error_cannot_roll_no_set_or_config)
            return
        }
        if (activeSet.diceConfigs.isEmpty() && _diceResults.value.isEmpty()) {
            _currentMessage.value = application.getString(R.string.error_cannot_roll_no_set_or_config)
            return
        }

        viewModelScope.launch {
            _isRolling.value = true
            val previousResults = _diceResults.value
            val newGeneratedResults = mutableListOf<IndividualDiceRollResult>()
            var newTotalSum = 0

            val resultsForAnimation = previousResults.map { if (it.isLocked) it else it.copy(value = 0) }
            if (previousResults.isNotEmpty() && previousResults.any { !it.isLocked }) {
                _diceResults.value = resultsForAnimation
                delay(200)
            } else if (previousResults.isEmpty()) {
                _diceResults.value = emptyList()
                delay(200)
            }

            if (previousResults.isEmpty()) {
                activeSet.diceConfigs.forEachIndexed { configIndex, diceConfig ->
                    repeat(diceConfig.count) { rollIndex ->
                        val rollValue = Random.nextInt(1, diceConfig.diceType.sides + 1)
                        newGeneratedResults.add(
                            IndividualDiceRollResult(
                                diceType = diceConfig.diceType,
                                value = rollValue,
                                configIndex = configIndex,
                                rollIndex = rollIndex,
                                isLocked = false
                            )
                        )
                        newTotalSum += rollValue
                    }
                }
            } else {
                previousResults.forEach { result ->
                    if (result.isLocked) {
                        newGeneratedResults.add(result)
                        newTotalSum += result.value
                    } else {
                        val rollValue = Random.nextInt(1, result.diceType.sides + 1)
                        newGeneratedResults.add(
                            result.copy(value = rollValue, isLocked = false)
                        )
                        newTotalSum += rollValue
                    }
                }
            }

            _diceResults.value = newGeneratedResults
            _totalRollValue.value = newTotalSum
            _currentMessage.value = application.getString(R.string.results_for_set, activeSet.name)

            val logSummaryDetails = newGeneratedResults.joinToString {
                // Pourrait être juste "${it.value}" si /${it.diceType.sides} est trop verbeux ici
                "${it.value}${if (it.isLocked) "(L)" else ""}/${it.diceType.sides}"
            }
            // MODIFIÉ ICI pour ajouter le saut de ligne
            val logSummary = "Rolled ${activeSet.name}. Total: $newTotalSum.\nDetails: $logSummaryDetails"
            launchLogRepository.insertLog(GameType.DICE_ROLL, logSummary)
            loadRecentLogs()

            _isRolling.value = false
        }
    }

    fun clearRoll() {
        _diceResults.value = emptyList()
        _totalRollValue.value = null
        _currentMessage.value = _activeDiceSet.value?.let { application.getString(R.string.dice_prompt_with_set, it.name) }
            ?: application.getString(R.string.error_no_active_set)
    }
}

class DiceRollViewModelFactory(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val diceSetDao: DiceSetDao
) : ViewModelProvider.Factory {
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