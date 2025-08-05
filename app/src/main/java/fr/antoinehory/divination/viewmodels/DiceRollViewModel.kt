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

class DiceRollViewModel(
    private val application: Application, // Gardé comme private val
    private val launchLogRepository: LaunchLogRepository // AJOUT: Repository
) : AndroidViewModel(application) {

    // private val app: Application = application // 'app' est redondant car 'application' est déjà un membre de la classe

    private val _currentMessage = MutableStateFlow("")
    val currentMessage: StateFlow<String> = _currentMessage.asStateFlow()

    private val _diceValue = MutableStateFlow<Int?>(null)
    val diceValue: StateFlow<Int?> = _diceValue.asStateFlow()

    private val _isRolling = MutableStateFlow(false)
    val isRolling: StateFlow<Boolean> = _isRolling.asStateFlow()

    companion object {
        private const val ROLLING_DELAY_MS = 700L
        private const val DICE_SIDES = 6
    }

    init {
        initializeDiceState(application.getString(R.string.dice_initial_prompt_generic))
    }

    private fun initializeDiceState(initialMessage: String) {
        _diceValue.value = null
        _currentMessage.value = initialMessage
        _isRolling.value = false
    }

    private fun determineRollOutcome(): Int { // MODIFIÉ: pour retourner la valeur du dé
        val randomDiceValue = Random.nextInt(1, DICE_SIDES + 1)
        _diceValue.value = randomDiceValue
        _currentMessage.value = application.getString(R.string.dice_result_format, randomDiceValue)
        return randomDiceValue // Retourne la valeur pour le log
    }

    fun performRoll() {
        if (_isRolling.value) return

        viewModelScope.launch {
            _isRolling.value = true
            _currentMessage.value = application.getString(R.string.dice_rolling_message)
            _diceValue.value = null

            delay(ROLLING_DELAY_MS)

            val rolledValue = determineRollOutcome() // Récupère la valeur du dé
            // Enregistre le log
            launchLogRepository.insertLog(GameType.DICE_ROLL, rolledValue.toString())
            
            _isRolling.value = false
        }
    }

    fun resetGameToGenericPrompt() {
        initializeDiceState(application.getString(R.string.dice_initial_prompt_generic))
    }
}

// AJOUT: Factory pour DiceRollViewModel
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

