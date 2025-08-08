package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.database.AppDatabase
import fr.antoinehory.divination.data.database.dao.DiceSetDao
import fr.antoinehory.divination.data.model.DiceConfig
import fr.antoinehory.divination.data.model.DiceSet
import fr.antoinehory.divination.data.model.DiceType // Ajout de l'importation manquante
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateEditDiceSetViewModel(
    private val application: Application,
    private val diceSetId: Long?
) : ViewModel() {

    private val diceSetDao: DiceSetDao = AppDatabase.getDatabase(application).diceSetDao()

    private val _setName = MutableStateFlow("")
    val setName: StateFlow<String> = _setName.asStateFlow()

    private val _diceConfigs = MutableStateFlow<List<DiceConfig>>(emptyList())
    val diceConfigs: StateFlow<List<DiceConfig>> = _diceConfigs.asStateFlow()

    private val _saveError = MutableStateFlow<Int?>(null)
    val saveError: StateFlow<Int?> = _saveError.asStateFlow()

    private var isNewSet: Boolean = true
    private var currentDiceSet: DiceSet? = null

    init {
        if (diceSetId != null) {
            isNewSet = false
            loadDiceSet(diceSetId)
        } else {
            isNewSet = true
            _setName.value = application.getString(R.string.default_new_dice_set_name)
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
        if (newName.isNotBlank()) {
            if (_saveError.value == R.string.error_set_name_empty) {
                clearSaveError()
            }
        }
    }

    fun addDiceConfig(newDiceConfig: DiceConfig) {
        _diceConfigs.update { currentList ->
            val existingConfigIndex = currentList.indexOfFirst { it.diceType == newDiceConfig.diceType }

            if (existingConfigIndex != -1 && !isNewSet) { // Si c'est un set existant, on fusionne
                val existingConfig = currentList[existingConfigIndex]
                val updatedConfig = existingConfig.copy(
                    count = existingConfig.count + newDiceConfig.count
                )
                currentList.toMutableList().apply {
                    this[existingConfigIndex] = updatedConfig
                }
            } else { // Si nouveau set, ou type de dé différent, on ajoute
                currentList + newDiceConfig
            }
        }
        if (_saveError.value == R.string.error_no_dice_configs) {
            clearSaveError()
        }
    }

    fun removeDiceConfig(index: Int) {
        _diceConfigs.update { currentList ->
            if (index in currentList.indices) {
                currentList.toMutableList().apply { removeAt(index) }
            } else {
                currentList
            }
        }
    }

    fun incrementDiceCount(index: Int) {
        _diceConfigs.update { currentList ->
            if (index in currentList.indices) {
                val configToUpdate = currentList[index]
                val updatedConfig = configToUpdate.copy(count = configToUpdate.count + 1)
                currentList.toMutableList().apply { this[index] = updatedConfig }
            } else {
                currentList
            }
        }
    }

    fun decrementDiceCount(index: Int) {
        _diceConfigs.update { currentList ->
            if (index in currentList.indices) {
                val configToUpdate = currentList[index]
                if (configToUpdate.count > 1) {
                    val updatedConfig = configToUpdate.copy(count = configToUpdate.count - 1)
                    currentList.toMutableList().apply { this[index] = updatedConfig }
                } else {
                    currentList
                }
            } else {
                currentList
            }
        }
    }

    // NOUVELLE FONCTION pour mettre à jour type ET quantité
    fun updateDiceConfig(index: Int, newType: DiceType, newCount: Int) {
        _diceConfigs.update { currentList ->
            if (index in currentList.indices && newCount > 0) {
                // Vérifier si un autre item avec le nouveau type existe déjà (hors de l'item en cours d'édition)
                val otherItemWithNewTypeIndex = currentList.indexOfFirst {
                    it.diceType == newType && currentList.indexOf(it) != index
                }

                if (otherItemWithNewTypeIndex != -1) {
                    // Un autre item avec ce type existe. On va fusionner avec lui et supprimer l'item en cours d'édition.
                    val itemToMergeWith = currentList[otherItemWithNewTypeIndex]
                    val mergedConfig = itemToMergeWith.copy(count = itemToMergeWith.count + newCount)

                    currentList.toMutableList().apply {
                        this[otherItemWithNewTypeIndex] = mergedConfig
                        removeAt(index) // Supprimer l'item original qui était en cours d'édition
                    }
                } else {
                    // Aucun autre item avec ce type, ou l'item actuel est le seul (ou devient le seul).
                    // On met simplement à jour l'item en cours d'édition.
                    val configToUpdate = currentList[index]
                    val updatedConfig = configToUpdate.copy(diceType = newType, count = newCount)
                    currentList.toMutableList().apply { this[index] = updatedConfig }
                }
            } else {
                currentList
            }
        }
    }


    fun saveDiceSet(onSuccess: () -> Unit) {
        val name = _setName.value.trim()
        val configs = _diceConfigs.value

        if (name.isBlank()) {
            _saveError.value = R.string.error_set_name_empty
            return
        }
        if (configs.isEmpty()) {
            _saveError.value = R.string.error_no_dice_configs
            return
        }

        _saveError.value = null

        viewModelScope.launch {
            val nameToSave = if (name == application.getString(R.string.default_new_dice_set_name) && isNewSet) {
                name
            } else {
                name
            }

            if (isNewSet || currentDiceSet == null) {
                val newSet = DiceSet(name = nameToSave, diceConfigs = configs, isFavorite = false) // Assurer que isFavorite est false par défaut
                diceSetDao.insert(newSet)
            } else {
                val updatedSet = currentDiceSet!!.copy(
                    name = nameToSave,
                    diceConfigs = configs
                    // isFavorite est conservé de currentDiceSet
                )
                diceSetDao.update(updatedSet)
            }
            onSuccess()
        }
    }

    fun clearSaveError() {
        _saveError.value = null
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