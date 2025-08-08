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
    private val diceSetId: Long? // L'ID est un Long, null pour la création
) : ViewModel() {

    private val diceSetDao: DiceSetDao = AppDatabase.getDatabase(application).diceSetDao()

    // État pour le nom du set de dés
    private val _setName = MutableStateFlow("")
    val setName: StateFlow<String> = _setName.asStateFlow()

    // État pour la liste des configurations de dés
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
            currentDiceSet = diceSetDao.getDiceSetByIdBlocking(id) // Note: Room retourne Flow, besoin d'une version bloquante ou collecter
            currentDiceSet?.let { set ->
                _setName.value = set.name
                _diceConfigs.value = set.diceConfigs
            }
        }
    }

    fun updateSetName(newName: String) {
        _setName.value = newName
    }

    // TODO: Fonctions pour ajouter, modifier, supprimer DiceConfig
    // fun addDiceConfig(diceConfig: DiceConfig) { ... }
    // fun removeDiceConfig(index: Int) { ... }
    // fun updateDiceConfig(index: Int, newConfig: DiceConfig) { ... }


    fun saveDiceSet(onSuccess: () -> Unit) {
        val name = _setName.value.trim()
        val configs = _diceConfigs.value

        if (name.isBlank()) {
            // Gérer l'erreur : nom vide (par exemple, afficher un message à l'utilisateur)
            // Pour l'instant, on ne fait rien pour ne pas crasher.
            return
        }
        // TODO: Ajouter une validation pour s'assurer qu'il y a au moins un dé.

        viewModelScope.launch {
            if (isNewSet || currentDiceSet == null) {
                val newSet = DiceSet(name = name, diceConfigs = configs, isFavorite = false) // Par défaut non favori à la création
                diceSetDao.insert(newSet)
            } else {
                // Mise à jour d'un set existant
                val updatedSet = currentDiceSet!!.copy(
                    name = name,
                    diceConfigs = configs
                    // isFavorite n'est pas modifié ici, seulement nom et configs
                )
                diceSetDao.update(updatedSet)
            }
            onSuccess() // Appeler le callback en cas de succès
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