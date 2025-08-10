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
import fr.antoinehory.divination.data.model.DiceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the screen used to create or edit a [DiceSet].
 *
 * This ViewModel manages the state of the dice set being created or edited,
 * including its name and the list of [DiceConfig]s. It handles loading an
 * existing dice set if an ID is provided, and saving the changes (either as a new
 * set or an update to an existing one). It also provides validation for saving.
 *
 * @param application The application context, used for accessing resources (e.g., strings) and the database.
 * @param diceSetId The ID of the [DiceSet] to be edited. If `null`, a new dice set is being created.
 */
class CreateEditDiceSetViewModel(
    private val application: Application,
    private val diceSetId: Long?
) : ViewModel() {

    private val diceSetDao: DiceSetDao = AppDatabase.getDatabase(application).diceSetDao()

    /**
     * Internal [MutableStateFlow] for the name of the dice set.
     */
    private val _setName = MutableStateFlow("")
    /**
     * Public [StateFlow] exposing the current name of the dice set.
     */
    val setName: StateFlow<String> = _setName.asStateFlow()

    /**
     * Internal [MutableStateFlow] for the list of [DiceConfig]s in the dice set.
     */
    private val _diceConfigs = MutableStateFlow<List<DiceConfig>>(emptyList())
    /**
     * Public [StateFlow] exposing the current list of dice configurations.
     */
    val diceConfigs: StateFlow<List<DiceConfig>> = _diceConfigs.asStateFlow()

    /**
     * Internal [MutableStateFlow] for holding a resource ID of a save error message.
     * `null` if there is no current save error.
     */
    private val _saveError = MutableStateFlow<Int?>(null)
    /**
     * Public [StateFlow] exposing the current save error message resource ID.
     * UI can observe this to display validation errors.
     */
    val saveError: StateFlow<Int?> = _saveError.asStateFlow()

    private var isNewSet: Boolean = true
    private var currentDiceSet: DiceSet? = null

    /**
     * Initializes the ViewModel.
     * If [diceSetId] is provided, it loads the existing dice set for editing.
     * Otherwise, it initializes the state for creating a new dice set, including a default name.
     */
    init {
        if (diceSetId != null) {
            isNewSet = false
            loadDiceSet(diceSetId)
        } else {
            isNewSet = true
            // Provide a default name for new sets, which can be localized.
            _setName.value = application.getString(R.string.default_new_dice_set_name)
        }
    }

    /**
     * Loads the details of an existing [DiceSet] from the database using its ID.
     * Updates [_setName] and [_diceConfigs] with the loaded data.
     * @param id The ID of the dice set to load.
     */
    private fun loadDiceSet(id: Long) {
        viewModelScope.launch {
            currentDiceSet = diceSetDao.getDiceSetByIdBlocking(id) // Consider non-blocking if UI can handle loading state
            currentDiceSet?.let { set ->
                _setName.value = set.name
                _diceConfigs.value = set.diceConfigs
            }
        }
    }

    /**
     * Updates the name of the dice set.
     * If the new name is not blank and a "name empty" error was previously shown, it clears the error.
     * @param newName The new name for the dice set.
     */
    fun updateSetName(newName: String) {
        _setName.value = newName
        // Clear name-related error if user starts typing a valid name
        if (newName.isNotBlank()) {
            if (_saveError.value == R.string.error_set_name_empty) {
                clearSaveError()
            }
        }
    }

    /**
     * Adds a new [DiceConfig] to the current list of dice configurations.
     *
     * If a [DiceConfig] with the same [DiceType] already exists, its count is incremented
     * by the count of the `newDiceConfig`. Otherwise, the `newDiceConfig` is added to the list.
     * If an "no dice configs" error was previously shown, it clears the error.
     *
     * @param newDiceConfig The [DiceConfig] to add or merge.
     */
    fun addDiceConfig(newDiceConfig: DiceConfig) {
        _diceConfigs.update { currentList ->
            val existingConfigIndex = currentList.indexOfFirst { it.diceType == newDiceConfig.diceType }

            if (existingConfigIndex != -1) {
                // If config for this dice type already exists, update its count
                currentList.map { existingConfig ->
                    if (existingConfig.diceType == newDiceConfig.diceType) {
                        existingConfig.copy(count = existingConfig.count + newDiceConfig.count)
                    } else {
                        existingConfig
                    }
                }
            } else {
                // Otherwise, add the new config to the list
                currentList + newDiceConfig
            }
        }
        // Clear "no dice" error if user adds a dice
        if (_saveError.value == R.string.error_no_dice_configs) {
            clearSaveError()
        }
    }

    /**
     * Removes a [DiceConfig] from the list at the specified index.
     * Does nothing if the index is out of bounds.
     * @param index The index of the [DiceConfig] to remove.
     */
    fun removeDiceConfig(index: Int) {
        _diceConfigs.update { currentList ->
            if (index in currentList.indices) {
                currentList.toMutableList().apply { removeAt(index) }
            } else {
                currentList // Return unmodified list if index is invalid
            }
        }
    }

    /**
     * Increments the count of dice in the [DiceConfig] at the specified index.
     * Does nothing if the index is out of bounds.
     * @param index The index of the [DiceConfig] whose dice count is to be incremented.
     */
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

    /**
     * Decrements the count of dice in the [DiceConfig] at the specified index.
     * The count will not go below 1. Does nothing if the index is out of bounds or count is already 1.
     * @param index The index of the [DiceConfig] whose dice count is to be decremented.
     */
    fun decrementDiceCount(index: Int) {
        _diceConfigs.update { currentList ->
            if (index in currentList.indices) {
                val configToUpdate = currentList[index]
                if (configToUpdate.count > 1) { // Prevent count from going below 1
                    val updatedConfig = configToUpdate.copy(count = configToUpdate.count - 1)
                    currentList.toMutableList().apply { this[index] = updatedConfig }
                } else {
                    currentList // Return unmodified if count is already 1
                }
            } else {
                currentList
            }
        }
    }

    /**
     * Updates an existing [DiceConfig] at the given index with a new type and count.
     *
     * If another [DiceConfig] already exists with the `newType` (and it's not the one
     * being currently edited at `index`), this method merges the counts of the updated config
     * into the existing one and removes the config at `index`.
     * Otherwise, it updates the [DiceConfig] at `index` with `newType` and `newCount`.
     * The `newCount` must be greater than 0.
     *
     * @param index The index of the [DiceConfig] to update.
     * @param newType The new [DiceType] for the configuration.
     * @param newCount The new count of dice (must be > 0).
     */
    fun updateDiceConfig(index: Int, newType: DiceType, newCount: Int) {
        _diceConfigs.update { currentList ->
            if (index in currentList.indices && newCount > 0) {
                // Check if another item with the newType already exists
                val otherItemWithNewTypeIndex = currentList.indexOfFirst {
                    it.diceType == newType && currentList.indexOf(it) != index
                }

                if (otherItemWithNewTypeIndex != -1) {
                    // Merge with the existing item of the same newType
                    val itemToMergeWith = currentList[otherItemWithNewTypeIndex]
                    val mergedConfig = itemToMergeWith.copy(count = itemToMergeWith.count + newCount)

                    currentList.toMutableList().apply {
                        this[otherItemWithNewTypeIndex] = mergedConfig
                        removeAt(index) // Remove the original item that was edited
                    }
                } else {
                    // No other item with this type, just update the current one
                    val configToUpdate = currentList[index]
                    val updatedConfig = configToUpdate.copy(diceType = newType, count = newCount)
                    currentList.toMutableList().apply { this[index] = updatedConfig }
                }
            } else {
                currentList // Return unmodified list if index invalid or newCount <= 0
            }
        }
    }


    /**
     * Attempts to save the current dice set configuration (either as a new set or an update).
     *
     * Performs validation:
     * - The set name must not be blank.
     * - The set must contain at least one dice configuration.
     * If validation fails, [_saveError] is updated with the appropriate error message.
     *
     * On successful validation, it either inserts a new [DiceSet] or updates the [currentDiceSet]
     * in the database. After a successful save, the [onSuccess] callback is invoked.
     *
     * @param onSuccess A callback function to be executed after the dice set is successfully saved.
     */
    fun saveDiceSet(onSuccess: () -> Unit) {
        val name = _setName.value.trim()
        val configs = _diceConfigs.value

        // Validation
        if (name.isBlank()) {
            _saveError.value = R.string.error_set_name_empty
            return
        }
        if (configs.isEmpty()) {
            _saveError.value = R.string.error_no_dice_configs
            return
        }

        _saveError.value = null // Clear any previous errors

        viewModelScope.launch {
            // Use the entered name. If it's the default name for a new set, that's fine.
            // No special handling needed here for default name as per previous logic.
            val nameToSave = name

            if (isNewSet || currentDiceSet == null) {
                val newSet = DiceSet(name = nameToSave, diceConfigs = configs, isFavorite = false)
                diceSetDao.insert(newSet)
            } else {
                // Update existing set
                val updatedSet = currentDiceSet!!.copy(
                    name = nameToSave,
                    diceConfigs = configs
                    // isFavorite status is preserved from currentDiceSet
                )
                diceSetDao.update(updatedSet)
            }
            onSuccess() // Call success callback
        }
    }

    /**
     * Clears any active save error message by setting [_saveError] to `null`.
     */
    fun clearSaveError() {
        _saveError.value = null
    }
}

/**
 * Factory for creating instances of [CreateEditDiceSetViewModel].
 *
 * This factory is necessary because [CreateEditDiceSetViewModel] has constructor dependencies
 * ([Application] and `diceSetId`) that need to be provided during ViewModel creation.
 *
 * @param application The application context.
 * @param diceSetId The ID of the [DiceSet] to edit, or `null` to create a new one.
 */
class CreateEditDiceSetViewModelFactory(
    private val application: Application,
    private val diceSetId: Long?
) : ViewModelProvider.Factory {
    /**
     * Creates a new instance of the given `modelClass`.
     *
     * @param T The type of the ViewModel to create.
     * @param modelClass A class whose instance is requested.
     * @return A newly created ViewModel.
     * @throws IllegalArgumentException if `modelClass` is not assignable from [CreateEditDiceSetViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateEditDiceSetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateEditDiceSetViewModel(application, diceSetId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for CreateEditDiceSetViewModel")
    }
}