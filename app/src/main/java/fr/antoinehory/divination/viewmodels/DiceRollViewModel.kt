package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.R
// import fr.antoinehory.divination.data.database.AppDatabase // Non directement utilisé
import fr.antoinehory.divination.data.database.dao.DiceSetDao
import fr.antoinehory.divination.data.model.DiceSet
import fr.antoinehory.divination.data.model.DiceType
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.data.repository.LaunchLogRepository
import fr.antoinehory.divination.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
// import java.util.Date // Non directement utilisé ici, implicite dans LaunchLogRepository
import kotlin.random.Random
import kotlinx.coroutines.delay // Pour l'effet de "roulement"

// MODIFIÉ: Ajout de isLocked
data class IndividualDiceRollResult(
    val diceType: DiceType,
    val value: Int,
    val configIndex: Int,
    val rollIndex: Int,
    val isLocked: Boolean = false // Nouveau champ pour l'état de verrouillage
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
                        _diceResults.value = emptyList() // Réinitialise les dés (et donc les verrous)
                        _totalRollValue.value = null
                        if (set == null) {
                            _currentMessage.value = application.getString(R.string.error_active_set_not_found)
                        } else {
                            _currentMessage.value = application.getString(R.string.dice_prompt_with_set, set.name)
                        }
                    }
                } else {
                    _activeDiceSet.value = null
                    _diceResults.value = emptyList() // Réinitialise
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

    // NOUVELLE FONCTION: Pour basculer l'état de verrouillage d'un dé
    fun toggleLockState(indexInResults: Int) {
        if (indexInResults < 0 || indexInResults >= _diceResults.value.size) return // Sécurité

        val currentResults = _diceResults.value
        val resultToToggle = currentResults[indexInResults]
        val updatedResult = resultToToggle.copy(isLocked = !resultToToggle.isLocked)

        val newResultsList = currentResults.toMutableList()
        newResultsList[indexInResults] = updatedResult
        _diceResults.value = newResultsList
        // Le total ne change pas ici, car les valeurs des dés sont les mêmes.
    }

    // MODIFIÉ: performRoll pour gérer les dés verrouillés
    fun performRoll() {
        if (_isRolling.value) return

        val activeSet = _activeDiceSet.value
        if (activeSet == null) {
            _currentMessage.value = application.getString(R.string.error_cannot_roll_no_set_or_config)
            return
        }
        // Si le set actif n'a pas de configuration de dés, on ne peut pas lancer.
        if (activeSet.diceConfigs.isEmpty() && _diceResults.value.isEmpty()) {
            _currentMessage.value = application.getString(R.string.error_cannot_roll_no_set_or_config)
            return
        }


        viewModelScope.launch {
            _isRolling.value = true

            val previousResults = _diceResults.value
            val newGeneratedResults = mutableListOf<IndividualDiceRollResult>()
            var newTotalSum = 0

            // Effet visuel de "roulement" pour les dés non verrouillés ou si c'est le premier lancer
            // On crée une version temporaire des résultats où les dés non verrouillés sont "vides"
            // ou on vide simplement la liste pour un court instant.
            val resultsForAnimation = previousResults.map { if (it.isLocked) it else it.copy(value = 0) } // Mettre 0 ou un placeholder
            if (previousResults.isNotEmpty() && previousResults.any { !it.isLocked }) {
                // Animation: on ne vide que si des dés sont relancés
                _diceResults.value = resultsForAnimation // Affiche les dés verrouillés + "blancs" pour les autres
                delay(200) // Court délai pour voir les dés "en attente"
            } else if (previousResults.isEmpty()) {
                _diceResults.value = emptyList() // Premier lancer, tout est vide au début de l'anim
                delay(200)
            }


            if (previousResults.isEmpty()) { // Premier lancer pour ce set actif
                activeSet.diceConfigs.forEachIndexed { configIndex, diceConfig ->
                    repeat(diceConfig.count) { rollIndex ->
                        val rollValue = Random.nextInt(1, diceConfig.diceType.sides + 1)
                        newGeneratedResults.add(
                            IndividualDiceRollResult(
                                diceType = diceConfig.diceType,
                                value = rollValue,
                                configIndex = configIndex,
                                rollIndex = rollIndex,
                                isLocked = false // Initialement non verrouillé
                            )
                        )
                        newTotalSum += rollValue
                    }
                }
            } else { // C'est un re-lancer, on respecte les dés verrouillés
                previousResults.forEach { result ->
                    if (result.isLocked) {
                        newGeneratedResults.add(result) // Conserver le dé verrouillé tel quel
                        newTotalSum += result.value
                    } else {
                        val rollValue = Random.nextInt(1, result.diceType.sides + 1)
                        newGeneratedResults.add(
                            result.copy(value = rollValue, isLocked = false) // Relancer, s'assurer que isLocked est false (même si c'était déjà le cas)
                        )
                        newTotalSum += rollValue
                    }
                }
            }

            // Appliquer les vrais résultats après le calcul (et l'animation potentielle)
            _diceResults.value = newGeneratedResults
            _totalRollValue.value = newTotalSum
            _currentMessage.value = application.getString(R.string.results_for_set, activeSet.name)

            val logSummaryDetails = newGeneratedResults.joinToString {
                "${it.value}${if (it.isLocked) "(L)" else ""}/${it.diceType.sides}"
            }
            val logSummary = "Rolled ${activeSet.name}. Total: $newTotalSum. Details: $logSummaryDetails"
            launchLogRepository.insertLog(GameType.DICE_ROLL, logSummary)
            loadRecentLogs()

            _isRolling.value = false
        }
    }

    fun clearRoll() {
        _diceResults.value = emptyList() // Réinitialise les dés et donc les verrous
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
