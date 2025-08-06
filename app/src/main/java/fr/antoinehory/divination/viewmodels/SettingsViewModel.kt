package fr.antoinehory.divination.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.data.repository.LaunchLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val launchLogRepository: LaunchLogRepository) : ViewModel() {

    private val _showClearStatsConfirmationDialog = MutableStateFlow(false)
    val showClearStatsConfirmationDialog: StateFlow<Boolean> = _showClearStatsConfirmationDialog.asStateFlow()

    fun onClearStatsClicked() {
        _showClearStatsConfirmationDialog.value = true
    }

    fun onConfirmClearStats() {
        viewModelScope.launch {
            launchLogRepository.deleteAllLogs()
            _showClearStatsConfirmationDialog.value = false
            // Optionnel : Afficher un Toast ou un Snackbar pour confirmer la suppression
        }
    }

    fun onDismissClearStatsDialog() {
        _showClearStatsConfirmationDialog.value = false
    }
}

// Factory pour SettingsViewModel
class SettingsViewModelFactory(
    private val launchLogRepository: LaunchLogRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(launchLogRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
