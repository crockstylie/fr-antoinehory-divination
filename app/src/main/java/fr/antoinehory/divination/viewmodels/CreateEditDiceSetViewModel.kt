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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateEditDiceSetViewModel(
    application: Application,
    private val diceSetId: Long?
) : ViewModel() {

    private val diceSetDao: DiceSetDao = AppDatabase.getDatabase(application).diceSetDao()

    private val _setName = MutableStateFlow("")
    val setName: StateFlow<String> = _setName.asStateFlow()

    private val _diceConfigs = MutableStateFlow<List<DiceConfig>>(emptyList())
    val diceConfigs: StateFlow<List<DiceConfig>> = _diceConfigs.asStateFlow()

    private var isNewSet: Boolean = true
    private var currentDiceSet: DiceSet? = null

    init {
        if (diceSetId != null) {
            isNewSet = false
            loadDiceSet(diceSetId)
        }
    }

    private fun loadDiceSet(id: Long) {
        viewModelScope.launch {
            currentDiceSet = diceSetDao.getDiceSetByIdBlocking(id)
            currentDiceSet?.let { set ->
                _setName.value = set.name
                _diceConfigs.value = set.diceConfigs
            }
        }
    }

    fun updateSetName(newName: String) {
        _setName.value = newName
    }

    // AJOUT: Fonction pour ajouter une configuration de dé
    fun addDiceConfig(diceConfig: DiceConfig) {
        _diceConfigs.update { currentList ->
            // Pourrait ajouter une logique pour fusionner si le même type de dé est ajouté
            // ou simplement ajouter à la liste.
            currentList + diceConfig
        }
    }

    // AJOUT: Fonction pour supprimer une configuration de dé par son index
    fun removeDiceConfig(index: Int) {
        _diceConfigs.update { currentList ->
            if (index in currentList.indices) {
                currentList.toMutableList().apply { removeAt(index) }
            } else {
                currentList // Retourne la liste inchangée si l'index est invalide
            }
        }
    }

    // TODO: updateDiceConfig(index: Int, newConfig: DiceConfig) si besoin de modifier en place


    fun saveDiceSet(onSuccess: () -> Unit) {
        val name = _setName.value.trim()
        val configs = _diceConfigs.value

        if (name.isBlank()) {
            // TODO: Afficher une erreur à l'utilisateur (par exemple, via un StateFlow d'erreur)
            return
        }
        if (configs.isEmpty()) {
            // TODO: Afficher une erreur, un set doit avoir au moins un dé
            return
        }

        viewModelScope.launch {
            if (isNewSet || currentDiceSet == null) {
                val newSet = DiceSet(name = name, diceConfigs = configs, isFavorite = false)
                diceSetDao.insert(newSet)
            } else {
                val updatedSet = currentDiceSet!!.copy(
                    name = name,
                    diceConfigs = configs
                )
                diceSetDao.update(updatedSet)
            }
            onSuccess()
        }
    }
}

class CreateEditDiceSetViewModelFactory(
    private val application: Application,
    private val diceSetId: Long?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateEditDiceSetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateEditDiceSetViewModel(application, diceSetId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for CreateEditDiceSetViewModel")
    }
}

