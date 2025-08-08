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
// AJOUT DE L'IMPORT POUR LaunchLog
import fr.antoinehory.divination.data.database.entity.LaunchLog

class DiceRollViewModel(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository
) : AndroidViewModel(application) {

    private val _currentMessage = MutableStateFlow("")
    val currentMessage: StateFlow<String> = _currentMessage.asStateFlow()

    private val _diceValue = MutableStateFlow<Int?>(null)
    val diceValue: StateFlow<Int?> = _diceValue.asStateFlow()

    private val _isRolling = MutableStateFlow(false)
    val isRolling: StateFlow<Boolean> = _isRolling.asStateFlow()

    // AJOUT : StateFlow pour les lancers récents
    private val _recentLogs = MutableStateFlow<List<LaunchLog>>(emptyList())
    val recentLogs: StateFlow<List<LaunchLog>> = _recentLogs.asStateFlow()

    companion object {
        private const val ROLLING_DELAY_MS = 700L
        private const val DICE_SIDES = 6
    }

    init {
        initializeDiceState(application.getString(R.string.dice_initial_prompt_generic))
        // AJOUT : Collecter les lancers récents
        viewModelScope.launch {
            launchLogRepository.getRecentLogsByGameType(GameType.DICE_ROLL, 10)
                .collect { logs ->
                    _recentLogs.value = logs
                }
        }
    }

    private fun initializeDiceState(initialMessage: String) {
        _diceValue.value = null
        _currentMessage.value = initialMessage
        _isRolling.value = false
    }

    private fun determineRollOutcome(): Int {
        val randomDiceValue = Random.nextInt(1, DICE_SIDES + 1)
        _diceValue.value = randomDiceValue
        _currentMessage.value = application.getString(R.string.dice_result_format, randomDiceValue)
        return randomDiceValue
    }

    fun performRoll() {
        if (_isRolling.value) return

        viewModelScope.launch {
            _isRolling.value = true
            _currentMessage.value = application.getString(R.string.dice_rolling_message)
            _diceValue.value = null

            delay(ROLLING_DELAY_MS)

            val rolledValue = determineRollOutcome()
            launchLogRepository.insertLog(GameType.DICE_ROLL, rolledValue.toString())

            _isRolling.value = false
        }
    }

    fun resetGameToGenericPrompt() {
        initializeDiceState(application.getString(R.string.dice_initial_prompt_generic))
    }
}

class DiceRollViewModelFactory(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiceRollViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiceRollViewModel(application, launchLogRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

