package fr.antoinehory.divination.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.data.repository.LaunchLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the settings screen.
 *
 * This ViewModel handles the logic related to application settings, such as
 * managing the clearing of game statistics. It interacts with [LaunchLogRepository]
 * to perform data operations.
 *
 * @property launchLogRepository The repository for accessing and modifying launch log data.
 */
class SettingsViewModel(
    private val launchLogRepository: LaunchLogRepository
) : ViewModel() {

    /**
     * Internal [MutableStateFlow] to control the visibility of the clear confirmation dialog.
     */
    private val _showClearConfirmationDialog = MutableStateFlow(false)
    /**
     * Public [StateFlow] exposing the visibility state of the clear confirmation dialog.
     * `true` if the dialog should be shown, `false` otherwise.
     */
    val showClearConfirmationDialog: StateFlow<Boolean> = _showClearConfirmationDialog.asStateFlow()

    /**
     * Internal [MutableStateFlow] to store the game type selected for clearing statistics.
     * A `null` value indicates that all game statistics are targeted for clearing.
     */
    private val _selectedGameForClear = MutableStateFlow<GameType?>(null)
    /**
     * Public [StateFlow] exposing the currently selected game type for clearing statistics.
     * Can be `null` if the option to clear all stats is selected or no selection has been made.
     */
    val selectedGameOrOptionForClear: StateFlow<GameType?> = _selectedGameForClear.asStateFlow()


    /**
     * A list of all available game types that can have their statistics cleared.
     * Sourced from [GameType.entries].
     */
    val availableGameTypes: List<GameType> = GameType.entries

    /**
     * Updates the selected game type for which statistics will be cleared.
     *
     * @param gameType The [GameType] selected by the user, or `null` if "clear all" is chosen.
     */
    fun onGameOrOptionSelectedForClear(gameType: GameType?) {
        _selectedGameForClear.value = gameType
    }

    /**
     * Initiates the process of clearing statistics by showing the confirmation dialog.
     * This is typically called when the user clicks a "Clear Stats" button.
     */
    fun onClearSelectedStatsClicked() {
        _showClearConfirmationDialog.value = true
    }

    /**
     * Confirms the action to clear selected game statistics.
     *
     * If a specific [GameType] is selected (i.e., [_selectedGameForClear.value] is not null),
     * only logs for that game type are deleted. Otherwise, all launch logs are deleted.
     * After the operation, the confirmation dialog is hidden and the selection is reset.
     */
    fun onConfirmClearSelectedStats() {
        viewModelScope.launch {
            val selection = _selectedGameForClear.value
            if (selection == null) {
                launchLogRepository.deleteAllLogs()
            } else {
                launchLogRepository.deleteLogsByGameType(selection)
            }
        }
        _showClearConfirmationDialog.value = false
        _selectedGameForClear.value = null
    }

    /**
     * Dismisses the clear confirmation dialog without performing any action.
     * This is typically called when the user cancels the clear operation.
     */
    fun onDismissClearConfirmationDialog() {
        _showClearConfirmationDialog.value = false
    }
}

/**
 * Factory for creating instances of [SettingsViewModel].
 *
 * This factory is necessary because [SettingsViewModel] has a constructor dependency
 * on [LaunchLogRepository], which needs to be provided during ViewModel creation.
 *
 * @property launchLogRepository The repository instance to be injected into the [SettingsViewModel].
 */
class SettingsViewModelFactory(
    private val launchLogRepository: LaunchLogRepository
) : ViewModelProvider.Factory {
    /**
     * Creates a new instance of the given `modelClass`.
     *
     * @param modelClass A class whose instance is requested.
     * @return A newly created ViewModel.
     * @throws IllegalArgumentException if `modelClass` is not assignable from [SettingsViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(launchLogRepository /*, application */) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

