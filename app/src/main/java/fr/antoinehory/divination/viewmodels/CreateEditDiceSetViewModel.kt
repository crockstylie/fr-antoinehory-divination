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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

/**
 * Maximum count for a single type of dice in a configuration within the ViewModel.
 */
const val MAX_DICE_COUNT_VM = 1000

/**
 * Maximum total count of all dice within a single dice set.
 */
const val MAX_TOTAL_DICE_IN_SET = 1000 // New constant for total dice limit

/**
 * Sealed interface for UI events that the ViewModel can send to the Screen.
 * These are typically one-time events.
 */
sealed interface UiEvent {
    /**
     * Event to show a toast message.
     * @property messageResId The resource ID of the string to display.
     * @property args Optional arguments for string formatting.
     */
    data class ShowToast(val messageResId: Int, val args: List<Any> = emptyList()) : UiEvent
}

/**
 * ViewModel for the screen used to create or edit a [DiceSet].
 *
 * This ViewModel manages the state of the dice set being created or edited,
 * including its name and the list of [DiceConfig]s. It handles loading an
 * existing dice set if an ID is provided, and saving the changes (either as a new
 * set or an update to an existing one). It also provides validation for saving
 * and manages the state of the dice configuration input section, including limits
 * on individual dice counts and the total number of dice in a set.
 *
 * @param application The application context, used for accessing resources (e.g., strings) and the database.
 * @param diceSetId The ID of the [DiceSet] to be edited. If `null`, a new dice set is being created.
 */
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

    private val _currentDiceTypeForInput = MutableStateFlow(DiceType.D6)
    val currentDiceTypeForInput: StateFlow<DiceType> = _currentDiceTypeForInput.asStateFlow()

    private val _currentDiceCountStringForInput = MutableStateFlow("1")
    val currentDiceCountStringForInput: StateFlow<String> = _currentDiceCountStringForInput.asStateFlow()

    private val _editingConfigIndex = MutableStateFlow<Int?>(null)
    val editingConfigIndex: StateFlow<Int?> = _editingConfigIndex.asStateFlow()

    private val _uiEvents = Channel<UiEvent>()
    val uiEvents: Flow<UiEvent> = _uiEvents.receiveAsFlow()

    private val _navigateBackEvent = Channel<Unit>()
    val navigateBackEvent: Flow<Unit> = _navigateBackEvent.receiveAsFlow()

    private var originalDiceSetForEdit: DiceSet? = null

    init {
        if (diceSetId != null) {
            loadDiceSet(diceSetId)
        } else {
            _setName.value = application.getString(R.string.default_new_dice_set_name)
            prepareForNewConfig()
        }
    }

    private fun loadDiceSet(id: Long) {
        viewModelScope.launch {
            val loadedSet = withContext(Dispatchers.IO) {
                diceSetDao.getDiceSetByIdBlocking(id)
            }
            loadedSet?.let { set ->
                originalDiceSetForEdit = set
                _setName.value = set.name
                _diceConfigs.value = set.diceConfigs
                prepareForNewConfig()
            }
        }
    }

    fun updateSetName(newName: String) {
        _setName.value = newName
        if (newName.isNotBlank() && _saveError.value == R.string.error_set_name_empty) {
            _saveError.value = null
        }
    }

    fun updateDiceTypeForInput(newType: DiceType) {
        _currentDiceTypeForInput.value = newType
    }

    fun updateDiceCountStringForInput(newString: String) {
        if (newString.isEmpty()) {
            _currentDiceCountStringForInput.value = ""
        } else if (newString.all { it.isDigit() }) {
            val countAsLong = newString.toLongOrNull()
            if (countAsLong != null) {
                _currentDiceCountStringForInput.value = if (countAsLong > MAX_DICE_COUNT_VM) {
                    MAX_DICE_COUNT_VM.toString()
                } else {
                    newString
                }
            } else {
                _currentDiceCountStringForInput.value = MAX_DICE_COUNT_VM.toString()
            }
        }
    }

    fun prepareForNewConfig() {
        _currentDiceTypeForInput.value = DiceType.D6
        _currentDiceCountStringForInput.value = "1"
        _editingConfigIndex.value = null
    }

    fun prepareToEditConfig(index: Int) {
        if (index >= 0 && index < _diceConfigs.value.size) {
            val configToEdit = _diceConfigs.value[index]
            _editingConfigIndex.value = index
            _currentDiceTypeForInput.value = configToEdit.diceType
            _currentDiceCountStringForInput.value = configToEdit.count.toString()
        }
    }

    fun cancelConfigInput() {
        prepareForNewConfig()
    }

    /**
     * Submits the current dice configuration from the input fields.
     * Validates individual dice count and total dice in set against limits.
     * If valid, adds or updates the configuration in the [_diceConfigs] list,
     * merging with existing configurations of the same type or clamping counts as needed.
     * Shows toast messages for validation errors via [UiEvent].
     * @return `true` if the submission was successful, `false` otherwise.
     */
    fun submitDiceConfig(): Boolean {
        val countString = _currentDiceCountStringForInput.value
        val count = countString.toIntOrNull()

        if (count == null || count <= 0) {
            viewModelScope.launch { _uiEvents.send(UiEvent.ShowToast(R.string.error_invalid_dice_count)) }
            return false
        }
        if (count > MAX_DICE_COUNT_VM) {
            viewModelScope.launch { _uiEvents.send(UiEvent.ShowToast(R.string.error_max_dice_count_exceeded, listOf(MAX_DICE_COUNT_VM))) }
            return false
        }

        val typeToSubmit = _currentDiceTypeForInput.value
        val indexBeingEdited = _editingConfigIndex.value

        // --- VALIDATION FOR THE TOTAL DICE IN SET ---
        // Simulate the list after the current config is added/updated to check total.
        val projectedList = _diceConfigs.value.toMutableList()
        if (indexBeingEdited != null) { // Editing existing config
            if (indexBeingEdited >= 0 && indexBeingEdited < projectedList.size) {
                val otherConfigWithNewTypeIndex = projectedList.indexOfFirst {
                    it.diceType == typeToSubmit && projectedList.indexOf(it) != indexBeingEdited
                }
                if (otherConfigWithNewTypeIndex != -1) { // Will merge
                    val targetConfig = projectedList[otherConfigWithNewTypeIndex]
                    val mergedCount = targetConfig.count + count
                    projectedList[otherConfigWithNewTypeIndex] = targetConfig.copy(count = mergedCount.coerceAtMost(MAX_DICE_COUNT_VM))
                    projectedList.removeAt(indexBeingEdited)
                } else { // Simple update
                    projectedList[indexBeingEdited] = projectedList[indexBeingEdited].copy(diceType = typeToSubmit, count = count)
                }
            }
        } else { // Adding a new config
            val existingConfigIndex = projectedList.indexOfFirst { it.diceType == typeToSubmit }
            if (existingConfigIndex != -1) { // Will merge
                val existingConfig = projectedList[existingConfigIndex]
                val newTotalCount = existingConfig.count + count
                projectedList[existingConfigIndex] = existingConfig.copy(count = newTotalCount.coerceAtMost(MAX_DICE_COUNT_VM))
            } else { // Add as new
                projectedList.add(DiceConfig(diceType = typeToSubmit, count = count))
            }
        }
        val potentialNewTotalDiceInSet = projectedList.sumOf { it.count }

        if (potentialNewTotalDiceInSet > MAX_TOTAL_DICE_IN_SET) {
            viewModelScope.launch { _uiEvents.send(UiEvent.ShowToast(R.string.error_max_dice_count_exceeded, listOf(MAX_TOTAL_DICE_IN_SET))) }
            return false
        }
        // --- END OF VALIDATION FOR TOTAL DICE IN SET ---

        // If all validations pass, update the actual list
        _diceConfigs.value = projectedList

        if (_saveError.value == R.string.error_no_dice_configs || _saveError.value == R.string.error_max_dice_count_exceeded) {
            _saveError.value = null // Clear relevant errors
        }
        prepareForNewConfig()
        return true
    }

    fun removeDiceConfig(index: Int) {
        _diceConfigs.update { currentList ->
            if (index >= 0 && index < currentList.size) {
                currentList.toMutableList().apply { removeAt(index) }
            } else {
                currentList
            }
        }
    }

    fun incrementDiceCount(index: Int) {
        _diceConfigs.update { currentList ->
            if (index >= 0 && index < currentList.size) {
                val config = currentList[index]
                val currentTotalWithoutThisConfig = currentList.filterIndexed { i, _ -> i != index }.sumOf { it.count }

                if (config.count < MAX_DICE_COUNT_VM && (currentTotalWithoutThisConfig + config.count + 1) <= MAX_TOTAL_DICE_IN_SET) {
                    currentList.toMutableList().apply { this[index] = config.copy(count = config.count + 1) }
                } else {
                    if (config.count >= MAX_DICE_COUNT_VM) {
                        viewModelScope.launch { _uiEvents.send(UiEvent.ShowToast(R.string.error_max_dice_count_exceeded, listOf(MAX_DICE_COUNT_VM))) }
                    } else {
                        viewModelScope.launch { _uiEvents.send(UiEvent.ShowToast(R.string.error_max_dice_count_exceeded, listOf(MAX_TOTAL_DICE_IN_SET))) }
                    }
                    currentList
                }
            } else {
                currentList
            }
        }
    }

    fun decrementDiceCount(index: Int) {
        _diceConfigs.update { currentList ->
            if (index >= 0 && index < currentList.size) {
                val config = currentList[index]
                if (config.count > 1) {
                    currentList.toMutableList().apply { this[index] = config.copy(count = config.count - 1) }
                } else {
                    currentList
                }
            } else {
                currentList
            }
        }
    }

    /**
     * Attempts to save the current dice set configuration.
     * Performs validation for set name, at least one dice configuration,
     * and the total number of dice in the set against [MAX_TOTAL_DICE_IN_SET].
     * If validation fails, [_saveError] is updated, and a toast may be shown.
     * On successful validation, inserts/updates the set in the database and sends a [navigateBackEvent].
     */
    fun saveDiceSet() {
        val nameToSave = _setName.value.trim()
        val configsToSave = _diceConfigs.value

        if (nameToSave.isBlank()) {
            _saveError.value = R.string.error_set_name_empty
            return
        }
        if (configsToSave.isEmpty()) {
            _saveError.value = R.string.error_no_dice_configs
            return
        }

        // --- VALIDATION FOR TOTAL DICE IN SET ON SAVE ---
        val totalDiceInSet = configsToSave.sumOf { it.count }
        if (totalDiceInSet > MAX_TOTAL_DICE_IN_SET) {
            _saveError.value = R.string.error_max_dice_count_exceeded
            // Send a toast as well, as _saveError might not be prominently displayed for this.
            viewModelScope.launch { _uiEvents.send(UiEvent.ShowToast(R.string.error_max_dice_count_exceeded, listOf(MAX_TOTAL_DICE_IN_SET))) }
            return
        }
        // --- END OF VALIDATION FOR TOTAL DICE IN SET ON SAVE ---

        _saveError.value = null // Clear any previous errors.

        viewModelScope.launch {
            val isNewSet = diceSetId == null
            val setForDatabaseOperations = if (isNewSet) {
                DiceSet(name = nameToSave, diceConfigs = configsToSave, isFavorite = false)
            } else {
                originalDiceSetForEdit!!.copy(
                    name = nameToSave,
                    diceConfigs = configsToSave
                )
            }

            withContext(Dispatchers.IO) {
                if (isNewSet) {
                    diceSetDao.insert(setForDatabaseOperations)
                } else {
                    diceSetDao.update(setForDatabaseOperations)
                }
            }
            _navigateBackEvent.send(Unit)
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
        throw IllegalArgumentException("Unknown ViewModel class for CreateEditDiceSetViewModelFactory: ${modelClass.name}")
    }
}
