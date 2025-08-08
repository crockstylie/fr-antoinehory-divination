package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.data.database.AppDatabase
import fr.antoinehory.divination.data.database.dao.DiceSetDao
import fr.antoinehory.divination.data.model.DiceConfig
import fr.antoinehory.divination.data.model.DiceSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiceSetViewModel(application: Application) : ViewModel() {

    private val diceSetDao: DiceSetDao = AppDatabase.getDatabase(application).diceSetDao()

    // State pour la confirmation de suppression
    private val _diceSetToDeleteConfirm = MutableStateFlow<DiceSet?>(null)
    val diceSetToDeleteConfirm: StateFlow<DiceSet?> = _diceSetToDeleteConfirm.asStateFlow()

    // State pour la confirmation de copie
    private val _diceSetToCopyConfirm = MutableStateFlow<DiceSet?>(null)
    val diceSetToCopyConfirm: StateFlow<DiceSet?> = _diceSetToCopyConfirm.asStateFlow()

    // Exposer la liste de tous les sets de dés
    val allDiceSets: StateFlow<List<DiceSet>> = diceSetDao.getAllDiceSets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Exposer la liste des sets de dés favoris
    val favoriteDiceSets: StateFlow<List<DiceSet>> = diceSetDao.getFavoriteDiceSets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Demande la confirmation avant de supprimer un set de dés.
     */
    fun requestDeleteConfirmation(diceSet: DiceSet) {
        _diceSetToDeleteConfirm.value = diceSet
    }

    /**
     * Annule la demande de confirmation de suppression.
     */
    fun cancelDeleteConfirmation() {
        _diceSetToDeleteConfirm.value = null
    }

    /**
     * Demande la confirmation avant de copier un set de dés.
     */
    fun requestCopyConfirmation(diceSet: DiceSet) {
        _diceSetToCopyConfirm.value = diceSet
    }

    /**
     * Annule la demande de confirmation de copie.
     */
    fun cancelCopyConfirmation() {
        _diceSetToCopyConfirm.value = null
    }

    /**
     * Copie le set de dés après confirmation.
     */
    fun confirmAndCopyDiceSet(diceSetToCopy: DiceSet) {
        viewModelScope.launch {
            // Le nom sera par exemple "NomDuSet (Copy)"
            val newName = "${diceSetToCopy.name} (Copy)"

            val newSet = diceSetToCopy.copy(
                id = 0, // Important pour que Room génère un nouvel ID
                name = newName,
                isFavorite = false // Une copie n'est pas favorite par défaut (modifiable selon besoin)
            )
            diceSetDao.insert(newSet)
            _diceSetToCopyConfirm.value = null // Réinitialiser l'état du dialogue
        }
    }

    /**
     * Ajoute un nouveau set de dés à la base de données.
     */
    fun addDiceSet(name: String, diceConfigs: List<DiceConfig>) {
        viewModelScope.launch {
            val newDiceSet = DiceSet(name = name, diceConfigs = diceConfigs, isFavorite = false)
            diceSetDao.insert(newDiceSet)
        }
    }

    /**
     * Met à jour un set de dés existant.
     */
    fun updateDiceSet(diceSet: DiceSet) {
        viewModelScope.launch {
            diceSetDao.update(diceSet)
        }
    }

    /**
     * Supprime un set de dés après confirmation.
     */
    fun deleteDiceSet(diceSet: DiceSet) {
        viewModelScope.launch {
            diceSetDao.delete(diceSet)
            if (_diceSetToDeleteConfirm.value?.id == diceSet.id) {
                _diceSetToDeleteConfirm.value = null
            }
        }
    }

    /**
     * Bascule le statut de favori d'un set de dés.
     */
    fun toggleFavoriteStatus(diceSet: DiceSet) {
        viewModelScope.launch {
            val updatedSet = diceSet.copy(isFavorite = !diceSet.isFavorite)
            diceSetDao.update(updatedSet)
        }
    }
}

/**
 * Factory pour créer une instance de DiceSetViewModel.
 */
class DiceSetViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiceSetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiceSetViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}