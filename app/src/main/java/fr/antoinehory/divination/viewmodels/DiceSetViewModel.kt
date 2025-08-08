package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.data.database.AppDatabase
import fr.antoinehory.divination.data.database.dao.DiceSetDao
import fr.antoinehory.divination.data.model.DiceConfig
import fr.antoinehory.divination.data.model.DiceSet
import fr.antoinehory.divination.data.model.DiceType // AJOUT POTENTIEL D'IMPORT
import fr.antoinehory.divination.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull // AJOUT D'IMPORT
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiceSetViewModel(
    application: Application,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val diceSetDao: DiceSetDao = AppDatabase.getDatabase(application).diceSetDao()

    // ... (StateFlows existants) ...
    private val _diceSetToDeleteConfirm = MutableStateFlow<DiceSet?>(null)
    val diceSetToDeleteConfirm: StateFlow<DiceSet?> = _diceSetToDeleteConfirm.asStateFlow()

    private val _diceSetToCopyConfirm = MutableStateFlow<DiceSet?>(null)
    val diceSetToCopyConfirm: StateFlow<DiceSet?> = _diceSetToCopyConfirm.asStateFlow()

    private val _diceSetToSetActiveConfirm = MutableStateFlow<DiceSet?>(null)
    val diceSetToSetActiveConfirm: StateFlow<DiceSet?> = _diceSetToSetActiveConfirm.asStateFlow()


    val allDiceSets: StateFlow<List<DiceSet>> = diceSetDao.getAllDiceSets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteDiceSets: StateFlow<List<DiceSet>> = diceSetDao.getFavoriteDiceSets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // AJOUT: init block pour la gestion du set par défaut
    init {
        viewModelScope.launch {
            // Vérifier s'il y a déjà un set actif
            val activeSetId = userPreferencesRepository.activeDiceSetIdFlow.firstOrNull()
            if (activeSetId == null) {
                // S'il n'y a pas de set actif, vérifier si la base de données est vide
                // Utiliser allDiceSets.value est généralement sûr ici si le flow a déjà une valeur initiale
                // ou si le stateIn est configuré pour émettre immédiatement.
                // Pour plus de robustesse, on pourrait collecter la première valeur,
                // mais puisque allDiceSets est initialisé avec emptyList, sa valeur initiale est connue.
                if (allDiceSets.value.isEmpty()) {
                    // Si pas de set actif ET base de données vide, créer et activer le set par défaut
                    createAndActivateDefaultSet()
                }
            }
        }
    }

    // AJOUT: Fonction privée pour créer et activer le set par défaut
    private suspend fun createAndActivateDefaultSet() {
        val defaultDiceConfigs = listOf(DiceConfig(diceType = DiceType.D6, count = 1))
        val defaultSet = DiceSet(
            name = "Quick Roll", // Peut être mis dans strings.xml plus tard
            diceConfigs = defaultDiceConfigs,
            isFavorite = false // Ou true, selon votre préférence
        )
        // Insérer le set et obtenir son ID. Room retourne l'ID inséré.
        val newSetId = diceSetDao.insert(defaultSet)
        // Vérifier si l'insertion a réussi (Room retourne l'ID, >0 pour succès)
        // Si votre ID n'est pas auto-généré ou si insert ne retourne pas l'ID, adaptez.
        if (newSetId > 0L) {
            userPreferencesRepository.setActiveDiceSetId(newSetId)
            // Optionnel: Afficher un log ou un message Toast que le set par défaut a été créé.
            println("Default dice set 'Quick Roll' created and activated with ID: $newSetId")
        } else {
            println("Failed to create or activate the default dice set.")
        }
    }

    // ... (fonctions existantes: requestDeleteConfirmation, etc.) ...

    fun requestDeleteConfirmation(diceSet: DiceSet) {
        _diceSetToDeleteConfirm.value = diceSet
    }

    fun cancelDeleteConfirmation() {
        _diceSetToDeleteConfirm.value = null
    }

    fun requestCopyConfirmation(diceSet: DiceSet) {
        _diceSetToCopyConfirm.value = diceSet
    }

    fun cancelCopyConfirmation() {
        _diceSetToCopyConfirm.value = null
    }

    fun confirmAndCopyDiceSet(diceSetToCopy: DiceSet) {
        viewModelScope.launch {
            val newName = "${diceSetToCopy.name} (Copy)"
            val newSet = diceSetToCopy.copy(
                id = 0,
                name = newName,
                isFavorite = false
            )
            diceSetDao.insert(newSet)
            _diceSetToCopyConfirm.value = null
        }
    }

    fun requestSetActiveConfirmation(diceSet: DiceSet) {
        _diceSetToSetActiveConfirm.value = diceSet
    }

    fun cancelSetActiveConfirmation() {
        _diceSetToSetActiveConfirm.value = null
    }

    fun confirmSetActiveDiceSet(diceSetToActivate: DiceSet) {
        if (diceSetToActivate.id != 0L) {
            viewModelScope.launch {
                userPreferencesRepository.setActiveDiceSetId(diceSetToActivate.id)
                _diceSetToSetActiveConfirm.value = null
            }
        } else {
            println("Error: Cannot set DiceSet with ID 0 as active.")
            _diceSetToSetActiveConfirm.value = null
        }
    }

    fun addDiceSet(name: String, diceConfigs: List<DiceConfig>) {
        viewModelScope.launch {
            val newDiceSet = DiceSet(name = name, diceConfigs = diceConfigs, isFavorite = false)
            diceSetDao.insert(newDiceSet)
        }
    }

    fun updateDiceSet(diceSet: DiceSet) {
        viewModelScope.launch {
            diceSetDao.update(diceSet)
        }
    }

    fun deleteDiceSet(diceSet: DiceSet) {
        viewModelScope.launch {
            diceSetDao.delete(diceSet)
            if (_diceSetToDeleteConfirm.value?.id == diceSet.id) {
                _diceSetToDeleteConfirm.value = null
            }
        }
    }

    fun toggleFavoriteStatus(diceSet: DiceSet) {
        viewModelScope.launch {
            val updatedSet = diceSet.copy(isFavorite = !diceSet.isFavorite)
            diceSetDao.update(updatedSet)
        }
    }
}

// ... (DiceSetViewModelFactory reste inchangée) ...
class DiceSetViewModelFactory(
    private val application: Application,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiceSetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiceSetViewModel(application, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
