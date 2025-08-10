package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.data.database.AppDatabase
import fr.antoinehory.divination.data.database.dao.DiceSetDao
import fr.antoinehory.divination.data.model.DiceConfig
import fr.antoinehory.divination.data.model.DiceSet
import fr.antoinehory.divination.data.model.DiceType
import fr.antoinehory.divination.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing user-defined dice sets.
 *
 * This ViewModel handles CRUD operations for [DiceSet] entities, manages the currently active dice set,
 * and allows users to mark dice sets as favorites. It interacts with [DiceSetDao] for database
 * operations and [UserPreferencesRepository] to store the active dice set ID.
 *
 * @param application The application context, used for initializing the database.
 * @param userPreferencesRepository The repository for managing user preferences,
 *                                  specifically the active dice set ID.
 */
class DiceSetViewModel(
    application: Application,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val diceSetDao: DiceSetDao = AppDatabase.getDatabase(application).diceSetDao()

    /**
     * Internal [MutableStateFlow] holding the [DiceSet] for which delete confirmation is requested.
     * `null` if no delete confirmation is active.
     */
    private val _diceSetToDeleteConfirm = MutableStateFlow<DiceSet?>(null)
    /**
     * Public [StateFlow] exposing the [DiceSet] awaiting delete confirmation.
     * Used by the UI to show a confirmation dialog.
     */
    val diceSetToDeleteConfirm: StateFlow<DiceSet?> = _diceSetToDeleteConfirm.asStateFlow()

    /**
     * Internal [MutableStateFlow] holding the [DiceSet] for which copy confirmation is requested.
     * `null` if no copy confirmation is active.
     */
    private val _diceSetToCopyConfirm = MutableStateFlow<DiceSet?>(null)
    /**
     * Public [StateFlow] exposing the [DiceSet] awaiting copy confirmation.
     * Used by the UI to show a confirmation dialog for copying.
     */
    val diceSetToCopyConfirm: StateFlow<DiceSet?> = _diceSetToCopyConfirm.asStateFlow()

    /**
     * Internal [MutableStateFlow] holding the [DiceSet] for which 'set active' confirmation is requested.
     * `null` if no 'set active' confirmation is active.
     */
    private val _diceSetToSetActiveConfirm = MutableStateFlow<DiceSet?>(null)
    /**
     * Public [StateFlow] exposing the [DiceSet] awaiting 'set active' confirmation.
     * Used by the UI to show a confirmation dialog before changing the active set.
     */
    val diceSetToSetActiveConfirm: StateFlow<DiceSet?> = _diceSetToSetActiveConfirm.asStateFlow()


    /**
     * A [StateFlow] emitting a list of all [DiceSet]s stored in the database.
     * The flow is collected while the subscriber is active (with a 5-second timeout)
     * and starts with an empty list.
     */
    val allDiceSets: StateFlow<List<DiceSet>> = diceSetDao.getAllDiceSets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * A [StateFlow] emitting a list of all favorite [DiceSet]s.
     * The flow is collected while the subscriber is active (with a 5-second timeout)
     * and starts with an empty list.
     */
    val favoriteDiceSets: StateFlow<List<DiceSet>> = diceSetDao.getFavoriteDiceSets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Initializes the ViewModel.
     * It launches a coroutine to check if an active dice set ID is stored in preferences.
     * If no active set ID is found and there are no dice sets in the database,
     * it calls [createAndActivateDefaultSet] to create a default "Quick Roll" set.
     */
    init {
        viewModelScope.launch {
            val activeSetId = userPreferencesRepository.activeDiceSetIdFlow.firstOrNull()
            if (activeSetId == null) {
                // Check if allDiceSets has loaded and is empty.
                // Using first() to ensure we get a loaded value before proceeding.
                if (allDiceSets.firstOrNull()?.isEmpty() == true) {
                    createAndActivateDefaultSet()
                }
            }
        }
    }

    /**
     * Creates a default "Quick Roll" dice set (1x D6) and sets it as the active dice set.
     * This is typically called on first app launch or if all dice sets are deleted
     * and no active set is configured.
     */
    private suspend fun createAndActivateDefaultSet() {
        val defaultDiceConfigs = listOf(DiceConfig(diceType = DiceType.D6, count = 1))
        val defaultSet = DiceSet(
            name = "Quick Roll",
            diceConfigs = defaultDiceConfigs,
            isFavorite = false
        )
        val newSetId = diceSetDao.insert(defaultSet)
        if (newSetId > 0L) { // Check if insertion was successful
            userPreferencesRepository.setActiveDiceSetId(newSetId)
            println("Default dice set 'Quick Roll' created and activated with ID: $newSetId")
        } else {
            println("Failed to create or activate the default dice set.")
        }
    }

    /**
     * Requests confirmation from the user before deleting a [DiceSet].
     * Sets [_diceSetToDeleteConfirm] to the specified dice set, triggering UI display.
     * @param diceSet The [DiceSet] to be marked for delete confirmation.
     */
    fun requestDeleteConfirmation(diceSet: DiceSet) {
        _diceSetToDeleteConfirm.value = diceSet
    }

    /**
     * Cancels an ongoing delete confirmation request.
     * Resets [_diceSetToDeleteConfirm] to `null`.
     */
    fun cancelDeleteConfirmation() {
        _diceSetToDeleteConfirm.value = null
    }

    /**
     * Requests confirmation from the user before copying a [DiceSet].
     * Sets [_diceSetToCopyConfirm] to the specified dice set.
     * @param diceSet The [DiceSet] to be marked for copy confirmation.
     */
    fun requestCopyConfirmation(diceSet: DiceSet) {
        _diceSetToCopyConfirm.value = diceSet
    }

    /**
     * Cancels an ongoing copy confirmation request.
     * Resets [_diceSetToCopyConfirm] to `null`.
     */
    fun cancelCopyConfirmation() {
        _diceSetToCopyConfirm.value = null
    }

    /**
     * Confirms and performs the copy operation for the given [DiceSet].
     * A new [DiceSet] is created with "(Copy)" appended to its name and `isFavorite` set to false.
     * The new set is then inserted into the database.
     * Resets [_diceSetToCopyConfirm] to `null` after the operation.
     * @param diceSetToCopy The [DiceSet] to copy.
     */
    fun confirmAndCopyDiceSet(diceSetToCopy: DiceSet) {
        viewModelScope.launch {
            val newName = "${diceSetToCopy.name} (Copy)"
            val newSet = diceSetToCopy.copy(
                id = 0, // Ensure a new ID is generated by Room
                name = newName,
                isFavorite = false
            )
            diceSetDao.insert(newSet)
            _diceSetToCopyConfirm.value = null
        }
    }

    /**
     * Requests confirmation from the user before setting a [DiceSet] as active.
     * Sets [_diceSetToSetActiveConfirm] to the specified dice set.
     * @param diceSet The [DiceSet] to be marked for 'set active' confirmation.
     */
    fun requestSetActiveConfirmation(diceSet: DiceSet) {
        _diceSetToSetActiveConfirm.value = diceSet
    }

    /**
     * Cancels an ongoing 'set active' confirmation request.
     * Resets [_diceSetToSetActiveConfirm] to `null`.
     */
    fun cancelSetActiveConfirmation() {
        _diceSetToSetActiveConfirm.value = null
    }

    /**
     * Confirms and sets the given [DiceSet] as the active dice set.
     * The dice set's ID is stored in [UserPreferencesRepository].
     * Does nothing and prints an error if the [DiceSet] ID is 0 (invalid for an existing set).
     * Resets [_diceSetToSetActiveConfirm] to `null` after the operation.
     * @param diceSetToActivate The [DiceSet] to set as active.
     */
    fun confirmSetActiveDiceSet(diceSetToActivate: DiceSet) {
        if (diceSetToActivate.id != 0L) { // Ensure the dice set has a valid ID
            viewModelScope.launch {
                userPreferencesRepository.setActiveDiceSetId(diceSetToActivate.id)
                _diceSetToSetActiveConfirm.value = null
            }
        } else {
            // Log error or handle appropriately if trying to set an unsaved DiceSet as active
            println("Error: Cannot set DiceSet with ID 0 as active.")
            _diceSetToSetActiveConfirm.value = null // Still dismiss confirmation
        }
    }

    /**
     * Adds a new [DiceSet] to the database.
     * @param name The name for the new dice set.
     * @param diceConfigs A list of [DiceConfig] for the new dice set.
     */
    fun addDiceSet(name: String, diceConfigs: List<DiceConfig>) {
        viewModelScope.launch {
            val newDiceSet = DiceSet(name = name, diceConfigs = diceConfigs, isFavorite = false)
            diceSetDao.insert(newDiceSet)
        }
    }

    /**
     * Updates an existing [DiceSet] in the database.
     * @param diceSet The [DiceSet] with updated information to be persisted.
     */
    fun updateDiceSet(diceSet: DiceSet) {
        viewModelScope.launch {
            diceSetDao.update(diceSet)
        }
    }

    /**
     * Deletes a [DiceSet] from the database.
     * Also clears any pending delete confirmation for this specific dice set.
     * @param diceSet The [DiceSet] to be deleted.
     */
    fun deleteDiceSet(diceSet: DiceSet) {
        viewModelScope.launch {
            diceSetDao.delete(diceSet)
            // If the deleted set was the one pending confirmation, clear the confirmation state.
            if (_diceSetToDeleteConfirm.value?.id == diceSet.id) {
                _diceSetToDeleteConfirm.value = null
            }
        }
    }

    /**
     * Toggles the favorite status of a [DiceSet].
     * If the set is currently a favorite, it will be marked as not favorite, and vice-versa.
     * The updated set is then persisted to the database.
     * @param diceSet The [DiceSet] whose favorite status is to be toggled.
     */
    fun toggleFavoriteStatus(diceSet: DiceSet) {
        viewModelScope.launch {
            val updatedSet = diceSet.copy(isFavorite = !diceSet.isFavorite)
            diceSetDao.update(updatedSet)
        }
    }
}

/**
 * Factory for creating instances of [DiceSetViewModel].
 *
 * This factory is necessary because [DiceSetViewModel] has constructor dependencies
 * ([Application] and [UserPreferencesRepository]) that need to be provided during ViewModel creation.
 *
 * @param application The application context.
 * @param userPreferencesRepository The repository for user preferences.
 */
class DiceSetViewModelFactory(
    private val application: Application,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    /**
     * Creates a new instance of the given `modelClass`.
     *
     * @param modelClass A class whose instance is requested.
     * @return A newly created ViewModel.
     * @throws IllegalArgumentException if `modelClass` is not assignable from [DiceSetViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiceSetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiceSetViewModel(application, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}